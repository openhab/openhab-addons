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
package org.openhab.binding.gardena.internal;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import org.eclipse.jetty.websocket.api.extensions.Frame;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.eclipse.jetty.websocket.common.WebSocketSession;
import org.eclipse.jetty.websocket.common.frames.PongFrame;
import org.eclipse.smarthome.io.net.http.WebSocketFactory;
import org.openhab.binding.gardena.internal.config.GardenaConfig;
import org.openhab.binding.gardena.internal.model.api.Location;
import org.openhab.binding.gardena.internal.model.api.PostOAuth2Response;
import org.openhab.binding.gardena.internal.model.api.WebSocketCreatedResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link GardenaSmartWebSocket} implements the websocket for receiving constant updates from the Gardena smart
 * system.
 *
 * @author Gerhard Riegler - Initial contribution
 */
@WebSocket
public class GardenaSmartWebSocket {
    private final Logger logger = LoggerFactory.getLogger(GardenaSmartWebSocket.class);
    private final GardenaSmartWebSocketListener socketEventListener;
    private final static long WEBSOCKET_IDLE_TIMEOUT = 300;

    private WebSocketSession session;
    private WebSocketClient webSocketClient;
    private boolean closing;
    private Instant lastPong = Instant.now();
    private ScheduledExecutorService scheduler;
    private ScheduledFuture connectionTrackerFuture;
    private ByteBuffer pingPayload = ByteBuffer.wrap("ping".getBytes());
    private PostOAuth2Response token;
    private Location location;

    /**
     * Starts the websocket session.
     */
    public GardenaSmartWebSocket(GardenaSmartWebSocketListener socketEventListener,
            WebSocketCreatedResponse webSocketCreatedResponse, GardenaConfig config, ScheduledExecutorService scheduler,
            WebSocketFactory webSocketFactory, PostOAuth2Response token, Location location) throws Exception {
        this.socketEventListener = socketEventListener;
        this.scheduler = scheduler;
        this.token = token;
        this.location = location;

        webSocketClient = webSocketFactory.createWebSocketClient(String.valueOf(this.getClass().hashCode()));
        webSocketClient.setConnectTimeout(config.getConnectionTimeout() * 1000L);
        webSocketClient.setStopTimeout(3000);
        webSocketClient.setMaxIdleTimeout(150000);
        webSocketClient.start();

        logger.debug("Connecting to Gardena Webservice ({})", location.name);
        session = (WebSocketSession) webSocketClient
                .connect(this, new URI(webSocketCreatedResponse.data.attributes.url)).get();
        session.setStopTimeout(3000);
    }

    /**
     * Stops the websocket session.
     */
    public synchronized void stop() {
        closing = true;
        if (connectionTrackerFuture != null) {
            connectionTrackerFuture.cancel(true);
        }
        if (isRunning()) {
            logger.debug("Closing Gardena Webservice client ({})", location.name);
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
        return session != null && session.isOpen();
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        closing = false;
        logger.debug("Connected to Gardena Webservice ({})", location.name);

        connectionTrackerFuture = scheduler.scheduleAtFixedRate(new ConnectionTrackerThread(), 2, 2, TimeUnit.MINUTES);
    }

    @OnWebSocketFrame
    public void onFrame(Frame pong) {
        if (pong instanceof PongFrame) {
            lastPong = Instant.now();
            logger.trace("Pong received ({})", location.name);
        }
    }

    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        if (!closing) {
            logger.debug("Connection to Gardena Webservice was closed ({}): code: {}, reason: {}", location.name,
                    statusCode, reason);
            socketEventListener.onWebSocketClose();
        }
    }

    @OnWebSocketError
    public void onError(Throwable cause) {
        if (!closing) {
            logger.warn("Gardena Webservice error ({}): {}, restarting", location.name, cause.getMessage());
            logger.debug("{}", cause.getMessage(), cause);
            socketEventListener.onWebSocketError();
        }
    }

    @OnWebSocketMessage
    public void onMessage(String msg) {
        if (!closing) {
            logger.trace("<<< event ({}): {}", location.name, msg);
            socketEventListener.onWebSocketMessage(msg);
        }
    }

    /**
     * Sends a ping in regular interval to tell the Gardena smart system that the client is alive.
     */
    private class ConnectionTrackerThread implements Runnable {

        @Override
        public void run() {
            try {
                logger.trace("Sending ping ({})", location.name);
                session.getRemote().sendPing(pingPayload);

                if ((Instant.now().getEpochSecond() - lastPong.getEpochSecond() > WEBSOCKET_IDLE_TIMEOUT)
                        || token.isAccessTokenExpired()) {
                    session.close(1000, "Timeout manually closing dead connection (" + location.name + ")");
                }
            } catch (IOException ex) {
                logger.error("{}", ex.getMessage(), ex);
            }
        }
    }
}
