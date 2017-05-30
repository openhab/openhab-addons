/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.neeo.internal;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Enumeration;

import org.openhab.binding.neeo.NeeoConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides static methods to help with MDNS discovery. openHAB's underlying library (JMDNS) doesn't seem to
 * send out queries that NEEO responds to. We use this class to send out the query that NEEO will respond to and provide
 * discovery of all Brains.
 *
 * @author Tim Roberts - Initial contribution
 *
 */
public class MdnsHelper {

    /** The MDNS broadcast address */
    private static final String MDNS_BROADCAST_ADDR = "224.0.0.251";

    /** The MDNS broadcast port */
    private static final int MDNS_BROADCAST_PORT = 5353;

    /**
     * Sends an MDNS query to all the NEEO Brains for discovery
     *
     * @throws IOException
     */
    public static void sendQuery() throws IOException {
        final Logger logger = LoggerFactory.getLogger(MdnsHelper.class);
        final InetAddress multicastAddress = InetAddress.getByName(MDNS_BROADCAST_ADDR);

        final Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            final NetworkInterface current = interfaces.nextElement();
            if (!current.isUp() || current.isLoopback() || current.isVirtual()) {
                continue;
            }

            final Enumeration<InetAddress> addresses = current.getInetAddresses();
            while (addresses.hasMoreElements()) {
                final InetAddress addr = addresses.nextElement();
                if (addr.isLoopbackAddress() || (addr instanceof Inet6Address)) {
                    continue;
                }

                final MulticastSocket socket = new MulticastSocket(MDNS_BROADCAST_PORT);
                try {
                    socket.setInterface(addr);

                    socket.joinGroup(multicastAddress);
                    socket.setTimeToLive(10);
                    socket.setSoTimeout(1000);

                    final ByteBuffer buffer = ByteBuffer.allocate(2000);

                    buffer.putShort((short) 0x0); // transaction id
                    buffer.putShort((short) 0x0); // flags
                    buffer.putShort((short) 0x1); // number of questions
                    buffer.putShort((short) 0x0); // number of answers
                    buffer.putShort((short) 0x0); // number of auth resource
                    buffer.putShort((short) 0x0); // number of addtl resource

                    // Write each of the names (QNAME)
                    for (String label : NeeoConstants.NEEO_MDNS_TYPE.split("\\.")) {
                        final byte[] labelBytes = label.getBytes();
                        buffer.put((byte) (labelBytes.length & 0xff));
                        buffer.put(labelBytes);
                    }
                    buffer.put((byte) 0); // terminator

                    buffer.putShort((short) 0x0c); // record type (QTYPE) - query
                    buffer.put((byte) 0x80); // Question: true
                    buffer.put((byte) 1); // class (IN) QCLASS

                    final DatagramPacket packet = new DatagramPacket(buffer.array(), buffer.position(),
                            multicastAddress, MDNS_BROADCAST_PORT);
                    packet.setAddress(multicastAddress);

                    logger.debug("Sending MDNS Query for {} on {}", NeeoConstants.NEEO_MDNS_TYPE, addr);
                    socket.send(packet);
                } catch (SocketException e) {
                    logger.debug("Exception sending MDNS Query for {} on {}: {}", NeeoConstants.NEEO_MDNS_TYPE, addr,
                            e.getMessage(), e);
                } finally {
                    socket.close();
                }
            }
        }
    }
}
