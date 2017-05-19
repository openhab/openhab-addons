/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nikohomecontrol.internal.protocol;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class {@link NikoHomeControlDiscover} is used to get the Niko Home Control IP-interface IP address for bridge
 * discovery.
 * <p>
 * The constructor sends a UDP packet with content 0x44 to the local network on port 10000.
 * The Niko Home Control IP-interface responds to this UDP packet.
 * The IP-address from the Niko Home Control IP-interface is then extracted from the response packet.
 * The data content of the response packet is used as a unique identifier for the bridge.
 *
 * @author Mark Herwege
 */
public final class NikoHomeControlDiscover {

    private Logger logger = LoggerFactory.getLogger(NikoHomeControlCommunication.class);

    private InetAddress addr;
    private String nhcBridgeId;

    public NikoHomeControlDiscover(InetAddress addr) throws IOException {

        InetAddress broadcastaddr = addr;
        if (broadcastaddr == null) {
            broadcastaddr = getBroadcastAddress();
        }
        if (broadcastaddr == null) {
            throw new IOException("Cannot determine broadcast address");
        }

        final byte[] discoverbuffer = { (byte) 0x44 };
        final int broadcastport = 10000;

        DatagramPacket discoveryPacket = new DatagramPacket(discoverbuffer, discoverbuffer.length, broadcastaddr,
                broadcastport);
        byte[] buffer = new byte[1024];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

        try (DatagramSocket datagramSocket = new DatagramSocket(null)) {
            datagramSocket.setBroadcast(true);
            datagramSocket.setSoTimeout(500);
            datagramSocket.send(discoveryPacket);
            datagramSocket.receive(packet);
            this.addr = packet.getAddress();
            setNhcBridgeId(packet);
            logger.debug("Niko Home Control: IP address is {}, unique ID is {}", this.addr, this.nhcBridgeId);
        }

    }

    public NikoHomeControlDiscover() throws IOException {
        this(null);
    }

    /**
     * @return the addr
     */
    public InetAddress getAddr() {
        return this.addr;
    }

    /**
     * @return the nhcBridgeId
     */
    public String getNhcBridgeId() {
        return this.nhcBridgeId;
    }

    /**
     * Retrieves a unique ID from the returned datagram packet received after sending the UDP discovery message.
     *
     * @param packet
     */
    private void setNhcBridgeId(DatagramPacket packet) {

        byte[] packetData = packet.getData();
        int packetLength = packet.getLength();
        packetLength = packetLength > 6 ? 6 : packetLength;
        StringBuilder sb = new StringBuilder(packetLength);
        for (int i = 0; i < packetLength; i++) {
            sb.append(String.format("%02x", packetData[i]));
        }
        this.nhcBridgeId = sb.toString();

    }

    /**
     * Method to try to find a broadcast address from the local network interfaces.
     * If this does not yield the correct result, the broadcast address needs to be set in the configuration.
     *
     * @return current broadcast address
     */
    private InetAddress getBroadcastAddress() {

        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                List<InterfaceAddress> interfaceAddresses = networkInterface.getInterfaceAddresses();
                for (InterfaceAddress interfaceAddress : interfaceAddresses) {
                    InetAddress addr = interfaceAddress.getAddress();
                    if (!addr.isLinkLocalAddress() && !addr.isLoopbackAddress() && addr instanceof Inet4Address) {
                        return interfaceAddress.getBroadcast();
                    }
                }
            }
        } catch (SocketException e) {
            logger.warn("Niko Home Control: unable to determine broadcast address");
        }
        return null;
    }

}
