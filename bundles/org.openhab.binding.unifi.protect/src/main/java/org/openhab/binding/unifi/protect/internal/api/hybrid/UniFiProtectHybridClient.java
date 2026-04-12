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
package org.openhab.binding.unifi.protect.internal.api.hybrid;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.unifi.api.UniFiSession;
import org.openhab.binding.unifi.protect.internal.api.priv.client.UniFiProtectPrivateClient;
import org.openhab.binding.unifi.protect.internal.api.pub.client.UniFiProtectPublicClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * Unified client that uses both Public Integration API and Private API.
 *
 * This client provides a hybrid approach:
 * - Public API: Official, stable, token-based authentication for device data
 * - Private API: Full-featured, cookie-based authentication for advanced features
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class UniFiProtectHybridClient implements Closeable {
    private final Logger logger = LoggerFactory.getLogger(UniFiProtectHybridClient.class);

    private final UniFiProtectPublicClient publicClient;
    private final UniFiProtectPrivateClient privateClient;

    /**
     * Create a hybrid client with both public and private API support.
     *
     * @param httpClient The HTTP client (supplied by the parent {@code unifi:controller} bridge)
     * @param gson Gson instance for JSON serialization
     * @param apiToken The API token for the public Integration API
     * @param executorService Scheduled executor for background tasks (supplied by the parent bridge)
     * @param host Hostname for API (read from the parent bridge configuration)
     * @param port Port for API (read from the parent bridge configuration, typically 443)
     * @param session The authenticated cookie/CSRF session owned by the parent bridge, used for all
     *            private-API (non-Integration-API) requests
     */
    public UniFiProtectHybridClient(HttpClient httpClient, Gson gson, String apiToken,
            ScheduledExecutorService executorService, String host, int port, UniFiSession session) {
        this.publicClient = new UniFiProtectPublicClient(httpClient, host, port, gson, apiToken, executorService);
        String baseUrl = "https://" + host + ":" + port;
        this.privateClient = new UniFiProtectPrivateClient(httpClient, executorService, baseUrl, session);
        logger.debug("Hybrid client initialized with both public and private API support");
    }

    @Override
    public void close() throws IOException {
        logger.debug("Closing hybrid client");
        try {
            publicClient.close();
        } finally {
            privateClient.close();
        }
    }

    /**
     * Access the public UniFi Protect client.
     */
    public UniFiProtectPublicClient getPublicClient() {
        return publicClient;
    }

    /**
     * Access the private UniFi Protect client.
     */
    public UniFiProtectPrivateClient getPrivateClient() {
        return privateClient;
    }
}
