/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.moonraker.internal;

import java.io.IOException;
import java.net.URI;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * The {@link MoonrakerWebSocket} implements the websocket for receiving
 * constant updates from the Moonraker web service.
 *
 * @author Arjan Mels - Initial contribution
 */
@NonNullByDefault
@WebSocket
public class MoonrakerWebSocket {

    private final Logger logger = LoggerFactory.getLogger(MoonrakerWebSocket.class);
    private final EventListener eventListener;
    private final URI webSocketURI;
    private final int maxIdleTimeout;

    private @Nullable Session session;
    private @Nullable WebSocketClient client;
    private boolean closing;
    private int requestId;

    /**
     * Constructs the {@link MoonrakerWebSocket}.
     *
     * @param eventListener the responsible {@link MoonrakerHandler}
     * @param webSocketURI the {@link URI} of the websocket endpoint
     * @param maxIdleTimeout
     */
    public MoonrakerWebSocket(EventListener eventListener, URI webSocketURI, int maxIdleTimeout) {
        this.eventListener = eventListener;
        this.webSocketURI = webSocketURI;
        this.maxIdleTimeout = maxIdleTimeout;
    }

    /**
     * Starts the {@link MoonrakerWebSocket}.
     *
     * @throws Exception
     */
    public synchronized void start() throws Exception {
        if (client == null || client.isStopped()) {
            client = new WebSocketClient();
            client.setMaxIdleTimeout(this.maxIdleTimeout);
            client.start();
        }

        if (session != null) {
            session.close();
        }

        logger.debug("Connecting to Moonraker WebSocket...");
        session = client.connect(this, webSocketURI).get();
    }

    /**
     * Stops the {@link MoonrakerWebSocket}.
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

    /**
     * Send a RPC request without parameters to the websocket
     * 
     * @param methodName name of the RPC method
     */
    public void sendRequest(String methodName) {
        sendRequest(methodName, (JsonObject) null);
    }

    /**
     * Send a RPC request with parameters to the websocket
     * 
     * @param methodName name of the RPC method
     * @param params {@link JsonObject} with the parameters for the RPC method
     */
    public void sendRequest(String methodName, final @Nullable JsonObject params) {
        JsonObject req = new JsonObject();
        req.addProperty("jsonrpc", "2.0");
        req.addProperty("id", methodName + ":" + requestId++);
        req.addProperty("method", methodName);

        if (params != null && params.size() > 0)
            req.add("params", params);

        String requestData = req.toString();
        logger.trace("Moonraker WebSocket sendRequest() - {}", requestData);

        try {
            session.getRemote().sendString(requestData);
        } catch (IOException e) {
            onError(e);
        }
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        this.closing = false;
        this.session = session;
        this.requestId = 1;
        logger.info("Connected to Moonraker Webservice.");
        logger.trace("Moonraker Websocket session: {}", session);
        eventListener.onConnect();
    }

    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        session = null;
        if (statusCode == StatusCode.NORMAL) {
            logger.info("Connection to Moonraker Webservice was closed normally.");
        } else if (!closing) {
            // An additional reconnect attempt is only required when the close/stop wasn't
            // executed by the binding.
            logger.info("Connection to Moonraker Webservice was closed abnormally (code: {}). Reason: {}", statusCode,
                    reason);
            eventListener.connectionClosed();
        }
    }

    @OnWebSocketError
    public void onError(Throwable cause) {
        logger.debug("Moonraker WebSocket onError() - {}", cause.getMessage());
        eventListener.onError(cause);
    }

    @OnWebSocketMessage
    public void onMessage(String msg) {
        logger.trace("Moonraker WebSocket onMessage() - {}", msg);
        if (closing) {
            logger.debug("Moonraker WebSocket onMessage() - ignored, WebSocket is closing...");
        } else {
            Gson gson = new Gson();
            RPCResponse response = gson.fromJson(msg, RPCResponse.class);
            if (response.error == null) {
                eventListener.onEvent(response);
            } else {
                logger.warn("Error {} in RPC call ({}): {}", response.error.code, response.id, response.error.message);
            }
        }
    }
}
