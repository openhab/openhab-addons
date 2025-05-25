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

import static java.util.Objects.requireNonNull;

import java.net.URI;
import java.net.URISyntaxException;
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
 * EmbyConnection provides an API for accessing an Emby device.
 *
 * All nullable fields are checked via a local copy + null-guard before use,
 * per openHAB’s Null Annotations guidelines.
 *
 * @author Zachary Christiansen - Initial contribution
 */
@NonNullByDefault
public class EmbyConnection implements EmbyClientSocketEventListener, AutoCloseable {

    private final Logger logger = LoggerFactory.getLogger(EmbyConnection.class);

    private String hostname = "";
    private int embyport = 0;

    private @Nullable URI wsUri;
    private @Nullable EmbyClientSocket socket;
    private @Nullable ScheduledExecutorService schedulerInstance;

    private int refreshRate = 0;

    private final EmbyBridgeListener listener;
    private final WebSocketClient sharedWebSocketClient;

    public EmbyConnection(EmbyBridgeListener listener, WebSocketClient embyWebSocketClient) {
        this.listener = listener;
        this.sharedWebSocketClient = embyWebSocketClient;
    }

    @Override
    public synchronized void onConnectionClosed() {
        listener.updateConnectionState(false);
    }

    @Override
    public synchronized void onConnectionOpened() {
        listener.updateConnectionState(true);

        // local copy + guard for socket:
        final EmbyClientSocket sock = this.socket;
        // once the connection is open, start sessions (if connected)
        if (sock != null && sock.isConnected()) {
            sock.sendCommand("SessionsStart", "0," + refreshRate);
        } else {
            logger.warn("onConnectionOpened() called but socket is not yet connected");
        }
    }

    public synchronized void connect(String setHostName, int port, String apiKey, ScheduledExecutorService scheduler,
            int refreshRate) {
        this.schedulerInstance = scheduler;
        this.hostname = setHostName;
        this.embyport = port;
        this.refreshRate = refreshRate;

        try {
            close(); // tear down any previous socket

            // build and store the WS URI
            wsUri = new URI("ws", null, hostname, embyport, null, "api_key=" + apiKey, null);

            // create and start the socket
            EmbyClientSocket localSocket = requireNonNull(
                    new EmbyClientSocket(this, wsUri, scheduler, sharedWebSocketClient),
                    "EmbyClientSocket constructor returned null");
            localSocket.attemptReconnect();
            this.socket = localSocket;
        } catch (URISyntaxException e) {
            logger.error("Exception constructing URI host={}, port={}", hostname, embyport, e);
        }
    }

    @Override
    public synchronized void close() {
        final EmbyClientSocket sock = this.socket;
        if (sock != null) {
            sock.close(); // stops reconnection internally
            this.socket = null;
        }
    }

    /**
     * Cleans up all resources held by this connection:
     * - closes the WebSocket socket (stopping any reconnection loops)
     * - shuts down the scheduler if present
     */
    public synchronized void dispose() {
        // close the socket
        close();
        ScheduledExecutorService localScheduler = schedulerInstance;
        // shut down the scheduler
        if (localScheduler != null) {
            try {
                localScheduler.shutdownNow();
                if (!localScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    logger.warn("Scheduler did not terminate cleanly");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.warn("Interrupted while shutting down scheduler", e);
            }
            schedulerInstance = null;
        }

        listener.updateConnectionState(false);
        logger.debug("Disposed EmbyConnection for {}:{}", hostname, embyport);
    }

    @Override
    public void handleEvent(EmbyPlayStateModel playstate) {
        logger.debug("Received event from EMBY server, passing to bridge: host={}, port={}", hostname, embyport);
        listener.handleEvent(playstate, hostname, embyport);
    }

    public boolean checkConnection() {
        final EmbyClientSocket sock = this.socket;
        if (!(sock instanceof EmbyClientSocket) || !sock.isConnected()) {
            logger.debug("Connection down, scheduling reconnect to {}", wsUri);
            if (sock instanceof EmbyClientSocket) {
                sock.attemptReconnect();
            }
            return false;
        }
        return true;
    }

    public String getConnectionName() {
        // blow up early if no URI
        final URI uri = requireNonNull(wsUri, "EmbyConnection not initialized (call connect() first)");
        return uri.toString();
    }
}
