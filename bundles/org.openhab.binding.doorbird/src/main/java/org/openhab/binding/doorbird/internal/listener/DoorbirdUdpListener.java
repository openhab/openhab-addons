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
package org.openhab.binding.doorbird.internal.listener;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.doorbird.internal.handler.DoorbellHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DoorbirdUdpListener} is responsible for receiving
 * UDP braodcasts from the Doorbird doorbell.
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
public class DoorbirdUdpListener extends Thread {
    // Doorbird devices report status on a UDP port
    private static final int UDP_PORT = 6524;

    // How long to wait in milliseconds for a UDP packet
    private static final int SOCKET_TIMEOUT_MILLISECONDS = 3000;

    private static final int BUFFER_SIZE = 80;

    private final Logger logger = LoggerFactory.getLogger(DoorbirdUdpListener.class);

    private final DoorbirdEvent event = new DoorbirdEvent();

    // Used for callbacks to handler
    private final DoorbellHandler thingHandler;

    // UDP socket used to receive status events from doorbell
    private @Nullable DatagramSocket socket;

    private byte @Nullable [] lastData;
    private int lastDataLength;
    private long lastDataTime;

    public DoorbirdUdpListener(DoorbellHandler thingHandler) {
        this.thingHandler = thingHandler;
    }

    @Override
    public void run() {
        receivePackets();
    }

    public void shutdown() {
        DatagramSocket socket = this.socket;
        if (socket != null) {
            socket.close();
            logger.debug("Listener closing listener socket");
            this.socket = null;
        }
    }

    private void receivePackets() {
        try {
            DatagramSocket s = new DatagramSocket(null);
            s.setSoTimeout(SOCKET_TIMEOUT_MILLISECONDS);
            s.setReuseAddress(true);
            InetSocketAddress address = new InetSocketAddress(UDP_PORT);
            s.bind(address);
            socket = s;
            logger.debug("Listener got UDP socket on port {} with timeout {}", UDP_PORT, SOCKET_TIMEOUT_MILLISECONDS);
        } catch (SocketException e) {
            logger.debug("Listener got SocketException: {}", e.getMessage(), e);
            socket = null;
            return;
        }

        DatagramPacket packet = new DatagramPacket(new byte[BUFFER_SIZE], BUFFER_SIZE);
        while (socket != null) {
            try {
                socket.receive(packet);
                processPacket(packet);
            } catch (SocketTimeoutException e) {
                // Nothing to do on socket timeout
            } catch (IOException e) {
                logger.debug("Listener got IOException waiting for datagram: {}", e.getMessage());
                socket = null;
            }
        }
        logger.debug("Listener exiting");
    }

    private void processPacket(DatagramPacket packet) {
        logger.trace("Got datagram of length {} from {}", packet.getLength(), packet.getAddress().getHostAddress());

        // Check for duplicate packet
        if (isDuplicate(packet)) {
            logger.trace("Dropping duplicate packet");
            return;
        }

        String userId = thingHandler.getUserId();
        String userPassword = thingHandler.getUserPassword();
        if (userId == null || userPassword == null) {
            logger.info("Doorbird user id and/or password is not set in configuration");
            return;
        }
        try {
            event.decrypt(packet, userPassword);
        } catch (RuntimeException e) {
            // The libsodium library might generate a runtime exception if the packet is malformed
            logger.info("DoorbirdEvent got unhandled exception: {}", e.getMessage(), e);
            return;
        }

        if (event.isDoorbellEvent()) {
            if ("motion".equalsIgnoreCase(event.getEventId())) {
                thingHandler.updateMotionChannel(event.getTimestamp());
            } else {
                String intercomId = event.getIntercomId();
                if (intercomId != null && userId.toLowerCase().startsWith(intercomId.toLowerCase())) {
                    thingHandler.updateDoorbellChannel(event.getTimestamp());
                } else {
                    logger.info("Received doorbell event for unknown device: {}", event.getIntercomId());
                }
            }
        }
    }

    private boolean isDuplicate(DatagramPacket packet) {
        boolean packetIsDuplicate = false;
        if (lastData != null && lastDataLength == packet.getLength()) {
            // Packet must be received within 750 ms of previous packet to be considered a duplicate
            if ((System.currentTimeMillis() - lastDataTime) < 750) {
                // Compare packets byte-for-byte
                if (Arrays.equals(lastData, Arrays.copyOf(packet.getData(), packet.getLength()))) {
                    packetIsDuplicate = true;
                }
            }
        }
        // Remember this packet for duplicate check
        lastDataLength = packet.getLength();
        lastData = Arrays.copyOf(packet.getData(), lastDataLength);
        lastDataTime = System.currentTimeMillis();
        return packetIsDuplicate;
    }
}
