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
package org.openhab.binding.unifi.internal.handler;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.openhab.binding.unifi.api.UniFiSession;
import org.openhab.binding.unifi.api.UniFiSessionRegistry;
import org.openhab.binding.unifi.handler.UniFiControllerBridgeHandler;
import org.openhab.binding.unifi.internal.UniFiBindingConstants;
import org.openhab.binding.unifi.internal.UniFiControllerConfiguration;
import org.openhab.binding.unifi.internal.api.UniFiAuthenticator;
import org.openhab.binding.unifi.internal.api.UniFiRequestThrottler;
import org.openhab.binding.unifi.internal.api.UniFiSessionImpl;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Concrete implementation of {@link UniFiControllerBridgeHandler}. Owns a per-bridge Jetty {@link HttpClient}
 * (configured to accept self-signed console certificates), logs in via {@link UniFiAuthenticator}, and publishes
 * a shared {@link UniFiSession} that Network, Protect, and Access child bindings consume.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class UniFiControllerBridgeHandlerImpl extends UniFiControllerBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(UniFiControllerBridgeHandlerImpl.class);

    private final HttpClientFactory httpClientFactory;

    private @Nullable HttpClient httpClient;
    private @Nullable UniFiSession session;
    private @Nullable UniFiAuthenticator authenticator;
    private CompletableFuture<UniFiSession> sessionFuture = new CompletableFuture<>();
    private UniFiControllerConfiguration config = new UniFiControllerConfiguration();

    public UniFiControllerBridgeHandlerImpl(Bridge bridge, HttpClientFactory httpClientFactory) {
        super(bridge);
        this.httpClientFactory = httpClientFactory;
    }

    @Override
    public void initialize() {
        config = getConfigAs(UniFiControllerConfiguration.class);
        logger.debug("Initializing UniFi controller bridge for host {}", config.host);

        if (config.host.isBlank() || config.username.isBlank() || config.password.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Host, username, and password are required");
            return;
        }

        HttpClient client = httpClientFactory.createHttpClient(UniFiBindingConstants.BINDING_ID,
                new SslContextFactory.Client(true));
        try {
            client.start();
        } catch (Exception e) {
            logger.debug("Failed to start HTTP client for {}: {}", thing.getUID(), e.getMessage(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Failed to start HTTP client: " + e.getMessage());
            return;
        }
        httpClient = client;

        String baseUrl = "https://" + config.host + ":" + config.port;
        UniFiAuthenticator auth = new UniFiAuthenticator(client, scheduler, baseUrl, config.username, config.password,
                config.unifios, true);
        authenticator = auth;
        UniFiRequestThrottler throttler = new UniFiRequestThrottler();
        UniFiSessionImpl sessionImpl = new UniFiSessionImpl(baseUrl, auth, throttler);

        updateStatus(ThingStatus.UNKNOWN);

        auth.authenticate().whenComplete((result, error) -> {
            if (error == null) {
                session = sessionImpl;
                UniFiSessionRegistry.getInstance().register(config.host, config.username, sessionImpl);
                sessionFuture.complete(sessionImpl);
                updateStatus(ThingStatus.ONLINE);
            } else {
                logger.debug("Initial authentication to {} failed: {}", config.host, error.getMessage());
                sessionFuture.completeExceptionally(error);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Authentication failed: " + error.getMessage());
            }
        });
    }

    @Override
    public void dispose() {
        logger.debug("Disposing UniFi controller bridge for host {}", config.host);
        if (!config.host.isBlank() && !config.username.isBlank()) {
            UniFiSessionRegistry.getInstance().unregister(config.host, config.username);
        }
        UniFiAuthenticator auth = authenticator;
        if (auth != null) {
            auth.clearAuth();
            authenticator = null;
        }
        HttpClient client = httpClient;
        if (client != null) {
            try {
                client.stop();
            } catch (Exception e) {
                logger.debug("Error stopping HTTP client for {}: {}", thing.getUID(), e.getMessage());
            }
            httpClient = null;
        }
        session = null;
        if (!sessionFuture.isDone()) {
            sessionFuture.completeExceptionally(new IllegalStateException("Bridge disposed"));
        }
        sessionFuture = new CompletableFuture<>();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // The controller bridge has no channels; nothing to handle.
    }

    @Override
    public CompletableFuture<UniFiSession> getSessionAsync() {
        return sessionFuture;
    }

    @Override
    public @Nullable UniFiSession getSession() {
        return session;
    }

    @Override
    public HttpClient getHttpClient() {
        HttpClient client = httpClient;
        if (client == null) {
            throw new IllegalStateException("HTTP client requested before bridge initialization");
        }
        return client;
    }

    @Override
    public ScheduledExecutorService getScheduler() {
        return scheduler;
    }

    @Override
    public String getHost() {
        return config.host;
    }

    @Override
    public int getPort() {
        return config.port;
    }
}
