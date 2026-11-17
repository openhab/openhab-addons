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
package org.openhab.binding.solarman.internal.modbus;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.function.LongSupplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.solarman.internal.modbus.exception.SolarmanConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Owns a cancellable socket and reassembles complete V5 or raw TCP frames.
 *
 * @author Catalin Sanda - Initial contribution
 * @author Ricardo Costa - Advanced profiles and shared compatibility corrections
 */
@NonNullByDefault
public class SolarmanLoggerConnection implements AutoCloseable {
    private static final int RESPONSE_TIMEOUT_MILLIS = 10_000;
    private static final int MAX_HEARTBEATS = 3;

    private final Logger logger = LoggerFactory.getLogger(SolarmanLoggerConnection.class);
    private final LongSupplier nanoTime;
    private volatile @Nullable Socket socket;

    SolarmanLoggerConnection() {
        this(new Socket());
    }

    SolarmanLoggerConnection(Socket socket) {
        this(socket, System::nanoTime);
    }

    SolarmanLoggerConnection(Socket socket, LongSupplier nanoTime) {
        this.socket = socket;
        this.nanoTime = nanoTime;
    }

    public SolarmanLoggerConnection(String hostname, int port) {
        this();
        connect(hostname, port);
    }

    void connect(String hostname, int port) {
        Socket local = socket;
        if (local == null) {
            return;
        }
        try {
            local.setSoTimeout(RESPONSE_TIMEOUT_MILLIS);
            local.connect(new InetSocketAddress(hostname, port), RESPONSE_TIMEOUT_MILLIS);
        } catch (IOException e) {
            logger.debug("Could not connect to logger at {}:{}", hostname, port, e);
            close();
        }
    }

    public byte[] sendRequest(byte[] request) throws SolarmanConnectionException {
        Socket local = socket;
        if (local == null || !isConnected()) {
            throw new SolarmanConnectionException("Logger connection is closed");
        }
        boolean v5 = request.length > 0 && request[0] == (byte) 0xA5;
        try {
            logger.debug("Request frame: {}", bytesToHex(request));
            local.getOutputStream().write(request);
            InputStream input = local.getInputStream();
            long deadline = nanoTime.getAsLong() + RESPONSE_TIMEOUT_MILLIS * 1_000_000L;
            int heartbeats = 0;
            while (true) {
                byte[] response = readFrame(input, local, v5, deadline);
                logger.debug("Response frame: {}", bytesToHex(response));
                if (!v5 || !isHeartbeat(response, request)) {
                    return response;
                }
                // Heartbeats have their own sequence and are not Modbus replies. Never resend the request here.
                if (++heartbeats > MAX_HEARTBEATS) {
                    throw new IOException("Too many V5 heartbeats while waiting for the Modbus response");
                }
                logger.debug("Ignoring V5 heartbeat while waiting for the Modbus response ({}/{})", heartbeats,
                        MAX_HEARTBEATS);
            }
        } catch (IOException e) {
            if (v5) {
                close();
            }
            throw new SolarmanConnectionException("Unable to exchange a complete logger frame", e);
        }
    }

    private byte[] readFrame(InputStream input, Socket local, boolean v5, long deadline)
            throws IOException, SolarmanConnectionException {
        byte[] header = readFully(input, v5 ? 3 : 6, local, v5, deadline);
        int payloadLength = Short.toUnsignedInt(ByteBuffer.wrap(header, v5 ? 1 : 4, 2)
                .order(v5 ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN).getShort());
        int totalLength = (v5 ? 13 : 6) + payloadLength;
        if (totalLength < header.length || totalLength > 4096) {
            throw new SolarmanConnectionException("Invalid response frame length");
        }
        byte[] response = ByteBuffer.allocate(totalLength).put(header)
                .put(readFully(input, totalLength - header.length, local, v5, deadline)).array();
        return response;
    }

    private boolean isHeartbeat(byte[] response, byte[] request) {
        if (response.length != 14 || request.length < 11 || response[0] != (byte) 0xA5 || response[3] != 0x10
                || response[4] != 0x47 || response[11] != 0 || response[13] != 0x15
                || !Arrays.equals(response, 7, 11, request, 7, 11)) {
            return false;
        }
        int checksum = 0;
        for (int i = 1; i < response.length - 2; i++) {
            checksum += Byte.toUnsignedInt(response[i]);
        }
        return (checksum & 255) == Byte.toUnsignedInt(response[12]);
    }

    private byte[] readFully(InputStream input, int length, Socket local, boolean v5, long deadline)
            throws IOException {
        if (!v5) {
            byte[] bytes = input.readNBytes(length);
            if (bytes.length != length) {
                throw new IOException("Connection closed before the response frame was complete");
            }
            return bytes;
        }
        byte[] bytes = new byte[length];
        int offset = 0;
        while (offset < length) {
            long remaining = deadline - nanoTime.getAsLong();
            if (remaining <= 0 || Thread.currentThread().isInterrupted()) {
                throw new SocketTimeoutException("V5 response deadline expired or logger task was cancelled");
            }
            local.setSoTimeout((int) Math.min(RESPONSE_TIMEOUT_MILLIS, (remaining + 999_999) / 1_000_000));
            int count = input.read(bytes, offset, length - offset);
            if (count < 0) {
                throw new IOException("Connection closed before the response frame was complete");
            }
            offset += count;
        }
        return bytes;
    }

    protected static String bytesToHex(byte[] bytes) {
        return IntStream.range(0, bytes.length).mapToObj(i -> String.format("%02X", bytes[i]))
                .collect(Collectors.joining());
    }

    boolean isOpen() {
        Socket local = socket;
        return local != null && !local.isClosed();
    }

    public boolean isConnected() {
        Socket local = socket;
        return local != null && local.isConnected() && !local.isClosed();
    }

    @Override
    public void close() {
        Socket local = socket;
        socket = null;
        if (local != null) {
            try {
                local.close();
            } catch (IOException e) {
                logger.debug("Unable to close logger connection", e);
            }
        }
    }
}
