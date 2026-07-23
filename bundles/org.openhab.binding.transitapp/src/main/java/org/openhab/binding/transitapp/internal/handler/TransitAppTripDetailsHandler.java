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

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.StringType;
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

/**
 * The {@link TransitAppTripDetailsHandler} is responsible for polling trip details
 * from the Transit API at configured intervals.
 *
 * @author Initial contribution - Initial contribution
 */
@NonNullByDefault
public class TransitAppTripDetailsHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(TransitAppTripDetailsHandler.class);
    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

    private @Nullable ScheduledFuture<?> refreshJob;

    public TransitAppTripDetailsHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing TransitAppTripDetailsHandler for thing: {}", getThing().getUID());

        // Read refresh interval configuration with fallback to 60 seconds
        Number refreshIntervalNum = (Number) getThing().getConfiguration().get("refreshInterval");
        long refreshInterval = refreshIntervalNum != null ? refreshIntervalNum.longValue() : 60L;

        logger.info("Scheduling trip details refresh job for {} with interval: {} seconds", getThing().getUID(),
                refreshInterval);

        // Schedule periodic background polling job
        refreshJob = scheduler.scheduleWithFixedDelay(this::pollTransitApi, 1, refreshInterval, TimeUnit.SECONDS);
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Received command {} for channel {}", command, channelUID);
        if (command instanceof RefreshType) {
            pollTransitApi();
        } else {
            logger.warn("Unsupported command received: {} for channel {}", command, channelUID);
        }
    }

    private void pollTransitApi() {
        logger.trace("TRACE: Starting background poll for trip details thing {}", getThing().getUID());

        String tripId = (String) getThing().getConfiguration().get("tripId");
        if (tripId == null || tripId.isEmpty()) {
            logger.error("ERROR: tripId is not configured for thing {}", getThing().getUID());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Trip ID is missing");
            return;
        }

        Bridge bridge = getBridge();
        if (bridge == null) {
            logger.error("ERROR: Trip details thing {} is not attached to a Bridge!", getThing().getUID());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "Bridge not found");
            return;
        }

        String apiKey = (String) bridge.getConfiguration().get("apiKey");
        if (apiKey == null || apiKey.isEmpty()) {
            logger.error("ERROR: API Key is missing on the TransitApp Bridge!");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "API Key missing on bridge");
            return;
        }

        try {
            String urlStr = "https://external.transitapp.com/v4/public/trip_details?trip_search_key=" + tripId;
            logger.debug("DEBUG: Building HTTP request for Trip Details URL: {}", urlStr);

            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(urlStr)).header("apiKey", apiKey).GET()
                    .build();

            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenAccept(response -> {
                int statusCode = response.statusCode();
                String jsonBody = response.body();

                if (statusCode == 200) {
                    logger.info("INFO: Successfully polled trip details for trip ID {}", tripId);
                    logger.debug("DEBUG: Full JSON response for trip {}:\n{}", tripId, jsonBody);
                    updateStatus(ThingStatus.ONLINE);

                    updateState("tripHeadsign", new StringType("Live Trip Data"));
                } else {
                    logger.warn("WARN: Transit API returned status code {} for trip {}. Response body: {}", statusCode,
                            tripId, jsonBody);
                }
            }).exceptionally(ex -> {
                logger.error("ERROR: Exception occurred while polling Transit API for trip {}: {}", tripId,
                        ex.getMessage(), ex);
                return null;
            });
        } catch (Exception e) {
            logger.error("ERROR: Failed to create HTTP request for trip {}: {}", tripId, e.getMessage(), e);
        }
    }

    @Override
    public void dispose() {
        logger.debug("Disposing TransitAppTripDetailsHandler for thing: {}", getThing().getUID());
        if (refreshJob != null) {
            refreshJob.cancel(true);
        }
        super.dispose();
    }
}
