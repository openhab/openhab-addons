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
package org.openhab.binding.senseenergy.internal.api;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.eclipse.jetty.websocket.common.WebSocketSession;
import org.openhab.binding.senseenergy.internal.api.dto.SenseEnergyWebSocketRealtimeUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * The {@link SenseEnergyWebSocket }
 *
 * @author Jeff James - Initial contribution
 */
@NonNullByDefault
@WebSocket
public class SenseEnergyWebSocket implements WebSocketListener {
    private final Logger logger = LoggerFactory.getLogger(SenseEnergyWebSocket.class);

    private static final String WSSURL = "wss://clientrt.sense.com/monitors/%s/realtimefeed?access_token=%s";

    private final SenseEnergyWebSocketListener listener;
    private WebSocketClient client;
    @Nullable
    private WebSocketSession session;
    private boolean closing;
    private long monitorId;

    private Gson gson = new Gson();

    public boolean isClosing() {
        return this.closing;
    }

    public SenseEnergyWebSocket(SenseEnergyWebSocketListener listener, WebSocketClient client) {
        this.listener = listener;
        this.client = client;
    }

    public void start(long monitorId, String accessToken) throws Exception {
        logger.debug("Starting Sense Energy WebSocket");
        this.monitorId = monitorId;

        String url = String.format(WSSURL, monitorId, accessToken);
        session = (WebSocketSession) client.connect(this, new URI(url)).get();
    }

    public void restart(String accessToken)
            throws InterruptedException, ExecutionException, IOException, URISyntaxException {
        logger.debug("Re-starting Sense Energy WebSocket");

        this.stop();
        String url = String.format(WSSURL, monitorId, accessToken);
        session = (WebSocketSession) client.connect(this, new URI(url)).get();
    }

    public synchronized void stop() {
        closing = true;
        logger.trace("Stopping Sense Energy WebSocket");

        WebSocketSession localSession = session;
        if (localSession != null) {
            try {
                localSession.close();
            } catch (Exception ex) {
                // ignore
            }

            return;
        }
    }

    public boolean isRunning() {
        return (session == null) ? false : session.isRunning();
    }

    /******** WebSocketListener interface ***********/
    @Override
    public void onWebSocketClose(int statusCode, @Nullable String reason) {
        this.session = null;
        logger.trace("WebSocket Close: {} - {}", statusCode, reason);
        if (!closing) {
            listener.onWebSocketClose(statusCode, reason);
        }
    }

    @Override
    public void onWebSocketConnect(@Nullable Session session) {
        closing = false;
        logger.debug("Connected to Sense Energy WebSocket");
    }

    @Override
    public void onWebSocketError(@Nullable Throwable cause) {
        String causeMessage = (cause instanceof Throwable causeSafe && causeSafe.getMessage() instanceof String message)
                ? message
                : "unknown";

        logger.debug("Sense Energy WebSocket error: {}", causeMessage);

        if (!closing) {
            // let listener handle restart of socket
            listener.onWebSocketError(causeMessage);
        }
    }

    @Override
    public void onWebSocketBinary(byte @Nullable [] payload, int offset, int len) {
        logger.trace("onWebSocketBinary");
    }

    @Override
    public void onWebSocketText(@Nullable String message) {
        if (!closing) {
            if (message == null) {
                return;
            }
            JsonObject jsonResponse = JsonParser.parseString(message).getAsJsonObject();

            String type = jsonResponse.get("type").getAsString();
            if ("realtime_update".equals(type)) {
                SenseEnergyWebSocketRealtimeUpdate update = gson.fromJson(jsonResponse.getAsJsonObject("payload"),
                        SenseEnergyWebSocketRealtimeUpdate.class);
                if (update != null) {
                    listener.onWebSocketRealtimeUpdate(update);
                }
            } else if ("error".equals(type)) {
                // TODO
                logger.debug("WS error {}", jsonResponse.get("payload").toString());
            }
        }
    }
}
