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
import java.io.InterruptedIOException;
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
 * A synchronous RSCP connection to an E3DC device. The socket is plain TCP; every frame is encrypted with
 * Rijndael-256 in CBC mode, so the chaining state belongs to the connection and a new socket needs a fresh
 * {@link RscpCipher}. Requests are serialized inside {@link #request(List)} because request and response are
 * not correlated by an identifier, so two requests in flight would read each other's answer and corrupt that
 * state. The timing figures are written by the requesting thread and read by the poll, so they are
 * {@code volatile}.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class RscpClient implements AutoCloseable {

    public static final int DEFAULT_PORT = 5033;

    /** The access level the account has to reach; zero means the device did not accept the login. */
    private static final int MIN_ACCESS_LEVEL = 10;

    private final Logger logger = LoggerFactory.getLogger(RscpClient.class);

    private final String host;
    private final int port;
    private final String user;
    private final String password;
    private final byte[] key;
    private final int timeoutMillis;

    private final AtomicLong busyNanos = new AtomicLong();

    private volatile long lastWireNanos;
    private volatile int lastAnswerBytes;

    private volatile long lastDeviceWaitNanos;

    // closing happens on another thread than the requests, so the connection state has to be visible to both
    private volatile @Nullable Socket socket;
    private volatile @Nullable InputStream input;
    private volatile @Nullable OutputStream output;
    private volatile @Nullable RscpCipher cipher;

    public RscpClient(String host, int port, String user, String password, String rscpPassword, int timeoutMillis) {
        this.host = host;
        this.port = port;
        this.user = user;
        this.password = password;
        this.key = rscpPassword.getBytes(StandardCharsets.UTF_8);
        this.timeoutMillis = timeoutMillis;
    }

    public void connect() throws RscpException {
        close();
        logger.debug("Connecting to {}:{} with a timeout of {} ms", host, port, timeoutMillis);
        try {
            Socket opened = openSocket();
            this.input = opened.getInputStream();
            this.output = opened.getOutputStream();
            this.cipher = new RscpCipher(key);
        } catch (IOException e) {
            restoreInterrupt(e);
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
        int accessLevel = accessLevelOf(response);
        if (accessLevel < MIN_ACCESS_LEVEL) {
            // a wrong login is answered with an access level of zero rather than with an error item
            close();
            throw new RscpException("The device accepted the connection but not the login: the access level is "
                    + accessLevel + ". Check the user name and password of the E3/DC account and the RSCP password.");
        }
        logger.debug("Authentication succeeded, the device answered with access level {}", accessLevel);
    }

    /**
     * Opens the TCP connection and returns the connected socket. The socket is published in the socket field
     * before the blocking connect, so a close on another thread closes it and aborts a pending attempt; an
     * attempt that fails closes its socket before the exception leaves.
     */
    private Socket openSocket() throws IOException {
        Socket attempt = createSocket();
        // close() from another thread has to reach the socket while the connect is still blocking
        this.socket = attempt;
        try {
            attempt.connect(new InetSocketAddress(host, port), timeoutMillis);
            attempt.setSoTimeout(timeoutMillis);
            attempt.setTcpNoDelay(true);
            return attempt;
        } catch (IOException e) {
            // a concurrent close() may already have taken the socket out of the field, so it is closed here as well
            closeQuietly(attempt);
            throw e;
        }
    }

    /** Creates the socket of a connection attempt; package-private so a test can hand in a socket double. */
    Socket createSocket() throws IOException {
        return new Socket();
    }

    private static int accessLevelOf(RscpValue response) {
        RscpValue level = response.find(RscpTag.RSCP_AUTHENTICATION);
        return level != null && level.getValue() instanceof Number number ? number.intValue() : 0;
    }

    /**
     * Turns a handshake failure into a message naming the likely cause: a timeout means nothing arrived,
     * while a connection closing without a single byte usually means the device could not decrypt the
     * request, which points at the RSCP key rather than at the credentials.
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
        if (root instanceof EOFException) {
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
     * Sends the elements and returns the decoded answer. A device side error item is returned rather than
     * treated as a failure, so the caller can tell an unsupported data point from a failed request; exactly
     * one frame is read, because further frames would be the answer of the next request.
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
            // the device wait is the time until the first block arrives; the rest depends on the answer size
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
     * The time all requests of this connection spent on the wire so far in nanoseconds, from sending to the
     * complete answer.
     */
    public long getBusyNanos() {
        return busyNanos.get();
    }

    public long getLastWireNanos() {
        return lastWireNanos;
    }

    public long getLastDeviceWaitNanos() {
        return lastDeviceWaitNanos;
    }

    public int getLastAnswerBytes() {
        return lastAnswerBytes;
    }

    private static String millis(long nanos) {
        return String.format("%.1f", nanos / 1_000_000.0);
    }

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
            restoreInterrupt(e);
            throw new RscpException("Cannot send the request: " + e.getMessage(), e);
        }
    }

    /**
     * Reads one encrypted frame. The whole frame is buffered before decryption because CBC chains across
     * blocks: decrypting a prefix twice would advance the chaining state twice and corrupt the stream; the
     * plain header is probed with a throwaway cipher state.
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
            restoreInterrupt(e);
            throw new RscpException("Cannot read the response: " + e.getMessage(), e);
        }
    }

    /**
     * Keeps an interruption visible to the caller. A {@link SocketTimeoutException} is also an
     * {@link InterruptedIOException}, but it only reports that the device stayed silent; any other
     * {@link InterruptedIOException} means the thread was interrupted, so the flag is set again.
     */
    private static void restoreInterrupt(IOException e) {
        if (e instanceof InterruptedIOException && !(e instanceof SocketTimeoutException)) {
            Thread.currentThread().interrupt();
        }
    }

    /** Derives the plaintext frame length from the first cipher block without touching the chaining state. */
    private int plainFrameLength(byte[] encrypted) throws RscpException {
        RscpCipher currentCipher = cipher;
        if (currentCipher == null) {
            throw new RscpException("Not connected");
        }
        byte[] probe = currentCipher.peek(encrypted);
        if (probe.length < RscpCodec.HEADER_LENGTH) {
            throw new RscpException("The response header is incomplete");
        }
        // anything but the magic means the stream is out of sync or the RSCP key is wrong, so stop before the
        // length of a garbage header makes this read tens of kilobytes
        if (probe[0] != RscpCodec.MAGIC_BYTES[0] || probe[1] != RscpCodec.MAGIC_BYTES[1]) {
            throw new RscpException(String.format("Not an RSCP response, magic was %02X %02X. The RSCP password may"
                    + " not match the one set on the device.", probe[0], probe[1]));
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
            closeQuietly(current);
        }
    }

    private void closeQuietly(Socket toClose) {
        try {
            toClose.close();
        } catch (IOException e) {
            // nothing is left to do for a socket that fails to close, the connection is dropped anyway
            logger.debug("Closing the connection to {}:{} failed: {}", host, port, e.getMessage());
        }
    }
}
