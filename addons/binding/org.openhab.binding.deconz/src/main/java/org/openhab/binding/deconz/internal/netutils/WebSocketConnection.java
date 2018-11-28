/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.deconz.internal.netutils;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.openhab.binding.deconz.internal.dto.SensorMessage;
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
    private final WebSocketConnectionListener connectionListener;
    private final Map<String, ValueUpdateListener> valueListener = new HashMap<>();
    private final Gson gson = new Gson();
    private boolean connected = false;

    public WebSocketConnection(WebSocketConnectionListener listener, WebSocketClient client) {
        this.connectionListener = listener;
        this.client = client;
    }

    public void start(String ip) {
        if (client.isRunning()) {
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
            client.stop();
        } catch (Exception e) {
            logger.debug("Error while closing connection: {}", e);
        }
        client.destroy();
    }

    public void registerValueListener(String sensorID, ValueUpdateListener listener) {
        valueListener.put(sensorID, listener);
    }

    public void unregisterValueListener(String sensorID) {
        valueListener.remove(sensorID);
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
        SensorMessage changedMessage = gson.fromJson(message, SensorMessage.class);
        ValueUpdateListener listener = valueListener.get(changedMessage.id);
        if (listener != null) {
            listener.valueUpdated(changedMessage.id, changedMessage.state);
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
