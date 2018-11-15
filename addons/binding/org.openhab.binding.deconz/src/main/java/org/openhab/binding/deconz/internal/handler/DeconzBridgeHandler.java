/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.deconz.internal.handler;

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
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.deconz.internal.BindingConstants;
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
 * It also performs the authorization process if necessary.
 *
 * It also establishes a websocket connection to the deCONZ software.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class DeconzBridgeHandler extends BaseBridgeHandler implements WebSocketConnectionListener {
    private final Logger logger = LoggerFactory.getLogger(DeconzBridgeHandler.class);
    private final ThingDiscoveryService thingDiscoveryService;
    private final WebSocketConnection websocket = new WebSocketConnection(this);
    private DeconzBridgeConfig config = new DeconzBridgeConfig();
    private @Nullable ScheduledFuture<?> scheduledFuture;
    private static final int POLL_FREQUENCY_SEC = 10;

    public DeconzBridgeHandler(ThingDiscoveryService thingDiscoveryService, Bridge thing) {
        super(thing);
        this.thingDiscoveryService = thingDiscoveryService;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    /**
     * Stops the API request timer
     */
    private void stopGetAPIKeyTimer() {
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
            scheduledFuture = scheduler.schedule(() -> requestApiKey(), POLL_FREQUENCY_SEC, TimeUnit.SECONDS);
        } else if (r.getResponseCode() == 200) {
            ApiKeyMessage[] response = new Gson().fromJson(r.getBody(), ApiKeyMessage[].class);
            if (response.length == 0) {
                throw new IllegalStateException("Authorisation request response is empty");
            }
            config.apikey = response[0].success.username;
            Configuration configuration = editConfiguration();
            configuration.put(BindingConstants.CONFIG_APIKEY, config.apikey);
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
            return new Gson().fromJson(r.getBody(), BridgeFullState.class);
        } else {
            throw new IllegalStateException("Unknown status code for full state request");
        }
    }

    /**
     * Perform a request to the REST API for retrieving the full bridge state with all sensors and switches
     * and configuration.
     */
    private void requestFullState() {
        String url = BindingConstants.url(config.host, config.apikey, null, null);

        AsyncHttpClient.getAsync(url, scheduler).thenApply(this::parseBridgeFullStateResponse).exceptionally(e -> {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            logger.warn("Get full state failed", e);
            return null;
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
            // Hand sensors to discovery
            fullState.sensors
                    .forEach((sensorID, sensor) -> thingDiscoveryService.addDevice(sensor, sensorID, thing.getUID()));

            // Add some information about the bridge
            Map<String, String> editProperties = editProperties();
            editProperties.put("apiversion", fullState.config.apiversion);
            editProperties.put("swversion", fullState.config.swversion);
            editProperties.put("fwversion", fullState.config.fwversion);
            editProperties.put("uuid", fullState.config.uuid);
            editProperties.put("zigbeechannel", String.valueOf(fullState.config.zigbeechannel));
            editProperties.put("ipaddress", fullState.config.ipaddress);
            updateProperties(editProperties);

            String host = config.host;
            if (host.indexOf(':') > 0) {
                host = host.substring(0, host.indexOf(':'));
            }

            websocket.start(host + ":" + String.valueOf(fullState.config.websocketport));
        }).exceptionally(e -> {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, e.getMessage());
            logger.warn("Full state parsing failed", e);
            return null;
        });
    }

    /**
     * Perform a request to the REST API for generating an API key.
     *
     * @param r The response
     */
    private CompletableFuture<?> requestApiKey() {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING, "Requesting API Key");
        stopGetAPIKeyTimer();
        String url = BindingConstants.url(config.host, null, null, null);
        return AsyncHttpClient.postAsync(url, "{\"devicetype\":\"openHAB\"}", scheduler)
                .thenAccept(this::parseAPIKeyResponse).exceptionally(e -> {
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
        stopGetAPIKeyTimer();
        websocket.close();
    }

    @Override
    public void connectionError(@Nullable Throwable e) {
        if (e != null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Unknown reason");
        }
    }

    @Override
    public void connectionEstablished() {
        updateStatus(ThingStatus.ONLINE);
    }

    /**
     * Return the websocket connection.
     */
    public WebSocketConnection getWebsocketConnection() {
        return websocket;
    }

    /**
     * Return the bridge configuration.
     */
    public DeconzBridgeConfig getBridgeConfig() {
        return config;
    }
}
