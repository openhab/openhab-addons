/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.evcc.internal.handler.ws;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import org.openhab.binding.evcc.internal.handler.EvccWsBridgeHandler;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * The {@link EvccWebSocket} class implements the WebSocket interface and is responsible for handling the WebSocket
 * connection and forwarding the messages to the appropriate handler.
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
@WebSocket
public class EvccWebSocket {

    private final EvccWsBridgeHandler parent;
    private final Gson gson = new Gson();

    public EvccWebSocket(EvccWsBridgeHandler parent) {
        this.parent = parent;
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        parent.onWsConnected(session);
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) {
        JsonObject obj = gson.fromJson(message, JsonObject.class);
        parent.onWsMessage(session, obj);
    }

    @OnWebSocketError
    public void onError(Session session, Throwable cause) {
        parent.onWsError(cause);
    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        parent.onWsClosed(session, statusCode, reason);
    }
}
