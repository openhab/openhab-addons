/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.doorbird.internal.DoorbirdHandler;
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
    private static final int SOCKET_TIMEOUT = 3000;

    private static final int BUFFER_SIZE = 80;

    private final Logger logger = LoggerFactory.getLogger(DoorbirdUdpListener.class);

    private final DoorbirdEvent event = new DoorbirdEvent();

    // Used for callbacks to handler
    private final DoorbirdHandler thingHandler;

    // UDP socket used to receive status events from doorbell
    private @Nullable DatagramSocket socket;

    private byte @Nullable [] lastData;
    private int lastDataLength;
    private long lastDataTime;

    public DoorbirdUdpListener(DoorbirdHandler thingHandler) {
        this.thingHandler = thingHandler;
    }

    @Override
    public void run() {
        receivePackets();
    }

    public void shutdown() {
        if (socket != null) {
            socket.close();
            logger.debug("Listener closing listener socket");
            socket = null;
        }
    }

    private void receivePackets() {
        try {
            socket = new DatagramSocket(UDP_PORT);
            socket.setSoTimeout(SOCKET_TIMEOUT);
            logger.debug("Listener got UDP socket on port {} with timeout {}", UDP_PORT, SOCKET_TIMEOUT);
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

        try {
            String userPassword = thingHandler.getUserPassword();
            if (userPassword != null) {
                event.decrypt(packet, userPassword);
            }
        } catch (RuntimeException e) {
            // The libsodium library might generate a runtime exception if the packet is malformed
            logger.info("DoorbirdEvent got unhandled exception: {}", e.getMessage(), e);
            return;
        }

        String doorbirdId = thingHandler.getDoorbirdId();
        if (event.isDoorbellEvent() && doorbirdId != null) {
            if ("motion".equalsIgnoreCase(event.getEventId())) {
                thingHandler.updateMotionChannel(event.getTimestamp());
            } else if (doorbirdId.equalsIgnoreCase(event.getEventId())) {
                thingHandler.updateDoorbellChannel(event.getTimestamp());
            } else {
                logger.debug("Unknown doorbell event type: {}", event.getEventId());
            }
        }
    }

    private boolean isDuplicate(DatagramPacket packet) {
        boolean packetIsDuplicate = false;
        if (lastData != null) {
            if (lastDataLength == packet.getLength()) {
                // Lengths are different, therefore not a dup
                if (Arrays.equals(lastData, Arrays.copyOf(packet.getData(), packet.getLength()))) {
                    // Packet must be received within 750 ms of previous packet to be consider a duplicate
                    if ((System.currentTimeMillis() - lastDataTime) < 750) {
                        packetIsDuplicate = true;
                    }
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
