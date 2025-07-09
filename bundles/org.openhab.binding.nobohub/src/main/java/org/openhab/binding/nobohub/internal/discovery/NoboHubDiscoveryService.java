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
package org.openhab.binding.nobohub.internal.discovery;

import static org.openhab.binding.nobohub.internal.NoboHubBindingConstants.NOBO_HUB_BROADCAST_ADDRESS;
import static org.openhab.binding.nobohub.internal.NoboHubBindingConstants.NOBO_HUB_BROADCAST_PORT;
import static org.openhab.binding.nobohub.internal.NoboHubBindingConstants.NOBO_HUB_MULTICAST_PORT;
import static org.openhab.binding.nobohub.internal.NoboHubBindingConstants.PROPERTY_HOSTNAME;
import static org.openhab.binding.nobohub.internal.NoboHubBindingConstants.PROPERTY_NAME;
import static org.openhab.binding.nobohub.internal.NoboHubBindingConstants.PROPERTY_VENDOR_NAME;
import static org.openhab.binding.nobohub.internal.NoboHubBindingConstants.THING_TYPE_HUB;
import static org.openhab.binding.nobohub.internal.NoboHubHandlerFactory.DISCOVERABLE_DEVICE_TYPES_UIDS;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.nobohub.internal.NoboHubBridgeHandler;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class identifies devices that are available on the Nobø hub and adds discovery results for them.
 *
 * @author Jørgen Austvik - Initial contribution
 * @author Espen Fossen - Initial contribution
 */
@NonNullByDefault
@Component(scope = ServiceScope.PROTOTYPE, service = NoboHubDiscoveryService.class, configurationPid = "discovery.nobohub")
public class NoboHubDiscoveryService extends AbstractThingHandlerDiscoveryService<NoboHubBridgeHandler> {
    private final Logger logger = LoggerFactory.getLogger(NoboHubDiscoveryService.class);

    public NoboHubDiscoveryService() {
        super(NoboHubBridgeHandler.class, DISCOVERABLE_DEVICE_TYPES_UIDS, 10, true);
    }

    @Override
    protected void startScan() {
        scheduler.execute(scanner);
    }

    @Override
    protected synchronized void stopScan() {
        super.stopScan();
        removeOlderResults(getTimestampOfLastScan());
    }

    @Override
    public void dispose() {
        super.dispose();
        removeOlderResults(Instant.now());
    }

    private final Runnable scanner = new Runnable() {
        @Override
        public void run() {
            boolean found = false;
            logger.info("Detecting Glen Dimplex Nobø Hubs, trying Multicast");
            try {
                MulticastSocket socket = new MulticastSocket(NOBO_HUB_MULTICAST_PORT);
                found = waitOnSocket(socket, "multicast");
            } catch (IOException ioex) {
                logger.error("Failed detecting Nobø Hub via multicast", ioex);
            }

            if (!found) {
                logger.debug("Detecting Glen Dimplex Nobø Hubs, trying Broadcast");

                try {
                    DatagramSocket socket = new DatagramSocket(NOBO_HUB_BROADCAST_PORT,
                            InetAddress.getByName(NOBO_HUB_BROADCAST_ADDRESS));
                    found = waitOnSocket(socket, "broadcast");
                } catch (IOException ioex) {
                    logger.error("Failed detecting Nobø Hub via multicast, will try with Broadcast", ioex);
                }
            }
        }

        private boolean waitOnSocket(DatagramSocket socket, String type) throws IOException {
            try (socket) {
                socket.setBroadcast(true);

                byte[] buffer = new byte[1024];
                DatagramPacket data = new DatagramPacket(buffer, buffer.length);
                String received = "";
                while (!received.startsWith("__NOBOHUB__")) {
                    socket.setSoTimeout((int) Duration.ofSeconds(4).toMillis());
                    socket.receive(data);
                    received = new String(buffer, 0, data.getLength());
                }

                logger.debug("Hub detection using {}: Received: {} from {}", type, received, data.getAddress());

                String[] parts = received.split("__", 3);
                if (3 != parts.length) {
                    logger.debug("Data error, didn't contain three parts: '{}''", String.join("','", parts));
                    return false;
                }

                String serialNumberStart = parts[parts.length - 1];
                addDevice(serialNumberStart, data.getAddress().getHostName());
                return true;
            }
        }

        private void addDevice(String serialNumberStart, String hostName) {
            ThingUID bridge = new ThingUID(THING_TYPE_HUB, serialNumberStart);
            String label = "Nobø Hub " + serialNumberStart;

            Map<String, Object> properties = new HashMap<>(4);
            properties.put(Thing.PROPERTY_SERIAL_NUMBER, serialNumberStart);
            properties.put(PROPERTY_NAME, label);
            properties.put(Thing.PROPERTY_VENDOR, PROPERTY_VENDOR_NAME);
            properties.put(PROPERTY_HOSTNAME, hostName);

            logger.debug("Adding device {} to inbox: {} {} at {}", bridge, label, serialNumberStart, hostName);
            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(bridge).withLabel(label)
                    .withProperties(properties).withRepresentationProperty(Thing.PROPERTY_SERIAL_NUMBER).build();
            thingDiscovered(discoveryResult);
        }
    };
}
