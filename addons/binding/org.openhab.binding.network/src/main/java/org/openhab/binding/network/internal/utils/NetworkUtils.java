/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.network.internal.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
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
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.net.util.SubnetUtils;
import org.eclipse.smarthome.io.net.exec.ExecUtil;

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
    public Set<String> getInterfaceIPs() {
        Set<String> interfaceIPs = new HashSet<>();

        Enumeration<NetworkInterface> interfaces;
        try {
            interfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException ignored) {
            // If we are not allowed to enumerate, we return an empty result set.
            return interfaceIPs;
        }

        // For each interface ...
        for (Enumeration<NetworkInterface> en = interfaces; en.hasMoreElements();) {
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
        Set<String> result = new HashSet<>();

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
     * @param maximumPerInterface The maximum of IP addresses per interface or 0 to get all.
     * @return Every single IP which can be assigned on the Networks the computer is connected to
     */
    public Set<String> getNetworkIPs(int maximumPerInterface) {
        return getNetworkIPs(getInterfaceIPs(), maximumPerInterface);
    }

    /**
     * Takes the interfaceIPs and fetches every IP which can be assigned on their network
     *
     * @param networkIPs The IPs which are assigned to the Network Interfaces
     * @param maximumPerInterface The maximum of IP addresses per interface or 0 to get all.
     * @return Every single IP which can be assigned on the Networks the computer is connected to
     */
    public Set<String> getNetworkIPs(Set<String> interfaceIPs, int maximumPerInterface) {
        LinkedHashSet<String> networkIPs = new LinkedHashSet<>();

        for (String string : interfaceIPs) {
            try {
                // gets every ip which can be assigned on the given network
                SubnetUtils utils = new SubnetUtils(string);
                String[] addresses = utils.getInfo().getAllAddresses();
                int len = addresses.length;
                if (maximumPerInterface != 0 && maximumPerInterface < len) {
                    len = maximumPerInterface;
                }
                for (int i = 0; i < len; i++) {
                    networkIPs.add(addresses[i]);
                }

            } catch (Exception ex) {
            }
        }

        return networkIPs;
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
        } catch (ConnectException | SocketTimeoutException | NoRouteToHostException ignored) {
            return false;
        }
    }

    /**
     * Return the working method for the native system ping. If no native ping
     * works JavaPing is returned.
     */
    public IpPingMethodEnum determinePingMethod() {
        IpPingMethodEnum method;
        if (SystemUtils.IS_OS_WINDOWS) {
            method = IpPingMethodEnum.WINDOWS_PING;
        } else if (SystemUtils.IS_OS_MAC) {
            method = IpPingMethodEnum.MAC_OS_PING;
        } else if (SystemUtils.IS_OS_UNIX) {
            method = IpPingMethodEnum.IPUTILS_LINUX_PING;
        } else {
            // We cannot estimate the command line for any other operating system and just return false
            return IpPingMethodEnum.JAVA_PING;
        }

        try {
            if (nativePing(method, "127.0.0.1", 1000)) {
                return method;
            }
        } catch (IOException ignored) {
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Reset interrupt flag
        }
        return IpPingMethodEnum.JAVA_PING;
    }

    /**
     * Return true if the external arp ping utility (arping) is available and executable on the given path.
     */
    public ArpPingUtilEnum determineNativeARPpingMethod(String arpToolPath) {
        String result = ExecUtil.executeCommandLineAndWaitResponse(arpToolPath, 100);
        if (StringUtils.isBlank(result)) {
            return null;
        } else if (result.contains("Thomas Habets")) {
            if (result.contains("-w sec Specify a timeout")) {
                return ArpPingUtilEnum.THOMAS_HABERT_ARPING;
            } else {
                return ArpPingUtilEnum.THOMAS_HABERT_ARPING_WITHOUT_TIMEOUT;
            }
        } else if (result.contains("-w timeout")) {
            return ArpPingUtilEnum.IPUTILS_ARPING;
        }
        return ArpPingUtilEnum.UNKNOWN_TOOL;
    }

    public enum IpPingMethodEnum {
        JAVA_PING,
        WINDOWS_PING,
        IPUTILS_LINUX_PING,
        MAC_OS_PING
    }

    /**
     * Use the native ping utility of the operating system to detect device presence.
     *
     * @param hostname The DNS name, IPv4 or IPv6 address. Must not be null.
     * @param timeoutInMS Timeout in milliseconds. Be aware that DNS resolution is not part of this timeout.
     * @return Returns true if the device responded
     * @throws IOException The ping command could probably not be found
     */
    public boolean nativePing(IpPingMethodEnum method, String hostname, int timeoutInMS)
            throws IOException, InterruptedException {
        Process proc;
        // Yes, all supported operating systems have their own ping utility with a different command line
        switch (method) {
            case IPUTILS_LINUX_PING:
                proc = new ProcessBuilder("ping", "-w", String.valueOf(timeoutInMS / 1000), "-c", "1", hostname)
                        .start();
                break;
            case MAC_OS_PING:
                proc = new ProcessBuilder("ping", "-t", String.valueOf(timeoutInMS / 1000), "-c", "1", hostname)
                        .start();
                break;
            case WINDOWS_PING:
                proc = new ProcessBuilder("ping", "-w", String.valueOf(timeoutInMS), "-n", "1", hostname).start();
                break;
            case JAVA_PING:
            default:
                // We cannot estimate the command line for any other operating system and just return false
                return false;

        }

        // The return code is 0 for a successful ping, 1 if device didn't
        // respond, and 2 if there is another error like network interface
        // not ready.
        // Exception: return code is also 0 in Windows for all requests on the local subnet.
        // see https://superuser.com/questions/403905/ping-from-windows-7-get-no-reply-but-sets-errorlevel-to-0
        if (method != IpPingMethodEnum.WINDOWS_PING) {
            return proc.waitFor() == 0;
        }

        int result = proc.waitFor();
        if (result != 0) {
            return false;
        }

        try (BufferedReader r = new BufferedReader(new InputStreamReader(proc.getInputStream()))) {
            String line = r.readLine();
            if (line == null) {
                throw new IOException("Received no output from ping process.");
            }
            do {
                if (line.contains("host unreachable") || line.contains("timed out")
                        || line.contains("could not find host")) {
                    return false;
                }
                line = r.readLine();
            } while (line != null);

            return true;
        }
    }

    public enum ArpPingUtilEnum {
        UNKNOWN_TOOL,
        IPUTILS_ARPING,
        THOMAS_HABERT_ARPING,
        THOMAS_HABERT_ARPING_WITHOUT_TIMEOUT
    }

    /**
     * Execute the arping tool to perform an ARP ping (only for IPv4 addresses).
     * There exist two different arping utils with the same name unfortunatelly.
     * * iputils arping which is sometimes preinstalled on fedora/ubuntu and the
     * * https://github.com/ThomasHabets/arping which also works on Windows and MacOS.
     *
     * @param arpUtilPath The arping absolute path including filename. Example: "arping" or "/usr/bin/arping" or
     *            "C:\something\arping.exe"
     * @param interfaceName An interface name, on linux for example "wlp58s0", shown by ifconfig. Must not be null.
     * @param ipV4address The ipV4 address. Must not be null.
     * @param timeoutInMS A timeout in milliseconds
     * @return Return true if the device responded
     * @throws IOException The ping command could probably not be found
     */
    public boolean nativeARPPing(ArpPingUtilEnum arpingTool, String arpUtilPath, String interfaceName,
            String ipV4address, int timeoutInMS) throws IOException, InterruptedException {
        if (arpUtilPath == null || arpingTool == null || arpingTool == ArpPingUtilEnum.UNKNOWN_TOOL) {
            return false;
        }
        Process proc;
        if (arpingTool == ArpPingUtilEnum.THOMAS_HABERT_ARPING_WITHOUT_TIMEOUT) {
            proc = new ProcessBuilder(arpUtilPath, "-c", "1", "-i", interfaceName, ipV4address).start();
        } else if (arpingTool == ArpPingUtilEnum.THOMAS_HABERT_ARPING) {
            proc = new ProcessBuilder(arpUtilPath, "-w", String.valueOf(timeoutInMS / 1000), "-c", "1", "-i",
                    interfaceName, ipV4address).start();
        } else {
            proc = new ProcessBuilder(arpUtilPath, "-w", String.valueOf(timeoutInMS / 1000), "-c", "1", "-I",
                    interfaceName, ipV4address).start();
        }

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
