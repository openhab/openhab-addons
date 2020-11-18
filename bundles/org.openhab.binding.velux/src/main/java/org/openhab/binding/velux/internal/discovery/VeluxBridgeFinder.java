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
package org.openhab.binding.velux.internal.discovery;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that uses Multicast DNS (mDNS) to discover Velux Bridges and return their ipv4 addresses
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class VeluxBridgeFinder implements Closeable {

    private final Logger logger = LoggerFactory.getLogger(VeluxBridgeFinder.class);

    // timing constants
    private static final int BUFFER_SIZE = 256;
    private static final int SLEEP_MSECS = 100;
    private static final int WARMUP_MSECS = 10;
    private static final int TIMEOUT_MSECS = 5000;
    private static final int REPEAT_COUNT = 3;

    // dns communication constants
    private static final int MDNS_PORT = 5353;
    private static final String MDNS_ADDR = "224.0.0.251";

    // dns flag constants
    private static final short FLAGS_QR = (short) 0x8000;
    private static final short FLAGS_AA = 0x0400;

    // dns message class constants
    private static final short CLASS_IN = 0x0001;
    private static final short CLASS_MASK = 0x7FFF;

    // dns message type constants
    private static final short TYPE_PTR = 0x000c;

    private static final byte NULL = 0x00;

    // Velux bridge identifiers
    private static final String KLF_SERVICE_ID = "_http._tcp.local";
    private static final String KLF_HOST_PREFIX = "VELUX_KLF_";

    private short randomQueryId;

    private Set<String> ipAddresses = new HashSet<>();

    private @Nullable Thread listenerThread = null;

    /**
     * A runnable this listens for incoming UDP responses
     */
    private Runnable listenerRunnable = () -> {
        byte[] rcvBytes = new byte[BUFFER_SIZE];

        // create a multicast listener socket
        try (MulticastSocket rcvSocket = new MulticastSocket(MDNS_PORT)) {
            rcvSocket.setReuseAddress(true);
            rcvSocket.joinGroup(InetAddress.getByName(MDNS_ADDR));
            rcvSocket.setSoTimeout(TIMEOUT_MSECS);

            // loop until stopped
            while (!Thread.interrupted()) {
                // read next packet
                DatagramPacket rcvPacket = new DatagramPacket(rcvBytes, rcvBytes.length);
                try {
                    rcvSocket.receive(rcvPacket);
                    if (isKlfLanResponse(rcvPacket.getData())) {
                        ipAddresses.add(rcvPacket.getAddress().getHostAddress());
                    }
                } catch (IOException e) {
                    logger.trace("listenerRunnable(): mdns packet receive exception '{}'", e.getMessage());
                    continue;
                }
            }
        } catch (IOException e) {
            logger.debug("listenerRunnable(): udp socket create exception '{}'", e.getMessage());
        }
        listenerThread = null;
    };

    /**
     * Build an mDNS query package to query SERVICE_ID looking for host names
     *
     * @return a byte array containing the query datagram payload, or an empty array if failed
     */
    private byte[] buildQuery() {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream(BUFFER_SIZE);
        DataOutputStream dataStream = new DataOutputStream(byteStream);
        try {
            dataStream.writeShort(randomQueryId); // id
            dataStream.writeShort(0); // flags
            dataStream.writeShort(1); // qdCount
            dataStream.writeShort(0); // anCount
            dataStream.writeShort(0); // nsCount
            dataStream.writeShort(0); // arCount
            for (String segString : KLF_SERVICE_ID.split("\\.")) {
                byte[] segBytes = segString.getBytes(StandardCharsets.UTF_8);
                dataStream.writeByte(segBytes.length); // length
                dataStream.write(segBytes); // byte string
            }
            dataStream.writeByte(NULL); // end of name
            dataStream.writeShort(TYPE_PTR); // type
            dataStream.writeShort(CLASS_IN); // class
            return byteStream.toByteArray();
        } catch (IOException e) {
            // fall through
        }
        return new byte[0];
    }

    /**
     * Parse an mDNS response package and check if it is from a KLF bridge
     *
     * @param responsePayload a byte array containing the response datagram payload
     * @return true if the response is from a KLF bridge
     */
    private boolean isKlfLanResponse(byte[] responsePayload) {
        DataInputStream dataStream = new DataInputStream(new ByteArrayInputStream(responsePayload));
        try {
            // check if the package id matches the query
            short id = dataStream.readShort();
            if (id == randomQueryId) {
                short flags = dataStream.readShort();
                boolean isResponse = (flags & FLAGS_QR) == FLAGS_QR;
                boolean isAuthoritative = (flags & FLAGS_AA) == FLAGS_AA;

                // check if it is an authoritative response
                if (isResponse && isAuthoritative) {
                    short qdCount = dataStream.readShort();
                    short anCount = dataStream.readShort();

                    @SuppressWarnings("unused")
                    short nsCount = dataStream.readShort();
                    @SuppressWarnings("unused")
                    short arCount = dataStream.readShort();

                    // check it is an answer (and not a query)
                    if ((anCount == 0) || (qdCount != 0)) {
                        return false;
                    }

                    // parse the answers
                    for (short an = 0; an < anCount; an++) {
                        // parse the name
                        byte[] str = new byte[BUFFER_SIZE];
                        int i = 0;
                        int segLength;
                        while ((segLength = dataStream.readByte()) > 0) {
                            for (int index = 0; index < segLength; index++) {
                                str[i] = dataStream.readByte();
                                i++;
                            }
                            str[i] = '.';
                            i++;
                        }
                        String name = new String(str, 0, i, StandardCharsets.UTF_8);
                        short typ = dataStream.readShort();
                        short clazz = (short) (CLASS_MASK & dataStream.readShort());
                        if (!(name.startsWith(KLF_SERVICE_ID)) || (typ != TYPE_PTR) || (clazz != CLASS_IN)) {
                            return false;
                        }

                        // if we got here, the name and response type are valid
                        @SuppressWarnings("unused")
                        int ttl = dataStream.readInt();
                        @SuppressWarnings("unused")
                        short dataLen = dataStream.readShort();

                        // parse the host name
                        i = 0;
                        while ((segLength = dataStream.readByte()) > 0) {
                            for (int index = 0; index < segLength; index++) {
                                str[i] = dataStream.readByte();
                                i++;
                            }
                            str[i] = '.';
                            i++;
                        }

                        // check if the host name matches
                        String host = new String(str, 0, i, StandardCharsets.UTF_8);
                        if (host.startsWith(KLF_HOST_PREFIX)) {
                            return true;
                        }
                    }
                }
            }
        } catch (IOException e) {
            // fall through
        }
        return false;
    }

    /**
     * Start the listener thread
     */
    private void startListener() {
        Thread listenerThreadX = this.listenerThread;
        if (listenerThreadX == null) {
            listenerThreadX = listenerThread = new Thread(listenerRunnable);
            listenerThreadX.start();
        }
    }

    /**
     * Stop the listener thread
     */
    private void stopListener() {
        Thread listenerThreadX = this.listenerThread;
        if (listenerThreadX != null) {
            listenerThreadX.interrupt();
        }
    }

    /**
     * Private method that searches for Velux Bridges and returns their IP addresses
     *
     * @return list of dotted IP address e.g. '123.123.123.123'
     */
    private Set<String> discoverBridgeIpAddresses() {
        ipAddresses.clear();

        // create a random query id
        Random random = new Random();
        randomQueryId = (short) random.nextInt(Short.MAX_VALUE);

        // create a datagram socket
        try (DatagramSocket socket = new DatagramSocket()) {
            // warm up the listener thread
            startListener();
            Thread.sleep(WARMUP_MSECS);

            // prepare query packet
            byte[] dnsBytes = buildQuery();
            DatagramPacket dnsPacket = new DatagramPacket(dnsBytes, 0, dnsBytes.length,
                    InetAddress.getByName(MDNS_ADDR), MDNS_PORT);

            // loop while listener polls for a response
            int i = (TIMEOUT_MSECS / SLEEP_MSECS) + 1;
            while (i-- >= 0) {
                // send the query several times
                int sent = 0;
                if (sent < REPEAT_COUNT) {
                    socket.send(dnsPacket);
                    sent++;
                }
                // sleep
                Thread.sleep(SLEEP_MSECS);
            }
        } catch (InterruptedException | IOException e) {
            logger.debug("getIpAddress(): exception '{}'", e.getMessage());
        } finally {
            stopListener();
        }
        return ipAddresses;
    }

    @Override
    public void close() throws IOException {
        stopListener();
    }

    /**
     * Static method to search for Velux Bridges and return their IP addresses
     *
     * @return list of dotted IP addresses
     */
    public static Set<String> discoverIpAddresses() {
        try (VeluxBridgeFinder finder = new VeluxBridgeFinder()) {
            return finder.discoverBridgeIpAddresses();
        } catch (IOException e) {
        }
        return new HashSet<>();
    }
}
