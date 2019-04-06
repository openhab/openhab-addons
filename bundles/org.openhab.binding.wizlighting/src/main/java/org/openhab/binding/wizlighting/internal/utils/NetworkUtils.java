/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wizlighting.internal.utils;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.rmi.UnknownHostException;
import java.util.Enumeration;

/**
 * Utility class to perform some network routines.
 *
 * @author Sriram Balakrishnan - Initial contribution
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
    public static String getMacAddress() throws UnknownHostException, SocketException {
        Enumeration<NetworkInterface> networks = NetworkInterface.getNetworkInterfaces();
        while (networks.hasMoreElements()) {
            NetworkInterface network = networks.nextElement();
            byte[] macAddressBytes = network.getHardwareAddress();
            if (macAddressBytes != null) {
                StringBuilder macAddressBuilder = new StringBuilder();

                for (int macAddressByteIndex = 0; macAddressByteIndex < macAddressBytes.length; macAddressByteIndex++) {
                    String macAddressHexByte = String.format("%02X", macAddressBytes[macAddressByteIndex]);
                    macAddressBuilder.append(macAddressHexByte);
                }
                return macAddressBuilder.toString();
            }
        }

        return null;
    }
}
