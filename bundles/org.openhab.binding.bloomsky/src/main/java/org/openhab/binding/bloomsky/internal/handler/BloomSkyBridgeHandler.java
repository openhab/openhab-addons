/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.bloomsky.internal.handler;

import static org.openhab.binding.bloomsky.internal.BloomSkyBindingConstants.THING_TYPE_BRIDGE;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.bloomsky.internal.bridge.BloomSkyBridge;
import org.openhab.binding.bloomsky.internal.config.BloomSkyBridgeConfiguration;
import org.openhab.binding.bloomsky.internal.connection.BloomSkyCommunicationException;
import org.openhab.binding.bloomsky.internal.dto.BloomSkyJsonSensorData;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BloomSkyBridgeHandler} is responsible for validating the Bridge configuration
 * and ensuring the service is online and available to the Sky an Storm devices
 * that are dependent on the Bridge to refresh their channel state information.
 *
 * @author Dave J Schoepel - Initial contribution
 *
 */
@NonNullByDefault
public class BloomSkyBridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(BloomSkyBridgeHandler.class);

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_BRIDGE);

    private static final long INITIAL_DELAY_IN_SECONDS = 0;

    private static final long DEFAULT_POLLING_INTERVAL_S = TimeUnit.MINUTES.toSeconds(5);

    private @Nullable ScheduledFuture<?> bloomSkyBridgePollingJob;
    private @Nullable BloomSkyBridge bridge;

    private final HttpClient httpClient;
    // keeps track of the parsed configuration
    private @NonNullByDefault({}) BloomSkyBridgeConfiguration config;

    /**
     * Bridge Handler Constructor
     *
     * @param bridge
     * @Param httpClient
     */
    public BloomSkyBridgeHandler(Bridge bridge, HttpClient httpClient) {
        super(bridge);
        this.httpClient = httpClient;
    }

    private void pollSkyDevices(BloomSkyBridge bridge) {
        BloomSkyJsonSensorData[] bloomSkyDevices;
        try {
            bloomSkyDevices = bridge.getBloomSkyDevices();
            updateStatus(ThingStatus.ONLINE);
            logger.debug("Found {} BloomSky devices.", bloomSkyDevices.length);
        } catch (BloomSkyCommunicationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/comm-error-query-sky-devices-failed");
            logger.warn("Unable to fetch BloomSky devices: {}", e.getMessage());
        }
    }

    @Override
    public void initialize() {
        logger.debug("Initialize BloomSky Bridge handler '{}'.", getThing().getUID());

        BloomSkyBridgeConfiguration bridgeConfiguration = getConfigAs(BloomSkyBridgeConfiguration.class);

        final String apiKey = bridgeConfiguration.getApikey();
        final String displayUnits = bridgeConfiguration.getUnits();
        final Integer refreshInterval = bridgeConfiguration.getRefreshInterval();

        if (apiKey == null || apiKey.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/configuration-error-no-api-key");
        } else if (displayUnits == null || displayUnits.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/configuration-error-no-measurement-display-units");
        } else if (refreshInterval < 5) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/configuration-error-invalid-refresh-interval-less-than-5");
        } else {
            if (bridge == null) {
                BloomSkyBridge currentBridge = new BloomSkyBridge(apiKey, displayUnits, httpClient, scheduler);
                bridge = currentBridge;
                startBloomSkyBridgePolling(currentBridge, refreshInterval);
            }
        }
    }

    private void startBloomSkyBridgePolling(BloomSkyBridge bridge, @Nullable Integer pollingIntervalS) {
        ScheduledFuture<?> currentPollingJob = bloomSkyBridgePollingJob;
        if (currentPollingJob == null) {
            final long pollingIntervalToUse = pollingIntervalS == null ? DEFAULT_POLLING_INTERVAL_S
                    : TimeUnit.MINUTES.toSeconds(pollingIntervalS);
            logger.debug("polling interval to use: {}", pollingIntervalToUse);
            bloomSkyBridgePollingJob = scheduler.scheduleWithFixedDelay(() -> pollSkyDevices(bridge), 1,
                    pollingIntervalToUse, TimeUnit.SECONDS);
        }
    }

    private void stopBloomSkyBridgePolling(BloomSkyBridge bridge) {
        ScheduledFuture<?> currentPollingJob = bloomSkyBridgePollingJob;
        if (currentPollingJob != null) {
            currentPollingJob.cancel(true);
            bloomSkyBridgePollingJob = null;
        }
    }

    @Override
    public void dispose() {
        BloomSkyBridge currentBridge = bridge;
        if (currentBridge != null) {
            stopBloomSkyBridgePolling(currentBridge);
            bridge = null;
            logger.debug("BloomSkyBridgeHandler: Stopping bridge polling job.");
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // The BloomSky Bridge does not handle any commands
    }

    public @Nullable BloomSkyBridge getBloomSkyBridge() {
        return bridge;
    }

    public Optional<BloomSkyJsonSensorData[]> getBloomSkyDevices() {
        BloomSkyBridge currentBridge = bridge;
        if (currentBridge == null) {
            return Optional.empty();
        }

        try {
            return Optional.of(currentBridge.getBloomSkyDevices());
        } catch (BloomSkyCommunicationException e) {
            logger.debug("Bridge cannot get list of available BloomSKy devices {}", e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public void childHandlerInitialized(ThingHandler childHandler, Thing childThing) {
        logger.debug("BloomSkyBridgeHandler: starting childThingInitialized...");
        logger.debug("chlidHandlerInitialized thing handler ThingTypeUID= {}, ThingUID = {}",
                childThing.getThingTypeUID(), childThing.getUID());
        switch (childThing.getThingTypeUID().toString()) {
            case "bloomsky:storm":
                scheduler.schedule(() -> {
                    updateStormThing((BloomSkyStormHandler) childHandler, childThing);
                    determineBridgeStatus();
                }, INITIAL_DELAY_IN_SECONDS, TimeUnit.SECONDS);
                break;
            case "bloomsky:sky":
                scheduler.schedule(() -> {
                    updateSkyThing((BloomSkySKYHandler) childHandler, childThing);
                    determineBridgeStatus();
                }, INITIAL_DELAY_IN_SECONDS, TimeUnit.SECONDS);
                break;
        }
        ; // end case for updating child handlers
    } // end childHandlerInitialized method

    @Override
    public void childHandlerDisposed(ThingHandler childHandler, Thing childThing) {
        logger.debug("BloomSkyBridgeHandler: childHandlerDisposed about to run determineBridgeStatus");
        determineBridgeStatus();
    }

    public BloomSkyBridgeConfiguration getBloomSkyBridgeConfig() {
        return config;
    }

    private void determineBridgeStatus() {
        logger.debug("DetermineBridgeStatus: thing = {}, status = {}", thing.getLabel(), thing.getStatus());
        ThingStatus status = thing.getStatus();
        for (Thing thing : getThing().getThings()) {
            logger.debug("DetermineBridgeStatus: thing = {}, status = {}", thing.getLabel(), thing.getStatus());
            if (ThingStatus.ONLINE.equals(thing.getStatus())) {
                status = ThingStatus.ONLINE;
                break;
            }
        }
        updateStatus(status);
    }

    private ThingStatus updateSkyThing(@Nullable BloomSkySKYHandler handler, Thing thing) {
        logger.debug("UpdateThing from ChildHandler of thing '{}' as Sky is null.", thing.getUID());

        if (handler != null) {
            handler.refreshSkyObservations();
        } else {
            logger.debug("Cannot update sensor data of thing '{}' as Sky is null.", thing.getUID());
        } // end if handler is valid (not null)
        return thing.getStatus();
    } // end updateThing

    private ThingStatus updateStormThing(@Nullable BloomSkyStormHandler handler, Thing thing) {
        logger.debug("UpdateThing from ChildHandler of thing '{}' as Sky is null.", thing.getUID());

        if (handler != null) {
            handler.refreshStormObservations();
        } else {
            logger.debug("Cannot update sensor data of thing '{}' as Sky is null.", thing.getUID());
        } // end if handler is valid (not null)
        return thing.getStatus();
    } // end updateThing

    public @Nullable String getApiKey() {
        return getConfigAs(BloomSkyBridgeConfiguration.class).apikey;
    }

    public @Nullable String getDisplayUnits() {
        return getConfigAs(BloomSkyBridgeConfiguration.class).units;
    }
}
