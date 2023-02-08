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
package org.openhab.binding.pioneeravr.internal.protocol.ip;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.openhab.binding.pioneeravr.internal.protocol.StreamAvrConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * A class that wraps the communication to a Pioneer AVR devices using an IP connection.
 *
 * @author Antoine Besnard - Initial contribution
 */
public class IpAvrConnection extends StreamAvrConnection {

    private final Logger logger = LoggerFactory.getLogger(IpAvrConnection.class);

    /** default port for IP communication **/
    public static final int DEFAULT_IPCONTROL_PORT = 8102;

    /** Connection timeout in milliseconds **/
    private static final int CONNECTION_TIMEOUT = 3000;

    /** Socket read timeout in milliseconds **/
    private static final int SOCKET_READ_TIMEOUT = 1000;

    private int receiverPort;
    private String receiverHost;

    private Socket ipControlSocket;

    public IpAvrConnection(String receiverHost) {
        this(receiverHost, null);
    }

    public IpAvrConnection(String receiverHost, Integer ipControlPort) {
        this.receiverHost = receiverHost;
        this.receiverPort = ipControlPort != null && ipControlPort >= 1 ? ipControlPort : DEFAULT_IPCONTROL_PORT;
    }

    @Override
    protected void openConnection() throws IOException {
        ipControlSocket = new Socket();

        // Set this timeout to unblock a blocking read when no data is received. It is useful to check if the
        // reading thread has to be stopped (it implies a latency of SOCKET_READ_TIMEOUT at most before the
        // thread is really stopped)
        ipControlSocket.setSoTimeout(SOCKET_READ_TIMEOUT);

        // Enable tcpKeepAlive to detect premature disconnection
        // and prevent disconnection because of inactivity
        ipControlSocket.setKeepAlive(true);

        // Connect to the AVR with a connection timeout.
        ipControlSocket.connect(new InetSocketAddress(receiverHost, receiverPort), CONNECTION_TIMEOUT);

        logger.debug("Connected to {}:{}", receiverHost, receiverPort);
    }

    @Override
    public boolean isConnected() {
        return ipControlSocket != null && ipControlSocket.isConnected() && !ipControlSocket.isClosed();
    }

    @Override
    public void close() {
        super.close();
        try {
            if (ipControlSocket != null) {
                ipControlSocket.close();
                ipControlSocket = null;
                logger.debug("Closed socket!");
            }
        } catch (IOException ioException) {
            logger.error("Closing connection throws an exception!", ioException);
        }
    }

    @Override
    public String getConnectionName() {
        return receiverHost + ":" + receiverPort;
    }

    @Override
    protected InputStream getInputStream() throws IOException {
        return ipControlSocket.getInputStream();
    }

    @Override
    protected OutputStream getOutputStream() throws IOException {
        return ipControlSocket.getOutputStream();
    }
}
