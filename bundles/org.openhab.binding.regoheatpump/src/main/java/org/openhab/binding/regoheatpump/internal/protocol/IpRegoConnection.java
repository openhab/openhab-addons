/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.regoheatpump.internal.protocol;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link IpRegoConnection} is responsible for creating TCP/IP connections to clients.
 *
 * @author Boris Krivonog - Initial contribution
 */
@NonNullByDefault
public class IpRegoConnection implements RegoConnection {
    /**
     * Connection timeout in milliseconds
     **/
    private static final int CONNECTION_TIMEOUT = 3000;

    /**
     * Socket read timeout in milliseconds
     **/
    private static final int SOCKET_READ_TIMEOUT = 2000;

    private final Logger logger = LoggerFactory.getLogger(IpRegoConnection.class);
    private final String address;
    private final int port;
    private @Nullable Socket clientSocket;

    public IpRegoConnection(String address, int port) {
        this.address = address;
        this.port = port;
    }

    @Override
    public void connect() throws IOException {
        logger.debug("Connecting to '{}', port = {}.", address, port);
        Socket clientSocket = this.clientSocket;
        if (clientSocket == null) {
            clientSocket = new Socket();
            clientSocket.setSoTimeout(SOCKET_READ_TIMEOUT);
            clientSocket.setKeepAlive(true);
            this.clientSocket = clientSocket;
        }
        clientSocket.connect(new InetSocketAddress(address, port), CONNECTION_TIMEOUT);
        logger.debug("Connected to '{}', port = {}.", address, port);
    }

    @Override
    public boolean isConnected() {
        Socket clientSocket = this.clientSocket;
        return clientSocket != null && clientSocket.isConnected();
    }

    @Override
    public void close() {
        try {
            Socket clientSocket = this.clientSocket;
            this.clientSocket = null;
            if (clientSocket != null) {
                clientSocket.close();
            }
        } catch (IOException e) {
            // There is really not much we can do here, ignore the error and continue execution.
            logger.warn("Closing socket failed", e);
        }
    }

    @Override
    public OutputStream outputStream() throws IOException {
        return getClientSocket().getOutputStream();
    }

    @Override
    public InputStream inputStream() throws IOException {
        return getClientSocket().getInputStream();
    }

    private Socket getClientSocket() throws IOException {
        Socket clientSocket = this.clientSocket;
        if (clientSocket == null) {
            throw new IOException("Socket closed");
        }
        return clientSocket;
    }
}
