/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.sonyaudio.internal.protocol;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

/**
 * The {@link SonyAudioConnection} is responsible for communicating with SONY audio products
 * handlers.
 *
 * @author David Åberg - Initial contribution
 */
public class SonyAudioClientSocket {
    private final Logger logger = LoggerFactory.getLogger(SonyAudioClientSocket.class);

    private final ScheduledExecutorService scheduler;
    private static final int REQUEST_TIMEOUT_MS = 60000;

    private CountDownLatch commandLatch = null;
    private JsonObject commandResponse = null;
    private int nextMessageId = 1;

    private boolean connected = false;

    private final URI uri;
    private Session session;

    private final Gson mapper;

    private static int ping = 0;

    private final SonyAudioClientSocketEventListener eventHandler;

    public SonyAudioClientSocket(SonyAudioClientSocketEventListener eventHandler, URI uri,
            ScheduledExecutorService scheduler) {
        mapper = new GsonBuilder().disableHtmlEscaping().create();
        this.eventHandler = eventHandler;
        this.uri = uri;
        this.scheduler = scheduler;
    }

    public synchronized void open(WebSocketClient webSocketClient) {
        try {
            if (isConnected()) {
                logger.warn("connect: connection is already open");
            }

            SonyAudioWebSocketListener socket = new SonyAudioWebSocketListener();
            ClientUpgradeRequest request = new ClientUpgradeRequest();

            try {
                webSocketClient.connect(socket, uri, request).get(1, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                logger.debug("Could not establish websocket within a second.");
            }
        } catch (Exception e) {
            logger.debug("Exception then trying to start the websocket {}", e.getMessage(), e);
        }
    }

    public void close() {
        logger.debug("Closing socket {}", uri);
        // if there is an old web socket then clean up and destroy
        if (session != null) {
            try {
                session.close();
            } catch (Exception e) {
                logger.debug("Exception during closing the websocket session {}", e.getMessage(), e);
            }
            session = null;
        }
    }

    public boolean isConnected() {
        if (session == null || !session.isOpen()) {
            connected = false;
            return connected;
        }

        RemoteEndpoint remote = session.getRemote();

        ByteBuffer payload = ByteBuffer.allocate(4).putInt(ping++);
        try {
            remote.sendPing(payload);
        } catch (IOException e) {
            logger.warn("Connection to {} lost: {}", uri, e.getMessage());
            connected = false;
        }

        return connected;
    }

    @WebSocket
    public class SonyAudioWebSocketListener {

        @OnWebSocketConnect
        public void onConnect(Session wssession) {
            logger.debug("Connected to server");
            session = wssession;
            connected = true;
            if (eventHandler != null) {
                scheduler.submit(() -> {
                    try {
                        eventHandler.onConnectionOpened(uri);
                    } catch (Exception e) {
                        logger.error("Error handling onConnectionOpened() {}", e.getMessage(), e);
                    }
                });
            }
        }

        @OnWebSocketMessage
        public void onMessage(String message) {
            logger.debug("Message received from server: {}", message);
            try {
                final JsonObject json = JsonParser.parseString(message).getAsJsonObject();
                if (json.has("id")) {
                    logger.debug("Response received from server: {}", json);
                    int messageId = json.get("id").getAsInt();
                    if (messageId == nextMessageId - 1) {
                        commandResponse = json;
                        commandLatch.countDown();
                    }
                } else {
                    logger.debug("Event received from server: {}", json);
                    try {
                        if (eventHandler != null) {
                            scheduler.submit(() -> {
                                try {
                                    eventHandler.handleEvent(json);
                                } catch (Exception e) {
                                    logger.error("Error handling event {} player state change message: {}", json,
                                            e.getMessage(), e);
                                }
                            });
                        }
                    } catch (Exception e) {
                        logger.error("Error handling player state change message", e);
                    }
                }
            } catch (JsonParseException e) {
                logger.debug("Not valid JSON message: {}", e.getMessage(), e);
            }
        }

        @OnWebSocketClose
        public void onClose(int statusCode, String reason) {
            session = null;
            connected = false;
            logger.debug("Closing a WebSocket due to {}", reason);
            scheduler.submit(() -> {
                try {
                    eventHandler.onConnectionClosed();
                } catch (Exception e) {
                    logger.error("Error handling onConnectionClosed()", e);
                }
            });
        }

        @OnWebSocketError
        public void onError(Throwable error) {
            onClose(0, error.getMessage());
        }
    }

    private void sendMessage(String str) throws IOException {
        if (isConnected()) {
            logger.debug("send message fo {}: {}", uri.toString(), str);
            session.getRemote().sendString(str);
        } else {
            String stack = "";
            stack += "Printing stack trace:\n";
            StackTraceElement[] elements = Thread.currentThread().getStackTrace();
            for (int i = 1; i < elements.length; i++) {
                StackTraceElement s = elements[i];
                stack += "\tat " + s.getClassName() + "." + s.getMethodName() + "(" + s.getFileName() + ":"
                        + s.getLineNumber() + ")\n";
            }
            logger.error("Socket not initialized, trying to send {} {}", str, stack);
            throw new IOException("Socket not initialized");
        }
    }

    public synchronized JsonElement callMethod(SonyAudioMethod method) throws IOException {
        try {
            method.id = nextMessageId;
            String message = mapper.toJson(method);
            logger.debug("callMethod send {}", message);

            commandLatch = new CountDownLatch(1);
            commandResponse = null;
            nextMessageId++;

            sendMessage(message);
            if (commandLatch.await(REQUEST_TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
                logger.debug("callMethod {} returns {}", uri.toString(), commandResponse.toString());
                return commandResponse.get("result");
            } else {
                logger.debug("Timeout during callMethod({}, {})", method.method, message);
                throw new IOException("Timeout during callMethod");
            }
        } catch (InterruptedException e) {
            throw new IOException("Timeout in callMethod");
        }
    }

    public URI getURI() {
        return uri;
    }
}
