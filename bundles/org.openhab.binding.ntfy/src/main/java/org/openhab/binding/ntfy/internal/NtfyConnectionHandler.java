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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.openhab.binding.ntfy.internal.network.NtfySender;
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
    private HttpClient httpClient;
    private @Nullable ScheduledFuture<?> retryConnectionFuture;
    private @Nullable String scheme;

    /**
     * Creates a new {@link NtfyConnectionHandler} for the given bridge (connection) thing.
     *
     * @param thing the bridge thing representing the ntfy connection
     * @param webSocketFactory factory used to create WebSocketClient instances
     */
    public NtfyConnectionHandler(Bridge thing, WebSocketFactory webSocketFactory, HttpClient httpClient) {
        super(thing);
        this.webSocketFactory = webSocketFactory;
        this.httpClient = httpClient;
        config = getConfigAs(NtfyConnectionConfiguration.class);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    public NtfySender CreateSender(String topicName) {
        return new NtfySender(topicName, httpClient, this);
    }

    @Override
    public void initialize() {
        cancelRetryFuture();

        config = getConfigAs(NtfyConnectionConfiguration.class);

        updateStatus(ThingStatus.UNKNOWN);

        String scheme;
        try {
            scheme = (new URI(config.hostname)).getScheme();
        } catch (URISyntaxException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getLocalizedMessage());
            return;
        }
        if (!"http".equals(scheme) && !"https".equals(scheme)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "URI scheme is missing in hostname or unsupported URI scheme (only http or https are supported): "
                            + config.hostname);
            return;
        }

        this.scheme = scheme;
        updateStatus(ThingStatus.ONLINE);
    }

    private void cancelRetryFuture() {
        final @Nullable ScheduledFuture<?> retryConnectionFuture = this.retryConnectionFuture;
        this.retryConnectionFuture = null;
        if (retryConnectionFuture != null) {
            retryConnectionFuture.cancel(true);
        }
    }

    /**
     * Creates a new Jetty {@link WebSocketClient} for the provided thing and registers
     * it under the thing's UID. If an existing client is present and running it will be
     * stopped before the new client is stored.
     *
     * @param thing the thing for which to create and register the WebSocket client
     */
    public synchronized void createAndRegisterWebSocketClient(Thing thing) {
        WebSocketClient newClient = webSocketFactory.createWebSocketClient(
                ThingWebClientUtil.buildWebClientConsumerName(thing.getUID(), NtfyBindingConstants.BINDING_ID));

        WebSocketClient client = webSocketConnections.get(thing.getUID());

        if (client != null && client.isRunning()) {
            try {
                client.stop();
            } catch (Exception e) {
                logger.warn("Error stopping WebSocketConnection", e);
            }
            webSocketConnections.remove(thing.getUID());
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
            if (client.isStarted()) {
                try {
                    client.stop();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.error("WebSocket connection was interrupted", e);
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getLocalizedMessage());
                    return false;
                } catch (Exception e) {
                    logger.warn("Error stopping WebSocket connection - ignore it and continue", e);
                }
            }
            try {
                client.start();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getLocalizedMessage());
                return false;
            } catch (Exception e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getLocalizedMessage());
                return false;
            }

            client.setMaxIdleTimeout(config.connectionTimeout);
            String webSocketScheme = "http".equals(this.scheme) ? "ws" : "wss";

            try {
                client.connect(ntfyWebSocket,
                        new URI(webSocketScheme + ":"
                                + (new URI(config.hostname + "/" + topicName + "/ws")).getRawSchemeSpecificPart()),
                        setupRequest());
            } catch (IOException | URISyntaxException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getLocalizedMessage());
                return false;
            }
            updateStatus(ThingStatus.ONLINE);

        }
        return true;
    }

    private String getTopicNameFromThing(Thing topicThing) {
        return topicThing.getConfiguration().as(NtfyTopicConfiguration.class).topicName;
    }

    /**
     * Handles a connection error by setting the thing status to OFFLINE and scheduling
     * a retry to set the status back to ONLINE after a fixed delay.
     *
     * @param cause the cause of the connection error
     */
    public void connectionError(Throwable cause) {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, cause.getLocalizedMessage());
        retryConnectionFuture = scheduler.schedule(() -> {
            retryConnectionFuture = null;
            updateStatus(ThingStatus.ONLINE);
        }, 30, TimeUnit.SECONDS);
    }

    @Override
    public void dispose() {
        super.dispose();

        cancelRetryFuture();

        webSocketConnections.values().forEach(client -> {
            try {
                if (client.isRunning()) {
                    client.stop();
                }
            } catch (Exception e) {
                logger.warn("Error stopping WebSocketConnection", e);
            }
        });
    }
}
