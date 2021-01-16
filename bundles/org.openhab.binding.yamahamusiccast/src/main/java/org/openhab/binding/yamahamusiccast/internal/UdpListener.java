/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.yamahamusiccast.internal;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.UUID;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link UdpListener} is responsible for receiving
 * UDP braodcasts from the Yamaha device/model.
 *
 * @author Lennert Coopman - Initial contribution
 */
@NonNullByDefault
public class UdpListener extends Thread {

    private static final int UDP_PORT = 41100;
    private static final int SOCKET_TIMEOUT_MILLISECONDS = 3000;
    private static final int BUFFER_SIZE = 5120;

    private final Logger logger = LoggerFactory.getLogger(UdpListener.class);
    private final YamahaMusiccastBridgeHandler bridgeHandler;
    private @Nullable DatagramSocket socket;

    public UdpListener(YamahaMusiccastBridgeHandler bridgeHandler) {
        this.bridgeHandler = bridgeHandler;
    }

    @Override
    public void run() {
        receivePackets();
    }

    public void shutdown() {
        if (socket != null) {
            socket.close();
            logger.debug("YXC - UDP Listener socket closed");
            socket = null;
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
            logger.debug("YXC - UDP Listener got socket on port {} with timeout {}", UDP_PORT,
                    SOCKET_TIMEOUT_MILLISECONDS);
        } catch (SocketException e) {
            logger.debug("YXC - UDP Listener got SocketException: {}", e.getMessage(), e);
            socket = null;
            return;
        }

        DatagramPacket packet = new DatagramPacket(new byte[BUFFER_SIZE], BUFFER_SIZE);
        while (socket != null) {
            try {
                socket.receive(packet);
                String received = new String(packet.getData(), 0, packet.getLength());
                String trackingID = UUID.randomUUID().toString().replace("-", "").substring(0, 32);
                logger.debug("YXC - Received packet: {} (Tracking: {})", received, trackingID);
                bridgeHandler.handleUDPEvent(received, trackingID);
            } catch (SocketTimeoutException e) {
                // Nothing to do on socket timeout
            } catch (IOException e) {
                logger.debug("YXC - UDP Listener got IOException waiting for datagram: {}", e.getMessage());
                socket = null;
            }
        }
        logger.debug("YXC - UDP Listener exiting");
    }
}
