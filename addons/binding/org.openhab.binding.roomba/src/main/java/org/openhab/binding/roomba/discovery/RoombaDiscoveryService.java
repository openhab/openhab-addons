/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.roomba.discovery;

import static org.openhab.binding.roomba.RoombaBindingConstants.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.roomba.model.discovery.RoombaDiscoveryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * Discovers Roombas that are available on the network. This works for models 980+.
 *
 * Roombas are discovered by sending "irobotmcs" to 255.255.255.255 via UDP Broadcast. The Roomba must already be paired
 * to the WiFi network.
 *
 * @author Stephen Liang
 */
public class RoombaDiscoveryService extends AbstractDiscoveryService {
    private final static Logger logger = LoggerFactory.getLogger(RoombaDiscoveryService.class);

    public RoombaDiscoveryService() throws IllegalArgumentException {
        super(SUPPORTED_THING_TYPES_UIDS, 15, false);
    }

    /**
     * Scans for roombas by spawning a search thread
     */
    @Override
    protected void startScan() {
        logger.info("Starting discovery for iRobot Roombas");
        scheduler.execute(searchRunnable);
    }

    // Runnable for search adapter

    private Runnable searchRunnable = new Runnable() {

        @Override
        public void run() {
            logger.debug("Start adapter discovery ");
            logger.trace("Send broadcast message");
            DatagramSocket localSocket = null;

            try {
                localSocket = new DatagramSocket();
                localSocket.setBroadcast(true);
                localSocket.setSoTimeout(10000); // Listen 10 seconds

                byte[] sendData = BROADCAST_MESSAGE.getBytes();

                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,
                        InetAddress.getByName(BROADCAST_ADDRESS), BROADCAST_PORT);
                localSocket.send(sendPacket);

                byte[] receiveBuffer = new byte[1000];

                // Listen for answer

                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                localSocket.receive(receivePacket);
                String receiveMessage = new String(receivePacket.getData()).trim();
                String receiveIP = receivePacket.getAddress().getHostAddress();
                int receivePort = receivePacket.getPort();
                logger.trace("Received Message: {}  ", receiveMessage);
                logger.trace("Received from Host: {}", receiveIP);
                logger.trace("Received from Port: {}", receivePort);

                // Parse the response (json) into its POJO equivalent
                Gson gson = new Gson();
                RoombaDiscoveryResult discoveryResult = gson.fromJson(receiveMessage, RoombaDiscoveryResult.class);

                // Add the roomba as a discovery result
                addRoomba(discoveryResult);
            } catch (IOException e) {
                logger.info("Unable to find any Roomba!", e);
            } finally {
                try {
                    if (localSocket != null) {
                        localSocket.close();
                    }
                } catch (Exception e) {
                    logger.error("Failed to close socket!", e);
                }
            }
        }

    };

    /**
     * Adds a single Roomba from its parsed response.
     *
     * @param roombaDiscoveryResult The discovery result for the Roomba
     */
    private void addRoomba(RoombaDiscoveryResult roombaDiscoveryResult) {
        if (roombaDiscoveryResult == null) {
            logger.warn("Received an unexpected null response from the Roomba. Ignoring.");
            return;
        }

        Map<String, Object> properties = new HashMap<>(3);
        properties.put(THING_PROPERTY_IP_ADDRESS, roombaDiscoveryResult.getIp());

        ThingUID uid = new ThingUID(THING_TYPE_IROBOT_ROOMBA, roombaDiscoveryResult.getHostname());

        if (uid != null) {
            DiscoveryResult result = DiscoveryResultBuilder.create(uid).withProperties(properties)
                    .withLabel(roombaDiscoveryResult.getHostname()).build();
            thingDiscovered(result);
        }
    }

}
