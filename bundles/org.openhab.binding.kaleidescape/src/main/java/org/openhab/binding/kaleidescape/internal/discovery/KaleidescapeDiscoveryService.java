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
package org.openhab.binding.kaleidescape.internal.discovery;

import static org.openhab.binding.kaleidescape.internal.KaleidescapeBindingConstants.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.common.NamedThreadFactory;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link KaleidescapeDiscoveryService} class allows manual discovery of legacy Premiere line components.
 *
 * @author Chris Graham - Initial contribution
 * @author Michael Lobstein - Adapted for the Kaleidescape binding
 *
 */
@NonNullByDefault
@Component(service = DiscoveryService.class, configurationPid = "discovery.kaleidescape")
public class KaleidescapeDiscoveryService extends AbstractDiscoveryService {
    private final Logger logger = LoggerFactory.getLogger(KaleidescapeDiscoveryService.class);
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_PLAYER,
            THING_TYPE_CINEMA_ONE);

    private static final int K_HEARTBEAT_PORT = 1443;

    // Component Types
    private static final String PLAYER = "Player";
    private static final String CINEMA_ONE = "Cinema One";
    private static final String DISC_VAULT = "Disc Vault";

    private static final Set<String> ALLOWED_DEVICES = Set.of(PLAYER, CINEMA_ONE, DISC_VAULT);

    @Nullable
    private ExecutorService executorService = null;

    /**
     * Whether we are currently scanning or not
     */
    private boolean scanning;

    private Set<String> foundIPs = new HashSet<>();

    @Activate
    public KaleidescapeDiscoveryService() {
        super(SUPPORTED_THING_TYPES_UIDS, DISCOVERY_DEFAULT_TIMEOUT_RATE_MS, DISCOVERY_DEFAULT_AUTO_DISCOVER);
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return SUPPORTED_THING_TYPES_UIDS;
    }

    /**
     * {@inheritDoc}
     *
     * Starts the scan. This discovery will:
     * <ul>
     * <li>Create a listening thread that opens up a broadcast {@link DatagramSocket} on port {@link #K_HEARTBEAT_PORT}
     * and will receive any {@link DatagramPacket} that comes in</li>
     * <li>The source IP address of the {@link DatagramPacket} is interrogated to verify it is a Kaleidescape component
     * and will create a new thing from it</li>
     * </ul>
     * The process will continue until {@link #stopScan()} is called.
     */
    @Override
    protected void startScan() {
        logger.debug("Starting discovery of Kaleidescape components.");

        if (executorService != null) {
            stopScan();
        }

        final ExecutorService service = Executors.newFixedThreadPool(DISCOVERY_THREAD_POOL_SIZE,
                new NamedThreadFactory("OH-binding-discovery.kaleidescape", true));
        executorService = service;

        scanning = true;
        foundIPs.clear();

        service.execute(() -> {
            try {
                DatagramSocket dSocket = new DatagramSocket(K_HEARTBEAT_PORT);
                dSocket.setSoTimeout(DISCOVERY_DEFAULT_TIMEOUT_RATE_MS);
                dSocket.setBroadcast(true);

                while (scanning) {
                    DatagramPacket receivePacket = new DatagramPacket(new byte[1], 1);
                    try {
                        dSocket.receive(receivePacket);

                        if (!foundIPs.contains(receivePacket.getAddress().getHostAddress())) {
                            String foundIp = receivePacket.getAddress().getHostAddress();
                            logger.debug("RECEIVED Kaleidescape packet from: {}", foundIp);
                            foundIPs.add(foundIp);
                            isKaleidescapeDevice(foundIp);
                        }
                    } catch (SocketTimeoutException e) {
                        // ignore
                        continue;
                    }
                }

                dSocket.close();
            } catch (IOException e) {
                logger.debug("KaleidescapeDiscoveryService IOException: {}", e.getMessage(), e);
            }
        });
    }

    /**
     * {@inheritDoc}
     *
     * Stops the discovery scan. We set {@link #scanning} to false (allowing the listening thread to end naturally
     * within {@link #DISCOVERY_DEFAULT_TIMEOUT_RATE_MS} * 5 time then shutdown the {@link #ExecutorService}
     */
    @Override
    protected synchronized void stopScan() {
        super.stopScan();
        ExecutorService service = executorService;
        if (service == null) {
            return;
        }

        scanning = false;

        try {
            service.awaitTermination(DISCOVERY_DEFAULT_TIMEOUT_RATE_MS * 5, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
        }
        service.shutdown();
        executorService = null;
    }

    /**
     * Tries to establish a connection to the specified ip address and then interrogate the component,
     * creates a discovery result if a valid component is found.
     *
     * @param ipAddress IP address to connect to
     */
    private void isKaleidescapeDevice(String ipAddress) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(ipAddress, DEFAULT_API_PORT), DISCOVERY_DEFAULT_IP_TIMEOUT_RATE_MS);

            OutputStream output = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);

            // query the component to see if it has video zones, the device type, friendly name, and serial number
            writer.println("01/1/GET_NUM_ZONES:");
            writer.println("01/1/GET_DEVICE_TYPE_NAME:");
            writer.println("01/1/GET_FRIENDLY_NAME:");
            writer.println("01/1/GET_DEVICE_INFO:");

            InputStream input = socket.getInputStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(input));

            String friendlyName = EMPTY;
            String serialNumber = EMPTY;
            String componentType = EMPTY;
            String line;
            String videoZone = null;
            int lineCount = 0;

            while ((line = reader.readLine()) != null) {
                String[] strArr = line.split(":");

                if (strArr.length >= 4) {
                    switch (strArr[1]) {
                        case "NUM_ZONES":
                            videoZone = strArr[2];
                            break;
                        case "DEVICE_TYPE_NAME":
                            componentType = strArr[2];
                            break;
                        case "FRIENDLY_NAME":
                            friendlyName = strArr[2];
                            break;
                        case "DEVICE_INFO":
                            serialNumber = strArr[3].trim(); // take off leading zeros
                            break;
                    }
                } else {
                    logger.debug("isKaleidescapeDevice() - Unable to process line: {}", line);
                }

                lineCount++;

                // stop after reading four lines
                if (lineCount > 3) {
                    break;
                }
            }

            // see if we have a video zone and are one of the allowed types
            if ("01".equals(videoZone) && ALLOWED_DEVICES.contains(componentType)) {
                // default THING_TYPE_PLAYER for Any KPlayer, M Class [M300, M500, M700] or Cinema One 1st Gen
                // Cinema One 2nd Gen uses THING_TYPE_CINEMA_ONE

                submitDiscoveryResults(CINEMA_ONE.equals(componentType) ? THING_TYPE_CINEMA_ONE : THING_TYPE_PLAYER,
                        ipAddress, friendlyName, serialNumber);
            } else {
                logger.debug("No Suitable Kaleidescape component found at IP address ({})", ipAddress);
            }
            reader.close();
            input.close();
            writer.close();
            output.close();
        } catch (IOException e) {
            logger.debug("isKaleidescapeDevice() IOException: {}", e.getMessage());
        }
    }

    /**
     * Create a new Thing with an IP address and Component type given. Uses default port.
     *
     * @param thingTypeUid ThingTypeUID of detected Kaleidescape component.
     * @param ip IP address of the Kaleidescape component as a string.
     * @param friendlyName Name of Kaleidescape component as a string.
     * @param serialNumber Serial Number of Kaleidescape component as a string.
     */
    private void submitDiscoveryResults(ThingTypeUID thingTypeUid, String ip, String friendlyName,
            String serialNumber) {
        ThingUID uid = new ThingUID(thingTypeUid, serialNumber);

        HashMap<String, Object> properties = new HashMap<>();

        properties.put(PROPERTY_HOST_NAME, ip);
        properties.put(PROPERTY_PORT_NUM, DEFAULT_API_PORT);

        thingDiscovered(DiscoveryResultBuilder.create(uid).withProperties(properties)
                .withRepresentationProperty(PROPERTY_HOST_NAME).withLabel(friendlyName).build());
    }
}
