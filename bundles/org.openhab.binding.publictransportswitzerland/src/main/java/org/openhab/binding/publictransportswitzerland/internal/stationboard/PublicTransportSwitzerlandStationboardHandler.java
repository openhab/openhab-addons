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
package org.openhab.binding.publictransportswitzerland.internal.stationboard;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.openhab.binding.publictransportswitzerland.internal.PublicTransportSwitzerlandBindingConstants.*;

/**
 * The {@link PublicTransportSwitzerlandStationboardHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Jeremy Stucki - Initial contribution
 */
@NonNullByDefault
public class PublicTransportSwitzerlandStationboardHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(PublicTransportSwitzerlandStationboardHandler.class);

    private @Nullable ScheduledFuture<?> updateDataJob;

    // Limit the API response to the necessary fields
    private static final String fieldFilters = createFilterForFields(
            "stationboard/to",
            "stationboard/category",
            "stationboard/number",
            "stationboard/stop/departureTimestamp",
            "stationboard/stop/delay",
            "stationboard/stop/platform");

    public PublicTransportSwitzerlandStationboardHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            // TODO?
        }
    }

    @Override
    public void initialize() {
        PublicTransportSwitzerlandStationboardConfiguration config = getConfigAs(PublicTransportSwitzerlandStationboardConfiguration.class);

        if (config.station == null || config.station.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
            return;
        }

        updateStatus(ThingStatus.INITIALIZING);

        if (updateDataJob == null || updateDataJob.isCancelled()) {
            updateDataJob = scheduler.scheduleWithFixedDelay(this::updateData, 0, 60, TimeUnit.SECONDS);
        }
    }

    public void updateData() {
        PublicTransportSwitzerlandStationboardConfiguration config = getConfigAs(PublicTransportSwitzerlandStationboardConfiguration.class);

        try {
            String escapedStation = URLEncoder.encode(config.station, StandardCharsets.UTF_8.name());
            String requestUrl = BASE_URL + "stationboard?station=" + escapedStation + fieldFilters;

            String response = HttpUtil.executeUrl("GET", requestUrl, 10_000);
            JsonElement jsonObject = new JsonParser().parse(response);

            updateCsvChannel(jsonObject);
            updateJsonChannel(jsonObject);
            updateStatus(ThingStatus.ONLINE);
        } catch (Exception e) {
            logger.warn("Unable to fetch stationboard data", e);

            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            updateState(CHANNEL_CSV, new StringType("No data available"));
            updateState(CHANNEL_JSON, new StringType("{}"));
        }
    }

    private static String createFilterForFields(String... fields) {
        return Arrays.stream(fields).map((field) -> "&fields[]=" + field).collect(Collectors.joining());
    }

    private void updateJsonChannel(JsonElement jsonObject) {
        updateState(CHANNEL_JSON, new StringType(jsonObject.toString()));
    }

    private void updateCsvChannel(JsonElement jsonObject) throws Exception {
        JsonArray stationboard = jsonObject.getAsJsonObject().get("stationboard").getAsJsonArray();

        List<String> departures = new ArrayList<>();

        for (JsonElement jsonElement : stationboard) {
            JsonObject departureObject = jsonElement.getAsJsonObject();
            JsonObject stopObject = departureObject.get("stop").getAsJsonObject();

            String category = departureObject.get("category").getAsString();
            String number = departureObject.get("number").getAsString();
            String destination = departureObject.get("to").getAsString();

            JsonElement delayElement = departureObject.get("delay");
            String delay = "";
            if (delayElement != null) {
                delay = delayElement.getAsString();
            }

            String departureTime = stopObject.get("departureTimestamp").getAsString();
            String track = stopObject.get("platform").getAsString();

            departures.add(String.join("\t", category, number, departureTime, destination, track, delay));
        }

        updateState(CHANNEL_CSV, new StringType(String.join("\n", departures)));
    }

}
