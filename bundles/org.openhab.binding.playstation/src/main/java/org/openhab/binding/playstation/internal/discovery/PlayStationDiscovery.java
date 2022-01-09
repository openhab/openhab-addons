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
package org.openhab.binding.playstation.internal.discovery;

import static org.openhab.binding.playstation.internal.PlayStationBindingConstants.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.net.NetworkAddressService;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.util.HexUtils;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PlayStationDiscovery} is responsible for discovering
 * all PS4 devices
 *
 * @author Fredrik Ahlström - Initial contribution
 */
@NonNullByDefault
@Component(service = { DiscoveryService.class, PlayStationDiscovery.class }, configurationPid = "discovery.playstation")
public class PlayStationDiscovery extends AbstractDiscoveryService {

    private final Logger logger = LoggerFactory.getLogger(PlayStationDiscovery.class);

    private static final int DISCOVERY_TIMEOUT_SECONDS = 2;

    private @Nullable NetworkAddressService networkAS;

    public PlayStationDiscovery() {
        super(SUPPORTED_THING_TYPES_UIDS, DISCOVERY_TIMEOUT_SECONDS * 2, true);
    }

    @Override
    protected void startScan() {
        logger.debug("Updating discovered things (new scan)");
        discoverPS4();
        discoverPS3();
    }

    @Reference
    public void bindNetworkAddressService(NetworkAddressService network) {
        networkAS = network;
    }

    private @Nullable InetAddress getBroadcastAdress() {
        NetworkAddressService nwService = networkAS;
        if (nwService != null) {
            try {
                String address = nwService.getConfiguredBroadcastAddress();
                if (address != null) {
                    return InetAddress.getByName(address);
                } else {
                    return InetAddress.getByName("255.255.255.255");
                }
            } catch (UnknownHostException e) {
                // We catch errors later.
            }
        }
        return null;
    }

    private @Nullable InetAddress getIPv4Adress() {
        NetworkAddressService nwService = networkAS;
        if (nwService != null) {
            try {
                String address = nwService.getPrimaryIpv4HostAddress();
                if (address != null) {
                    return InetAddress.getByName(address);
                }
            } catch (UnknownHostException e) {
                // We catch errors later.
            }
        }
        return null;
    }

    private synchronized void discoverPS4() {
        logger.debug("Trying to discover all PS4 devices");

        try (DatagramSocket socket = new DatagramSocket(0, getIPv4Adress())) {
            socket.setBroadcast(true);
            socket.setSoTimeout(DISCOVERY_TIMEOUT_SECONDS * 1000);

            InetAddress bcAddress = getBroadcastAdress();

            // send discover
            byte[] discover = "SRCH * HTTP/1.1\ndevice-discovery-protocol-version:00020020\n".getBytes();
            DatagramPacket packet = new DatagramPacket(discover, discover.length, bcAddress, DEFAULT_BROADCAST_PORT);
            socket.send(packet);
            logger.debug("Discover message sent: '{}'", discover);

            // wait for responses
            while (true) {
                byte[] rxbuf = new byte[256];
                packet = new DatagramPacket(rxbuf, rxbuf.length);
                try {
                    socket.receive(packet);
                    parsePS4Packet(packet);
                } catch (SocketTimeoutException e) {
                    break; // leave the endless loop
                }
            }
        } catch (IOException e) {
            logger.debug("No PS4 device found. Diagnostic: {}", e.getMessage());
        }
    }

    private synchronized void discoverPS3() {
        logger.trace("Trying to discover all PS3 devices that have \"Connect PS Vita System Using Network\" on.");

        InetAddress bcAddress = getBroadcastAdress();
        InetAddress localAddress = getIPv4Adress();

        if (localAddress == null || bcAddress == null) {
            logger.warn("No IP/Broadcast address found. Make sure OpenHab is configured!");
            return;
        }
        try (DatagramSocket socket = new DatagramSocket(0, getIPv4Adress())) {
            socket.setBroadcast(true);
            socket.setSoTimeout(DISCOVERY_TIMEOUT_SECONDS * 1000);

            NetworkInterface nic = NetworkInterface.getByInetAddress(localAddress);
            byte[] macAdr = nic.getHardwareAddress();
            String macString = HexUtils.bytesToHex(macAdr);
            // send discover
            StringBuilder srchBuilder = new StringBuilder("SRCH3 * HTTP/1.1\n");
            srchBuilder.append("device-id:");
            srchBuilder.append(macString);
            srchBuilder.append("01010101010101010101\n");
            srchBuilder.append("device-type:PS Vita\n");
            srchBuilder.append("device-class:0\n");
            srchBuilder.append("device-mac-address:");
            srchBuilder.append(macString);
            srchBuilder.append("\n");
            srchBuilder.append("device-wireless-protocol-version:01000000\n\n");
            byte[] discover = srchBuilder.toString().getBytes();
            DatagramPacket packet = new DatagramPacket(discover, discover.length, bcAddress,
                    DEFAULT_PS3_MEDIA_MANAGER_PORT);
            socket.send(packet);

            // wait for responses
            while (true) {
                byte[] rxbuf = new byte[512];
                packet = new DatagramPacket(rxbuf, rxbuf.length);
                try {
                    socket.receive(packet);
                    parsePS3Packet(packet);
                } catch (SocketTimeoutException e) {
                    break; // leave the endless loop
                }
            }
        } catch (IOException e) {
            logger.debug("No PS3 device found. Diagnostic: {}", e.getMessage());
        }
    }

    /**
     * The response from the PS4 looks something like this:
     *
     * HTTP/1.1 200 Ok
     * host-id:0123456789AB
     * host-type:PS4
     * host-name:MyPS4
     * host-request-port:997
     * device-discovery-protocol-version:00020020
     * system-version:07020001
     * running-app-name:Youtube
     * running-app-titleid:CUSA01116
     *
     * @param packet
     * @return
     */
    private boolean parsePS4Packet(DatagramPacket packet) {
        byte[] data = packet.getData();
        String message = new String(data, StandardCharsets.UTF_8);
        logger.debug("PS4 data '{}', length:{}", message, packet.getLength());

        String ipAddress = packet.getAddress().toString().split("/")[1];
        String hostId = "";
        String hostType = "";
        String hostName = "";
        String hostPort = "";
        String protocolVersion = "";
        String systemVersion = "";

        String[] rowStrings = message.trim().split("\\r?\\n");
        for (String row : rowStrings) {
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
                    if (!"00020020".equals(protocolVersion)) {
                        logger.debug("Different protocol version: '{}'", protocolVersion);
                    }
                    break;
                case RESPONSE_SYSTEM_VERSION:
                    systemVersion = value;
                    break;
                default:
                    break;
            }
        }
        String hwVersion = hwVersionFromHostId(hostId);
        String modelID = modelNameFromHostTypeAndHWVersion(hostType, hwVersion);
        Map<String, Object> properties = new HashMap<>();
        properties.put(IP_ADDRESS, ipAddress);
        properties.put(IP_PORT, Integer.valueOf(hostPort));
        properties.put(Thing.PROPERTY_MODEL_ID, modelID);
        properties.put(Thing.PROPERTY_HARDWARE_VERSION, hwVersion);
        properties.put(Thing.PROPERTY_FIRMWARE_VERSION, formatPS4Version(systemVersion));
        properties.put(Thing.PROPERTY_MAC_ADDRESS, hostIdToMacAddress(hostId));
        ThingUID uid = hostType.equalsIgnoreCase("PS5") ? new ThingUID(THING_TYPE_PS5, hostId)
                : new ThingUID(THING_TYPE_PS4, hostId);

        DiscoveryResult result = DiscoveryResultBuilder.create(uid).withProperties(properties).withLabel(hostName)
                .withRepresentationProperty(Thing.PROPERTY_MAC_ADDRESS).build();
        thingDiscovered(result);
        return true;
    }

    /**
     * The response from the PS3 looks like this:
     *
     * HTTP/1.1 200 OK
     * host-id:00000000-0000-0000-0000-123456789abc
     * host-type:ps3
     * host-name:MyPS3
     * host-mtp-protocol-version:1800010
     * host-request-port:9309
     * host-wireless-protocol-version:1000000
     * host-mac-address:123456789abc
     * host-supported-device:PS Vita, PS Vita TV
     *
     * @param packet
     * @return
     */
    private boolean parsePS3Packet(DatagramPacket packet) {
        byte[] data = packet.getData();
        String message = new String(data, StandardCharsets.UTF_8);
        logger.debug("PS3 data '{}', length:{}", message, packet.getLength());

        String ipAddress = packet.getAddress().toString().split("/")[1];
        String hostId = "";
        String hostType = "";
        String hostName = "";
        String hostPort = "";
        String protocolVersion = "";

        String[] rowStrings = message.trim().split("\\r?\\n");
        for (String row : rowStrings) {
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
                    if (!Integer.toString(DEFAULT_PS3_MEDIA_MANAGER_PORT).equals(hostPort)) {
                        logger.debug("Different host request port: '{}'", hostPort);
                    }
                    break;
                case RESPONSE_HOST_WIRELESS_PROTOCOL_VERSION:
                    protocolVersion = value;
                    if (!"1000000".equals(protocolVersion)) {
                        logger.debug("Different protocol version: '{}'", protocolVersion);
                    }
                    break;
                case RESPONSE_HOST_MAC_ADDRESS:
                    hostId = value;
                    break;
                default:
                    break;
            }
        }
        String hwVersion = hwVersionFromHostId(hostId);
        String modelID = modelNameFromHostTypeAndHWVersion(hostType, hwVersion);
        Map<String, Object> properties = new HashMap<>();
        properties.put(IP_ADDRESS, ipAddress);
        properties.put(Thing.PROPERTY_MODEL_ID, modelID);
        properties.put(Thing.PROPERTY_HARDWARE_VERSION, hwVersion);
        properties.put(Thing.PROPERTY_MAC_ADDRESS, hostIdToMacAddress(hostId));
        ThingUID uid = new ThingUID(THING_TYPE_PS3, hostId);

        DiscoveryResult result = DiscoveryResultBuilder.create(uid).withProperties(properties).withLabel(hostName)
                .withRepresentationProperty(Thing.PROPERTY_MAC_ADDRESS).build();
        thingDiscovered(result);
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

    public static String formatPS4Version(String fwVersion) {
        String resultV = fwVersion;
        int len = fwVersion.length();
        for (Character c : fwVersion.toCharArray()) {
            if (!Character.isDigit(c)) {
                return resultV;
            }
        }
        if (len > 4) {
            resultV = resultV.substring(0, 4) + "." + resultV.substring(4, len);
            len++;
        }
        if (len > 2) {
            resultV = resultV.substring(0, 2) + "." + resultV.substring(2, len);
        }

        if (resultV.charAt(0) == '0') {
            resultV = resultV.substring(1);
        }
        return resultV;
    }

    private static String hwVersionFromHostId(String hostId) {
        String hwVersion = PS4HW_CUHXXXX;
        if (hostId.length() >= 12) {
            final String manufacturer = hostId.substring(0, 6).toLowerCase();
            final String ethId = hostId.substring(6, 8).toLowerCase();
            switch (manufacturer) {
                case "d44b5e":
                    hwVersion = PSVHW_PCHXXXX;
                    break;
                case "001315":
                case "001fa7":
                case "a8e3ee":
                case "fc0fe6":
                case "00248d":
                case "280dfc":
                case "0015c1":
                case "0019c5":
                case "0022a6":
                case "0cfe45":
                case "f8d0ac":
                case "00041f":
                case "001d0d":
                    hwVersion = PS3HW_CECHXXXX;
                    break;
                case "00d9d1":
                    hwVersion = PS3HW_CECH4000;
                    break;
                case "709e29": // Ethernet
                case "b00594": // WiFi
                    hwVersion = PS4HW_CUH1000;
                    break;
                case "bc60a7": // Ethernet
                    if (ethId.equals("7b")) {
                        hwVersion = PS4HW_CUH2000;
                    }
                    if (ethId.equals("8f")) {
                        hwVersion = PS4HW_CUH7000;
                    }
                    break;
                case "c863f1": // Ethernet
                case "f8461c": // Ethernet
                case "5cea1d": // WiFi
                case "f8da0c": // WiFi
                    hwVersion = PS4HW_CUH2000;
                    break;
                case "40490f": // WiFi
                case "5c9656": // WiFi
                    if (ethId.equals("07")) {
                        hwVersion = PS4HW_CUH2000;
                    }
                    if (ethId.equals("da")) {
                        hwVersion = PS4HW_CUH7000;
                    }
                    break;
                case "2ccc44": // Ethernet
                case "dca266": // WiFi
                    hwVersion = PS4HW_CUH7100;
                    break;
                case "78c881": // Ethernet
                case "1c98c1": // WiFi
                    hwVersion = PS5HW_CFI1000B;
                    break;
                default:
                    break;
            }
        }

        return hwVersion;
    }

    private static String modelNameFromHostTypeAndHWVersion(String hostType, String hwVersion) {
        String modelName = "PlayStation 4";
        switch (hostType.toUpperCase()) {
            case "PS3":
                modelName = "PlayStation 3";
                if (hwVersion.startsWith("CECH-2") || hwVersion.startsWith("CECH-3")) {
                    modelName += " Slim";
                } else if (hwVersion.startsWith("CECH-4")) {
                    modelName += " Super Slim";
                }
                break;
            case "PS4":
                modelName = "PlayStation 4";
                if (hwVersion.startsWith("CUH-2")) {
                    modelName += " Slim";
                } else if (hwVersion.startsWith("CUH-7")) {
                    modelName += " Pro";
                }
                break;
            case "PS5":
                modelName = "PlayStation 5";
                if (hwVersion.endsWith("B")) {
                    modelName += " Digital Edition";
                }
                break;
            default:
                break;
        }
        return modelName;
    }
}
