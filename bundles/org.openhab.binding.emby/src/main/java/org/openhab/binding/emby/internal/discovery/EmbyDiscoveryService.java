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
package org.openhab.binding.emby.internal.discovery;

import static org.openhab.binding.emby.internal.EmbyBindingConstants.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.emby.internal.protocol.EmbyDeviceEncoder;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * The {@EmbyDiscoveryService} handles the bridge discovery which find emby servers on the network
 *
 * @author Zachary Christiansen - Initial contribution
 */
@NonNullByDefault
@Component(service = DiscoveryService.class, immediate = true, configurationPid = "discovery.emby")
public class EmbyDiscoveryService extends AbstractDiscoveryService {

    private static final String REQUEST_MSG = "who is EmbyServer?";
    private static final int REQUEST_PORT = 7359;

    private final Logger logger = LoggerFactory.getLogger(EmbyDiscoveryService.class);
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections
            .singleton(THING_TYPE_EMBY_CONTROLLER);

    public EmbyDiscoveryService() {
        super(SUPPORTED_THING_TYPES_UIDS, 30, false);
    }

    @Activate
    @Override
    protected void activate(@Nullable Map<String, @Nullable Object> configProperties) {
        super.activate(configProperties);
    }

    @Modified
    @Override
    protected void modified(@Nullable Map<String, @Nullable Object> configProperties) {
        super.modified(configProperties);
    }

    @Deactivate
    @Override
    protected void deactivate() {
        super.deactivate();
    }

    @Override
    public void startScan() {
        // Find the server using UDP broadcast
        try {
            // Open a random port to send the package
            DatagramSocket c = new DatagramSocket();
            c.setBroadcast(true);

            byte[] sendData = REQUEST_MSG.getBytes();

            // Try the 255.255.255.255 first
            try {
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,
                        InetAddress.getByName("255.255.255.255"), REQUEST_PORT);
                c.send(sendPacket);
                logger.debug(">>> Request packet sent to: {} ({})", REQUEST_MSG, REQUEST_PORT);
            } catch (Exception e) {
            }

            // Broadcast the message over all the network interfaces
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();

                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue; // Don't want to broadcast to the loopback interface
                }

                for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                    InetAddress broadcast = interfaceAddress.getBroadcast();
                    if (broadcast == null) {
                        continue;
                    }

                    // Send the broadcast package!
                    try {
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, broadcast,
                                REQUEST_PORT);
                        c.send(sendPacket);
                    } catch (Exception e) {
                    }

                    logger.debug(">>> Request packet sent to: {} ; Interface: {}  ", broadcast.getHostAddress(),
                            networkInterface.getDisplayName());
                }
            }

            logger.debug(">>> Done looping over all network interfaces. Now waiting for a reply!");

            // Wait for a response
            byte[] recvBuf = new byte[15000];
            DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);
            c.receive(receivePacket);

            // We have a response
            logger.debug(">>> Broadcast response from server:{}", receivePacket.getAddress().getHostAddress());

            // Check if the message is correct
            String message = new String(receivePacket.getData()).trim();

            logger.debug("The message is {}", message);

            // DO SOMETHING WITH THE SERVER'S IP (for example, store it in your controller)
            receivePacket.getAddress();

            Gson gson = new Gson();

            JsonObject body = gson.fromJson(message, JsonObject.class);
            String serverId = body.get("Id").getAsString();
            String serverName = body.get("Name").getAsString();
            String serverAddress = body.get("Address").getAsString();
            EmbyDeviceEncoder encode = new EmbyDeviceEncoder();
            serverId = encode.encodeDeviceID(serverId);
            try {
                URI serverAddressURI = new URI(serverAddress);
                addEMBYServer(serverAddressURI.getHost(), serverAddressURI.getPort(), serverId, serverName);
            } catch (URISyntaxException e) {
                logger.debug("unable to parse URI from Emby server address: {}", e.getMessage());
            }

            // Close the port!
            c.close();
        } catch (IOException ex) {
            logger.debug("The exception was: {}", ex.getMessage());
        }
    }

    public void addEMBYServer(String hostAddress, int embyPort, String DeviceID, String Name) {
        logger.debug("creating discovery result with address: {}:{}, for server {}", hostAddress,
                Integer.toString(embyPort), Name);
        ThingUID thingUID = getThingUID(DeviceID);
        ThingTypeUID thingTypeUID = THING_TYPE_EMBY_CONTROLLER;

        if (thingUID != null && DeviceID != null) {
            Map<String, Object> properties = new HashMap<>();
            properties.put(DEVICE_ID, DeviceID);
            properties.put(HOST_PARAMETER, hostAddress);
            properties.put(WS_PORT_PARAMETER, Integer.toString(embyPort));

            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withThingType(thingTypeUID)
                    .withProperties(properties).withRepresentationProperty(DEVICE_ID).withLabel(Name).build();

            thingDiscovered(discoveryResult);
        } else {
            logger.debug("Unable to add {} found at {}:{} with id of {}", Name, hostAddress, embyPort, DeviceID);
        }
    }

    private @Nullable ThingUID getThingUID(String DeviceID) {
        ThingTypeUID thingTypeUID = THING_TYPE_EMBY_CONTROLLER;
        if (DeviceID != null) {
            return new ThingUID(thingTypeUID, DeviceID);
        } else {
            return null;
        }
    }
}
