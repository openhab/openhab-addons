/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import static org.openhab.binding.deconz.internal.Util.buildUrl;

import java.net.SocketTimeoutException;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.deconz.internal.dto.DeconzBaseMessage;
import org.openhab.binding.deconz.internal.netutils.AsyncHttpClient;
import org.openhab.binding.deconz.internal.netutils.WebSocketConnection;
import org.openhab.binding.deconz.internal.netutils.WebSocketMessageListener;
import org.openhab.binding.deconz.internal.types.ResourceType;
import org.openhab.core.thing.*;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * This base thing doesn't establish any connections, that is done by the bridge Thing.
 *
 * It waits for the bridge to come online, grab the websocket connection and bridge configuration
 * and registers to the websocket connection as a listener.
 **
 * @author David Graeff - Initial contribution
 * @author Jan N. Klug - Refactored to abstract class
 */
@NonNullByDefault
public abstract class DeconzBaseThingHandler<T extends DeconzBaseMessage> extends BaseThingHandler
        implements WebSocketMessageListener {
    private final Logger logger = LoggerFactory.getLogger(DeconzBaseThingHandler.class);
    protected final ResourceType resourceType;
    protected ThingConfig config = new ThingConfig();
    protected DeconzBridgeConfig bridgeConfig = new DeconzBridgeConfig();
    protected final Gson gson;
    private @Nullable ScheduledFuture<?> initializationJob;
    protected @Nullable WebSocketConnection connection;
    protected @Nullable AsyncHttpClient http;

    public DeconzBaseThingHandler(Thing thing, Gson gson, ResourceType resourceType) {
        super(thing);
        this.gson = gson;
        this.resourceType = resourceType;
    }

    /**
     * Stops the API request
     */
    private void stopInitializationJob() {
        ScheduledFuture<?> future = initializationJob;
        if (future != null) {
            future.cancel(true);
            initializationJob = null;
        }
    }

    private void registerListener() {
        WebSocketConnection conn = connection;
        if (conn != null) {
            conn.registerListener(resourceType, config.id, this);
        }
    }

    private void unregisterListener() {
        WebSocketConnection conn = connection;
        if (conn != null) {
            conn.unregisterListener(resourceType, config.id);
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        if (config.id.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "ID not set");
            return;
        }

        if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
            // the bridge is ONLINE, we can communicate with the gateway, so we update the connection parameters and
            // register the listener
            Bridge bridge = getBridge();
            if (bridge == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
                return;
            }
            DeconzBridgeHandler bridgeHandler = (DeconzBridgeHandler) bridge.getHandler();
            if (bridgeHandler == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
                return;
            }

            final WebSocketConnection webSocketConnection = bridgeHandler.getWebsocketConnection();
            this.connection = webSocketConnection;
            this.http = bridgeHandler.getHttp();
            this.bridgeConfig = bridgeHandler.getBridgeConfig();

            updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE);

            // Real-time data
            registerListener();

            // get initial values
            requestState(this::processStateResponse);
        } else {
            // if the bridge is not ONLINE, we assume communication is not possible, so we unregister the listener and
            // set the thing status to OFFLINE
            unregisterListener();
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
    }

    protected abstract @Nullable T parseStateResponse(AsyncHttpClient.Result r);

    /**
     * processes a newly received (initial) state response
     *
     * MUST set the thing status!
     *
     * @param stateResponse
     */
    protected abstract void processStateResponse(@Nullable T stateResponse);

    /**
     * Perform a request to the REST API for retrieving the full light state with all data and configuration.
     */
    protected void requestState(Consumer<@Nullable T> processor) {
        AsyncHttpClient asyncHttpClient = http;
        if (asyncHttpClient == null) {
            return;
        }

        String url = buildUrl(bridgeConfig.host, bridgeConfig.httpPort, bridgeConfig.apikey,
                resourceType.getIdentifier(), config.id);
        logger.trace("Requesting URL for initial data: {}", url);

        // Get initial data
        asyncHttpClient.get(url, bridgeConfig.timeout).thenApply(this::parseStateResponse).exceptionally(e -> {
            if (e instanceof SocketTimeoutException || e instanceof TimeoutException
                    || e instanceof CompletionException) {
                logger.debug("Get new state failed: ", e);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }

            stopInitializationJob();
            initializationJob = scheduler.schedule(() -> requestState(this::processStateResponse), 10,
                    TimeUnit.SECONDS);

            return null;
        }).thenAccept(processor);
    }

    /**
     * sends a command to the bridge
     *
     * @param object must be serializable and contain the command
     * @param originalCommand the original openHAB command (used for logging purposes)
     * @param channelUID the channel that this command was send to (used for logging purposes)
     * @param acceptProcessing additional processing after the command was successfully send (might be null)
     */
    protected void sendCommand(Object object, Command originalCommand, ChannelUID channelUID,
            @Nullable Runnable acceptProcessing) {
        AsyncHttpClient asyncHttpClient = http;
        if (asyncHttpClient == null) {
            return;
        }
        String url = buildUrl(bridgeConfig.host, bridgeConfig.httpPort, bridgeConfig.apikey,
                resourceType.getIdentifier(), config.id, resourceType.getCommandUrl());

        String json = gson.toJson(object);
        logger.trace("Sending {} to {} {} via {}", json, resourceType, config.id, url);

        asyncHttpClient.put(url, json, bridgeConfig.timeout).thenAccept(v -> {
            if (acceptProcessing != null) {
                acceptProcessing.run();
            }
            if (v.getResponseCode() != java.net.HttpURLConnection.HTTP_OK) {
                logger.warn("Sending command {} to channel {} failed: {} - {}", originalCommand, channelUID,
                        v.getResponseCode(), v.getBody());
            } else {
                logger.trace("Result code={}, body={}", v.getResponseCode(), v.getBody());
            }
        }).exceptionally(e -> {
            logger.warn("Sending command {} to channel {} failed: {} - {}", originalCommand, channelUID, e.getClass(),
                    e.getMessage());
            return null;
        });
    }

    @Override
    public void dispose() {
        stopInitializationJob();
        unregisterListener();
        super.dispose();
    }

    @Override
    public void initialize() {
        config = getConfigAs(ThingConfig.class);

        Bridge bridge = getBridge();
        if (bridge != null) {
            bridgeStatusChanged(bridge.getStatusInfo());
        }
    }
}
