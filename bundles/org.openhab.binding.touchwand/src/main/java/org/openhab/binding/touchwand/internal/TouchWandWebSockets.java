/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.touchwand.internal;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.openhab.binding.touchwand.internal.data.TouchWandShutterSwitchUnitData;
import org.openhab.binding.touchwand.internal.data.TouchWandUnitData;
import org.openhab.binding.touchwand.internal.data.TouchWandUnitDataWallController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link TouchWandWebSockets} class implements WebSockets API to TouchWand controller
 *
 * @author Roie Geron - Initial contribution
 */
@NonNullByDefault
public class TouchWandWebSockets {

    private WebSocketClient client;
    private String controllerAddress;
    private TouchWandSocket touchWandSocket;

    private final Logger logger = LoggerFactory.getLogger(TouchWandWebSockets.class);
    private List<TouchWandWebSocketListener> listeners = new ArrayList<>();

    private static final String WS_ENDPOINT_TOUCHWAND = "/async";
    private @Nullable URI uri;

    public TouchWandWebSockets(String ipAddress) {

        client = new WebSocketClient();
        touchWandSocket = new TouchWandSocket();
        this.controllerAddress = ipAddress;
    }

    public void connect() {

        try {
            uri = new URI("ws://" + controllerAddress + WS_ENDPOINT_TOUCHWAND);
        } catch (URISyntaxException e) {
            logger.warn("URI not valid {} message {}", uri, e.getMessage());
            return;
        }

        client.setConnectTimeout(10000);
        ClientUpgradeRequest request = new ClientUpgradeRequest();
        request.setSubProtocols("relay_protocol");

        try {
            client.start();
            client.connect(touchWandSocket, uri, request);
        } catch (Exception e) {
            logger.warn("Could not connect webSocket URI {} message {}", uri, e.getMessage());
            return;
        }
    }

    public void dispose() {
        try {
            client.stop();
        } catch (Exception e) {
            logger.warn("Could not stop webSocketClient,  message {}", e.getMessage());
        }
    }

    public synchronized void registerListener(TouchWandWebSocketListener listener) {
        if (!listeners.contains(listener)) {
            logger.trace("Adding TouchWandWebSocket listener {}", listener);
            listeners.add(listener);
        }
    }

    public synchronized void unregisterListener(TouchWandWebSocketListener listener) {
        listeners.remove(listener);
    }

    @WebSocket
    public class TouchWandSocket {
        @SuppressWarnings("unused")
        private @Nullable Session session;

        public TouchWandSocket() {
        }

        @OnWebSocketClose
        public void onClose(int statusCode, String reason) {
            logger.debug("Connection closed: {} - {}", statusCode, reason);
            this.session = null;
        }

        @OnWebSocketConnect
        public void onConnect(Session session) {
            logger.debug("TouchWandWebSockets connected to {}", session.getRemoteAddress().toString());
            this.session = session;
            try {
                session.getRemote().sendString("{\"openhab\": \"openhab\"}");
            } catch (IOException e) {
                logger.warn("sendString : {}", e.getMessage());

            }
        }

        @OnWebSocketMessage
        public void onMessage(String msg) {

            TouchWandUnitData touchWandUnit;
            JsonParser jsonParser = new JsonParser();
            Gson gson = new Gson();
            try {
                JsonObject unitObj = jsonParser.parse(msg).getAsJsonObject();
                if (unitObj.get("type").getAsString().equals("UNIT_CHANGED")) {
                    if (unitObj.get("unit").getAsJsonObject().get("type").getAsString().equals("WallController")) {
                        touchWandUnit = gson.fromJson(unitObj.get("unit").getAsJsonObject(),
                                TouchWandUnitDataWallController.class);
                    } else {
                        touchWandUnit = gson.fromJson(unitObj.get("unit").getAsJsonObject(),
                                TouchWandShutterSwitchUnitData.class);
                    }
                    logger.debug("UNIT_CHANGED: name {} id {} status {}", touchWandUnit.getName(),
                            touchWandUnit.getId(), touchWandUnit.getCurrStatus());
                    for (TouchWandWebSocketListener listener : listeners) {
                        listener.onDataReceived(touchWandUnit);
                    }
                }
            } catch (JsonSyntaxException e) {
                logger.warn("jsonParser.parse {} ", e.getMessage());
            }

        }

        @OnWebSocketError
        public void onError(Throwable cause) {
            logger.warn("WebSocket Error: {}", cause.getMessage());
        }
    }

}
