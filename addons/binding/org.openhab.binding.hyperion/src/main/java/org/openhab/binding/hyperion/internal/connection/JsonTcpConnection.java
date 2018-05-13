/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.hyperion.internal.connection;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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
        try (Socket hyperionServer = new Socket(address, port)) {
            DataOutputStream outToServer = new DataOutputStream(hyperionServer.getOutputStream());
            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(hyperionServer.getInputStream()));
            logger.debug("Sending: {}", json);
            outToServer.writeBytes(json + System.lineSeparator());
            outToServer.flush();
            response = inFromServer.readLine();
        } catch (IOException e) {
            throw e;
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
