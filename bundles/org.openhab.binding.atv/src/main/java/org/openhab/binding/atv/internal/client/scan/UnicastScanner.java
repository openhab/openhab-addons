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

import java.net.InetAddress;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service discovery based on unicast mDNS.
 *
 * <p>
 * Each configured host is queried directly on port 5353 (the port is injectable for
 * tests). While a host is being queried, its well-known service ports are knocked
 * concurrently to wake devices sleeping behind a Bonjour sleep proxy (see
 * {@link Knock}).
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class UnicastScanner extends ScanOrchestrator {

    private static final Logger LOGGER = LoggerFactory.getLogger(UnicastScanner.class);
    private static final int MDNS_PORT = 5353;

    private final List<InetAddress> hosts;
    private final int port;

    /**
     * Creates a new scanner querying the standard mDNS port.
     *
     * @param hosts hosts to scan
     */
    public UnicastScanner(List<InetAddress> hosts) {
        this(hosts, MDNS_PORT);
    }

    /**
     * Creates a new scanner with a custom target port (for testing purposes).
     *
     * @param hosts hosts to scan
     * @param port UDP port to send queries to
     */
    public UnicastScanner(List<InetAddress> hosts, int port) {
        this.hosts = List.copyOf(hosts);
        this.port = port;
    }

    @Override
    protected CompletableFuture<@Nullable Void> process(Duration timeout) {
        List<CompletableFuture<MdnsResponse>> responses = new ArrayList<>();
        for (InetAddress host : hosts) {
            responses.add(getServices(host, timeout));
        }
        return CompletableFuture.allOf(responses.toArray(CompletableFuture[]::new)).thenRun(() -> {
            for (CompletableFuture<MdnsResponse> response : responses) {
                handleResponse(response.join());
            }
        });
    }

    private CompletableFuture<MdnsResponse> getServices(InetAddress host, Duration timeout) {
        CompletableFuture<@Nullable Void> knocker = startKnocker(host, Knock.KNOCK_PORTS, timeout);
        return Mdns.unicast(host, port, services(), timeout).exceptionally(e -> {
            LOGGER.debug("Unicast scan of {} failed: {}", host, e.toString());
            return MdnsResponse.EMPTY;
        }).whenComplete((response, error) -> knocker.cancel(false));
    }

    /**
     * Starts the port knocker accompanying a host scan. Overridable so tests can
     * substitute a fake without binding the real, commonly occupied, service ports.
     *
     * @param host host to knock on
     * @param ports ports to knock ({@link Knock#KNOCK_PORTS} on the production path)
     * @param timeout how long to keep knocking
     * @return future completing/cancellable when the scan finishes
     */
    protected CompletableFuture<@Nullable Void> startKnocker(InetAddress host, List<Integer> ports, Duration timeout) {
        return Knock.knocker(host, ports, timeout);
    }
}
