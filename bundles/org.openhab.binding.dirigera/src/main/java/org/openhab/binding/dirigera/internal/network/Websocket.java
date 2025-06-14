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
package org.openhab.binding.dirigera.internal.network;

import static org.openhab.binding.dirigera.internal.Constants.WS_URL;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
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
import org.json.JSONObject;
import org.openhab.binding.dirigera.internal.interfaces.Gateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link Websocket} listens to device changes
 *
 * @author Bernd Weymann - Initial contribution
 */
@WebSocket
@NonNullByDefault
public class Websocket {
    private final Logger logger = LoggerFactory.getLogger(Websocket.class);
    private final Map<String, Instant> pingPongMap = new HashMap<>();

    private static final String STARTS = "starts";
    private static final String STOPS = "stops";
    private static final String DISCONNECTS = "disconnetcs";
    private static final String ERRORS = "errors";
    private static final String PINGS = "pings";
    private static final String PING_LATENCY = "pingLatency";
    private static final String PING_LAST = "lastPing";
    private static final String MESSAGES = "messages";
    public static final String MODEL_UPDATES = "modelUpdates";
    public static final String MODEL_UPDATE_TIME = "modelUpdateDuration";
    public static final String MODEL_UPDATE_LAST = "lastModelUpdate";

    private Optional<WebSocketClient> websocketClient = Optional.empty();
    private Optional<Session> session = Optional.empty();
    private JSONObject statistics = new JSONObject();
    private HttpClient httpClient;
    private Gateway gateway;
    private boolean disposed = false;

    public Websocket(Gateway gateway, HttpClient httpClient) {
        this.gateway = gateway;
        this.httpClient = httpClient;
    }

    public void initialize() {
        disposed = false;
    }

    public void start() {
        if ("unit-test".equals(gateway.getToken())) {
            // handle unit tests online
            gateway.websocketConnected(true, "unit test");
            return;
        }
        if (disposed) {
            logger.debug("DIRIGERA WS start rejected, disposed {}", disposed);
            return;
        }
        increase(STARTS);
        internalStop(); // don't count this internal stopping
        try {
            pingPongMap.clear();
            WebSocketClient client = new WebSocketClient(httpClient);
            client.setMaxIdleTimeout(0);

            ClientUpgradeRequest request = new ClientUpgradeRequest();
            request.setHeader("Authorization", "Bearer " + gateway.getToken());

            String websocketURL = String.format(WS_URL, gateway.getIpAddress());
            logger.trace("DIRIGERA WS start {}", websocketURL);
            websocketClient = Optional.of(client);
            client.start();
            client.connect(this, new URI(websocketURL), request);
        } catch (Exception t) {
            // catch Exceptions of start stop and declare communication error
            logger.warn("DIRIGERA WS handling exception: {}", t.getMessage());
        }
    }

    public boolean isRunning() {
        return websocketClient.isPresent() && session.isPresent() && session.get().isOpen();
    }

    public void stop() {
        increase(STOPS);
        internalStop();
    }

    private void internalStop() {
        session.ifPresent(session -> {
            session.close();
        });
        websocketClient.ifPresent(client -> {
            try {
                client.stop();
                client.destroy();
            } catch (Exception e) {
                logger.warn("DIRIGERA WS exception stopping running client");
            }
        });
        websocketClient = Optional.empty();
        this.session = Optional.empty();
    }

    public void dispose() {
        internalStop();
        disposed = true;
    }

    public void ping() {
        session.ifPresentOrElse((session) -> {
            try {
                // build ping message
                String pingId = UUID.randomUUID().toString();
                pingPongMap.put(pingId, Instant.now());
                session.getRemote().sendPing(ByteBuffer.wrap(pingId.getBytes()));
                increase(PINGS);
            } catch (IOException e) {
                logger.warn("DIRIGERA WS ping failed with exception {}", e.getMessage());
            }
        }, () -> {
            logger.debug("DIRIGERA WS ping found no session - restart websocket");
        });
    }

    /**
     * endpoints
     */

    @OnWebSocketMessage
    public void onTextMessage(String message) {
        increase(MESSAGES);
        gateway.websocketUpdate(message);
    }

    @OnWebSocketFrame
    public void onFrame(Frame frame) {
        if (Frame.Type.PONG.equals(frame.getType())) {
            ByteBuffer buffer = frame.getPayload();
            byte[] bytes = new byte[frame.getPayloadLength()];
            for (int i = 0; i < frame.getPayloadLength(); i++) {
                bytes[i] = buffer.get(i);
            }
            String paylodString = new String(bytes);
            Instant sent = pingPongMap.remove(paylodString);
            if (sent != null) {
                long durationMS = Duration.between(sent, Instant.now()).toMillis();
                statistics.put(PING_LATENCY, durationMS);
                statistics.put(PING_LAST, Instant.now());
            } else {
                logger.debug("DIRIGERA WS receiced pong without ping {}", paylodString);
            }
        } else if (Frame.Type.PING.equals(frame.getType())) {
            session.ifPresentOrElse((session) -> {
                logger.trace("DIRIGERA onPing ");
                ByteBuffer buffer = frame.getPayload();
                try {
                    session.getRemote().sendPong(buffer);
                } catch (IOException e) {
                    logger.warn("DIRIGERA WS onPing answer exception {}", e.getMessage());
                }
            }, () -> {
                logger.debug("DIRIGERA WS onPing answer cannot be initiated");
            });
        }
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        logger.debug("DIRIGERA WS onConnect");
        this.session = Optional.of(session);
        session.setIdleTimeout(-1);
        gateway.websocketConnected(true, "connected");
    }

    @OnWebSocketClose
    public void onDisconnect(Session session, int statusCode, String reason) {
        logger.debug("DIRIGERA WS onDisconnect Status {} Reason {}", statusCode, reason);
        this.session = Optional.empty();
        increase(DISCONNECTS);
        gateway.websocketConnected(false, reason);
    }

    @OnWebSocketError
    public void onError(Throwable t) {
        String message = t.getMessage();
        logger.warn("DIRIGERA WS onError {}", message);
        this.session = Optional.empty();
        if (message == null) {
            message = "unknown";
        }
        increase(ERRORS);
        gateway.websocketConnected(false, message);
    }

    /**
     * Helper functions
     */

    public JSONObject getStatistics() {
        return statistics;
    }

    public void increase(String key) {
        if (statistics.has(key)) {
            int counter = statistics.getInt(key);
            statistics.put(key, ++counter);
        } else {
            statistics.put(key, 1);
        }
    }

    public Map<String, Instant> getPingPongMap() {
        return pingPongMap;
    }
}
