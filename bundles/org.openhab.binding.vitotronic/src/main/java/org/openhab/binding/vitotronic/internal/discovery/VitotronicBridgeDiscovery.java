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
package org.openhab.binding.vitotronic.internal.discovery;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.vitotronic.internal.VitotronicBindingConstants;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link VitotronicBridgeDiscovery} class handles the discovery of optolink adapter
 * with broadcasting and put it to inbox, if found.
 *
 *
 * @author Stefan Andres - Initial contribution
 */
@NonNullByDefault
@Component(service = DiscoveryService.class, configurationPid = "discovery.vitotronic")
public class VitotronicBridgeDiscovery extends AbstractDiscoveryService {

    private int adapterPort = 31113;

    private final Logger logger = LoggerFactory.getLogger(VitotronicBridgeDiscovery.class);

    public VitotronicBridgeDiscovery() throws IllegalArgumentException {
        super(VitotronicBindingConstants.SUPPORTED_BRIDGE_THING_TYPES_UIDS, 15, false);
    }

    @Override
    protected void startScan() {
        logger.trace("Start discovery of Vitotronic Optolink Adapter (VOP)");
        adapterPort = VitotronicBindingConstants.BROADCAST_PORT;
        scheduler.execute(searchRunnable);
    }

    // Runnable for search adapter

    private Runnable searchRunnable = () -> {
        logger.trace("Start adapter discovery ");
        logger.debug("Send broadcast message");
        try (DatagramSocket localSocket = new DatagramSocket()) {
            localSocket.setBroadcast(true);
            localSocket.setSoTimeout(10000); // Listen 10 seconds

            String broadcastMsg = VitotronicBindingConstants.BROADCAST_MESSAGE + "*";
            byte[] sendData = broadcastMsg.getBytes();

            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,
                    InetAddress.getByName("255.255.255.255"), adapterPort);
            localSocket.send(sendPacket);

            byte[] receiveBuffer = new byte[255];

            // Listen for answer

            DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
            localSocket.receive(receivePacket);
            String receiveMessage = new String(receivePacket.getData()).trim();
            String receiveIP = receivePacket.getAddress().getHostAddress();
            int receivePort = receivePacket.getPort();
            logger.debug("Received Message: {}  ", receiveMessage);
            logger.debug("Received from Host: {}", receiveIP);
            logger.debug("Received from Port: {}", receivePort);

            if (receiveMessage.startsWith(VitotronicBindingConstants.BROADCAST_MESSAGE)) {
                // register bridge
                String adapterID = receiveMessage.substring(VitotronicBindingConstants.BROADCAST_MESSAGE.length())
                        .toUpperCase();
                addAdapter(receiveIP, receivePort, adapterID);
            }
        } catch (IOException e) {
            logger.debug("No optolink adapter found!");
        }
    };

    private void addAdapter(String remoteIP, int remotePort, String adapterID) {
        Map<String, Object> properties = new HashMap<>(3);
        properties.put(VitotronicBindingConstants.IP_ADDRESS, remoteIP);
        properties.put(VitotronicBindingConstants.PORT, remotePort);
        properties.put(VitotronicBindingConstants.ADAPTER_ID, adapterID);

        ThingUID uid = new ThingUID(VitotronicBindingConstants.THING_TYPE_UID_BRIDGE, adapterID);
        thingDiscovered(DiscoveryResultBuilder.create(uid).withProperties(properties).withLabel(adapterID).build());
    }
}
