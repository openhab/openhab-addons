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
package org.openhab.binding.livisismarthome.internal;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.ExecutionException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.openhab.binding.livisismarthome.internal.client.exception.WebSocketConnectException;
import org.openhab.binding.livisismarthome.internal.listener.EventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link LivisiWebSocket} implements the websocket for receiving constant updates
 * from the LIVISI SmartHome web service.
 *
 * @author Oliver Kuhl - Initial contribution
 * @author Sven Strohschein - Renamed from Innogy to Livisi
 */
@NonNullByDefault
@WebSocket
public class LivisiWebSocket {

    private final Logger logger = LoggerFactory.getLogger(LivisiWebSocket.class);

    private final HttpClient httpClient;
    private final EventListener eventListener;
    private final URI webSocketURI;
    private final int maxIdleTimeout;

    private WebSocketClient client;
    private @Nullable Session session;
    private boolean closing;

    /**
     * Constructs the {@link LivisiWebSocket}.
     *
     * @param eventListener the responsible
     *            {@link org.openhab.binding.livisismarthome.internal.handler.LivisiBridgeHandler}
     * @param webSocketURI the {@link URI} of the websocket endpoint
     * @param maxIdleTimeout max idle timeout
     */
    public LivisiWebSocket(HttpClient httpClient, EventListener eventListener, URI webSocketURI, int maxIdleTimeout) {
        this.httpClient = httpClient;
        this.eventListener = eventListener;
        this.webSocketURI = webSocketURI;
        this.maxIdleTimeout = maxIdleTimeout;
        this.client = createWebSocketClient();
    }

    /**
     * Starts the {@link LivisiWebSocket}.
     */
    public synchronized void start() throws WebSocketConnectException {
        if (client.isStopped()) {
            client = createWebSocketClient();
            startWebSocketClient(client);
        }

        session = connectWebSocket(session);
    }

    private Session connectWebSocket(@Nullable Session session) throws WebSocketConnectException {
        closeSession(session);
        this.session = null;

        logger.debug("Connecting to LIVISI SmartHome WebSocket...");
        try {
            return client.connect(this, webSocketURI).get();
        } catch (IOException | InterruptedException | ExecutionException e) {
            throw new WebSocketConnectException("The WebSocket couldn't get connected!", e);
        }
    }

    /**
     * Stops the {@link LivisiWebSocket}.
     */
    public synchronized void stop() {
        this.closing = true;
        if (isRunning()) {
            closeSession(session);
        } else {
            logger.trace("Stopping websocket ignored - was not running.");
        }
        session = null;
        stopWebSocketClient(client);
        client = createWebSocketClient();
    }

    private void closeSession(@Nullable Session session) {
        if (session != null) {
            logger.debug("Closing session...");
            session.close();
        }
    }

    /**
     * Return true, if the websocket is running.
     *
     * @return true if the websocket is running, otherwise false
     */
    public synchronized boolean isRunning() {
        return isRunning(session);
    }

    private boolean isRunning(@Nullable Session session) {
        return session != null && session.isOpen();
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        this.closing = false;
        logger.debug("Connected to LIVISI SmartHome webservice.");
        logger.trace("LIVISI SmartHome websocket session: {}", session);
    }

    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        if (statusCode == StatusCode.NORMAL) {
            logger.debug("Connection to LIVISI SmartHome webservice was closed normally.");
        } else if (!closing) {
            // An additional reconnect attempt is only required when the close/stop wasn't executed by the binding.
            logger.debug("Connection to LIVISI SmartHome webservice was closed abnormally (code: {}). Reason: {}",
                    statusCode, reason);
            eventListener.connectionClosed();
        }
    }

    @OnWebSocketError
    public void onError(Throwable cause) {
        logger.debug("LIVISI SmartHome websocket onError() - {}", cause.getMessage());
        eventListener.onError(cause);
    }

    @OnWebSocketMessage
    public void onMessage(String msg) {
        logger.debug("LIVISI SmartHome websocket onMessage() - {}", msg);
        if (closing) {
            logger.debug("LIVISI SmartHome websocket onMessage() - ignored, WebSocket is closing...");
        } else {
            eventListener.onEvent(msg);
        }
    }

    WebSocketClient createWebSocketClient() {
        WebSocketClient client = new WebSocketClient(httpClient);
        client.setMaxIdleTimeout(maxIdleTimeout);
        return client;
    }

    void startWebSocketClient(WebSocketClient client) throws WebSocketConnectException {
        try {
            client.start();
        } catch (Exception e) {
            throw new WebSocketConnectException("Starting WebSocket failed!", e);
        }
    }

    void stopWebSocketClient(WebSocketClient client) {
        try {
            client.stop();
            client.destroy();
        } catch (Exception e) {
            logger.debug("Stopping WebSocket failed", e);
        }
    }
}
