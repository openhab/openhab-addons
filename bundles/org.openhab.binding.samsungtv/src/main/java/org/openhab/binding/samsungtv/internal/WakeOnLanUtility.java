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
package org.openhab.binding.samsungtv.internal;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.time.Duration;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.io.net.exec.ExecUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class with utility functions to support Wake On Lan (WOL)
 *
 * @author Arjan Mels - Initial contribution
 * @author Laurent Garnier - Use improvements from the LG webOS binding
 * @author Nick Waterton - use single ip address as source per interface
 *
 */
@NonNullByDefault
public class WakeOnLanUtility {

    private static final Logger LOGGER = LoggerFactory.getLogger(WakeOnLanUtility.class);
    private static final int CMD_TIMEOUT_MS = 1000;
    private static String host = "";

    /**
     * Get os command to find MAC address
     *
     * @return os COMMAND
     */
    public static String getCommand() {
        String os = System.getProperty("os.name");
        String COMMAND = "";
        if (os != null) {
            os = os.toLowerCase();
            LOGGER.debug("{}: os: {}", host, os);
            if ((os.contains("win"))) {
                COMMAND = "arp -a %s";
            } else if ((os.contains("mac"))) {
                COMMAND = "arp %s";
            } else { // linux
                if (checkIfLinuxCommandExists("arp")) {
                    COMMAND = "arp %s";
                } else if (checkIfLinuxCommandExists("arping")) { // typically OH provided docker image
                    COMMAND = "arping -r -c 1 -C 1 %s";
                } else {
                    LOGGER.warn("{}: arping not installed", host);
                }
            }
        } else {
            LOGGER.warn("{}: Unable to determine os", host);
        }
        return COMMAND;
    }

    /**
     * Get MAC address for host
     *
     * @param hostName Host Name (or IP address) of host to retrieve MAC address for
     * @return MAC address
     */
    public static @Nullable String getMACAddress(String hostName) {
        host = hostName;
        String COMMAND = getCommand();
        if (COMMAND.isEmpty()) {
            LOGGER.debug("{}: MAC address detection not possible. No command to identify MAC found.", hostName);
            return null;
        }

        Pattern MAC_REGEX = Pattern.compile("(([0-9a-fA-F]{2}[:-]){5}[0-9a-fA-F]{2})");
        String[] cmds = Stream.of(COMMAND.split(" ")).map(arg -> String.format(arg, hostName)).toArray(String[]::new);
        String response = ExecUtil.executeCommandLineAndWaitResponse(Duration.ofMillis(CMD_TIMEOUT_MS), cmds);
        String macAddress = null;

        if (response != null) {
            Matcher matcher = MAC_REGEX.matcher(response);
            while (matcher.find()) {
                String group = matcher.group();

                if (group.length() == 17) {
                    macAddress = group;
                    break;
                }
            }
        }
        if (macAddress != null) {
            LOGGER.debug("{}: MAC address of host {} is {}", hostName, hostName, macAddress);
        } else {
            LOGGER.debug("{}: Problem executing command {} to retrieve MAC address for {}: {}", hostName,
                    String.format(COMMAND, hostName), hostName, response);
        }
        return macAddress;
    }

    /**
     * Send single WOL (Wake On Lan) package on all interfaces
     *
     * @param macAddress MAC address to send WOL package to
     */
    public static void sendWOLPacket(String macAddress) {
        byte[] bytes = getWOLPackage(macAddress);

        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces != null && interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue; // Do not want to use the loopback or down interface.
                }
                for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                    InetAddress broadcast = interfaceAddress.getBroadcast();
                    if (broadcast == null) {
                        continue;
                    }

                    InetAddress local = interfaceAddress.getAddress();
                    DatagramPacket packet = new DatagramPacket(bytes, bytes.length, broadcast, 9);
                    try (DatagramSocket socket = new DatagramSocket()) {
                        socket.send(packet);
                        LOGGER.trace("Sent WOL packet from {} to {} {}", local, broadcast, macAddress);
                        break;
                    } catch (IOException e) {
                        LOGGER.warn("Problem sending WOL packet from {} to {} {}", local, broadcast, macAddress);
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

    private static boolean checkIfLinuxCommandExists(String cmd) {
        try {
            return 0 == Runtime.getRuntime().exec(String.format("which %s", cmd)).waitFor();
        } catch (InterruptedException | IOException e) {
            LOGGER.debug("{}: Error trying to check if command {} exists: {}", host, cmd, e.getMessage());
        }
        return false;
    }
}
