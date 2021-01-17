/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.broadlink.internal;

import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilities for working with the local network.
 *
 * @author John Marshall/Cato Sognen - Initial contribution
 */
@NonNullByDefault
public class NetworkUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkUtils.class);

    public static boolean hostAvailabilityCheck(@Nullable String host, int timeout) {
        if (host == null) {
            LOGGER.warn("Can't check availability of a null host");
            return false;
        }
        try {
            InetAddress address = InetAddress.getByName(host);
            return address.isReachable(timeout);
        } catch (Exception e) {
            LOGGER.error("Exception while trying to determine reachability of {}", host, e);
        }
        return false;
    }

    public static @Nullable InetAddress findNonLoopbackAddress() throws SocketException {
        Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
        while (ifaces.hasMoreElements()) {
            NetworkInterface iface = ifaces.nextElement();
            Enumeration<InetAddress> inetAddrs = iface.getInetAddresses();
            while (inetAddrs.hasMoreElements()) {
                InetAddress inetAddr = inetAddrs.nextElement();
                if (inetAddr.isLoopbackAddress()) {
                    continue; /* Loop/switch isn't completed */
                }

                if (inetAddr.isSiteLocalAddress()) {
                    return inetAddr;
                }
            }
        }

        return null;
    }

    public static InetAddress getLocalHostLANAddress() throws UnknownHostException {
        try {
            InetAddress candidateAddress = findNonLoopbackAddress();

            if (candidateAddress != null)
                return candidateAddress;

            InetAddress jdkSuppliedAddress = InetAddress.getLocalHost();
            if (jdkSuppliedAddress == null)
                throw new UnknownHostException("The JDK InetAddress.getLocalHost() method unexpectedly returned null.");
            else
                return jdkSuppliedAddress;
        } catch (Exception e) {
            UnknownHostException unknownHostException = new UnknownHostException(
                    (new StringBuilder("Failed to determine LAN address: ")).append(e).toString());
            unknownHostException.initCause(e);
            throw unknownHostException;
        }
    }

    public static int nextFreePort(InetAddress host, int from, int to) {
        int port = randInt(from, to);
        do {
            if (isLocalPortFree(host, port))
                return port;
            port = ThreadLocalRandom.current().nextInt(from, to);
        } while (true);
    }

    public static boolean isLocalPortFree(InetAddress host, int port) {
        try {
            (new ServerSocket(port, 50, host)).close();
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public static int randInt(int min, int max) {
        int randomNum = ThreadLocalRandom.current().nextInt((max - min) + 1) + min;
        return randomNum;
    }
}
