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
import org.eclipse.smarthome.core.cache.ExpiringCache;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.*;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.UnDefType;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
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

    // Limit the API response to the necessary fields
    private static final String FIELD_FILTERS = createFilterForFields(
            "stationboard/to",
            "stationboard/category",
            "stationboard/number",
            "stationboard/stop/departureTimestamp",
            "stationboard/stop/delay",
            "stationboard/stop/platform");

    private final ChannelGroupUID dynamicChannelGroupUID = new ChannelGroupUID(getThing().getUID(), "departures");

    private final Logger logger = LoggerFactory.getLogger(PublicTransportSwitzerlandStationboardHandler.class);

    private @Nullable ScheduledFuture<?> updateChannelsJob;
    private @Nullable ExpiringCache<@Nullable JsonElement> cache;

    public PublicTransportSwitzerlandStationboardHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            updateChannels();
        }
    }

    @Override
    public void initialize() {
        PublicTransportSwitzerlandStationboardConfiguration config = getConfigAs(PublicTransportSwitzerlandStationboardConfiguration.class);

        cache = new ExpiringCache<>(60_000, this::updateData);

        if (config.station == null || config.station.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
        } else {
            updateStatus(ThingStatus.UNKNOWN);
            startChannelUpdate();
        }
    }

    @Override
    public void dispose() {
        stopChannelUpdate();
    }

    private void startChannelUpdate() {
        updateChannelsJob = scheduler.scheduleWithFixedDelay(this::updateChannels, 0, 10, TimeUnit.SECONDS);
    }

    private void stopChannelUpdate() {
        if (updateChannelsJob != null && !updateChannelsJob.isCancelled()) {
            updateChannelsJob.cancel(true);
        }
    }

    public @Nullable JsonElement updateData() {
        PublicTransportSwitzerlandStationboardConfiguration config = getConfigAs(PublicTransportSwitzerlandStationboardConfiguration.class);

        try {
            String escapedStation = URLEncoder.encode(config.station, StandardCharsets.UTF_8.name());
            String requestUrl = BASE_URL + "stationboard?station=" + escapedStation + FIELD_FILTERS;

            String response = HttpUtil.executeUrl("GET", requestUrl, 10_000);
            logger.debug("Got response from API: {}", response);

            return new JsonParser().parse(response);
        } catch (Exception e) {
            logger.warn("Unable to fetch stationboard data", e);
            return null;
        }
    }

    private static String createFilterForFields(String... fields) {
        return Arrays.stream(fields).map((field) -> "&fields[]=" + field).collect(Collectors.joining());
    }

    private void updateChannels() {
        @Nullable JsonElement jsonObject = cache.getValue();

        if (jsonObject == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);

            updateState(CHANNEL_TSV, UnDefType.UNDEF);

            for (Channel channel : getThing().getChannelsOfGroup(dynamicChannelGroupUID.getId())) {
                updateState(channel.getUID(), UnDefType.UNDEF);
            }

            return;
        }

        updateStatus(ThingStatus.ONLINE);

        JsonArray stationboard = jsonObject.getAsJsonObject().get("stationboard").getAsJsonArray();

        createDynamicChannels(stationboard.size());
        setUnusedDynamicChannelsToUndef(stationboard.size());

        List<String> tsvRows = new ArrayList<>();

        for (int i = 0; i < stationboard.size(); i++) {
            JsonElement jsonElement = stationboard.get(i);

            JsonObject departureObject = jsonElement.getAsJsonObject();
            JsonObject stopObject = departureObject.get("stop").getAsJsonObject();

            String category = departureObject.get("category").getAsString();
            String number = departureObject.get("number").getAsString();
            String destination = departureObject.get("to").getAsString();
            Long departureTime = stopObject.get("departureTimestamp").getAsLong();

            @Nullable String delay = getStringValueOrNull(departureObject.get("delay"));
            @Nullable String track = getStringValueOrNull(stopObject.get("platform"));

            String identifier = createIdentifier(category, number);

            updateState(getChannelUIDForPosition(i), new StringType(formatDeparture(identifier, departureTime, destination, track, delay)));
            tsvRows.add(String.join("\t", identifier, departureTime.toString(), destination, track, delay));
        }

        updateState(CHANNEL_TSV, new StringType(String.join("\n", tsvRows)));
    }

    private @Nullable String getStringValueOrNull(@Nullable JsonElement jsonElement) {
        if (jsonElement == null || jsonElement.isJsonNull()) {
            return null;
        }

        String stringValue = jsonElement.getAsString();

        if (stringValue.isEmpty()) {
            return null;
        }

        return stringValue;
    }

    private String formatDeparture(String identifier, Long departureTimestamp, String destination, @Nullable String track, @Nullable String delay) {
        Date departureDate = new Date(departureTimestamp * 1000);

        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        String formattedDate = timeFormat.format(departureDate);

        String result = String.format("%s - %s %s", formattedDate, identifier, destination);

        if (track != null) {
            result += " - Pl. " + track;
        }

        if (delay != null) {
            result += String.format(" (%s' late)", delay);
        }

        return result;
    }

    private String createIdentifier(String category, String number) {
        // Only show the number for buses
        if (category.equals("B")) {
            return number;
        }

        // Some weird quirk with the API
        if (number.startsWith(category)) {
            return category;
        }

        return category + number;
    }

    private void createDynamicChannels(int numberOfChannels) {
        List<Channel> existingChannels = getThing().getChannelsOfGroup(dynamicChannelGroupUID.getId());

        ThingBuilder thingBuilder = editThing();

        for (int i = existingChannels.size(); i < numberOfChannels; i++) {
            Channel channel = ChannelBuilder
                    .create(getChannelUIDForPosition(i), "String")
                    .withLabel("Departure " + (i + 1))
                    .build();
            thingBuilder.withChannel(channel);
        }

        updateThing(thingBuilder.build());
    }

    private void setUnusedDynamicChannelsToUndef(int amountOfUsedChannels) {
        getThing().getChannelsOfGroup(dynamicChannelGroupUID.getId())
                .stream()
                .skip(amountOfUsedChannels)
                .forEach(channel -> updateState(channel.getUID(), UnDefType.UNDEF));
    }

    private ChannelUID getChannelUIDForPosition(int position) {
        return new ChannelUID(dynamicChannelGroupUID, String.valueOf(position + 1));
    }

}
