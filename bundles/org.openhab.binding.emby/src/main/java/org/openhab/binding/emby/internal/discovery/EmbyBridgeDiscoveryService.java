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
package org.openhab.binding.emby.internal.discovery;

import static org.openhab.binding.emby.internal.EmbyBindingConstants.CONFIG_DEVICE_ID;
import static org.openhab.binding.emby.internal.EmbyBindingConstants.CONFIG_HOST_PARAMETER;
import static org.openhab.binding.emby.internal.EmbyBindingConstants.CONFIG_WS_PORT_PARAMETER;
import static org.openhab.binding.emby.internal.EmbyBindingConstants.THING_TYPE_EMBY_CONTROLLER;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.emby.internal.protocol.EmbyDeviceEncoder;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.net.NetUtil;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * The {@EmbyBridgeDiscoveryService} handles the bridge discovery which finds emby servers on the network
 *
 * @author Zachary Christiansen - Initial contribution
 */
@NonNullByDefault
@Component(service = DiscoveryService.class, immediate = true, configurationPid = "discovery.embybridge")
public class EmbyBridgeDiscoveryService extends AbstractDiscoveryService {

    private static final String REQUEST_MSG = "who is EmbyServer?";
    private static final int REQUEST_PORT = 7359;

    private final Logger logger = LoggerFactory.getLogger(EmbyBridgeDiscoveryService.class);
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections
            .singleton(THING_TYPE_EMBY_CONTROLLER);

    public EmbyBridgeDiscoveryService() {
        super(SUPPORTED_THING_TYPES_UIDS, 30, false);
    }

    @Override
    public void startScan() {
        // Find the server using UDP broadcast
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setBroadcast(true);
            socket.setSoTimeout(5000);
            byte[] sendData = REQUEST_MSG.getBytes();

            // Send to 255.255.255.255 broadcast address
            try {
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,
                        InetAddress.getByName("255.255.255.255"), REQUEST_PORT);
                socket.send(sendPacket);
                logger.trace(">>> Request packet sent to: {} ({})", REQUEST_MSG, REQUEST_PORT);
            } catch (InterruptedIOException ie) {
                // Discovery loop was interrupted—stop scanning
                Thread.currentThread().interrupt();
                logger.debug("Discovery interrupted, exiting");
            }

            List<String> broadcastStrings = NetUtil.getAllBroadcastAddresses();
            for (String broadcastStr : broadcastStrings) {
                try {
                    InetAddress broadcast = InetAddress.getByName(broadcastStr);
                    socket.send(new DatagramPacket(sendData, sendData.length, broadcast, REQUEST_PORT));
                    logger.trace(">>> Request packet sent to: {}", broadcast.getHostAddress());
                } catch (IOException e) {
                    logger.warn("Failed to send broadcast to {}: {}", broadcastStr, e.getMessage());
                }
            }
            logger.trace(">>> Done sending broadcasts. Now waiting for a reply!");

            // Wait for a response
            byte[] recvBuf = new byte[15000];
            DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);

            try {
                socket.receive(receivePacket);

                // We have a response
                logger.debug(">>> Broadcast response from server: {}", receivePacket.getAddress().getHostAddress());
                String message = new String(receivePacket.getData(), StandardCharsets.UTF_8).trim();
                logger.debug("The message is {}", message);

                final Gson gson = new Gson();

                @Nullable
                JsonObject body = gson.fromJson(message, JsonObject.class);
                body = Objects.requireNonNull(body, "EmbyBridgeDiscoveryService: response body was null");

                String serverId = body.get("Id").getAsString();
                String serverName = body.get("Name").getAsString();
                String serverAddress = body.get("Address").getAsString();

                EmbyDeviceEncoder encoder = new EmbyDeviceEncoder();
                serverId = encoder.encodeDeviceID(serverId);

                try {
                    URI serverAddressURI = new URI(serverAddress);
                    addEMBYServer(serverAddressURI.getHost(), serverAddressURI.getPort(), serverId, serverName);
                } catch (URISyntaxException use) {
                    logger.error("Unexpected URI syntax: {}", use.getMessage(), use);
                    throw new IllegalStateException(use);
                }
            } catch (SocketTimeoutException timeout) {
                logger.debug("Socket receive timed out, no Emby server discovered");
            }
        } catch (IOException e) {
            logger.warn("Exception occurred during Emby server discovery: {}", e.getMessage(), e);
        }
    }

    public void addEMBYServer(String hostAddress, int embyPort, @Nullable String DeviceID, String Name) {
        logger.debug("creating discovery result with address: {}:{}, for server {}", hostAddress,
                Integer.toString(embyPort), Name);
        ThingUID thingUID = getThingUID(DeviceID);
        ThingTypeUID thingTypeUID = THING_TYPE_EMBY_CONTROLLER;

        if (thingUID != null && DeviceID != null) {
            Map<String, Object> properties = new HashMap<>();
            properties.put(CONFIG_DEVICE_ID, DeviceID);
            properties.put(CONFIG_HOST_PARAMETER, hostAddress);
            properties.put(CONFIG_WS_PORT_PARAMETER, Integer.toString(embyPort));

            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withThingType(thingTypeUID)
                    .withProperties(properties).withRepresentationProperty(CONFIG_DEVICE_ID).withLabel(Name).build();

            thingDiscovered(discoveryResult);
        } else {
            logger.debug("Unable to add {} found at {}:{} with id of {}", Name, hostAddress, embyPort, DeviceID);
        }
    }

    private @Nullable ThingUID getThingUID(@Nullable String DeviceID) {
        ThingTypeUID thingTypeUID = THING_TYPE_EMBY_CONTROLLER;
        if (DeviceID != null) {
            return new ThingUID(thingTypeUID, DeviceID);
        } else {
            return null;
        }
    }
}
