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
package org.openhab.binding.yioremote.internal.utils;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link Websocket} represents a single WebSocket connection to the YIO Dock. A new instance is created for every
 * connection attempt.
 *
 * @author Michael Loercher - Initial contribution
 */
@NonNullByDefault
@WebSocket
public class Websocket {

    private final Logger logger = LoggerFactory.getLogger(Websocket.class);
    private final WebsocketInterface listener;
    private volatile @Nullable Session session;

    public Websocket(WebsocketInterface listener) {
        this.listener = listener;
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        this.session = session;
        listener.onConnect(this);
    }

    @OnWebSocketMessage
    public void onText(Session session, String message) {
        listener.onMessage(this, message);
    }

    @OnWebSocketError
    public void onError(Throwable cause) {
        listener.onError(this, cause);
    }

    @OnWebSocketClose
    public void onClose(int statusCode, @Nullable String reason) {
        session = null;
        listener.onClose(this, statusCode, reason);
    }

    public void sendMessage(String message) {
        Session localSession = session;
        if (localSession != null && localSession.isOpen()) {
            localSession.getRemote().sendStringByFuture(message);
        } else {
            logger.debug("WebSocket is not connected, message not sent");
        }
    }

    public void close() {
        Session localSession = session;
        session = null;
        if (localSession != null) {
            localSession.close();
        }
    }
}
