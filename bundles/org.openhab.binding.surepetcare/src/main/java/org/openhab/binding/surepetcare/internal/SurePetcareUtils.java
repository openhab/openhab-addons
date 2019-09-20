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
package org.openhab.binding.surepetcare.internal;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SurePetcareUtils} class defines a number of static utility methods.
 *
 * @author Rene Scherer - Initial contribution
 */
@NonNullByDefault
public class SurePetcareUtils {

    private final static Logger logger = LoggerFactory.getLogger(SurePetcareUtils.class);
    private static final int DEFAULT_DEVICE_ID = 12344711;

    public static Integer getDeviceId() {
        int decimal = 0;
        try {
            if (NetworkInterface.getNetworkInterfaces().hasMoreElements()) {
                NetworkInterface netif = NetworkInterface.getNetworkInterfaces().nextElement();

                byte[] mac = netif.getHardwareAddress();
                if (mac != null) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < mac.length; i++) {
                        sb.append(String.format("%02x", mac[i]));
                    }
                    String hex = sb.toString();
                    decimal = (int) (Long.parseLong(hex, 16) % Integer.MAX_VALUE);
                    logger.debug("current MAC address: {}, device id: {}", hex, decimal);
                } else {
                    try {
                        InetAddress ip = InetAddress.getLocalHost();
                        String hostname = ip.getHostName();
                        decimal = hostname.hashCode();
                        logger.debug("current hostname: {}, device id: {}", hostname, decimal);
                    } catch (UnknownHostException e) {
                        decimal = DEFAULT_DEVICE_ID;
                        logger.warn("unable to discover mac or hostname, assigning default device id {}", decimal);
                    }
                }
            }
        } catch (SocketException e) {
            logger.warn("Socket Exception: ", e.getMessage());
        }
        return decimal;
    }
}
