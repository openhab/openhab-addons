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
package org.openhab.binding.samsungtv.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class with utility functions to support Wake On Lan (WOL)
 *
 * @author Arjan Mels - Initial contribution
 *
 */
@NonNullByDefault
public class WakeOnLanUtility {

    private static Logger logger = LoggerFactory.getLogger(WakeOnLanUtility.class);

    /**
     * Get MAC address for host
     * uses "arping" tool
     *
     * @param hostName Host Name (or IP address) of host to retrieve MAC address for
     * @return MAC address
     */
    public static @Nullable String getMACAddress(String hostName) {
        try {
            Process proc = Runtime.getRuntime().exec("arping -r -c 1 -C 1 " + hostName);
            proc.waitFor();
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String macAddress = stdInput.readLine();
            if (macAddress != null) {
                logger.info("MAC address of host {} is {}", hostName, macAddress);
                return macAddress;
            } else {
                BufferedReader stdErr = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
                String error = stdErr.readLine();
                logger.warn("Cannot get MAC addres of host {}: {}", hostName, error);
            }
        } catch (IOException | InterruptedException e) {
            logger.debug("Problem getting MAC address: {}", e.getMessage());
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

                    try {
                        DatagramPacket packet = new DatagramPacket(bytes, bytes.length, broadcast, 9);
                        DatagramSocket socket = new DatagramSocket();
                        socket.send(packet);
                        socket.close();
                        logger.trace("Sent WOL packet to {} {}", broadcast, macAddress);
                    } catch (IOException e) {
                        logger.warn("Problem sending WOL packet to {} {}", broadcast, macAddress);
                    }
                }
            }

        } catch (IOException e) {
            logger.warn("Problem with interface while sending WOL packet to {}", macAddress);
        }
    }

    /**
     * Create WOL UDP package: 6 bytes 0xff and then 6 times the 6 byte mac address repeated
     *
     * @param macStr String representation of teh MAC address (either with : or -)
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
