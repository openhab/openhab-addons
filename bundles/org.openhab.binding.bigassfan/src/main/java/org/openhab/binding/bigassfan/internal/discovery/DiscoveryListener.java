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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DiscoveryListener} is responsible for listening on the UDP socket for fan discovery messages.
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
public class DiscoveryListener {
    private final Logger logger = LoggerFactory.getLogger(DiscoveryListener.class);

    private static final String BCAST_ADDRESS = "255.255.255.255";
    private static final int SOCKET_RECEIVE_TIMEOUT = 500;
    private static final String POLL_MESSAGE = "<ALL;DEVICE;ID;GET>";

    @Nullable
    DatagramSocket dSocket;
    @Nullable
    DatagramPacket rcvPacket;
    byte[] rcvBuffer = new byte[0];
    @Nullable
    InetAddress bcastAddress;
    byte[] bcastBuffer = new byte[0];
    @Nullable
    DatagramPacket bcastPacket;

    BigAssFanDevice device;

    public DiscoveryListener() throws IOException, SocketException {
        logger.debug("DiscoveryListener opening UDP broadcast socket");
        dSocket = null;
        device = new BigAssFanDevice();
        try {
            // Create a socket on the UDP port and get send & receive buffers
            DatagramSocket localDatagramSocket = new DatagramSocket(BAF_PORT);
            localDatagramSocket.setSoTimeout(SOCKET_RECEIVE_TIMEOUT);
            localDatagramSocket.setBroadcast(true);
            dSocket = localDatagramSocket;
            rcvBuffer = new byte[256];
            rcvPacket = new DatagramPacket(rcvBuffer, rcvBuffer.length);
            bcastAddress = InetAddress.getByName(BCAST_ADDRESS);
            bcastBuffer = POLL_MESSAGE.getBytes(StandardCharsets.US_ASCII);
            bcastPacket = new DatagramPacket(bcastBuffer, bcastBuffer.length, bcastAddress, BAF_PORT);
        } catch (UnknownHostException | SocketException | SecurityException e) {
            logger.warn("Unexpected exception sending poll request for fans: {}", e.getMessage(), e);
        }
    }

    public BigAssFanDevice waitForMessage() throws IOException, SocketTimeoutException {
        // Wait to receive a packet
        DatagramPacket localPacket = rcvPacket;
        DatagramSocket localDatagramSocket = dSocket;

        if (localPacket != null) {
            localPacket.setLength(rcvBuffer.length);
        }

        if (localDatagramSocket != null && localPacket != null) {
            localDatagramSocket.receive(localPacket);

            // Process the received packet
            device.reset();

            String address = localPacket.getAddress().getHostAddress();
            device.setIpAddress(address != null ? address : "");

            String message = (new String(rcvBuffer, 0, localPacket.getLength()));
            device.setDiscoveryMessage(message);
            logger.debug("RECEIVED packet of length {} from {}: {}", message.length(), device.getIpAddress(), message);
        }

        return device;
    }

    public void pollForDevices() {
        DatagramSocket localDatagramSocket = dSocket;
        if (localDatagramSocket == null) {
            logger.debug("Socket is null in discoveryListener.pollForDevices()");
            return;
        }

        logger.debug("Sending poll request for fans: {}", POLL_MESSAGE);
        try {
            localDatagramSocket.send(bcastPacket);
        } catch (IllegalArgumentException | SecurityException | IOException e) {
            logger.warn("Unexpected exception while sending poll request for fans: {}", e.getMessage(), e);
        }
    }

    public void shutdown() {
        logger.debug("DiscoveryListener closing socket");
        DatagramSocket localDatagramSocket = dSocket;
        if (localDatagramSocket != null) {
            localDatagramSocket.close();
            dSocket = null;
        }
    }
}
