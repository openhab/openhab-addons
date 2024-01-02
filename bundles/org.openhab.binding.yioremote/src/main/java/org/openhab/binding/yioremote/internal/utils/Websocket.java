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
package org.openhab.binding.yioremote.internal.utils;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link Websocket} is responsible for the Websocket Connection
 *
 * @author Michael Loercher - Initial contribution
 */

@NonNullByDefault
@WebSocket
public class Websocket {

    private @Nullable Session session;
    private final Logger logger = LoggerFactory.getLogger(Websocket.class);
    private @Nullable WebsocketInterface websocketHandler;

    public void addMessageHandler(WebsocketInterface yioremotedockwebsocketinterfacehandler) {
        this.websocketHandler = yioremotedockwebsocketinterfacehandler;
    }

    @OnWebSocketMessage
    public void onText(Session session, String receivedMessage) {
        if (websocketHandler != null) {
            websocketHandler.onMessage(receivedMessage);
        }
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        this.session = session;
        if (websocketHandler != null) {
            websocketHandler.onConnect(true);
        }
    }

    @OnWebSocketError
    public void onError(Throwable cause) {
        logger.debug("WebSocketError {}", cause.getMessage());
        if (websocketHandler != null) {
            websocketHandler.onError(cause);
        }
    }

    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        if (statusCode != StatusCode.NORMAL) {
            logger.debug("WebSocket Connection closed: {} - {}", statusCode, reason);
        }
        if (session != null) {
            if (!session.isOpen()) {
                if (session != null) {
                    session.close();
                }
            }
            session = null;
        }
        if (websocketHandler != null) {
            websocketHandler.onClose();
        }
    }

    public void sendMessage(String str) {
        if (session != null) {
            try {
                session.getRemote().sendString(str);
            } catch (IOException e) {
                logger.warn("Error during sendMessage function {}", e.getMessage());
            }
        }
    }

    public void closeWebsocketSession() {
        if (session != null) {
            session.close();
        }
    }
}
