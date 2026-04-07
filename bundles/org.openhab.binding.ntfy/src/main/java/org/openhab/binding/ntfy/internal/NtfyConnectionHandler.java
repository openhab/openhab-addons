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
package org.openhab.binding.ntfy.internal;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.openhab.binding.ntfy.internal.network.NtfyWebSocket;
import org.openhab.core.io.net.http.WebSocketFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.util.ThingWebClientUtil;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NtfyConnectionHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Christian Kittel - Initial contribution
 */
@NonNullByDefault
public class NtfyConnectionHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(NtfyConnectionHandler.class);

    private NtfyConnectionConfiguration config;
    private Map<ThingUID, WebSocketClient> webSocketConnections = new HashMap<>();
    private WebSocketFactory webSocketFactory;

    /**
     * Creates a new {@link NtfyConnectionHandler} for the given bridge (connection) thing.
     *
     * @param thing the bridge thing representing the ntfy connection
     * @param webSocketFactory factory used to create WebSocketClient instances
     */
    public NtfyConnectionHandler(Bridge thing, WebSocketFactory webSocketFactory) {
        super(thing);
        this.webSocketFactory = webSocketFactory;
        config = getConfigAs(NtfyConnectionConfiguration.class);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);

        config = getConfigAs(NtfyConnectionConfiguration.class);

        scheduler.execute(() -> updateStatus(ThingStatus.ONLINE));
    }

    /**
     * Creates a new Jetty {@link WebSocketClient} for the provided thing and registers
     * it under the thing's UID. If an existing client is present and running it will be
     * stopped before the new client is stored.
     *
     * @param thing the thing for which to create and register the WebSocket client
     */
    public synchronized void createAndRegisterWebSocketClient(Thing thing) {
        WebSocketClient newClient = webSocketFactory
                .createWebSocketClient(ThingWebClientUtil.buildWebClientConsumerName(thing.getUID(), "ntfy"));

        WebSocketClient client = webSocketConnections.get(thing.getUID());

        if (client != null && client.isRunning()) {
            try {
                client.stop();
            } catch (Exception e) {
                logger.error("Error stopping WebSocketConnection", e);
            }
        }

        webSocketConnections.put(thing.getUID(), newClient);
    }

    private ClientUpgradeRequest setupRequest() {
        ClientUpgradeRequest request = new ClientUpgradeRequest();
        if (config.isAuthHeaderNeeded()) {
            String authHeader = config.buildAuthHeader();
            request.setHeader("Authorization", authHeader);
        }
        return request;
    }

    /**
     * Starts a WebSocket connection for the provided topic thing using the given
     * {@link NtfyWebSocket} listener.
     *
     * @param topicThing the thing representing the ntfy topic to connect to
     * @param ntfyWebSocket the WebSocket listener that will handle incoming events
     * @return {@code true} when the connection was successfully started or when there
     *         was no client registered for the thing; {@code false} on failure
     */
    public synchronized boolean startWebSocketConnection(Thing topicThing, NtfyWebSocket ntfyWebSocket) {
        WebSocketClient client = webSocketConnections.get(topicThing.getUID());

        String topicName = getTopicNameFromThing(topicThing);

        if (client != null) {
            try {
                if (client.isStarted()) {
                    client.stop();
                }
                client.start();
                client.setMaxIdleTimeout(config.connectionTimeout);

                client.connect(ntfyWebSocket,
                        new URI("wss:"
                                + (new URI(config.hostname + "/" + topicName + "/ws")).getRawSchemeSpecificPart()),
                        setupRequest());
            } catch (Exception e) {
                logger.error("Error creating WebSocketConnection", e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getLocalizedMessage());
                return false;
            }
        }
        return true;
    }

    private String getTopicNameFromThing(Thing topicThing) {
        return topicThing.getConfiguration().as(NtfyTopicConfiguration.class).topicname;
    }

    /**
     * Handles a connection error by setting the thing status to OFFLINE and scheduling
     * a retry to set the status back to ONLINE after a fixed delay.
     *
     * @param cause the cause of the connection error
     */
    public void connectionError(Throwable cause) {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, cause.getLocalizedMessage());
        scheduler.schedule(() -> updateStatus(ThingStatus.ONLINE), 30, TimeUnit.SECONDS);
    }
}
