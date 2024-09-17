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
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.concurrent.ThreadLocalRandom;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.broadlink.internal.handler.BroadlinkHostNotReachableException;
import org.slf4j.Logger;

/**
 * Utilities for working with the local network.
 *
 * @author John Marshall/Cato Sognen - Initial contribution
 */
@NonNullByDefault
public class NetworkUtils {

    /**
     * Check whether the host is available.
     *
     * @param host hostname or ip address of the host to check for availability
     * @param timeout time in milliseconds for the check to timeout
     * @param logger the logger to use for logging any issues
     * @throws IOException if there is an unexpected error
     * @throws BroadlinkHostNotReachableException when the device is not responding
     */

    public static void hostAvailabilityCheck(@Nullable String host, int timeout, Logger logger)
            throws IOException, BroadlinkHostNotReachableException {
        InetAddress address = InetAddress.getByName(host);
        if (!address.isReachable(timeout)) {
            throw new BroadlinkHostNotReachableException("Cannot reach " + host);
        }
    }

    /**
     * Finds an InetAddress that is associated to a non-loopback device.
     *
     * @return null, if there is no non-loopback device address, otherwise the InetAddress associated to a non-loopback
     *         device
     * @throws SocketException thrown when no socket can be opened.
     */
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
     */
    public static int nextFreePort(InetAddress host, int from, int to) {
        int port = randInt(from, to);
        do {
            if (isLocalPortFree(host, port)) {
                return port;
            }
            port = ThreadLocalRandom.current().nextInt(from, to);
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
