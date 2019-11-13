/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.magentatv.internal.network;

import static org.openhab.binding.magentatv.internal.MagentaTVBindingConstants.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Map;

import org.apache.commons.net.util.SubnetUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.net.NetworkAddressService;
import org.openhab.binding.magentatv.internal.MagentaTVException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MagentaTVNetwork} supplies network functions.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class MagentaTVNetwork {
    private final Logger logger = LoggerFactory.getLogger(MagentaTVNetwork.class);

    private String localIP = "";
    private String localPort = "";
    private String localMAC = "";
    private @Nullable NetworkInterface localInterface;

    /**
     * Init local network interface, determine local IP and MAC address
     *
     * @param networkAddressService
     * @return
     */
    @SuppressWarnings({ "null", "unused" })
    public void initLocalNet(NetworkAddressService networkAddressService) throws MagentaTVException {
        try {
            Map<String, String> env = System.getenv();
            String portEnv = env.get(OPENHAB_HTTP_PORT);
            localPort = (portEnv != null) ? portEnv : DEF_LOCAL_PORT;
            // }
            logger.trace("initLocalNet(): local OH port = {}", localPort);
            System.setProperty("java.net.preferIPv4Stack", "true");

            if (networkAddressService == null) {
                throw new MagentaTVException("networkAddressService not started");
            }
            String lip = networkAddressService.getPrimaryIpv4HostAddress();
            if (lip != null) {
                localIP = lip;
            }
            if ((localIP == null) || localIP.isEmpty()) {
                InetAddress candidateAddress = null;
                NetworkInterface condidateIFace = null;

                // Iterate all NICs (network interface cards)...
                for (Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces(); ifaces
                        .hasMoreElements();) {
                    if (!localIP.isEmpty()) {
                        break;
                    }
                    NetworkInterface iface = ifaces.nextElement();
                    if (iface.isLoopback() || iface.isVirtual() || iface.isPointToPoint()
                            || iface.getName().contains("tun") || iface.getName().contains("awd")
                            || iface.getName().contains("bridge")) {
                        logger.trace("skipping interface {}", iface.getName());
                        continue;
                    }

                    // Iterate all IP addresses assigned to each card...
                    for (Enumeration<InetAddress> inetAddrs = iface.getInetAddresses(); inetAddrs.hasMoreElements();) {
                        InetAddress inetAddr = inetAddrs.nextElement();
                        logger.trace("interface {}: ip={}", iface.getName(), inetAddr.getHostAddress());
                        if (inetAddr.getHostAddress().contains(":")) {
                            logger.trace("skip IPv6 address {}", inetAddr.getHostAddress());
                        }
                        if (!inetAddr.isLoopbackAddress()) {
                            if (inetAddr.isSiteLocalAddress()) {
                                // Found non-loopback site-local address. Return it immediately...
                                logger.trace("site-local address: {}", inetAddr.getHostAddress());
                                localIP = inetAddr.getHostAddress();
                                localInterface = iface;
                                break;
                            } else if (candidateAddress == null) {
                                // Found non-loopback address, but not necessarily site-local.
                                // Store it as a candidate to be returned if site-local address is not
                                // subsequently found...
                                // Note that we don't repeatedly assign non-loopback non-site-local addresses as
                                // candidates, only the first. For subsequent iterations, candidateAddress will
                                // be non-null.
                                candidateAddress = inetAddr;
                                condidateIFace = iface;
                                logger.trace("local address candidate: {}", candidateAddress.getHostAddress());
                            }
                        }
                    }
                }
                if (localIP.isEmpty() && (candidateAddress != null)) {
                    // We did not find a site-local address, but we found some other non-loopback
                    // address.
                    // Server might have a non-site-local address assigned to its NIC
                    // (or it might be running IPv6 which deprecates the "site-local" concept)
                    // So return this non-loopback candidate address...
                    logger.trace("founbd local interface address: {}", candidateAddress.getHostAddress());
                    localIP = candidateAddress.getHostAddress();
                    localInterface = condidateIFace;

                }

                if (localIP.isEmpty() || localIP.equals("0.0.0.0") || localIP.equals("127.0.0.1")) {
                    logger.trace("getLocalHostAsString() is empty, try datagram socket");
                    try (final DatagramSocket socket = new DatagramSocket()) {
                        socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
                        localIP = socket.getLocalAddress().getHostAddress().toString();
                    }
                }
                if (localIP.isEmpty() || localIP.equals("0.0.0.0") || localIP.equals("127.0.0.1")) {
                    // At this point, we did not find a non-loopback address.
                    // So fall back to returning whatever InetAddress.getLocalHost() returns...
                    InetAddress jdkSuppliedAddress = InetAddress.getLocalHost();
                    if (jdkSuppliedAddress == null) {
                        throw new UnknownHostException(
                                "The JDK InetAddress.getLocalHost() method unexpectedly returned null.");
                    }
                    localIP = jdkSuppliedAddress.getHostAddress();
                    logger.trace("jdkSuppliedAddress: {}", jdkSuppliedAddress.getHostAddress());
                }
            }

            if (!localIP.isEmpty() && !localIP.equals("0.0.0.0") && !localIP.equals("127.0.0.1")) {
                // get MAC address
                InetAddress ip = InetAddress.getByName(localIP);
                localInterface = NetworkInterface.getByInetAddress(ip);
                if (localInterface != null) {
                    byte[] mac = localInterface.getHardwareAddress();

                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < mac.length; i++) {
                        sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? ":" : ""));
                    }
                    localMAC = sb.toString();
                    logger.debug("Local IP address={}, Local MAC address = {}", localIP, localMAC);
                    return;
                }
            }
        } catch (IOException e) {
        }
        throw new MagentaTVException("Unable to get local IP / MAC address");
    }

    /**
     * Checks if client ip equals or is in range of ip networks provided by
     * semicolon separated list
     *
     * @param clientIp in numeric form like "192.168.0.10"
     * @param ipList like "127.0.0.1;192.168.0.0/24;10.0.0.0/8"
     * @return true if client ip from the list os ips and networks
     */
    @SuppressWarnings("null")
    public static boolean isIpInSubnet(String clientIp, String ipList) {
        if ((ipList == null) || ipList.equals("")) {
            // No ip address provided
            return true;
        }
        String[] subnetMasks = ipList.split(";");
        for (String subnetMask : subnetMasks) {
            subnetMask = subnetMask.trim();
            if (clientIp.equals(subnetMask)) {
                return true;
            }
            if (subnetMask.contains("/")) {
                if (new SubnetUtils(subnetMask).getInfo().isInRange(clientIp)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Nullable
    public NetworkInterface getLocalInterface() {
        return localInterface;
    }

    @SuppressWarnings("null")
    public String getLocalIP() {
        return localIP != null ? localIP : "";
    }

    public String getLocalPort() {
        return localPort;
    }

    public String getLocalMAC() {
        return localMAC;
    }

    public static final int WOL_PORT = 9;

    /**
     * Send a Waker-on-LAN packet
     *
     * @param ipAddr destination ip
     * @param macAddress destination MAC address
     * @throws MagentaTVException
     */
    public void sendWakeOnLAN(String ipAddr, String macAddress) throws MagentaTVException {
        try {
            byte[] macBytes = getMacBytes(macAddress);
            byte[] bytes = new byte[6 + 16 * macBytes.length];
            for (int i = 0; i < 6; i++) {
                bytes[i] = (byte) 0xff;
            }
            for (int i = 6; i < bytes.length; i += macBytes.length) {
                System.arraycopy(macBytes, 0, bytes, i, macBytes.length);
            }

            InetAddress address = InetAddress.getByName(ipAddr);
            DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address, WOL_PORT);
            DatagramSocket socket = new DatagramSocket();
            socket.send(packet);
            socket.close();

            logger.debug("Wake-on-LAN packet sent to {} / {}", ipAddr, macAddress);
        } catch (IOException e) {
            throw new MagentaTVException(e, "Unable to send Wake-on-LAN packet to {} / {}", ipAddr, macAddress);
        }

    }

    /**
     * Convert MAC address from string to byte array
     *
     * @param macStr MAC address as string
     * @return MAC address as byte array
     * @throws IllegalArgumentException
     */
    private static byte[] getMacBytes(String macStr) throws IllegalArgumentException {
        byte[] bytes = new byte[6];
        String[] hex = macStr.split("(\\:|\\-)");
        if (hex.length != 6) {
            throw new IllegalArgumentException("Invalid MAC address.");
        }
        try {
            for (int i = 0; i < 6; i++) {
                bytes[i] = (byte) Integer.parseInt(hex[i], 16);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid hex digit in MAC address.");
        }
        return bytes;
    }
}
