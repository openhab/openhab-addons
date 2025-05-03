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
    private String hostname;
    private int embyport;
    private @Nullable URI wsUri;
    private @Nullable EmbyClientSocket socket;
    private @Nullable ScheduledExecutorService schedulerInstance;

    private final EmbyBridgeListener listener;
    private WebSocketClient sharedWebSocketClient;
    private volatile boolean hasSubscribed = false;

    public EmbyConnection(EmbyBridgeListener listener, WebSocketClient embyWebSocketClient) {
        this.listener = listener;
        this.hostname = "";
        this.sharedWebSocketClient = embyWebSocketClient;
    }

    @Override
    public synchronized void onConnectionClosed() {
        listener.updateConnectionState(false);
        // Optional: this.checkConnection(); — only needed if socket is null or not retrying
    }

    @Override
    public synchronized void onConnectionOpened() {
        listener.updateConnectionState(true);

        if (socket == null || !socket.isConnected()) {
            schedulerInstance.schedule(this::onConnectionOpened, 50, TimeUnit.MILLISECONDS);
            return;
        }

        if (hasSubscribed) {
            logger.debug("Already subscribed—skipping SessionsStart");
            return;
        }
        hasSubscribed = true;

        socket.sendCommand("SessionsStart", "0," + refreshRate);
    }

    public synchronized void connect(String setHostName, int port, String apiKey, ScheduledExecutorService scheduler,
            int refreshRate, int bufferSize) {
        // remember the scheduler so onConnectionOpened() can reschedule itself
        this.schedulerInstance = scheduler;
        this.hostname = setHostName;
        this.embyport = port;
        this.refreshRate = refreshRate;
        try {
            close();

            wsUri = new URI("ws", null, hostname, embyport, null, "api_key=" + apiKey, null);
            socket = new EmbyClientSocket(this, wsUri, scheduler, bufferSize, sharedWebSocketClient);
            checkConnection();
        } catch (URISyntaxException e) {
            logger.error("exception during constructing URI host={}, port={}", hostname, embyport, e);
        }
    }

    public synchronized void close() {
        if (socket != null) {
            socket.close(); // Internally sets shouldReconnect = false
            socket = null;
        }
    }

    @Override
    public void handleEvent(EmbyPlayStateModel playstate) {
        logger.debug("Received event from EMBY server passing it to the bridge handler with hostname {} and port {}",
                hostname, embyport);
        this.listener.handleEvent(playstate, this.hostname, this.embyport);
    }

    public boolean checkConnection() {
        if (!socket.isConnected()) {
            logger.debug("checkConnection: try to connect to emby {}", wsUri);
            try {
                socket.open();
                return socket.isConnected();
            } catch (Exception e) {
                logger.error("exception during connect to {}", wsUri, e);
                socket.close();
                return false;
            }
        } else {
            return true;
        }
    }

    public String getConnectionName() {
        return wsUri.toString();
    }
}
