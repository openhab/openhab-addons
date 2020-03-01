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
package org.openhab.binding.playstation.internal.discovery;

import static org.openhab.binding.playstation.internal.PlayStationBindingConstants.*;
import static org.openhab.binding.playstation.internal.SonyPS4Configuration.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PlayStationDiscovery} is responsible for discovering
 * all PS4 devices
 *
 * @author Fredrik Ahlström - Initial contribution
 */
@NonNullByDefault
@Component(service = { DiscoveryService.class, PlayStationDiscovery.class }, configurationPid = "binding.playstation")
public class PlayStationDiscovery extends AbstractDiscoveryService {

    private final Logger logger = LoggerFactory.getLogger(PlayStationDiscovery.class);

    private static final int DISCOVERY_TIMEOUT_SECONDS = 2;

    public PlayStationDiscovery() {
        super(SUPPORTED_THING_TYPES_UIDS, DISCOVERY_TIMEOUT_SECONDS * 2, true);
    }

    /**
     * Activates the Discovery Service.
     */
    @Override
    public void activate(@Nullable Map<String, @Nullable Object> configProperties) {
    }

    /**
     * Deactivates the Discovery Service.
     */
    @Override
    public void deactivate() {
    }

    @Override
    protected void startScan() {
        logger.debug("Updating discovered things (new scan)");
        discover();
        discoverPS3();
    }

    private synchronized void discover() {
        logger.debug("Trying to discover all PS4 devices");

        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setBroadcast(true);
            socket.setSoTimeout(DISCOVERY_TIMEOUT_SECONDS * 1000);

            InetAddress inetAddress = InetAddress.getByName("255.255.255.255");

            // send discover
            byte[] discover = "SRCH * HTTP/1.1\ndevice-discovery-protocol-version:00020020\n".getBytes();
            DatagramPacket packet = new DatagramPacket(discover, discover.length, inetAddress, DEFAULT_BROADCAST_PORT);
            socket.send(packet);
            logger.debug("Disover message sent: '{}'", discover);

            // wait for responses
            while (true) {
                byte[] rxbuf = new byte[256];
                packet = new DatagramPacket(rxbuf, rxbuf.length);
                try {
                    socket.receive(packet);
                } catch (SocketTimeoutException e) {
                    break; // leave the endless loop
                }

                parsePS4Packet(packet);
            }
        } catch (IOException e) {
            logger.debug("No PS4 device found. Diagnostic: {}", e.getMessage());
        }
    }

    private synchronized void discoverPS3() {
        logger.debug("Trying to discover all PS3 devices");

        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setBroadcast(true);
            socket.setSoTimeout(DISCOVERY_TIMEOUT_SECONDS * 1000);

            InetAddress inetAddress = InetAddress.getByName("255.255.255.255");

            // send discover
            byte[] discover = "SRCH".getBytes();
            DatagramPacket packet = new DatagramPacket(discover, discover.length, inetAddress,
                    DEFAULT_PS3_BROADCAST_PORT);
            socket.send(packet);
            logger.debug("Disover message sent: '{}'", discover);

            // wait for responses
            while (true) {
                byte[] rxbuf = new byte[256];
                packet = new DatagramPacket(rxbuf, rxbuf.length);
                try {
                    socket.receive(packet);
                } catch (SocketTimeoutException e) {
                    break; // leave the endless loop
                }

                parsePS3Packet(packet);
            }
        } catch (IOException e) {
            logger.debug("No PS3 device found. Diagnostic: {}", e.getMessage());
        }
    }

    private boolean parsePS4Packet(DatagramPacket packet) {
        byte[] data = packet.getData();
        String message = new String(data, StandardCharsets.UTF_8);

        String ipAddress = packet.getAddress().toString().split("/")[1];
        String hostId = "";
        String hostType = "";
        String hostName = "";
        String hostPort = "";
        String protocolVersion = "";
        String systemVersion = "";

        String[] ss = message.trim().split("\n");
        for (String row : ss) {
            int index = row.indexOf(':');
            index = index != -1 ? index : 0;
            String key = row.substring(0, index);
            String value = row.substring(index + 1);
            switch (key) {
                case RESPONSE_HOST_ID:
                    hostId = value;
                    break;
                case RESPONSE_HOST_TYPE:
                    hostType = value;
                    break;
                case RESPONSE_HOST_NAME:
                    hostName = value;
                    break;
                case RESPONSE_HOST_REQUEST_PORT:
                    hostPort = value;
                    break;
                case RESPONSE_DEVICE_DISCOVERY_PROTOCOL_VERSION:
                    protocolVersion = value;
                    break;
                case RESPONSE_SYSTEM_VERSION:
                    systemVersion = value;
                    break;
                default:
                    break;
            }
        }
        String modelID = hostType;
        switch (hostType) {
            case "PS4":
                modelID = "PlayStation 4";
                break;
            case "PS5":
                modelID = "PlayStation 5";
                break;
            default:
                break;
        }
        logger.debug("Adding a new Sony {} with IP '{}' and host-ID '{}' to inbox", hostType, ipAddress, hostId);
        Map<String, Object> properties = new HashMap<>();
        properties.put(IP_ADDRESS, ipAddress);
        properties.put(IP_PORT, Integer.valueOf(hostPort));
        properties.put(Thing.PROPERTY_MODEL_ID, modelID);
        properties.put(Thing.PROPERTY_HARDWARE_VERSION, hostIdToHWVersion(hostId));
        properties.put(Thing.PROPERTY_FIRMWARE_VERSION, systemVersion);
        properties.put(Thing.PROPERTY_MAC_ADDRESS, hostIdToMacAddress(hostId));
        ThingUID uid = hostType.equals("PS5") ? new ThingUID(THING_TYPE_SONYPS5, hostId)
                : new ThingUID(THING_TYPE_SONYPS4, hostId);

        DiscoveryResult result = DiscoveryResultBuilder.create(uid).withProperties(properties).withLabel(hostName)
                .build();
        thingDiscovered(result);
        logger.debug("Thing discovered '{}'", result);
        return true;
    }

    private boolean parsePS3Packet(DatagramPacket packet) {
        byte[] data = packet.getData();
        logger.debug("PS3 data '{}', length:{}", data, packet.getLength());
        String resp = new String(data, 0, 4);
        if (!"RESP".equals(resp) || packet.getLength() < 156) {
            return false;
        }

        String ipAddress = packet.getAddress().toString().split("/")[1];
        String hostId = String.format("%02x%02x%02x%02x%02x%02x", data[10], data[11], data[12], data[13], data[14],
                data[15]);
        String hostType = "Playstation 3";
        String hostName = new String(data, 16, 128);
        String systemVersion = String.format("%d.%d", data[5], data[6]);
        String unknown = new String(data, 144, 12);

        logger.debug("Adding a new Sony {} with IP '{}' and host-ID '{}' to inbox", hostType, ipAddress, hostId);
        Map<String, Object> properties = new HashMap<>();
        properties.put(IP_ADDRESS, ipAddress);
        properties.put(IP_PORT, Integer.valueOf(DEFAULT_PS3_BROADCAST_PORT));
        properties.put(Thing.PROPERTY_MODEL_ID, hostType);
        properties.put(Thing.PROPERTY_HARDWARE_VERSION, PS3HW_CECHXXXX);
        properties.put(Thing.PROPERTY_FIRMWARE_VERSION, systemVersion);
        properties.put(Thing.PROPERTY_MAC_ADDRESS, hostIdToMacAddress(hostId));
        ThingUID uid = new ThingUID(THING_TYPE_SONYPS3, hostId);

        DiscoveryResult result = DiscoveryResultBuilder.create(uid).withProperties(properties).withLabel(hostName)
                .build();
        thingDiscovered(result);
        logger.debug("Thing discovered '{}'", result);
        return true;
    }

    private static String hostIdToMacAddress(String hostId) {
        StringBuilder sb = new StringBuilder();
        if (hostId.length() >= 12) {
            for (int i = 0; i < 6; i++) {
                sb.append(hostId.substring(i * 2, i * 2 + 2).toLowerCase());
                if (i < 5) {
                    sb.append(':');
                }
            }
        }
        return sb.toString();
    }

    private static String hostIdToHWVersion(String hostId) {
        String hwVersion = PS4HW_CUHXXXX;
        if (hostId.length() >= 12) {
            final String manufacturer = hostId.substring(0, 6).toLowerCase();
            final String ethId = hostId.substring(6, 8).toLowerCase();
            switch (manufacturer) {
                // Ethernet
                case "2ccc44":
                    hwVersion = PS4HW_CUH7100;
                    break;
                case "709e29":
                    hwVersion = PS4HW_CUH1000;
                    break;
                case "bc60a7":
                    if (ethId.equals("7b")) {
                        hwVersion = PS4HW_CUH2000;
                    }
                    if (ethId.equals("8f")) {
                        hwVersion = PS4HW_CUH7000;
                    }
                    break;
                case "c863f1":
                case "f8461c":
                    hwVersion = PS4HW_CUH2000;
                    break;

                // WiFi
                case "b00594":
                    hwVersion = PS4HW_CUH1000;
                    break;
                case "40490f":
                case "5c9656":
                    if (ethId.equals("07")) {
                        hwVersion = PS4HW_CUH2000;
                    }
                    if (ethId.equals("da")) {
                        hwVersion = PS4HW_CUH7000;
                    }
                    break;
                case "5cea1d":
                case "f8da0c":
                    hwVersion = PS4HW_CUH2000;
                    break;
                case "dca266":
                    hwVersion = PS4HW_CUH7100;
                    break;

                default:
                    break;
            }
        }

        return hwVersion;
    }

}
