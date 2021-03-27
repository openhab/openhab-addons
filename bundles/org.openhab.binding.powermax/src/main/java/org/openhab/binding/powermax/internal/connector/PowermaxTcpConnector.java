/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.powermax.internal.connector;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class for the communication with the Visonic alarm panel through a TCP connection
 *
 * @author Laurent Garnier - Initial contribution
 */
public class PowermaxTcpConnector extends PowermaxConnector {

    private final Logger logger = LoggerFactory.getLogger(PowermaxTcpConnector.class);

    private final String ipAddress;
    private final int tcpPort;
    private final int connectTimeout;
    private Socket tcpSocket;

    /**
     * Constructor.
     *
     * @param ip the IP address
     * @param port the TCP port number
     * @param timeout the timeout for socket communications
     * @param readerThreadName the name of thread to be created
     */
    public PowermaxTcpConnector(String ip, int port, int timeout, String readerThreadName) {
        super(readerThreadName);
        ipAddress = ip;
        tcpPort = port;
        connectTimeout = timeout;
    }

    @Override
    public void open() throws Exception {
        logger.debug("open(): Opening TCP Connection");

        tcpSocket = new Socket();
        tcpSocket.setSoTimeout(250);
        SocketAddress socketAddress = new InetSocketAddress(ipAddress, tcpPort);
        tcpSocket.connect(socketAddress, connectTimeout);

        setInput(tcpSocket.getInputStream());
        setOutput(tcpSocket.getOutputStream());

        setReaderThread(new PowermaxReaderThread(this, readerThreadName));
        getReaderThread().start();

        setConnected(true);
    }

    @Override
    public void close() {
        logger.debug("close(): Closing TCP Connection");

        super.cleanup(false);

        if (tcpSocket != null) {
            try {
                tcpSocket.close();
            } catch (IOException e) {
                logger.debug("Error while closing the socket: {}", e.getMessage());
            }
        }

        tcpSocket = null;

        setConnected(false);
    }

    @Override
    public int read(byte[] buffer) throws IOException {
        try {
            return super.read(buffer);
        } catch (SocketTimeoutException ignore) {
            // ignore this exception, instead return 0 to behave like the serial read
            return 0;
        }
    }
}
