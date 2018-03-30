/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.innogysmarthome.internal;

import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.openhab.binding.innogysmarthome.handler.InnogyBridgeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link InnogyWebSocket} implements the websocket for receiving constant updates
 * from the innogy SmartHome web service.
 *
 * @author Oliver Kuhl - Initial contribution
 */
@WebSocket
public class InnogyWebSocket {

    private Logger logger = LoggerFactory.getLogger(InnogyWebSocket.class);
    private final CountDownLatch closeLatch;
    private Session session;
    private org.openhab.binding.innogysmarthome.internal.listener.EventListener eventListener;
    private WebSocketClient client;
    private final URI webSocketURI;
    private final int maxIdleTimeout;

    /**
     * Constructs the {@link InnogyWebSocket}.
     *
     * @param bridgeHandler the responsible {@link InnogyBridgeHandler}
     * @param webSocketURI the {@link URI} of the websocket endpoint
     * @param maxIdleTimeout
     */
    public InnogyWebSocket(InnogyBridgeHandler bridgeHandler, URI webSocketURI, int maxIdleTimeout) {
        this.eventListener = bridgeHandler;
        this.closeLatch = new CountDownLatch(1);
        this.webSocketURI = webSocketURI;
        this.maxIdleTimeout = maxIdleTimeout;
    }

    /**
     * Starts the {@link InnogyWebSocket}.
     *
     * @throws Exception
     */
    public synchronized void start() throws Exception {
        SslContextFactory sslContextFactory = new SslContextFactory();
        // sslContextFactory.setTrustAll(true); // The magic

        if (client == null || client.isStopped()) {
            client = new WebSocketClient(sslContextFactory);
            client.setMaxIdleTimeout(this.maxIdleTimeout);
            client.start();
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
        if (isRunning()) {
            logger.debug("Closing session...");
            session.close();
            session = null;
        } else {
            session = null;
            logger.trace("Stopping websocket ignored - was not running.");
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
        logger.info("Connected to innogy Webservice.");
        logger.trace("innogy Websocket session: {}", session);
    }

    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        this.closeLatch.countDown();
        if (statusCode == StatusCode.NORMAL) {
            logger.info("Connection to innogy Webservice was closed normally.");
        } else {
            logger.info("Connection to innogy Webservice was closed abnormally (code: {}). Reason: {}", statusCode,
                    reason);
            eventListener.connectionClosed();
        }
    }

    /**
     * Await the closing of the websocket.
     *
     * @param duration
     * @param unit
     * @return
     * @throws InterruptedException
     */
    public boolean awaitClose(int duration, TimeUnit unit) throws InterruptedException {
        logger.debug("innogy WebSocket awaitClose() - {}{}", duration, unit);
        return this.closeLatch.await(duration, unit);
    }

    @OnWebSocketError
    public void onError(Throwable cause) {
        logger.error("innogy WebSocket onError() - {}", cause.getMessage());
    }

    @OnWebSocketMessage
    public void onMessage(String msg) {
        logger.debug("innogy WebSocket onMessage() - {}", msg);
        eventListener.onEvent(msg);
    }
}
