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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.unifi.api.UniFiSession;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.binding.BaseBridgeHandler;

/**
 * Public API exposed by the shared UniFi parent binding for child bindings (Network, Protect, Access) to attach
 * their things to a {@code unifi:controller} bridge and reuse a single authenticated session.
 * <p>
 * Analogous to {@code org.openhab.binding.mqtt.handler.AbstractBrokerHandler} in the MQTT family: child handlers
 * cast {@code getBridge().getHandler()} to this type and call {@link #getSessionAsync()} to obtain the shared
 * {@link UniFiSession}.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public abstract class UniFiControllerBridgeHandler extends BaseBridgeHandler {

    protected UniFiControllerBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    /**
     * @return a future that completes with the authenticated {@link UniFiSession} once the bridge has finished
     *         logging in. Child handlers should prefer this over {@link #getSession()} because it lets them wait
     *         cleanly for the initial login to complete.
     */
    public abstract CompletableFuture<UniFiSession> getSessionAsync();

    /**
     * @return the current authenticated session, or {@code null} if the bridge has not yet finished initializing
     *         or is offline. Prefer {@link #getSessionAsync()} unless you already know the bridge is online.
     */
    @Nullable
    public abstract UniFiSession getSession();

    /**
     * @return the Jetty {@link HttpClient} configured by the bridge (including the trust-all SSL context for
     *         self-signed console certificates). Child bindings should use this client for all REST and WebSocket
     *         requests against the console, not create their own.
     */
    public abstract HttpClient getHttpClient();

    /**
     * @return a scheduled executor that child bindings can use for their own polling loops, WebSocket keepalives,
     *         etc. Backed by the bridge handler's scheduler.
     */
    public abstract ScheduledExecutorService getScheduler();

    /**
     * @return the hostname or IP address of the UniFi console this bridge is connected to. Child bindings use
     *         this to build service-specific URLs (for example, {@code https://<host>:<port>/proxy/protect/api/...})
     *         and for identifying the console in log messages.
     */
    public abstract String getHost();

    /**
     * @return the TCP port of the UniFi console this bridge is connected to. Child bindings use this together
     *         with {@link #getHost()} to build service-specific URLs.
     */
    public abstract int getPort();
}
