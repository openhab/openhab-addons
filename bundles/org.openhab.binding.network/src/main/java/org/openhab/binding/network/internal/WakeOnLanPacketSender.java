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
package org.openhab.binding.network.internal;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.net.NetUtil;
import org.eclipse.smarthome.core.util.HexUtils;
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
    static final int MAC_REPETITIONS = 16;
    static final int MAC_BYTE_SIZE = 6;
    static final int MAGIC_PACKET_BYTE_SIZE = PREFIX_BYTE_SIZE + MAC_REPETITIONS * MAC_BYTE_SIZE;
    static final String[] MAC_SEPARATORS = new String[] { ":", "-" };

    private final Logger logger = LoggerFactory.getLogger(WakeOnLanPacketSender.class);

    private final String macAddress;
    private byte @Nullable [] magicPacket;
    private final Consumer<byte[]> magicPacketSender;

    public WakeOnLanPacketSender(String macAddress) {
        this.macAddress = macAddress;
        this.magicPacketSender = this::broadcastMagicPacket;
    }

    /**
     * Used for testing only.
     */
    WakeOnLanPacketSender(String macAddress, Consumer<byte[]> magicPacketSender) {
        this.macAddress = macAddress;
        this.magicPacketSender = magicPacketSender;
    }

    public void sendPacket() {
        byte[] localMagicPacket = magicPacket;
        if (localMagicPacket == null) {
            localMagicPacket = createMagicPacket(createMacBytes(macAddress));
            magicPacket = localMagicPacket;
        }

        magicPacketSender.accept(localMagicPacket);
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

    private byte[] createMagicPacket(byte[] macBytes) {
        byte[] bytes = new byte[MAGIC_PACKET_BYTE_SIZE];
        Arrays.fill(bytes, 0, PREFIX_BYTE_SIZE, (byte) 0xff);
        for (int i = PREFIX_BYTE_SIZE; i < MAGIC_PACKET_BYTE_SIZE; i += MAC_BYTE_SIZE) {
            System.arraycopy(macBytes, 0, bytes, i, macBytes.length);
        }
        return bytes;
    }

    private void broadcastMagicPacket(byte[] magicPacket) {
        try (DatagramSocket socket = new DatagramSocket()) {
            broadcastAddressStream().forEach(broadcastAddress -> {
                try {
                    DatagramPacket packet = new DatagramPacket(magicPacket, MAGIC_PACKET_BYTE_SIZE, broadcastAddress,
                            WOL_UDP_PORT);
                    socket.send(packet);
                    logger.debug("Wake-on-LAN packet sent (MAC address: {}, broadcast address: {})", macAddress,
                            broadcastAddress.getHostAddress());
                } catch (IOException e) {
                    logger.debug("Failed to send Wake-on-LAN packet (MAC address: {}, broadcast address: {})",
                            macAddress, broadcastAddress.getHostAddress(), e);
                }
            });
            logger.info("Wake-on-LAN packets sent (MAC address: {})", macAddress);
        } catch (SocketException e) {
            logger.error("Failed to open Wake-on-LAN datagram socket", e);
        }
    }

    private Stream<InetAddress> broadcastAddressStream() {
        return NetUtil.getAllBroadcastAddresses().stream().map(address -> {
            try {
                return InetAddress.getByName(address);
            } catch (UnknownHostException e) {
                logger.debug("Failed to get broadcast address '{}' by name", address, e);
                return null;
            }
        }).filter(Objects::nonNull);
    }
}
