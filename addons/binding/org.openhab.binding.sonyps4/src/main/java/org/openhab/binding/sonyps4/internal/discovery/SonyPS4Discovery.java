/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sonyps4.internal.discovery;

import static org.openhab.binding.sonyps4.internal.SonyPS4BindingConstants.*;
import static org.openhab.binding.sonyps4.internal.SonyPS4Configuration.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.sonyps4.internal.SonyPS4BindingConstants;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SonyPS4Discovery} is responsible for discovering
 * all PS4 devices
 *
 * @author Fredrik Ahlström - Initial contribution
 */
@NonNullByDefault
@Component(service = { DiscoveryService.class,
        SonyPS4Discovery.class }, immediate = true, configurationPid = "binding.sonyps4")
public class SonyPS4Discovery extends AbstractDiscoveryService {

    private final Logger logger = LoggerFactory.getLogger(SonyPS4Discovery.class);

    private static final int BROADCAST_PORT = 987;
    private static final int DISCOVERY_TIMEOUT_SECONDS = 3;

    public SonyPS4Discovery() {
        super(SonyPS4BindingConstants.SUPPORTED_THING_TYPES_UIDS, DISCOVERY_TIMEOUT_SECONDS, true);
    }

    /**
     * Activates the Discovery Service.
     */
    public void activate() {
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
    }

    private synchronized void discover() {
        logger.debug("Try to discover all PS4 devices");

        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setBroadcast(true);
            socket.setSoTimeout(DISCOVERY_TIMEOUT_SECONDS * 1000);

            InetAddress inetAddress = InetAddress.getByName("255.255.255.255");

            // send discover
            byte[] discover = "SRCH * HTTP/1.1\ndevice-discovery-protocol-version:00020020\n".getBytes();
            DatagramPacket packet = new DatagramPacket(discover, discover.length, inetAddress, BROADCAST_PORT);
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

                parsePacket(packet);
            }
        } catch (IOException e) {
            logger.debug("No PS4 device found. Diagnostic: {}", e.getMessage());
        }
    }

    private boolean parsePacket(DatagramPacket packet) {
        byte[] data = packet.getData();
        String message = "";
        try {
            message = new String(data, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            logger.debug("UTF-8 decoding not supported? {}", e);
        }
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
        logger.debug("Adding a new Sony {} with IP '{}' and host-ID '{}' to inbox", hostType, ipAddress, hostId);
        Map<String, Object> properties = new HashMap<>();
        properties.put(IP_ADDRESS, ipAddress);
        properties.put(IP_PORT, hostPort);
        properties.put(Thing.PROPERTY_MODEL_ID, hostType);
        properties.put(Thing.PROPERTY_HARDWARE_VERSION, hostIdToHWVersion(hostId));
        properties.put(Thing.PROPERTY_FIRMWARE_VERSION, systemVersion);
        properties.put(Thing.PROPERTY_MAC_ADDRESS, hostIdToMacAddress(hostId));
        ThingUID uid = new ThingUID(THING_TYPE_SONYPS4, hostId);

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

    /*
     * CUH-1004A, Kenneth
     * Ethernet: 70:9e:29:00:35:b2
     * Wi-Fi: b0:05:94:02:33:bb
     *
     * CUH-2016B, Oscar
     * Ethernet: bc:60:a7:7b:17:5d
     * Wi-Fi: 5c:96:56:07:37:1f
     *
     * CUH-7016B, Fredrik
     * Ethernet: bc:60:a7:8f:7e:f9
     * Wi-Fi: 40:49:0f:da:c9:8d
     *
     * CUH-20XX, Claes
     * Ethernet: c8:63:f1:74:6c:0f
     * Wi-Fi: 5c:ea:1d:eb:3a:4b
     *
     * CUH-20XX, Marcus
     * Ethernet: f8:46:1c:88:f0:4e
     * Wi-Fi: f8:da:0c:c8:88:75
     *
     */

    private static String hostIdToHWVersion(String hostId) {
        String hwVersion = "CUH-XXXX";
        if (hostId.length() >= 12) {
            String manufacturer = hostId.substring(0, 6).toLowerCase();
            String ethId = hostId.substring(6, 8).toLowerCase();
            switch (manufacturer) {
                // Ethernet
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

                default:
                    break;
            }
        }

        return hwVersion;
    }

}
