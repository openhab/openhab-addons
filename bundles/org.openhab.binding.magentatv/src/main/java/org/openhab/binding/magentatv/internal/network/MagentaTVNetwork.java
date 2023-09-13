/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.magentatv.internal.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.magentatv.internal.MagentaTVException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MagentaTVNetwork} supplies network functions.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class MagentaTVNetwork {
    private final Logger logger = LoggerFactory.getLogger(MagentaTVNetwork.class);

    private String localIP = "";
    private String localPort = "";
    private String localMAC = "";
    private @Nullable NetworkInterface localInterface;

    /**
     * Init local network interface, determine local IP and MAC address
     *
     * @param localIP
     * @param localPort
     */
    public void initLocalNet(String localIP, String localPort) throws MagentaTVException {
        try {
            if (localIP.isEmpty() || "0.0.0.0".equals(localIP) || "127.0.0.1".equals(localIP)) {
                throw new MagentaTVException("Unable to detect local IP address!");
            }
            this.localPort = localPort;
            this.localIP = localIP;

            // get MAC address
            InetAddress ip = InetAddress.getByName(localIP);
            localInterface = NetworkInterface.getByInetAddress(ip);
            if (localInterface != null) {
                byte[] mac = localInterface.getHardwareAddress();
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < mac.length; i++) {
                    sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? ":" : ""));
                }
                localMAC = sb.toString().toUpperCase();
                logger.debug("Local IP address={}, Local MAC address = {}", localIP, localMAC);
                return;
            }
        } catch (UnknownHostException | SocketException e) {
            throw new MagentaTVException(e);
        }

        throw new MagentaTVException(
                "Unable to get local IP / MAC address, check network settings in openHAB system configuration!");
    }

    @Nullable
    public NetworkInterface getLocalInterface() {
        return localInterface;
    }

    public String getLocalIP() {
        return localIP;
    }

    public String getLocalPort() {
        return localPort;
    }

    public String getLocalMAC() {
        return localMAC;
    }

    public static final int WOL_PORT = 9;

    /**
     * Send a Wake-on-LAN packet
     *
     * @param ipAddr destination ip
     * @param macAddress destination MAC address
     * @throws MagentaTVException
     */
    public void sendWakeOnLAN(String ipAddr, String macAddress) throws MagentaTVException {
        try {
            byte[] macBytes = getMacBytes(macAddress);
            byte[] bytes = new byte[6 + 16 * macBytes.length];
            for (int i = 0; i < 6; i++) {
                bytes[i] = (byte) 0xff;
            }
            for (int i = 6; i < bytes.length; i += macBytes.length) {
                System.arraycopy(macBytes, 0, bytes, i, macBytes.length);
            }

            InetAddress address = InetAddress.getByName(ipAddr);
            DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address, WOL_PORT);
            try (DatagramSocket socket = new DatagramSocket()) {
                socket.send(packet);
            }

            logger.debug("Wake-on-LAN packet sent to {} / {}", ipAddr, macAddress);
        } catch (IOException e) {
            throw new MagentaTVException(e, "Unable to send Wake-on-LAN packet to {} / {}", ipAddr, macAddress);
        }
    }

    /**
     * Convert MAC address from string to byte array
     *
     * @param macStr MAC address as string
     * @return MAC address as byte array
     * @throws IllegalArgumentException
     */
    private static byte[] getMacBytes(String macStr) throws IllegalArgumentException {
        byte[] bytes = new byte[6];
        String[] hex = macStr.split("(\\:|\\-)");
        if (hex.length != 6) {
            throw new IllegalArgumentException("Invalid MAC address.");
        }
        try {
            for (int i = 0; i < 6; i++) {
                bytes[i] = (byte) Integer.parseInt(hex[i], 16);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid hex digit in MAC address.", e);
        }
        return bytes;
    }
}
