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
package org.openhab.binding.mercedesme.internal.server;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.openhab.binding.mercedesme.internal.handler.AccountHandler;
import org.openhab.binding.mercedesme.internal.proto.VehicleEvents;
import org.openhab.binding.mercedesme.internal.proto.VehicleEvents.PushMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MBWebsocket} class provides authentication callback endpoint
 *
 * @author Bernd Weymann - Initial contribution
 */

@WebSocket
public class MBWebsocket {
    private final Logger logger = LoggerFactory.getLogger(MBWebsocket.class);
    private final int OPEN_TIME_MS = 30000;
    private WebSocketClient client;
    private AccountHandler accountHandler;
    private @Nullable Future<?> sessionFuture;
    private @Nullable Session session;

    public MBWebsocket(AccountHandler ah) {
        accountHandler = ah;
    }

    /**
     * Lifecycle handling
     */

    public void open() {
        WebSocketClient client = this.client;
        if (client == null || !client.isRunning() || !isConnected()) {
            if (client != null) {
                try {
                    client.stop();
                } catch (Exception e) {
                    logger.warn("OPEN FRAME - Failed to stop websocket client: {}", e.getMessage());
                }
                client.destroy();
            }
            client.setMaxIdleTimeout(OPEN_TIME_MS);
            this.client = client;

            ClientUpgradeRequest request = accountHandler.getClientUpgradeRequest();

            try {
                logger.debug("Starting Websocket connection");
                client.start();
            } catch (Exception e) {
                logger.warn("Websocket Start Exception: {}", e.getMessage());
            }
            try {
                logger.debug("Connecting Websocket connection");
                sessionFuture = client.connect(this, new URI(accountHandler.getWSUri()), request);
                try {
                    Thread.sleep(OPEN_TIME_MS);
                } catch (InterruptedException e) {
                }
                if (!isConnected()) {
                    logger.warn("Unable to establish websocket session - Reattempting connection on next refresh");
                } else {
                    logger.debug("Websocket session established");
                }
            } catch (IOException e) {
                logger.warn("Websocket Connect Exception: {}", e.getMessage());
            } catch (URISyntaxException e) {
                logger.warn("Websocket URI Exception: {}", e.getMessage());
            }
        } else {
            logger.warn("Open: Websocket client already running");
        }
    }

    public boolean isConnected() {
        Session session = this.session;
        return session != null && session.isOpen();
    }

    public void close() {
        Session session = this.session;
        if (session != null) {
            try {
                session.close();
            } catch (Exception e) {
                logger.warn("Unable to disconnect session");
            }
            this.session = null;
        }
        Future<?> sessionFuture = this.sessionFuture;
        if (sessionFuture != null && !sessionFuture.isDone()) {
            sessionFuture.cancel(true);
        }
        WebSocketClient client = this.client;
        if (client != null) {
            try {
                client.stop();
            } catch (Exception e) {
                logger.warn("CLOSE FRAME - Failed to stop websocket client: {}", e.getMessage());
            }
            client.destroy();
        }
    }

    public void dispose() {
        if (isConnected()) {
            close();
            WebSocketClient client = this.client;
            if (client != null) {
                try {
                    logger.debug("DISPOSE - Stopping and Terminating Websocket connection");
                    client.stop();
                } catch (Exception e) {
                    logger.warn("Websocket Client Stop Exception: {}", e.getMessage());
                }
                client.destroy();
                this.client = null;
            }
        }
    }

    /**
     * endpoints
     */

    @OnWebSocketMessage
    public void onBytes(InputStream is) {
        try {
            PushMessage pm = VehicleEvents.PushMessage.parseFrom(is);
            Map m = pm.getAllFields();
            // FieldDescriptor fd = new FieldDescriptor()
            Set keys = m.keySet();
            for (Iterator iterator = keys.iterator(); iterator.hasNext();) {
                Object object = iterator.next();
                logger.info("{}", object);
            }
        } catch (IOException e) {
            logger.warn("Error parsing message {}", e.getMessage());
        }
    }

    @OnWebSocketClose
    public void onDisconnect(Session session, int statusCode, String reason) {
        logger.info("Disonnected from server. Status {} Reason {}", statusCode, reason);
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        logger.info("Connected to server");
        this.session = session;
    }

    @OnWebSocketError
    public void onError(Throwable t) {
        logger.warn("Error {}", t.getMessage());
    }
}
