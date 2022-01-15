/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
import java.time.Instant;
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
import org.openhab.binding.gardena.internal.config.GardenaConfig;
import org.openhab.binding.gardena.internal.model.dto.api.PostOAuth2Response;
import org.openhab.binding.gardena.internal.model.dto.api.WebSocketCreatedResponse;
import org.openhab.core.io.net.http.WebSocketFactory;
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
    private final long WEBSOCKET_IDLE_TIMEOUT = 300;

    private WebSocketSession session;
    private WebSocketClient webSocketClient;
    private boolean closing;
    private Instant lastPong = Instant.now();
    private ScheduledExecutorService scheduler;
    private @Nullable ScheduledFuture<?> connectionTracker;
    private ByteBuffer pingPayload = ByteBuffer.wrap("ping".getBytes(StandardCharsets.UTF_8));
    private @Nullable PostOAuth2Response token;
    private String socketId;

    /**
     * Starts the websocket session.
     */
    public GardenaSmartWebSocket(GardenaSmartWebSocketListener socketEventListener,
            WebSocketCreatedResponse webSocketCreatedResponse, GardenaConfig config, ScheduledExecutorService scheduler,
            WebSocketFactory webSocketFactory, @Nullable PostOAuth2Response token, String socketId) throws Exception {
        this.socketEventListener = socketEventListener;
        this.scheduler = scheduler;
        this.token = token;
        this.socketId = socketId;

        String webSocketId = String.valueOf(hashCode());
        webSocketClient = webSocketFactory.createWebSocketClient(webSocketId);
        webSocketClient.setConnectTimeout(config.getConnectionTimeout() * 1000L);
        webSocketClient.setStopTimeout(3000);
        webSocketClient.setMaxIdleTimeout(150000);
        webSocketClient.start();

        logger.debug("Connecting to Gardena Webservice ({})", socketId);
        session = (WebSocketSession) webSocketClient
                .connect(this, new URI(webSocketCreatedResponse.data.attributes.url)).get();
        session.setStopTimeout(3000);
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
        if (isRunning()) {
            logger.debug("Closing Gardena Webservice client ({})", socketId);
            try {
                session.close();
            } catch (Exception ex) {
                // ignore
            } finally {
                try {
                    webSocketClient.stop();
                } catch (Exception e) {
                    // ignore
                }
            }
        }
    }

    /**
     * Returns true, if the websocket is running.
     */
    public synchronized boolean isRunning() {
        return session.isOpen();
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        closing = false;
        logger.debug("Connected to Gardena Webservice ({})", socketId);

        connectionTracker = scheduler.scheduleWithFixedDelay(this::sendKeepAlivePing, 2, 2, TimeUnit.MINUTES);
    }

    @OnWebSocketFrame
    public void onFrame(Frame pong) {
        if (pong instanceof PongFrame) {
            lastPong = Instant.now();
            logger.trace("Pong received ({})", socketId);
        }
    }

    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        if (!closing) {
            logger.debug("Connection to Gardena Webservice was closed ({}): code: {}, reason: {}", socketId, statusCode,
                    reason);
            socketEventListener.onWebSocketClose();
        }
    }

    @OnWebSocketError
    public void onError(Throwable cause) {
        if (!closing) {
            logger.warn("Gardena Webservice error ({}): {}, restarting", socketId, cause.getMessage());
            logger.debug("{}", cause.getMessage(), cause);
            socketEventListener.onWebSocketError();
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
    private void sendKeepAlivePing() {
        try {
            logger.trace("Sending ping ({})", socketId);
            session.getRemote().sendPing(pingPayload);
            final PostOAuth2Response accessToken = token;
            if ((Instant.now().getEpochSecond() - lastPong.getEpochSecond() > WEBSOCKET_IDLE_TIMEOUT)
                    || accessToken == null || accessToken.isAccessTokenExpired()) {
                session.close(1000, "Timeout manually closing dead connection (" + socketId + ")");
            }
        } catch (IOException ex) {
            logger.debug("{}", ex.getMessage());
        }
    }
}
