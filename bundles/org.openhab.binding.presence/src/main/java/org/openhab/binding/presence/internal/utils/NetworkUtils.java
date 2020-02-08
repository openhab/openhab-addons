/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.presence.internal.utils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.PortUnreachableException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.io.net.exec.ExecUtil;
import org.openhab.binding.presence.internal.binding.PresenceBindingConfiguration.ArpPingUtilEnum;
import org.openhab.binding.presence.internal.binding.PresenceBindingConfiguration.IpPingMethodEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Mike Dabbs - Initial contribution
 */
@NonNullByDefault
public class NetworkUtils {
    private static final Logger logger = LoggerFactory.getLogger(NetworkUtils.class);

    /**
     * Return true if the external arp ping utility (arping) is available and executable on the given path.
     */
    static public ArpPingUtilEnum determineNativeARPpingMethod(String arpToolPath) {
        String result = ExecUtil.executeCommandLineAndWaitResponse(arpToolPath + " --help", 1000);
        if (StringUtils.isBlank(result)) {
            logger.trace("using unknown arping tool");
            return ArpPingUtilEnum.UNKNOWN_TOOL;
        } else if (result.contains("Thomas Habets")) {
            if (result.matches("(?s)(.*)w sec Specify a timeout(.*)")) {
                logger.trace("using thomas habet arping tool");
                return ArpPingUtilEnum.THOMAS_HABET_ARPING;
            } else {
                logger.trace("using thomas habet arping without timeout tool");
                return ArpPingUtilEnum.THOMAS_HABET_ARPING_WITHOUT_TIMEOUT;
            }
        } else if (result.contains("-w timeout")) {
            logger.trace("using iptools tool");
            return ArpPingUtilEnum.IPUTILS_ARPING;
        } else if (result.contains("Usage: arp-ping.exe")) {
            logger.trace("using eli fulkerson tool");
            return ArpPingUtilEnum.ELI_FULKERSON_ARP_PING_FOR_WINDOWS;
        }
        logger.trace("using unknown tool: {}", result);
        return ArpPingUtilEnum.UNKNOWN_TOOL;
    }

    /**
     * Return the working method for the native system ping. If no native ping
     * works JavaPing is returned.
     */
    static public IpPingMethodEnum determinePingMethod(String pingPath) {
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
            if (nativePing(method, pingPath, "127.0.0.1", 2000, false)) {
                return method;
            }
        } catch (IOException ignored) {
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Reset interrupt flag
        }
        return IpPingMethodEnum.JAVA_PING;
    }

    /**
     * Use the native ping utility of the operating system to detect device presence.
     *
     * @param hostname The DNS name, IPv4 or IPv6 address. Must not be null.
     * @param timeoutInMS Timeout in milliseconds. Be aware that DNS resolution is not part of this timeout.
     * @return Returns true if the device responded
     * @throws IOException The ping command could probably not be found
     */
    static private boolean nativePing(@Nullable IpPingMethodEnum method, String pingPath, String hostname,
            int timeoutInMS, boolean quiet) throws IOException, InterruptedException {
        Process proc;
        if (method == null) {
            logger.trace("Unable to use system ping, ping method is null");
            return false;
        }
        // Yes, all supported operating systems have their own ping utility with a different command line
        switch (method) {
            case IPUTILS_LINUX_PING:
                proc = new ProcessBuilder(pingPath, "-w", String.valueOf(timeoutInMS / 1000), "-c", "1", hostname)
                        .start();
                break;
            case MAC_OS_PING:
                proc = new ProcessBuilder(pingPath, "-t", String.valueOf(timeoutInMS / 1000), "-c", "1", hostname)
                        .start();
                break;
            case WINDOWS_PING:
                proc = new ProcessBuilder(pingPath, "-w", String.valueOf(timeoutInMS), "-n", "1", hostname).start();
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
            // capture the output for debugging
            BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            BufferedReader ereader = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
            StringBuilder builder = new StringBuilder();
            StringBuilder ebuilder = new StringBuilder();
            String line = null;
            while (proc.isAlive()) {
                Thread.yield();
            }
            while ((line = reader.readLine()) != null) {
                builder.append(line);
                builder.append(System.getProperty("line.separator"));
            }
            String result = builder.toString();
            while ((line = ereader.readLine()) != null) {
                ebuilder.append(line);
                ebuilder.append(System.getProperty("line.separator"));
            }
            String eresult = ebuilder.toString();

            if (!quiet) {
                logger.debug("ping output: {}", result);
                if (eresult != null && eresult.trim().length() > 0) {
                    logger.debug("ping error output: {}", eresult);
                }
            }

            int rc = proc.waitFor();
            if (rc != 0) {
                logger.trace("system ping response was not zero: {}", rc);
            } else {
                logger.debug("system ping is usable");
            }
            return rc == 0;
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
                if (line.contains("TTL=")) {
                    return true;
                }
                line = r.readLine();
            } while (line != null);

            return false;
        }
    }

    /*
     * This method returns a new Process that does nothing but fail. It is used to
     * return a process when the system ping method is unavailable.
     */
    static private Process getNoopProcess() {
        return new Process() {

            @Override
            public void destroy() {
            }

            @Override
            public int exitValue() {
                return 1;
            }

            @Override
            public InputStream getErrorStream() {
                return getInputStream();
            }

            @Override
            public InputStream getInputStream() {
                // TODO Auto-generated method stub
                return new ByteArrayInputStream("Noop process always fails".getBytes());
            }

            @Override
            public OutputStream getOutputStream() {
                return new ByteArrayOutputStream();
            }

            @Override
            public int waitFor() throws InterruptedException {
                return 1;
            }
        };
    }

    /**
     * Use the native ping utility of the operating system to detect device presence.
     *
     * @param hostname The DNS name, IPv4 or IPv6 address. Must not be null.
     * @param timeoutInMS Timeout in milliseconds. Be aware that DNS resolution is not part of this timeout.
     * @return Returns true if the device responded
     * @throws IOException The ping command could probably not be found
     */
    static public Process getPingProcess(@Nullable IpPingMethodEnum method, String pingPath, String hostname,
            int timeoutInMS) throws IOException {

        Process proc;
        if (method == null) {
            logger.trace("Unable to use system ping, ping method is null");
            return getNoopProcess();
        }
        // Yes, all supported operating systems have their own ping utility with a different command line
        switch (method) {
            case IPUTILS_LINUX_PING:
                proc = new ProcessBuilder(pingPath, "-w", String.valueOf(timeoutInMS / 1000), "-c", "3", hostname)
                        .redirectErrorStream(true).start();
                break;
            case MAC_OS_PING:
                proc = new ProcessBuilder(pingPath, "-t", String.valueOf(timeoutInMS / 1000), "-c", "1", hostname)
                        .redirectErrorStream(true).start();
                break;
            case WINDOWS_PING:
                proc = new ProcessBuilder(pingPath, "-w", String.valueOf(timeoutInMS), "-n", "1", hostname)
                        .redirectErrorStream(true).start();
                break;
            case JAVA_PING:
            default:
                // We cannot estimate the command line for any other operating system and just return a noop process
                return getNoopProcess();
        }
        return proc;
    }

    /*
     * Return a Process that executes the arping command
     */
    static public Process getArpProcess(@Nullable ArpPingUtilEnum arpingTool, @Nullable String arpUtilPath,
            String interfaceName, String ipV4address, int timeoutInMS) throws IOException {
        if (arpingTool == ArpPingUtilEnum.THOMAS_HABET_ARPING_WITHOUT_TIMEOUT) {
            return new ProcessBuilder(arpUtilPath, "-c", "1", "-i", interfaceName, ipV4address)
                    .redirectErrorStream(true).start();
        } else if (arpingTool == ArpPingUtilEnum.THOMAS_HABET_ARPING) {
            return new ProcessBuilder(arpUtilPath, "-W", "0.1", "-C", "1", "-c", String.valueOf(timeoutInMS / 100),
                    "-w", String.valueOf(timeoutInMS / 1000), "-i", interfaceName, ipV4address)
                            .redirectErrorStream(true).start();
        } else if (arpingTool == ArpPingUtilEnum.ELI_FULKERSON_ARP_PING_FOR_WINDOWS) {
            return new ProcessBuilder(arpUtilPath, "-w", String.valueOf(timeoutInMS), "-x", ipV4address)
                    .redirectErrorStream(true).start();
        } else {
            return new ProcessBuilder(arpUtilPath, "-w", String.valueOf(timeoutInMS / 1000), "-f", "-c", "1", "-I",
                    interfaceName, ipV4address).redirectErrorStream(true).start();
        }
    }

    // Return a list of interface names
    static public List<String> getInterfaceNames() {
        Set<String> result = new HashSet<>();

        try {
            // For each interface ...
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface networkInterface = en.nextElement();
                if (!networkInterface.isLoopback()) {
                    if (networkInterface.getInterfaceAddresses().size() > 0) {
                        result.add(networkInterface.getName());
                    }
                    // if (networkInterface.getInetAddresses().hasMoreElements()) {
                    // result.add(networkInterface.getName());
                    // }
                }
            }
        } catch (SocketException ignored) {
            // If we are not allowed to enumerate, we return an empty result set.
        }

        return Arrays.asList(result.toArray(new String[0]));
    }

    /**
     * Performs a java ping. It is not recommended to use this, as it will not work on windows
     * systems reliably and will fall back from ICMP pings to
     * the TCP echo service on port 7 which barely no device or server supports nowadays.
     * (http://docs.oracle.com/javase/7/docs/api/java/net/InetAddress.html#isReachable%28int%29)
     * This method is safe to use when wrapped within a Future that is cancellable.
     */
    static public boolean performJavaPing(String address, int timeoutInMS) {
        try {
            logger.trace("Perform java ping presence detection for {}", address);
            InetAddress ip = InetAddress.getByName(address);
            if (ip != null && ip.isReachable(timeoutInMS)) {
                return true;
            }
        } catch (IOException e) {
            logger.trace("Failed to execute a java ping for ip {}", address, e);
        }
        return false;
    }

    /*
     * A help method to send a wakeup packet to an iOS device by hostname
     */
    static public void wakeUpIOS(String address) throws IOException {
        try {
            InetAddress ip = InetAddress.getByName(address);
            wakeUpIOS(ip);
        } catch (UnknownHostException e) {
        }
    }

    /**
     * iOS devices are in a deep sleep mode, where they only listen to UDP traffic on port 5353 (Bonjour service
     * discovery). A packet on port 5353 will wake up the network stack to respond to ARP pings at least.
     *
     * @throws IOException
     */
    static public void wakeUpIOS(InetAddress address) throws IOException {
        try (DatagramSocket s = new DatagramSocket()) {
            byte[] buffer = new byte[0];
            s.send(new DatagramPacket(buffer, buffer.length, address, 5353));
        } catch (PortUnreachableException ignored) {
            // We ignore the port unreachable error
        }
    }
}
