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
package org.openhab.binding.mercedesme.internal.server;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mercedesme.internal.Constants;

/**
 * The {@link Utils} class defines an HTTP Server for authentication callbacks
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class Utils {
    private static int port = 8090;
    private static List<Integer> ports = new ArrayList<Integer>();

    public static int getFreePort() {
        while (ports.contains(port)) {
            port++;
        }
        ports.add(port);
        return port;
    }

    public static void addPort(int portNr) {
        ports.add(portNr);
    }

    public static String getCallbackAddress() {
        // https://stackoverflow.com/questions/1062041/ip-address-not-obtained-in-java
        String ip = Constants.NOT_SET;
        try {
            for (Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces(); ifaces
                    .hasMoreElements();) {
                NetworkInterface iface = ifaces.nextElement();
                // guess IP address, not loopback!
                if (!Constants.LOOPBACK_ADDRESS.equals(iface.getName())) {
                    for (Enumeration<InetAddress> addresses = iface.getInetAddresses(); addresses.hasMoreElements();) {
                        InetAddress address = addresses.nextElement();
                        ip = address.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {
        }
        return Constants.HTTP + ip + Constants.COLON + "8090" + Constants.CALLBACK_ENDPOINT;
    }

    public static String getCallbackIP() {
        // https://stackoverflow.com/questions/1062041/ip-address-not-obtained-in-java
        String ip = Constants.NOT_SET;
        try {
            for (Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces(); ifaces
                    .hasMoreElements();) {
                NetworkInterface iface = ifaces.nextElement();
                // guess IP address, not loopback!
                if (!Constants.LOOPBACK_ADDRESS.equals(iface.getName())) {
                    for (Enumeration<InetAddress> addresses = iface.getInetAddresses(); addresses.hasMoreElements();) {
                        InetAddress address = addresses.nextElement();
                        ip = address.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {
        }
        return ip;
    }

    public static @NonNull String getCallbackAddress(@NonNull String callbackIP, int callbackPort) {
        return Constants.HTTP + callbackIP + Constants.COLON + callbackPort + Constants.CALLBACK_ENDPOINT;
    }
}
