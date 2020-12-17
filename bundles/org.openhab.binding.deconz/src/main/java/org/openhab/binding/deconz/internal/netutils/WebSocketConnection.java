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
package org.openhab.binding.deconz.internal.netutils;

import java.net.URI;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.openhab.binding.deconz.internal.dto.DeconzBaseMessage;
import org.openhab.binding.deconz.internal.types.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * Establishes and keeps a websocket connection to the deCONZ software.
 *
 * The connection is closed by deCONZ now and then and needs to be re-established.
 *
 * @author David Graeff - Initial contribution
 */
@WebSocket
@NonNullByDefault
public class WebSocketConnection {
    private final Logger logger = LoggerFactory.getLogger(WebSocketConnection.class);

    private final WebSocketClient client;
    private final String socketName;
    private final Gson gson;

    private final WebSocketConnectionListener connectionListener;
    private final Map<Map.Entry<ResourceType, String>, WebSocketMessageListener> listeners = new ConcurrentHashMap<>();

    private ConnectionState connectionState = ConnectionState.DISCONNECTED;

    public WebSocketConnection(WebSocketConnectionListener listener, WebSocketClient client, Gson gson) {
        this.connectionListener = listener;
        this.client = client;
        this.client.setMaxIdleTimeout(0);
        this.gson = gson;
        this.socketName = ((QueuedThreadPool) client.getExecutor()).getName() + "$" + this.hashCode();
    }

    public void start(String ip) {
        if (connectionState == ConnectionState.CONNECTED) {
            return;
        } else if (connectionState == ConnectionState.CONNECTING) {
            logger.debug("{} already connecting", socketName);
            return;
        }
        try {
            URI destUri = URI.create("ws://" + ip);
            client.start();
            logger.debug("Trying to connect {} to {}", socketName, destUri);
            client.connect(this, destUri).get();
        } catch (Exception e) {
            connectionListener.connectionError(e);
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

    public void registerListener(ResourceType resourceType, String sensorID, WebSocketMessageListener listener) {
        listeners.put(Map.entry(resourceType, sensorID), listener);
    }

    public void unregisterListener(ResourceType resourceType, String sensorID) {
        listeners.remove(Map.entry(resourceType, sensorID));
    }

    @SuppressWarnings("unused")
    @OnWebSocketConnect
    public void onConnect(Session session) {
        connectionState = ConnectionState.CONNECTED;
        logger.debug("{} successfully connected to {}", socketName, session.getRemoteAddress().getAddress());
        connectionListener.connectionEstablished();
    }

    @SuppressWarnings({ "null", "unused" })
    @OnWebSocketMessage
    public void onMessage(String message) {
        logger.trace("Raw data received by websocket {}: {}", socketName, message);

        DeconzBaseMessage changedMessage = Objects.requireNonNull(gson.fromJson(message, DeconzBaseMessage.class));
        if (changedMessage.r == ResourceType.UNKNOWN) {
            logger.trace("Received message has unknown resource type. Skipping message.");
            return;
        }

        WebSocketMessageListener listener = listeners.get(Map.entry(changedMessage.r, changedMessage.id));
        if (listener == null) {
            logger.debug(
                    "Couldn't find listener for id {} with resource type {}. Either no thing for this id has been defined or this is a bug.",
                    changedMessage.id, changedMessage.r);
            return;
        }

        Class<? extends DeconzBaseMessage> expectedMessageType = changedMessage.r.getExpectedMessageType();
        if (expectedMessageType == null) {
            logger.warn("BUG! Could not get expected message type for resource type {}. Please report this incident.",
                    changedMessage.r);
            return;
        }

        DeconzBaseMessage deconzMessage = gson.fromJson(message, expectedMessageType);
        if (deconzMessage != null) {
            listener.messageReceived(changedMessage.id, deconzMessage);
        }
    }

    @SuppressWarnings("unused")
    @OnWebSocketError
    public void onError(Throwable cause) {
        connectionState = ConnectionState.DISCONNECTED;
        connectionListener.connectionError(cause);
    }

    @SuppressWarnings("unused")
    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        connectionState = ConnectionState.DISCONNECTED;
        connectionListener.connectionLost(reason);
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
