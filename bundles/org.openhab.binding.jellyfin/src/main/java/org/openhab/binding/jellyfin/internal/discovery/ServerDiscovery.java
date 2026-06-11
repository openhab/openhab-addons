/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.jellyfin.internal.api.ApiClientWrapper;
import org.openhab.binding.jellyfin.internal.gen.current.model.ServerDiscoveryInfo;
import org.openhab.core.net.NetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Patrik Gfeller - Initial Contribution
 */
@NonNullByDefault
class ServerDiscovery {
    private final String discoveryMessage;

    private final Logger logger = LoggerFactory.getLogger(ServerDiscovery.class);
    private final ObjectMapper objectMapper = ApiClientWrapper.createDefaultObjectMapper();

    private final List<ServerDiscoveryInfo> serverList = new CopyOnWriteArrayList<>();

    private final int port;
    private final int timeout;

    ServerDiscovery(int port, int timeout, String discoveryMessage) {
        this.port = port;
        this.timeout = timeout;
        this.discoveryMessage = discoveryMessage;
    }

    List<ServerDiscoveryInfo> discoverServers() {
        serverList.clear();
        List<String> broadcastAddresses = NetUtil.getAllBroadcastAddresses();
        ExecutorService executor = Executors.newCachedThreadPool();
        try {
            for (String broadcastAddress : broadcastAddresses) {
                try {
                    InetAddress broadcast = InetAddress.getByName(broadcastAddress);
                    executor.submit(() -> sendDiscoveryPacket(broadcast));
                } catch (UnknownHostException e) {
                    logger.warn("Skipping invalid broadcast address '{}': {}", broadcastAddress, e.getMessage());
                }
            }
            Thread.sleep(this.timeout);
        } catch (InterruptedException e) {
            logger.warn("Discovery interrupted: {}", e.getMessage());
            Thread.currentThread().interrupt();
        } finally {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(this.timeout, java.util.concurrent.TimeUnit.MILLISECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException ie) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        return new ArrayList<>(serverList);
    }

    private void sendDiscoveryPacket(InetAddress broadcastAddress) {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setBroadcast(true);
            socket.setSoTimeout(this.timeout); // Set a timeout for receiving responses

            byte[] sendData = discoveryMessage.getBytes();
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

                    serverList.add(objectMapper.readValue(response, ServerDiscoveryInfo.class));
                } catch (SocketTimeoutException e) {
                    // No more responses within the timeout
                    break;
                } catch (IOException e) {
                    logger.warn("Error receiving discovery response: {}", e.getMessage());
                    break;
                }
            }
        } catch (IOException e) {
            logger.warn("Error creating or sending discovery socket: {}", e.getMessage());
        }
    }
}
