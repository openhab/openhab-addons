/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.bigassfan.internal.discovery;

import static org.openhab.binding.bigassfan.internal.BigAssFanBindingConstants.BAF_PORT;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DiscoveryListener} is responsible for listening on the UDP socket for fan discovery messages.
 *
 * @author Mark Hilbush - Initial contribution
 */
public class DiscoveryListener {
    private final Logger logger = LoggerFactory.getLogger(DiscoveryListener.class);

    private final String BCAST_ADDRESS = "255.255.255.255";
    private final int SOCKET_RECEIVE_TIMEOUT = 500;

    private final String POLL_MESSAGE = "<ALL;DEVICE;ID;GET>";

    DatagramSocket dSocket;
    DatagramPacket rcvPacket;
    byte[] rcvBuffer;
    InetAddress bcastAddress;
    byte[] bcastBuffer;
    DatagramPacket bcastPacket;

    BigAssFanDevice device;

    public DiscoveryListener() throws IOException, SocketException {
        logger.debug("DiscoveryListener opening UDP broadcast socket");
        dSocket = null;
        device = new BigAssFanDevice();
        try {
            // Create a socket on the UDP port and get send & receive buffers
            dSocket = new DatagramSocket(BAF_PORT);
            dSocket.setSoTimeout(SOCKET_RECEIVE_TIMEOUT);
            dSocket.setBroadcast(true);
            rcvBuffer = new byte[256];
            rcvPacket = new DatagramPacket(rcvBuffer, rcvBuffer.length);
            bcastAddress = InetAddress.getByName(BCAST_ADDRESS);
            bcastBuffer = POLL_MESSAGE.getBytes(StandardCharsets.US_ASCII);
            bcastPacket = new DatagramPacket(bcastBuffer, bcastBuffer.length, bcastAddress, BAF_PORT);
        } catch (UnknownHostException uhe) {
            logger.warn("UnknownHostException sending poll request for fans: {}", uhe.getMessage(), uhe);
        }
    }

    public BigAssFanDevice waitForMessage() throws IOException, SocketTimeoutException {
        // Wait to receive a packet
        rcvPacket.setLength(rcvBuffer.length);
        dSocket.receive(rcvPacket);

        // Process the received packet
        device.reset();
        device.setIpAddress(rcvPacket.getAddress().getHostAddress());
        String message = (new String(rcvBuffer, 0, rcvPacket.getLength()));
        device.setDiscoveryMessage(message);
        logger.debug("RECEIVED packet of length {} from {}: {}", message.length(), device.getIpAddress(), message);

        return device;
    }

    public void pollForDevices() {
        if (dSocket == null) {
            logger.debug("Socket is null in discoveryListener.pollForDevices()");
            return;
        }

        logger.debug("Sending poll request for fans: {}", POLL_MESSAGE);
        try {
            dSocket.send(bcastPacket);
        } catch (IOException ioe) {
            logger.warn("IOException sending poll request for fans: {}", ioe.getMessage(), ioe);
        }
    }

    public void shutdown() {
        logger.debug("DiscoveryListener closing socket");
        if (dSocket != null) {
            dSocket.close();
            dSocket = null;
        }
    }
}
