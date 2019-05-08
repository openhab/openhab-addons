/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.deconz.internal.handler;

import static org.openhab.binding.deconz.internal.BindingConstants.*;

import java.net.SocketTimeoutException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerService;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.io.net.http.WebSocketFactory;
import org.openhab.binding.deconz.internal.discovery.ThingDiscoveryService;
import org.openhab.binding.deconz.internal.dto.ApiKeyMessage;
import org.openhab.binding.deconz.internal.dto.BridgeFullState;
import org.openhab.binding.deconz.internal.netutils.AsyncHttpClient;
import org.openhab.binding.deconz.internal.netutils.WebSocketConnection;
import org.openhab.binding.deconz.internal.netutils.WebSocketConnectionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The bridge Thing is responsible for requesting all available sensors and switches and propagate
 * them to the discovery service.
 *
 * It performs the authorization process if necessary.
 *
 * A websocket connection is established to the deCONZ software and kept alive.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class DeconzBridgeHandler extends BaseBridgeHandler implements WebSocketConnectionListener {
    private final Logger logger = LoggerFactory.getLogger(DeconzBridgeHandler.class);
    private @Nullable ThingDiscoveryService thingDiscoveryService;
    private final WebSocketConnection websocket;
    private final AsyncHttpClient http;
    private DeconzBridgeConfig config = new DeconzBridgeConfig();
    private final Gson gson = new Gson();
    private @Nullable ScheduledFuture<?> scheduledFuture;
    private int websocketport = 0;
    /** Prevent a dispose/init cycle while this flag is set. Use for property updates */
    private boolean ignoreConfigurationUpdate;

    /** The poll frequency for the API Key verification */
    private static final int POLL_FREQUENCY_SEC = 10;

    public DeconzBridgeHandler(Bridge thing, WebSocketFactory webSocketFactory, AsyncHttpClient http) {
        super(thing);
        this.http = http;
        String websocketID = thing.getUID().getAsString().replace(':', '-');
        websocketID = websocketID.length() < 3 ? websocketID : websocketID.substring(websocketID.length() - 20);
        this.websocket = new WebSocketConnection(this, webSocketFactory.createWebSocketClient(websocketID));
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(ThingDiscoveryService.class);
    }

    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters) {
        if (!ignoreConfigurationUpdate) {
            super.handleConfigurationUpdate(configurationParameters);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    /**
     * Stops the API request or websocket reconnect timer
     */
    private void stopTimer() {
        ScheduledFuture<?> future = scheduledFuture;
        if (future != null) {
            future.cancel(false);
            scheduledFuture = null;
        }
    }

    /**
     * Parses the response message to the API key generation REST API.
     *
     * @param r The response
     */
    private void parseAPIKeyResponse(AsyncHttpClient.Result r) {
        if (r.getResponseCode() == 403) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING,
                    "Allow authentification for 3rd party apps. Trying again in " + String.valueOf(POLL_FREQUENCY_SEC)
                            + " seconds");
            stopTimer();
            scheduledFuture = scheduler.schedule(() -> requestApiKey(), POLL_FREQUENCY_SEC, TimeUnit.SECONDS);
        } else if (r.getResponseCode() == 200) {
            ApiKeyMessage[] response = gson.fromJson(r.getBody(), ApiKeyMessage[].class);
            if (response.length == 0) {
                throw new IllegalStateException("Authorisation request response is empty");
            }
            config.apikey = response[0].success.username;
            Configuration configuration = editConfiguration();
            configuration.put(CONFIG_APIKEY, config.apikey);
            updateConfiguration(configuration);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING, "Waiting for configuration");
            requestFullState();
        } else {
            throw new IllegalStateException("Unknown status code for authorisation request");
        }
    }

    /**
     * Parses the response message to the REST API for retrieving the full bridge state with all sensors and switches
     * and configuration.
     *
     * @param r The response
     */
    private @Nullable BridgeFullState parseBridgeFullStateResponse(AsyncHttpClient.Result r) {
        if (r.getResponseCode() == 403) {
            return null;
        } else if (r.getResponseCode() == 200) {
            return gson.fromJson(r.getBody(), BridgeFullState.class);
        } else {
            throw new IllegalStateException("Unknown status code for full state request");
        }
    }

    /**
     * Perform a request to the REST API for retrieving the full bridge state with all sensors and switches
     * and configuration.
     */
    public void requestFullState() {
        if (config.apikey == null) {
            return;
        }
        String url = url(config.host, config.apikey, null, null);

        http.get(url, config.timeout).thenApply(this::parseBridgeFullStateResponse).exceptionally(e -> {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            if (!(e instanceof SocketTimeoutException)) {
                logger.warn("Get full state failed", e);
            }
            return null;
        }).whenComplete((value, error) -> {
            if (thingDiscoveryService != null) {
                // Hand over sensors to discovery service
                thingDiscoveryService.stateRequestFinished(value != null ? value.sensors : null);
            }
        }).thenAccept(fullState -> {
            // Auth failed
            if (fullState == null) {
                requestApiKey();
                return;
            }
            if (fullState.config.name.isEmpty()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE,
                        "You are connected to a HUE bridge, not a deCONZ software!");
                return;
            }
            if (fullState.config.websocketport == 0) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE,
                        "deCONZ software too old. No websocket support!");
                return;
            }

            // Add some information about the bridge
            Map<String, String> editProperties = editProperties();
            editProperties.put("apiversion", fullState.config.apiversion);
            editProperties.put("swversion", fullState.config.swversion);
            editProperties.put("fwversion", fullState.config.fwversion);
            editProperties.put("uuid", fullState.config.uuid);
            editProperties.put("zigbeechannel", String.valueOf(fullState.config.zigbeechannel));
            editProperties.put("ipaddress", fullState.config.ipaddress);
            ignoreConfigurationUpdate = true;
            updateProperties(editProperties);
            ignoreConfigurationUpdate = false;

            websocketport = fullState.config.websocketport;
            startWebsocket();
        }).exceptionally(e -> {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, e.getMessage());
            logger.warn("Full state parsing failed", e);
            return null;
        });
    }

    /**
     * Starts the websocket connection.
     * {@link #requestFullState} need to be called first to obtain the websocket port.
     */
    private void startWebsocket() {
        if (websocket.isConnected() || websocketport == 0) {
            return;
        }

        String host = config.host;
        if (host.indexOf(':') > 0) {
            host = host.substring(0, host.indexOf(':'));
        }
        stopTimer();
        scheduledFuture = scheduler.scheduleWithFixedDelay(this::startWebsocket, POLL_FREQUENCY_SEC, POLL_FREQUENCY_SEC,
                TimeUnit.SECONDS);

        websocket.start(host + ":" + String.valueOf(websocketport));
    }

    /**
     * Perform a request to the REST API for generating an API key.
     *
     * @param r The response
     */
    private CompletableFuture<?> requestApiKey() {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING, "Requesting API Key");
        stopTimer();
        String url = url(config.host, null, null, null);
        return http.post(url, "{\"devicetype\":\"openHAB\"}", config.timeout).thenAccept(this::parseAPIKeyResponse)
                .exceptionally(e -> {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                    logger.warn("Authorisation failed", e);
                    return null;
                });
    }

    @Override
    public void initialize() {
        logger.debug("Start initializing!");
        config = getConfigAs(DeconzBridgeConfig.class);
        if (config.apikey == null) {
            requestApiKey();
        } else {
            requestFullState();
        }
    }

    @Override
    public void dispose() {
        stopTimer();
        websocket.close();
    }

    @Override
    public void connectionError(@Nullable Throwable e) {
        if (e != null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Unknown reason");
        }
        stopTimer();
        // Wait for POLL_FREQUENCY_SEC after a connection error before trying again
        scheduledFuture = scheduler.scheduleWithFixedDelay(this::startWebsocket, POLL_FREQUENCY_SEC, POLL_FREQUENCY_SEC,
                TimeUnit.SECONDS);
    }

    @Override
    public void connectionEstablished() {
        stopTimer();
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void connectionLost(String reason) {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, reason);
        startWebsocket();
    }

    /**
     * Return the websocket connection.
     */
    public WebSocketConnection getWebsocketConnection() {
        return websocket;
    }

    /**
     * Return the http connection.
     */
    public AsyncHttpClient getHttp() {
        return http;
    }

    /**
     * Return the bridge configuration.
     */
    public DeconzBridgeConfig getBridgeConfig() {
        return config;
    }

    /**
     * Called by the {@link ThingDiscoveryService}. Informs the bridge handler about the service.
     *
     * @param thingDiscoveryService The service
     */
    public void setDiscoveryService(ThingDiscoveryService thingDiscoveryService) {
        this.thingDiscoveryService = thingDiscoveryService;
    }
}
