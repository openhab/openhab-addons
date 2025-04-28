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

    private final ScheduledExecutorService scheduler;
    private static final int REQUEST_TIMEOUT_MS = 10000;

    private @Nullable CountDownLatch commandLatch = null;
    private @Nullable JsonObject commandResponse = null;

    private boolean connected = false;

    private final Gson mapper = new Gson();
    private @Nullable URI uri;
    private @Nullable Session session;
    private WebSocketClient client;
    private int bufferSize;
    private EmbyWebSocketListener socket;
    private ClientUpgradeRequest request;

    private final EmbyClientSocketEventListener eventHandler;

    private final int maxReconnectAttempts = 5;
    private final long reconnectDelayMs = 5000;
    private volatile boolean shouldReconnect = true;
    private int reconnectAttempts = 0;

    public EmbyClientSocket(EmbyClientSocketEventListener setEventHandler, @Nullable URI setUri,
            ScheduledExecutorService setScheduler, int setBufferSize, WebSocketClient sharedWebSocketClient) {
        eventHandler = setEventHandler;
        uri = setUri;
        client = sharedWebSocketClient;
        scheduler = setScheduler;
        bufferSize = setBufferSize;
        socket = new EmbyWebSocketListener();
        request = new ClientUpgradeRequest();
    }

    /**
     * Attempts to create a connection to the Emby host and begin listening for
     * updates over the async http web socket
     *
     * @throws Exception
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

        // 1) initiate the connect, wait for handshake to finish
        Future<Session> future = client.connect(socket, uri, request);
        Session wsSession = future.get(REQUEST_TIMEOUT_MS, TimeUnit.MILLISECONDS);

        // 2) store the session; actual "connected" event will fire in onConnect()
        this.session = wsSession;
        logger.debug("EMBY client socket open; awaiting Jetty onConnect callback");
    }

    /***
     * Close this connection to the Emby instance
     */
    public void close() {
        // if there is an old web socket then clean up and destroy
        shouldReconnect = false;
        reconnectAttempts = 0;
        if (session != null) {
            try {
                session.close();
            } catch (Exception e) {
                logger.debug("Exception during the closing the websocket: {}", e.getMessage(), e);
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

    @WebSocket
    @NonNullByDefault
    public class EmbyWebSocketListener {
        @OnWebSocketConnect
        public void onConnect(Session wssession) {
            // Jetty says “we’re open” – set the session and flag immediately
            session = wssession;
            connected = true;
            logger.debug("EMBY client socket connected (Jetty callback); session.isOpen()={}  connected={}",
                    session.isOpen(), connected);

            // Fire your handler *synchronously*, so any callMethod() in onConnectionOpened()
            // sees session!=null, session.isOpen()==true, and connected==true.
            if (eventHandler != null) {
                try {
                    eventHandler.onConnectionOpened();
                } catch (Exception e) {
                    logger.error("Error in onConnectionOpened(): {}", e.getMessage(), e);
                }
            }
        }

        @OnWebSocketMessage
        public void onMessage(String message) {
            logger.trace("Message received from server: {}", message);
            final JsonObject json = JsonParser.parseString(message).getAsJsonObject();
            if (json.has("Data")) {
                JsonArray messageId = json.get("Data").getAsJsonArray();
                Gson gson = new Gson();
                Type type = new TypeToken<List<EmbyPlayStateModel>>() {
                }.getType();
                List<EmbyPlayStateModel> response = gson.fromJson(messageId, type);
                response.forEach(eventHandler::handleEvent);
            }
        }

        @OnWebSocketClose
        public void onClose(int statusCode, @Nullable String reason) {
            logger.debug("Closing a WebSocket due to {} with status code: {}", reason != null ? reason : "unknown",
                    statusCode);

            session = null;
            connected = false;

            if (eventHandler != null) {
                scheduler.submit(() -> {
                    try {
                        eventHandler.onConnectionClosed();
                    } catch (Exception e) {
                        logger.debug("Error handling onConnectionClosed(): {}", e.getMessage(), e);
                    }
                });
            }

            // Trigger reconnect if appropriate
            if (shouldReconnect) {
                scheduler.schedule(EmbyClientSocket.this::attemptReconnect, reconnectDelayMs, TimeUnit.MILLISECONDS);
            }
        }

        @OnWebSocketError
        public void onError(@Nullable Throwable error) {
            onClose(0, error.getMessage());
        }
    }

    private synchronized void attemptReconnect() {
        if (reconnectAttempts >= maxReconnectAttempts) {
            logger.warn("Max reconnect attempts reached. Giving up.");
            return;
        }

        reconnectAttempts++;
        logger.debug("Attempting reconnect {} of {}", reconnectAttempts, maxReconnectAttempts);

        try {
            open(); // reuse the existing open logic
            logger.debug("Reconnection successful.");
            reconnectAttempts = 0; // reset on success
        } catch (Exception e) {
            logger.error("Reconnect attempt failed: {}", e.getMessage(), e);
            scheduler.schedule(this::attemptReconnect, reconnectDelayMs, TimeUnit.MILLISECONDS);
        }
    }

    private void sendMessage(String str) throws IOException {
        boolean conn = isConnected();
        logger.trace("sendMessage: isConnected={}  session={}  session.isOpen()={}", conn, session,
                session != null ? session.isOpen() : "N/A");
        if (conn) {
            session.getRemote().sendString(str);
        } else {
            throw new IOException("socket not initialized");
        }
    }

    public @Nullable JsonElement callMethod(String methodName) {
        return callMethod(methodName, null);
    }

    public @Nullable JsonElement callMethodString(String methodName, String dataParams) {
        JsonObject payloadObject = new JsonObject();
        payloadObject.addProperty("MessageType", methodName);
        payloadObject.addProperty("Data", dataParams);
        return callMethod(payloadObject);
    }

    public @Nullable JsonElement callMethod(String methodName, @Nullable JsonObject params) {
        JsonObject payloadObject = new JsonObject();
        payloadObject.addProperty("MessageType", methodName);
        if (params != null) {
            payloadObject.add("Data", params);
        }
        return callMethod(payloadObject);
    }

    public synchronized @Nullable JsonElement callMethod(JsonObject payloadObject) {
        try {
            String message = mapper.toJson(payloadObject);
            commandLatch = new CountDownLatch(1);
            commandResponse = null;
            sendMessage(message);
            if (commandLatch.await(REQUEST_TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
                logger.debug("callMethod returns {}", commandResponse);
                if (commandResponse.has("result")) {
                    return commandResponse.get("result");
                } else {
                    JsonElement error = commandResponse.get("error");
                    logger.warn("Error received from server: {}", error);
                    return null;
                }
            } else {
                logger.warn("Timeout during callMethod({}, {})", payloadObject.get("MessageType").toString(),
                        payloadObject.get("Data").toString());
                return null;
            }
        } catch (Exception e) {
            logger.error("Error during callMethod({}): {}", payloadObject.get("MessageType").toString(), e.getMessage(),
                    e);
            return null;
        }
    }
}
