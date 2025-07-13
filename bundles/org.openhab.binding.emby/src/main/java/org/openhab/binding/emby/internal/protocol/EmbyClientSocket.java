/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.emby.internal.protocol;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.openhab.binding.emby.internal.model.EmbyPlayStateModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

/**
 * EmbyClientSocket implements the low level communication to Emby through
 * websocket. Usually this communication is done through port 9090
 *
 * @author Zachary Christiansen - Initial contribution
 *
 */
@NonNullByDefault
public class EmbyClientSocket {

    private static final int REQUEST_TIMEOUT_MS = 10000;
    private final Logger logger = LoggerFactory.getLogger(EmbyClientSocket.class);
    private final ScheduledExecutorService scheduler;
    private final Gson mapper = new Gson();
    private final EmbyClientSocketEventListener eventHandler;
    private final WebSocketClient client;
    private final ClientUpgradeRequest request = new ClientUpgradeRequest();
    private final EmbyWebSocketListener socket = new EmbyWebSocketListener();

    private @Nullable URI uri;
    private @Nullable Session session;

    private volatile boolean shouldReconnect = true;
    private int reconnectAttempts = 0;

    public EmbyClientSocket(EmbyClientSocketEventListener handler, @Nullable URI setUri,
            ScheduledExecutorService setScheduler, WebSocketClient sharedWebSocketClient) {
        this.eventHandler = handler;
        this.uri = setUri;
        this.scheduler = setScheduler;
        this.client = sharedWebSocketClient;
    }

    public synchronized void open() throws Exception {
        if (isConnected()) {
            logger.debug("already open");
            return;
        }

        Future<Session> future = requireNonNull(client.connect(socket, uri, request),
                "WebSocketClient.connect returned null Future<Session>");
        Session wsSession = requireNonNull(future.get(REQUEST_TIMEOUT_MS, TimeUnit.MILLISECONDS),
                "Future<Session>.get returned null Session");
        requireNonNull(wsSession, "WebSocketClient.connect returned null Session");
        this.session = wsSession;
        logger.debug("Connected to Emby");
        // Reset retry counters
        reconnectAttempts = 0;
        // Fire the connection‐opened event
        scheduler.submit(() -> eventHandler.onConnectionOpened());
    }

    public void close() {
        shouldReconnect = false;
        reconnectAttempts = 0;
        Session s = session;
        if (s != null) {
            try {
                s.close();
            } catch (Exception e) {
                logger.debug("Exception during closing the websocket: {}", e.getMessage(), e);
            }
            session = null;
        }
    }

    public boolean isConnected() {
        Session s = session;
        return s != null && s.isOpen();
    }

    public synchronized void attemptReconnect() {
        if (!shouldReconnect) {
            return;
        }
        reconnectAttempts++;
        long delay = Math.min(60_000, (1 << Math.min(reconnectAttempts, 6)) * 1000L);
        logger.debug("Scheduling reconnect #{} in {}ms", reconnectAttempts, delay);
        scheduler.schedule(() -> {
            try {
                open();
            } catch (Exception e) {
                logger.debug("Reconnect attempt #{} failed: {}", reconnectAttempts, e.getMessage());
                attemptReconnect();
            }
        }, delay, TimeUnit.MILLISECONDS);
    }

    public void sendCommand(String methodName, String dataParams) {
        JsonObject payload = new JsonObject();
        payload.addProperty("MessageType", methodName);
        payload.addProperty("Data", dataParams);
        scheduler.submit(() -> {
            try {
                Session s = this.session;
                if (s == null || !s.isOpen()) {
                    logger.debug("Cannot send {}, session not open", methodName);
                    return;
                }
                s.getRemote().sendString(mapper.toJson(payload));
                logger.debug("Sent command: {}", payload);
            } catch (IOException e) {
                logger.error("Failed sending {}: {}", methodName, e.getMessage(), e);
            }
        });
    }

    @WebSocket
    public class EmbyWebSocketListener {
        @OnWebSocketConnect
        public void onConnect(Session wssession) {
        }

        @OnWebSocketMessage
        public void onMessage(String message) {
            logger.trace("Message received from server: {}", message);
            JsonObject json = JsonParser.parseString(message).getAsJsonObject();
            if (json.has("Data")) {
                JsonArray dataArr = json.get("Data").getAsJsonArray();
                Type listType = new TypeToken<List<EmbyPlayStateModel>>() {
                }.getType();
                @SuppressWarnings("unchecked")
                List<EmbyPlayStateModel> states = (List<EmbyPlayStateModel>) mapper.fromJson(dataArr, listType);

                if (states != null) {
                    states.forEach(eventHandler::handleEvent);
                } else {
                    logger.trace("Parsed EmbyPlayStateModel list was null; skipping event handling");
                }
            }
        }

        @OnWebSocketClose
        public void onClose(int statusCode, @Nullable String reason) {
            logger.debug("WebSocket closed ({}): {}", statusCode, reason);
            session = null;
            eventHandler.onConnectionClosed();
            if (shouldReconnect) {
                attemptReconnect();
            }
        }

        @OnWebSocketError
        public void onError(@Nullable Throwable error) {
            logger.error("WebSocket error, scheduling reconnect", error);
            Session current = session;
            if (current != null) {
                try {
                    current.disconnect();
                } catch (Exception e) {
                    logger.error("Failed to cleanly disconnect WebSocket session: {}", e.getMessage(), e);
                }
                current = null;
            }
            eventHandler.onConnectionClosed();
            if (shouldReconnect) {
                attemptReconnect();
            }
        }
    }
}
