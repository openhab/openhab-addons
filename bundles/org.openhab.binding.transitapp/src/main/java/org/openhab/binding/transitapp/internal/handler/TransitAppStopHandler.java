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
package org.openhab.binding.transitapp.internal.handler;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.transitapp.internal.net.TransitApiClient;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NonNullByDefault
public class TransitAppStopHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(TransitAppStopHandler.class);
    private final TransitApiClient apiClient = new TransitApiClient();
    private @Nullable ScheduledFuture<?> refreshJob;

    public TransitAppStopHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        Number refreshIntervalNum = (Number) getThing().getConfiguration().get("refreshInterval");
        long refreshInterval = refreshIntervalNum != null ? refreshIntervalNum.longValue() : 60L;
        refreshJob = scheduler.scheduleWithFixedDelay(this::pollTransitApi, 1, refreshInterval, TimeUnit.SECONDS);
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            pollTransitApi();
        }
    }

    private void pollTransitApi() {
        String globalStopId = (String) getThing().getConfiguration().get("globalStopId");
        Bridge bridge = getBridge();

        if (globalStopId == null || globalStopId.isEmpty() || bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Stop ID or Bridge missing");
            return;
        }

        String apiKey = (String) bridge.getConfiguration().get("apiKey");
        try {
            String response = apiClient.fetchStopDepartures(apiKey, globalStopId);
            logger.debug("Polling transit API for stop ID: {}", globalStopId);
            logger.trace("Received raw JSON response for stop {}: {}", globalStopId, response);
            updateStatus(ThingStatus.ONLINE);

            // Trigger Event Channel on success as an example
            triggerChannel(new ChannelUID(getThing().getUID(), "service-alarm"), "UPDATE - API polled successfully");

            // Dynamic channels would be updated here based on 'response'
            updateState("depart1#routeLongName", new StringType("Dynamic Live Data"));
            updateState("depart1#minutesUntilDeparture", new QuantityType<>(5, Units.MINUTE));
        } catch (Exception e) {
            logger.warn("Warning/Communication issue while polling stop {}: {}", globalStopId, e.getMessage());
            logger.error("Detailed API error for stop {}: {}", globalStopId, e.getMessage(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    public String findNextDepartureByLine(String lineName) {
        // Mocked for Binding Action
        return "12:45";
    }

    @Override
    public void dispose() {
        ScheduledFuture<?> job = refreshJob;
        if (job != null) {
            job.cancel(true);
        }
        super.dispose();
    }
}
