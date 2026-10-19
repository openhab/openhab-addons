/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.e3dc.internal.rscp;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A synchronous RSCP connection to an E3/DC device.
 *
 * <p>
 * The connection is a plain TCP socket; every frame is encrypted with Rijndael-256 in CBC mode, so
 * the chaining state belongs to the connection and a new socket needs a fresh {@link RscpCipher}.
 * Authentication happens inside the encrypted channel with the credentials of the E3/DC cloud account.
 *
 * <p>
 * This class is not thread safe. Callers must serialize access, because request and response are not
 * correlated by an identifier.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class RscpClient implements AutoCloseable {

    public static final int DEFAULT_PORT = 5033;

    private final Logger logger = LoggerFactory.getLogger(RscpClient.class);

    private final String host;
    private final int port;
    private final String user;
    private final String password;
    private final byte[] key;
    private final int timeoutMillis;

    /** Time spent on the wire by all requests of this connection, from sending to the complete answer. */
    private final AtomicLong busyNanos = new AtomicLong();

    private volatile long lastWireNanos;
    private volatile int lastAnswerBytes;

    /** Time the device needed to start its answer, measured for the trace of the last request. */
    private volatile long lastDeviceWaitNanos;

    private @Nullable Socket socket;
    private @Nullable InputStream input;
    private @Nullable OutputStream output;
    private @Nullable RscpCipher cipher;

    public RscpClient(String host, int port, String user, String password, String rscpPassword, int timeoutMillis) {
        this.host = host;
        this.port = port;
        this.user = user;
        this.password = password;
        this.key = rscpPassword.getBytes(StandardCharsets.UTF_8);
        this.timeoutMillis = timeoutMillis;
    }

    /** Opens the socket and authenticates. */
    public void connect() throws RscpException {
        close();
        logger.debug("Connecting to {}:{} with a timeout of {} ms", host, port, timeoutMillis);
        try {
            Socket newSocket = new Socket();
            newSocket.connect(new InetSocketAddress(host, port), timeoutMillis);
            newSocket.setSoTimeout(timeoutMillis);
            newSocket.setTcpNoDelay(true);
            this.socket = newSocket;
            this.input = newSocket.getInputStream();
            this.output = newSocket.getOutputStream();
            this.cipher = new RscpCipher(key);
        } catch (IOException e) {
            close();
            throw new RscpException("Cannot connect to " + host + ":" + port, e);
        }
        Socket connected = socket;
        logger.debug("Connected to {}:{} from local port {}. Authenticating user '{}' (password and RSCP key are"
                + " not logged)", host, port, connected != null ? connected.getLocalPort() : -1, user);

        RscpValue authentication = RscpValue.container(RscpTag.RSCP_REQ_AUTHENTICATION,
                List.of(RscpValue.cstring(RscpTag.RSCP_AUTHENTICATION_USER, user),
                        RscpValue.cstring(RscpTag.RSCP_AUTHENTICATION_PASSWORD, password)));
        RscpValue response;
        try {
            response = request(List.of(authentication));
        } catch (RscpException e) {
            close();
            throw new RscpException(explainHandshakeFailure(e), e);
        }
        RscpError error = response.findError();
        if (error != null) {
            close();
            if (error == RscpError.ACCESS_DENIED) {
                throw new RscpException("The device rejected the credentials (access denied). The user name or the"
                        + " password does not match the E3/DC cloud account.");
            }
            throw new RscpException("Authentication failed with " + error);
        }
        logger.debug("Authentication succeeded, the device answered with {}", response);
    }

    /**
     * Turns a failure during the handshake into a message that names the likely cause.
     *
     * <p>
     * The two failures look alike in a log but have different causes: a timeout means that nothing arrived
     * at all, while a connection that closes without a single byte usually means the device could not
     * decrypt the request, which points at the RSCP key rather than at the credentials.
     */
    private String explainHandshakeFailure(RscpException cause) {
        String detail = cause.getMessage();
        if (detail == null) {
            detail = cause.toString();
        }
        Throwable root = cause.getCause() != null ? cause.getCause() : cause;
        if (root instanceof SocketTimeoutException) {
            return "No answer from " + host + ":" + port + " within " + timeoutMillis + " ms. The host answered on"
                    + " the TCP level but the RSCP interface stayed silent; check the port and whether another"
                    + " client still holds the connection. (" + detail + ")";
        }
        if (root instanceof EOFException || detail.contains("closed after 0 of")) {
            return "The device closed the connection without sending a single byte. It most likely could not"
                    + " decrypt the request, so the RSCP password probably does not match the one set on the"
                    + " device. (" + detail + ")";
        }
        return detail;
    }

    public boolean isConnected() {
        Socket current = socket;
        return current != null && current.isConnected() && !current.isClosed();
    }

    /**
     * Sends the elements and returns the decoded answer.
     *
     * <p>
     * A device side error item for a requested tag is not treated as a failure: it is returned so the
     * caller can distinguish an unsupported data point from a failed request.
     *
     * <p>
     * The device answers with exactly one frame that carries all elements, so one frame is read. Reading
     * further frames would consume the answer of the next request, because the device replies about a
     * second later while a follow-up read waits only a fraction of that.
     *
     * <p>
     * Requests are sent one at a time. The poll and a command arrive on different threads but share one
     * socket and one cipher whose chaining state moves with every frame, so two requests in flight would
     * read each other's answer, corrupt that state, and both end in a timeout.
     */
    public synchronized RscpValue request(List<RscpValue> requestItems) throws RscpException {
        long started = System.nanoTime();
        byte[] frame;
        try {
            frame = RscpCodec.encodeFrame(requestItems);
        } catch (IOException e) {
            throw new RscpException("Cannot encode the request: " + e.getMessage(), e);
        }
        long encoded = System.nanoTime();
        send(frame);
        long sent = System.nanoTime();
        byte[] plainResponse = receive();
        long received0 = System.nanoTime();
        busyNanos.addAndGet(received0 - encoded);
        lastWireNanos = received0 - encoded;
        lastAnswerBytes = plainResponse.length;
        List<RscpValue> received = RscpCodec.decodeFrame(plainResponse);
        long decoded = System.nanoTime();
        if (logger.isTraceEnabled()) {
            // the device wait is the time until the first block of the answer arrives; it is what the device
            // needs to collect the data, the transfer afterwards depends on the size of the answer
            logger.trace(
                    "Timing of one request: encode {} ms, send {} ms, device wait {} ms, "
                            + "transfer and decrypt {} ms, decode {} ms, total {} ms; "
                            + "{} elements sent as {} bytes, answer of {} bytes",
                    millis(encoded - started), millis(sent - encoded), millis(lastDeviceWaitNanos),
                    millis(received0 - sent - lastDeviceWaitNanos), millis(decoded - received0),
                    millis(decoded - started), requestItems.size(), frame.length, plainResponse.length);
        }
        if (received.isEmpty()) {
            throw new RscpException("The device sent an empty response");
        }
        logger.debug("Request of {} elements answered with {} elements", requestItems.size(), received.size());
        RscpValue answer = received.size() == 1 ? received.get(0) : RscpValue.container(0, received);
        // the tree is a text of several hundred lines, so it is only built when it is logged
        if (logger.isTraceEnabled()) {
            logger.trace("Answer: {}", answer.describeTree());
        }
        return answer;
    }

    /**
     * The time all requests of this connection spent on the wire so far, in nanoseconds.
     *
     * <p>
     * Each request counts from sending to the complete answer, which is the time the device is occupied by
     * this client. Polls, commands and fetches all go through the same connection, so they are all included.
     */
    public long getBusyNanos() {
        return busyNanos.get();
    }

    /** Time the last request spent on the wire, from sending to the complete answer, in nanoseconds. */
    public long getLastWireNanos() {
        return lastWireNanos;
    }

    /** Time the device needed to start the answer to the last request, in nanoseconds. */
    public long getLastDeviceWaitNanos() {
        return lastDeviceWaitNanos;
    }

    /** Size of the answer to the last request in bytes, as plain frame. */
    public int getLastAnswerBytes() {
        return lastAnswerBytes;
    }

    private static String millis(long nanos) {
        return String.format("%.1f", nanos / 1_000_000.0);
    }

    /** Requests one tag, which is a top level element of the frame. */
    public RscpValue requestTag(int tag) throws RscpException {
        return request(List.of(RscpValue.none(tag)));
    }

    private void send(byte[] frame) throws RscpException {
        RscpCipher currentCipher = cipher;
        OutputStream currentOutput = output;
        if (currentCipher == null || currentOutput == null) {
            throw new RscpException("Not connected");
        }
        try {
            byte[] encrypted = currentCipher.encrypt(frame);
            currentOutput.write(encrypted);
            currentOutput.flush();
            logger.trace("Sent {} plain bytes, {} encrypted bytes", frame.length, encrypted.length);
        } catch (IOException e) {
            throw new RscpException("Cannot send the request: " + e.getMessage(), e);
        }
    }

    /**
     * Reads one encrypted frame.
     *
     * <p>
     * The whole frame is buffered before decryption because CBC chains across blocks: decrypting a
     * prefix twice would advance the chaining state twice and corrupt the stream. The plain header is
     * only known after the first block, which is probed with a throwaway cipher state.
     */
    private byte[] receive() throws RscpException {
        RscpCipher currentCipher = cipher;
        InputStream currentInput = input;
        if (currentCipher == null || currentInput == null) {
            throw new RscpException("Not connected");
        }
        try {
            int needed = Rijndael256.BLOCK;
            int totalPlain = -1;
            ByteArrayOutputStream raw = new ByteArrayOutputStream();
            long waitStarted = System.nanoTime();
            boolean firstBlock = true;
            while (true) {
                if (raw.size() < needed) {
                    byte[] chunk = RscpCodec.readFully(currentInput, needed - raw.size());
                    raw.write(chunk, 0, chunk.length);
                    if (firstBlock) {
                        lastDeviceWaitNanos = System.nanoTime() - waitStarted;
                        firstBlock = false;
                    }
                }
                byte[] encrypted = raw.toByteArray();
                if (totalPlain < 0) {
                    totalPlain = plainFrameLength(encrypted);
                    needed = ((totalPlain + Rijndael256.BLOCK - 1) / Rijndael256.BLOCK) * Rijndael256.BLOCK;
                }
                if (encrypted.length >= needed) {
                    break;
                }
            }
            byte[] plain = currentCipher.decrypt(raw.toByteArray());
            logger.trace("Received {} encrypted bytes for a plain frame of {} bytes", raw.size(), totalPlain);
            return java.util.Arrays.copyOf(plain, totalPlain);
        } catch (IOException e) {
            throw new RscpException("Cannot read the response: " + e.getMessage(), e);
        }
    }

    /**
     * Derives the plaintext frame length from a buffer holding at least the first cipher block,
     * without touching the connection's chaining state.
     */
    private int plainFrameLength(byte[] encrypted) throws RscpException {
        RscpCipher currentCipher = cipher;
        if (currentCipher == null) {
            throw new RscpException("Not connected");
        }
        byte[] probe = currentCipher.peek(encrypted);
        if (probe.length < RscpCodec.HEADER_LENGTH) {
            throw new RscpException("The response header is incomplete");
        }
        // the flag sits in the second control byte, index 2 is zero
        int ctrl = probe[3] & 0xFF;
        int dataLength = ByteBuffer.wrap(probe, 16, 2).order(ByteOrder.LITTLE_ENDIAN).getShort() & 0xFFFF;
        return RscpCodec.HEADER_LENGTH + dataLength + ((ctrl & RscpCodec.CTRL_CRC) != 0 ? 4 : 0);
    }

    @Override
    public void close() {
        Socket current = socket;
        socket = null;
        input = null;
        output = null;
        cipher = null;
        if (current != null) {
            try {
                current.close();
            } catch (IOException e) {
                // closing a socket is best effort
            }
        }
    }
}
