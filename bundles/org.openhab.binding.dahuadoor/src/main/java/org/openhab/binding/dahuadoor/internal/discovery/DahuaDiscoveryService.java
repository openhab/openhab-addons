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
package org.openhab.binding.dahuadoor.internal.discovery;

import static org.openhab.binding.dahuadoor.internal.DahuaDoorBindingConstants.THING_TYPE_VTO2202;
import static org.openhab.binding.dahuadoor.internal.DahuaDoorBindingConstants.THING_TYPE_VTO3211;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link DahuaDiscoveryService} discovers Dahua VTO door station devices on the local network using
 * the DHIP protocol. It sends a UDP multicast {@code DHDiscover.search} request to
 * {@code 239.255.255.251:37810} and parses the JSON responses to create thing discovery results.
 *
 * <p>
 * Protocol details: the 32-byte DHIP header is structured as follows:
 * <ul>
 * <li>Bytes 0–7: magic {@code 0x2000000044484950} (big-endian, last 4 bytes = "DHIP")</li>
 * <li>Bytes 8–15: zero padding</li>
 * <li>Bytes 16–19: payload length (little-endian uint32)</li>
 * <li>Bytes 20–23: zero padding</li>
 * <li>Bytes 24–27: payload length again (little-endian uint32)</li>
 * <li>Bytes 28–31: zero padding</li>
 * </ul>
 *
 * @author Sven Schad - Initial contribution
 */
@NonNullByDefault
@Component(service = DiscoveryService.class, configurationPid = "discovery.dahuadoor")
public class DahuaDiscoveryService extends AbstractDiscoveryService {

    private final Logger logger = LoggerFactory.getLogger(DahuaDiscoveryService.class);

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_VTO2202, THING_TYPE_VTO3211);

    /** DHIP multicast address used by Dahua devices for device discovery. */
    private static final String DHIP_MULTICAST_ADDRESS = "239.255.255.251";

    /** UDP port for DHIP discovery. */
    private static final int DHIP_DISCOVERY_PORT = 37810;

    /** Timeout in seconds for the overall discovery scan. */
    private static final int SCAN_TIMEOUT_SECONDS = 10;

    /** Socket read timeout in milliseconds – keep short so the loop can check for scan completion. */
    private static final int SOCKET_TIMEOUT_MS = 1000;

    /** Maximum UDP response size in bytes. */
    private static final int RECEIVE_BUFFER_SIZE = 4096;

    /** DHIP discover request payload. */
    private static final String DISCOVER_PAYLOAD = "{\"method\":\"DHDiscover.search\",\"params\":{\"mac\":\"\",\"uni\":1}}";

    private @Nullable Future<?> scanTask;

    public DahuaDiscoveryService() {
        super(SUPPORTED_THING_TYPES, SCAN_TIMEOUT_SECONDS);
    }

    @Override
    protected void startScan() {
        logger.debug("Starting Dahua DHIP discovery scan (multicast {}:{})", DHIP_MULTICAST_ADDRESS,
                DHIP_DISCOVERY_PORT);
        scanTask = scheduler.schedule(this::performScan, 0, TimeUnit.SECONDS);
    }

    @Override
    protected synchronized void stopScan() {
        super.stopScan();
        Future<?> task = scanTask;
        if (task != null) {
            task.cancel(true);
            scanTask = null;
        }
    }

    /**
     * Performs the actual UDP multicast scan.
     * Opens a UDP socket, sends the DHIP discovery packet, and collects responses until the socket
     * timeout expires. Each response is handed to {@link #parseResponse(byte[], String)}.
     */
    private void performScan() {
        byte[] packet = buildDiscoveryPacket();

        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(SOCKET_TIMEOUT_MS);
            socket.setBroadcast(true);

            InetAddress multicastAddress = InetAddress.getByName(DHIP_MULTICAST_ADDRESS);
            DatagramPacket sendPacket = new DatagramPacket(packet, packet.length, multicastAddress,
                    DHIP_DISCOVERY_PORT);
            socket.send(sendPacket);
            logger.debug("Sent DHIP discover request to {}:{}", DHIP_MULTICAST_ADDRESS, DHIP_DISCOVERY_PORT);

            long deadline = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(SCAN_TIMEOUT_SECONDS);
            byte[] receiveBuffer = new byte[RECEIVE_BUFFER_SIZE];

            while (System.currentTimeMillis() < deadline) {
                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                try {
                    socket.receive(receivePacket);
                    String senderIp = receivePacket.getAddress().getHostAddress();
                    logger.debug("Received DHIP response from {}", senderIp);

                    parseResponse(receivePacket.getData(), receivePacket.getLength(), senderIp);
                } catch (SocketTimeoutException e) {
                    // No packet in this interval – keep looping until deadline.
                }
            }
        } catch (IOException e) {
            logger.debug("Error during Dahua DHIP discovery scan: {}", e.getMessage());
        }

        stopScan();
    }

    /**
     * Builds the 32-byte DHIP header followed by the JSON discover payload,
     * using the same structure as {@code DahuaEventClient.send()}.
     *
     * @return the complete UDP packet bytes
     */
    private byte[] buildDiscoveryPacket() {
        byte[] payloadBytes = DISCOVER_PAYLOAD.getBytes(StandardCharsets.UTF_8);
        int payloadLen = payloadBytes.length;

        ByteBuffer buf = ByteBuffer.allocate(32 + payloadLen);

        buf.order(ByteOrder.BIG_ENDIAN);
        buf.putInt(0x20000000);
        buf.putInt(0x44484950); // "DHIP"
        buf.order(ByteOrder.LITTLE_ENDIAN);
        buf.putInt(0); // sessionId (0 for unauthenticated discovery)
        buf.putInt(0); // id
        buf.putInt(payloadLen);
        buf.putInt(0);
        buf.putInt(payloadLen);
        buf.putInt(0);

        buf.put(payloadBytes);
        return buf.array();
    }

    /**
     * Parses a raw DHIP response packet. The first 32 bytes are the DHIP header; the remainder is a
     * null-terminated JSON string.
     *
     * @param data the raw packet bytes
     * @param length the number of valid bytes in {@code data}
     * @param senderIp the IP address of the responding device
     */
    private void parseResponse(byte[] data, int length, String senderIp) {
        if (length <= 32) {
            logger.debug("DHIP response too short ({} bytes) from {}", length, senderIp);
            return;
        }

        // Strip the 32-byte DHIP header.
        String jsonString = new String(data, 32, length - 32, StandardCharsets.UTF_8).replace("\u0000", "").trim();

        if (jsonString.isEmpty()) {
            logger.debug("Empty DHIP JSON payload from {}", senderIp);
            return;
        }

        try {
            JsonObject root = JsonParser.parseString(jsonString).getAsJsonObject();
            logger.debug("DHIP JSON from {}: {}", senderIp, jsonString);
            handleDiscoveredDevice(root, senderIp);
        } catch (JsonSyntaxException | IllegalStateException e) {
            logger.debug("Failed to parse DHIP JSON response from {}: {}", senderIp, e.getMessage());
        }
    }

    /**
     * Processes a parsed DHIP JSON response and submits a {@link DiscoveryResult} if the device
     * type is supported.
     *
     * <p>
     * The {@code DHDiscover.search} response has the form:
     *
     * <pre>
     * {
     *   "method": "DHDiscover.search",
     *   "params": {
     *     "DeviceType": "VTO2202F-P",
     *     "DeviceClass": "VTO",
     *     "Mac": "3c:ef:8c:bf:a2:04",
     *     "IPv4Address": {
     *       "IPAddress": "192.168.1.100",
     *       ...
     *     },
     *     "SoftwareVersion": "V2.680...",
     *     "SerialNo": "..."
     *   }
     * }
     * </pre>
     *
     * @param root the root JSON object of the response
     * @param senderIp the IP address the response was received from
     */
    private void handleDiscoveredDevice(JsonObject root, String senderIp) {
        JsonObject deviceInfo = getDeviceInfo(root);

        String deviceType = getStringField(deviceInfo, "DeviceType");
        String deviceClass = getStringField(deviceInfo, "DeviceClass");
        // MAC is at the root level in client.notifyDevInfo responses.
        String mac = getStringField(root, "mac");
        if (mac.isEmpty()) {
            mac = getStringField(deviceInfo, "Mac");
        }
        String hostname = resolveHostname(deviceInfo, senderIp);

        // Only discover door stations (VTO/VTH entrance units). Skip indoor monitors (VTH).
        if ("VTH".equalsIgnoreCase(deviceClass) && !deviceType.toUpperCase(Locale.ROOT).contains("VTO")) {
            logger.debug("Skipping non-door-station device (class={}, type={}) at {}", deviceClass, deviceType,
                    senderIp);
            return;
        }
        // "Version" is used in client.notifyDevInfo; "SoftwareVersion" in DHDiscover.search responses.
        String softwareVersion = getStringField(deviceInfo, "Version");
        if (softwareVersion.isEmpty()) {
            softwareVersion = getStringField(deviceInfo, "SoftwareVersion");
        }
        String serialNo = getStringField(deviceInfo, "SerialNo");

        ThingTypeUID thingTypeUID = resolveThingType(deviceType);
        if (thingTypeUID == null) {
            logger.debug("Unknown Dahua device type '{}' (class={}) at {} – defaulting to VTO2202", deviceType,
                    deviceClass, hostname);
            thingTypeUID = THING_TYPE_VTO2202;
        }

        // Use the serial number as the unique thing ID; fall back to MAC, then hostname.
        String uid = !serialNo.isEmpty() ? serialNo.toLowerCase(Locale.ROOT) : //
                !mac.isEmpty() ? mac.replace(":", "").toLowerCase(Locale.ROOT) : //
                        hostname.replace(".", "-");
        ThingUID thingUID = new ThingUID(thingTypeUID, uid);

        String label = deviceType.isEmpty() ? "Dahua Door Station" : deviceType;

        Map<String, Object> properties = new HashMap<>();
        properties.put("hostname", hostname);
        if (!mac.isEmpty()) {
            properties.put("mac", mac);
        }
        if (!softwareVersion.isEmpty()) {
            properties.put("softwareVersion", softwareVersion);
        }
        if (!serialNo.isEmpty()) {
            properties.put("serialNumber", serialNo);
        }
        if (!deviceClass.isEmpty()) {
            properties.put("deviceClass", deviceClass);
        }

        DiscoveryResult result = DiscoveryResultBuilder.create(thingUID).withThingType(thingTypeUID)
                .withProperties(properties).withRepresentationProperty("serialNumber").withLabel(label).build();

        thingDiscovered(result);
        logger.debug("Discovered Dahua device: {} ({}) at {}", deviceType, uid, hostname);
    }

    /**
     * Returns the device info object from the response. Handles two nesting variants:
     * <ul>
     * <li>{@code root.params.deviceInfo} – used by {@code client.notifyDevInfo}</li>
     * <li>{@code root.params} – used by {@code DHDiscover.search}</li>
     * <li>{@code root} – fallback</li>
     * </ul>
     */
    private JsonObject getDeviceInfo(JsonObject root) {
        JsonElement paramsElement = root.get("params");
        if (paramsElement != null && paramsElement.isJsonObject()) {
            JsonObject params = paramsElement.getAsJsonObject();
            JsonElement deviceInfoElement = params.get("deviceInfo");
            if (deviceInfoElement != null && deviceInfoElement.isJsonObject()) {
                return deviceInfoElement.getAsJsonObject();
            }
            return params;
        }
        return root;
    }

    /**
     * Extracts the device hostname / IP address. Looks inside {@code IPv4Address.IPAddress} first,
     * then falls back to the UDP sender address.
     */
    private String resolveHostname(JsonObject params, String senderIp) {
        JsonElement ipv4 = params.get("IPv4Address");
        if (ipv4 != null && ipv4.isJsonObject()) {
            String ip = getStringField(ipv4.getAsJsonObject(), "IPAddress");
            if (!ip.isEmpty()) {
                return ip;
            }
        }
        return senderIp;
    }

    /**
     * Maps a Dahua {@code DeviceType} string to the corresponding openHAB thing type UID.
     *
     * @param deviceType the device type string (e.g. {@code "VTO2202F-P"})
     * @return the matching {@link ThingTypeUID}, or {@code null} if not supported
     */
    private @Nullable ThingTypeUID resolveThingType(String deviceType) {
        String upper = deviceType.toUpperCase(Locale.ROOT);
        if (upper.contains("VTO3211")) {
            return THING_TYPE_VTO3211;
        }
        if (upper.contains("VTO2202") || upper.contains("VTO")) {
            // Default VTO devices to VTO2202 as the generic VTO type.
            return THING_TYPE_VTO2202;
        }
        return null;
    }

    /**
     * Safely reads a String field from a {@link JsonObject}.
     *
     * @param obj the JSON object to read from
     * @param fieldName the field name
     * @return the string value, or an empty string if absent or not a primitive
     */
    private String getStringField(JsonObject obj, String fieldName) {
        JsonElement element = obj.get(fieldName);
        if (element != null && element.isJsonPrimitive()) {
            return element.getAsString();
        }
        return "";
    }
}
