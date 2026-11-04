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
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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
    private final Logger logger = LoggerFactory.getLogger(SolarmanLoggerConnection.class);
    private volatile @Nullable Socket socket;

    SolarmanLoggerConnection() {
        this(new Socket());
    }

    SolarmanLoggerConnection(Socket socket) {
        this.socket = socket;
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
            local.setSoTimeout(10_000);
            local.connect(new InetSocketAddress(hostname, port), 10_000);
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
        try {
            logger.debug("Request frame: {}", bytesToHex(request));
            local.getOutputStream().write(request);
            InputStream input = local.getInputStream();
            boolean v5 = request.length > 0 && request[0] == (byte) 0xA5;
            byte[] header = readFully(input, v5 ? 3 : 6);
            int payloadLength = Short.toUnsignedInt(ByteBuffer.wrap(header, v5 ? 1 : 4, 2)
                    .order(v5 ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN).getShort());
            int totalLength = (v5 ? 13 : 6) + payloadLength;
            if (totalLength < header.length || totalLength > 4096) {
                throw new SolarmanConnectionException("Invalid response frame length");
            }
            byte[] response = ByteBuffer.allocate(totalLength).put(header)
                    .put(readFully(input, totalLength - header.length)).array();
            logger.debug("Response frame: {}", bytesToHex(response));
            return response;
        } catch (IOException e) {
            throw new SolarmanConnectionException("Unable to exchange a complete logger frame", e);
        }
    }

    private byte[] readFully(InputStream input, int length) throws IOException {
        byte[] bytes = input.readNBytes(length);
        if (bytes.length != length) {
            throw new IOException("Connection closed before the response frame was complete");
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
