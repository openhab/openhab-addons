/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
public class EmbyClientSocket {

    private final Logger logger = LoggerFactory.getLogger(EmbyClientSocket.class);

    private final ScheduledExecutorService scheduler;
    private static final int REQUEST_TIMEOUT_MS = 60000;

    private CountDownLatch commandLatch = null;
    private JsonObject commandResponse = null;

    private boolean connected = false;

    private final JsonParser parser = new JsonParser();
    private final Gson mapper = new Gson();
    private URI uri;
    private Session session;
    private WebSocketClient client;
    private int bufferSize;

    private final EmbyClientSocketEventListener eventHandler;

    public EmbyClientSocket(EmbyClientSocketEventListener eventHandler, URI uri, ScheduledExecutorService scheduler,
            int buffersize) {
        this.eventHandler = eventHandler;
        this.uri = uri;
        client = new WebSocketClient();
        this.scheduler = scheduler;
        this.bufferSize = buffersize;
    }

    /**
     * Attempts to create a connection to the Emby host and begin listening for
     * updates over the async http web socket
     *
     * @throws Exception
     */
    public synchronized void open() throws Exception {
        if (isConnected()) {
            logger.warn("connect: connection is already open");
        }
        if (!client.isStarted()) {
            client.start();
            client.setMaxTextMessageBufferSize(bufferSize);
        }
        EmbyWebSocketListener socket = new EmbyWebSocketListener();
        ClientUpgradeRequest request = new ClientUpgradeRequest();

        client.connect(socket, uri, request);
    }

    /***
     * Close this connection to the Emby instance
     */
    public void close() {
        // if there is an old web socket then clean up and destroy
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
        if (session == null || !session.isOpen()) {
            return false;
        }

        return connected;
    }

    @WebSocket
    public class EmbyWebSocketListener {
        @OnWebSocketConnect
        public void onConnect(Session wssession) {
            logger.debug("Connected to server");
            session = wssession;
            connected = true;
            if (eventHandler != null) {
                scheduler.submit(() -> {
                    try {
                        eventHandler.onConnectionOpened();
                    } catch (Exception e) {
                        logger.debug("Error handling onConnectionOpened(): {}", e.getMessage(), e);
                    }
                });
            }
        }

        @OnWebSocketMessage
        public void onMessage(String message) {
            logger.debug("Message received from server: {}", message);
            final JsonObject json = parser.parse(message).getAsJsonObject();
            if (json.has("Data")) {
                JsonArray messageId = json.get("Data").getAsJsonArray();
                Gson gson = new Gson();
                Type type = new TypeToken<List<EmbyPlayStateModel>>() {
                }.getType();
                List<EmbyPlayStateModel> response = gson.fromJson(messageId, type);

                response.forEach(playstate -> {

                    // need to add internal validation method check but right now will pass all states to handler
                    eventHandler.handleEvent(playstate);

                });
            }
        }

        @OnWebSocketClose
        public void onClose(int statusCode, String reason) {
            logger.debug("Closing a WebSocket due to {} with status code: {}", reason, Integer.toString(statusCode));
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
        }

        @OnWebSocketError
        public void onError(Throwable error) {
            onClose(0, error.getMessage());
        }

    }

    private void sendMessage(String str) throws IOException {
        if (isConnected()) {
            logger.debug("send message: {}", str);
            session.getRemote().sendString(str);
        } else {
            throw new IOException("socket not initialized");
        }
    }

    public JsonElement callMethod(String methodName) {
        return callMethod(methodName, null);
    }

    public JsonElement callMethodString(String methodName, String dataParams) {
        JsonObject payloadObject = new JsonObject();
        payloadObject.addProperty("MessageType", methodName);
        payloadObject.addProperty("Data", dataParams);
        return callMethod(payloadObject);
    }

    public JsonElement callMethod(String methodName, JsonObject params) {
        JsonObject payloadObject = new JsonObject();
        // payloadObject.addProperty("jsonrpc", "2.0");
        // payloadObject.addProperty("id", nextMessageId);
        payloadObject.addProperty("MessageType", methodName);
        if (params != null) {
            payloadObject.add("Data", params);
        }
        return callMethod(payloadObject);
    }

    public synchronized JsonElement callMethod(JsonObject payloadObject) {
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
                    logger.debug("Error received from server: {}", error);
                    return null;
                }
            } else {
                logger.debug("Timeout during callMethod({}, {})", payloadObject.get("MessageType").toString(),
                        payloadObject.get("Data").toString());
                return null;
            }
        } catch (Exception e) {
            logger.debug("Error during callMethod({}): {}", payloadObject.get("MessageType").toString(), e.getMessage(),
                    e);
            return null;
        }
    }
}
