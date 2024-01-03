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
package org.openhab.binding.androidtv.internal.protocol.philipstv;

import static org.openhab.binding.androidtv.internal.protocol.philipstv.ConnectionManager.OBJECT_MAPPER;
import static org.openhab.binding.androidtv.internal.protocol.philipstv.PhilipsTVBindingConstants.GET_NETWORK_DEVICES_PATH;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * The {@link WakeOnLanUtil} is offering methods for powering on TVs via Wake-On-LAN.
 *
 * @author Benjamin Meyer - Initial contribution
 * @author Ben Rosenblum - Merged into AndroidTV
 */
@NonNullByDefault
public final class WakeOnLanUtil {

    private static final int MAX_WOL_RETRIES = 10;

    private static final int WOL_PORT = 9;

    private static final Logger LOGGER = LoggerFactory.getLogger(WakeOnLanUtil.class);

    private static final Pattern MAC_PATTERN = Pattern.compile("([:\\-])");

    private static final Predicate<JsonNode> IS_WOL_ENABLED = j -> j.get("wake-on-lan").asText()
            .equalsIgnoreCase("Enabled");

    private static final Predicate<NetworkInterface> IS_NOT_LOOPBACK = ni -> {
        try {
            return !ni.isLoopback();
        } catch (SocketException e) {
            return false;
        }
    };

    private WakeOnLanUtil() {
    }

    public static Optional<String> getMacFromEnabledInterface(ConnectionManager connectionManager) throws IOException {
        String jsonContent = connectionManager.doHttpsGet(GET_NETWORK_DEVICES_PATH);
        List<JsonNode> jsonNode = OBJECT_MAPPER.readValue(jsonContent, new TypeReference<List<JsonNode>>() {
        });

        return jsonNode.stream().filter(IS_WOL_ENABLED).map(j -> j.get("mac").asText())
                .peek(m -> LOGGER.debug("Mac identified as: {}", m)).findFirst();
    }

    public static void wakeOnLan(String ip, String mac) throws IOException, InterruptedException {
        for (int i = 0; i < MAX_WOL_RETRIES; i++) {
            if (isReachable(ip)) {
                Thread.sleep(2000);
                return;
            } else {
                Thread.sleep(100);
                sendWakeOnLanPackage(mac);
            }
        }
    }

    private static void sendWakeOnLanPackage(String mac) throws IOException {
        byte[] macBytes = getMacBytes(mac);
        byte[] bytes = new byte[6 + (16 * macBytes.length)];
        for (int i = 0; i < 6; i++) {
            bytes[i] = (byte) 0xff;
        }
        for (int i = 6; i < bytes.length; i += macBytes.length) {
            System.arraycopy(macBytes, 0, bytes, i, macBytes.length);
        }

        List<InetAddress> broadcastAddresses = Collections.list(NetworkInterface.getNetworkInterfaces()).stream()
                .filter(IS_NOT_LOOPBACK).map(NetworkInterface::getInterfaceAddresses).flatMap(Collection::stream)
                .map(InterfaceAddress::getBroadcast).filter(Objects::nonNull).collect(Collectors.toList());

        for (InetAddress broadcast : broadcastAddresses) {
            DatagramPacket packet = new DatagramPacket(bytes, bytes.length, broadcast, WOL_PORT);
            try (DatagramSocket socket = new DatagramSocket()) {
                LOGGER.debug("WOL sent to Broadcast-IP {} with MAC {}", broadcast, mac);
                socket.send(packet);
            }
        }
    }

    private static byte[] getMacBytes(String mac) {
        byte[] bytes = new byte[6];
        String[] hex = MAC_PATTERN.split(mac);
        if (hex.length != 6) {
            throw new IllegalArgumentException("Invalid MAC address.");
        }
        try {
            for (int i = 0; i < 6; i++) {
                bytes[i] = (byte) Integer.parseInt(hex[i], 16);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid hex digit in MAC address.");
        }
        return bytes;
    }

    public static boolean isReachable(String ipAddress) throws IOException {
        InetAddress inetAddress = InetAddress.getByName(ipAddress);
        return inetAddress.isReachable(1000);
    }
}
