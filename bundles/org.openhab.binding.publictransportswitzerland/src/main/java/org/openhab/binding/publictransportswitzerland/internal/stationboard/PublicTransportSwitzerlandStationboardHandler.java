/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import static org.openhab.binding.publictransportswitzerland.internal.PublicTransportSwitzerlandBindingConstants.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.cache.ExpiringCache;
import org.openhab.core.io.net.http.HttpUtil;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * The {@link PublicTransportSwitzerlandStationboardHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Jeremy Stucki - Initial contribution
 */
@NonNullByDefault
public class PublicTransportSwitzerlandStationboardHandler extends BaseThingHandler {

    // Limit the API response to the necessary fields
    private static final String FIELD_FILTERS = createFilterForFields("stationboard/to", "stationboard/category",
            "stationboard/number", "stationboard/stop/departureTimestamp", "stationboard/stop/delay",
            "stationboard/stop/platform");

    private static final String TSV_CHANNEL = "tsv";

    private final ChannelGroupUID dynamicChannelGroupUID = new ChannelGroupUID(getThing().getUID(), "departures");

    private final Logger logger = LoggerFactory.getLogger(PublicTransportSwitzerlandStationboardHandler.class);

    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

    private @Nullable ScheduledFuture<?> updateChannelsJob;
    private @Nullable ExpiringCache<@Nullable JsonElement> cache;
    private @Nullable PublicTransportSwitzerlandStationboardConfiguration configuration;

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
        // Together with the 10 second timeout, this should be less than a minute
        cache = new ExpiringCache<>(45_000, this::updateData);

        PublicTransportSwitzerlandStationboardConfiguration configuration = getConfigAs(
                PublicTransportSwitzerlandStationboardConfiguration.class);
        this.configuration = configuration;

        String configurationError = findConfigurationError(configuration);
        if (configurationError != null) {
            stopChannelUpdate();
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, configurationError);
        } else {
            updateStatus(ThingStatus.UNKNOWN);
            startChannelUpdate();
        }
    }

    @Override
    public void dispose() {
        stopChannelUpdate();
    }

    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters) {
        super.handleConfigurationUpdate(configurationParameters);

        PublicTransportSwitzerlandStationboardConfiguration configuration = getConfigAs(
                PublicTransportSwitzerlandStationboardConfiguration.class);
        this.configuration = configuration;

        ScheduledFuture<?> updateJob = updateChannelsJob;

        String configurationError = findConfigurationError(configuration);
        if (configurationError != null) {
            stopChannelUpdate();
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, configurationError);
        } else if (updateJob == null || updateJob.isCancelled()) {
            startChannelUpdate();
        }
    }

    private @Nullable String findConfigurationError(PublicTransportSwitzerlandStationboardConfiguration configuration) {
        String station = configuration.station;
        if (station == null || station.isEmpty()) {
            return "The station is not set";
        }

        return null;
    }

    private void startChannelUpdate() {
        updateChannelsJob = scheduler.scheduleWithFixedDelay(this::updateChannels, 0, 60, TimeUnit.SECONDS);
    }

    private void stopChannelUpdate() {
        ScheduledFuture<?> updateJob = updateChannelsJob;

        if (updateJob != null) {
            updateJob.cancel(true);
        }
    }

    public @Nullable JsonElement updateData() {
        PublicTransportSwitzerlandStationboardConfiguration config = configuration;
        if (config == null) {
            logger.warn("Unable to access configuration");
            return null;
        }

        String station = config.station;
        if (station == null) {
            logger.warn("Station is null");
            return null;
        }

        try {
            String escapedStation = URLEncoder.encode(station, StandardCharsets.UTF_8.name());
            String requestUrl = BASE_URL + "stationboard?station=" + escapedStation + FIELD_FILTERS;

            String response = HttpUtil.executeUrl("GET", requestUrl, 10_000);
            logger.debug("Got response from API: {}", response);

            return JsonParser.parseString(response);
        } catch (IOException e) {
            logger.warn("Unable to fetch stationboard data: {}", e.getMessage());
            return null;
        }
    }

    private static String createFilterForFields(String... fields) {
        return Arrays.stream(fields).map((field) -> "&fields[]=" + field).collect(Collectors.joining());
    }

    private void updateChannels() {
        ExpiringCache<@Nullable JsonElement> expiringCache = cache;

        if (expiringCache == null) {
            logger.warn("Cache is null");
            return;
        }

        JsonElement jsonObject = expiringCache.getValue();

        if (jsonObject == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);

            updateState(TSV_CHANNEL, UnDefType.UNDEF);

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
            JsonElement stopElement = departureObject.get("stop");

            if (stopElement == null) {
                logger.warn("Skipping stationboard item. Stop element is missing from departure object");
                continue;
            }

            JsonObject stopObject = stopElement.getAsJsonObject();

            JsonElement categoryElement = departureObject.get("category");
            JsonElement numberElement = departureObject.get("number");
            JsonElement destinationElement = departureObject.get("to");
            JsonElement departureTimeElement = stopObject.get("departureTimestamp");

            if (categoryElement == null || numberElement == null || destinationElement == null
                    || departureTimeElement == null) {
                logger.warn("Skipping stationboard item."
                        + "One of the following is null: category: {}, number: {}, destination: {}, departureTime: {}",
                        categoryElement, numberElement, destinationElement, departureTimeElement);
                continue;
            }

            String category = categoryElement.getAsString();
            String number = numberElement.getAsString();
            String destination = destinationElement.getAsString();
            Long departureTime = departureTimeElement.getAsLong();

            String identifier = createIdentifier(category, number);

            String delay = getStringValueOrNull(departureObject.get("delay"));
            String track = getStringValueOrNull(stopObject.get("platform"));

            updateState(getChannelUIDForPosition(i),
                    new StringType(formatDeparture(identifier, departureTime, destination, track, delay)));
            tsvRows.add(String.join("\t", identifier, departureTimeElement.toString(), destination, track, delay));
        }

        updateState(TSV_CHANNEL, new StringType(String.join("\n", tsvRows)));
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

    private String formatDeparture(String identifier, Long departureTimestamp, String destination,
            @Nullable String track, @Nullable String delay) {
        Date departureDate = new Date(departureTimestamp * 1000);
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
        if ("B".equals(category)) {
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
            Channel channel = ChannelBuilder.create(getChannelUIDForPosition(i), "String")
                    .withLabel("Departure " + (i + 1))
                    .withType(new ChannelTypeUID("publictransportswitzerland", "departure")).build();
            thingBuilder.withChannel(channel);
        }

        updateThing(thingBuilder.build());
    }

    private void setUnusedDynamicChannelsToUndef(int amountOfUsedChannels) {
        getThing().getChannelsOfGroup(dynamicChannelGroupUID.getId()).stream().skip(amountOfUsedChannels)
                .forEach(channel -> updateState(channel.getUID(), UnDefType.UNDEF));
    }

    private ChannelUID getChannelUIDForPosition(int position) {
        return new ChannelUID(dynamicChannelGroupUID, String.valueOf(position + 1));
    }
}
