/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homematic.internal.misc;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class to retrieve local network interfaces.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class LocalNetworkInterface {
    private static final Logger logger = LoggerFactory.getLogger(LocalNetworkInterface.class);

    /**
     * Finds the (non loopback, non localhost) local network interface.
     */
    public static String getLocalNetworkInterface() {
        try {
            String localInterface = null;
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface current = interfaces.nextElement();
                if (!current.isUp() || current.isLoopback() || current.isVirtual()) {
                    continue;
                }
                Enumeration<InetAddress> addresses = current.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress current_addr = addresses.nextElement();
                    if (current_addr.isLoopbackAddress() || (current_addr instanceof Inet6Address)) {
                        continue;
                    }
                    if (localInterface != null) {
                        logger.warn("Found multiple local interfaces! Replacing " + localInterface + " with "
                                + current_addr.getHostAddress());
                    }
                    localInterface = current_addr.getHostAddress();
                }
            }
            return localInterface;
        } catch (SocketException ex) {
            logger.error("Could not retrieve network interface: " + ex.getMessage(), ex);
            return null;
        }
    }

}
