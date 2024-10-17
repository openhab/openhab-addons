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
    private static String STARTS = "starts";
    private static String STOPS = "stops";
    private static String DISCONNECTS = "disconnetcs";
    private static String ERRORS = "errors";
    private static String PINGS = "pings";
    private static String MESSAGES = "messages";

    private final Logger logger = LoggerFactory.getLogger(Websocket.class);
    private final Map<String, Instant> pingPongMap = new HashMap<>();

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

    public void start() {
        if (disposed) {
            logger.trace("DIRIGERA Websocket start rejected, disposed {}", disposed);
            return;
        }
        increase(STARTS);
        internalStop(); // don't count this internal stopping
        try {
            pingPongMap.clear();
            WebSocketClient client = new WebSocketClient(httpClient);
            client.setMaxIdleTimeout(0);
            // client.setStopTimeout(CONNECT_TIMEOUT_MS);

            ClientUpgradeRequest request = new ClientUpgradeRequest();
            request.setHeader("Authorization", "Bearer " + gateway.getToken());

            String websocketURL = String.format(WS_URL, gateway.getIpAddress());
            logger.trace("DIRIGERA Websocket start {}", websocketURL);
            websocketClient = Optional.of(client);
            client.start();
            client.connect(this, new URI(websocketURL), request);
        } catch (Throwable t) {
            // catch Exceptions of start stop and declare communication error
            logger.warn("DIRIGERA Websocket handling exception: {}", t.getMessage());
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
        websocketClient.ifPresent(client -> {
            try {
                logger.info("DIRIGERA stop socket before start");
                client.stop();
                client.destroy();
            } catch (Exception e) {
                logger.warn("DIRIGERA exception stopping running client");
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
                logger.info("DIRIGERA ping failed with exception {}", e.getMessage());
            }
        }, () -> {
            logger.info("DIRIGERA ping found no session - restart websocket");
        });
    }

    /**
     * endpoints
     */

    @OnWebSocketMessage
    public void onTextMessage(String message) {
        increase(MESSAGES);
        // logger.info("DIRIGERA onMessage {}", message);
        gateway.websocketUpdate(new JSONObject(message));
    }

    @OnWebSocketFrame
    public void onFrame(Frame frame) {
        // logger.trace("DIRIGERA onFrame {} {} {}", frame.getType(), frame.hasPayload(), frame.getPayloadLength());
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
                logger.trace("DIRIGERA ping answered after {} ms", Duration.between(sent, Instant.now()).toMillis());
                statistics.put("latency", durationMS);
            } else {
                logger.trace("DIRIGERA receiced pong without ping {}", paylodString);
            }
            logger.trace("DIRIGERA unanswered pings {}", pingPongMap.size());
        } else if (Frame.Type.PING.equals(frame.getType())) {
            session.ifPresentOrElse((session) -> {
                logger.trace("DIRIGERA onPing ");
                ByteBuffer buffer = frame.getPayload();
                try {
                    session.getRemote().sendPong(buffer);
                    logger.trace("DIRIGERA onPing answered");
                } catch (IOException e) {
                    logger.trace("DIRIGERA onPing answer exception {}", e.getMessage());
                }
            }, () -> {
                logger.trace("DIRIGERA onPing answer cannot be initiated");
            });
        }
    }

    @OnWebSocketClose
    public void onDisconnect(Session session, int statusCode, String reason) {
        logger.info("DIRIGERA onDisconnect Status {} Reason {}", statusCode, reason);
        this.session = Optional.empty();
        increase(DISCONNECTS);
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        logger.info("DIRIGERA onConnect ");
        session.setIdleTimeout(-1);
        this.session = Optional.of(session);
    }

    @OnWebSocketError
    public void onError(Throwable t) {
        logger.warn("DIRIGERA onError {}", t.getMessage());
        increase(ERRORS);
    }

    /**
     * Helper functions
     */

    public JSONObject getStatistics() {
        return statistics;
    }

    private void increase(String key) {
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
