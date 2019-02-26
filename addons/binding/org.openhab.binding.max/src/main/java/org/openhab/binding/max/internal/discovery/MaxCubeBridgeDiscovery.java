/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.max.internal.discovery;

import static org.eclipse.smarthome.core.thing.Thing.PROPERTY_SERIAL_NUMBER;
import static org.openhab.binding.max.internal.MaxBindingConstants.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.max.internal.Utils;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MaxCubeBridgeDiscovery} is responsible for discovering new MAX!
 * Cube LAN gateway devices on the network
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@Component(service = DiscoveryService.class, immediate = true, configurationPid = "discovery.max")
public class MaxCubeBridgeDiscovery extends AbstractDiscoveryService {

    private static final String MAXCUBE_DISCOVER_STRING = "eQ3Max*\0**********I";
    private static final int SEARCH_TIME = 15;

    private final Logger logger = LoggerFactory.getLogger(MaxCubeBridgeDiscovery.class);

    protected static boolean discoveryRunning;

    /** The refresh interval for discovery of MAX! Cubes */
    private static final long SEARCH_INTERVAL = 600;
    private ScheduledFuture<?> cubeDiscoveryJob;

    public MaxCubeBridgeDiscovery() {
        super(SEARCH_TIME);
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return SUPPORTED_BRIDGE_THING_TYPES_UIDS;
    }

    @Override
    public void startScan() {
        logger.debug("Start MAX! Cube discovery");
        discoverCube();
    }

    @Override
    protected void stopBackgroundDiscovery() {
        logger.debug("Stop MAX! Cube background discovery");
        if (cubeDiscoveryJob != null && !cubeDiscoveryJob.isCancelled()) {
            cubeDiscoveryJob.cancel(true);
            cubeDiscoveryJob = null;
        }
    }

    @Override
    protected void startBackgroundDiscovery() {
        logger.debug("Start MAX! Cube background discovery");
        if (cubeDiscoveryJob == null || cubeDiscoveryJob.isCancelled()) {
            cubeDiscoveryJob = scheduler.scheduleWithFixedDelay(this::discoverCube, 0, SEARCH_INTERVAL,
                    TimeUnit.SECONDS);
        }
    }

    private synchronized void discoverCube() {
        logger.debug("Run MAX! Cube discovery");
        sendDiscoveryMessage(MAXCUBE_DISCOVER_STRING);
        logger.trace("Done sending broadcast discovery messages.");
        receiveDiscoveryMessage();
        logger.debug("Done receiving discovery messages.");
    }

    private void receiveDiscoveryMessage() {
        try (final DatagramSocket bcReceipt = new DatagramSocket(23272)) {
            discoveryRunning = true;
            bcReceipt.setReuseAddress(true);
            bcReceipt.setSoTimeout(5000);

            while (discoveryRunning) {
                // Wait for a response
                final byte[] recvBuf = new byte[1500];
                final DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);
                bcReceipt.receive(receivePacket);

                // We have a response
                final byte[] messageBuf = Arrays.copyOfRange(receivePacket.getData(), receivePacket.getOffset(),
                        receivePacket.getOffset() + receivePacket.getLength());
                final String message = new String(messageBuf, StandardCharsets.UTF_8);
                logger.trace("Broadcast response from {} : {} '{}'", receivePacket.getAddress(), message.length(),
                        message);

                // Check if the message is correct
                if (message.startsWith("eQ3Max") && !message.equals(MAXCUBE_DISCOVER_STRING)) {
                    String maxCubeIP = receivePacket.getAddress().getHostAddress();
                    String maxCubeState = message.substring(0, 8);
                    String serialNumber = message.substring(8, 18);
                    String msgValidid = message.substring(18, 19);
                    String requestType = message.substring(19, 20);
                    String rfAddress = "";
                    logger.debug("MAX! Cube found on network");
                    logger.debug("Found at  : {}", maxCubeIP);
                    logger.trace("Cube State: {}", maxCubeState);
                    logger.debug("Serial    : {}", serialNumber);
                    logger.trace("Msg Valid : {}", msgValidid);
                    logger.trace("Msg Type  : {}", requestType);

                    if (requestType.equals("I")) {
                        rfAddress = Utils.getHex(Arrays.copyOfRange(messageBuf, 21, 24)).replace(" ", "").toLowerCase();
                        String firmwareVersion = Utils.getHex(Arrays.copyOfRange(messageBuf, 24, 26)).replace(" ", ".");
                        logger.debug("RF Address: {}", rfAddress);
                        logger.debug("Firmware  : {}", firmwareVersion);
                    }
                    discoveryResultSubmission(maxCubeIP, serialNumber, rfAddress);
                }
            }
        } catch (SocketTimeoutException e) {
            logger.trace("No further response");
            discoveryRunning = false;
        } catch (IOException e) {
            logger.debug("IO error during MAX! Cube discovery: {}", e.getMessage());
            discoveryRunning = false;
        }
    }

    private void discoveryResultSubmission(String IpAddress, String cubeSerialNumber, String rfAddress) {
        if (cubeSerialNumber != null) {
            logger.trace("Adding new MAX! Cube Lan Gateway on {} with id '{}' to Smarthome inbox", IpAddress,
                    cubeSerialNumber);
            Map<String, Object> properties = new HashMap<>(2);
            properties.put(PROPERTY_IP_ADDRESS, IpAddress);
            properties.put(PROPERTY_SERIAL_NUMBER, cubeSerialNumber);
            properties.put(PROPERTY_RFADDRESS, rfAddress);
            ThingUID uid = new ThingUID(CUBEBRIDGE_THING_TYPE, cubeSerialNumber);
            thingDiscovered(DiscoveryResultBuilder.create(uid).withProperties(properties)
                    .withRepresentationProperty(PROPERTY_SERIAL_NUMBER).withThingType(CUBEBRIDGE_THING_TYPE)
                    .withLabel("MAX! Cube LAN Gateway").build());
        }
    }

    /**
     * Send broadcast message over all active interfaces
     *
     * @param discoverString
     *            String to be used for the discovery
     */
    private void sendDiscoveryMessage(String discoverString) {
        // Find the MaxCube using UDP broadcast
        try (DatagramSocket bcSend = new DatagramSocket()) {
            bcSend.setBroadcast(true);

            byte[] sendData = discoverString.getBytes(StandardCharsets.UTF_8);

            // Broadcast the message over all the network interfaces
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue;
                }
                for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                    InetAddress[] broadcast = new InetAddress[3];
                    broadcast[0] = InetAddress.getByName("224.0.0.1");
                    broadcast[1] = InetAddress.getByName("255.255.255.255");
                    broadcast[2] = interfaceAddress.getBroadcast();
                    for (InetAddress bc : broadcast) {
                        // Send the broadcast package!
                        if (bc != null) {
                            try {
                                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, bc, 23272);
                                bcSend.send(sendPacket);
                            } catch (IOException e) {
                                logger.debug("IO error during MAX! Cube discovery: {}", e.getMessage());
                            } catch (Exception e) {
                                logger.debug("{}", e.getMessage(), e);
                            }
                            logger.trace("Request packet sent to: {} Interface: {}", bc.getHostAddress(),
                                    networkInterface.getDisplayName());
                        }
                    }
                }
            }
            logger.trace("Done looping over all network interfaces. Now waiting for a reply!");

        } catch (IOException e) {
            logger.debug("IO error during MAX! Cube discovery: {}", e.getMessage());
        }
    }

}
