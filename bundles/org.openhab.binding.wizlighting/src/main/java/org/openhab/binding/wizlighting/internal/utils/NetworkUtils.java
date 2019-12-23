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
package org.openhab.binding.wizlighting.internal.utils;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.rmi.UnknownHostException;
import java.util.Enumeration;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Utility class to perform some network routines.
 *
 * @author Sriram Balakrishnan - Initial contribution
 *
 */
@NonNullByDefault
public final class NetworkUtils {

    /**
     * Returns the MAC address of the openHAB first network device.
     *
     * @return The MAC address of the openHAB network device.
     * @throws UnknownHostException
     * @throws SocketException
     */
    public static @Nullable String getMyMacAddress() throws UnknownHostException, SocketException {
        Enumeration<NetworkInterface> networks = NetworkInterface.getNetworkInterfaces();
        while (networks.hasMoreElements()) {
            @Nullable NetworkInterface network = networks.nextElement();
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
