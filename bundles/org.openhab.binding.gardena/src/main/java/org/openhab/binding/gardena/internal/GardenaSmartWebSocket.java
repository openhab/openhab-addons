/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.gardena.internal;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ScheduledExecutorService;
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
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.eclipse.jetty.websocket.common.WebSocketSession;
import org.eclipse.jetty.websocket.common.frames.PongFrame;
import org.openhab.binding.gardena.internal.model.dto.api.PostOAuth2Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link GardenaSmartWebSocket} implements the websocket for receiving constant updates from the Gardena smart
 * system.
 *
 * @author Gerhard Riegler - Initial contribution
 */
@NonNullByDefault
@WebSocket
public class GardenaSmartWebSocket {
    private final Logger logger = LoggerFactory.getLogger(GardenaSmartWebSocket.class);
    private final GardenaSmartWebSocketListener socketEventListener;
    private static final int MAX_UNANSWERED_PINGS = 5;

    private WebSocketSession session;
    private WebSocketClient webSocketClient;
    private boolean closing;
    private int unansweredPings = 0;
    private ScheduledExecutorService scheduler;
    private @Nullable ScheduledFuture<?> connectionTracker;
    private ByteBuffer pingPayload = ByteBuffer.wrap("ping".getBytes(StandardCharsets.UTF_8));
    private @Nullable PostOAuth2Response token;
    private String socketId;
    private String locationID;

    /**
     * Starts the websocket session.
     */
    public GardenaSmartWebSocket(GardenaSmartWebSocketListener socketEventListener, WebSocketClient webSocketClient,
            ScheduledExecutorService scheduler, String url, @Nullable PostOAuth2Response token, String socketId,
            String locationID) throws Exception {
        this.socketEventListener = socketEventListener;
        this.webSocketClient = webSocketClient;
        this.scheduler = scheduler;
        this.token = token;
        this.socketId = socketId;
        this.locationID = locationID;

        session = (WebSocketSession) webSocketClient.connect(this, new URI(url)).get();
        logger.debug("Connecting to Gardena Webservice ({})", socketId);
    }

    /**
     * Stops the websocket session.
     */
    public synchronized void stop() {
        closing = true;
        final ScheduledFuture<?> connectionTracker = this.connectionTracker;
        if (connectionTracker != null) {
            connectionTracker.cancel(true);
        }

        logger.debug("Closing Gardena Webservice ({})", socketId);
        try {
            session.close();
        } catch (Exception ex) {
            // ignore
        }
    }

    public boolean isClosing() {
        return this.closing;
    }

    public String getSocketID() {
        return this.socketId;
    }

    public String getLocationID() {
        return this.locationID;
    }

    public void restart(String newUrl) throws Exception {
        logger.debug("Reconnecting to Gardena Webservice ({})", socketId);
        session = (WebSocketSession) webSocketClient.connect(this, new URI(newUrl)).get();
    }

    @OnWebSocketConnect
    public synchronized void onConnect(Session session) {
        closing = false;
        unansweredPings = 0;
        logger.debug("Connected to Gardena Webservice ({})", socketId);

        ScheduledFuture<?> connectionTracker = this.connectionTracker;
        if (connectionTracker != null && !connectionTracker.isCancelled()) {
            connectionTracker.cancel(true);
        }

        // start sending PING every two minutes
        this.connectionTracker = scheduler.scheduleWithFixedDelay(this::sendKeepAlivePing, 1, 2, TimeUnit.MINUTES);
    }

    @OnWebSocketFrame
    public synchronized void onFrame(Frame pong) {
        if (pong instanceof PongFrame) {
            unansweredPings = 0;
            logger.trace("Pong received ({})", socketId);
        }
    }

    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        logger.debug("Connection to Gardena Webservice was closed ({}): code: {}, reason: {}", socketId, statusCode,
                reason);

        if (!closing) {
            // let listener handle restart of socket
            socketEventListener.onWebSocketClose(locationID);
        }
    }

    @OnWebSocketError
    public void onError(Throwable cause) {
        logger.debug("Gardena Webservice error ({})", socketId, cause); // log whole stack trace

        if (!closing) {
            // let listener handle restart of socket
            socketEventListener.onWebSocketError(locationID);
        }
    }

    @OnWebSocketMessage
    public void onMessage(String msg) {
        if (!closing) {
            logger.trace("<<< event ({}): {}", socketId, msg);
            socketEventListener.onWebSocketMessage(msg);
        }
    }

    /**
     * Sends a ping to tell the Gardena smart system that the client is alive.
     */
    private synchronized void sendKeepAlivePing() {
        final PostOAuth2Response accessToken = token;
        if (unansweredPings > MAX_UNANSWERED_PINGS || accessToken == null || accessToken.isAccessTokenExpired()) {
            session.close(1000, "Timeout manually closing dead connection (" + socketId + ")");
        } else {
            if (session.isOpen()) {
                try {
                    logger.trace("Sending ping ({})", socketId);
                    session.getRemote().sendPing(pingPayload);
                    ++unansweredPings;
                } catch (IOException ex) {
                    logger.debug("Error while sending ping: {}", ex.getMessage());
                }
            }
        }
    }
}
