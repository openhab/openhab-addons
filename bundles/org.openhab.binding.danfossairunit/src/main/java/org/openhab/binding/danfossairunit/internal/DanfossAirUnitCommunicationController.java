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
package org.openhab.binding.danfossairunit.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.danfossairunit.internal.protocol.Parameter;
import org.openhab.core.util.HexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DanfossAirUnitCommunicationController} class does the actual network communication with the air unit.
 *
 * @author Robert Bach - initial contribution
 * @author Jacob Laursen - Refactoring, bugfixes and enhancements
 */

@NonNullByDefault
public class DanfossAirUnitCommunicationController implements CommunicationController {

    private static final int TCP_PORT = 30046;
    private static final int READ_TIMEOUT_MILLISECONDS = 5_000;
    private static final int DEFAULT_CONNECT_TIMEOUT_MILLISECONDS = 5_000;
    private static final int RESPONSE_LENGTH = 63;
    private static final byte[] EMPTY = new byte[0];

    private final Logger logger = LoggerFactory.getLogger(DanfossAirUnitCommunicationController.class);
    private final InetAddress hostAddress;
    private final int connectTimeoutMilliseconds;

    private @Nullable Socket socket;
    private @Nullable OutputStream outputStream;
    private @Nullable InputStream inputStream;

    public DanfossAirUnitCommunicationController(InetAddress hostAddress) {
        this(hostAddress, DEFAULT_CONNECT_TIMEOUT_MILLISECONDS);
    }

    public DanfossAirUnitCommunicationController(InetAddress hostAddress, int connectTimeoutMilliseconds) {
        this.hostAddress = hostAddress;
        this.connectTimeoutMilliseconds = connectTimeoutMilliseconds;
    }

    private synchronized void connect() throws IOException {
        Socket socket = this.socket;
        if (socket != null && !socket.isClosed()) {
            // Already connected
            return;
        }

        socket = new Socket();
        socket.connect(new InetSocketAddress(hostAddress, TCP_PORT), connectTimeoutMilliseconds);
        socket.setSoTimeout(READ_TIMEOUT_MILLISECONDS);

        OutputStream outputStream = socket.getOutputStream();
        InputStream inputStream = socket.getInputStream();

        this.socket = socket;
        this.outputStream = outputStream;
        this.inputStream = inputStream;
    }

    @Override
    public synchronized void close() {
        try {
            Socket socket = this.socket;
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException ioe) {
            logger.debug("Connection to air unit could not be closed gracefully. {}", ioe.getMessage());
        } finally {
            this.socket = null;
            this.inputStream = null;
            this.outputStream = null;
        }
    }

    @Override
    public byte[] sendRobustRequest(Parameter parameter) throws IOException {
        return sendRobustRequest(parameter, EMPTY);
    }

    @Override
    public synchronized byte[] sendRobustRequest(Parameter parameter, byte[] value) throws IOException {
        byte[] request = parameter.getRequest(value);

        try {
            connect();

            byte[] response = sendRequestInternal(request);
            if (logger.isTraceEnabled()) {
                logger.trace("{} response: {}", parameter, HexUtils.bytesToHex(response));
            }

            return response;
        } catch (IOException suppressedException) {
            logger.debug("{} request failed, retrying once: {}", parameter, suppressedException.getMessage());

            close();

            try {
                connect();

                byte[] response = sendRequestInternal(request);
                if (logger.isTraceEnabled()) {
                    logger.trace("{} response: {}", parameter, HexUtils.bytesToHex(response));
                }

                return response;
            } catch (IOException e) {
                suppressedException.addSuppressed(e);
                throw suppressedException;
            }
        }
    }

    private byte[] sendRequestInternal(byte[] request) throws IOException {
        OutputStream outputStream = this.outputStream;
        InputStream inputStream = this.inputStream;

        if (outputStream == null || inputStream == null) {
            throw new IOException("Input/output streams not initialized");
        }

        outputStream.write(request);
        outputStream.flush();

        return readExact(inputStream, RESPONSE_LENGTH);
    }

    private byte[] readExact(InputStream inputStream, int length) throws IOException {
        byte[] response = new byte[length];
        int offset = 0;

        while (offset < length) {
            int bytesRead = inputStream.read(response, offset, length - offset);

            if (bytesRead == -1) {
                throw new IOException("Stream closed while reading response");
            }
            if (bytesRead == 0) {
                throw new IOException("Read returned 0 bytes, possible stream stall");
            }

            offset += bytesRead;
        }

        return response;
    }
}
