/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.jellyfin.internal.discovery;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Patrik Gfeller - Initial Contribution
 */
@NonNullByDefault
class ServerDiscovery {
    private static final String DISCOVERY_MESSAGE = "who is JellyfinServer?";

    private final Logger logger = LoggerFactory.getLogger(ServerDiscovery.class);

    private final List<ServerDiscoveryResult> serverList = new CopyOnWriteArrayList<>();
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    private final int port;
    private final int timeout;

    ServerDiscovery(int port, int timeout) {
        this.port = port;
        this.timeout = timeout;
    }

    List<ServerDiscoveryResult> discoverServers() {
        serverList.clear();
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    logger.trace("Interface {} ignored.", networkInterface.getDisplayName());
                    continue;
                }

                for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                    InetAddress broadcast = interfaceAddress.getBroadcast();
                    if (broadcast != null) {
                        executorService.submit(() -> sendDiscoveryPacket(broadcast));
                    }
                }
            }
            Thread.sleep(this.timeout);
        } catch (SocketException | InterruptedException e) {
            logger.error("Error during network interface enumeration or sleep: {}", e.getMessage());
        } finally {
            executorService.shutdown();
        }
        return new ArrayList<>(serverList);
    }

    private void sendDiscoveryPacket(InetAddress broadcastAddress) {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setBroadcast(true);
            socket.setSoTimeout(this.timeout); // Set a timeout for receiving responses

            byte[] sendData = DISCOVERY_MESSAGE.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, broadcastAddress, this.port);
            socket.send(sendPacket);

            logger.trace("Sent discovery packet to {}:{}", broadcastAddress.getHostAddress(), this.port);

            // Listen for responses
            byte[] receiveData = new byte[1024];
            while (true) {
                try {
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    socket.receive(receivePacket);
                    String response = new String(receivePacket.getData(), 0, receivePacket.getLength());

                    logger.debug("Discovered Jellyfin Server: {}", response);

                    serverList.add(ServerDiscoveryResult.fromJson(response));
                } catch (SocketTimeoutException e) {
                    // No more responses within the timeout
                    break;
                } catch (IOException e) {
                    logger.error("Error receiving discovery response: {}", e.getMessage());
                    break;
                }
            }
        } catch (IOException e) {
            logger.error("Error creating or sending discovery socket: {}", e.getMessage());
        }
    }
}
