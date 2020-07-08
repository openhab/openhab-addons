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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.openhab.binding.deconz.internal.dto.DeconzBaseMessage;
import org.openhab.binding.deconz.internal.netutils.AsyncHttpClient;
import org.openhab.binding.deconz.internal.netutils.WebSocketConnection;
import org.openhab.binding.deconz.internal.netutils.WebSocketMessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * This base thing doesn't establish any connections, that is done by the bridge Thing.
 *
 * It waits for the bridge to come online, grab the websocket connection and bridge configuration
 * and registers to the websocket connection as a listener.
 *
 * A REST API call is made to get the initial light/rollershutter state.
 *
 * @author David Graeff - Initial contribution
 * @author Jan N. Klug - Refactored to abstract class
 */
@NonNullByDefault
public abstract class DeconzBaseThingHandler<T extends DeconzBaseMessage> extends BaseThingHandler
        implements WebSocketMessageListener {
    private final Logger logger = LoggerFactory.getLogger(DeconzBaseThingHandler.class);
    protected ThingConfig config = new ThingConfig();
    protected DeconzBridgeConfig bridgeConfig = new DeconzBridgeConfig();
    protected final Gson gson;
    @Nullable
    protected ScheduledFuture<?> scheduledFuture;
    protected @Nullable WebSocketConnection connection;
    protected @Nullable AsyncHttpClient http;

    public DeconzBaseThingHandler(Thing thing, Gson gson) {
        super(thing);
        this.gson = gson;
    }

    /**
     * Stops the API request
     */
    private void stopTimer() {
        ScheduledFuture<?> future = scheduledFuture;
        if (future != null) {
            future.cancel(true);
            scheduledFuture = null;
        }
    }

    protected abstract void registerListener();

    protected abstract void unregisterListener();

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        if (config.id.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "ID not set");
            return;
        }

        if (bridgeStatusInfo.getStatus() == ThingStatus.OFFLINE) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            unregisterListener();
            return;
        }

        if (bridgeStatusInfo.getStatus() != ThingStatus.ONLINE) {
            return;
        }

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
        requestState();
    }

    protected abstract @Nullable T parseStateResponse(AsyncHttpClient.Result r);

    /**
     * processes a newly received state response
     *
     * MUST set the thing status!
     *
     * @param stateResponse
     */
    protected abstract void processStateResponse(@Nullable T stateResponse);

    /**
     * call requestState(type) in this method only
     */
    protected abstract void requestState();

    /**
     * Perform a request to the REST API for retrieving the full light state with all data and configuration.
     */
    protected void requestState(String type) {
        AsyncHttpClient asyncHttpClient = http;
        if (asyncHttpClient == null) {
            return;
        }

        String url = buildUrl(bridgeConfig.host, bridgeConfig.httpPort, bridgeConfig.apikey, type, config.id);
        logger.trace("Requesting URL for initial data: {}", url);

        // Get initial data
        asyncHttpClient.get(url, bridgeConfig.timeout).thenApply(this::parseStateResponse).exceptionally(e -> {
            if (e instanceof SocketTimeoutException || e instanceof TimeoutException
                    || e instanceof CompletionException) {
                logger.debug("Get new state failed: ", e);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }

            stopTimer();
            scheduledFuture = scheduler.schedule((Runnable) this::requestState, 10, TimeUnit.SECONDS);

            return null;
        }).thenAccept(this::processStateResponse);
    }

    @Override
    public void dispose() {
        stopTimer();
        WebSocketConnection webSocketConnection = connection;
        if (webSocketConnection != null) {
            webSocketConnection.unregisterLightListener(config.id);
        }
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
