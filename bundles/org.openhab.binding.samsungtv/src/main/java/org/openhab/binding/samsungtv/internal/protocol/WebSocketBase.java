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
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.eclipse.jetty.websocket.api.WebSocketPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Websocket base class
 *
 * @author Arjan Mels - Initial contribution
 * @author Nick Waterton - refactoring
 */
@NonNullByDefault
class WebSocketBase extends WebSocketAdapter {
    private final Logger logger = LoggerFactory.getLogger(WebSocketBase.class);
    final RemoteControllerWebSocket remoteControllerWebSocket;
    final int bufferSize = 1048576; // 1 Mb

    private Optional<Future<?>> sessionFuture = Optional.empty();
    private volatile Optional<ScheduledFuture<?>> pingJob = Optional.empty();

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

    @Override
    public void onWebSocketClose(int statusCode, @Nullable String reason) {
        logger.debug("{}: {} connection closed: {} - {}", host, className, statusCode, reason);
        super.onWebSocketClose(statusCode, reason);
        stopPing();
        if (statusCode == 1001) {
            // timeout
            reconnect();
        }
        if (statusCode == 1002 || statusCode == 1006) {
            // Disconnected, 1002 when the TV closes with an invalid close code after a channel timeout
            reconnect();
        }
    }

    @Override
    public void onWebSocketError(@Nullable Throwable error) {
        logger.debug("{}: {} connection error {}", host, className, error != null ? error.getMessage() : "");
        super.onWebSocketError(error);
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
                        openSession(u);
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
        openSession(uri);
    }

    // does not reset the reconnect count, so reconnect() gives up after its retries
    private void openSession(URI uri) throws RemoteControllerException {
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

    @Override
    public void onWebSocketConnect(@Nullable Session session) {
        logger.debug("{}: {} connection established: {}", host, className,
                session != null ? session.getRemoteAddress().getHostString() : "");
        if (session != null) {
            final WebSocketPolicy currentPolicy = session.getPolicy();
            currentPolicy.setInputBufferSize(bufferSize);
            currentPolicy.setMaxTextMessageSize(bufferSize);
            currentPolicy.setMaxBinaryMessageSize(bufferSize);
            logger.trace("{}: {} Buffer Size set to {} Mb", host, className,
                    Math.round((bufferSize / 1048576.0) * 100.0) / 100.0);
            // avoid 5 minute idle timeout
            stopPing();
            pingJob = Optional.of(remoteControllerWebSocket.callback.handler.getScheduler()
                    .scheduleWithFixedDelay(() -> sendPing(session), 4, 4, TimeUnit.MINUTES));
        }
        super.onWebSocketConnect(session);
        count = 0;
    }

    /**
     * Ping now, so a session left half-open by a TV restart fails and is reconnected
     */
    void ping() {
        Session session = getSession();
        if (session != null && isConnected()) {
            sendPing(session);
        }
    }

    private void sendPing(Session session) {
        try {
            session.getRemote().sendPing(ByteBuffer.wrap("Ping".getBytes()));
        } catch (IOException e) {
            logger.debug("{}: {} ping failed: {}", host, className, e.getMessage());
        }
    }

    private void stopPing() {
        pingJob.ifPresent(job -> job.cancel(true));
        pingJob = Optional.empty();
    }

    void close() {
        stopPing();
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
        try {
            if (isConnected()) {
                getRemote().sendString(cmd);
                logger.trace("{}: {}: sendCommand: {}", host, className, cmd);
            } else {
                logger.warn("{}: {} not connected: {}", host, className, cmd);
            }
        } catch (IOException e) {
            logger.warn("{}: {}: cannot send command: {}", host, className, e.getMessage());
        }
    }

    @Override
    public void onWebSocketText(@Nullable String str) {
        logger.trace("{}: {}: onWebSocketText: {}", host, className, str);
    }

    @Override
    public void onWebSocketBinary(byte @Nullable [] arr, int pos, int len) {
        logger.trace("{}: {}: onWebSocketBinary: offset: {}, len: {}", host, className, pos, len);
    }
}
