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
package org.openhab.binding.shelly.internal.api2;

import static org.openhab.binding.shelly.internal.api2.dto.Shelly2DeviceJsonDTO.*;
import static org.openhab.binding.shelly.internal.util.ShellyUtils.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;

import javax.ws.rs.core.HttpHeaders;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.openhab.binding.shelly.internal.api.ShellyApiException;
import org.openhab.binding.shelly.internal.api2.dto.Shelly2DeviceJsonDTO.Shelly2DebugLogMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * {@link Shelly2DebugLogSocket} connects to a Gen2+ device's dedicated {@code /debug/log} WebSocket endpoint
 * (separate from the regular {@code /rpc} socket managed by {@link Shelly2RpcSocket}) and forwards decoded log
 * lines to a {@link Shelly2DebugLogListener}. The device only streams to this endpoint while
 * {@code Sys.SetConfig}'s {@code debug.websocket.enable} is armed.
 * <p>
 * This socket does not auto-reconnect: if the connection drops unexpectedly the listener is notified via
 * {@link Shelly2DebugLogListener#onDebugLogClosed(Shelly2DebugLogSocket)} and the caller decides whether to re-arm the
 * feature.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
@WebSocket
public class Shelly2DebugLogSocket {
    private final Logger logger = LoggerFactory.getLogger(Shelly2DebugLogSocket.class);
    private final Gson gson = new Gson();

    private final String thingName;
    private final @Nullable InetSocketAddress deviceSocketAddr;
    private final WebSocketClient client;
    private final Shelly2DebugLogListener listener;

    // All access must be guarded by "this"
    private @Nullable Session session;

    public Shelly2DebugLogSocket(String thingName, @Nullable InetSocketAddress deviceSocketAddr,
            WebSocketClient webSocketClient, Shelly2DebugLogListener listener) {
        this.thingName = thingName;
        this.deviceSocketAddr = deviceSocketAddr;
        this.client = webSocketClient;
        this.listener = listener;
    }

    public void connect(@Nullable String authHeader) throws ShellyApiException {
        InetSocketAddress socketAddr = this.deviceSocketAddr;
        InetAddress inetAddr;
        if (socketAddr == null || (inetAddr = socketAddr.getAddress()) == null) {
            throw new ShellyApiException(thingName + ": Device IP not set");
        }
        int port = socketAddr.getPort();
        String hostHeader = port > 0 ? inetAddr.getHostAddress() + ":" + port : inetAddr.getHostAddress();
        URI uri;
        try {
            uri = new URI("ws://" + hostHeader + SHELLY2_DEBUGLOG_ENDPOINT);
        } catch (URISyntaxException e) {
            throw new ShellyApiException(thingName + ": Invalid Debug Log URI: " + e.getMessage(), e);
        }
        ClientUpgradeRequest request = new ClientUpgradeRequest();
        request.setHeader(HttpHeaders.HOST, hostHeader);
        request.setHeader("Origin", "http://" + hostHeader);
        if (authHeader != null && !authHeader.isEmpty()) {
            // Password-protected devices reject the /debug/log upgrade with 401; the endpoint uses the same
            // HTTP digest as /rpc, so a precomputed Authorization header on the upgrade request unblocks it
            request.setHeader(HttpHeaders.AUTHORIZATION, authHeader);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("{}: Connect Debug Log WebSocket, URI={}", thingName, uri);
        }

        try {
            client.connect(this, uri, request);
        } catch (RuntimeException | IOException e) {
            throw new ShellyApiException("Failed to connect Debug Log WebSocket: " + e.getMessage(), e);
        }
    }

    public void disconnect() {
        Session session;
        synchronized (this) {
            session = this.session;
            this.session = null;
        }
        if (session != null && session.isOpen()) {
            session.close(StatusCode.NORMAL, "Socket closed");
        }
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        synchronized (this) {
            this.session = session;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("{}: Debug Log WebSocket connected {}<-{}", thingName, session.getLocalAddress(),
                    session.getRemoteAddress());
        }
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String receivedMessage) {
        try {
            Shelly2DebugLogMessage message = fromJson(gson, receivedMessage, Shelly2DebugLogMessage.class);
            Integer level = message.level;
            listener.onDebugLogLine(level != null ? level : SHELLY2_DEBUGLOG_LEVEL_INFO, getString(message.data));
        } catch (ShellyApiException | IllegalArgumentException e) {
            logger.debug("{}: Unable to process Debug Log message ({}): {}", thingName, e.getMessage(),
                    receivedMessage);
        }
    }

    @OnWebSocketClose
    public void onClose(Session closingSession, int statusCode, String reason) {
        if (logger.isTraceEnabled()) {
            logger.trace("{}: Debug Log WebSocket closed: {} - {}", thingName, statusCode, reason);
        }
        if (clearSessionIfCurrent(closingSession)) {
            listener.onDebugLogClosed(this);
        }
    }

    @OnWebSocketError
    public void onError(Session closingSession, Throwable cause) {
        logger.debug("{}: Debug Log WebSocket error: {}", thingName, cause.getMessage());
        if (clearSessionIfCurrent(closingSession)) {
            listener.onDebugLogClosed(this);
        }
    }

    /**
     * Clears the active session only if the closing/erroring session is still the current one. A stale session
     * closing after {@link #connect()} already established a newer one must not wipe it or notify the listener.
     *
     * @return {@code true} if this was the active session (listener should be notified), {@code false} otherwise
     */
    private synchronized boolean clearSessionIfCurrent(@Nullable Session closingSession) {
        if (closingSession != null && !closingSession.equals(session)) {
            logger.debug("{}: Ignoring close/error from a stale Debug Log WebSocket session", thingName);
            return false;
        }
        session = null;
        return true;
    }
}
