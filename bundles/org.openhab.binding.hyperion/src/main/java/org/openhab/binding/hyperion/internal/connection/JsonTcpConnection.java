/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.hyperion.internal.connection;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link JsonTcpConnection} class is responsible for handling
 * the communication with the Hyperion json server.
 *
 * @author Daniel Walters - Initial contribution
 */
public class JsonTcpConnection {

    private final Logger logger = LoggerFactory.getLogger(JsonTcpConnection.class);
    private InetAddress address;
    private int port;
    private Socket hyperionServerSocket;

    public JsonTcpConnection(InetAddress address, int port) {
        this.setAddress(address);
        this.port = port;
    }

    public JsonTcpConnection(String sAddress, int port) throws UnknownHostException {
        this(InetAddress.getByName(sAddress), port);
    }

    public int getPort() {
        return port;
    }

    public InetAddress getAddress() {
        return address;
    }

    public void setAddress(InetAddress address) {
        this.address = address;
    }

    public String send(String json) throws IOException {
        String response = null;
        try (Socket hyperionServer = new Socket(address, port);
                DataOutputStream outToServer = new DataOutputStream(hyperionServer.getOutputStream());
                Reader isr = new InputStreamReader(hyperionServer.getInputStream());
                BufferedReader inFromServer = new BufferedReader(isr)) {
            logger.debug("Sending: {}", json);
            outToServer.writeBytes(json + System.lineSeparator());
            outToServer.flush();
            response = inFromServer.readLine();
        }
        logger.debug("Received: {}", response);
        return response;
    }

    public void connect() throws IOException {
        if (hyperionServerSocket == null || !hyperionServerSocket.isConnected()) {
            hyperionServerSocket = new Socket(address, port);
        }
    }

    public void close() throws IOException {
        if (hyperionServerSocket != null && hyperionServerSocket.isConnected()) {
            hyperionServerSocket.close();
        }
    }

    public boolean isConnected() {
        return hyperionServerSocket != null && hyperionServerSocket.isConnected();
    }
}
