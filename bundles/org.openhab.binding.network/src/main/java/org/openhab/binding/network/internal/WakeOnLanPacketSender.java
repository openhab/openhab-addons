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
package org.openhab.binding.network.internal;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.net.NetUtil;
import org.openhab.core.util.HexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link WakeOnLanPacketSender} broadcasts a magic packet to wake a device.
 *
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
public class WakeOnLanPacketSender {

    private static final int WOL_UDP_PORT = 9;

    // Wake-on-LAN magic packet constants
    static final int PREFIX_BYTE_SIZE = 6;
    private static final int MAC_REPETITIONS = 16;
    static final int MAC_BYTE_SIZE = 6;
    static final int MAGIC_PACKET_BYTE_SIZE = PREFIX_BYTE_SIZE + MAC_REPETITIONS * MAC_BYTE_SIZE;
    private static final String[] MAC_SEPARATORS = new String[] { ":", "-" };

    private final Logger logger = LoggerFactory.getLogger(WakeOnLanPacketSender.class);

    private final String macAddress;

    private final @Nullable String hostname;
    private final @Nullable Integer port;

    private final Consumer<byte[]> magicPacketMacSender;
    private final Consumer<byte[]> magicPacketIpSender;

    public WakeOnLanPacketSender(String macAddress, @Nullable String hostname, @Nullable Integer port) {
        logger.debug("initialized WOL Packet Sender (mac: {}, hostname: {}, port: {}", macAddress, hostname, port);
        this.macAddress = macAddress;
        this.hostname = hostname;
        this.port = port;
        this.magicPacketMacSender = this::sendMagicPacketViaMac;
        this.magicPacketIpSender = this::sendMagicPacketViaIp;
    }

    /**
     * Used for testing only.
     */
    public WakeOnLanPacketSender(String macAddress) {
        logger.debug("initialized WOL Packet Sender (mac: {}", macAddress);
        this.macAddress = macAddress;
        this.hostname = null;
        this.port = null;
        this.magicPacketMacSender = this::sendMagicPacketViaMac;
        this.magicPacketIpSender = this::sendMagicPacketViaIp;
    }

    /**
     * Used for testing only.
     */
    WakeOnLanPacketSender(String macAddress, Consumer<byte[]> magicPacketSender) {
        this.macAddress = macAddress;
        this.hostname = null;
        this.port = null;
        this.magicPacketMacSender = magicPacketSender;
        this.magicPacketIpSender = this::sendMagicPacketViaIp;
    }

    public void sendWakeOnLanPacketViaMac() {
        byte[] magicPacket = createMagicPacket();
        this.magicPacketMacSender.accept(magicPacket);
    }

    public void sendWakeOnLanPacketViaIp() {
        byte[] magicPacket = createMagicPacket();
        this.magicPacketIpSender.accept(magicPacket);
    }

    private byte[] createMagicPacket() {
        byte[] macBytes = createMacBytes(this.macAddress);
        byte[] magicPacket = new byte[MAGIC_PACKET_BYTE_SIZE];
        Arrays.fill(magicPacket, 0, PREFIX_BYTE_SIZE, (byte) 0xff);
        for (int i = PREFIX_BYTE_SIZE; i < MAGIC_PACKET_BYTE_SIZE; i += MAC_BYTE_SIZE) {
            System.arraycopy(macBytes, 0, magicPacket, i, macBytes.length);
        }
        return magicPacket;
    }

    private byte[] createMacBytes(String macAddress) {
        String hexString = macAddress;
        for (String macSeparator : MAC_SEPARATORS) {
            hexString = hexString.replaceAll(macSeparator, "");
        }
        if (hexString.length() != 2 * MAC_BYTE_SIZE) {
            throw new IllegalStateException("Invalid MAC address: " + macAddress);
        }
        return HexUtils.hexToBytes(hexString);
    }

    private void sendMagicPacketViaMac(byte[] magicPacket) {
        try (DatagramSocket socket = new DatagramSocket()) {
            logger.debug("Sending Wake-on-LAN Packet via Broadcast");
            broadcastMagicPacket(magicPacket, socket);
        } catch (SocketException e) {
            logger.error("Failed to open Wake-on-LAN datagram socket", e);
        }
    }

    private void sendMagicPacketViaIp(byte[] magicPacket) {
        try (DatagramSocket socket = new DatagramSocket()) {
            if (hostname != null && !hostname.isBlank()) {
                logger.debug("Sending Wake-on-LAN Packet via IP Address");
                SocketAddress socketAddress = new InetSocketAddress(hostname,
                        Objects.requireNonNullElse(port, WOL_UDP_PORT));
                sendMagicPacketToIp(magicPacket, socket, socketAddress);
            } else {
                throw new IllegalStateException("Hostname is not set!");
            }
        } catch (SocketException e) {
            logger.error("Failed to open Wake-on-LAN datagram socket", e);
        }
    }

    private void broadcastMagicPacket(byte[] magicPacket, DatagramSocket socket) {
        broadcastAddressStream().forEach(broadcastAddress -> {
            try {
                DatagramPacket packet = new DatagramPacket(magicPacket, MAGIC_PACKET_BYTE_SIZE, broadcastAddress,
                        WOL_UDP_PORT);
                socket.send(packet);
                logger.debug("Wake-on-LAN packet sent (MAC address: {}, broadcast address: {})", this.macAddress,
                        broadcastAddress.getHostAddress());
            } catch (IOException e) {
                logger.error("Failed to send Wake-on-LAN packet (MAC address: {}, broadcast address: {})",
                        this.macAddress, broadcastAddress.getHostAddress(), e);
            }
        });
        logger.info("Wake-on-LAN packets sent (MAC address: {})", this.macAddress);
    }

    private void sendMagicPacketToIp(byte[] magicPacket, DatagramSocket socket, SocketAddress ip) {
        DatagramPacket packet = new DatagramPacket(magicPacket, MAGIC_PACKET_BYTE_SIZE, ip);
        try {
            socket.send(packet);
        } catch (IOException e) {
            logger.error("Failed to send Wake-on-LAN packet (IP address: {})", ip, e);
        }
        logger.info("Wake-on-LAN packets sent (IP address: {})", ip);
    }

    private Stream<InetAddress> broadcastAddressStream() {
        return NetUtil.getAllBroadcastAddresses().stream().map(address -> {
            try {
                return InetAddress.getByName(address);
            } catch (UnknownHostException e) {
                logger.error("Failed to get broadcast address '{}' by name", address, e);
                return null;
            }
        }).filter(Objects::nonNull);
    }
}
