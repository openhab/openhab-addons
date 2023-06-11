/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.silvercrestwifisocket.internal.utils;

import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

/**
 * Utilitary static class to perform some network routines.
 *
 * @author Jaime Vaz - Initial contribution
 *
 */
public final class NetworkUtils {

    private NetworkUtils() {
        // Avoid instantiation.
    }

    /**
     * Gets all the broadcast address's from the machine.
     *
     * @return list with all the broadcast address's
     */
    public static List<InetAddress> getAllBroadcastAddresses() {
        List<InetAddress> listOfBroadcasts = new ArrayList<>();
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
            return new ArrayList<>();
        }
        return listOfBroadcasts;
    }
}
