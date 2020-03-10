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
package org.openhab.binding.lgwebos.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class with utility functions to support Wake On Lan (WOL)
 *
 * @author Arjan Mels - Initial contribution
 * @author Sebastian Prehn - Modification to getMACAddress
 *
 */
@NonNullByDefault
public class WakeOnLanUtility {

    private static final Logger LOGGER = LoggerFactory.getLogger(WakeOnLanUtility.class);
    private static final Pattern MAC_REGEX = Pattern.compile("(([0-9a-fA-F]{2}[:-]){5}[0-9a-fA-F]{2})");

    /**
     * Get MAC address for host usesing "arping" tool
     *
     * @param hostName Host Name (or IP address) of host to retrieve MAC address for
     * @return MAC address
     */
    public static @Nullable String getMACAddress(String hostName) {
        try {
            Process proc = Runtime.getRuntime().exec("arping -r -c 1 -C 1 " + hostName);
            int returnCode = proc.waitFor();
            String s;
            StringBuilder builder = new StringBuilder();
            BufferedReader input = new BufferedReader(
                    new InputStreamReader(returnCode == 0 ? proc.getInputStream() : proc.getErrorStream()));
            while ((s = input.readLine()) != null) {
                builder.append(s);
            }

            if (returnCode != 0) {
                LOGGER.debug("getMacAddress error stream: {}", builder.toString());
            } else {
                Matcher matcher = MAC_REGEX.matcher(builder.toString());
                String macAddress = null;

                while (matcher.find()) {
                    String group = matcher.group();

                    if (group.length() == 17) {
                        macAddress = group;
                    }
                }

                if (macAddress != null) {
                    LOGGER.debug("MAC address of host {} is {}", hostName, macAddress);
                    return macAddress;
                }
            }
        } catch (IOException | InterruptedException e) {
            LOGGER.debug("Problem getting MAC address: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Send single WOL (Wake On Lan) package on all interfaces
     *
     * @macAddress MAC address to send WOL package to
     */
    public static void sendWOLPacket(String macAddress) {
        byte[] bytes = getWOLPackage(macAddress);

        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (networkInterface.isLoopback()) {
                    continue; // Do not want to use the loopback interface.
                }
                for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                    InetAddress broadcast = interfaceAddress.getBroadcast();
                    if (broadcast == null) {
                        continue;
                    }

                    DatagramPacket packet = new DatagramPacket(bytes, bytes.length, broadcast, 9);
                    try (DatagramSocket socket = new DatagramSocket()) {
                        socket.send(packet);
                        LOGGER.trace("Sent WOL packet to {} {}", broadcast, macAddress);
                    } catch (IOException e) {
                        LOGGER.warn("Problem sending WOL packet to {} {}", broadcast, macAddress);
                    }
                }
            }

        } catch (IOException e) {
            LOGGER.warn("Problem with interface while sending WOL packet to {}", macAddress);
        }
    }

    /**
     * Create WOL UDP package: 6 bytes 0xff and then 16 times the 6 byte mac address repeated
     *
     * @param macStr String representation of the MAC address (either with : or -)
     * @return byte array with the WOL package
     * @throws IllegalArgumentException
     */
    private static byte[] getWOLPackage(String macStr) throws IllegalArgumentException {
        byte[] macBytes = new byte[6];
        String[] hex = macStr.split("(\\:|\\-)");
        if (hex.length != 6) {
            throw new IllegalArgumentException("Invalid MAC address.");
        }
        try {
            for (int i = 0; i < 6; i++) {
                macBytes[i] = (byte) Integer.parseInt(hex[i], 16);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid hex digit in MAC address.");
        }

        byte[] bytes = new byte[6 + 16 * macBytes.length];
        for (int i = 0; i < 6; i++) {
            bytes[i] = (byte) 0xff;
        }
        for (int i = 6; i < bytes.length; i += macBytes.length) {
            System.arraycopy(macBytes, 0, bytes, i, macBytes.length);
        }

        return bytes;
    }

}
