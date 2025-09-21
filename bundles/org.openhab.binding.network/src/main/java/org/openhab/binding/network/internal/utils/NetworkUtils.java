/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
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

    /**
     * Nanos per millisecond and microsecond.
     */
    private static final long NANOS_PER_MILLI = 1000_000L;
    private static final long NANOS_PER_MICRO = 1000L;

    /**
     * Converts a {@link Duration} to milliseconds.
     * <p>
     * The result has a greater than millisecond precision compared to {@link Duration#toMillis()} which drops excess
     * precision information.
     *
     * @param duration the {@link Duration} to be converted
     * @return the equivalent milliseconds of the given {@link Duration}
     */
    public static double durationToMillis(Duration duration) {
        return (double) duration.toNanos() / NANOS_PER_MILLI;
    }

    /**
     * Converts a double representing milliseconds to a {@link Duration} instance.
     * <p>
     * The result has a greater than millisecond precision compared to {@link Duration#ofMillis(long)}.
     *
     * @param millis the milliseconds to be converted
     * @return a {@link Duration} instance representing the given milliseconds
     */
    public static Duration millisToDuration(double millis) {
        return Duration.ofNanos((long) (millis * NANOS_PER_MILLI));
    }

    /**
     * Converts a double representing microseconds to a {@link Duration} instance.
     * <p>
     *
     * @param micros the microseconds to be converted
     * @return a {@link Duration} instance representing the given microseconds
     */
    public static Duration microsToDuration(double micros) {
        return Duration.ofNanos((long) (micros * NANOS_PER_MICRO));
    }

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
            logger.trace("Could not build net IP address.", e);
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
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface networkInterface = en.nextElement();
                if (networkInterface.isUp() && !networkInterface.isLoopback()) {
                    result.add(networkInterface.getName());
                }
            }
        } catch (SocketException e) {
            logger.trace("Could not get network interfaces", e);
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
     * Retrieves a map of network interface names to their associated IP addresses.
     *
     * @return A map where the key is the name of the network interface and the value is a set of CidrAddress objects
     *         representing the IP addresses and network prefix lengths for that interface.
     */
    public Map<String, Set<CidrAddress>> getNetworkIPsPerInterface() {
        Map<String, Set<CidrAddress>> outputMap = new HashMap<>();
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface networkInterface = en.nextElement();
                if (!networkInterface.isUp() || networkInterface.isLoopback()) {
                    logger.trace("Network interface: {} is excluded in the search", networkInterface.getName());
                    continue;
                }

                Set<CidrAddress> addresses = networkInterface.getInterfaceAddresses().stream()
                        .map(m -> new CidrAddress(m.getAddress(), m.getNetworkPrefixLength()))
                        .filter(cidr -> !cidr.getAddress().isLoopbackAddress()) // (127.x.x.x, ::1)
                        .filter(cidr -> !cidr.getAddress().isLinkLocalAddress())// (169.254.x.x or fe80::/10)
                        .collect(Collectors.toSet());

                if (!addresses.isEmpty()) {
                    logger.trace("Network interface: {} is included in the search", networkInterface.getName());
                    outputMap.put(networkInterface.getName(), addresses);
                } else {
                    logger.trace("Network interface: {} has no usable addresses", networkInterface.getName());
                }
            }
        } catch (SocketException e) {
            logger.trace("Could not get network interfaces", e);
        }
        return outputMap;
    }

    /**
     * Takes the interfaceIPs and fetches every IP which can be assigned on their network
     *
     * @param interfaceIPs The IPs which are assigned to the Network Interfaces
     * @param maximumPerInterface The maximum of IP addresses per interface or 0 to get all.
     * @return Every single IP which can be assigned on the Networks the computer is connected to
     */
    public Set<String> getNetworkIPs(Set<CidrAddress> interfaceIPs, int maximumPerInterface) {
        Set<String> networkIPs = new LinkedHashSet<>();

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
     * Try to establish a TCP connection to the given port.
     *
     * @param host the IP or hostname
     * @param port the TCP port. Must be not 0.
     * @param timeout the timeout before the call aborts
     * @return the {@link PingResult} of connecting to the given port
     * @throws IOException if an error occurs during the connection
     */
    public PingResult servicePing(String host, int port, Duration timeout) throws IOException {
        Instant execStartTime = Instant.now();
        boolean success = false;
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), (int) timeout.toMillis());
            success = true;
        } catch (ConnectException | SocketTimeoutException | NoRouteToHostException e) {
            logger.trace("Could not connect to {}:{} {}", host, port, e.getMessage());
        }
        return new PingResult(success, Duration.between(execStartTime, Instant.now()));
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
            if (os.contains("win")) {
                method = IpPingMethodEnum.WINDOWS_PING;
            } else if (os.contains("mac")) {
                method = IpPingMethodEnum.MAC_OS_PING;
            } else if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
                method = IpPingMethodEnum.IPUTILS_LINUX_PING;
            } else {
                // We cannot estimate the command line for any other operating system and just return false
                return IpPingMethodEnum.JAVA_PING;
            }
        }

        try {
            PingResult pingResult = nativePing(method, "127.0.0.1", Duration.ofSeconds(1));
            if (pingResult != null && pingResult.isSuccess()) {
                return method;
            }
        } catch (IOException e) {
            logger.trace("Native ping to 127.0.0.1 failed", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return IpPingMethodEnum.JAVA_PING;
    }

    /**
     * Return true if the external ARP ping utility (arping) is available and executable on the given path.
     */
    public ArpPingUtilEnum determineNativeArpPingMethod(String arpToolPath) {
        String result = ExecUtil.executeCommandLineAndWaitResponse(Duration.ofMillis(100), arpToolPath, "--help");
        if (result == null || result.isBlank()) {
            logger.trace("The command did not return a response due to an error or timeout");
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
        } else {
            logger.trace("The command output did not match any known output");
            return ArpPingUtilEnum.DISABLED_UNKNOWN_TOOL;
        }
    }

    public enum IpPingMethodEnum {
        DISABLED,
        JAVA_PING,
        WINDOWS_PING,
        IPUTILS_LINUX_PING,
        MAC_OS_PING
    }

    /**
     * Use the native ping utility of the operating system to detect device presence.
     *
     * @param hostname The DNS name, IPv4 or IPv6 address. Must not be null.
     * @param timeout the timeout before the call aborts. Be aware that DNS resolution is not part of this timeout.
     * @return Ping result information. <code>null</code> if ping command was not executed.
     * @throws IOException The ping command could probably not be found
     */
    public @Nullable PingResult nativePing(@Nullable IpPingMethodEnum method, String hostname, Duration timeout)
            throws IOException, InterruptedException {
        Instant execStartTime = Instant.now();

        Process proc;
        if (method == null) {
            return null;
        }
        // Yes, all supported operating systems have their own ping utility with a different command line
        switch (method) {
            case IPUTILS_LINUX_PING:
                proc = new ProcessBuilder("ping", "-w", String.valueOf(timeout.toSeconds()), "-c", "1", hostname)
                        .redirectErrorStream(true).start();
                break;
            case MAC_OS_PING:
                proc = new ProcessBuilder("ping", "-t", String.valueOf(timeout.toSeconds()), "-c", "1", hostname)
                        .redirectErrorStream(true).start();
                break;
            case WINDOWS_PING:
                proc = new ProcessBuilder("ping", "-w", String.valueOf(timeout.toMillis()), "-n", "1", hostname)
                        .redirectErrorStream(true).start();
                break;
            case JAVA_PING:
            default:
                // We cannot estimate the command line for any other operating system and just return null
                return null;
        }

        // Consume the output while the process runs
        List<String> output = new ArrayList<>();
        try (InputStreamReader isr = new InputStreamReader(proc.getInputStream(), StandardCharsets.UTF_8);
                BufferedReader br = new BufferedReader(isr)) {
            String line;
            while ((line = br.readLine()) != null) {
                output.add(line);
                logger.trace("Network [ping output]: '{}'", line);
            }
        }

        // The return code is 0 for a successful ping, 1 if device didn't
        // respond, and 2 if there is another error like network interface
        // not ready.
        // Exception: return code is also 0 in Windows for all requests on the local subnet.
        // see https://superuser.com/questions/403905/ping-from-windows-7-get-no-reply-but-sets-errorlevel-to-0

        int result = proc.waitFor();
        Instant execStopTime = Instant.now();
        if (result != 0) {
            return new PingResult(false, Duration.between(execStartTime, execStopTime));
        }

        if (output.isEmpty()) {
            throw new IOException("Received no output from ping process.");
        }

        for (String line : output) {
            // Because of the Windows issue, we need to check this. We assume that the ping was successful whenever
            // this specific string is contained in the output
            if (line.contains("TTL=") || line.contains("ttl=")) {
                PingResult pingResult = new PingResult(true, Duration.between(execStartTime, execStopTime));
                pingResult.setResponseTime(latencyParser.parseLatency(line, null));
                return pingResult;
            }
        }
        return new PingResult(false, Duration.between(execStartTime, execStopTime));
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
     * There exist two different arping utils with the same name unfortunately.
     * <ul>
     * <li>iputils arping which is sometimes preinstalled on Fedora/Ubuntu and the
     * <li>https://github.com/ThomasHabets/arping which also works on Windows and macOS.
     * </ul>
     *
     * @param arpUtilPath The arping absolute path including filename. Example: "arping" or "/usr/bin/arping" or
     *            "C:\something\arping.exe" or "arp-ping.exe"
     * @param interfaceName An interface name, on linux for example "wlp58s0", shown by ifconfig. Must not be null.
     * @param ipV4address The ipV4 address. Must not be null.
     * @param timeout the timeout before the call aborts
     * @return Ping result information. <code>null</code> if ping command was not executed.
     * @throws IOException The ping command could probably not be found
     */
    public @Nullable PingResult nativeArpPing(@Nullable ArpPingUtilEnum arpingTool, @Nullable String arpUtilPath,
            String interfaceName, String ipV4address, Duration timeout) throws IOException, InterruptedException {
        if (arpUtilPath == null || arpingTool == null || !arpingTool.canProceed) {
            return null;
        }
        Instant execStartTime = Instant.now();
        Process proc;
        if (arpingTool == ArpPingUtilEnum.THOMAS_HABERT_ARPING_WITHOUT_TIMEOUT) {
            proc = new ProcessBuilder(arpUtilPath, "-c", "1", "-i", interfaceName, ipV4address)
                    .redirectErrorStream(true).start();
        } else if (arpingTool == ArpPingUtilEnum.THOMAS_HABERT_ARPING) {
            proc = new ProcessBuilder(arpUtilPath, "-w", String.valueOf(timeout.toSeconds()), "-C", "1", "-i",
                    interfaceName, ipV4address).redirectErrorStream(true).start();
        } else if (arpingTool == ArpPingUtilEnum.ELI_FULKERSON_ARP_PING_FOR_WINDOWS) {
            proc = new ProcessBuilder(arpUtilPath, "-w", String.valueOf(timeout.toMillis()), "-x", ipV4address)
                    .redirectErrorStream(true).start();
        } else {
            proc = new ProcessBuilder(arpUtilPath, "-w", String.valueOf(timeout.toSeconds()), "-c", "1", "-I",
                    interfaceName, ipV4address).redirectErrorStream(true).start();
        }

        // Consume the output while the process runs
        List<String> output = new ArrayList<>();
        try (InputStreamReader isr = new InputStreamReader(proc.getInputStream(), StandardCharsets.UTF_8);
                BufferedReader br = new BufferedReader(isr)) {
            String line;
            while ((line = br.readLine()) != null) {
                output.add(line);
                logger.trace("Network [arping output]: '{}'", line);
            }
        }

        // The return code is 0 for a successful ping. 1 if device didn't respond and 2 if there is another error like
        // network interface not ready.
        int result = proc.waitFor();
        Instant execStopTime = Instant.now();
        if (result != 0) {
            return new PingResult(false, Duration.between(execStartTime, execStopTime));
        }

        PingResult pingResult = new PingResult(true, Duration.between(execStartTime, execStopTime));
        Duration responseTime;
        for (String line : output) {
            if (!line.isBlank()) {
                responseTime = latencyParser.parseLatency(line, arpingTool);
                if (responseTime != null) {
                    pingResult.setResponseTime(responseTime);
                    return pingResult;
                }
            }
        }

        return pingResult;
    }

    /**
     * Execute a Java ping.
     *
     * @param timeout the timeout before the call aborts
     * @param destinationAddress The address to check
     * @return Ping result information
     */
    public PingResult javaPing(Duration timeout, InetAddress destinationAddress) {
        Instant execStartTime = Instant.now();
        boolean success = false;
        try {
            if (destinationAddress.isReachable((int) timeout.toMillis())) {
                success = true;
            }
        } catch (IOException e) {
            logger.trace("Could not connect to {}", destinationAddress, e);
        }
        return new PingResult(success, Duration.between(execStartTime, Instant.now()));
    }

    /**
     * iOS devices are in a deep sleep mode, where they only listen to UDP traffic on port 5353 (Bonjour service
     * discovery). A packet on port 5353 will wake up the network stack to respond to ARP pings at least.
     *
     * @throws IOException if an error occurs during the connection
     */
    public void wakeUpIOS(InetAddress address) throws IOException {
        int port = 5353;
        try (DatagramSocket s = new DatagramSocket()) {
            // Send a valid mDNS packet (12 bytes of zeroes)
            byte[] buffer = new byte[12];
            s.send(new DatagramPacket(buffer, buffer.length, address, port));
            logger.trace("Sent packet to {}:{} to wake up iOS device", address, port);
        } catch (PortUnreachableException e) {
            logger.trace("Unable to send packet to wake up iOS device at {}:{}", address, port, e);
        }
    }
}
