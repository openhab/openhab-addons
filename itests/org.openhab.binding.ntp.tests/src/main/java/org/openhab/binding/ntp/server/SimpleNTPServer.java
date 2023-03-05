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
package org.openhab.binding.ntp.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import org.apache.commons.net.ntp.NtpV3Impl;
import org.apache.commons.net.ntp.NtpV3Packet;
import org.apache.commons.net.ntp.TimeStamp;
import org.openhab.binding.ntp.test.NtpOSGiTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a simple NTP server which provides timestamps to the {@link NtpOSGiTest} tests.
 * Its main purpose is to remove the dependence on a remote ntp server because it is hosted locally.
 *
 * @author Erdoan Hadzhiyusein - Initial Contribution
 *
 */
public class SimpleNTPServer {

    private DatagramSocket socket;
    private int port;
    private volatile boolean isRunning;
    private byte[] array = new byte[48];
    private final DatagramPacket request = new DatagramPacket(array, array.length);
    private Logger logger = LoggerFactory.getLogger(SimpleNTPServer.class);

    /**
     * The server must use an available port to be able to start.
     * According to RFC 793, the port is a 16 bit unsigned int.
     *
     * @param port
     */
    public SimpleNTPServer(int port) {
        if (port > 0 && port < 65535) {
            this.port = port;
        } else {
            throw new IllegalArgumentException(
                    "Please choose an available port! This port cannot be used at the moment" + port);
        }
    }

    /**
     * This method opens a new socket and receives requests calling handleRequest() for each one.
     */
    public void startServer() {
        isRunning = true;

        new Thread() {
            @Override
            public void run() {
                try {
                    socket = new DatagramSocket(port);
                } catch (SocketException e) {
                    logger.error("Occured an error {}. Couldn't open a socket on this port:", port, e);
                }
                while (isRunning) {
                    try {
                        socket.receive(request);
                        handleRequest(request);
                    } catch (IOException e) {
                        logger.error("There was an error {} while processing the request!", request, e);
                    }
                }
            }
        }.start();
    }

    /**
     * Stopping the server which causes closing the socket too
     */
    public void stopServer() {
        isRunning = false;
        if (socket != null) {
            socket.close(); // force closing of the socket
            socket = null;
        }
    }

    private void handleRequest(DatagramPacket requestPacket) throws IOException {
        final long receivedTime = System.currentTimeMillis();
        NtpV3Packet responsePacket = new NtpV3Impl();
        responsePacket.setMode(NtpV3Packet.MODE_SERVER);
        responsePacket.setTransmitTime(TimeStamp.getNtpTime(receivedTime));
        DatagramPacket dataPacket = responsePacket.getDatagramPacket();
        dataPacket.setPort(requestPacket.getPort());
        dataPacket.setAddress(requestPacket.getAddress());
        socket.send(dataPacket);
    }
}
