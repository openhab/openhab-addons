/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.yioremote.internal;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link YIOremoteDockWebsocket} is responsible for the Websocket Connection to the YIO Remote Dock
 *
 * @author Michael Loercher - Initial contribution
 */

@NonNullByDefault
@WebSocket
public class YIOremoteDockWebsocket {

    private @Nullable Session session;
    private String stringreceivedmessage = "";
    private final Logger logger = LoggerFactory.getLogger(YIOremoteDockWebsocket.class);
    private @Nullable YIOremoteDockWebsocketInterface websocketHandler;

    public void addMessageHandler(YIOremoteDockWebsocketInterface yioremotedockwebsocketinterfacehandler) {
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
        logger.warn("WebSocketError {}", cause.getMessage());
        if (websocketHandler != null) {
            websocketHandler.onError();
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
}
