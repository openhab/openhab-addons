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
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

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
    private static final int SOCKET_TIMEOUT_MSECS = 500;
    private static final int SEARCH_DURATION_MSECS = 5000;
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
    private ScheduledExecutorService executor;
    private @Nullable Listener listener = null;

    private class Listener implements Callable<Set<String>> {

        private boolean interrupted = false;
        private boolean started = false;

        public void interrupt() {
            interrupted = true;
        }

        public boolean hasStarted() {
            return started;
        }

        /**
         * Listens for Velux Bridges and returns their IP addresses. It loops for SEARCH_DURATION_MSECS or until
         * 'interrupt()' or 'Thread.interrupted()' are called when it terminates early after the next socket read
         * timeout i.e. after SOCKET_TIMEOUT_MSECS
         *
         * @return a set of strings containing dotted IP addresses e.g. '123.123.123.123'
         */
        @Override
        public Set<String> call() throws Exception {
            final Set<String> ipAddresses = new HashSet<>();

            // create a multicast listener socket
            try (MulticastSocket rcvSocket = new MulticastSocket(MDNS_PORT)) {
                final byte[] rcvBytes = new byte[BUFFER_SIZE];
                final long finishTime = System.currentTimeMillis() + SEARCH_DURATION_MSECS;

                rcvSocket.setReuseAddress(true);
                rcvSocket.joinGroup(InetAddress.getByName(MDNS_ADDR));
                rcvSocket.setSoTimeout(SOCKET_TIMEOUT_MSECS);

                // tell the caller that we are ready to roll
                started = true;

                // loop until time out or internally or externally interrupted
                while ((System.currentTimeMillis() < finishTime) && (!interrupted) && (!Thread.interrupted())) {
                    // read next packet
                    DatagramPacket rcvPacket = new DatagramPacket(rcvBytes, rcvBytes.length);
                    try {
                        rcvSocket.receive(rcvPacket);
                        if (isKlfLanResponse(rcvPacket.getData())) {
                            ipAddresses.add(rcvPacket.getAddress().getHostAddress());
                        }
                    } catch (SocketTimeoutException e) {
                        // time out is ok, continue listening
                        continue;
                    }
                }
            } catch (IOException e) {
                logger.debug("listenerRunnable(): udp socket exception '{}'", e.getMessage());
            }
            // prevent caller waiting forever in case start up failed
            started = true;
            return ipAddresses;
        }
    }

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

                    dataStream.readShort(); // nsCount
                    dataStream.readShort(); // arCount

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
                            i += dataStream.read(str, i, segLength);
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
                        dataStream.readInt(); // TTL
                        dataStream.readShort(); // dataLen

                        // parse the host name
                        i = 0;
                        while ((segLength = dataStream.readByte()) > 0) {
                            i += dataStream.read(str, i, segLength);
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
     * Private synchronized method that searches for Velux Bridges and returns their IP addresses. Takes
     * SEARCH_DURATION_MSECS to complete.
     *
     * @return a set of strings containing dotted IP addresses e.g. '123.123.123.123'
     */
    private synchronized Set<String> discoverBridgeIpAddresses() {
        @Nullable
        Set<String> result = null;

        // create a random query id
        Random random = new Random();
        randomQueryId = (short) random.nextInt(Short.MAX_VALUE);

        // create the listener task and start it
        Listener listener = this.listener = new Listener();

        // create a datagram socket
        try (DatagramSocket socket = new DatagramSocket()) {
            // prepare query packet
            byte[] dnsBytes = buildQuery();
            DatagramPacket dnsPacket = new DatagramPacket(dnsBytes, 0, dnsBytes.length,
                    InetAddress.getByName(MDNS_ADDR), MDNS_PORT);

            // create listener and wait until it has started
            Future<Set<String>> future = executor.submit(listener);
            while (!listener.hasStarted()) {
                Thread.sleep(SLEEP_MSECS);
            }

            // send the query several times
            for (int i = 0; i < REPEAT_COUNT; i++) {
                // send the query several times
                socket.send(dnsPacket);
                Thread.sleep(SLEEP_MSECS);
            }

            // wait for the listener future to get the result
            result = future.get();
        } catch (InterruptedException | IOException | ExecutionException e) {
            logger.debug("discoverBridgeIpAddresses(): unexpected exception '{}'", e.getMessage());
        }

        // clean up listener task (just in case) and return
        listener.interrupt();
        this.listener = null;
        return result != null ? result : new HashSet<>();
    }

    /**
     * Constructor
     *
     * @param executor the caller's task executor
     */
    public VeluxBridgeFinder(ScheduledExecutorService executor) {
        this.executor = executor;
    }

    /**
     * Interrupt the {@link Listener}
     *
     * @throws IOException (not)
     */
    @Override
    public void close() throws IOException {
        Listener listener = this.listener;
        if (listener != null) {
            listener.interrupt();
            this.listener = null;
        }
    }

    /**
     * Static method to search for Velux Bridges and return their IP addresses. NOTE: it takes SEARCH_DURATION_MSECS to
     * complete, so don't call it on the main thread!
     *
     * @return set of dotted IP address e.g. '123.123.123.123'
     */
    public static Set<String> discoverIpAddresses(ScheduledExecutorService scheduler) {
        try (VeluxBridgeFinder finder = new VeluxBridgeFinder(scheduler)) {
            return finder.discoverBridgeIpAddresses();
        } catch (IOException e) {
            return new HashSet<>();
        }
    }
}
