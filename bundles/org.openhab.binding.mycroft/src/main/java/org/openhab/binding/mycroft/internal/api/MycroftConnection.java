/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.mycroft.internal.api;

import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.openhab.binding.mycroft.internal.api.dto.BaseMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Establishes and keeps a websocket connection to the Mycroft bus
 *
 * @author Gwendal ROULLEAU - Initial contribution. Inspired by the deconz binding.
 */
@WebSocket
@NonNullByDefault
public class MycroftConnection {
    private static final AtomicInteger INSTANCE_COUNTER = new AtomicInteger();
    private final Logger logger = LoggerFactory.getLogger(MycroftConnection.class);

    private final WebSocketClient client;
    private final String socketName;
    private final Gson gson;

    private final MycroftConnectionListener connectionListener;
    private final Map<MessageType, Set<MycroftMessageListener<? extends BaseMessage>>> listeners = new ConcurrentHashMap<>();

    private ConnectionState connectionState = ConnectionState.DISCONNECTED;
    private @Nullable Session session;

    private static final int TIMEOUT_MILLISECONDS = 3000;

    public MycroftConnection(MycroftConnectionListener listener, WebSocketClient client) {
        this.connectionListener = listener;
        this.client = client;
        this.client.setConnectTimeout(TIMEOUT_MILLISECONDS);
        this.client.setMaxIdleTimeout(0);
        this.socketName = "Websocket$" + System.currentTimeMillis() + "-" + INSTANCE_COUNTER.incrementAndGet();

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(MessageType.class, new MessageTypeConverter());
        gson = gsonBuilder.create();
    }

    public MycroftConnection(MycroftConnectionListener listener) {
        this.connectionListener = listener;
        this.client = new WebSocketClient();
        this.client.setMaxIdleTimeout(0);
        this.client.setConnectTimeout(TIMEOUT_MILLISECONDS);
        this.socketName = "Websocket$" + System.currentTimeMillis() + "-" + INSTANCE_COUNTER.incrementAndGet();

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(MessageType.class, new MessageTypeConverter());
        gson = gsonBuilder.create();
    }

    public void start(String ip, int port) {
        if (connectionState == ConnectionState.CONNECTED) {
            return;
        } else if (connectionState == ConnectionState.CONNECTING) {
            logger.debug("{} already connecting", socketName);
            return;
        } else if (connectionState == ConnectionState.DISCONNECTING) {
            logger.warn("{} trying to re-connect while still disconnecting", socketName);
        }
        Future<Session> futureConnect = null;
        try {
            URI destUri = URI.create("ws://" + ip + ":" + port + "/core");
            client.start();
            logger.debug("Trying to connect {} to {}", socketName, destUri);
            futureConnect = client.connect(this, destUri);
            futureConnect.get(TIMEOUT_MILLISECONDS, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            if (futureConnect != null) {
                futureConnect.cancel(true);
            }
            connectionListener
                    .connectionLost("Error while connecting: " + (e.getMessage() != null ? e.getMessage() : "unknown"));
        }
    }

    public void close() {
        try {
            connectionState = ConnectionState.DISCONNECTING;
            client.stop();
        } catch (Exception e) {
            logger.debug("{} encountered an error while closing connection", socketName, e);
        }
        client.destroy();
    }

    /**
     * The listener registered in this method will be called when a corresponding message will be detected
     * on the mycroft bus
     *
     * @param messageType
     * @param listener
     */
    public void registerListener(MessageType messageType, MycroftMessageListener<? extends BaseMessage> listener) {
        Set<MycroftMessageListener<? extends BaseMessage>> messageTypeListeners = listeners.get(messageType);
        if (messageTypeListeners == null) {
            messageTypeListeners = new HashSet<MycroftMessageListener<? extends BaseMessage>>();
            listeners.put(messageType, messageTypeListeners);
        }
        messageTypeListeners.add(listener);
    }

    public void unregisterListener(MessageType messageType, MycroftMessageListener<?> listener) {
        Optional.ofNullable(listeners.get(messageType))
                .ifPresent((messageTypeListeners) -> messageTypeListeners.remove(listener));
    }

    public void sendMessage(BaseMessage message) throws IOException {
        sendMessage(gson.toJson(message));
    }

    public void sendMessage(String message) throws IOException {
        final Session storedSession = this.session;
        try {
            if (storedSession != null) {
                storedSession.getRemote().sendString(message);
            } else {
                throw new IOException("Session is not initialized");
            }
        } catch (IOException e) {
            if (storedSession != null && storedSession.isOpen()) {
                storedSession.close(-1, "Sending message error");
            }
            throw e;
        }
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        connectionState = ConnectionState.CONNECTED;
        logger.debug("{} successfully connected to {}: {}", socketName, session.getRemoteAddress().getAddress(),
                session.hashCode());
        connectionListener.connectionEstablished();
        this.session = session;
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) {
        if (!session.equals(this.session)) {
            handleWrongSession(session, message);
            return;
        }
        logger.trace("{} received raw data: {}", socketName, message);

        try {

            // listeners on message type :
            BaseMessage mycroftMessage = gson.fromJson(message, BaseMessage.class);
            Objects.requireNonNull(mycroftMessage);
            mycroftMessage.message = message;

            // special listener : to any messages
            Set<MycroftMessageListener<? extends BaseMessage>> listenersAnyToNotify = listeners.get(MessageType.any);
            if (listenersAnyToNotify != null && !listenersAnyToNotify.isEmpty()) {
                for (MycroftMessageListener<? extends BaseMessage> listener : listenersAnyToNotify) {
                    listener.baseMessageReceived(mycroftMessage);
                }
            }

            // listener for specific message type
            Set<MycroftMessageListener<? extends BaseMessage>> listenersToNotify = listeners.get(mycroftMessage.type);
            if (mycroftMessage.type != MessageType.any && listenersToNotify != null && !listenersToNotify.isEmpty()) {
                BaseMessage fullMycroftMessage = gson.fromJson(message, mycroftMessage.type.getMessageTypeClass());
                mycroftMessage.message = message;
                Objects.requireNonNull(fullMycroftMessage);
                listenersToNotify.forEach(listener -> listener.baseMessageReceived(fullMycroftMessage));
            }

        } catch (RuntimeException e) {
            // we need to catch all processing exceptions, otherwise they could affect the connection
            logger.warn("{} encountered an error while processing the message {}: {}", socketName, message,
                    e.getMessage());
        }
    }

    @OnWebSocketError
    public void onError(@Nullable Session session, Throwable cause) {

        if (session == null || !session.equals(this.session)) {
            handleWrongSession(session, "Connection error: " + cause.getMessage());
            return;
        }
        logger.warn("{} connection error, closing: {}", socketName, cause.getMessage());

        Session storedSession = this.session;
        if (storedSession != null && storedSession.isOpen()) {
            storedSession.close(-1, "Processing error");
        }
    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        if (!session.equals(this.session)) {
            handleWrongSession(session, "Connection closed: " + statusCode + " / " + reason);
            return;
        }
        logger.trace("{} closed connection: {} / {}", socketName, statusCode, reason);
        connectionState = ConnectionState.DISCONNECTED;
        this.session = null;
        connectionListener.connectionLost(reason);
    }

    private void handleWrongSession(@Nullable Session session, String message) {
        if (session == null) {
            logger.warn("received and discarded message for null session : {}", message);
        } else {
            logger.warn("{} received and discarded message for other session {}: {}.", socketName, session.hashCode(),
                    message);
        }
    }

    /**
     * check connection state (successfully connected)
     *
     * @return true if connected, false if connecting, disconnecting or disconnected
     */
    public boolean isConnected() {
        return connectionState == ConnectionState.CONNECTED;
    }

    /**
     * used internally to represent the connection state
     */
    private enum ConnectionState {
        CONNECTING,
        CONNECTED,
        DISCONNECTING,
        DISCONNECTED
    }
}
