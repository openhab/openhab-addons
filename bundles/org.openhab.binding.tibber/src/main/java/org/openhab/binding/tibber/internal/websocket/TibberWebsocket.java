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
package org.openhab.binding.tibber.internal.websocket;

import static org.openhab.binding.tibber.internal.TibberBindingConstants.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketFrame;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.api.extensions.Frame;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.openhab.binding.tibber.internal.Utils;
import org.openhab.binding.tibber.internal.config.TibberConfiguration;
import org.openhab.binding.tibber.internal.handler.TibberHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * The {@link TibberWebsocket} connects to Tibber and retreives live data for Tibber Pulse.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
@WebSocket
public class TibberWebsocket {
    private final Logger logger = LoggerFactory.getLogger(TibberWebsocket.class);
    private final Map<String, Instant> pingPongMap = new HashMap<>();
    private final TibberConfiguration config;
    private final HttpClient httpClient;
    private final TibberHandler handler;
    private @Nullable WebSocketClient wsClient;
    private @Nullable Session session;
    private @Nullable URI wsUrl;

    public TibberWebsocket(TibberHandler handler, TibberConfiguration config, HttpClient httpClient) {
        this.handler = handler;
        this.config = config;
        this.httpClient = httpClient;
    }

    public void start() {
        if (isConnected()) {
            logger.trace("Tibber Websocket already running");
            return;
        }
        pingPongMap.clear();
        WebSocketClient client = new WebSocketClient(httpClient);
        client.setMaxIdleTimeout(30 * 1000);

        ClientUpgradeRequest newRequest = new ClientUpgradeRequest();
        newRequest.setHeader(HttpHeader.USER_AGENT.asString(), Utils.getUserAgent(this));
        newRequest.setHeader(HttpHeader.AUTHORIZATION.asString(), "Bearer " + config.token);
        newRequest.setSubProtocols("graphql-transport-ws");

        logger.debug("Starting Websocket connection");
        try {
            client.start();
            client.connect(this, getSubscriptionUrl(), newRequest);
            wsClient = client;
        } catch (Exception e) {
            logger.warn("Exception ws connection {}", e.getMessage());
        }
    }

    private @Nullable URI getSubscriptionUrl() throws IOException {
        if (wsUrl != null) {
            return wsUrl;
        } else {
            Request websocketUrlRequest = handler.getRequest();
            websocketUrlRequest.content(new StringContentProvider(WEBSOCKET_URL_QUERY, "utf-8"));
            try {
                ContentResponse response = websocketUrlRequest.send();
                int responseStatus = response.getStatus();
                String jsonResponse = response.getContentAsString();
                logger.trace("getSubscriptionUrl response {} - {}", responseStatus, jsonResponse);
                if (response.getStatus() == HttpStatus.OK_200) {
                    JsonObject wsobject = (JsonObject) JsonParser.parseString(jsonResponse);
                    JsonObject dataObject = wsobject.getAsJsonObject("data");
                    if (dataObject != null) {
                        JsonObject viewerObject = dataObject.getAsJsonObject("viewer");
                        if (viewerObject != null) {
                            JsonElement subscriptionElement = viewerObject.get("websocketSubscriptionUrl");
                            if (subscriptionElement != null) {
                                URI wsURI = new URI(subscriptionElement.toString().replaceAll("^\"|\"$", ""));
                                wsUrl = wsURI;
                                return wsURI;
                            }
                        }
                    }
                }
            } catch (InterruptedException | TimeoutException | ExecutionException | URISyntaxException e) {
                logger.warn("Exception getting ws url {}", e.getMessage());
            }
        }
        return null;
    }

    public void stop() {
        WebSocketClient wsClient = this.wsClient;
        if (wsClient != null) {
            try {
                Session session = this.session;
                if (session != null) {
                    sendMessage(DISCONNECT_MESSAGE);
                    session.close();
                }
                wsClient.stop();
                wsClient.destroy();
            } catch (Exception e) {
                logger.warn("Exception stopping ws url {}", e.getMessage());
            } finally {
                this.wsClient = null;
            }
        }
        this.session = null;
    }

    @OnWebSocketConnect
    public void onConnect(Session wssession) {
        session = wssession;
        String connection = String.format(CONNECT_MESSAGE, config.token);
        sendMessage(connection);
    }

    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        session = null;
        logger.debug("WebSocket closed - Status {} Reason {}", statusCode, reason);
    }

    @OnWebSocketError
    public void onWebSocketError(Throwable e) {
        session = null;
        logger.warn("Websocket error {}", e.getMessage());
    }

    @OnWebSocketMessage
    public void onMessage(String message) {
        if (message.contains("connection_ack")) {
            logger.debug("WebSocket connected to Server");
            String subScriptionMessage = String.format(SUBSCRIPTION_MESSAGE, config.homeid);
            sendMessage(subScriptionMessage);
        } else {
            handler.newMessage(message);
        }
    }

    @OnWebSocketFrame
    public void onFrame(Frame frame) {
        if (Frame.Type.PONG.equals(frame.getType())) {
            ByteBuffer buffer = frame.getPayload();
            byte[] bytes = new byte[frame.getPayloadLength()];
            for (int i = 0; i < frame.getPayloadLength(); i++) {
                bytes[i] = buffer.get(i);
            }
            String payloadString = new String(bytes);
            Instant sent = pingPongMap.remove(payloadString);
            if (sent == null) {
                logger.debug("Websocket receiced pong without ping {}", payloadString);
            }
        } else if (Frame.Type.PING.equals(frame.getType())) {
            Session session = this.session;
            if (session != null) {
                ByteBuffer buffer = frame.getPayload();
                try {
                    session.getRemote().sendPong(buffer);
                } catch (IOException e) {
                    logger.debug("Websocket onPing answer exception {}", e.getMessage());
                }
            } else {
                logger.debug("Websocket onPing answer cannot be initiated");
            }
        }
    }

    public boolean isConnected() {
        Session session = this.session;
        return session != null && session.isOpen();
    }

    public void ping() {
        Session session = this.session;
        if (session != null) {
            try {
                String pingId = UUID.randomUUID().toString();
                pingPongMap.put(pingId, Instant.now());
                session.getRemote().sendPing(ByteBuffer.wrap(pingId.getBytes()));
            } catch (IOException e) {
                logger.debug("Websocket ping failed {}", e.getMessage());
            }
        }
    }

    private void sendMessage(String message) {
        Session session = this.session;
        if (session != null) {
            logger.trace("Websocket send message {}", message);
            try {
                session.getRemote().sendString(message);
            } catch (IOException e) {
                logger.warn("Websocket send message {} failed - reason {}", message, e.getMessage());
            }
        } else {
            logger.debug("Websocket send message {} rejected - websocket offline", message);
        }
    }
}
