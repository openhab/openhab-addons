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
package org.openhab.binding.evcc.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * The {@link EvccWebSocketAdapter} is a thin Jetty WebSocketAdapter
 * implementation that forwards all WebSocket lifecycle events and messages
 * to the {@link EvccWebSocketClient}. It contains no logic of its own and
 * serves purely as an event bridge between Jetty and the EVCC API layer.
 *
 * @author Marcel Goerentz - Initial contribution
 */

@NonNullByDefault
@WebSocket
public class EvccWebSocketAdapter {
    private final EvccWebSocketClient parent;
    private final Gson gson = new Gson();

    public EvccWebSocketAdapter(EvccWebSocketClient parent) {
        this.parent = parent;
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        parent.handleConnected(session);
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) {
        @Nullable
        JsonObject obj = gson.fromJson(message, JsonObject.class);
        parent.handleMessage(obj);
    }

    @OnWebSocketError
    public void onError(Session session, Throwable cause) {
        parent.handleError(cause);
    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        parent.handleClosed(statusCode, reason);
    }
}
