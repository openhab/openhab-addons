/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.regoheatpump.internal.protocol;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link IpRegoConnection} is responsible for creating TCP/IP connections to clients.
 *
 * @author Boris Krivonog - Initial contribution
 */
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
    private Socket clientSocket;

    public IpRegoConnection(String address, int port) {
        this.address = address;
        this.port = port;
    }

    @Override
    public void connect() throws IOException {
        logger.debug("Connecting to '{}', port = {}.", address, port);
        if (clientSocket == null) {
            clientSocket = new Socket();
            clientSocket.setSoTimeout(SOCKET_READ_TIMEOUT);
            clientSocket.setKeepAlive(true);
        }
        clientSocket.connect(new InetSocketAddress(address, port), CONNECTION_TIMEOUT);
        logger.debug("Connected to '{}', port = {}.", address, port);
    }

    @Override
    public boolean isConnected() {
        return clientSocket != null && clientSocket.isConnected();
    }

    @Override
    public void close() {
        try {
            if (clientSocket != null) {
                clientSocket.close();
            }
        } catch (IOException e) {
            // There is really not much we can do here, ignore the error and continue execution.
            logger.warn("Closing socket failed", e);
        }

        clientSocket = null;
    }

    @Override
    public OutputStream outputStream() throws IOException {
        return clientSocket.getOutputStream();
    }

    @Override
    public InputStream inputStream() throws IOException {
        return clientSocket.getInputStream();
    }
}
