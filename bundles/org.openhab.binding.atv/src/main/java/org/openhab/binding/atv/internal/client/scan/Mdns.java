/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.atv.internal.client.scan;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.atv.internal.client.support.CaseInsensitiveMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Minimalistic DNS-SD implementation, unicast path only.
 *
 * <p>
 * openHAB discovers devices via configured host addresses (or jmDNS for multicast
 * browsing, see {@link JmdnsScanner}), and jmDNS cannot direct queries at a specific
 * host — hence this implementation.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class Mdns {

    /** Number of services to include in each request. */
    public static final int SERVICES_PER_MSG = 3;

    /** Sleep proxy service type, implicitly queried with every request. */
    public static final String SLEEP_PROXY_SERVICE = "_sleep-proxy._udp.local";

    /** Device information service type carrying the {@code model} property. */
    public static final String DEVICE_INFO_SERVICE = "_device-info._tcp.local";

    private static final Logger LOGGER = LoggerFactory.getLogger(Mdns.class);
    private static final int RECEIVE_BUFFER_SIZE = 65535;

    private Mdns() {
    }

    /**
     * Creates service request messages.
     *
     * <p>
     * Services are batched {@link #SERVICES_PER_MSG} per message and
     * {@code _sleep-proxy._udp.local} is always appended to each message. Note: four
     * (not three) services are sliced per chunk while still advancing by three, so a
     * fourth service overlaps into the previous message; this is intentional, kept for
     * request-count compatibility.
     *
     * @param services service types to query
     * @param qtype query type (PTR for scans, ANY for sleep-proxy follow-ups)
     * @return encoded query messages
     */
    public static List<byte[]> createServiceQueries(List<String> services, QueryType qtype) {
        List<byte[]> queries = new ArrayList<>();
        int messages = (services.size() + SERVICES_PER_MSG - 1) / SERVICES_PER_MSG;
        for (int i = 0; i < messages; i++) {
            int from = i * SERVICES_PER_MSG;
            int to = Math.min(from + 4, services.size());

            DnsMessage msg = new DnsMessage(0x35FF);
            for (String service : services.subList(from, to)) {
                msg.questions().add(new DnsQuestion(service, qtype, 0x8001));
            }
            msg.questions().add(new DnsQuestion(SLEEP_PROXY_SERVICE, qtype, 0x8001));
            queries.add(msg.pack());
        }
        return queries;
    }

    /**
     * Decodes a bytes value, converting non-breaking spaces ({@code 0xC2A0},
     * {@code 0x00A0}) to regular spaces before decoding.
     *
     * <p>
     * When UTF-8 decoding fails, the fallback decodes as ISO-8859-1 instead, which
     * keeps the raw byte values readable.
     *
     * @param value raw property value
     * @return decoded string
     */
    public static String decodeValue(byte[] value) {
        byte[] replaced = replaceNbsp(value);
        try {
            return StandardCharsets.UTF_8.newDecoder().decode(ByteBuffer.wrap(replaced)).toString();
        } catch (CharacterCodingException e) {
            return new String(value, StandardCharsets.ISO_8859_1);
        }
    }

    private static byte[] replaceNbsp(byte[] value) {
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream(value.length);
        for (int i = 0; i < value.length; i++) {
            if (i + 1 < value.length && (value[i] == (byte) 0xC2 || value[i] == 0x00) && value[i + 1] == (byte) 0xA0) {
                out.write(' ');
                i++;
            } else {
                out.write(value[i]);
            }
        }
        return out.toByteArray();
    }

    /**
     * Decodes raw TXT properties into strings.
     *
     * @param properties raw properties
     * @return decoded properties with case-insensitive keys
     */
    public static CaseInsensitiveMap<String> decodeProperties(Map<String, byte[]> properties) {
        CaseInsensitiveMap<String> decoded = new CaseInsensitiveMap<>();
        for (Map.Entry<String, byte[]> entry : properties.entrySet()) {
            decoded.put(entry.getKey(), decodeValue(entry.getValue()));
        }
        return decoded;
    }

    /**
     * Extracts the device model from a {@code _device-info._tcp.local} service.
     *
     * @param services discovered services
     * @return the model string or {@code null}
     */
    public static @Nullable String getModel(List<MdnsService> services) {
        for (MdnsService service : services) {
            if (DEVICE_INFO_SERVICE.equals(service.type())) {
                return service.properties().get("model");
            }
        }
        return null;
    }

    /**
     * Sends a request for services to a specific host.
     *
     * <p>
     * All queries are (re)sent once per second until every query message has been
     * answered or the timeout expires. Responses accumulate in a {@link ServiceParser},
     * so a service may be assembled from records spread over several messages.
     *
     * <p>
     * Since openHAB only scans via unicast, this also folds in two extra behaviors:
     * <ul>
     * <li>Sleep-proxy detection: a response in which every service has port 0 marks the
     * device as deep-sleeping and triggers follow-up {@code ANY} queries for the
     * announced service instance names (sent by the resend loop). Such responses do not
     * count towards completion.</li>
     * <li>On timeout, the accumulated partial response (with the deep-sleep flag) is
     * returned instead of an empty one.</li>
     * <li>Responses without services do not count towards completion, so early
     * completion only happens once every query message got a non-empty,
     * non-sleep-proxy answer.</li>
     * </ul>
     *
     * @param address host to query
     * @param port target port (5353 for mDNS; injectable for tests)
     * @param services service types to query
     * @param timeout maximum time to wait
     * @return future completing with the accumulated response (never exceptionally,
     *         except when the socket cannot be created)
     */
    public static CompletableFuture<MdnsResponse> unicast(InetAddress address, int port, List<String> services,
            Duration timeout) {
        List<byte[]> queries = createServiceQueries(services, QueryType.PTR);
        CompletableFuture<MdnsResponse> result = new CompletableFuture<>();

        DatagramSocket socket;
        try {
            socket = new DatagramSocket();
            socket.connect(address, port);
        } catch (IOException e) {
            result.completeExceptionally(e);
            return result;
        }

        UnicastQueryState state = new UnicastQueryState(queries, socket, result, address);
        result.whenComplete((r, e) -> socket.close());

        Thread.ofVirtual().name("atv-mdns-recv-" + address.getHostAddress()).start(state::receiveLoop);
        Thread.ofVirtual().name("atv-mdns-send-" + address.getHostAddress()).start(() -> state.resendLoop(timeout));
        return result;
    }

    /** Mutable state shared between the send and receive threads of a unicast query. */
    private static final class UnicastQueryState {

        private final List<byte[]> queries;
        private final DatagramSocket socket;
        private final CompletableFuture<MdnsResponse> result;
        private final InetAddress address;
        private final ServiceParser parser = new ServiceParser();
        private final Object lock = new Object();
        private List<byte[]> followUps = List.of();
        private boolean deepSleep;
        private int receivedResponses;

        private UnicastQueryState(List<byte[]> queries, DatagramSocket socket, CompletableFuture<MdnsResponse> result,
                InetAddress address) {
            this.queries = queries;
            this.socket = socket;
            this.result = result;
            this.address = address;
        }

        private void receiveLoop() {
            byte[] buffer = new byte[RECEIVE_BUFFER_SIZE];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            while (!result.isDone()) {
                try {
                    socket.receive(packet);
                } catch (IOException e) {
                    // Socket closed (query finished) or a send/receive error, which
                    // also finishes the query.
                    if (!result.isDone()) {
                        LOGGER.debug("Error during DNS lookup for {}: {}", address, e.toString());
                        completeNow();
                    }
                    return;
                }
                try {
                    handleDatagram(java.util.Arrays.copyOfRange(packet.getData(), packet.getOffset(),
                            packet.getOffset() + packet.getLength()));
                } catch (RuntimeException e) {
                    LOGGER.debug("Failed to decode DNS response from {}", address, e);
                }
            }
        }

        private void handleDatagram(byte[] data) {
            DnsMessage message = new DnsMessage().unpack(data);
            List<MdnsService> messageServices = new ServiceParser().addMessage(message).parse();
            synchronized (lock) {
                parser.addMessage(message);

                if (messageServices.isEmpty()) {
                    // Responses without services do not count towards completion,
                    // otherwise empty answers to resent queries would trip the
                    // completion count early
                    return;
                }

                boolean isSleepProxy = messageServices.stream().allMatch(service -> service.port() == 0);
                if (isSleepProxy) {
                    // Device is sleeping: ask the sleep proxy for everything it knows
                    // about the announced services
                    deepSleep = true;
                    followUps = createServiceQueries(
                            messageServices.stream().map(service -> service.name() + "." + service.type()).toList(),
                            QueryType.ANY);
                } else {
                    receivedResponses++;
                    if (receivedResponses >= queries.size()) {
                        completeNow();
                    }
                }
            }
        }

        private void resendLoop(Duration timeout) {
            long iterations = (timeout.toMillis() + 999) / 1000;
            for (long i = 0; i < iterations && !result.isDone(); i++) {
                List<byte[]> pending;
                synchronized (lock) {
                    pending = new ArrayList<>(queries);
                    pending.addAll(followUps);
                }
                for (byte[] query : pending) {
                    try {
                        socket.send(new DatagramPacket(query, query.length));
                    } catch (IOException e) {
                        LOGGER.debug("Failed to send DNS request to {}: {}", address, e.toString());
                    }
                }
                try {
                    result.get(1, TimeUnit.SECONDS);
                    return;
                } catch (TimeoutException e) {
                    // resend
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (ExecutionException e) {
                    return;
                }
            }
            completeNow();
        }

        private void completeNow() {
            MdnsResponse response;
            synchronized (lock) {
                List<MdnsService> services = parser.parse();
                response = new MdnsResponse(services, deepSleep, getModel(services));
            }
            result.complete(response);
        }
    }
}
