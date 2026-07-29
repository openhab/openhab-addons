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
import org.openhab.core.library.types.DecimalType;
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

@NonNullByDefault
public class TransitAppRouteDetailsHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(TransitAppRouteDetailsHandler.class);

    private @Nullable ScheduledFuture<?> refreshJob;

    public TransitAppRouteDetailsHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        ScheduledFuture<?> job = refreshJob;
        if (job != null) {
            job.cancel(true);
        }
        Object refreshIntervalObj = getThing().getConfiguration().get("refreshInterval");
        long refreshInterval = refreshIntervalObj instanceof Number ? ((Number) refreshIntervalObj).longValue() : 300L;
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
        String routeId = (String) getThing().getConfiguration().get("routeId");
        if (routeId == null || routeId.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Route ID is missing");
            return;
        }

        Bridge bridge = getBridge();
        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "Bridge not found");
            return;
        }

        String apiKey = (String) bridge.getConfiguration().get("apiKey");
        if (apiKey == null || apiKey.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "API Key missing on bridge");
            return;
        }

        try {
            TransitAppBridgeHandler bridgeHandler = (TransitAppBridgeHandler) bridge;
            String jsonBody = bridgeHandler.getApiClient().fetchRouteDetails(apiKey, routeId);
            if (jsonBody != null) {
                logger.debug("Successfully polled route details for route ID {}", routeId);
                updateStatus(ThingStatus.ONLINE);

                try {
                    com.google.gson.JsonObject jsonResponse = com.google.gson.JsonParser.parseString(jsonBody)
                            .getAsJsonObject();
                    com.google.gson.JsonObject route = jsonResponse.has("route") ? jsonResponse.getAsJsonObject("route")
                            : jsonResponse;

                    if (route.has("route_long_name") && !route.get("route_long_name").isJsonNull()) {
                        updateState("route#routeLongName", new StringType(route.get("route_long_name").getAsString()));
                    }
                    if (route.has("route_short_name") && !route.get("route_short_name").isJsonNull()) {
                        updateState("route#routeShortName",
                                new StringType(route.get("route_short_name").getAsString()));
                    }
                    if (route.has("route_color") && !route.get("route_color").isJsonNull()) {
                        updateState("route#routeColor", new StringType(route.get("route_color").getAsString()));
                    }

                    if (jsonResponse.has("alerts")) {
                        com.google.gson.JsonArray alerts = jsonResponse.getAsJsonArray("alerts");
                        updateState("route#activeAlertsCount", new DecimalType(alerts.size()));
                    } else {
                        updateState("route#activeAlertsCount", new DecimalType(0));
                    }
                } catch (Exception ex) {
                    logger.warn("Failed to parse JSON for route {}: {}", routeId, ex.getMessage());
                }
            } else {
                logger.warn("Transit API failed for route.");
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Failed to fetch route");
            }
        } catch (Exception e) {
            logger.warn("Communication error while polling route {}: {}", routeId, e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
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
