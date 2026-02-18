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
package org.openhab.binding.evcc.internal.api;

import java.net.URI;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * The {@link EvccWebSocketClient} encapsulates the EVCC WebSocket connection,
 * including connection lifecycle, reconnect handling, watchdog supervision,
 * and message parsing. It does not maintain state itself; instead, it forwards
 * full and partial updates to the bridge via callback functions.
 *
 * This class forms the transport layer of the EVCC binding and is completely
 * decoupled from openHAB-specific logic or Thing lifecycle management.
 *
 * @author Marcel Goerentz - Initial contribution
 */

@NonNullByDefault
public class EvccWebSocketClient {

    private final Logger logger = LoggerFactory.getLogger(EvccWebSocketClient.class);

    private final WebSocketClient client = new WebSocketClient();
    private final ScheduledExecutorService scheduler;
    private final String wsUrl;

    private final java.util.function.Consumer<JsonObject> onFullState;
    private final java.util.function.BiConsumer<String, JsonElement> onPartialUpdate;
    private final Runnable onConnected;
    private final Runnable onDisconnected;

    private @Nullable Session session;
    private @Nullable ScheduledFuture<?> watchdogJob;
    private @Nullable ScheduledFuture<?> reconnectJob;

    public EvccWebSocketClient(String wsUrl, ScheduledExecutorService scheduler,
            java.util.function.Consumer<JsonObject> onFullState,
            java.util.function.BiConsumer<String, JsonElement> onPartialUpdate, Runnable onConnected,
            Runnable onDisconnected) {

        this.wsUrl = wsUrl;
        this.scheduler = scheduler;
        this.onFullState = onFullState;
        this.onPartialUpdate = onPartialUpdate;
        this.onConnected = onConnected;
        this.onDisconnected = onDisconnected;
    }

    // --------------------------------------------------------------------
    // Lifecycle
    // --------------------------------------------------------------------

    public void start() {
        try {
            client.getPolicy().setIdleTimeout(0); // Jetty 9
            client.getPolicy().setMaxTextMessageSize(512 * 1024);
            client.getPolicy().setMaxTextMessageBufferSize(512 * 1024);
            client.start();
            connect();
        } catch (Exception e) {
            logger.warn("Failed to start EVCC WebSocket client: {}", e.getMessage());
            scheduleReconnect();
        }
    }

    public void stop() {
        stopWatchdog();
        closeSession();
        try {
            client.stop();
        } catch (Exception ignored) {
        }
    }

    // --------------------------------------------------------------------
    // Connection Handling
    // --------------------------------------------------------------------

    private void connect() {
        logger.info("Connecting EVCC WebSocket to {}", wsUrl);

        try {
            reconnectJob = null;
            client.start();
            client.connect(new EvccWebSocketAdapter(this), URI.create(wsUrl));
        } catch (Exception e) {
            logger.warn("EVCC WebSocket connect failed: {}", e.getMessage());
            scheduleReconnect();
        }
    }

    private void scheduleReconnect() {
        if (reconnectJob == null || reconnectJob.isDone()) {
            reconnectJob = scheduler.schedule(this::connect, 10, TimeUnit.SECONDS);
            logger.info("Scheduled EVCC WebSocket reconnect in 10 seconds");
        }
    }

    private void startWatchdog() {
        stopWatchdog();
        watchdogJob = scheduler.schedule(() -> {
            logger.warn("EVCC WebSocket watchdog timeout — reconnecting");
            closeSession();
            scheduleReconnect();
        }, 30, TimeUnit.SECONDS);
    }

    private void stopWatchdog() {
        Optional.ofNullable(watchdogJob).ifPresent(wj -> {
            wj.cancel(false);
            watchdogJob = null;
        });
    }

    private void closeSession() {
        Session s = session;
        if (s != null && s.isOpen()) {
            try {
                s.close();
            } catch (Exception ignored) {
            }
        }
        session = null;
    }

    // --------------------------------------------------------------------
    // Callbacks from Adapter
    // --------------------------------------------------------------------

    void handleConnected(Session sess) {
        session = sess;
        startWatchdog();
        onConnected.run();
    }

    void handleClosed(int code, String reason) {
        stopWatchdog();
        session = null;
        onDisconnected.run();
        scheduleReconnect();
    }

    void handleError(Throwable cause) {
        logger.warn("EVCC WebSocket error: {}", cause.getMessage());
        scheduleReconnect();
    }

    void handleMessage(@Nullable JsonObject obj) {
        logger.trace("Message received: {}", obj);
        startWatchdog();
        if (obj == null) {
            return;
        }

        if (obj.entrySet().isEmpty())
            return;

        if (isFullState(obj)) {
            onFullState.accept(obj);
        } else {
            obj.entrySet().forEach(e -> onPartialUpdate.accept(e.getKey(), e.getValue()));
        }
    }

    // --------------------------------------------------------------------
    // State Detection (Bridge übernimmt das Merging)
    // --------------------------------------------------------------------

    private boolean isFullState(JsonObject obj) {
        return obj.entrySet().size() > 1
                && obj.entrySet().stream().anyMatch(e -> e.getValue().isJsonObject() || e.getValue().isJsonArray());
    }
}
