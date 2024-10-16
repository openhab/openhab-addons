/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.net.NetUtil;

/**
 * Utilities for working with the local network.
 *
 * @author John Marshall/Cato Sognen - Initial contribution
 */
@NonNullByDefault
public class NetworkUtils {
    /**
     * Finds an InetAddress that is associated to a non-loopback device.
     *
     * @return null, if there is no non-loopback device address, otherwise the InetAddress associated to a non-loopback
     *         device
     * @throws SocketException thrown when no socket can be opened.
     */
    public static @Nullable InetAddress findNonLoopbackAddress() throws SocketException {
        for (InetAddress address : NetUtil.getAllInterfaceAddresses().stream()
                .filter(a -> a.getAddress() instanceof Inet4Address).map(a -> a.getAddress()).toList()) {
            if (address.isSiteLocalAddress()) {
                return address;
            }
        }
        return null;
    }

    /**
     * Find the address of the local lan host
     *
     * @return InetAddress of the local lan host
     * @throws UnknownHostException if no local lan address can be found
     */
    public static InetAddress getLocalHostLANAddress() throws UnknownHostException {
        try {
            InetAddress candidateAddress = findNonLoopbackAddress();

            if (candidateAddress != null) {
                return candidateAddress;
            }
            InetAddress jdkSuppliedAddress = InetAddress.getLocalHost();
            if (jdkSuppliedAddress == null) {
                throw new UnknownHostException("The JDK InetAddress.getLocalHost() method unexpectedly returned null.");
            } else {
                return jdkSuppliedAddress;
            }
        } catch (Exception e) {
            UnknownHostException unknownHostException = new UnknownHostException(
                    (new StringBuilder("Failed to determine LAN address: ")).append(e).toString());
            unknownHostException.initCause(e);
            throw unknownHostException;
        }
    }

    /**
     * Randomly find a free port on the host in the defined range
     *
     * @param host The address of the host to find a free port on
     * @param from port number of the start of the range
     * @param to port number of the end of the range
     * @return number of the available port
     * @throws TimeoutException when no available port can be found in 30 seconds
     */
    public static int nextFreePort(InetAddress host, int from, int to) throws TimeoutException {
        if (to < from) {
            throw new IllegalArgumentException("To value is smaller than from value.");
        }
        int port = randInt(from, to);
        long startTime = System.currentTimeMillis();
        do {
            if (isLocalPortFree(host, port)) {
                return port;
            }
            port = ThreadLocalRandom.current().nextInt(from, to);
            if (System.currentTimeMillis() - startTime > 30000) {
                throw new TimeoutException("Cannot find an available port in the specified range");
            }
        } while (true);
    }

    /**
     * Test whether the port is available on the host
     *
     * @param host the host to check the port of
     * @param port the port to check for availability
     * @return true when available, otherwise false
     */
    public static boolean isLocalPortFree(InetAddress host, int port) {
        try {
            (new ServerSocket(port, 50, host)).close();
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    /**
     * Return a random integer in the range (min, max)
     *
     * @param min the lower limit of the range
     * @param max the upper limit of the range
     * @return the random integer
     */
    public static int randInt(int min, int max) {
        return ThreadLocalRandom.current().nextInt((max - min) + 1) + min;
    }
}
