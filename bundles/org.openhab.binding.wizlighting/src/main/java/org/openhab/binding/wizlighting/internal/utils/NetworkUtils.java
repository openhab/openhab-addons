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
package org.openhab.binding.wizlighting.internal.utils;

import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.rmi.UnknownHostException;
import java.util.Enumeration;

/**
 * Utility class to perform some network routines.
 *
 * @author Sriram Balakrishnan - Initial contribution
 * @author Joshua Freeman - Modified to get MAC matching IP
 *
 */
public final class NetworkUtils {
    /**
     * Returns the MAC address of the openHAB first network device.
     *
     * @return The MAC address of the openHAB network device.
     * @throws UnknownHostException
     * @throws SocketException
     */
    public static String getMyMacAddress(String matchIP) throws UnknownHostException, SocketException {
        String macAddress = null;
        Enumeration<NetworkInterface> networks = NetworkInterface.getNetworkInterfaces();
        while (networks.hasMoreElements()) {
            NetworkInterface network = networks.nextElement();

            if (network != null && networkMatchesIP(network, matchIP)) {
                macAddress = convertBytesToMACString(network.getHardwareAddress());
                break; // Short circuit if we found it
            }
        }
        return macAddress;
    }

    private static boolean networkMatchesIP(NetworkInterface network, String ip) {
        if (network != null && ip != null) {
            for (InterfaceAddress interfaceAddress : network.getInterfaceAddresses()) {
                String hostAddress = interfaceAddress.getAddress().getHostAddress();
                if (ip.equals(hostAddress)) {
                    return true;
                }
            }
        }

        return false;
    }

    private static String convertBytesToMACString(byte[] hardwareAddress) {
        StringBuilder macAddressBuilder = new StringBuilder();
        if (hardwareAddress != null) {
            for (int macAddressByteIndex = 0; macAddressByteIndex < hardwareAddress.length; macAddressByteIndex++) {
                String macAddressHexByte = String.format("%02X", hardwareAddress[macAddressByteIndex]);
                macAddressBuilder.append(macAddressHexByte);
            }
        }
        return macAddressBuilder.toString();
    }
}
