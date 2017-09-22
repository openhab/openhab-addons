/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wizlighting.internal.utils;

import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

/**
 * Utility class to perform some network routines.
 *
 * @author Sriram Balakrishnan - Initial contribution
 *
 */
public final class NetworkUtils {

    private NetworkUtils() {
    }

    /**
     * Gets all the broadcast address's from the machine.
     *
     * @return list with all the broadcast address's
     */
    public static List<InetAddress> getAllBroadcastAddresses() {
        List<InetAddress> listOfBroadcasts = new ArrayList<InetAddress>();
        Enumeration<NetworkInterface> list;
        try {
            list = NetworkInterface.getNetworkInterfaces();

            while (list.hasMoreElements()) {
                NetworkInterface iface = list.nextElement();
                if (iface == null) {
                    continue;
                }
                if (!iface.isLoopback() && iface.isUp()) {
                    Iterator<InterfaceAddress> it = iface.getInterfaceAddresses().iterator();
                    while (it.hasNext()) {
                        InterfaceAddress address = it.next();
                        if (address == null) {
                            continue;
                        }
                        InetAddress broadcast = address.getBroadcast();
                        if (broadcast != null) {
                            listOfBroadcasts.add(broadcast);
                        }
                    }
                }
            }
        } catch (SocketException ex) {
            return new ArrayList<InetAddress>();
        }
        return listOfBroadcasts;
    }

    public static String getMacAddress() throws UnknownHostException, SocketException {
        // NetworkInterface networkInterface = NetworkInterface.getByInetAddress(ipAddress);
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
