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
    private final java.util.Map<String, String> latestLineDepartures = new java.util.concurrent.ConcurrentHashMap<>();
    private @Nullable ScheduledFuture<?> refreshJob;

    public TransitAppStopHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        ScheduledFuture<?> job = refreshJob;
        if (job != null) {
            job.cancel(true);
        }
        Object refreshIntervalObj = getThing().getConfiguration().get("refreshInterval");
        long refreshInterval = refreshIntervalObj instanceof Number ? ((Number) refreshIntervalObj).longValue() : 60L;
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
        Bridge bridge = getBridge();
        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "Bridge not found");
            return;
        }

        String globalStopId = (String) getThing().getConfiguration().get("globalStopId");
        if (globalStopId == null || globalStopId.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Stop ID is missing");
            return;
        }

        String apiKey = (String) bridge.getConfiguration().get("apiKey");
        if (apiKey == null || apiKey.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "API Key missing on bridge");
            return;
        }

        try {
            String response = apiClient.fetchStopDepartures(apiKey, globalStopId);
            logger.debug("Polling transit API for stop ID: {}", globalStopId);
            logger.trace("Received raw JSON response for stop {}: {}", globalStopId, response);
            updateStatus(ThingStatus.ONLINE);

            try {
                com.google.gson.JsonObject jsonResponse = com.google.gson.JsonParser.parseString(response)
                        .getAsJsonObject();
                int groupIdx = 1;
                long now = System.currentTimeMillis() / 1000;
                latestLineDepartures.clear();

                if (jsonResponse.has("route_departures")) {
                    com.google.gson.JsonArray departures = jsonResponse.getAsJsonArray("route_departures");
                    for (int i = 0; i < departures.size() && groupIdx <= 10; i++) {
                        com.google.gson.JsonObject routeDep = departures.get(i).getAsJsonObject();
                        String shortName = routeDep.has("route_short_name")
                                ? routeDep.get("route_short_name").getAsString()
                                : null;
                        String longName = routeDep.has("route_long_name")
                                ? routeDep.get("route_long_name").getAsString()
                                : null;

                        if (routeDep.has("itineraries")) {
                            com.google.gson.JsonArray itineraries = routeDep.getAsJsonArray("itineraries");
                            for (int j = 0; j < itineraries.size() && groupIdx <= 10; j++) {
                                com.google.gson.JsonObject itinerary = itineraries.get(j).getAsJsonObject();
                                if (itinerary.has("schedule_items")) {
                                    com.google.gson.JsonArray schedules = itinerary.getAsJsonArray("schedule_items");
                                    for (int k = 0; k < schedules.size() && groupIdx <= 10; k++) {
                                        com.google.gson.JsonObject schedule = schedules.get(k).getAsJsonObject();
                                        String prefix = "depart" + groupIdx + "#";

                                        if (shortName != null) {
                                            updateState(prefix + "routeShortName", new StringType(shortName));
                                        }
                                        if (longName != null) {
                                            updateState(prefix + "routeLongName", new StringType(longName));
                                        }

                                        if (schedule.has("departure_time")) {
                                            long depTime = schedule.get("departure_time").getAsLong();
                                            long diff = (depTime - now) / 60;
                                            if (diff < 0) {
                                                continue;
                                            }
                                            updateState(prefix + "minutesUntilDeparture", new QuantityType<>(diff,
                                                    org.openhab.core.library.unit.Units.MINUTE));
                                            updateState(prefix + "departureTime",
                                                    new org.openhab.core.library.types.DateTimeType(
                                                            java.time.ZonedDateTime.ofInstant(
                                                                    java.time.Instant.ofEpochSecond(depTime),
                                                                    java.time.ZoneId.systemDefault())));
                                            if (shortName != null && !latestLineDepartures.containsKey(shortName)) {
                                                latestLineDepartures
                                                        .put(shortName,
                                                                java.time.LocalTime
                                                                        .ofInstant(
                                                                                java.time.Instant
                                                                                        .ofEpochSecond(depTime),
                                                                                java.time.ZoneId.systemDefault())
                                                                        .toString());
                                            }
                                        } else {
                                            updateState(prefix + "departureTime",
                                                    org.openhab.core.types.UnDefType.UNDEF);
                                        }

                                        if (schedule.has("delay")) {
                                            long delaySec = schedule.get("delay").getAsLong();
                                            updateState(prefix + "delayMinutes", new QuantityType<>(delaySec / 60,
                                                    org.openhab.core.library.unit.Units.MINUTE));
                                        } else {
                                            updateState(prefix + "delayMinutes",
                                                    org.openhab.core.types.UnDefType.UNDEF);
                                        }

                                        if (schedule.has("track") && !schedule.get("track").isJsonNull()) {
                                            updateState(prefix + "platform",
                                                    new StringType(schedule.get("track").getAsString()));
                                        } else {
                                            updateState(prefix + "platform", org.openhab.core.types.UnDefType.UNDEF);
                                        }

                                        if (schedule.has("wheelchair_accessible")) {
                                            updateState(prefix + "wheelchairAccessible",
                                                    schedule.get("wheelchair_accessible").getAsBoolean()
                                                            ? org.openhab.core.library.types.OnOffType.ON
                                                            : org.openhab.core.library.types.OnOffType.OFF);
                                        } else {
                                            updateState(prefix + "wheelchairAccessible",
                                                    org.openhab.core.types.UnDefType.UNDEF);
                                        }

                                        if (schedule.has("occupancy_status")
                                                && !schedule.get("occupancy_status").isJsonNull()) {
                                            updateState(prefix + "occupancy",
                                                    new StringType(schedule.get("occupancy_status").getAsString()));
                                        } else {
                                            updateState(prefix + "occupancy", org.openhab.core.types.UnDefType.UNDEF);
                                        }

                                        if (schedule.has("is_cancelled")) {
                                            updateState(prefix + "isCancelled",
                                                    schedule.get("is_cancelled").getAsBoolean()
                                                            ? org.openhab.core.library.types.OnOffType.ON
                                                            : org.openhab.core.library.types.OnOffType.OFF);
                                        } else {
                                            updateState(prefix + "isCancelled", org.openhab.core.types.UnDefType.UNDEF);
                                        }
                                        groupIdx++;
                                    }
                                }
                            }
                        }
                    }
                }

                for (int i = groupIdx; i <= 10; i++) {
                    String prefix = "depart" + i + "#";
                    updateState(prefix + "routeShortName", org.openhab.core.types.UnDefType.UNDEF);
                    updateState(prefix + "routeLongName", org.openhab.core.types.UnDefType.UNDEF);
                    updateState(prefix + "departureTime", org.openhab.core.types.UnDefType.UNDEF);
                    updateState(prefix + "minutesUntilDeparture", org.openhab.core.types.UnDefType.UNDEF);
                    updateState(prefix + "delayMinutes", org.openhab.core.types.UnDefType.UNDEF);
                    updateState(prefix + "platform", org.openhab.core.types.UnDefType.UNDEF);
                    updateState(prefix + "wheelchairAccessible", org.openhab.core.types.UnDefType.UNDEF);
                    updateState(prefix + "occupancy", org.openhab.core.types.UnDefType.UNDEF);
                    updateState(prefix + "isCancelled", org.openhab.core.types.UnDefType.UNDEF);
                }
            } catch (Exception parseEx) {
                logger.warn("Failed to parse JSON for stop {}: {}", globalStopId, parseEx.getMessage());
            }
        } catch (Exception e) {
            logger.warn("Warning/Communication issue while polling stop {}: {}", globalStopId, e.getMessage());
            logger.error("Detailed API error for stop {}: {}", globalStopId, e.getMessage(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    public String findNextDepartureByLine(String lineName) {
        return latestLineDepartures.getOrDefault(lineName, "N/A");
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
