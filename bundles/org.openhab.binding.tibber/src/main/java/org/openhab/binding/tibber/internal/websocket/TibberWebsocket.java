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
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.openhab.binding.tibber.internal.config.TibberConfiguration;
import org.openhab.binding.tibber.internal.handler.TibberHandler;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * The {@link TibberWebsocket} is responsible for handling queries to/from Tibber API.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
@WebSocket
public class TibberWebsocket {
    private final Logger logger = LoggerFactory.getLogger(TibberWebsocket.class);
    private final TibberConfiguration config;
    private final HttpClient httpClient;
    private final TibberHandler handler;
    private Optional<WebSocketClient> wsClient = Optional.empty();
    private Optional<Session> session = Optional.empty();
    private Optional<URI> wsUrl = Optional.empty();

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
        WebSocketClient client = new WebSocketClient(httpClient);
        client.setMaxIdleTimeout(30 * 1000);

        ClientUpgradeRequest newRequest = new ClientUpgradeRequest();
        newRequest.setHeader(HttpHeader.USER_AGENT.asString(), "openHAB/Tibber "
                + FrameworkUtil.getBundle(this.getClass()).getVersion().toString() + " Tibber driver " + TIBBER_DRIVER);
        newRequest.setHeader(HttpHeader.AUTHORIZATION.asString(), "Bearer " + config.token);
        newRequest.setSubProtocols("graphql-transport-ws");

        logger.debug("Starting Websocket connection");
        try {
            client.start();
            client.connect(this, getSubscriptionUrl(), newRequest);
            wsClient = Optional.of(client);
        } catch (Exception e) {
            logger.warn("Exception ws connection {}", e.getMessage());
        }
    }

    private @Nullable URI getSubscriptionUrl() throws IOException {
        if (wsUrl.isPresent()) {
            return wsUrl.get();
        } else {
            Request websocketUrlRequest = handler.getRequest();
            websocketUrlRequest.content(new StringContentProvider(WEBSOCKET_URL_QUERY, "utf-8"));
            try {
                ContentResponse response = websocketUrlRequest.send();
                int responseStatus = response.getStatus();
                String jsonResponse = response.getContentAsString();
                logger.trace("isRealtimeEnabled response {} - {}", responseStatus, jsonResponse);
                if (response.getStatus() == 200) {
                    JsonObject wsobject = (JsonObject) JsonParser.parseString(jsonResponse);
                    JsonObject dataObject = wsobject.getAsJsonObject("data");
                    if (dataObject != null) {
                        JsonObject viewerObject = dataObject.getAsJsonObject("viewer");
                        if (viewerObject != null) {
                            JsonElement subscriptionElement = viewerObject.get("websocketSubscriptionUrl");
                            if (subscriptionElement != null) {
                                wsUrl = Optional.of(new URI(subscriptionElement.toString().replaceAll("^\"|\"$", "")));
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
        wsClient.ifPresentOrElse(client -> {
            try {
                client.stop();
                client.destroy();
            } catch (Exception e) {
                logger.warn("Exception stopping ws url {}", e.getMessage());
            } finally {
                wsClient = Optional.empty();
                session = Optional.empty();
            }
        }, () -> {
        });
    }

    @OnWebSocketConnect
    public void onConnect(Session wssession) {
        session = Optional.of(wssession);
        String connection = String.format(CONNECTION_PATH, config.token);
        sendMessage(connection);
    }

    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        session = Optional.empty();
        logger.debug("WebSocket closed - Status {} Reason {}", statusCode, reason);
    }

    @OnWebSocketError
    public void onWebSocketError(Throwable e) {
        session = Optional.empty();
        logger.warn("Websocket error {}", e.getMessage());
    }

    @OnWebSocketMessage
    public void onMessage(String message) {
        handler.newMessage(message);
    }

    private void sendMessage(String message) {
        session.ifPresentOrElse(session -> {
            logger.trace("Websocket send message {}", message);
            try {
                session.getRemote().sendString(message);
            } catch (IOException e) {
                logger.warn("Websocket send message {} failed - reason {}", message, e.getMessage());
            }
        }, () -> {
            logger.info("Websocket send message {} rejected - websocket offline", message);
        });
    }

    public void startSubscription() {
        String query = String.format(CONNECTION_PATH, config.homeid);
        sendMessage(query);
    }

    public boolean isConnected() {
        if (session.isPresent()) {
            return session.get().isOpen();
        }
        return false;
    }
}
