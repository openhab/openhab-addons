/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.network.internal.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.NoRouteToHostException;
import java.net.PortUnreachableException;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.io.net.exec.ExecUtil;
import org.openhab.core.net.CidrAddress;
import org.openhab.core.net.NetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Network utility functions for pinging and for determining all interfaces and assigned IP addresses.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class NetworkUtils {
    private final Logger logger = LoggerFactory.getLogger(NetworkUtils.class);

    private LatencyParser latencyParser = new LatencyParser();

    /**
     * Gets every IPv4 Address on each Interface except the loopback
     * The Address format is ip/subnet
     *
     * @return The collected IPv4 Addresses
     */
    public Set<CidrAddress> getInterfaceIPs() {
        return NetUtil.getAllInterfaceAddresses().stream().filter(a -> a.getAddress() instanceof Inet4Address)
                .collect(Collectors.toSet());
    }

    /**
     * Gets every IPv4 address on the network defined by its cidr
     *
     * @return The collected IPv4 Addresses
     */
    private List<String> getIPAddresses(CidrAddress adr) {
        List<String> result = new ArrayList<>();
        byte[] octets = adr.getAddress().getAddress();
        final int addressCount = (1 << (32 - adr.getPrefix())) - 2;
        final int ipMask = 0xFFFFFFFF << (32 - adr.getPrefix());
        octets[0] &= ipMask >> 24;
        octets[1] &= ipMask >> 16;
        octets[2] &= ipMask >> 8;
        octets[3] &= ipMask;
        try {
            final CidrAddress baseIp = new CidrAddress(InetAddress.getByAddress(octets), (short) adr.getPrefix());
            for (int i = 1; i <= addressCount; i++) {
                int octet = i & ~ipMask;
                byte[] segments = baseIp.getAddress().getAddress();
                segments[2] += (octet >> 8);
                segments[3] += octet;
                result.add(InetAddress.getByAddress(segments).getHostAddress());
            }
        } catch (UnknownHostException e) {
            logger.debug("Could not build net ip address.", e);
        }
        return result;
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
     * @param interfaceIPs The IPs which are assigned to the Network Interfaces
     * @param maximumPerInterface The maximum of IP addresses per interface or 0 to get all.
     * @return Every single IP which can be assigned on the Networks the computer is connected to
     */
    private Set<String> getNetworkIPs(Set<CidrAddress> interfaceIPs, int maximumPerInterface) {
        LinkedHashSet<String> networkIPs = new LinkedHashSet<>();

        short minCidrPrefixLength = 8; // historic Class A network, addresses = 16777214
        if (maximumPerInterface != 0) {
            // calculate minimum CIDR prefix length from maximumPerInterface
            // (equals leading unset bits (Integer has 32 bits)
            minCidrPrefixLength = (short) Integer.numberOfLeadingZeros(maximumPerInterface);
            if (Integer.bitCount(maximumPerInterface) == 1) {
                // if only the highest is set, decrease prefix by 1 to cover all addresses
                minCidrPrefixLength--;
            }
        }
        logger.trace("set minCidrPrefixLength to {}, maximumPerInterface is {}", minCidrPrefixLength,
                maximumPerInterface);

        for (CidrAddress cidrNotation : interfaceIPs) {
            if (cidrNotation.getPrefix() < minCidrPrefixLength) {
                logger.info(
                        "CIDR prefix is smaller than /{} on interface with address {}, truncating to /{}, some addresses might be lost",
                        minCidrPrefixLength, cidrNotation, minCidrPrefixLength);
                cidrNotation = new CidrAddress(cidrNotation.getAddress(), minCidrPrefixLength);
            }

            List<String> addresses = getIPAddresses(cidrNotation);
            int len = addresses.size();
            if (maximumPerInterface != 0 && maximumPerInterface < len) {
                len = maximumPerInterface;
            }
            for (int i = 0; i < len; i++) {
                networkIPs.add(addresses.get(i));
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
     * @return Ping result information. Optional is empty if ping command was not executed.
     * @throws IOException
     */
    public Optional<PingResult> servicePing(String host, int port, int timeout) throws IOException {
        double execStartTimeInMS = System.currentTimeMillis();

        SocketAddress socketAddress = new InetSocketAddress(host, port);
        try (Socket socket = new Socket()) {
            socket.connect(socketAddress, timeout);
            return Optional.of(new PingResult(true, System.currentTimeMillis() - execStartTimeInMS));
        } catch (ConnectException | SocketTimeoutException | NoRouteToHostException ignored) {
            return Optional.of(new PingResult(false, System.currentTimeMillis() - execStartTimeInMS));
        }
    }

    /**
     * Return the working method for the native system ping. If no native ping
     * works JavaPing is returned.
     */
    public IpPingMethodEnum determinePingMethod() {
        String os = System.getProperty("os.name");
        IpPingMethodEnum method;
        if (os == null) {
            return IpPingMethodEnum.JAVA_PING;
        } else {
            os = os.toLowerCase();
            if (os.indexOf("win") >= 0) {
                method = IpPingMethodEnum.WINDOWS_PING;
            } else if (os.indexOf("mac") >= 0) {
                method = IpPingMethodEnum.MAC_OS_PING;
            } else if (os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0 || os.indexOf("aix") >= 0) {
                method = IpPingMethodEnum.IPUTILS_LINUX_PING;
            } else {
                // We cannot estimate the command line for any other operating system and just return false
                return IpPingMethodEnum.JAVA_PING;
            }
        }

        try {
            Optional<PingResult> pingResult = nativePing(method, "127.0.0.1", 1000);
            if (pingResult.isPresent() && pingResult.get().isSuccess()) {
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
        String result = ExecUtil.executeCommandLineAndWaitResponse(Duration.ofMillis(100), arpToolPath, "--help");
        if (result == null || result.isBlank()) {
            return ArpPingUtilEnum.DISABLED_UNKNOWN_TOOL;
        } else if (result.contains("Thomas Habets")) {
            if (result.matches("(?s)(.*)w sec Specify a timeout(.*)")) {
                return ArpPingUtilEnum.THOMAS_HABERT_ARPING;
            } else {
                return ArpPingUtilEnum.THOMAS_HABERT_ARPING_WITHOUT_TIMEOUT;
            }
        } else if (result.contains("-w timeout") || result.contains("-w <timeout>")) {
            return ArpPingUtilEnum.IPUTILS_ARPING;
        } else if (result.contains("Usage: arp-ping.exe")) {
            return ArpPingUtilEnum.ELI_FULKERSON_ARP_PING_FOR_WINDOWS;
        }
        return ArpPingUtilEnum.DISABLED_UNKNOWN_TOOL;
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
     * @return Ping result information. Optional is empty if ping command was not executed.
     * @throws IOException The ping command could probably not be found
     */
    public Optional<PingResult> nativePing(@Nullable IpPingMethodEnum method, String hostname, int timeoutInMS)
            throws IOException, InterruptedException {
        double execStartTimeInMS = System.currentTimeMillis();

        Process proc;
        if (method == null) {
            return Optional.empty();
        }
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
                return Optional.empty();
        }

        // The return code is 0 for a successful ping, 1 if device didn't
        // respond, and 2 if there is another error like network interface
        // not ready.
        // Exception: return code is also 0 in Windows for all requests on the local subnet.
        // see https://superuser.com/questions/403905/ping-from-windows-7-get-no-reply-but-sets-errorlevel-to-0

        int result = proc.waitFor();
        if (result != 0) {
            return Optional.of(new PingResult(false, System.currentTimeMillis() - execStartTimeInMS));
        }

        try (BufferedReader r = new BufferedReader(new InputStreamReader(proc.getInputStream()))) {
            String line = r.readLine();
            if (line == null) {
                throw new IOException("Received no output from ping process.");
            }
            do {
                // Because of the Windows issue, we need to check this. We assume that the ping was successful whenever
                // this specific string is contained in the output
                if (line.contains("TTL=") || line.contains("ttl=")) {
                    PingResult pingResult = new PingResult(true, System.currentTimeMillis() - execStartTimeInMS);
                    latencyParser.parseLatency(line).ifPresent(pingResult::setResponseTimeInMS);
                    return Optional.of(pingResult);
                }
                line = r.readLine();
            } while (line != null);

            return Optional.of(new PingResult(false, System.currentTimeMillis() - execStartTimeInMS));
        }
    }

    public enum ArpPingUtilEnum {
        DISABLED("Disabled", false),
        DISABLED_INVALID_IP("Destination is not a valid IPv4 address", false),
        DISABLED_UNKNOWN_TOOL("Unknown arping tool", false),
        IPUTILS_ARPING("Iputils Arping", true),
        THOMAS_HABERT_ARPING("Arping tool by Thomas Habets", true),
        THOMAS_HABERT_ARPING_WITHOUT_TIMEOUT("Arping tool by Thomas Habets (old version)", true),
        ELI_FULKERSON_ARP_PING_FOR_WINDOWS("Eli Fulkerson ARPing tool for Windows", true);

        public final String description;
        public final boolean canProceed;

        ArpPingUtilEnum(String description, boolean canProceed) {
            this.description = description;
            this.canProceed = canProceed;
        }
    }

    /**
     * Execute the arping tool to perform an ARP ping (only for IPv4 addresses).
     * There exist two different arping utils with the same name unfortunatelly.
     * * iputils arping which is sometimes preinstalled on fedora/ubuntu and the
     * * https://github.com/ThomasHabets/arping which also works on Windows and MacOS.
     *
     * @param arpUtilPath The arping absolute path including filename. Example: "arping" or "/usr/bin/arping" or
     *            "C:\something\arping.exe" or "arp-ping.exe"
     * @param interfaceName An interface name, on linux for example "wlp58s0", shown by ifconfig. Must not be null.
     * @param ipV4address The ipV4 address. Must not be null.
     * @param timeoutInMS A timeout in milliseconds
     * @return Ping result information. Optional is empty if ping command was not executed.
     * @throws IOException The ping command could probably not be found
     */
    public Optional<PingResult> nativeARPPing(@Nullable ArpPingUtilEnum arpingTool, @Nullable String arpUtilPath,
            String interfaceName, String ipV4address, int timeoutInMS) throws IOException, InterruptedException {
        double execStartTimeInMS = System.currentTimeMillis();

        if (arpUtilPath == null || arpingTool == null || !arpingTool.canProceed) {
            return Optional.empty();
        }
        Process proc;
        if (arpingTool == ArpPingUtilEnum.THOMAS_HABERT_ARPING_WITHOUT_TIMEOUT) {
            proc = new ProcessBuilder(arpUtilPath, "-c", "1", "-i", interfaceName, ipV4address).start();
        } else if (arpingTool == ArpPingUtilEnum.THOMAS_HABERT_ARPING) {
            proc = new ProcessBuilder(arpUtilPath, "-w", String.valueOf(timeoutInMS / 1000), "-C", "1", "-i",
                    interfaceName, ipV4address).start();
        } else if (arpingTool == ArpPingUtilEnum.ELI_FULKERSON_ARP_PING_FOR_WINDOWS) {
            proc = new ProcessBuilder(arpUtilPath, "-w", String.valueOf(timeoutInMS), "-x", ipV4address).start();
        } else {
            proc = new ProcessBuilder(arpUtilPath, "-w", String.valueOf(timeoutInMS / 1000), "-c", "1", "-I",
                    interfaceName, ipV4address).start();
        }

        // The return code is 0 for a successful ping. 1 if device didn't respond and 2 if there is another error like
        // network interface not ready.
        return Optional.of(new PingResult(proc.waitFor() == 0, System.currentTimeMillis() - execStartTimeInMS));
    }

    /**
     * Execute a Java ping.
     *
     * @param timeoutInMS A timeout in milliseconds
     * @param destinationAddress The address to check
     * @return Ping result information. Optional is empty if ping command was not executed.
     */
    public Optional<PingResult> javaPing(int timeoutInMS, InetAddress destinationAddress) {
        double execStartTimeInMS = System.currentTimeMillis();

        try {
            if (destinationAddress.isReachable(timeoutInMS)) {
                return Optional.of(new PingResult(true, System.currentTimeMillis() - execStartTimeInMS));
            } else {
                return Optional.of(new PingResult(false, System.currentTimeMillis() - execStartTimeInMS));
            }
        } catch (IOException e) {
            return Optional.of(new PingResult(false, System.currentTimeMillis() - execStartTimeInMS));
        }
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
