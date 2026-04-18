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
package org.openhab.binding.unifi.handler;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.openhab.binding.unifi.api.UniFiException;
import org.openhab.binding.unifi.api.UniFiException.AuthState;
import org.openhab.binding.unifi.api.UniFiSession;
import org.openhab.binding.unifi.api.UniFiSessionRegistry;
import org.openhab.binding.unifi.internal.UniFiBindingConstants;
import org.openhab.binding.unifi.internal.UniFiControllerConfiguration;
import org.openhab.binding.unifi.internal.api.UniFiAuthenticator;
import org.openhab.binding.unifi.internal.api.UniFiRequestThrottler;
import org.openhab.binding.unifi.internal.api.UniFiSessionImpl;
import org.openhab.binding.unifi.internal.discovery.UniFiAppDiscoveryService;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for the shared {@code unifi:controller} bridge. Owns a per-bridge Jetty {@link HttpClient} (configured
 * to accept self-signed console certificates), logs in via {@link UniFiAuthenticator}, and publishes a shared
 * {@link UniFiSession} that the Network, Protect, and Access child bindings consume by casting
 * {@code getBridge().getHandler()} to this type and calling {@link #getSessionAsync()}.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class UniFiControllerBridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(UniFiControllerBridgeHandler.class);

    private static final int MAX_RECONNECT_DELAY_SECONDS = 300;
    private static final int THROTTLED_INITIAL_DELAY_SECONDS = 60;
    private static final int THROTTLED_MAX_DELAY_SECONDS = 1800;

    private final HttpClientFactory httpClientFactory;

    private @Nullable HttpClient httpClient;
    private @Nullable UniFiSession session;
    private @Nullable UniFiSessionImpl sessionImpl;
    private @Nullable UniFiAuthenticator authenticator;
    private CompletableFuture<UniFiSession> sessionFuture = new CompletableFuture<>();
    private UniFiControllerConfiguration config = new UniFiControllerConfiguration();
    private @Nullable ScheduledFuture<?> reconnectTask;
    private volatile boolean shuttingDown = false;
    private int reconnectAttempt = 0;
    private int throttledReconnectAttempt = 0;

    public UniFiControllerBridgeHandler(Bridge bridge, HttpClientFactory httpClientFactory) {
        super(bridge);
        this.httpClientFactory = httpClientFactory;
    }

    @Override
    public void initialize() {
        config = getConfigAs(UniFiControllerConfiguration.class);
        logger.debug("Initializing UniFi controller bridge for host {}", config.host);

        shuttingDown = false;
        reconnectAttempt = 0;
        throttledReconnectAttempt = 0;

        if (config.host.isBlank() || config.username.isBlank() || config.password.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Host, username, and password are required");
            sessionFuture.completeExceptionally(new UniFiException("Missing configuration"));
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
            sessionFuture.completeExceptionally(new UniFiException("Failed to start HTTP client", e));
            return;
        }
        httpClient = client;

        String baseUrl = "https://" + config.host + ":" + config.port;
        UniFiAuthenticator auth = new UniFiAuthenticator(client, scheduler, baseUrl, config.username, config.password,
                config.unifios, true);
        authenticator = auth;
        UniFiRequestThrottler throttler = new UniFiRequestThrottler();
        sessionImpl = new UniFiSessionImpl(baseUrl, auth, throttler);

        updateStatus(ThingStatus.UNKNOWN);
        attemptConnect();
    }

    private void attemptConnect() {
        if (shuttingDown) {
            return;
        }
        UniFiAuthenticator auth = authenticator;
        UniFiSessionImpl impl = sessionImpl;
        if (auth == null || impl == null) {
            return;
        }
        auth.authenticate().whenComplete((result, error) -> {
            if (shuttingDown) {
                return;
            }
            if (error == null) {
                reconnectAttempt = 0;
                throttledReconnectAttempt = 0;
                session = impl;
                UniFiSessionRegistry.getInstance().register(config.host, config.username, impl);
                if (!sessionFuture.isDone()) {
                    sessionFuture.complete(impl);
                }
                updateStatus(ThingStatus.ONLINE);
            } else {
                logger.debug("Authentication to {} failed: {}", config.host, error.getMessage());
                handleAuthFailure(error);
            }
        });
    }

    private void handleAuthFailure(Throwable error) {
        Throwable cause = error;
        while (cause instanceof CompletionException) {
            Throwable inner = cause.getCause();
            if (inner == null) {
                break;
            }
            cause = inner;
        }
        AuthState authState = cause instanceof UniFiException ue ? ue.getAuthState() : AuthState.OK;
        switch (authState) {
            case REJECTED:
                if (!sessionFuture.isDone()) {
                    sessionFuture.completeExceptionally(cause);
                }
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "@text/offline.conf-error-auth-rejected");
                return;
            case THROTTLED:
                scheduleReconnect(true);
                return;
            default:
                scheduleReconnect(false);
                return;
        }
    }

    private synchronized void scheduleReconnect(boolean throttled) {
        if (shuttingDown) {
            return;
        }
        ScheduledFuture<?> existing = reconnectTask;
        if (existing != null && !existing.isDone()) {
            // Throttled supersedes a pending fast reconnect; otherwise let the existing one run.
            if (throttled) {
                existing.cancel(false);
            } else {
                return;
            }
        }
        int delay;
        if (throttled) {
            delay = Math.min((int) Math.pow(2, throttledReconnectAttempt) * THROTTLED_INITIAL_DELAY_SECONDS,
                    THROTTLED_MAX_DELAY_SECONDS);
            throttledReconnectAttempt++;
            logger.debug("Scheduling reconnect in {} seconds (throttled, attempt {})", delay,
                    throttledReconnectAttempt);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "@text/offline.login-throttled");
        } else {
            delay = Math.min((int) Math.pow(2, reconnectAttempt) * 5, MAX_RECONNECT_DELAY_SECONDS);
            reconnectAttempt++;
            logger.debug("Scheduling reconnect in {} seconds (attempt {})", delay, reconnectAttempt);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/offline.comm-error-retrying");
        }
        reconnectTask = scheduler.schedule(this::attemptConnect, delay, TimeUnit.SECONDS);
    }

    @Override
    public void dispose() {
        logger.debug("Disposing UniFi controller bridge for host {}", config.host);
        shuttingDown = true;
        ScheduledFuture<?> task = reconnectTask;
        if (task != null) {
            task.cancel(false);
            reconnectTask = null;
        }
        if (!config.host.isBlank() && !config.username.isBlank()) {
            UniFiSessionRegistry.getInstance().unregister(config.host, config.username);
        }
        UniFiAuthenticator auth = authenticator;
        if (auth != null) {
            auth.clearAuth();
            authenticator = null;
        }
        sessionImpl = null;
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
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return List.of(UniFiAppDiscoveryService.class);
    }

    /**
     * @return a future that completes with the authenticated {@link UniFiSession} once the bridge has finished
     *         logging in. Child handlers should prefer this over {@link #getSession()} because it lets them wait
     *         cleanly for the initial login to complete.
     */
    public CompletableFuture<UniFiSession> getSessionAsync() {
        return sessionFuture;
    }

    /**
     * @return the current authenticated session, or {@code null} if the bridge has not yet finished initializing
     *         or is offline. Prefer {@link #getSessionAsync()} unless you already know the bridge is online.
     */
    public @Nullable UniFiSession getSession() {
        return session;
    }

    /**
     * @return the Jetty {@link HttpClient} configured by the bridge (including the trust-all SSL context for
     *         self-signed console certificates). Child bindings should use this client for all REST and WebSocket
     *         requests against the console, not create their own.
     */
    public HttpClient getHttpClient() {
        HttpClient client = httpClient;
        if (client == null) {
            throw new IllegalStateException("HTTP client requested before bridge initialization");
        }
        return client;
    }

    /**
     * @return a scheduled executor that child bindings can use for their own polling loops, WebSocket keepalives,
     *         etc. Backed by the bridge handler's scheduler.
     */
    public ScheduledExecutorService getScheduler() {
        return scheduler;
    }

    /**
     * @return the hostname or IP address of the UniFi console this bridge is connected to.
     */
    public String getHost() {
        return config.host;
    }

    /**
     * @return the TCP port of the UniFi console this bridge is connected to.
     */
    public int getPort() {
        return config.port;
    }
}
