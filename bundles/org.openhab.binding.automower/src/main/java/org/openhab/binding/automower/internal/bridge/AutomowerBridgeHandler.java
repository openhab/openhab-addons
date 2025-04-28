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
package org.openhab.binding.automower.internal.bridge;

import static org.openhab.binding.automower.internal.AutomowerBindingConstants.THING_TYPE_BRIDGE;

import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.eclipse.jetty.websocket.common.WebSocketSession;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.MowerListResult;
import org.openhab.binding.automower.internal.rest.exceptions.AutomowerCommunicationException;
import org.openhab.binding.automower.internal.things.AutomowerHandler;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;
import org.openhab.core.auth.client.oauth2.OAuthClientService;
import org.openhab.core.auth.client.oauth2.OAuthException;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.auth.client.oauth2.OAuthResponseException;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AutomowerBridgeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Markus Pfleger - Initial contribution
 */
@NonNullByDefault
public class AutomowerBridgeHandler extends BaseBridgeHandler {
    private final Logger logger = LoggerFactory.getLogger(AutomowerBridgeHandler.class);

    private static final String HUSQVARNA_API_TOKEN_URL = "https://api.authentication.husqvarnagroup.dev/v1/oauth2/token";

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_BRIDGE);
    private static final long DEFAULT_POLLING_INTERVAL_S = TimeUnit.HOURS.toSeconds(1);

    private final OAuthFactory oAuthFactory;
    private @Nullable WebSocketSession webSocketSession;

    private @Nullable OAuthClientService oAuthService;
    private @Nullable ScheduledFuture<?> automowerBridgePollingJob;
    private @Nullable AutomowerBridge bridge;
    private final HttpClient httpClient;
    private final WebSocketClient webSocketClient;
    private boolean closing;
    private final Map<String, AutomowerHandler> automowerHandlers;

    public AutomowerBridgeHandler(Bridge bridge, OAuthFactory oAuthFactory, HttpClient httpClient,
            WebSocketClient webSocketClient, Map<String, AutomowerHandler> automowerHandlers) {
        super(bridge);
        this.oAuthFactory = oAuthFactory;
        this.httpClient = httpClient;
        this.webSocketClient = webSocketClient;
        this.automowerHandlers = automowerHandlers;
    }

    public WebSocketClient getWebSocketClient() {
        return webSocketClient;
    }

    public ScheduledExecutorService getScheduler() {
        return scheduler;
    }

    public @Nullable WebSocketSession getWebSocketSession() {
        return webSocketSession;
    }

    public void setWebSocketSession(@Nullable WebSocketSession webSocketSession) {
        this.webSocketSession = webSocketSession;
    }

    public boolean isClosing() {
        return closing;
    }

    public void setClosing(boolean closing) {
        this.closing = closing;
    }

    public @Nullable AutomowerHandler getAutomowerHandlerByThingId(@Nullable String thingId) {
        return automowerHandlers.get(thingId);
    }

    private void pollAutomowers(AutomowerBridge bridge) {
        MowerListResult automowers;
        try {
            automowers = bridge.getAutomowers();
            updateStatus(ThingStatus.ONLINE);
            logger.debug("Found {} automowers", automowers.getData().size());
        } catch (AutomowerCommunicationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/comm-error-query-mowers-failed");
            logger.warn("Unable to fetch automowers: {}", e.getMessage());
        }
    }

    @Override
    public void dispose() {
        closing = true;

        AutomowerBridge currentBridge = bridge;
        if (currentBridge != null) {
            stopAutomowerBridgePolling(currentBridge);
            bridge = null;
        }

        if (webSocketSession != null) {
            try {
                webSocketSession.close();
            } catch (Exception e) {
                logger.error("Failed to close WebSocket session: {}", e.getMessage());
            }
        }
        /*
         * if (webSocketClient != null) {
         * try {
         * webSocketClient.stop();
         * } catch (Exception e) {
         * logger.error("Failed to stop WebSocket client: {}", e.getMessage());
         * }
         * }
         */
        OAuthClientService oAuthService = this.oAuthService;
        if (oAuthService != null) {
            oAuthFactory.ungetOAuthService(thing.getUID().getAsString());
            this.oAuthService = null;
        }
        logger.debug("Bridge {} disposed", thing.getUID().getAsString());
    }

    @Override
    public void initialize() {
        AutomowerBridgeConfiguration bridgeConfiguration = getConfigAs(AutomowerBridgeConfiguration.class);

        final String appKey = bridgeConfiguration.getAppKey();
        final String appSecret = bridgeConfiguration.getAppSecret();
        final Integer pollingIntervalS = bridgeConfiguration.getPollingInterval();

        if (appKey == null || appKey.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "@text/conf-error-no-app-key");
        } else if (appSecret == null || appSecret.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "@text/conf-error-no-app-secret");
        } else if (pollingIntervalS != null && pollingIntervalS < 1) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/conf-error-invalid-polling-interval");
        } else {
            OAuthClientService oAuthService = oAuthFactory.createOAuthClientService(thing.getUID().getAsString(),
                    HUSQVARNA_API_TOKEN_URL, null, appKey, appSecret, null, null);
            this.oAuthService = oAuthService;

            if (bridge == null) {
                AutomowerBridge currentBridge = new AutomowerBridge(oAuthService, appKey, httpClient, scheduler);
                bridge = currentBridge;
                startAutomowerBridgePolling(currentBridge, pollingIntervalS);

                connectWebSocket();
            }
            updateStatus(ThingStatus.UNKNOWN);
        }
    }

    @Override
    public void handleRemoval() {
        oAuthFactory.deleteServiceAndAccessToken(thing.getUID().getAsString());
        super.handleRemoval();
    }

    private void startAutomowerBridgePolling(AutomowerBridge bridge, @Nullable Integer pollingIntervalS) {
        ScheduledFuture<?> currentPollingJob = automowerBridgePollingJob;
        if (currentPollingJob == null) {
            final long pollingIntervalToUse = pollingIntervalS == null ? DEFAULT_POLLING_INTERVAL_S : pollingIntervalS;
            automowerBridgePollingJob = scheduler.scheduleWithFixedDelay(() -> pollAutomowers(bridge), 1,
                    pollingIntervalToUse, TimeUnit.SECONDS);
        }
    }

    private void stopAutomowerBridgePolling(AutomowerBridge bridge) {
        ScheduledFuture<?> currentPollingJob = automowerBridgePollingJob;
        if (currentPollingJob != null) {
            currentPollingJob.cancel(true);
            automowerBridgePollingJob = null;
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    public @Nullable AutomowerBridge getAutomowerBridge() {
        return bridge;
    }

    public Optional<MowerListResult> getAutomowers() {
        AutomowerBridge currentBridge = bridge;
        if (currentBridge == null) {
            return Optional.empty();
        }

        try {
            return Optional.of(currentBridge.getAutomowers());
        } catch (AutomowerCommunicationException e) {
            logger.debug("Bridge cannot get list of available automowers {}", e.getMessage());
            return Optional.empty();
        }
    }

    private void connectWebSocket() {
        if (oAuthService == null) {
            logger.error("OAuthService is not initialized, cannot connect WebSocket");
            return;
        }
        try {
            String accessToken = authenticate().getAccessToken();
            if (accessToken == null) {
                logger.error("No OAuth2 access token available for WebSocket connection");
                return;
            }
            String wsUrl = "wss://ws.openapi.husqvarna.dev/v1";
            org.eclipse.jetty.websocket.client.ClientUpgradeRequest request = new org.eclipse.jetty.websocket.client.ClientUpgradeRequest();
            request.setHeader("Authorization", "Bearer " + accessToken);
            webSocketSession = (WebSocketSession) webSocketClient
                    .connect(new AutomowerWebSocketAdapter(this), URI.create(wsUrl), request).get();
        } catch (Exception e) {
            logger.error("Failed to start WebSocket client: {}", e.getMessage());
        }
    }

    public AccessTokenResponse authenticate() throws AutomowerCommunicationException {
        try {
            AccessTokenResponse result = oAuthService.getAccessTokenResponse();
            if (result == null || result.isExpired(Instant.now(), 120)) {
                logger.debug("renew Authentication token");
                result = oAuthService.getAccessTokenByClientCredentials(null);
            }
            return result;
        } catch (OAuthException | IOException | OAuthResponseException e) {
            throw new AutomowerCommunicationException("Unable to authenticate", e);
        }
    }
    /*
     * @WebSocket
     * public class AutomowerWebSocketAdapter {
     * private @Nullable ScheduledFuture<?> connectionTracker;
     * 
     * @OnWebSocketConnect
     * public synchronized void onConnect(Session session) {
     * closing = false;
     * unansweredPings = 0;
     * 
     * logger.debug("Connected to Husqvarna Webservice ({})", session.getRemoteAddress().getHostString());
     * // Subscribe to all messages after connecting
     * try {
     * String subscribeAllMessage = "{\"type\":\"subscribe\",\"topics\":[\"*\"]}";
     * session.getRemote().sendString(subscribeAllMessage);
     * logger.debug("Sent subscription message to subscribe to all topics");
     * } catch (Exception e) {
     * logger.error("Failed to send subscription message: {}", e.getMessage());
     * }
     * 
     * ScheduledFuture<?> connectionTracker = this.connectionTracker;
     * if (connectionTracker != null && !connectionTracker.isCancelled()) {
     * connectionTracker.cancel(true);
     * }
     * // start sending PING every minute
     * connectionTracker = scheduler.scheduleWithFixedDelay(this::sendKeepAlivePing, 1, 1, TimeUnit.MINUTES);
     * }
     * 
     * @OnWebSocketFrame
     * public synchronized void onFrame(Frame pong) {
     * if (pong instanceof PongFrame) {
     * unansweredPings = 0;
     * // logger.trace("Pong received");
     * }
     * }
     * 
     * @OnWebSocketMessage
     * public void onMessage(String message) {
     * logger.debug("Message from Server: {}", message);
     * }
     * 
     * @OnWebSocketClose
     * public void onClose(int statusCode, String reason) {
     * logger.info("WebSocket closed: {} - {}", statusCode, reason);
     * 
     * // Cancel ping task on disconnect
     * final ScheduledFuture<?> connectionTracker = this.connectionTracker;
     * if (connectionTracker != null) {
     * logger.trace("Cancelling ping task");
     * connectionTracker.cancel(true);
     * }
     * 
     * if (!closing) {
     * try {
     * restart();
     * } catch (Exception e) {
     * logger.error("Failed to restart WebSocket client: {}", e.getMessage());
     * }
     * }
     * }
     * 
     * public void restart() throws Exception {
     * String accessToken = authenticate().getAccessToken();
     * if (accessToken == null) {
     * logger.error("No OAuth2 access token available for WebSocket connection");
     * return;
     * }
     * logger.debug("Reconnecting to Husqvarna Webservice ()");
     * String wsUrl = "wss://ws.openapi.husqvarna.dev/v1";
     * org.eclipse.jetty.websocket.client.ClientUpgradeRequest request = new
     * org.eclipse.jetty.websocket.client.ClientUpgradeRequest();
     * request.setHeader("Authorization", "Bearer " + accessToken);
     * webSocketSession = (WebSocketSession) webSocketClient.connect(this, URI.create(wsUrl), request).get();
     * }
     * 
     * @OnWebSocketError
     * public void onError(Throwable cause) {
     * logger.error("WebSocket error: {}", cause.getMessage());
     * }
     * 
     * /**
     * Sends a ping to tell the Husqvarna smart system that the client is alive.
     *
     * private synchronized void sendKeepAlivePing() {
     * try {
     * String accessToken = authenticate().getAccessToken();
     * if (unansweredPings > MAX_UNANSWERED_PINGS || accessToken == null) {
     * webSocketSession.close(1000, "Timeout manually closing dead connection");
     * } else {
     * if (webSocketSession.isOpen()) {
     * try {
     * // logger.trace("Sending ping ...");
     * webSocketSession.getRemote().sendPing(pingPayload);
     * ++unansweredPings;
     * } catch (IOException ex) {
     * logger.debug("Error while sending ping: {}", ex.getMessage());
     * }
     * }
     * }
     * } catch (AutomowerCommunicationException e) {
     * logger.error("Failed to authenticate while sending keep-alive ping: {}", e.getMessage());
     * }
     * }
     * }
     */
}
