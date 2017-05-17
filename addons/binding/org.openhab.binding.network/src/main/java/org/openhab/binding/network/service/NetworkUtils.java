/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.network.service;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.TreeSet;

import org.apache.commons.lang.SystemUtils;
import org.apache.commons.net.util.SubnetUtils;

/**
 * Network utility functions for pinging and for determining all interfaces and assigned IP addresses.
 *
 * @author David Graeff <david.graeff@web.de>
 */
public class NetworkUtils {

    /**
     * Gets every IPv4 Address on each Interface except the loopback
     * The Address format is ip/subnet
     *
     * @return The collected IPv4 Addresses
     */
    public static TreeSet<String> getInterfaceIPs() {
        TreeSet<String> interfaceIPs = new TreeSet<String>();

        try {
            // For each interface ...
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface networkInterface = en.nextElement();
                if (!networkInterface.isLoopback()) {

                    // .. and for each address ...
                    for (Iterator<InterfaceAddress> it = networkInterface.getInterfaceAddresses().iterator(); it
                            .hasNext();) {

                        // ... get IP and Subnet
                        InterfaceAddress interfaceAddress = it.next();
                        interfaceIPs.add(interfaceAddress.getAddress().getHostAddress() + "/"
                                + interfaceAddress.getNetworkPrefixLength());
                    }
                }
            }
        } catch (SocketException e) {
        }

        return interfaceIPs;
    }

    /**
     * Takes the interfaceIPs and fetches every IP which can be assigned on their network
     *
     * @param networkIPs The IPs which are assigned to the Network Interfaces
     * @return Every single IP which can be assigned on the Networks the computer is connected to
     */
    public static LinkedHashSet<String> getNetworkIPs(TreeSet<String> interfaceIPs) {
        LinkedHashSet<String> networkIPs = new LinkedHashSet<String>();

        for (Iterator<String> it = interfaceIPs.iterator(); it.hasNext();) {
            try {
                // gets every ip which can be assigned on the given network
                SubnetUtils utils = new SubnetUtils(it.next());
                String[] addresses = utils.getInfo().getAllAddresses();
                for (int i = 0; i < addresses.length; i++) {
                    networkIPs.add(addresses[i]);
                }

            } catch (Exception ex) {
            }
        }

        return networkIPs;
    }

    /**
     * Converts 32 bits int to IPv4 <tt>InetAddress</tt>.
     *
     * @param val int representation of IPv4 address
     * @return the address object
     */
    public static final InetAddress int2InetAddress(int val) {
        byte[] value = { (byte) ((val & 0xFF000000) >>> 24), (byte) ((val & 0X00FF0000) >>> 16),
                (byte) ((val & 0x0000FF00) >>> 8), (byte) ((val & 0x000000FF)) };
        try {
            return InetAddress.getByAddress(value);
        } catch (UnknownHostException e) {
            return null;
        }
    }

    /**
     * Converts 32 bits int packaged into a 64bits long to IPv4 <tt>InetAddress</tt>.
     *
     * @param val int representation of IPv4 address
     * @return the address object
     */
    public static final InetAddress long2InetAddress(long val) {
        if ((val < 0) || (val > 0xFFFFFFFFL)) {
            // TODO exception ???
        }
        return int2InetAddress((int) val);
    }

    /**
     * Converts IPv4 <tt>InetAddress</tt> to 32 bits int.
     *
     * @param addr IPv4 address object
     * @return 32 bits int
     * @throws NullPointerException <tt>addr</tt> is <tt>null</tt>.
     * @throws IllegalArgumentException the address is not IPv4 (Inet4Address).
     */
    public static final int inetAddress2Int(InetAddress addr) {
        if (!(addr instanceof Inet4Address)) {
            throw new IllegalArgumentException("Only IPv4 supported");
        }

        byte[] addrBytes = addr.getAddress();
        return ((addrBytes[0] & 0xFF) << 24) | ((addrBytes[1] & 0xFF) << 16) | ((addrBytes[2] & 0xFF) << 8)
                | ((addrBytes[3] & 0xFF));
    }

    /**
     * Converts IPv4 <tt>InetAddress</tt> to 32 bits int, packages into a 64 bits <tt>long</tt>.
     *
     * @param addr IPv4 address object
     * @return 32 bits int
     * @throws NullPointerException <tt>addr</tt> is <tt>null</tt>.
     * @throws IllegalArgumentException the address is not IPv4 (Inet4Address).
     */
    public static final long inetAddress2Long(InetAddress addr) {
        return (inetAddress2Int(addr) & 0xFFFFFFFFL);
    }

    public static boolean nativePing(String hostname, int port, int timeout)
            throws InvalidConfigurationException, IOException, InterruptedException {
        Process proc;
        if (SystemUtils.IS_OS_UNIX) {
            proc = new ProcessBuilder("ping", "-w", String.valueOf(timeout / 1000), "-c", "1", hostname).start();
        } else if (SystemUtils.IS_OS_WINDOWS) {
            proc = new ProcessBuilder("ping", "-w", String.valueOf(timeout), "-n", "1", hostname).start();
        } else {
            throw new InvalidConfigurationException("System Ping not supported");
        }

        int exitValue = proc.waitFor();
        if (exitValue != 0) {
            throw new IOException("Ping stopped with Error Number: " + exitValue + " on Command :" + "ping"
                    + (SystemUtils.IS_OS_UNIX ? " -t " : " -w ")
                    + (SystemUtils.IS_OS_UNIX ? String.valueOf(timeout / 1000) : String.valueOf(timeout))
                    + (SystemUtils.IS_OS_UNIX ? " -c" : " -n") + " 1 " + hostname);
        }
        return exitValue == 0;
    }

}
