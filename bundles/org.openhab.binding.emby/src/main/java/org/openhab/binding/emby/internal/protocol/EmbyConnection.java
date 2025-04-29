/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

package org.openhab.binding.emby.internal.protocol;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.openhab.binding.emby.internal.EmbyBridgeListener;
import org.openhab.binding.emby.internal.model.EmbyPlayStateModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * EmbyConnection provides an API for accessing a Emby device.
 *
 * @author Zachary Christiansen - Initial contribution
 */
@NonNullByDefault
public class EmbyConnection implements EmbyClientSocketEventListener {

    private final Logger logger = LoggerFactory.getLogger(EmbyConnection.class);
    private int refreshRate;
    private @Nullable URI wsUri;
    private @Nullable EmbyClientSocket socket;
    private @Nullable ScheduledExecutorService scheduler;
    private final EmbyBridgeListener listener;
    private WebSocketClient sharedWebSocketClient;
    private @Nullable String hostname;
    private int embyport;

    public EmbyConnection(EmbyBridgeListener listener, WebSocketClient client) {
        this.listener = listener;
        this.sharedWebSocketClient = client;
    }

    @Override
    public void handleEvent(EmbyPlayStateModel playstate) {
        // hostname is initialized in connect(...); enforce non-null here
        String host = Objects.requireNonNull(this.hostname,
                "EmbyConnection.hostname must be set before handling events");
        logger.debug("Received event from EMBY server passing it to the bridge handler with hostname {} and port {}",
                host, embyport);
        listener.handleEvent(playstate, host, this.embyport);
    }

    @Override
    public synchronized void onConnectionOpened() {
        // Notify openHAB framework we're ONLINE
        listener.updateConnectionState(true);
        // Start session polling
        if (socket != null) {
            socket.callMethodString("SessionsStart", "0," + this.refreshRate);
        }
    }

    @Override
    public synchronized void onConnectionClosed() {
        // Notify framework we're OFFLINE immediately
        listener.updateConnectionState(false);
    }

    /**
     * Connect using the HTTP base URL configured by the user (e.g. "http://host:8096/emby").
     */
    public synchronized void connect(String httpBaseUrl, String apiKey, ScheduledExecutorService sched, int refreshRate,
            int bufferSize) {

        this.scheduler = sched;
        this.refreshRate = refreshRate;

        try {
            // derive WS URI from whatever HTTP URL the user gave us
            URI httpUri = new URI(httpBaseUrl);
            // keep host/port for later event callbacks
            this.hostname = httpUri.getHost();
            this.embyport = httpUri.getPort();

            String wsScheme = httpUri.getScheme().equalsIgnoreCase("https") ? "wss" : "ws";
            String path = httpUri.getPath();
            String query = "api_key=" + apiKey;
            wsUri = new URI(wsScheme, null, httpUri.getHost(), httpUri.getPort(), path, query, null);

            // tear down any old socket
            close();

            // build new socket with shared client
            socket = new EmbyClientSocket(this, wsUri, sched, bufferSize, sharedWebSocketClient);

            // first attempt now; if it fails, schedule exactly one retry in 60s
            if (!checkConnection()) {
                logger.warn("Initial connect to {} failed; retrying in one minute", wsUri);
                scheduler.schedule(() -> connect(httpBaseUrl, apiKey, sched, refreshRate, bufferSize), 1,
                        TimeUnit.MINUTES);
            }

        } catch (URISyntaxException e) {
            logger.error("Invalid HTTP base URL '{}': {}", httpBaseUrl, e.getMessage(), e);
        }
    }

    public synchronized boolean checkConnection() {
        if (socket == null) {
            return false;
        }
        try {
            if (!socket.isConnected()) {
                logger.debug("checkConnection: opening {}", wsUri);
                socket.open();
            }
            return socket.isConnected();
        } catch (Exception e) {
            logger.error("Connection attempt to {} failed: {}", wsUri, e.getMessage());
            socket.close();
            return false;
        }
    }

    public synchronized void close() {
        if (socket != null) {
            socket.close();
            socket = null;
        }
    }

    public String getConnectionName() {
        return wsUri != null ? wsUri.toString() : "";
    }
}
