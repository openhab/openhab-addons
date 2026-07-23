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
 * The {@link TransitAppRouteDetailsHandler} is responsible for polling route details
 * and itineraries from the Transit API at configured intervals.
 *
 * @author Initial contribution - Initial contribution
 */
@NonNullByDefault
public class TransitAppRouteDetailsHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(TransitAppRouteDetailsHandler.class);
    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

    private @Nullable ScheduledFuture<?> refreshJob;

    public TransitAppRouteDetailsHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing TransitAppRouteDetailsHandler for thing: {}", getThing().getUID());

        // Read refresh interval configuration with fallback to 300 seconds
        Number refreshIntervalNum = (Number) getThing().getConfiguration().get("refreshInterval");
        long refreshInterval = refreshIntervalNum != null ? refreshIntervalNum.longValue() : 300L;

        logger.info("Scheduling route details refresh job for {} with interval: {} seconds", getThing().getUID(),
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
        logger.trace("TRACE: Starting background poll for route details thing {}", getThing().getUID());

        String routeId = (String) getThing().getConfiguration().get("routeId");
        if (routeId == null || routeId.isEmpty()) {
            logger.error("ERROR: routeId is not configured for thing {}", getThing().getUID());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Route ID is missing");
            return;
        }

        Bridge bridge = getBridge();
        if (bridge == null) {
            logger.error("ERROR: Route details thing {} is not attached to a Bridge!", getThing().getUID());
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
            String urlStr = "https://external.transitapp.com/v4/public/route_details?global_route_id=" + routeId;
            logger.debug("DEBUG: Building HTTP request for Route Details URL: {}", urlStr);

            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(urlStr)).header("apiKey", apiKey).GET()
                    .build();

            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenAccept(response -> {
                int statusCode = response.statusCode();
                String jsonBody = response.body();

                if (statusCode == 200) {
                    logger.info("INFO: Successfully polled route details for route ID {}", routeId);
                    logger.debug("DEBUG: Full JSON response for route {}:\n{}", routeId, jsonBody);
                    updateStatus(ThingStatus.ONLINE);

                    updateState("routeLongName", new StringType("Live Route Data"));
                } else {
                    logger.warn("WARN: Transit API returned status code {} for route {}. Response body: {}", statusCode,
                            routeId, jsonBody);
                }
            }).exceptionally(ex -> {
                logger.error("ERROR: Exception occurred while polling Transit API for route {}: {}", routeId,
                        ex.getMessage(), ex);
                return null;
            });
        } catch (Exception e) {
            logger.error("ERROR: Failed to create HTTP request for route {}: {}", routeId, e.getMessage(), e);
        }
    }

    @Override
    public void dispose() {
        logger.debug("Disposing TransitAppRouteDetailsHandler for thing: {}", getThing().getUID());
        if (refreshJob != null) {
            refreshJob.cancel(true);
        }
        super.dispose();
    }
}
