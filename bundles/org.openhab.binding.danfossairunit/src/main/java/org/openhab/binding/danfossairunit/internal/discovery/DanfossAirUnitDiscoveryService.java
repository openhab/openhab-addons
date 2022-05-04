/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.danfossairunit.internal.discovery;

import static org.openhab.binding.danfossairunit.internal.DanfossAirUnitBindingConstants.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Discovery service implementation to scan for available air units in the network via broadcast.
 *
 * @author Ralf Duckstein - Initial contribution
 * @author Robert Bach - heavy refactorings
 */
@Component(service = DiscoveryService.class)
@NonNullByDefault
public class DanfossAirUnitDiscoveryService extends AbstractDiscoveryService {

    private static final int BROADCAST_PORT = 30045;
    private static final byte[] DISCOVER_SEND = { 0x0c, 0x00, 0x30, 0x00, 0x11, 0x00, 0x12, 0x00, 0x13 };
    private static final byte[] DISCOVER_RECEIVE = { 0x0d, 0x00, 0x07, 0x00, 0x02, 0x02, 0x00 };
    private static final int TIMEOUT_IN_SECONDS = 15;

    private final Logger logger = LoggerFactory.getLogger(DanfossAirUnitDiscoveryService.class);

    public DanfossAirUnitDiscoveryService() {
        super(SUPPORTED_THING_TYPES_UIDS, TIMEOUT_IN_SECONDS, true);
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return SUPPORTED_THING_TYPES_UIDS;
    }

    @Override
    protected void startBackgroundDiscovery() {
        logger.debug("Start Danfoss Air CCM background discovery");
        scheduler.execute(this::discover);
    }

    @Override
    public void startScan() {
        logger.debug("Start Danfoss Air CCM scan");
        discover();
    }

    private synchronized void discover() {
        logger.debug("Try to discover all Danfoss Air CCM devices");

        try (DatagramSocket socket = new DatagramSocket()) {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                @Nullable
                NetworkInterface networkInterface = interfaces.nextElement();
                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue;
                }
                for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                    if (interfaceAddress.getBroadcast() == null) {
                        continue;
                    }
                    logger.debug("Sending broadcast on interface {} to discover Danfoss Air CCM device...",
                            interfaceAddress.getAddress());
                    sendBroadcastToDiscoverThing(socket, interfaceAddress.getBroadcast());
                }
            }
        } catch (IOException e) {
            logger.debug("No Danfoss Air CCM device found. Diagnostic: {}", e.getMessage());
        }
    }

    private void sendBroadcastToDiscoverThing(DatagramSocket socket, InetAddress broadcastAddress) throws IOException {
        socket.setBroadcast(true);
        socket.setSoTimeout(500);
        // send discover
        byte[] sendBuffer = DISCOVER_SEND;
        DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, broadcastAddress, BROADCAST_PORT);
        socket.send(sendPacket);
        logger.debug("Discover message sent");

        // wait for responses
        while (true) {
            byte[] receiveBuffer = new byte[7];
            DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
            try {
                socket.receive(receivePacket);
            } catch (SocketTimeoutException e) {
                break; // leave the endless loop
            }

            byte[] data = receivePacket.getData();
            if (Arrays.equals(data, DISCOVER_RECEIVE)) {
                logger.debug("Discover received correct response");

                String host = receivePacket.getAddress().getHostName();
                Map<String, Object> properties = new HashMap<>();
                properties.put("host", host);

                logger.debug("Adding a new Danfoss Air Unit CCM '{}' to inbox", host);

                ThingUID uid = new ThingUID(THING_TYPE_AIRUNIT, String.valueOf(receivePacket.getAddress().hashCode()));

                DiscoveryResult result = DiscoveryResultBuilder.create(uid).withRepresentationProperty("host")
                        .withProperties(properties).withLabel("Danfoss HRV").build();
                thingDiscovered(result);

                logger.debug("Thing discovered '{}'", result);
            }
        }
    }
}
