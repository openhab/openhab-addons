/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.network.internal.utils;

import java.io.IOException;
import java.net.ConnectException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.NoRouteToHostException;
import java.net.PortUnreachableException;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang.SystemUtils;
import org.apache.commons.net.util.SubnetUtils;

/**
 * Network utility functions for pinging and for determining all interfaces and assigned IP addresses.
 *
 * @author David Graeff <david.graeff@web.de>
 */
public class NetworkUtils {
    /**
     * Use this within the class to internally call NetworkInterface.getNetworkInterfaces().
     * This is done for testing purposes.
     *
     * @return
     */
    public Enumeration<NetworkInterface> getNetworkInterfaces() {
        try {
            return NetworkInterface.getNetworkInterfaces();
        } catch (SocketException ignored) {
            // If we are not allowed to enumerate, we return an empty result set.
            return Collections.enumeration(new ArrayList<NetworkInterface>());
        }

    }

    /**
     * Gets every IPv4 Address on each Interface except the loopback
     * The Address format is ip/subnet
     *
     * @return The collected IPv4 Addresses
     */
    public Set<String> getInterfaceIPs() {
        Set<String> interfaceIPs = new HashSet<String>();

        // For each interface ...
        for (Enumeration<NetworkInterface> en = getNetworkInterfaces(); en.hasMoreElements();) {
            NetworkInterface networkInterface = en.nextElement();
            boolean isLoopback = true;
            try {
                isLoopback = networkInterface.isLoopback();
            } catch (SocketException ignored) {
            }
            if (!isLoopback) {
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

        return interfaceIPs;
    }

    /**
     * Get a set of all interface names.
     *
     * @return Set of interface names
     */
    public Set<String> getInterfaceNames() {
        Set<String> result = new HashSet<String>();

        try {
            // For each interface ...
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface networkInterface = en.nextElement();
                if (!networkInterface.isLoopback()) {
                    result.add(networkInterface.getName());
                }
            }
        } catch (SocketException ignored) {
            // If we are not allowed to enumerate, we return an empty result set.
        }

        return result;
    }

    /**
     * Determines every IP which can be assigned on all available interfaces
     *
     * @return Every single IP which can be assigned on the Networks the computer is connected to
     */
    public Set<String> getNetworkIPs() {
        return getNetworkIPs(getInterfaceIPs());
    }

    /**
     * Takes the interfaceIPs and fetches every IP which can be assigned on their network
     *
     * @param networkIPs The IPs which are assigned to the Network Interfaces
     * @return Every single IP which can be assigned on the Networks the computer is connected to
     */
    public Set<String> getNetworkIPs(Set<String> interfaceIPs) {
        LinkedHashSet<String> networkIPs = new LinkedHashSet<String>();

        for (String string : interfaceIPs) {
            try {
                // gets every ip which can be assigned on the given network
                SubnetUtils utils = new SubnetUtils(string);
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
     * Try to establish a tcp connection to the given port. Returns false if a timeout occurred
     * or the connection was denied.
     *
     * @param host The IP or hostname
     * @param port The tcp port. Must be not 0.
     * @param timeout Timeout in ms
     * @param logger A slf4j logger instance to log IOException
     * @return
     * @throws IOException
     */
    public boolean servicePing(String host, int port, int timeout) throws IOException {
        SocketAddress socketAddress = new InetSocketAddress(host, port);
        try (Socket socket = new Socket()) {
            socket.connect(socketAddress, timeout);
            return true;
        } catch (NoRouteToHostException ignored) {
            return false;
        } catch (SocketTimeoutException ignored) {
            return false;
        } catch (ConnectException e) {
            // Connection refused, there is a device on the other end though
            return true;
        }
    }

    /**
     * Return true if the native system ping is working
     */
    public boolean isNativePingWorking() {
        try {
            return nativePing("127.0.0.1", 1000);
        } catch (IOException ignored) {
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Reset interrupt flag
            return false;
        }
    }

    /**
     * Return true if the external arp ping utility (arping) is available and executable on the given path.
     */
    public boolean isNativeARPpingWorking(String arpToolPath) {
        try {
            // If no exception is thrown, the arp utility is working
            nativeARPPing(arpToolPath, "lo", "127.0.0.1", 1000);
            return true;
        } catch (IOException ignored) {
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Reset interrupt flag
            return false;
        }
    }

    /**
     * Use the native ping utility of the operating system to detect device presence.
     *
     * @param hostname The DNS name, IPv4 or IPv6 address. Must not be null.
     * @param timeoutInMS Timeout in milliseconds. Be aware that DNS resolution is not part of this timeout.
     * @return Returns true if the device responded
     * @throws IOException The ping command could probably not be found
     */
    public boolean nativePing(String hostname, int timeoutInMS) throws IOException, InterruptedException {
        Process proc;
        if (SystemUtils.IS_OS_WINDOWS) {
            proc = new ProcessBuilder("ping", "-w", String.valueOf(timeoutInMS), "-n", "1", hostname).start();
        } else { // We expect POSIX behaviour on any other OS
            proc = new ProcessBuilder("ping", "-w", String.valueOf(timeoutInMS / 1000), "-c", "1", hostname).start();
        }

        // The return code is 0 for a successful ping. 1 if device didn't respond and 2 if there is another error like
        // network interface not ready.
        return proc.waitFor() == 0;
    }

    /**
     *
     * @param arpUtilPath The arping absolute path including filename. Example: "arping" or "/usr/bin/arping" or
     *            "C:\something\arping.exe"
     * @param interfaceName An interface name, on linux for example "wlp58s0", shown by ifconfig. Must not be null.
     * @param ipV4address The ipV4 address. Must not be null.
     * @param timeoutInMS A timeout in milliseconds
     * @return Return true if the device responded
     * @throws IOException The ping command could probably not be found
     */
    public boolean nativeARPPing(String arpUtilPath, String interfaceName, String ipV4address, int timeoutInMS)
            throws IOException, InterruptedException {
        if (arpUtilPath == null) {
            return false;
        }
        Process proc;
        // The syntax of the iputils arping which is preinstalled on fedora/ubuntu and the
        // https://github.com/ThomasHabets/arping which also works on Windows and MacOS is similar enough to not use a
        // different command line on different OSs.
        proc = new ProcessBuilder(arpUtilPath, "-w", String.valueOf(timeoutInMS / 1000), "-c", "1", "-I", interfaceName,
                ipV4address).start();

        // The return code is 0 for a successful ping. 1 if device didn't respond and 2 if there is another error like
        // network interface not ready.
        return proc.waitFor() == 0;
    }

    /**
     * iOS devices are in a deep sleep mode, where they only listen to UDP traffic on port 5353 (Bonjour service
     * discovery). A packet on port 5353 will wake up the network stack to respond to ARP pings at least.
     *
     * @throws IOException
     */
    public void wakeUpIOS(InetAddress address) throws IOException {
        try (DatagramSocket s = new DatagramSocket()) {
            byte[] buffer = new byte[0];
            s.send(new DatagramPacket(buffer, buffer.length, address, 5353));
        } catch (PortUnreachableException ignored) {
            // We ignore the port unreachable error
        }
    }

}
