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
package org.openhab.binding.tesla.internal.handler;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.eclipse.jetty.websocket.api.WebSocketPingPongListener;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.openhab.binding.tesla.internal.protocol.Event;
import org.openhab.core.io.net.http.WebSocketFactory;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.util.ThingWebClientUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link TeslaEventEndpoint} is responsible managing a websocket connection to a specific URI, most notably the
 * Tesla event stream infrastructure. Consumers can register an {@link EventHandler} in order to receive data that was
 * received by the websocket endpoint. The {@link TeslaEventEndpoint} can also implements a ping/pong mechanism to keep
 * websockets alive.
 *
 * @author Karel Goderis - Initial contribution
 */
public class TeslaEventEndpoint implements WebSocketListener, WebSocketPingPongListener {

    private static final int TIMEOUT_MILLISECONDS = 3000;
    private static final int IDLE_TIMEOUT_MILLISECONDS = 30000;

    private final Logger logger = LoggerFactory.getLogger(TeslaEventEndpoint.class);

    private String endpointId;
    protected WebSocketFactory webSocketFactory;

    private WebSocketClient client;
    private ConnectionState connectionState = ConnectionState.CLOSED;
    private @Nullable Session session;
    private EventHandler eventHandler;
    private final Gson gson = new Gson();

    public TeslaEventEndpoint(ThingUID uid, WebSocketFactory webSocketFactory) {
        try {
            this.endpointId = "TeslaEventEndpoint-" + uid.getAsString();

            String name = ThingWebClientUtil.buildWebClientConsumerName(uid, null);
            client = webSocketFactory.createWebSocketClient(name);
            this.client.setConnectTimeout(TIMEOUT_MILLISECONDS);
            this.client.setMaxIdleTimeout(IDLE_TIMEOUT_MILLISECONDS);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void close() {
        try {
            if (client.isRunning()) {
                client.stop();
            }
        } catch (Exception e) {
            logger.warn("An exception occurred while stopping the WebSocket client : {}", e.getMessage());
        }
    }

    public void connect(URI endpointURI) {
        if (connectionState == ConnectionState.CONNECTED) {
            return;
        } else if (connectionState == ConnectionState.CONNECTING) {
            logger.debug("{} : Already connecting to {}", endpointId, endpointURI);
            return;
        } else if (connectionState == ConnectionState.CLOSING) {
            logger.warn("{} : Connecting to {} while already closing the connection", endpointId, endpointURI);
            return;
        }
        Future<Session> futureConnect = null;
        try {
            if (!client.isRunning()) {
                logger.debug("{} : Starting the client to connect to {}", endpointId, endpointURI);
                client.start();
            } else {
                logger.debug("{} : The client to connect to {} is already running", endpointId, endpointURI);
            }

            logger.debug("{} : Connecting to {}", endpointId, endpointURI);
            connectionState = ConnectionState.CONNECTING;
            futureConnect = client.connect(this, endpointURI);
            futureConnect.get(TIMEOUT_MILLISECONDS, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            logger.error("An exception occurred while connecting the Event Endpoint : '{}'", e.getMessage());
            if (futureConnect != null) {
                futureConnect.cancel(true);
            }
        }
    }

    @Override
    public void onWebSocketConnect(Session session) {
        logger.debug("{} : Connected to {} with hash {}", endpointId, session.getRemoteAddress().getAddress(),
                session.hashCode());
        connectionState = ConnectionState.CONNECTED;
        this.session = session;
    }

    public void closeConnection() {
        try {
            connectionState = ConnectionState.CLOSING;
            if (session != null && session.isOpen()) {
                logger.debug("{} : Closing the session", endpointId);
                session.close(StatusCode.NORMAL, "bye");
            }
        } catch (Exception e) {
            logger.error("{} : An exception occurred while closing the session : {}", endpointId, e.getMessage());
            connectionState = ConnectionState.CLOSED;
        }
    }

    @Override
    public void onWebSocketClose(int statusCode, String reason) {
        logger.debug("{} : Closed the session with status {} for reason {}", endpointId, statusCode, reason);
        connectionState = ConnectionState.CLOSED;
        this.session = null;
    }

    @Override
    public void onWebSocketText(String message) {
        // NoOp
    }

    @Override
    public void onWebSocketBinary(byte[] payload, int offset, int length) {
        BufferedReader in = new BufferedReader(
                new InputStreamReader(new ByteArrayInputStream(payload), StandardCharsets.UTF_8.newDecoder()
                        .onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT)));
        String str;
        try {
            while ((str = in.readLine()) != null) {
                logger.trace("{} : Received raw data '{}'", endpointId, str);
                if (this.eventHandler != null) {
                    try {
                        Event event = gson.fromJson(str, Event.class);
                        this.eventHandler.handleEvent(event);
                    } catch (RuntimeException e) {
                        logger.error("{} : An exception occurred while processing raw data : {}", endpointId,
                                e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            logger.error("{} : An exception occurred while receiving raw data : {}", endpointId, e.getMessage());
        }
    }

    @Override
    public void onWebSocketError(Throwable cause) {
        logger.error("{} : An error occurred in the session : {}", endpointId, cause.getMessage());
        if (session != null && session.isOpen()) {
            session.close(StatusCode.ABNORMAL, "Session Error");
        }
    }

    public void sendMessage(String message) throws IOException {
        try {
            if (session != null) {
                logger.debug("{} : Sending raw data '{}'", endpointId, message);
                session.getRemote().sendString(message);
            } else {
                throw new IOException("Session is not initialized");
            }
        } catch (IOException e) {
            if (session != null && session.isOpen()) {
                session.close(StatusCode.ABNORMAL, "Send Message Error");
            }
            throw e;
        }
    }

    public void ping() {
        try {
            if (session != null) {
                ByteBuffer buffer = ByteBuffer.allocate(8).putLong(System.nanoTime()).flip();
                session.getRemote().sendPing(buffer);
            }
        } catch (IOException e) {
            logger.error("{} : An exception occurred while pinging the remote end : {}", endpointId, e.getMessage());
        }
    }

    @Override
    public void onWebSocketPing(ByteBuffer payload) {
        ByteBuffer buffer = ByteBuffer.allocate(8).putLong(System.nanoTime()).flip();
        try {
            if (session != null) {
                session.getRemote().sendPing(buffer);
            }
        } catch (IOException e) {
            logger.error("{} : An exception occurred while processing a ping message : {}", endpointId, e.getMessage());
        }
    }

    @Override
    public void onWebSocketPong(ByteBuffer payload) {
        long start = payload.getLong();
        long roundTrip = System.nanoTime() - start;

        logger.trace("{} : Received a Pong with a roundtrip of {} milliseconds", endpointId,
                TimeUnit.MILLISECONDS.convert(roundTrip, TimeUnit.NANOSECONDS));
    }

    public void addEventHandler(EventHandler eventHandler) {
        this.eventHandler = eventHandler;
    }

    public boolean isConnected() {
        return connectionState == ConnectionState.CONNECTED;
    }

    public static interface EventHandler {
        public void handleEvent(Event event);
    }

    private enum ConnectionState {
        CONNECTING,
        CONNECTED,
        CLOSING,
        CLOSED
    }
}
