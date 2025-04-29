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
import java.util.concurrent.CountDownLatch;
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
import com.google.gson.JsonElement;
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

    private final Logger logger = LoggerFactory.getLogger(EmbyClientSocket.class);

    private static final int REQUEST_TIMEOUT_MS = 10_000;
    private @Nullable CountDownLatch commandLatch;
    private @Nullable JsonObject commandResponse;

    private final Gson mapper = new Gson();
    private @Nullable URI uri;
    private @Nullable Session session;
    private final WebSocketClient client;
    private final ScheduledExecutorService scheduler;
    private final int bufferSize;
    private final EmbyWebSocketListener socket;
    private final ClientUpgradeRequest request;
    private final EmbyClientSocketEventListener eventHandler;

    public EmbyClientSocket(EmbyClientSocketEventListener eventHandler, @Nullable URI uri,
            ScheduledExecutorService scheduler, int bufferSize, WebSocketClient sharedWebSocketClient) {
        this.eventHandler = eventHandler;
        this.uri = uri;
        this.scheduler = scheduler;
        this.bufferSize = bufferSize;
        this.client = sharedWebSocketClient;
        this.socket = new EmbyWebSocketListener();
        this.request = new ClientUpgradeRequest();
    }

    /**
     * Attempts to open the WebSocket connection to Emby.
     */
    public synchronized void open() throws Exception {
        if (isConnected()) {
            logger.debug("connect: connection is already open");
            return;
        }
        if (!client.isStarted()) {
            client.start();
            client.setMaxTextMessageBufferSize(bufferSize);
        }
        Future<Session> future = client.connect(socket, uri, request);
        Session wsSession = future.get(REQUEST_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        this.session = wsSession;
        logger.debug("EMBY client socket open; awaiting Jetty onConnect callback");
    }

    /**
     * Closes the WebSocket connection.
     */
    public synchronized void close() {
        if (session != null) {
            try {
                session.close();
            } catch (Exception e) {
                logger.debug("Exception during closing websocket: {}", e.getMessage(), e);
            }
            session = null;
        }
        try {
            client.stop();
        } catch (Exception e) {
            logger.debug("Exception during stopping client: {}", e.getMessage(), e);
        }
    }

    /**
     * Returns true if the WebSocket session is open.
     */
    public synchronized boolean isConnected() {
        return session != null && session.isOpen();
    }

    @WebSocket
    @NonNullByDefault
    public class EmbyWebSocketListener {

        @OnWebSocketConnect
        public void onConnect(Session wsSession) {
            session = wsSession;
            logger.debug("WebSocket connected: session.isOpen()={}", session.isOpen());
            eventHandler.onConnectionOpened();
        }

        @OnWebSocketMessage
        public void onMessage(String message) {
            logger.trace("Message received: {}", message);
            JsonObject json = JsonParser.parseString(message).getAsJsonObject();

            // 1) Play‐state events
            if (json.has("Data") && json.get("Data").isJsonArray()) {
                JsonArray dataArray = json.get("Data").getAsJsonArray();
                Type listType = new TypeToken<List<EmbyPlayStateModel>>() {
                }.getType();
                List<EmbyPlayStateModel> events = mapper.fromJson(dataArray, listType);
                events.forEach(eventHandler::handleEvent);
            }

            // 2) Responses for callMethod(...)
            if (json.has("MessageType") || json.has("Response")) {
                commandResponse = json;
                if (commandLatch != null) {
                    commandLatch.countDown();
                }
            }
        }

        @OnWebSocketClose
        public void onClose(int statusCode, @Nullable String reason) {
            logger.debug("WebSocket closed ({}): {}", statusCode, reason);
            session = null;
            eventHandler.onConnectionClosed();
            // No internal reconnect—EmbyConnection will schedule any retry.
        }

        @OnWebSocketError
        public void onError(Throwable error) {
            logger.debug("WebSocket error: {}", error.getMessage(), error);
            eventHandler.onConnectionClosed();
        }
    }

    /**
     * Send a raw JSON string over the socket.
     */
    private void sendMessage(String msg) throws IOException {
        if (isConnected()) {
            session.getRemote().sendString(msg);
        } else {
            throw new IOException("WebSocket not connected");
        }
    }

    /**
     * Call a method without parameters.
     */
    public @Nullable JsonElement callMethod(String methodName) {
        JsonObject payload = new JsonObject();
        payload.addProperty("MessageType", methodName);
        return callMethod(payload);
    }

    /**
     * Call a method with string parameters.
     */
    public @Nullable JsonElement callMethodString(String methodName, String dataParams) {
        JsonObject payload = new JsonObject();
        payload.addProperty("MessageType", methodName);
        payload.addProperty("Data", dataParams);
        return callMethod(payload);
    }

    /**
     * Send a JSON-RPC payload and wait for its response.
     */
    private synchronized @Nullable JsonElement callMethod(JsonObject payload) {
        try {
            String jsonStr = mapper.toJson(payload);
            commandLatch = new CountDownLatch(1);
            commandResponse = null;
            sendMessage(jsonStr);

            if (commandLatch.await(REQUEST_TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
                logger.debug("Received callMethod response: {}", commandResponse);
                if (commandResponse.has("result")) {
                    return commandResponse.get("result");
                } else if (commandResponse.has("error")) {
                    logger.warn("Error in callMethod response: {}", commandResponse.get("error"));
                    return null;
                } else {
                    return commandResponse;
                }
            } else {
                logger.warn("Timeout waiting for response to {}", payload.get("MessageType"));
            }
        } catch (Exception e) {
            logger.error("callMethod error: {}", e.getMessage(), e);
        }
        return null;
    }
}
