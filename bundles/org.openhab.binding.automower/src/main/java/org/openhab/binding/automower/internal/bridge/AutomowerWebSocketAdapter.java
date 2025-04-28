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
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The {@link AutomowerWebSocketAdapter} handles the WebSocket Connection of the Husqvarna web service.
 *
 * @author MikeTheTux - Initial contribution
 */
@WebSocket
public class AutomowerWebSocketAdapter {
    private final AutomowerBridgeHandler handler;
    private final Logger logger = LoggerFactory.getLogger(AutomowerWebSocketAdapter.class);
    private @Nullable ScheduledFuture<?> connectionTracker;
    private int unansweredPings = 0;
    private static final int MAX_UNANSWERED_PINGS = 5;

    private ByteBuffer pingPayload = ByteBuffer.wrap("ping".getBytes(StandardCharsets.UTF_8));
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public AutomowerWebSocketAdapter(AutomowerBridgeHandler handler) {
        this.handler = handler;
    }

    @OnWebSocketConnect
    public synchronized void onConnect(Session session) {
        handler.setClosing(false);
        unansweredPings = 0;

        logger.debug("Connected to Husqvarna Webservice ({})", session.getRemoteAddress().getHostString());
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
        // logger.debug("Message from Server: {}", message);
        try {
            JsonNode node = OBJECT_MAPPER.readTree(message);
            String id = node.has("id") ? node.get("id").asText() : null;
            if (id != null) {
                AutomowerHandler automowerHandler = handler.getAutomowerHandlerByThingId(id);
                if (automowerHandler != null) {
                    logger.trace("Message from WebSocket for AutomowerHandler: {}", message);
                    // automowerHandler.processWebSocketMessage(message);
                } else {
                    logger.trace("No AutomowerHandler found for id: {}", id);
                }
            } else {
                logger.warn("Received message without id: {}", message);
            }
        } catch (Exception e) {
            logger.error("Failed to process message: {}", e.getMessage());
        }

        /*
         * StringBuilder logging = new StringBuilder(
         * "Message from Server: " + message + ", known Automower: " + automowerHandlers.size());
         * automowerHandlers.forEach(automowerHandler -> {
         * if (message.contains(automowerHandler.getThing().getUID().getId())) {
         * // logger.debug("Message from Server for Automower {}", automowerHandler.getThing().getUID().getId());
         * // automowerHandler.processWebSocketMessage(message);
         * logging.append(", mower: ").append(automowerHandler.getThing().getUID().getId());
         * }
         * });
         * logger.debug("{}", logging.toString());
         */
        /*
         * try {
         * bridge.getAutomowers().getData().forEach(automower -> {
         * if (message.contains(automower.getId())) {
         * logger.debug("Message from Server for Automower {}", automower.getId());
         * }
         * });
         * } catch (AutomowerCommunicationException e) {
         * logger.error("Failed to process message due to communication error: {}", e.getMessage());
         * }
         */
    }

    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        logger.info("WebSocket closed: {} - {}", statusCode, reason);

        // Cancel ping task on disconnect
        final ScheduledFuture<?> connectionTracker = this.connectionTracker;
        if (connectionTracker != null) {
            logger.trace("Cancelling connectionTracker (ping)");
            connectionTracker.cancel(true);
        }

        if (!handler.isClosing()) {
            try {
                restart();
            } catch (Exception e) {
                logger.error("Failed to restart WebSocket client: {}", e.getMessage());
            }
        }
    }

    public void restart() throws Exception {
        String accessToken = handler.authenticate().getAccessToken();
        if (accessToken == null) {
            logger.error("No OAuth2 access token available for WebSocket connection");
            return;
        }
        logger.debug("Reconnecting to Husqvarna Webservice ()");
        String wsUrl = "wss://ws.openapi.husqvarna.dev/v1";
        org.eclipse.jetty.websocket.client.ClientUpgradeRequest request = new org.eclipse.jetty.websocket.client.ClientUpgradeRequest();
        request.setHeader("Authorization", "Bearer " + accessToken);
        handler.setWebSocketSession(
                (WebSocketSession) handler.getWebSocketClient().connect(this, URI.create(wsUrl), request).get());
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
            String accessToken = handler.authenticate().getAccessToken();
            if (unansweredPings > MAX_UNANSWERED_PINGS || accessToken == null) {
                handler.getWebSocketSession().close(1000, "Timeout manually closing dead connection");
            } else {
                if (handler.getWebSocketSession().isOpen()) {
                    try {
                        // logger.trace("Sending ping ...");
                        handler.getWebSocketSession().getRemote().sendPing(pingPayload);
                        ++unansweredPings;
                    } catch (IOException ex) {
                        logger.debug("Error while sending ping: {}", ex.getMessage());
                    }
                }
            }
        } catch (AutomowerCommunicationException e) {
            logger.error("Failed to authenticate while sending keep-alive ping: {}", e.getMessage());
        }
    }
}
