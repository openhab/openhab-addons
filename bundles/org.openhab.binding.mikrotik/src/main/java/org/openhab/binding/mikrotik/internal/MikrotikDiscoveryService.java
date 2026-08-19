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
package org.openhab.binding.mikrotik.internal;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.time.Instant;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MikrotikDiscoveryService} class Detects Bridge things when a scan is done via the inbox.
 *
 * @author Matthew Skinner - Initial contribution
 */
@NonNullByDefault
@Component(service = DiscoveryService.class, immediate = true, configurationPid = "binding.mikrotik")
public class MikrotikDiscoveryService extends AbstractDiscoveryService {
    private final Logger logger = LoggerFactory.getLogger(MikrotikDiscoveryService.class);
    private static final int MNDP_PORT = 5678;
    private static final int TIMEOUT_MS = 3500;

    public MikrotikDiscoveryService() {
        super(Set.of(MikrotikBindingConstants.THING_TYPE_ROUTEROS), 6);
    }

    @Override
    protected void startScan() {
        removeOlderResults(Instant.now());
        logger.debug("Starting MNDP discovery scan for MikroTik routers");

        try (DatagramSocket txSocket = new DatagramSocket()) {
            txSocket.setBroadcast(true);
            // MNDP Request Header
            byte[] requestBytes = new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00, 0x06, 0x00, 0x00 };
            DatagramPacket pingPacket = new DatagramPacket(requestBytes, requestBytes.length,
                    InetAddress.getByName("255.255.255.255"), MNDP_PORT);
            txSocket.send(pingPacket);
        } catch (IOException e) {
            logger.error("Failed to send MNDP discovery probe: {}", e.getMessage());
        }
        Thread listenerThread = new Thread(() -> {
            try (DatagramSocket rxSocket = new DatagramSocket(MNDP_PORT)) {
                rxSocket.setSoTimeout(TIMEOUT_MS);
                byte[] receiveBuffer = new byte[2048];
                while (!Thread.currentThread().isInterrupted()) {
                    DatagramPacket responsePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                    try {
                        rxSocket.receive(responsePacket);
                        parseMndpPacket(responsePacket.getAddress(), responsePacket.getData());
                    } catch (SocketTimeoutException e) {
                        logger.debug("MNDP discovery scan completed (listen window timeout reached).");
                        break; // Exit the loop safely when openHAB timeout hits
                    }
                }
            } catch (IOException e) {
                logger.error("MNDP Port 5678, Ensure another process isn't holding the port: {}", e.getMessage());
            }
        });
        listenerThread.setName("openhab-mikrotik-mndp-listener");
        listenerThread.start();
    }

    private void parseMndpPacket(InetAddress routerOsIp, byte[] data) {
        String ipString = routerOsIp.getHostAddress();
        logger.debug("Discovered MikroTik Router successfully at: {}", ipString);
        String cleanedIP = ipString.replaceAll("[^a-zA-Z0-9]", "");
        ThingUID thingUID = new ThingUID(MikrotikBindingConstants.THING_TYPE_ROUTEROS, cleanedIP);
        Map<String, Object> properties = Map.of(MikrotikBindingConstants.CONFIG_HOSTNAME, ipString);
        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withLabel("MikroTik " + ipString)
                .withProperties(properties).withRepresentationProperty(MikrotikBindingConstants.CONFIG_HOSTNAME)
                .build();
        thingDiscovered(discoveryResult);
    }
}
