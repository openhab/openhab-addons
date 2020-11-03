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
import java.util.Random;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that uses Multicast DNS (mDNS) to resolve a host name to its respective ipv4 address
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class MDnsHostnameResolver implements Closeable {

    private final Logger logger = LoggerFactory.getLogger(MDnsHostnameResolver.class);

    // timing constants
    private final static int BUFFER_SIZE = 256;
    private final static int SLEEP_MSECS = 100;
    private final static int WARMUP_MSECS = 10;
    private final static int TIMEOUT_MSECS = 1000;

    // dns communication constants
    private final static int MDNS_PORT = 5353;
    private final static String MDNS_ADDR = "224.0.0.251";

    // dns flag constants
    private static final short FLAGS_QR = (short) 0x8000;
    private static final short FLAGS_AA = 0x0400;
    private static final short FLAGS_RD = 0x0100;

    // dns message class constants
    private static final short CLASS_IN = 0x0001;
    private static final short CLASS_MASK = 0x7FFF;

    // dns message type constants
    private static final short TYPE_A = 0x0001;

    private static final byte NULL = 0x00;

    private short randomQueryId;
    private String dottedHostName = "";
    private String ipAddress = "";

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
                    String ipAddress = parseResponse(rcvPacket.getData(), dottedHostName);
                    if (!ipAddress.isEmpty()) {
                        this.ipAddress = ipAddress;
                        break;
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
     * Build an mdns query package to query the IPv4 address of the given host name
     *
     * @param hostName text host name e.g. 'foobar.local.'
     * @return a byte array containing the query datagram payload, or an empty array if failed
     */
    private byte[] buildQuery(String hostName) {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream(BUFFER_SIZE);
        DataOutputStream dataStream = new DataOutputStream(byteStream);
        try {
            dataStream.writeShort(randomQueryId); // id
            dataStream.writeShort(FLAGS_RD); // flags
            dataStream.writeShort(1); // qdCount
            dataStream.writeShort(0); // anCount
            dataStream.writeShort(0); // nsCount
            dataStream.writeShort(0); // arCount
            for (String segString : hostName.split("\\.")) {
                byte[] segBytes = segString.getBytes(StandardCharsets.UTF_8);
                dataStream.writeByte(segBytes.length); // length
                dataStream.write(segBytes); // byte string
            }
            dataStream.writeByte(NULL); // end of name
            dataStream.writeShort(TYPE_A); // type
            dataStream.writeShort(CLASS_IN); // class
            return byteStream.toByteArray();
        } catch (IOException e) {
            // fall through
        }
        return new byte[0];
    }

    /**
     * Parse an mdns response package and check if it is a valid response with an valid IPv4 address
     *
     * @param responsePayload a byte array containing the response datagram payload
     * @param hostName text host name e.g. 'foobar.local.'
     * @return the dotted IPv4 address, or empty string if failed
     */
    private String parseResponse(byte[] responsePayload, String hostName) {
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
                    if ((anCount > 0) && (qdCount == 0)) {
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

                        // check if the name in the payload matches the hostName
                        if (name.contentEquals(hostName)) {
                            short _type = dataStream.readShort();
                            short _class = (short) (CLASS_MASK & dataStream.readShort());

                            // check if it is an IPv4 address
                            if (_type == TYPE_A && _class == CLASS_IN) {
                                @SuppressWarnings("unused")
                                int ttl = dataStream.readInt();
                                short dataLen = dataStream.readShort();

                                // check if the ipv4 address has a length of 4
                                if (dataLen == 4) {
                                    int ipv4Address = dataStream.readInt();
                                    if (ipv4Address != 0) {
                                        return ((ipv4Address >> 24) & 0xFF) + "." + ((ipv4Address >> 16) & 0xFF) + "."
                                                + ((ipv4Address >> 8) & 0xFF) + "." + (ipv4Address & 0xFF);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            // fall through
        }
        return "";
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
     * Method that resolves the given host name to its ipv4 address
     *
     * @param hostName the textual host name e.g. 'foobar.local'
     * @return dotted ip address e.g. '123.123.123.123'
     */
    public String getIpAddress(String hostName) {
        dottedHostName = hostName + ".";

        // create a random query id
        Random random = new Random();
        randomQueryId = (short) random.nextInt(Short.MAX_VALUE);

        // create a datagram socket
        try (DatagramSocket socket = new DatagramSocket()) {

            // warm up the listener thread
            startListener();
            Thread.sleep(WARMUP_MSECS);

            // prepare query packet
            byte[] dnsBytes = buildQuery(hostName);
            DatagramPacket dnsPacket = new DatagramPacket(dnsBytes, 0, dnsBytes.length,
                    InetAddress.getByName(MDNS_ADDR), MDNS_PORT);

            // send the query
            socket.send(dnsPacket);

            // loop while listener polls for a response
            int i = (TIMEOUT_MSECS / SLEEP_MSECS) + 1;
            while (i-- >= 0) {
                if (!ipAddress.isEmpty()) {
                    return ipAddress;
                }
                Thread.sleep(SLEEP_MSECS);
            }
        } catch (InterruptedException | IOException e) {
            logger.debug("getIpAddress(): exception '{}'", e.getMessage());
        } finally {
            stopListener();
        }
        return "";
    }

    @Override
    public void close() throws IOException {
        stopListener();
    }
}
