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
package org.openhab.binding.evcc.internal.handler;

import static org.openhab.binding.evcc.internal.EvccBindingConstants.*;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelTypeRegistry;
import org.openhab.core.types.State;
import org.openhab.core.types.TimeSeries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * The {@link EvccForecastHandler} is responsible for fetching the data from the API response for Forecast things
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public class EvccForecastHandler extends EvccBaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(EvccForecastHandler.class);
    private final String subType;

    public EvccForecastHandler(Thing thing, ChannelTypeRegistry channelTypeRegistry) {
        super(thing, channelTypeRegistry);
        subType = getPropertyOrConfigValue(PROPERTY_TYPE);
        type = PROPERTY_FORECAST;
    }

    @Override
    public void initialize() {
        super.initialize();
        Optional.ofNullable(bridgeHandler).ifPresent(handler -> {
            JsonObject stateOpt = handler.getCachedEvccState().deepCopy();
            if (stateOpt.isEmpty()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                return;
            }
            handler.register(this);
            updateStatus(ThingStatus.ONLINE);
            if (stateOpt.has(JSON_KEY_FORECAST) && stateOpt.getAsJsonObject(JSON_KEY_FORECAST).has(subType)) {
                String thingKey = getThingKey(subType);
                ChannelUID channelUID = new ChannelUID(getThing().getUID(), thingKey);
                Channel existingChannel = getThing().getChannel(channelUID.getId());
                if (existingChannel == null) {
                    ThingBuilder builder = editThing();
                    List<Channel> channels = new ArrayList<>(getThing().getChannels());
                    builder.withoutChannels(channels);
                    @Nullable
                    Channel newChannel = createChannel(thingKey, new JsonPrimitive(0));
                    if (null != newChannel) {
                        channels.add(newChannel);
                        channels.sort(Comparator.comparing(channel -> channel.getUID().getId()));
                        updateThing(builder.withChannels(channels).build());
                    }
                }
                isInitialized = true;
                prepareApiResponseForChannelStateUpdate(stateOpt);
            }

        });
    }

    @Override
    public void prepareApiResponseForChannelStateUpdate(JsonObject state) {
        JsonArray forecastArray = new JsonArray();
        switch (subType) {
            case JSON_KEY_CO2, JSON_KEY_FEED_IN, JSON_KEY_GRID -> forecastArray = extractCorrespondingForecast(state);
            case JSON_KEY_SOLAR -> {
                forecastArray = extractCorrespondingForecast(state);
                updateStatesFromApiResponse(state);
            }
            default -> {
                logger.warn("Unknown forecast type: {}", subType);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
            }
        }
        if (state.isJsonNull() || state.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
        } else {
            propagateForecastArrayToChannel(forecastArray);
        }
    }

    private JsonArray extractCorrespondingForecast(JsonObject state) {
        if (state.has(JSON_KEY_FORECAST) && state.getAsJsonObject(JSON_KEY_FORECAST).has(subType)) {
            if (JSON_KEY_SOLAR.equals(subType)) {
                JsonObject solarObject = state.getAsJsonObject(JSON_KEY_FORECAST).getAsJsonObject(subType);
                ModifyJSON(solarObject);
                return solarObject.has("timeseries") ? solarObject.getAsJsonArray("timeseries") : new JsonArray();
            } else {
                return state.getAsJsonObject(JSON_KEY_FORECAST).getAsJsonArray(subType);
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
            return new JsonArray();
        }
    }

    private void ModifyJSON(JsonObject state) {
        if (state.has(JSON_KEY_TODAY)) {
            if (state.getAsJsonObject(JSON_KEY_TODAY).get("complete").getAsBoolean()) {
                state.add(JSON_KEY_TODAY, state.getAsJsonObject(JSON_KEY_TODAY).get(JSON_KEY_ENERGY));
            }
        }
        if (state.has(JSON_KEY_TOMORROW)) {
            if (state.getAsJsonObject(JSON_KEY_TOMORROW).get("complete").getAsBoolean()) {
                state.add(JSON_KEY_TOMORROW, state.getAsJsonObject(JSON_KEY_TOMORROW).get(JSON_KEY_ENERGY));
            }
        }
        if (state.has(JSON_KEY_DAY_AFTER_TOMORROW)) {
            if (state.getAsJsonObject(JSON_KEY_DAY_AFTER_TOMORROW).get("complete").getAsBoolean()) {
                state.add(JSON_KEY_DAY_AFTER_TOMORROW,
                        state.getAsJsonObject(JSON_KEY_DAY_AFTER_TOMORROW).get(JSON_KEY_ENERGY));
            }
        }
    }

    private void propagateForecastArrayToChannel(JsonArray forecastArray) {
        TimeSeries timeSeries = new TimeSeries(TimeSeries.Policy.REPLACE);
        String thingKey = getThingKey(subType);
        ChannelUID channelUID = new ChannelUID(thing.getUID(), thingKey);
        for (JsonElement data : forecastArray) {
            if (data instanceof JsonObject dataObj) {
                Optional.ofNullable(stateResolver).ifPresent(resolver -> {
                    State value = null;
                    String timestamp = "";
                    switch (subType) {
                        case JSON_KEY_CO2, JSON_KEY_FEED_IN, JSON_KEY_GRID -> {
                            value = resolver.resolveState(thingKey, dataObj.get("value"));
                            timestamp = dataObj.get("start").getAsString();
                        }
                        case JSON_KEY_SOLAR -> {
                            value = resolver.resolveState(thingKey, dataObj.get("val"));
                            timestamp = dataObj.get("ts").getAsString();
                        }
                    }
                    if (!timestamp.isEmpty() && value != null) {
                        Instant time = OffsetDateTime.parse(timestamp).toInstant();
                        timeSeries.add(time, value);
                    }
                });
            }
        }
        sendTimeSeries(channelUID, timeSeries);
    }

    @Override
    public JsonObject getStateFromCachedState(JsonObject state) {
        return state.has(JSON_KEY_FORECAST) ? state.getAsJsonObject(JSON_KEY_FORECAST).has(subType)
                ? state.getAsJsonObject(JSON_KEY_FORECAST).getAsJsonObject(subType)
                : new JsonObject() : new JsonObject();
    }
}
