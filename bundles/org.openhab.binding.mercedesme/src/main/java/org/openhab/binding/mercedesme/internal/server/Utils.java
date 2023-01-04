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

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mercedesme.internal.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link Utils} class defines an HTTP Server for authentication callbacks
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class Utils {
    private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);
    private static final List<Integer> PORTS = new ArrayList<Integer>();
    private static int port = 8090;

    /**
     * Get free port without other Thread interference
     *
     * @return
     */
    public static synchronized int getFreePort() {
        while (PORTS.contains(port)) {
            port++;
        }
        PORTS.add(port);
        return port;
    }

    public static synchronized void addPort(int portNr) {
        if (PORTS.contains(portNr)) {
            LOGGER.warn("Port {} already occupied", portNr);
        }
        PORTS.add(portNr);
    }

    public static synchronized void removePort(int portNr) {
        PORTS.remove(Integer.valueOf(portNr));
    }

    public static String getCallbackIP() throws SocketException {
        // https://stackoverflow.com/questions/901755/how-to-get-the-ip-of-the-computer-on-linux-through-java
        // https://stackoverflow.com/questions/1062041/ip-address-not-obtained-in-java
        for (Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces(); ifaces
                .hasMoreElements();) {
            NetworkInterface iface = ifaces.nextElement();
            try {
                if (!iface.isLoopback()) {
                    if (iface.isUp()) {
                        for (Enumeration<InetAddress> addresses = iface.getInetAddresses(); addresses
                                .hasMoreElements();) {
                            InetAddress address = addresses.nextElement();
                            if (address instanceof Inet4Address) {
                                return address.getHostAddress();
                            }
                        }
                    }
                }
            } catch (SocketException se) {
                // Calling one network interface failed - continue searching
                LOGGER.trace("Network {} failed {}", iface.getName(), se.getMessage());
            }
        }
        throw new SocketException("IP address not detected");
    }

    public static String getCallbackAddress(String callbackIP, int callbackPort) {
        return "http://" + callbackIP + Constants.COLON + callbackPort + Constants.CALLBACK_ENDPOINT;
    }
}
