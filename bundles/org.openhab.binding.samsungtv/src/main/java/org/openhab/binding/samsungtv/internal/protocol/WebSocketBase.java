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
package org.openhab.binding.samsungtv.internal.protocol;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.websocket.api.Callback;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketOpen;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Websocket base class
 *
 * @author Arjan Mels - Initial contribution
 * @author Nick Waterton - refactoring
 */
@NonNullByDefault
@WebSocket
class WebSocketBase {
    private final Logger logger = LoggerFactory.getLogger(WebSocketBase.class);
    final RemoteControllerWebSocket remoteControllerWebSocket;
    final int bufferSize = 1048576; // 1 Mb

    protected @Nullable Session session;

    private Optional<Future<?>> sessionFuture = Optional.empty();

    private String host = "";
    private String className = "";
    private Optional<URI> uri = Optional.empty();
    private int count = 0;

    /**
     * @param remoteControllerWebSocket
     */
    WebSocketBase(RemoteControllerWebSocket remoteControllerWebSocket) {
        this.remoteControllerWebSocket = remoteControllerWebSocket;
        this.host = remoteControllerWebSocket.host;
        this.className = this.getClass().getSimpleName();
    }

    public boolean isConnected() {
        Session s = session;
        return s != null && s.isOpen();
    }

    protected @Nullable Session getSession() {
        return session;
    }

    @OnWebSocketClose
    public void onWebSocketClose(int statusCode, @Nullable String reason) {
        this.session = null;
        logger.debug("{}: {} connection closed: {} - {}", host, className, statusCode, reason);
        if (statusCode == 1001) {
            // timeout
            reconnect();
        }
        if (statusCode == 1006) {
            // Disconnected
            reconnect();
        }
    }

    @OnWebSocketError
    public void onWebSocketError(@Nullable Throwable error) {
        logger.debug("{}: {} connection error {}", host, className, error != null ? error.getMessage() : "");
    }

    void reconnect() {
        if (!isConnected()) {
            if (sessionFuture.isPresent() && count++ < 4) {
                uri.ifPresent(u -> {
                    try {
                        logger.debug("{}: Reconnecting : {} try: {}", host, className, count);
                        remoteControllerWebSocket.callback.handler.getScheduler().schedule(() -> {
                            reconnect();
                        }, 2000, TimeUnit.MILLISECONDS);
                        connect(u);
                    } catch (RemoteControllerException e) {
                        logger.warn("{} Reconnect Failed {} : {}", host, className, e.getMessage());
                    }
                });
            } else {
                count = 0;
            }
        }
    }

    void connect(URI uri) throws RemoteControllerException {
        count = 0;
        if (isConnected() || sessionFuture.map(sf -> !sf.isDone()).orElse(false)) {
            logger.trace("{}: {} already connecting or connected", host, className);
            return;
        }
        logger.debug("{}: {} connecting to: {}", host, className, uri);
        this.uri = Optional.of(uri);
        try {
            sessionFuture = Optional.of(remoteControllerWebSocket.client.connect(this, uri));
        } catch (IOException | IllegalStateException e) {
            throw new RemoteControllerException(e);
        }
    }

    @OnWebSocketOpen
    public void onWebSocketConnect(@Nullable Session session) {
        this.session = session;
        logger.debug("{}: {} connection established: {}", host, className,
                session != null ? session.getRemoteSocketAddress().toString() : "");
        if (session != null) {
            logger.trace("{}: {} Buffer Size set to {} Mb", host, className,
                    Math.round((bufferSize / 1048576.0) * 100.0) / 100.0);
            // avoid 5 minute idle timeout
            final Session s = session;
            remoteControllerWebSocket.callback.handler.getScheduler().scheduleWithFixedDelay(() -> {
                String data = "Ping";
                ByteBuffer payload = ByteBuffer.wrap(data.getBytes());
                s.sendPing(payload, Callback.NOOP);
            }, 4, 4, TimeUnit.MINUTES);
        }
        count = 0;
    }

    void close() {
        this.sessionFuture.ifPresent(sf -> {
            if (!sf.isDone()) {
                logger.trace("{}: Cancelling session Future: {}", host, sf);
                sf.cancel(true);
            }
        });
        sessionFuture = Optional.empty();
        Optional.ofNullable(getSession()).ifPresent(s -> {
            logger.debug("{}: {} Connection close requested", host, className);
            s.close();
        });
    }

    void sendCommand(String cmd) {
        Session s = session;
        if (s != null && s.isOpen()) {
            s.sendText(cmd, Callback.NOOP);
            logger.trace("{}: {}: sendCommand: {}", host, className, cmd);
        } else {
            logger.warn("{}: {} not connected: {}", host, className, cmd);
        }
    }

    @OnWebSocketMessage
    public void onWebSocketText(@Nullable String str) {
        logger.trace("{}: {}: onWebSocketText: {}", host, className, str);
    }

    @OnWebSocketMessage
    public void onWebSocketBinary(byte @Nullable [] arr, int pos, int len) {
        logger.trace("{}: {}: onWebSocketBinary: offset: {}, len: {}", host, className, pos, len);
    }
}
