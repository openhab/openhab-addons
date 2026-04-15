/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.ntfy.internal.network;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.openhab.binding.ntfy.internal.models.BaseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * WebSocket client implementation used to receive events from the ntfy server.
 * The class forwards lifecycle and message events to the configured
 * {@link WebSocketConnectionListener}.
 *
 * @author Christian Kittel - Initial contribution
 */
@WebSocket
@NonNullByDefault
public class NtfyWebSocket {

    private final Logger logger = LoggerFactory.getLogger(NtfyWebSocket.class);

    private WebSocketConnectionListener listener;

    /**
     * Creates a new WebSocket wrapper forwarding events to the provided listener.
     *
     * @param listener the listener to notify about connection and message events
     */
    public NtfyWebSocket(WebSocketConnectionListener listener) {
        this.listener = listener;
    }

    /**
     * Jetty callback invoked when the websocket connection has been established.
     *
     * @param session the websocket session
     */
    @OnWebSocketConnect
    public void onConnect(Session session) {
        listener.connectionEstablished();
        logger.debug("Connected to: {}", session.getRemoteAddress().getAddress());
    }

    /**
     * Jetty callback invoked for incoming text messages. The JSON payload is
     * deserialized and forwarded to the configured listener.
     *
     * @param message the JSON message payload
     */
    @OnWebSocketMessage
    public void onMessage(String message) {
        logger.debug("Message from Server: {}", message);

        try {
            BaseEvent fromJson = GsonDeserializer.deserialize(message);
            if (fromJson != null) {
                listener.messageReceived(fromJson);
            }
        } catch (RuntimeException e) {
            logger.warn("Failed to deserialize websocket message: {}", message, e);
            listener.connectionError(e);
        }
    }

    /**
     * Jetty callback invoked when an error occurs on the websocket connection.
     *
     * @param cause the underlying error
     */
    @OnWebSocketError
    public void onError(Throwable cause) {
        listener.connectionError(cause);
        logger.debug("Connection failed: {}", cause.getMessage());
    }

    /**
     * Jetty callback invoked when the websocket connection is closed.
     *
     * @param statusCode the close status code
     * @param reason the reason message for the close
     */
    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        listener.connectionLost(reason);
        logger.debug("WebSocket Closed. Code: {}; Reason: {}", statusCode, reason);
    }
}
