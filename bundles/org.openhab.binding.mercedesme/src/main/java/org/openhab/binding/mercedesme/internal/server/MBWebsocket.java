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
    private final int CONNECT_TIMEOUT_MS = 10 * 1000;
    private AccountHandler accountHandler;
    private boolean running = false;
    private @Nullable Future<?> sessionFuture;
    private @Nullable Session session;

    public MBWebsocket(AccountHandler ah) {
        accountHandler = ah;
    }

    /**
     * Lifecycle handling
     */
    public void run(int runtimeMS) {
        synchronized (this) {
            if (running) {
                return;
            } else {
                running = true;
            }
        }
        try {
            WebSocketClient client = new WebSocketClient();
            client.setMaxIdleTimeout(CONNECT_TIMEOUT_MS);
            ClientUpgradeRequest request = accountHandler.getClientUpgradeRequest();
            logger.info("Websocket start");
            client.start();
            sessionFuture = client.connect(this, new URI(accountHandler.getWSUri()), request);
            Thread.sleep(runtimeMS);
            logger.info("Websocket stop");
            client.stop();
            client.destroy();
        } catch (Exception e) {
            logger.warn("Websocket handling exception: {}", e.getMessage());
        }
        synchronized (this) {
            running = false;
        }
    }

    /**
     * endpoints
     */

    @OnWebSocketMessage
    public void onBytes(InputStream is) {
        try {
            PushMessage pm = VehicleEvents.PushMessage.parseFrom(is);
            // data update
            if (pm.hasVepUpdates()) {
                accountHandler.distributeVepUpdates(pm.getVepUpdates().getUpdatesMap());
            } else if (pm.hasAssignedVehicles()) {
                for (int i = 0; i < pm.getAssignedVehicles().getVinsCount(); i++) {
                    String vin = pm.getAssignedVehicles().getVins(0);
                    accountHandler.discovery(vin);
                }
            }
        } catch (

        IOException e) {
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
