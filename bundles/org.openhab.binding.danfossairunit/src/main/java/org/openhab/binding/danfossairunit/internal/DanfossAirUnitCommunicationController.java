/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.danfossairunit.internal.protocol.Parameter;
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

    private final Logger logger = LoggerFactory.getLogger(DanfossAirUnitCommunicationController.class);
    private final InetAddress inetAddr;
    private final int connectTimeoutMilliseconds;

    private boolean connected = false;
    private @Nullable Socket socket;
    private @Nullable OutputStream outputStream;
    private @Nullable InputStream inputStream;

    public DanfossAirUnitCommunicationController(InetAddress inetAddr) {
        this(inetAddr, DEFAULT_CONNECT_TIMEOUT_MILLISECONDS);
    }

    public DanfossAirUnitCommunicationController(InetAddress inetAddr, int connectTimeoutMilliseconds) {
        this.inetAddr = inetAddr;
        this.connectTimeoutMilliseconds = connectTimeoutMilliseconds;
    }

    @Override
    public synchronized void connect() throws IOException {
        if (connected) {
            return;
        }
        Socket socket = this.socket = new Socket();
        socket.connect(new InetSocketAddress(inetAddr, TCP_PORT), connectTimeoutMilliseconds);
        socket.setSoTimeout(READ_TIMEOUT_MILLISECONDS);
        outputStream = socket.getOutputStream();
        inputStream = socket.getInputStream();
        connected = true;
    }

    @Override
    public synchronized void disconnect() {
        if (!connected) {
            return;
        }
        try {
            Socket localSocket = this.socket;
            if (localSocket != null) {
                localSocket.close();
            }
        } catch (IOException ioe) {
            logger.debug("Connection to air unit could not be closed gracefully. {}", ioe.getMessage());
        } finally {
            this.socket = null;
            this.inputStream = null;
            this.outputStream = null;
        }
        connected = false;
    }

    @Override
    public byte[] sendRobustRequest(Parameter parameter) throws IOException {
        return sendRobustRequest(parameter, new byte[] {});
    }

    @Override
    public synchronized byte[] sendRobustRequest(Parameter parameter, byte[] value) throws IOException {
        connect();
        byte[] request = parameter.getRequest(value);
        try {
            return sendRequestInternal(request);
        } catch (IOException ioe) {
            // retry once if there was connection problem
            disconnect();
            connect();
            return sendRequestInternal(request);
        }
    }

    private synchronized byte[] sendRequestInternal(byte[] request) throws IOException {
        OutputStream localOutputStream = this.outputStream;

        if (localOutputStream == null) {
            throw new IOException(
                    String.format("Output stream is null while sending request: %s", Arrays.toString(request)));
        }
        localOutputStream.write(request);
        localOutputStream.flush();

        byte[] result = new byte[63];
        InputStream localInputStream = this.inputStream;
        if (localInputStream == null) {
            throw new IOException(
                    String.format("Input stream is null while sending request: %s", Arrays.toString(request)));
        }

        int bytesRead = localInputStream.read(result, 0, 63);
        if (bytesRead < 63) {
            throw new IOException(String.format(
                    "Error reading from stream, read returned %d as number of bytes read into the buffer", bytesRead));
        }

        return result;
    }
}
