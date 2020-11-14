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
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.openhab.binding.deconz.internal.dto.DeconzBaseMessage;
import org.openhab.binding.deconz.internal.dto.GroupMessage;
import org.openhab.binding.deconz.internal.dto.LightMessage;
import org.openhab.binding.deconz.internal.dto.SensorMessage;
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
    private static final Map<ResourceType, Class<? extends DeconzBaseMessage>> EXPECTED_MESSAGE_TYPES = Map.of(
            ResourceType.GROUPS, GroupMessage.class, ResourceType.LIGHTS, LightMessage.class, ResourceType.SENSORS,
            SensorMessage.class);

    private final Logger logger = LoggerFactory.getLogger(WebSocketConnection.class);

    private final WebSocketClient client;
    private final WebSocketConnectionListener connectionListener;
    private final Map<Map.Entry<ResourceType, String>, WebSocketMessageListener> listeners = new ConcurrentHashMap<>();

    private final Gson gson;
    private boolean connected = false;

    public WebSocketConnection(WebSocketConnectionListener listener, WebSocketClient client, Gson gson) {
        this.connectionListener = listener;
        this.client = client;
        this.client.setMaxIdleTimeout(0);
        this.gson = gson;
    }

    public void start(String ip) {
        if (connected) {
            return;
        }
        try {
            URI destUri = URI.create("ws://" + ip);

            client.start();

            logger.debug("Connecting to: {}", destUri);
            client.connect(this, destUri).get();
        } catch (Exception e) {
            connectionListener.connectionError(e);
        }
    }

    public void close() {
        try {
            connected = false;
            client.stop();
        } catch (Exception e) {
            logger.debug("Error while closing connection", e);
        }
        client.destroy();
    }

    public void registerListener(ResourceType resourceType, String sensorID, WebSocketMessageListener listener) {
        listeners.put(Map.entry(resourceType, sensorID), listener);
    }

    public void unregisterListener(ResourceType resourceType, String sensorID) {
        listeners.remove(Map.entry(resourceType, sensorID));
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        connected = true;
        logger.debug("Connect: {}", session.getRemoteAddress().getAddress());
        connectionListener.connectionEstablished();
    }

    @SuppressWarnings("null")
    @OnWebSocketMessage
    public void onMessage(String message) {
        logger.trace("Raw data received by websocket: {}", message);

        DeconzBaseMessage changedMessage = gson.fromJson(message, DeconzBaseMessage.class);
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

        Class<? extends DeconzBaseMessage> expectedMessageType = EXPECTED_MESSAGE_TYPES.get(changedMessage.r);
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

    @OnWebSocketError
    public void onError(Throwable cause) {
        connected = false;
        connectionListener.connectionError(cause);
    }

    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        connected = false;
        connectionListener.connectionLost(reason);
    }

    public boolean isConnected() {
        return connected;
    }
}
