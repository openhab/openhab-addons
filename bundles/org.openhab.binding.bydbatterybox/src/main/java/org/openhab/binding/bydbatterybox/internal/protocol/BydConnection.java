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
package org.openhab.binding.bydbatterybox.internal.protocol;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.HexFormat;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bydbatterybox.internal.protocol.BydCommunicationException.Reason;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * One TCP session with the battery. The battery accepts a single client only, so a session is opened for one polling
 * cycle and closed again right after it.
 * <p>
 * {@link #close()} may be called from another thread to abort a blocking read.
 *
 * @author Michael Weger - Initial contribution
 */
@NonNullByDefault
public class BydConnection implements AutoCloseable {

    private static final int MAX_FRAME_LENGTH = 3 + 255 + 2;

    private final Logger logger = LoggerFactory.getLogger(BydConnection.class);
    private final Socket socket;
    private final int replyTimeoutMillis;
    private boolean anyReplyReceived;

    private BydConnection(Socket socket, int replyTimeoutMillis) {
        this.socket = socket;
        this.replyTimeoutMillis = replyTimeoutMillis;
    }

    /**
     * Opens a session.
     *
     * @param timeoutMillis used for the TCP connect and for each reply
     */
    public static BydConnection open(String host, int port, int timeoutMillis) throws BydCommunicationException {
        Socket socket = new Socket();
        try {
            socket.setTcpNoDelay(true);
            socket.connect(new InetSocketAddress(host, port), timeoutMillis);
            return new BydConnection(socket, timeoutMillis);
        } catch (IOException | IllegalArgumentException e) {
            closeQuietly(socket);
            throw new BydCommunicationException(Reason.UNREACHABLE,
                    "cannot connect to " + host + ":" + port + ": " + e.getMessage(), e);
        }
    }

    /**
     * Sends a request and returns the validated reply frame.
     */
    public byte[] transact(byte[] request) throws BydCommunicationException {
        if (logger.isTraceEnabled()) {
            logger.trace("TX {}", HexFormat.of().formatHex(request));
        }
        try {
            OutputStream out = socket.getOutputStream();
            out.write(request);
            out.flush();
            byte[] reply = readFrame();
            if (logger.isTraceEnabled()) {
                logger.trace("RX {}", HexFormat.of().formatHex(reply));
            }
            ModbusFrame.validateReply(request, reply);
            return reply;
        } catch (SocketTimeoutException e) {
            throw new BydCommunicationException(Reason.TIMEOUT,
                    "no complete reply within " + replyTimeoutMillis + " ms", e);
        } catch (IOException e) {
            throw closedOrFailed(String.valueOf(e.getMessage()), e);
        }
    }

    private byte[] readFrame() throws IOException, BydCommunicationException {
        InputStream in = socket.getInputStream();
        byte[] buffer = new byte[MAX_FRAME_LENGTH];
        int received = 0;
        int expected = ModbusFrame.LENGTH_UNKNOWN;
        long deadline = System.nanoTime() + replyTimeoutMillis * 1_000_000L;
        while (expected == ModbusFrame.LENGTH_UNKNOWN || received < expected) {
            long remainingMillis = (deadline - System.nanoTime()) / 1_000_000L;
            if (remainingMillis <= 0) {
                throw new SocketTimeoutException("reply incomplete after " + received + " bytes");
            }
            socket.setSoTimeout((int) remainingMillis);
            int n = in.read(buffer, received, buffer.length - received);
            if (n < 0) {
                throw closedOrFailed("connection closed by the battery after " + received + " bytes", null);
            }
            received += n;
            anyReplyReceived = true;
            if (expected == ModbusFrame.LENGTH_UNKNOWN) {
                expected = ModbusFrame.expectedLength(buffer, received);
            }
        }
        if (received > expected) {
            logger.debug("Discarding {} unexpected trailing bytes", received - expected);
        }
        return Arrays.copyOf(buffer, expected);
    }

    /**
     * A connection that is closed or reset before the battery sent anything is how the battery rejects a second
     * client; anything later is a protocol failure.
     */
    private BydCommunicationException closedOrFailed(String message, @Nullable Exception cause) {
        return anyReplyReceived ? new BydCommunicationException(Reason.PROTOCOL, message, cause)
                : new BydCommunicationException(Reason.BUSY, "connection closed before any reply: " + message, cause);
    }

    @Override
    public void close() {
        closeQuietly(socket);
    }

    private static void closeQuietly(Socket socket) {
        try {
            socket.close();
        } catch (IOException e) {
            // nothing left to release
        }
    }
}
