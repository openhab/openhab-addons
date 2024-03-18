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
package org.openhab.binding.neohub.internal;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.openhab.core.io.net.http.TlsTrustManagerProvider;
import org.openhab.core.io.net.http.WebSocketFactory;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.util.ThingWebClientUtil;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * Handles the text based communication via web socket between openHAB and NeoHub
 *
 * @author Andrew Fiddian-Green - Initial contribution
 *
 */
@NonNullByDefault
@WebSocket
public class NeoHubWebSocket extends NeoHubSocketBase {

    private static final int SLEEP_MILLISECONDS = 100;
    private static final String REQUEST_OUTER = "{\"message_type\":\"hm_get_command_queue\",\"message\":\"%s\"}";
    private static final String REQUEST_INNER = "{\"token\":\"%s\",\"COMMANDS\":[{\"COMMAND\":\"%s\",\"COMMANDID\":1}]}";

    private final Logger logger = LoggerFactory.getLogger(NeoHubWebSocket.class);
    private final Gson gson = new Gson();
    private final WebSocketClient webSocketClient;
    private final ServiceRegistration<?> trustManagerRegistration;

    private @Nullable Session session = null;
    private @Nullable IOException websocketException = null;
    private List<String> responses = new CopyOnWriteArrayList<>();
    private boolean closing;

    /**
     * DTO to receive and parse the response JSON.
     *
     * @author Andrew Fiddian-Green - Initial contribution
     */
    private static class Response {
        @SuppressWarnings("unused")
        public @Nullable String command_id;
        @SuppressWarnings("unused")
        public @Nullable String device_id;
        public @Nullable String message_type;
        public @Nullable String response;
    }

    public NeoHubWebSocket(NeoHubConfiguration config, WebSocketFactory webSocketFactory, ThingUID bridgeUID)
            throws IOException {
        super(config, bridgeUID.getAsString());

        String name = ThingWebClientUtil.buildWebClientConsumerName(bridgeUID, null);
        webSocketClient = webSocketFactory.createWebSocketClient(name);
        webSocketClient.setConnectTimeout(config.socketTimeout * 1000);
        try {
            webSocketClient.start();
        } catch (Exception e) {
            throw new IOException("Error starting Web Socket client", e);
        }
        NeoHubTlsTrustManagerProvider trustManagerProvider = new NeoHubTlsTrustManagerProvider(config);
        try {
            trustManagerRegistration = FrameworkUtil.getBundle(getClass()).getBundleContext()
                    .registerService(TlsTrustManagerProvider.class.getName(), trustManagerProvider, null);
        } catch (IllegalStateException e) {
            throw new IOException("Error registering trust manager", e);
        }
    }

    /**
     * Open the web socket session.
     *
     * @throws IOException if unable to open the web socket
     */
    private synchronized void startSession() throws IOException {
        Session session = this.session;
        if (session == null || !session.isOpen()) {
            try {
                int port = config.portNumber > 0 ? config.portNumber : NeoHubBindingConstants.PORT_WSS;
                URI uri = new URI(String.format("wss://%s:%d", config.hostName, port));
                this.session = webSocketClient.connect(this, uri).get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Error starting session", e);
            } catch (ExecutionException | IOException | URISyntaxException e) {
                throw new IOException("Error starting session", e);
            }
        }
    }

    /**
     * Helper to escape the quote marks in a JSON string.
     *
     * @param json the input JSON string.
     * @return the escaped JSON version.
     */
    private String jsonEscape(String json) {
        return json.replace("\"", "\\\"");
    }

    /**
     * Helper to remove quote escape marks from an escaped JSON string.
     *
     * @param escapedJson the escaped input string.
     * @return the clean JSON version.
     */
    private String jsonUnEscape(String escapedJson) {
        return escapedJson.replace("\\\"", "\"");
    }

    /**
     * Helper to replace double quote marks in a JSON string with single quote marks.
     *
     * @param json the input string.
     * @return the modified version.
     */
    private String jsonReplaceQuotes(String json) {
        return json.replace("\"", "'");
    }

    @Override
    public String sendMessage(final String requestJson) throws IOException, NeoHubException {
        if (!closing && websocketException != null) {
            throw websocketException;
        }

        try (Throttler throttler = new Throttler()) {
            // start the session
            startSession();

            // session start failed
            Session session = this.session;
            if (session == null) {
                throw new IOException("Session is null");
            }

            // wrap the inner request in an outer request string
            String requestOuter = String.format(REQUEST_OUTER,
                    jsonEscape(String.format(REQUEST_INNER, config.apiToken, jsonReplaceQuotes(requestJson))));

            IOException caughtException = null;
            try {
                // send the request
                logger.debug("hub '{}' sending characters:{}", hubId, requestOuter.length());
                session.getRemote().sendString(requestOuter);
                logger.trace("hub '{}' sent:{}", hubId, requestOuter);

                // sleep and loop until we get a response, the socket is closed, or it times out
                Instant timeout = Instant.now().plusSeconds(config.socketTimeout);
                while (!closing && websocketException == null && responses.isEmpty()) {
                    try {
                        Thread.sleep(SLEEP_MILLISECONDS);
                        if (Instant.now().isAfter(timeout)) {
                            throw new IOException("Read timed out");
                        }
                    } catch (InterruptedException e) {
                        throw new IOException("Read interrupted", e);
                    }
                }
            } catch (IOException e) {
                caughtException = e;
            }

            String responseOuter = !responses.isEmpty() ? responses.remove(0) : "";
            logger.debug("hub '{}' received characters:{}", hubId, responseOuter.length());
            logger.trace("hub '{}' received:{}", hubId, responseOuter);

            // don't throw an exception if already closing
            if (closing) {
                return "{}";
            }

            // if an IOException was caught above, re-throw it again
            caughtException = websocketException != null ? websocketException : caughtException;
            if (caughtException != null) {
                throw caughtException;
            }

            try {
                Response responseDto = gson.fromJson(responseOuter, Response.class);
                if (responseDto == null) {
                    throw new JsonSyntaxException("Response DTO is invalid");
                }
                if (!NeoHubBindingConstants.HM_SET_COMMAND_RESPONSE.equals(responseDto.message_type)) {
                    throw new JsonSyntaxException("DTO 'message_type' field is invalid");
                }
                String responseJson = responseDto.response;
                if (responseJson == null) {
                    throw new JsonSyntaxException("DTO 'response' field is null");
                }
                responseJson = jsonUnEscape(responseJson).strip();
                if (!JsonParser.parseString(responseJson).isJsonObject()) {
                    throw new JsonSyntaxException("DTO 'response' field is not a JSON object");
                }
                return responseJson;
            } catch (JsonSyntaxException e) {
                logger.debug("hub '{}' {}; response:{}", hubId, e.getMessage(), responseOuter);
                throw new NeoHubException("Invalid response");
            }
        } catch (InterruptedException e) {
            throw new NeoHubException("Throttler was interrupted unexpectedly");
        }
    }

    @Override
    public void close() {
        closing = true;
        Session session = this.session;
        if (session != null) {
            session.close();
            this.session = null;
        }
        try {
            trustManagerRegistration.unregister();
        } catch (Exception e) {
        }
        try {
            webSocketClient.stop();
        } catch (Exception e) {
        }
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        logger.debug("hub '{}' onConnect() ok", hubId);
    }

    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        String closeMessage = String.format("onClose() statusCode:%d, reason:%s", statusCode, reason);
        logger.debug("hub '{}' {}", hubId, closeMessage);
        websocketException = new IOException(closeMessage);
    }

    @OnWebSocketError
    public void onError(@Nullable Throwable cause) {
        logger.debug("hub '{}' onError() cause:{}", hubId, cause != null ? cause.getMessage() : "null");
        websocketException = cause instanceof IOException ioCause ? ioCause : new IOException(cause);
    }

    @OnWebSocketMessage
    public synchronized void onMessage(String msg) {
        int responseCount = responses.size();
        if (responseCount > 0) {
            String errorMessage = String.format("onMessage() too many responses:%d", responseCount);
            logger.debug("hub '{}' {}", hubId, errorMessage);
            websocketException = new IOException(errorMessage);
        }
        responses.add(msg.strip());
    }
}
