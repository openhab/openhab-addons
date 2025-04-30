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
    private int bufferSize;

    private final int maxReconnectAttempts = 5;
    private final long reconnectDelayMs = 5000;
    private volatile boolean shouldReconnect = true;
    private int reconnectAttempts = 0;

    public EmbyClientSocket(EmbyClientSocketEventListener handler, @Nullable URI setUri,
            ScheduledExecutorService setScheduler, int setBufferSize, WebSocketClient sharedWebSocketClient) {
        this.eventHandler = handler;
        this.uri = setUri;
        this.scheduler = setScheduler;
        this.bufferSize = setBufferSize;
        this.client = sharedWebSocketClient;
    }

    public synchronized void open() throws Exception {
        if (isConnected()) {
            logger.warn("connect: connection is already open");
            return;
        }
        if (!client.isStarted()) {
            client.start();
            client.setMaxTextMessageBufferSize(bufferSize);
        }

        Future<Session> future = client.connect(socket, uri, request);
        Session wsSession = future.get(REQUEST_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        this.session = wsSession;

        logger.info("EMBY client socket is Connected to emby server");
        if (eventHandler != null) {
            scheduler.submit(() -> eventHandler.onConnectionOpened());
        }
    }

    public void close() {
        shouldReconnect = false;
        reconnectAttempts = 0;
        if (session != null) {
            try {
                session.close();
            } catch (Exception e) {
                logger.debug("Exception during closing the websocket: {}", e.getMessage(), e);
            }
            session = null;
        }
        try {
            client.stop();
        } catch (Exception e) {
            logger.debug("Exception during closing the websocket: {}", e.getMessage(), e);
        }
    }

    public boolean isConnected() {
        return session != null && session.isOpen();
    }

    public void sendCommand(String methodName, String dataParams) {
        JsonObject payload = new JsonObject();
        payload.addProperty("MessageType", methodName);
        payload.addProperty("Data", dataParams);

        scheduler.submit(() -> {
            try {
                if (session == null || !session.isOpen()) {
                    logger.warn("Cannot send {}, session not open", methodName);
                    return;
                }
                String json = mapper.toJson(payload);
                session.getRemote().sendString(json);
                logger.debug("Sent command: {}", json);
            } catch (IOException e) {
                logger.error("Failed sending {}: {}", methodName, e.getMessage(), e);
            }
        });
    }

    @WebSocket
    @NonNullByDefault
    public class EmbyWebSocketListener {
        @OnWebSocketConnect
        public void onConnect(Session wssession) {
            logger.info("EMBY client socket connected (Jetty callback)");
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

                states.forEach(eventHandler::handleEvent);
            }
        }

        @OnWebSocketClose
        public void onClose(int statusCode, @Nullable String reason) {
            logger.debug("WebSocket closed ({}): {}", statusCode, reason);
            session = null;
            shouldReconnect = true;
            eventHandler.onConnectionClosed();
            if (shouldReconnect && reconnectAttempts < maxReconnectAttempts) {
                scheduler.schedule(EmbyClientSocket.this::attemptReconnect, reconnectDelayMs, TimeUnit.MILLISECONDS);
            }
        }

        @OnWebSocketError
        public void onError(@Nullable Throwable error) {
            onClose(0, error != null ? error.getMessage() : "unknown");
        }
    }

    private synchronized void attemptReconnect() {
        reconnectAttempts++;
        logger.info("Attempting reconnect {}/{}", reconnectAttempts, maxReconnectAttempts);
        try {
            open();
            reconnectAttempts = 0;
            logger.info("Reconnection successful.");
        } catch (Exception e) {
            logger.error("Reconnect failed: {}", e.getMessage(), e);
            if (reconnectAttempts < maxReconnectAttempts) {
                scheduler.schedule(this::attemptReconnect, reconnectDelayMs, TimeUnit.MILLISECONDS);
            }
        }
    }
}
