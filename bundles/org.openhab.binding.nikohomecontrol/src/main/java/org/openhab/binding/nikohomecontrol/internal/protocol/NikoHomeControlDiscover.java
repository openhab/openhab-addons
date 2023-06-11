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
package org.openhab.binding.nikohomecontrol.internal.protocol;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.util.HexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class {@link NikoHomeControlDiscover} is used to get the Niko Home Control IP-interface IP address for bridge
 * discovery.
 * <p>
 * The constructor broadcasts a UDP packet with content 0x44 on port 10000.
 * The Niko Home Control IP-interface responds to this UDP packet.
 * The IP-address from the Niko Home Control IP-interface is then extracted from the response packet.
 * The data content of the response packet is used as a unique identifier for the bridge.
 *
 * @author Mark Herwege - Initial Contribution
 */
/**
 * @author Mark Herwege - Initial Contribution
 *
 */
@NonNullByDefault
public final class NikoHomeControlDiscover {

    private final Logger logger = LoggerFactory.getLogger(NikoHomeControlDiscover.class);

    private List<String> nhcBridgeIds = new ArrayList<>();
    private Map<String, InetAddress> addr = new HashMap<>();
    private Map<String, Boolean> isNhcII = new HashMap<>();

    /**
     * Discover the list of Niko Home Control IP interfaces by broadcasting UDP packet 0x44 to port 10000. The IP
     * interface will reply. The address of the IP interface is than derived from that response.
     *
     * @param broadcast Broadcast address of the network
     * @throws IOException
     */
    public NikoHomeControlDiscover(String broadcast) throws IOException {
        final byte[] discoverBuffer = { (byte) 0x44 };
        final InetAddress broadcastAddr = InetAddress.getByName(broadcast);
        final int broadcastPort = 10000;

        DatagramPacket discoveryPacket = new DatagramPacket(discoverBuffer, discoverBuffer.length, broadcastAddr,
                broadcastPort);
        byte[] buffer = new byte[1024];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

        try (DatagramSocket datagramSocket = new DatagramSocket(null)) {
            datagramSocket.setBroadcast(true);
            datagramSocket.setSoTimeout(500);
            datagramSocket.send(discoveryPacket);
            try {
                while (true) {
                    datagramSocket.receive(packet);
                    logger.trace("bridge discovery response {}",
                            HexUtils.bytesToHex(Arrays.copyOf(packet.getData(), packet.getLength())));
                    if (isNhcController(packet)) {
                        String bridgeId = setNhcBridgeId(packet);
                        setIsNhcII(bridgeId, packet);
                        setAddr(bridgeId, packet);
                        logger.debug("IP address is {}, unique ID is {}", addr, bridgeId);
                    }
                }
            } catch (SocketTimeoutException e) {
                // all received, continue
            }
        }
    }

    /**
     * @return the discovered nhcBridgeIds
     */
    public List<String> getNhcBridgeIds() {
        return nhcBridgeIds;
    }

    /**
     * @param bridgeId discovered bridgeId
     * @return the addr, null if not in the list of discovered bridgeId's
     */
    public @Nullable InetAddress getAddr(String bridgeId) {
        return addr.get(bridgeId);
    }

    /**
     * Check if the UDP packet comes from a Niko Home Control controller. The response should start with 0x44.
     *
     * @param packet
     * @return true if packet is from a Niko Home Control controller
     */
    private boolean isNhcController(DatagramPacket packet) {
        byte[] packetData = packet.getData();
        boolean isNhc = (packet.getLength() > 2) && (packetData[0] == 0x44);
        // filter response from Gen1 touchscreens
        boolean isController = isNhc && (packetData[1] == 0x3b) || (packetData[1] == 0x0c) || (packetData[1] == 0x0e);
        if (!isController) {
            logger.trace("not a NHC controller");
        }
        return isController;
    }

    /**
     * Retrieves a unique ID from the returned datagram packet received after sending the UDP discovery message.
     *
     * @param packet
     */
    private String setNhcBridgeId(DatagramPacket packet) {
        byte[] packetData = packet.getData();
        int packetLength = packet.getLength();
        packetLength = packetLength > 6 ? 6 : packetLength;
        StringBuilder sb = new StringBuilder(packetLength);
        for (int i = 0; i < packetLength; i++) {
            sb.append(String.format("%02x", packetData[i]));
        }
        String bridgeId = sb.toString();
        nhcBridgeIds.add(bridgeId);
        return bridgeId;
    }

    /**
     * Checks if this is a NHC II Connected Controller
     *
     * @param bridgeId
     * @param packet
     */
    private void setIsNhcII(String bridgeId, DatagramPacket packet) {
        byte[] packetData = packet.getData();
        int packetLength = packet.getLength();
        // The 16th byte in the packet is 2 for a NHC II Connected Controller
        if ((packetLength >= 16) && (packetData[15] >= 2)) {
            isNhcII.put(bridgeId, true);
        } else {
            isNhcII.put(bridgeId, false);
        }
    }

    /**
     * Sets the IP address retrieved from the packet response
     *
     * @param bridgeId
     * @param packet
     */
    private void setAddr(String bridgeId, DatagramPacket packet) {
        addr.put(bridgeId, packet.getAddress());
    }

    /**
     * Test if the installation is a Niko Home Control II installation
     *
     * @param bridgeId
     * @return true if this is a Niko Home Control II installation
     */
    public boolean isNhcII(String bridgeId) {
        return isNhcII.getOrDefault(bridgeId, false);
    }
}
