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
package org.openhab.binding.innogysmarthome.internal;

import java.net.URI;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.openhab.binding.innogysmarthome.internal.handler.InnogyBridgeHandler;
import org.openhab.binding.innogysmarthome.internal.listener.EventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link InnogyWebSocket} implements the websocket for receiving constant updates
 * from the innogy SmartHome web service.
 *
 * @author Oliver Kuhl - Initial contribution
 */
@NonNullByDefault
@WebSocket
public class InnogyWebSocket {

    private final Logger logger = LoggerFactory.getLogger(InnogyWebSocket.class);
    private final EventListener eventListener;
    private final URI webSocketURI;
    private final int maxIdleTimeout;

    private @Nullable Session session;
    private @Nullable WebSocketClient client;
    private boolean closing;

    /**
     * Constructs the {@link InnogyWebSocket}.
     *
     * @param eventListener the responsible {@link InnogyBridgeHandler}
     * @param webSocketURI the {@link URI} of the websocket endpoint
     * @param maxIdleTimeout
     */
    public InnogyWebSocket(EventListener eventListener, URI webSocketURI, int maxIdleTimeout) {
        this.eventListener = eventListener;
        this.webSocketURI = webSocketURI;
        this.maxIdleTimeout = maxIdleTimeout;
    }

    /**
     * Starts the {@link InnogyWebSocket}.
     *
     * @throws Exception
     */
    public synchronized void start() throws Exception {
        if (client == null || client.isStopped()) {
            client = startWebSocketClient();
        }

        if (session != null) {
            session.close();
        }

        logger.debug("Connecting to innogy WebSocket...");
        session = client.connect(this, webSocketURI).get();
    }

    /**
     * Stops the {@link InnogyWebSocket}.
     */
    public synchronized void stop() {
        this.closing = true;
        if (isRunning()) {
            logger.debug("Closing session...");
            session.close();
            session = null;
        } else {
            session = null;
            logger.trace("Stopping websocket ignored - was not running.");
        }
        if (client != null) {
            try {
                client.stop();
                client.destroy();
            } catch (Exception e) {
                logger.debug("Stopping websocket failed", e);
            }
            client = null;
        }
    }

    /**
     * Return true, if the websocket is running.
     *
     * @return
     */
    public synchronized boolean isRunning() {
        return session != null && session.isOpen();
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        this.closing = false;
        logger.info("Connected to innogy Webservice.");
        logger.trace("innogy Websocket session: {}", session);
    }

    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        if (statusCode == StatusCode.NORMAL) {
            logger.info("Connection to innogy Webservice was closed normally.");
        } else if (!closing) {
            // An additional reconnect attempt is only required when the close/stop wasn't executed by the binding.
            logger.info("Connection to innogy Webservice was closed abnormally (code: {}). Reason: {}", statusCode,
                    reason);
            eventListener.connectionClosed();
        }
    }

    @OnWebSocketError
    public void onError(Throwable cause) {
        logger.debug("innogy WebSocket onError() - {}", cause.getMessage());
        eventListener.onError(cause);
    }

    @OnWebSocketMessage
    public void onMessage(String msg) {
        logger.debug("innogy WebSocket onMessage() - {}", msg);
        if (closing) {
            logger.debug("innogy WebSocket onMessage() - ignored, WebSocket is closing...");
        } else {
            eventListener.onEvent(msg);
        }
    }

    WebSocketClient startWebSocketClient() throws Exception {
        WebSocketClient client = new WebSocketClient(new SslContextFactory.Client());
        client.setMaxIdleTimeout(this.maxIdleTimeout);
        client.start();
        return client;
    }
}
