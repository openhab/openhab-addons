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
package org.openhab.binding.unifi.internal.protect.api.priv.client;

import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.openhab.binding.unifi.internal.protect.UnifiProtectBindingConstants;
import org.openhab.binding.unifi.internal.protect.api.priv.dto.devices.Bridge;
import org.openhab.binding.unifi.internal.protect.api.priv.dto.devices.Camera;
import org.openhab.binding.unifi.internal.protect.api.priv.dto.devices.Chime;
import org.openhab.binding.unifi.internal.protect.api.priv.dto.devices.Doorlock;
import org.openhab.binding.unifi.internal.protect.api.priv.dto.devices.Light;
import org.openhab.binding.unifi.internal.protect.api.priv.dto.devices.Sensor;
import org.openhab.binding.unifi.internal.protect.api.priv.dto.devices.Viewer;
import org.openhab.binding.unifi.internal.protect.api.priv.dto.gson.JsonUtil;
import org.openhab.binding.unifi.internal.protect.api.priv.dto.system.Bootstrap;
import org.openhab.binding.unifi.internal.protect.api.priv.dto.system.Event;
import org.openhab.binding.unifi.internal.protect.api.priv.dto.system.Nvr;
import org.openhab.binding.unifi.internal.protect.api.priv.dto.system.User;
import org.openhab.binding.unifi.internal.protect.api.priv.dto.types.ModelType;
import org.openhab.binding.unifi.internal.protect.api.priv.exception.UniFiProtectException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * WebSocket client for UniFi Protect real-time updates
 * Connects to /proxy/protect/ws/updates for device and event updates
 *
 * @author Dan Cunningham - Initial contribution
 */
public class UniFiProtectPrivateWebSocket {

    private final Logger logger = LoggerFactory.getLogger(UniFiProtectPrivateWebSocket.class);

    private final String wsUrl;
    private final @Nullable String authCookie;
    private final Consumer<WebSocketUpdate> updateHandler;
    private final Runnable onReconnected;
    private final UniFiProtectPrivateClient client;
    private final WebSocketClient wsClient;

    private volatile @Nullable Session session;
    private volatile @Nullable CompletableFuture<Void> connectFuture;
    private volatile boolean shouldReconnect = true;
    // Protect does not replay what was missed, so a dropped session has to resynchronise.
    private volatile boolean resyncAfterConnect = false;
    private final AtomicInteger reconnectAttempts = new AtomicInteger(0);
    private static final int MAX_RECONNECT_ATTEMPTS = 10;
    private static final int INITIAL_RECONNECT_DELAY_MS = 1_000;
    private static final int MAX_RECONNECT_DELAY_MS = 60_000;
    private static final int CIRCUIT_BREAKER_DELAY_MS = 60_000;
    private static final int BOOTSTRAP_REFRESH_TIMEOUT_SECONDS = 30;

    /**
     * Create UniFi Protect WebSocket client
     *
     * @param wsUrl WebSocket URL
     * @param authCookie Authentication cookie
     * @param updateHandler Handler for WebSocket updates
     * @param client Reference to the main client
     * @param httpClient HTTP client to use (shared from main client)
     */
    public UniFiProtectPrivateWebSocket(String wsUrl, @Nullable String authCookie,
            Consumer<WebSocketUpdate> updateHandler, Runnable onReconnected, UniFiProtectPrivateClient client,
            HttpClient httpClient) {
        this.wsUrl = wsUrl;
        this.authCookie = authCookie;
        this.updateHandler = updateHandler;
        this.onReconnected = onReconnected;
        this.client = client;

        this.wsClient = new WebSocketClient(httpClient);
        // Prevent wsClient.stop() from stopping the shared HttpClient instance
        this.wsClient.unmanage(httpClient);
        this.wsClient.setMaxIdleTimeout(UnifiProtectBindingConstants.WEBSOCKET_IDLE_TIMEOUT_MS);

        try {
            wsClient.start();
        } catch (Exception e) {
            throw new UniFiProtectException("Failed to start WebSocket client", e);
        }
    }

    /**
     * Connect to WebSocket
     */
    public synchronized CompletableFuture<Void> connect() {
        CompletableFuture<Void> existingFuture = connectFuture;
        if (existingFuture != null && !existingFuture.isDone()) {
            return existingFuture;
        }

        CompletableFuture<Void> newFuture = new CompletableFuture<>();
        connectFuture = newFuture;

        try {
            URI uri = new URI(wsUrl);
            UniFiWebSocketAdapter adapter = new UniFiWebSocketAdapter();

            ClientUpgradeRequest request = new ClientUpgradeRequest();
            if (authCookie != null) {
                request.setHeader("Cookie", authCookie);
            }

            CompletableFuture.runAsync(() -> {
                try {
                    // Before the socket opens, so a live update cannot be overwritten by the
                    // older snapshot, and so a failure is retried by scheduleReconnect.
                    resyncBeforeConnect();

                    Future<Session> future = wsClient.connect(adapter, uri, request);
                    session = future.get(10, TimeUnit.SECONDS);
                    reconnectAttempts.set(0);
                    logger.debug("WebSocket connected");
                    newFuture.complete(null);
                } catch (Exception ex) {
                    logger.debug("WebSocket connection failed", ex);
                    newFuture.completeExceptionally(ex);
                    scheduleReconnect();
                }
            });

        } catch (Exception e) {
            logger.debug("Failed to connect WebSocket", e);
            newFuture.completeExceptionally(e);
        }

        return newFuture;
    }

    /**
     * Refresh the bootstrap and push it onto the Thing channels before the socket reopens. The
     * device sync is needed because a refresh only replaces the client's cached copy.
     */
    private void resyncBeforeConnect() throws ExecutionException, InterruptedException, TimeoutException {
        if (!resyncAfterConnect) {
            // First connect: the caller has just bootstrapped.
            return;
        }
        logger.debug("Refreshing bootstrap before reopening the WebSocket to pick up missed updates");
        client.refreshBootstrap().get(BOOTSTRAP_REFRESH_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        onReconnected.run();
        // Only now: a throw above leaves the flag set so the retry repeats the whole sequence.
        resyncAfterConnect = false;
    }

    /**
     * Disconnect WebSocket
     */
    public void disconnect() {
        shouldReconnect = false;

        Session localSession = session;
        if (localSession != null && localSession.isOpen()) {
            localSession.close();
        }
        session = null;

        try {
            wsClient.stop();
        } catch (Exception e) {
            logger.debug("Error stopping WebSocket client", e);
        }

        logger.debug("WebSocket disconnected");
    }

    /**
     * Schedule reconnection with exponential backoff.
     * After exhausting fast reconnect attempts, switches to periodic probes
     * every 5 minutes until the connection succeeds or disconnect() is called.
     */
    private void scheduleReconnect() {
        if (!shouldReconnect) {
            logger.debug("Reconnection disabled, not attempting to reconnect");
            return;
        }

        int attempts = reconnectAttempts.incrementAndGet();
        int delayMs;

        if (attempts <= MAX_RECONNECT_ATTEMPTS) {
            // Exponential backoff: 1s, 2s, 4s, 8s, ... capped at 60s
            delayMs = Math.min(INITIAL_RECONNECT_DELAY_MS * (1 << (attempts - 1)), MAX_RECONNECT_DELAY_MS);
        } else {
            // Circuit breaker: probe every 5 minutes
            delayMs = CIRCUIT_BREAKER_DELAY_MS;
        }

        logger.debug("Scheduling WebSocket reconnection attempt {} in {}ms", attempts, delayMs);

        CompletableFuture.delayedExecutor(delayMs, TimeUnit.MILLISECONDS).execute(() -> {
            if (shouldReconnect) {
                logger.debug("Attempting WebSocket reconnection (attempt {})", reconnectAttempts.get());
                connect().exceptionally(ex -> {
                    logger.debug("Reconnection attempt {} failed", reconnectAttempts.get(), ex);
                    return null;
                });
            }
        });
    }

    /**
     * Check if connected
     */
    public boolean isConnected() {
        Session localSession = session;
        return localSession != null && localSession.isOpen();
    }

    /**
     * WebSocket update message
     */
    public static class WebSocketUpdate {
        public String action;
        public ModelType modelType;
        public String id;
        public String newUpdateId;
        public JsonObject data;

        public WebSocketUpdate(String action, ModelType modelType, String id, String newUpdateId, JsonObject data) {
            this.action = action;
            this.modelType = modelType;
            this.id = id;
            this.newUpdateId = newUpdateId;
            this.data = data;
        }

        @Override
        public String toString() {
            return String.format("WebSocketUpdate{action='%s', modelType=%s, id='%s'}", action, modelType, id);
        }
    }

    /**
     * WebSocket adapter to handle messages
     */
    private class UniFiWebSocketAdapter extends WebSocketAdapter {

        @Override
        public void onWebSocketConnect(Session session) {
            super.onWebSocketConnect(session);
            logger.debug("WebSocket session connected");
        }

        @Override
        public void onWebSocketBinary(byte[] payload, int offset, int len) {
            try {
                ByteBuffer buffer = ByteBuffer.wrap(payload, offset, len);
                parseWebSocketPacket(buffer);
            } catch (Exception e) {
                logger.debug("Error parsing WebSocket binary message", e);
            }
        }

        @Override
        public void onWebSocketText(String message) {
            try {
                // Sometimes we might get text messages too
                logger.trace("WebSocket Text Message: {}", message);
                JsonObject json = JsonParser.parseString(message).getAsJsonObject();
                processUpdate(json);
            } catch (Exception e) {
                logger.debug("Error parsing WebSocket text message", e);
            }
        }

        @Override
        public void onWebSocketError(Throwable cause) {
            logger.debug("WebSocket error", cause);
        }

        @Override
        public void onWebSocketClose(int statusCode, String reason) {
            logger.debug("WebSocket closed: {} - {}", statusCode, reason);
            if (shouldReconnect) {
                resyncAfterConnect = true;
                scheduleReconnect();
            }
        }

        /**
         * Parse binary WebSocket packet from UniFi Protect
         * The format is: [action frame][data frame]
         * Each frame has a header (8 bytes) + JSON payload
         */
        private void parseWebSocketPacket(ByteBuffer buffer) {
            try {
                // Parse action frame (first frame)
                if (buffer.remaining() < 8) {
                    logger.debug("Packet too small for header");
                    return;
                }

                // Read header (8 bytes)
                int packetType = buffer.get() & 0xFF;
                int payloadFormat = buffer.get() & 0xFF;
                int deflated = buffer.get() & 0xFF;
                buffer.get(); // Skip unknown byte
                int payloadSize = buffer.getInt();

                logger.trace("Action frame header: type={}, format={}, deflated={}, size={}", packetType, payloadFormat,
                        deflated, payloadSize);

                // Read action JSON
                if (buffer.remaining() < payloadSize) {
                    logger.debug("Not enough data for action payload");
                    return;
                }

                byte[] actionBytes = new byte[payloadSize];
                buffer.get(actionBytes);
                String actionJson = new String(actionBytes, StandardCharsets.UTF_8);
                JsonObject actionData = JsonParser.parseString(actionJson).getAsJsonObject();

                logger.trace("WebSocket Action Frame JSON: {}", actionJson);

                // Parse data frame (second frame) if present
                JsonObject dataObject = null;
                if (buffer.remaining() >= 8) {
                    // Read data frame header
                    buffer.get(); // packet type
                    buffer.get(); // payload format
                    buffer.get(); // deflated
                    buffer.get(); // unknown
                    int dataPayloadSize = buffer.getInt();

                    if (buffer.remaining() >= dataPayloadSize && dataPayloadSize > 0) {
                        byte[] dataBytes = new byte[dataPayloadSize];
                        buffer.get(dataBytes);
                        String dataJson = new String(dataBytes, StandardCharsets.UTF_8);
                        dataObject = JsonParser.parseString(dataJson).getAsJsonObject();

                        logger.trace("WebSocket Data Frame JSON: {}", dataJson);
                    }
                }

                // Process the update
                processUpdate(actionData, dataObject);

            } catch (Exception e) {
                logger.debug("Error parsing WebSocket packet", e);
            }
        }

        /**
         * Process update from action and data JSON
         */
        private void processUpdate(JsonObject actionData, JsonObject dataObject) {
            try {
                String action = actionData.has("action") ? actionData.get("action").getAsString() : null;
                String modelKey = actionData.has("modelKey") ? actionData.get("modelKey").getAsString() : null;
                String id = actionData.has("id") ? actionData.get("id").getAsString() : null;
                String newUpdateId = actionData.has("newUpdateId") && !actionData.get("newUpdateId").isJsonNull()
                        ? actionData.get("newUpdateId").getAsString()
                        : null;

                ModelType modelType = ModelType.fromString(modelKey);

                logger.debug("WebSocket update: action={}, model={}, id={}", action, modelType, id);

                if (logger.isTraceEnabled()) {
                    logger.trace("Decoded WebSocket Message:");
                    logger.trace("  Action: {}", action);
                    logger.trace("  ModelKey: {}", modelKey);
                    logger.trace("  ModelType: {}", modelType);
                    logger.trace("  ID: {}", id);
                    logger.trace("  NewUpdateId: {}", newUpdateId);
                    if (dataObject != null) {
                        logger.trace("  Data fields: {}", dataObject.keySet());
                    }
                }

                // Update bootstrap's lastUpdateId if present
                Bootstrap bootstrap = client.getCachedBootstrap();
                if (newUpdateId != null && bootstrap != null) {
                    bootstrap.lastUpdateId = newUpdateId;
                }

                WebSocketUpdate update = new WebSocketUpdate(action, modelType, id, newUpdateId, dataObject);

                // Apply incremental updates to cached bootstrap
                applyIncrementalUpdate(action, modelType, id, dataObject);

                // Call handler after applying update
                if (updateHandler != null) {
                    updateHandler.accept(update);
                }

            } catch (Exception e) {
                logger.debug("Error processing WebSocket update", e);
            }
        }

        /**
         * Process simple JSON update (text message)
         */
        private void processUpdate(JsonObject json) {
            processUpdate(json, json.has("data") ? json.getAsJsonObject("data") : null);
        }

        /**
         * Apply incremental update to cached bootstrap
         */
        private void applyIncrementalUpdate(String action, ModelType modelType, String id, JsonObject data) {
            Bootstrap bootstrap = client.getCachedBootstrap();
            if (bootstrap == null) {
                logger.debug("No cached bootstrap to update");
                return;
            }

            if (data == null || id == null || modelType == null) {
                logger.debug("Incomplete update data, skipping incremental update");
                return;
            }

            try {
                switch (action) {
                    case "add":
                    case "update":
                        updateBootstrapObject(bootstrap, modelType, id, data);
                        break;
                    case "remove":
                        removeBootstrapObject(bootstrap, modelType, id);
                        break;
                    default:
                        logger.debug("Unknown action: {}", action);
                }
            } catch (Exception e) {
                logger.debug("Error applying incremental update: action={}, model={}, id={}", action, modelType, id, e);
            }
        }

        /**
         * Update or add an object in bootstrap. Update payloads are partial deltas, so they are
         * merged field by field over the cached object.
         */
        private void updateBootstrapObject(Bootstrap bootstrap, ModelType modelType, String id, JsonObject data) {
            switch (modelType) {
                case CAMERA:
                    if (bootstrap.cameras != null) {
                        bootstrap.cameras.put(id, mergeIntoCached(bootstrap.cameras.get(id), data, Camera.class));
                        logger.debug("Updated camera: {}", id);
                    }
                    break;
                case LIGHT:
                    if (bootstrap.lights != null) {
                        bootstrap.lights.put(id, mergeIntoCached(bootstrap.lights.get(id), data, Light.class));
                        logger.debug("Updated light: {}", id);
                    }
                    break;
                case SENSOR:
                    if (bootstrap.sensors != null) {
                        bootstrap.sensors.put(id, mergeIntoCached(bootstrap.sensors.get(id), data, Sensor.class));
                        logger.debug("Updated sensor: {}", id);
                    }
                    break;
                case DOORLOCK:
                    if (bootstrap.doorlocks != null) {
                        bootstrap.doorlocks.put(id, mergeIntoCached(bootstrap.doorlocks.get(id), data, Doorlock.class));
                        logger.debug("Updated doorlock: {}", id);
                    }
                    break;
                case CHIME:
                    if (bootstrap.chimes != null) {
                        bootstrap.chimes.put(id, mergeIntoCached(bootstrap.chimes.get(id), data, Chime.class));
                        logger.debug("Updated chime: {}", id);
                    }
                    break;
                case VIEWER:
                    if (bootstrap.viewers != null) {
                        bootstrap.viewers.put(id, mergeIntoCached(bootstrap.viewers.get(id), data, Viewer.class));
                        logger.debug("Updated viewer: {}", id);
                    }
                    break;
                case BRIDGE:
                    if (bootstrap.bridges != null) {
                        bootstrap.bridges.put(id, mergeIntoCached(bootstrap.bridges.get(id), data, Bridge.class));
                        logger.debug("Updated bridge: {}", id);
                    }
                    break;
                case NVR:
                    bootstrap.nvr = mergeIntoCached(bootstrap.nvr, data, Nvr.class);
                    logger.debug("Updated NVR");
                    break;
                case USER:
                    if (bootstrap.users != null) {
                        bootstrap.users.put(id, mergeIntoCached(bootstrap.users.get(id), data, User.class));
                        logger.debug("Updated user: {}", id);
                    }
                    break;
                case EVENT:
                    if (bootstrap.events != null) {
                        bootstrap.events.put(id, mergeIntoCached(bootstrap.events.get(id), data, Event.class));
                        logger.debug("Updated event: {}", id);
                    }
                    break;
                default:
                    logger.debug("Unhandled model type for update: {}", modelType);
            }
        }

        private <T> T mergeIntoCached(@Nullable T cached, JsonObject delta, Class<T> type) {
            var gson = JsonUtil.getGson();
            if (cached == null) {
                return gson.fromJson(delta, type);
            }
            JsonObject merged = gson.toJsonTree(cached).getAsJsonObject();
            mergeJson(merged, delta);
            return gson.fromJson(merged, type);
        }

        /**
         * Recursive merge: deltas can patch a single member of a nested settings object, so
         * nested objects merge member-wise while scalars, arrays and nulls replace.
         */
        private void mergeJson(JsonObject target, JsonObject delta) {
            for (var entry : delta.entrySet()) {
                var existing = target.get(entry.getKey());
                if (entry.getValue() instanceof JsonObject deltaObj && existing instanceof JsonObject existingObj) {
                    mergeJson(existingObj, deltaObj);
                } else {
                    target.add(entry.getKey(), entry.getValue());
                }
            }
        }

        /**
         * Remove an object from bootstrap
         */
        private void removeBootstrapObject(Bootstrap bootstrap, ModelType modelType, String id) {
            switch (modelType) {
                case CAMERA:
                    if (bootstrap.cameras != null) {
                        bootstrap.cameras.remove(id);
                        logger.debug("Removed camera: {}", id);
                    }
                    break;
                case LIGHT:
                    if (bootstrap.lights != null) {
                        bootstrap.lights.remove(id);
                        logger.debug("Removed light: {}", id);
                    }
                    break;
                case SENSOR:
                    if (bootstrap.sensors != null) {
                        bootstrap.sensors.remove(id);
                        logger.debug("Removed sensor: {}", id);
                    }
                    break;
                case DOORLOCK:
                    if (bootstrap.doorlocks != null) {
                        bootstrap.doorlocks.remove(id);
                        logger.debug("Removed doorlock: {}", id);
                    }
                    break;
                case CHIME:
                    if (bootstrap.chimes != null) {
                        bootstrap.chimes.remove(id);
                        logger.debug("Removed chime: {}", id);
                    }
                    break;
                case VIEWER:
                    if (bootstrap.viewers != null) {
                        bootstrap.viewers.remove(id);
                        logger.debug("Removed viewer: {}", id);
                    }
                    break;
                case BRIDGE:
                    if (bootstrap.bridges != null) {
                        bootstrap.bridges.remove(id);
                        logger.debug("Removed bridge: {}", id);
                    }
                    break;
                case USER:
                    if (bootstrap.users != null) {
                        bootstrap.users.remove(id);
                        logger.debug("Removed user: {}", id);
                    }
                    break;
                case EVENT:
                    if (bootstrap.events != null) {
                        bootstrap.events.remove(id);
                        logger.debug("Removed event: {}", id);
                    }
                    break;
                default:
                    logger.debug("Unhandled model type for removal: {}", modelType);
            }
        }
    }
}
