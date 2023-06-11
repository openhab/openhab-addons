/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.samsungtv.internal.protocol;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.Future;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Websocket base class
 *
 * @author Arjan Mels - Initial contribution
 */
@NonNullByDefault
class WebSocketBase extends WebSocketAdapter {
    private final Logger logger = LoggerFactory.getLogger(WebSocketBase.class);
    /**
     *
     */
    final RemoteControllerWebSocket remoteControllerWebSocket;

    private @Nullable Future<?> sessionFuture;

    /**
     * @param remoteControllerWebSocket
     */
    WebSocketBase(RemoteControllerWebSocket remoteControllerWebSocket) {
        this.remoteControllerWebSocket = remoteControllerWebSocket;
    }

    boolean isConnecting = false;

    @Override
    public void onWebSocketClose(int statusCode, @Nullable String reason) {
        logger.debug("{} connection closed: {} - {}", this.getClass().getSimpleName(), statusCode, reason);
        super.onWebSocketClose(statusCode, reason);
        isConnecting = false;
    }

    @Override
    public void onWebSocketError(@Nullable Throwable error) {
        if (logger.isTraceEnabled()) {
            logger.trace("{} connection error", this.getClass().getSimpleName(), error);
        } else {
            logger.debug("{} connection error", this.getClass().getSimpleName());
        }
        super.onWebSocketError(error);
        isConnecting = false;
    }

    void connect(URI uri) throws RemoteControllerException {
        if (isConnecting || isConnected()) {
            logger.trace("{} already connecting or connected", this.getClass().getSimpleName());
            return;
        }

        logger.debug("{} connecting to: {}", this.getClass().getSimpleName(), uri);
        isConnecting = true;

        try {
            sessionFuture = remoteControllerWebSocket.client.connect(this, uri);
            logger.trace("Connecting session Future: {}", sessionFuture);
        } catch (IOException | IllegalStateException e) {
            throw new RemoteControllerException(e);
        }
    }

    @Override
    public void onWebSocketConnect(@Nullable Session session) {
        logger.debug("{} connection established: {}", this.getClass().getSimpleName(),
                session != null ? session.getRemoteAddress().getHostString() : "");
        super.onWebSocketConnect(session);

        isConnecting = false;
    }

    void close() {
        logger.debug("{} connection close requested", this.getClass().getSimpleName());

        Session session = getSession();
        if (session != null) {
            session.close();
        }

        final Future<?> sessionFuture = this.sessionFuture;
        logger.trace("Closing session Future: {}", sessionFuture);
        if (sessionFuture != null && !sessionFuture.isDone()) {
            sessionFuture.cancel(true);
        }
    }

    void sendCommand(String cmd) {
        try {
            if (isConnected()) {
                getRemote().sendString(cmd);
                logger.trace("{}: sendCommand: {}", this.getClass().getSimpleName(), cmd);
            } else {
                logger.warn("{} sending command while socket not connected: {}", this.getClass().getSimpleName(), cmd);
                // retry opening connection just in case
                remoteControllerWebSocket.openConnection();

                getRemote().sendString(cmd);
                logger.trace("{}: sendCommand: {}", this.getClass().getSimpleName(), cmd);
            }
        } catch (IOException | RemoteControllerException e) {
            logger.warn("{}: cannot send command", this.getClass().getSimpleName(), e);
        }
    }

    @Override
    public void onWebSocketText(@Nullable String str) {
        logger.trace("{}: onWebSocketText: {}", this.getClass().getSimpleName(), str);
    }
}
