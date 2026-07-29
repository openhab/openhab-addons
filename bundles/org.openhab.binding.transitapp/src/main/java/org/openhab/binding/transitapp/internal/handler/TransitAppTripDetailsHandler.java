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
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.PointType;
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

        Number refreshIntervalNum = (Number) getThing().getConfiguration().get("refreshInterval");
        long refreshInterval = refreshIntervalNum != null ? refreshIntervalNum.longValue() : 60L;

        logger.info("Scheduling trip details refresh job for {} with interval: {} seconds", getThing().getUID(),
                refreshInterval);

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
            logger.debug("Polling trip details and calculating target countdown");
            logger.error("ERROR: tripId is not configured for thing {}", getThing().getUID());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Trip ID is missing");
            return;
        }

        Bridge bridge = getBridge();
        if (bridge == null) {
            logger.debug("Polling trip details and calculating target countdown");
            logger.error("ERROR: Trip details thing {} is not attached to a Bridge!", getThing().getUID());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "Bridge not found");
            return;
        }

        String apiKey = (String) bridge.getConfiguration().get("apiKey");
        if (apiKey == null || apiKey.isEmpty()) {
            logger.debug("Polling trip details and calculating target countdown");
            logger.error("ERROR: API Key is missing on the TransitApp Bridge!");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "API Key missing on bridge");
            return;
        }

        try {
            String urlStr = "https://external.transitapp.com/v4/public/trip_details?trip_search_key="
                    + URLEncoder.encode(tripId, StandardCharsets.UTF_8);
            logger.debug("DEBUG: Building HTTP request for Trip Details URL: {}", urlStr);

            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(urlStr)).header("apiKey", apiKey).GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            int statusCode = response.statusCode();
            String jsonBody = response.body();

            if (statusCode == 200) {
                logger.info("INFO: Successfully polled trip details for trip ID {}", tripId);
                logger.debug("DEBUG: Full JSON response for trip {}: {}", tripId, jsonBody);
                updateStatus(ThingStatus.ONLINE);

                try {
                    com.google.gson.JsonObject jsonResponse = com.google.gson.JsonParser.parseString(jsonBody)
                            .getAsJsonObject();
                    com.google.gson.JsonObject trip = jsonResponse.has("trip") ? jsonResponse.getAsJsonObject("trip")
                            : jsonResponse;

                    if (trip.has("trip_headsign") && !trip.get("trip_headsign").isJsonNull()) {
                        updateState("trip#tripHeadsign", new StringType(trip.get("trip_headsign").getAsString()));
                    }
                    if (trip.has("route_short_name") && !trip.get("route_short_name").isJsonNull()) {
                        updateState("trip#routeShortName", new StringType(trip.get("route_short_name").getAsString()));
                    }

                    if (jsonResponse.has("vehicle") && !jsonResponse.get("vehicle").isJsonNull()) {
                        com.google.gson.JsonObject vehicle = jsonResponse.getAsJsonObject("vehicle");
                        if (vehicle.has("location") && !vehicle.get("location").isJsonNull()) {
                            com.google.gson.JsonObject loc = vehicle.getAsJsonObject("location");
                            if (loc.has("lat") && loc.has("lon")) {
                                updateState("trip#location",
                                        new PointType(new DecimalType(loc.get("lat").getAsDouble()),
                                                new DecimalType(loc.get("lon").getAsDouble())));
                            }
                        }
                    } else {
                        updateState("trip#location", org.openhab.core.types.UnDefType.UNDEF);
                    }

                    String targetStopId = (String) getThing().getConfiguration().get("targetStopId");
                    long now = System.currentTimeMillis() / 1000;

                    if (jsonResponse.has("stops")) {
                        com.google.gson.JsonArray stops = jsonResponse.getAsJsonArray("stops");
                        int stopIdx = 1;

                        for (int i = 0; i < stops.size(); i++) {
                            com.google.gson.JsonObject stop = stops.get(i).getAsJsonObject();

                            // Check target stop
                            if (targetStopId != null && stop.has("global_stop_id")
                                    && targetStopId.equals(stop.get("global_stop_id").getAsString())) {
                                if (stop.has("departure_time")) {
                                    long depTime = stop.get("departure_time").getAsLong();
                                    long diff = (depTime - now) / 60;
                                    updateState("trip#timeToTarget", new org.openhab.core.library.types.QuantityType<>(
                                            diff, org.openhab.core.library.unit.Units.MINUTE));
                                }
                            }

                            // Fill upcoming stops (limit to 10 future stops)
                            if (stopIdx <= 10 && stop.has("departure_time")
                                    && stop.get("departure_time").getAsLong() > now) {
                                String prefix = "stop" + stopIdx + "#";
                                if (stop.has("stop_name")) {
                                    updateState(prefix + "stopName",
                                            new StringType(stop.get("stop_name").getAsString()));
                                }
                                long depTime = stop.get("departure_time").getAsLong();
                                long diff = (depTime - now) / 60;
                                updateState(prefix + "minutesUntilDeparture",
                                        new org.openhab.core.library.types.QuantityType<>(diff,
                                                org.openhab.core.library.unit.Units.MINUTE));
                                stopIdx++;
                            }
                        }

                        // Clear remaining unused stop channels
                        for (int i = stopIdx; i <= 10; i++) {
                            updateState("stop" + i + "#stopName", org.openhab.core.types.UnDefType.UNDEF);
                            updateState("stop" + i + "#minutesUntilDeparture", org.openhab.core.types.UnDefType.UNDEF);
                        }
                    }
                } catch (Exception ex) {
                    logger.warn("Failed to parse JSON for trip {}: {}", tripId, ex.getMessage());
                }
            } else {
                logger.warn("WARN: Transit API returned status code {} for trip {}. Response body: {}", statusCode,
                        tripId, jsonBody);
            }
        } catch (Exception e) {
            logger.debug("Polling trip details and calculating target countdown");
            logger.error("ERROR: Failed to create HTTP request for trip {}: {}", tripId, e.getMessage(), e);
        }
    }

    @Override
    public void dispose() {
        logger.debug("Disposing TransitAppTripDetailsHandler for thing: {}", getThing().getUID());
        ScheduledFuture<?> job = refreshJob;
        if (job != null) {
            job.cancel(true);
        }
        super.dispose();
    }
}
