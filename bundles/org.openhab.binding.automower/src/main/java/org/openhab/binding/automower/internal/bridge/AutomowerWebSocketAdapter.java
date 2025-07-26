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

package org.openhab.binding.automower.internal.bridge;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketFrame;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.api.extensions.Frame;
import org.eclipse.jetty.websocket.common.WebSocketSession;
import org.eclipse.jetty.websocket.common.frames.PongFrame;
import org.openhab.binding.automower.internal.rest.exceptions.AutomowerCommunicationException;
import org.openhab.binding.automower.internal.things.AutomowerHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * The {@link AutomowerWebSocketAdapter} handles the WebSocket Connection of the Husqvarna web service.
 *
 * @author MikeTheTux - Initial contribution
 */
@NonNullByDefault
@WebSocket
public class AutomowerWebSocketAdapter {
    private final AutomowerBridgeHandler handler;
    private final AutomowerBridge bridge;
    private final Logger logger = LoggerFactory.getLogger(AutomowerWebSocketAdapter.class);
    private @Nullable ScheduledFuture<?> connectionTracker;
    private int unansweredPings = 0;
    private static final int MAX_UNANSWERED_PINGS = 5;

    private ByteBuffer pingPayload = ByteBuffer.wrap("ping".getBytes(StandardCharsets.UTF_8));

    public AutomowerWebSocketAdapter(AutomowerBridgeHandler handler, AutomowerBridge bridge) {
        this.handler = handler;
        this.bridge = bridge;
    }

    @OnWebSocketConnect
    public synchronized void onConnect(Session session) {
        handler.setClosing(false);
        unansweredPings = 0;

        logger.debug("Connected to Husqvarna WebSocket ({})", session.getRemoteAddress().getHostString());

        // Initialize MowerStatus via polling the REST API
        logger.debug("Polling Automowers for initial state / after reconnect of WebSocket");
        // Poll all automowers to get their initial state
        // This is necessary because the WebSocket does not provide the initial state
        // and we need to have the initial state before subscribing to the WebSocket messages
        // This will also recover potential lost WebSocket messages during WebSocket reconnect
        handler.pollAutomowers(bridge);

        // Subscribe to all messages after connecting
        try {
            String subscribeAllMessage = "{\"type\":\"subscribe\",\"topics\":[\"*\"]}";
            session.getRemote().sendString(subscribeAllMessage);
            logger.debug("Sent subscription message to subscribe to all topics");
        } catch (Exception e) {
            logger.error("Failed to send subscription message: {}", e.getMessage());
        }

        // Cancel previous ping task if it exists
        ScheduledFuture<?> connectionTracker = this.connectionTracker;
        if (connectionTracker != null && !connectionTracker.isCancelled()) {
            logger.trace("Cancelling previous connectionTracker (ping)");
            connectionTracker.cancel(true);
        }
        logger.trace("Starting connectionTracker (ping)");
        // start sending PING every minute
        this.connectionTracker = handler.getScheduler().scheduleWithFixedDelay(this::sendKeepAlivePing, 1, 1,
                TimeUnit.MINUTES);
    }

    @OnWebSocketFrame
    public synchronized void onFrame(Frame pong) {
        if (pong instanceof PongFrame) {
            unansweredPings = 0;
            // logger.trace("Pong received");
        }
    }

    @OnWebSocketMessage
    public void onMessage(String message) {
        try {
            if (!message.isBlank()) {
                JsonObject event = JsonParser.parseString(message).getAsJsonObject();
                if (event.has("id")) {
                    String id = event.get("id").getAsString();
                    AutomowerHandler automowerHandler = handler.getAutomowerHandlerByThingId(id);
                    if (automowerHandler != null) {
                        logger.debug("Message from WebSocket for known AutomowerHandler: {}", message);
                        automowerHandler.processWebSocketMessage(event);
                    } else {
                        logger.debug("Message from WebSocket for unknown AutomowerHandler: {}", message);
                    }
                } else {
                    logger.trace("Message from WebSocket without Id: {}", message);
                }
            } else {
                logger.trace("Received empty message from WebSocket");
            }
        } catch (Exception e) {
            logger.error("Failed to process WebSocket message: {}", e.getMessage());
        }
    }

    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        logger.debug("WebSocket closed: {} - {}", statusCode, reason);

        // Cancel ping task on disconnect
        final ScheduledFuture<?> connectionTracker = this.connectionTracker;
        if (connectionTracker != null) {
            logger.trace("Cancelling connectionTracker (ping)");
            connectionTracker.cancel(true);
        }

        if (!handler.isClosing()) {
            try {
                logger.debug("Reconnecting to Husqvarna Webservice ()");
                handler.connectWebSocket(this);
            } catch (Exception e) {
                logger.error("Failed to restart WebSocket client: {}", e.getMessage());
            }
        }
    }

    @OnWebSocketError
    public void onError(Throwable cause) {
        logger.error("WebSocket error: {}", cause.getMessage());
    }

    /**
     * Sends a ping to tell the Husqvarna smart system that the client is alive.
     */
    private synchronized void sendKeepAlivePing() {
        try {
            WebSocketSession webSocketSession = handler.getWebSocketSession();
            if (webSocketSession != null) {
                String accessToken = bridge.authenticate().getAccessToken();
                if (unansweredPings > MAX_UNANSWERED_PINGS || accessToken == null) {
                    webSocketSession.close(1000, "Timeout: manually closing dead connection");
                } else {
                    if (webSocketSession.isOpen()) {
                        try {
                            // logger.trace("Sending ping ...");
                            webSocketSession.getRemote().sendPing(pingPayload);
                            ++unansweredPings;
                        } catch (IOException ex) {
                            logger.warn("Error while sending ping: {}", ex.getMessage());
                        }
                    }
                }
            } else {
                logger.warn("WebSocket session is null, cannot send ping");
            }
        } catch (AutomowerCommunicationException e) {
            logger.error("Failed to authenticate while sending keep-alive ping: {}", e.getMessage());
        }
    }
}
