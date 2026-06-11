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
package org.openhab.binding.homematic.internal.discovery;

import static org.openhab.binding.homematic.internal.HomematicBindingConstants.THING_TYPE_BRIDGE;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.util.Set;
import java.util.concurrent.Future;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.homematic.internal.discovery.eq3udp.Eq3UdpRequest;
import org.openhab.binding.homematic.internal.discovery.eq3udp.Eq3UdpResponse;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.net.NetworkAddressService;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Discovers Homematic CCU's and adds the results to the inbox.
 *
 * @author Gerhard Riegler - Initial contribution
 */
@NonNullByDefault
@Component(service = DiscoveryService.class, configurationPid = "discovery.homematic")
public class CcuDiscoveryService extends AbstractDiscoveryService {
    private final Logger logger = LoggerFactory.getLogger(CcuDiscoveryService.class);

    private static final int RECEIVE_TIMEOUT_MSECS = 3000;
    private @Nullable InetAddress broadcastAddress;
    private @Nullable Future<?> scanFuture;
    private final NetworkAddressService networkAddressService;

    @Activate
    public CcuDiscoveryService(@Reference NetworkAddressService networkAddressService) {
        super(Set.of(THING_TYPE_BRIDGE), 5, true);
        this.networkAddressService = networkAddressService;
    }

    @Override
    protected void startScan() {
        if (scanFuture == null) {
            scanFuture = scheduler.submit(this::startDiscovery);
        } else {
            logger.debug("Homematic CCU background discovery scan in progress");
        }
    }

    @Override
    protected void stopScan() {
        Future<?> scanFuture = this.scanFuture;
        if (scanFuture != null) {
            scanFuture.cancel(false);
        }
        this.scanFuture = null;
    }

    @Override
    protected void startBackgroundDiscovery() {
        // only start once at startup
        startScan();
    }

    @Override
    protected void stopBackgroundDiscovery() {
        stopScan();
    }

    private synchronized void startDiscovery() {
        try {
            logger.debug("Starting Homematic CCU discovery scan");
            String configuredBroadcastAddress = networkAddressService.getConfiguredBroadcastAddress();
            if (configuredBroadcastAddress != null) {
                broadcastAddress = InetAddress.getByName(configuredBroadcastAddress);
            }
            if (broadcastAddress == null) {
                logger.warn("Homematic CCU discovery: discovery not possible, no broadcast address found");
                return;
            }
            try (MulticastSocket socket = new MulticastSocket()) {
                socket.setBroadcast(true);
                socket.setTimeToLive(5);
                socket.setSoTimeout(800);
                sendBroadcast(socket);
                receiveResponses(socket);
            }
        } catch (Exception ex) {
            logger.error("An error was thrown while running Homematic CCU discovery {}", ex.getMessage(), ex);
        } finally {
            scanFuture = null;
        }
    }

    /**
     * Sends a UDP hello broadcast message for CCU gateways.
     */
    private void sendBroadcast(MulticastSocket socket) throws IOException {
        Eq3UdpRequest hello = new Eq3UdpRequest();
        byte[] data = hello.getBytes();
        DatagramPacket packet = new DatagramPacket(data, data.length, broadcastAddress, 43439);
        socket.send(packet);
    }

    /**
     * Receives the UDP responses to the hello messages.
     */
    private void receiveResponses(MulticastSocket socket) throws IOException {
        long startTime = System.currentTimeMillis();
        long currentTime = System.currentTimeMillis();
        while (currentTime - startTime < RECEIVE_TIMEOUT_MSECS) {
            extractGatewayInfos(socket);
            currentTime = System.currentTimeMillis();
        }
    }

    /**
     * Extracts the CCU infos from the UDP response.
     */
    private void extractGatewayInfos(MulticastSocket socket) throws IOException {
        try {
            DatagramPacket packet = new DatagramPacket(new byte[265], 256);
            socket.receive(packet);

            Eq3UdpResponse response = new Eq3UdpResponse(packet.getData());
            logger.trace("Eq3UdpResponse: {}", response);
            if (response.isValid()) {
                logger.debug("Discovered a CCU gateway with serial number '{}'", response.getSerialNumber());

                String address = packet.getAddress().getHostAddress();
                ThingUID thingUid = new ThingUID(THING_TYPE_BRIDGE, response.getSerialNumber());
                thingDiscovered(DiscoveryResultBuilder.create(thingUid).withProperty("gatewayAddress", address)
                        .withRepresentationProperty("gatewayAddress")
                        .withLabel(response.getDeviceTypeId() + " - " + address).build());
            }
        } catch (SocketTimeoutException ex) {
            // ignore
        }
    }
}
