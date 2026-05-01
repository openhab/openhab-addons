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
package org.openhab.binding.homeconnectdirect.internal.service.websocket;

import static org.eclipse.jetty.websocket.api.extensions.Frame.Type.PING;
import static org.eclipse.jetty.websocket.api.extensions.Frame.Type.PONG;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.WS_INACTIVITY_CHECK_INITIAL_DELAY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.WS_INACTIVITY_CHECK_INTERVAL;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.WS_INACTIVITY_TIMEOUT;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.WS_PING_INITIAL_DELAY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.WS_PING_INTERVAL;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.time.Duration;
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
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.api.extensions.Frame;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for WebSocket client services.
 *
 * @author Jonas Brüstel - Initial contribution
 */
@WebSocket
@NonNullByDefault
public abstract class AbstractWebSocketClientService implements WebSocketClientService {

    private final Logger logger;
    private final WebSocketHandler webSocketHandler;
    private final ScheduledExecutorService scheduler;
    private final URI applianceUri;
    private final Thing thing;

    private @Nullable Instant lastMessageReceived;
    private @Nullable Session session;
    private @Nullable ScheduledFuture<?> pingFuture;
    private @Nullable ScheduledFuture<?> staleConnectionFuture;
    private @Nullable WebSocketClient webSocketClient;

    public AbstractWebSocketClientService(Thing thing, URI applianceUri, WebSocketHandler webSocketHandler,
            ScheduledExecutorService scheduler) {
        logger = LoggerFactory.getLogger(AbstractWebSocketClientService.class);
        this.webSocketHandler = webSocketHandler;
        this.scheduler = scheduler;
        this.applianceUri = applianceUri;
        this.thing = thing;
    }

    @Override
    public void connect() {
        logger.debug("Connecting to {} ({}).", applianceUri, getThingUID());
        try {
            var webSocketClient = getWebSocketClient();
            if (webSocketClient != null) {
                webSocketClient.start();
                webSocketClient.connect(this, applianceUri).get();
            }
        } catch (Exception ignored) {
            // best-effort connect, errors are handled by the connection check mechanism
        }
    }

    @Override
    public void dispose() {
        stopConnectionChecks();
        var webSocketClient = getWebSocketClient();
        if (webSocketClient != null) {
            try {
                logger.debug("Stop web socket client ({}).", applianceUri);
                webSocketClient.stop();
            } catch (Exception ignored) {
                // best-effort stop during cleanup
            }

            logger.debug("Destroy web socket client ({}).", applianceUri);
            webSocketClient.destroy();
        }
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        setSession(session);
        getWebSocketHandler().onWebSocketConnect();
        updateLastMessageReceived();
        startConnectionChecks(session);
    }

    @OnWebSocketFrame
    public void onFrame(Session session, Frame frame) {
        if (PONG.equals(frame.getType())) {
            logger.trace("<< PONG ({})", thing.getUID());
        } else if (PING.equals(frame.getType())) {
            logger.trace("<< PING ({})", thing.getUID());
            sendPong(session);
        }

        updateLastMessageReceived();
    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        logger.debug("Closed websocket connection. status={}, reason={} thingUID={}", statusCode, reason,
                thing.getUID());
        stopConnectionChecks();
        getWebSocketHandler().onWebSocketClose();
    }

    @OnWebSocketError
    public void onError(@Nullable Session session, Throwable error) throws Exception {
        stopConnectionChecks();
        getWebSocketHandler().onWebSocketError(error);

        if (session == null || !session.isOpen()) {
            getWebSocketHandler().onWebSocketClose();
        }
    }

    protected WebSocketHandler getWebSocketHandler() {
        return webSocketHandler;
    }

    protected @Nullable Instant getLastMessageReceived() {
        return lastMessageReceived;
    }

    protected void updateLastMessageReceived() {
        lastMessageReceived = Instant.now();
    }

    protected @Nullable Session getSession() {
        return session;
    }

    protected void setSession(Session session) {
        this.session = session;
    }

    protected ThingUID getThingUID() {
        return thing.getUID();
    }

    protected @Nullable WebSocketClient getWebSocketClient() {
        return webSocketClient;
    }

    protected void setWebSocketClient(WebSocketClient webSocketClient) {
        this.webSocketClient = webSocketClient;
    }

    protected void sendPong(@Nullable Session session) {
        if (session != null && session.isOpen()) {
            try {
                session.getRemote().sendPong(ByteBuffer.allocate(0));
                logger.trace(">> PONG ({})", thing.getUID());
            } catch (IOException e) {
                logger.warn("Could not send PONG! error={} thingUID={}", e.getMessage(), thing.getUID());
            }
        }
    }

    protected void sendPing(@Nullable Session session) {
        if (session != null && session.isOpen()) {
            try {
                session.getRemote().sendPing(ByteBuffer.allocate(0));
                logger.trace(">> PING ({})", thing.getUID());
            } catch (IOException e) {
                logger.warn("Could not send PING! error={} thingUID={}", e.getMessage(), thing.getUID());
            }
        }
    }

    protected synchronized void startConnectionChecks(Session session) {
        pingFuture = scheduler.scheduleWithFixedDelay(() -> sendPing(session), WS_PING_INITIAL_DELAY.toSeconds(),
                WS_PING_INTERVAL.toSeconds(), TimeUnit.SECONDS);

        staleConnectionFuture = scheduler.scheduleWithFixedDelay(() -> {
            if (session.isOpen()) {
                var lastMessage = getLastMessageReceived();
                var now = Instant.now();
                if (lastMessage != null && now.isAfter(lastMessage.plus(WS_INACTIVITY_TIMEOUT))) {
                    logger.debug("Last message received {} seconds ago. -> reconnect. (thingUID={})",
                            Duration.between(lastMessage, now).toSeconds(), thing.getUID());
                    try {
                        session.disconnect();
                    } catch (IOException e) {
                        logger.error("Could not disconnect from session! error={}", e.getMessage());
                    }
                }
            }
        }, WS_INACTIVITY_CHECK_INITIAL_DELAY.toSeconds(), WS_INACTIVITY_CHECK_INTERVAL.toSeconds(), TimeUnit.SECONDS);
    }

    protected synchronized void stopConnectionChecks() {
        ScheduledFuture<?> pingFuture = this.pingFuture;
        if (pingFuture != null) {
            pingFuture.cancel(true);
        }

        ScheduledFuture<?> staleConnectionFuture = this.staleConnectionFuture;
        if (staleConnectionFuture != null) {
            staleConnectionFuture.cancel(true);
        }
    }
}
