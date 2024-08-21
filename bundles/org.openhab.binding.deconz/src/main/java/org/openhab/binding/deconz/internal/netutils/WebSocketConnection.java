/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.openhab.binding.deconz.internal.dto.DeconzBaseMessage;
import org.openhab.binding.deconz.internal.types.ResourceType;
import org.openhab.core.common.ThreadPoolManager;
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
    private static final AtomicInteger INSTANCE_COUNTER = new AtomicInteger();
    private final Logger logger = LoggerFactory.getLogger(WebSocketConnection.class);
    private final ScheduledExecutorService scheduler = ThreadPoolManager.getScheduledPool("thingHandler");

    private final WebSocketClient client;
    private final String socketName;
    private final Gson gson;
    private int watchdogInterval;

    private final WebSocketConnectionListener connectionListener;
    private final Map<String, WebSocketMessageListener> listeners = new ConcurrentHashMap<>();

    private ConnectionState connectionState = ConnectionState.DISCONNECTED;
    private @Nullable ScheduledFuture<?> watchdogJob;

    private @Nullable Session session;

    public WebSocketConnection(WebSocketConnectionListener listener, WebSocketClient client, Gson gson,
            int watchdogInterval) {
        this.connectionListener = listener;
        this.client = client;
        this.client.setMaxIdleTimeout(0);
        this.gson = gson;
        this.socketName = "Websocket$" + System.currentTimeMillis() + "-" + INSTANCE_COUNTER.incrementAndGet();
        this.watchdogInterval = watchdogInterval;
    }

    public void setWatchdogInterval(int watchdogInterval) {
        this.watchdogInterval = watchdogInterval;
    }

    public void start(String ip) {
        if (connectionState == ConnectionState.CONNECTED) {
            return;
        } else if (connectionState == ConnectionState.CONNECTING) {
            logger.debug("{} already connecting", socketName);
            return;
        } else if (connectionState == ConnectionState.DISCONNECTING) {
            logger.warn("{} trying to re-connect while still disconnecting", socketName);
            return;
        }
        try {
            connectionState = ConnectionState.CONNECTING;
            URI destUri = URI.create("ws://" + ip);
            client.start();
            logger.debug("Trying to connect {} to {}", socketName, destUri);
            client.connect(this, destUri).get();
        } catch (Exception e) {
            String reason = "Error while connecting: " + e.getMessage();
            if (e.getMessage() == null) {
                logger.warn("{}: {}", socketName, reason, e);
            } else {
                logger.warn("{}: {}", socketName, reason);
            }
            connectionListener.webSocketConnectionLost(reason);
        }
    }

    private void startOrResetWatchdogTimer() {
        stopWatchdogTimer(); // stop already running timer
        watchdogJob = scheduler.schedule(
                () -> connectionListener.webSocketConnectionLost(
                        "Watchdog timed out after " + watchdogInterval + "s. Websocket seems to be dead."),
                watchdogInterval, TimeUnit.SECONDS);
    }

    private void stopWatchdogTimer() {
        ScheduledFuture<?> watchdogTimer = this.watchdogJob;
        if (watchdogTimer != null) {
            watchdogTimer.cancel(false);
            this.watchdogJob = null;
        }
    }

    /**
     * dispose the websocket (close connection and destroy client)
     *
     */
    public void dispose() {
        stopWatchdogTimer();
        try {
            connectionState = ConnectionState.DISCONNECTING;
            client.stop();
        } catch (Exception e) {
            logger.debug("{} encountered an error while closing connection", socketName, e);
        }
        client.destroy();
        connectionState = ConnectionState.DISCONNECTED;
    }

    public void registerListener(ResourceType resourceType, String sensorID, WebSocketMessageListener listener) {
        listeners.put(getListenerId(resourceType, sensorID), listener);
    }

    public void unregisterListener(ResourceType resourceType, String sensorID) {
        listeners.remove(getListenerId(resourceType, sensorID));
    }

    @SuppressWarnings("unused")
    @OnWebSocketConnect
    public void onConnect(Session session) {
        connectionState = ConnectionState.CONNECTED;
        logger.debug("{} successfully connected to {}: {}", socketName, session.getRemoteAddress().getAddress(),
                session.hashCode());
        connectionListener.webSocketConnectionEstablished();
        startOrResetWatchdogTimer();
        this.session = session;
    }

    @SuppressWarnings("unused")
    @OnWebSocketMessage
    public void onMessage(Session session, String message) {
        if (!session.equals(this.session)) {
            handleWrongSession(session, message);
            return;
        }
        startOrResetWatchdogTimer();
        logger.trace("{} received raw data: {}", socketName, message);

        try {
            DeconzBaseMessage changedMessage = Objects.requireNonNull(gson.fromJson(message, DeconzBaseMessage.class));
            if (changedMessage.r == ResourceType.UNKNOWN) {
                logger.trace("Received message has unknown resource type. Skipping message.");
                return;
            }

            ResourceType resourceType = changedMessage.r;
            String resourceId = changedMessage.id;

            if (resourceType == ResourceType.SCENES) {
                // scene recalls
                resourceType = ResourceType.GROUPS;
                resourceId = changedMessage.gid;
            }

            WebSocketMessageListener listener = listeners.get(getListenerId(resourceType, resourceId));
            if (listener == null) {
                logger.trace(
                        "Couldn't find listener for id {} with resource type {}. Either no thing for this id has been defined or this is a bug.",
                        changedMessage.id, changedMessage.r);
                return;
            }

            // we still need the original resource type here
            Class<? extends DeconzBaseMessage> expectedMessageType = changedMessage.r.getExpectedMessageType();
            if (expectedMessageType == null) {
                logger.warn(
                        "BUG! Could not get expected message type for resource type {}. Please report this incident.",
                        changedMessage.r);
                return;
            }

            DeconzBaseMessage deconzMessage = Objects.requireNonNull(gson.fromJson(message, expectedMessageType));
            listener.messageReceived(deconzMessage);
        } catch (RuntimeException e) {
            // we need to catch all processing exceptions, otherwise they could affect the connection
            logger.warn("{} encountered an error while processing the message {}: {}", socketName, message,
                    e.getMessage());
        }
    }

    @SuppressWarnings("unused")
    @OnWebSocketError
    public void onError(@Nullable Session session, Throwable cause) {
        if (session != null && !session.equals(this.session)) {
            handleWrongSession(session, "Connection error: " + cause.getMessage());
            return;
        }
        logger.warn("{} connection errored, closing: {}", socketName, cause.getMessage());

        stopWatchdogTimer();
        Session storedSession = this.session;
        if (storedSession != null && storedSession.isOpen()) {
            storedSession.close(-1, "Processing error");
        }
    }

    @SuppressWarnings("unused")
    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        if (!session.equals(this.session)) {
            handleWrongSession(session, "Connection closed: " + statusCode + " / " + reason);
            return;
        }
        logger.trace("{} closed connection: {} / {}", socketName, statusCode, reason);
        connectionState = ConnectionState.DISCONNECTED;
        stopWatchdogTimer();
        this.session = null;
        connectionListener.webSocketConnectionLost(reason);
    }

    private void handleWrongSession(Session session, String message) {
        logger.warn("{}{} received and discarded message for other or session {}: {}.", socketName, session.hashCode(),
                session.hashCode(), message);
        if (session.isOpen()) {
            // Close the session if it is still open. It should already be closed anyway
            session.close();
        }
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
     * create a unique identifier for a listener
     *
     * @param resourceType the listener resource-type (LIGHT, SENSOR, ...)
     * @param id the listener id (same as deconz-id)
     * @return a unique string for this listener
     */
    private String getListenerId(ResourceType resourceType, String id) {
        return resourceType.name() + "$" + id;
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
