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
package org.openhab.binding.lifx.internal.util;

import static org.openhab.binding.lifx.internal.LifxBindingConstants.BROADCAST_PORT;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link LifxNetworkUtil} provides network interface information to the LIFX binding objects. The information is
 * updated when it is older than {@link #UPDATE_INTERVAL_MILLIS}.
 *
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
public final class LifxNetworkUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(LifxNetworkUtil.class);
    private static final AtomicInteger BROADCAST_PORT_COUNTER = new AtomicInteger(1);
    private static final long UPDATE_INTERVAL_MILLIS = Duration.ofSeconds(15).toMillis();
    private static final int PORT_MAX = 65535;

    private static List<InetSocketAddress> broadcastAddresses = new ArrayList<>();
    private static List<InetAddress> interfaceAddresses = new ArrayList<>();
    private static int bufferSize; // upper bound of the MTUs of all available IPv4 network interfaces
    private static long lastUpdateMillis;

    private LifxNetworkUtil() {
        // hidden utility class constructor
    }

    /**
     * Updates the network information without any synchronization in a thread-safe way.
     */
    private static void updateNetworkInformation() {
        LOGGER.debug("Updating network information");

        List<InetSocketAddress> newBroadcastAddresses = new ArrayList<>();
        List<InetAddress> newInterfaceAddresses = new ArrayList<>();
        int newBufferSize = 0;

        Enumeration<NetworkInterface> networkInterfaces = null;
        try {
            networkInterfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            LOGGER.debug("Exception while getting network interfaces: '{}'", e.getMessage());
        }

        if (networkInterfaces != null) {
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface iface = networkInterfaces.nextElement();
                try {
                    if (iface.isUp() && !iface.isLoopback()) {
                        for (InterfaceAddress ifaceAddr : iface.getInterfaceAddresses()) {
                            if (ifaceAddr.getAddress() instanceof Inet4Address) {
                                newInterfaceAddresses.add(ifaceAddr.getAddress());
                                newBufferSize = Math.max(newBufferSize, iface.getMTU());
                                if (ifaceAddr.getBroadcast() != null) {
                                    newBroadcastAddresses
                                            .add(new InetSocketAddress(ifaceAddr.getBroadcast(), BROADCAST_PORT));
                                }
                            }
                        }
                    }
                } catch (SocketException e) {
                    LOGGER.debug("Exception while getting information for network interface '{}': '{}'",
                            iface.getName(), e.getMessage());
                }
            }

            broadcastAddresses = newBroadcastAddresses;
            interfaceAddresses = newInterfaceAddresses;
            bufferSize = newBufferSize;
        }

        lastUpdateMillis = System.currentTimeMillis();
    }

    private static void updateOutdatedNetworkInformation() {
        boolean updateIntervalElapsed = System.currentTimeMillis() - lastUpdateMillis > UPDATE_INTERVAL_MILLIS;
        if (updateIntervalElapsed) {
            updateNetworkInformation();
        }
    }

    public static List<InetSocketAddress> getBroadcastAddresses() {
        updateOutdatedNetworkInformation();
        return broadcastAddresses;
    }

    public static List<InetAddress> getInterfaceAddresses() {
        updateOutdatedNetworkInformation();
        return interfaceAddresses;
    }

    public static int getBufferSize() {
        updateOutdatedNetworkInformation();
        return bufferSize;
    }

    public static boolean isLocalAddress(InetAddress address) {
        return getInterfaceAddresses().contains(address);
    }

    public static boolean isRemoteAddress(InetAddress address) {
        return !isLocalAddress(address);
    }

    public static int getNewBroadcastPort() {
        int offset = BROADCAST_PORT_COUNTER.getAndUpdate((value) -> {
            return (value + 1) % Integer.MAX_VALUE;
        });
        return BROADCAST_PORT + (offset % (PORT_MAX - BROADCAST_PORT));
    }
}
