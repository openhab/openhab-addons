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
package org.openhab.binding.lgwebos.internal.handler;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * WebSocket implementation to connect to WebOSTV mouse api.
 *
 * @author Sebastian Prehn - Initial contribution
 *
 */
@WebSocket()
@NonNullByDefault
public class LGWebOSTVMouseSocket {
    private final Logger logger = LoggerFactory.getLogger(LGWebOSTVMouseSocket.class);

    public enum State {
        DISCONNECTED,
        CONNECTING,
        CONNECTED,
        DISCONNECTING
    }

    public enum ButtonType {
        HOME,
        BACK,
        UP,
        DOWN,
        LEFT,
        RIGHT,
    }

    private State state = State.DISCONNECTED;
    private final WebSocketClient client;
    private @Nullable Session session;
    private @Nullable WebOSTVMouseSocketListener listener;

    public LGWebOSTVMouseSocket(WebSocketClient client) {
        this.client = client;
    }

    public State getState() {
        return state;
    }

    private void setState(State state) {
        State oldState = this.state;
        this.state = state;
        Optional.ofNullable(this.listener).ifPresent(l -> l.onStateChanged(oldState, this.state));
    }

    public interface WebOSTVMouseSocketListener {

        public void onStateChanged(State oldState, State newState);

        public void onError(String errorMessage);
    }

    public void setListener(@Nullable WebOSTVMouseSocketListener listener) {
        this.listener = listener;
    }

    public void connect(URI destUri) {
        synchronized (this) {
            if (state != State.DISCONNECTED) {
                logger.debug("Already connecting; not trying to connect again: {}", state);
                return;
            }
            setState(State.CONNECTING);
        }

        try {
            this.client.connect(this, destUri);
            logger.debug("Connecting to: {}", destUri);
        } catch (IOException e) {
            logger.warn("Unable to connect.", e);
            setState(State.DISCONNECTED);
        }
    }

    public void disconnect() {
        setState(State.DISCONNECTING);
        try {
            Optional.ofNullable(this.session).ifPresent(s -> s.close());
        } catch (Exception e) {
            logger.debug("Error connecting to device.", e);
        }
        setState(State.DISCONNECTED);
    }

    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        setState(State.DISCONNECTED);
        logger.debug("WebSocket Closed - Code: {}, Reason: {}", statusCode, reason);
        this.session = null;
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        logger.debug("WebSocket Connected to: {}", session.getRemoteAddress().getAddress());
        this.session = session;
        setState(State.CONNECTED);
    }

    @OnWebSocketMessage
    public void onMessage(String message) {
        logger.debug("Message [in]: {}", message);
    }

    @OnWebSocketError
    public void onError(Throwable cause) {
        String message = cause.getMessage();
        Optional.ofNullable(this.listener).ifPresent(l -> l.onError(message != null ? message : ""));
        logger.debug("Connection Error.", cause);
    }

    private void sendMessage(String msg) {
        Session s = this.session;
        try {
            if (s != null) {
                logger.debug("Message [out]: {}", msg);
                s.getRemote().sendString(msg);
            } else {
                logger.warn("No Connection to TV, skipping [out]: {}", msg);
            }

        } catch (IOException e) {
            logger.error("Unable to send message.", e);
        }
    }

    public void click() {
        sendMessage("type:click\n" + "\n");
    }

    public void button(ButtonType type) {
        String keyName;
        switch (type) {
            case HOME:
                keyName = "HOME";
                break;
            case BACK:
                keyName = "BACK";
                break;
            case UP:
                keyName = "UP";
                break;
            case DOWN:
                keyName = "DOWN";
                break;
            case LEFT:
                keyName = "LEFT";
                break;
            case RIGHT:
                keyName = "RIGHT";
                break;

            default:
                keyName = "NONE";
                break;
        }

        button(keyName);
    }

    public void button(String keyName) {
        sendMessage("type:button\n" + "name:" + keyName + "\n" + "\n");
    }

    public void move(double dx, double dy) {
        sendMessage("type:move\n" + "dx:" + dx + "\n" + "dy:" + dy + "\n" + "down:0\n" + "\n");
    }

    public void move(double dx, double dy, boolean drag) {
        sendMessage("type:move\n" + "dx:" + dx + "\n" + "dy:" + dy + "\n" + "down:" + (drag ? 1 : 0) + "\n" + "\n");
    }

    public void scroll(double dx, double dy) {
        sendMessage("type:scroll\n" + "dx:" + dx + "\n" + "dy:" + dy + "\n" + "\n");
    }
}
