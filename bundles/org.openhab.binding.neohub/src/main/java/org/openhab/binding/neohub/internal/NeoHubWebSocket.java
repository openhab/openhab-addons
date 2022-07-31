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
package org.openhab.binding.neohub.internal;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * Handles the ASCII based communication via web socket between openHAB and NeoHub
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

    private @Nullable Session session = null;
    private String responseOuter = "";
    private boolean responseWaiting;

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

    public NeoHubWebSocket(NeoHubConfiguration config) throws NeoHubException {
        super(config);

        // initialise and start ssl context factory, http client, web socket client
        SslContextFactory.Client sslContextFactory = new SslContextFactory.Client();
        sslContextFactory.setTrustAll(true);
        HttpClient httpClient = new HttpClient(sslContextFactory);
        try {
            httpClient.start();
        } catch (Exception e) {
            throw new NeoHubException(String.format("Error starting http client: '%s'", e.getMessage()));
        }
        webSocketClient = new WebSocketClient(httpClient);
        webSocketClient.setConnectTimeout(config.socketTimeout * 1000);
        try {
            webSocketClient.start();
        } catch (Exception e) {
            throw new NeoHubException(String.format("Error starting web socket client: '%s'", e.getMessage()));
        }
    }

    /**
     * Open the web socket session.
     *
     * @throws NeoHubException
     */
    private void startSession() throws NeoHubException {
        Session session = this.session;
        if (session == null || !session.isOpen()) {
            closeSession();
            try {
                int port = config.portNumber > 0 ? config.portNumber : NeoHubBindingConstants.PORT_WSS;
                URI uri = new URI(String.format("wss://%s:%d", config.hostName, port));
                webSocketClient.connect(this, uri).get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new NeoHubException(String.format("Error starting session: '%s'", e.getMessage(), e));
            } catch (ExecutionException | IOException | URISyntaxException e) {
                throw new NeoHubException(String.format("Error starting session: '%s'", e.getMessage(), e));
            }
        }
    }

    /**
     * Close the web socket session.
     */
    private void closeSession() {
        Session session = this.session;
        if (session != null) {
            session.close();
            this.session = null;
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
    public synchronized String sendMessage(final String requestJson) throws IOException, NeoHubException {
        // start the session
        startSession();

        // session start failed
        Session session = this.session;
        if (session == null) {
            throw new NeoHubException("Session is null.");
        }

        // wrap the inner request in an outer request string
        String requestOuter = String.format(REQUEST_OUTER,
                jsonEscape(String.format(REQUEST_INNER, config.apiToken, jsonReplaceQuotes(requestJson))));

        // initialise the response
        responseOuter = "";
        responseWaiting = true;

        // send the request
        logger.trace("Sending request: {}", requestOuter);
        session.getRemote().sendString(requestOuter);

        // sleep and loop until we get a response or the socket is closed
        int sleepRemainingMilliseconds = config.socketTimeout * 1000;
        while (responseWaiting && (sleepRemainingMilliseconds > 0)) {
            try {
                Thread.sleep(SLEEP_MILLISECONDS);
                sleepRemainingMilliseconds = sleepRemainingMilliseconds - SLEEP_MILLISECONDS;
            } catch (InterruptedException e) {
                throw new NeoHubException(String.format("Read timeout '%s'", e.getMessage()));
            }
        }

        // extract the inner response from the outer response string
        Response responseDto = gson.fromJson(responseOuter, Response.class);
        if (responseDto != null && NeoHubBindingConstants.HM_SET_COMMAND_RESPONSE.equals(responseDto.message_type)) {
            String responseJson = responseDto.response;
            if (responseJson != null) {
                responseJson = jsonUnEscape(responseJson);
                logger.trace("Received response: {}", responseJson);
                return responseJson;
            }
        }
        logger.debug("Null or invalid response.");
        return "";
    }

    @Override
    public void close() {
        closeSession();
        try {
            webSocketClient.stop();
        } catch (Exception e) {
        }
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        logger.trace("onConnect: ok");
        this.session = session;
    }

    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        logger.trace("onClose: code:{}, reason:{}", statusCode, reason);
        responseWaiting = false;
        this.session = null;
    }

    @OnWebSocketError
    public void onError(Throwable cause) {
        logger.trace("onError: cause:{}", cause.getMessage());
        closeSession();
    }

    @OnWebSocketMessage
    public void onMessage(String msg) {
        logger.trace("onMessage: msg:{}", msg);
        responseOuter = msg;
        responseWaiting = false;
    }
}
