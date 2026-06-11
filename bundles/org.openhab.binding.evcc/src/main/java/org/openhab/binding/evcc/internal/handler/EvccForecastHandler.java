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
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.type.ChannelTypeRegistry;
import org.openhab.core.types.State;
import org.openhab.core.types.TimeSeries;
import org.openhab.core.types.UnDefType;
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
        subType = getPropertyOrConfigValue(PROPERTY_SUBTYPE);
        type = PROPERTY_FORECAST;
    }

    @Override
    public void initialize() {
        super.initialize();
        Optional.ofNullable(bridgeHandler).ifPresent(handler -> {
            if (!SUPPORTED_FORECAST_TYPES.contains(subType)) {
                logger.warn("Unsupported forecast type: {}", subType);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Unsupported forecast type: " + subType);
                return;
            }
            JsonObject stateOpt = handler.getCachedEvccState().deepCopy();
            if (stateOpt.isEmpty()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                return;
            }
            if (stateOpt.has(JSON_KEY_FORECAST) && stateOpt.getAsJsonObject(JSON_KEY_FORECAST).has(subType)) {
                JsonObject state = new JsonObject();
                if (JSON_KEY_SOLAR.equals(subType)) {
                    state = stateOpt.getAsJsonObject(JSON_KEY_FORECAST).getAsJsonObject(subType).deepCopy();
                    modifyJSON(state);
                    state.addProperty("scaled", 0);
                    state.addProperty("solar", 0);
                }
                commonInitialize(state);
                isInitialized = true;
                handler.register(this);
                updateStatus(ThingStatus.ONLINE);
            } else {
                logger.warn("Forecast data for type {} is not available in the evcc state.", subType);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Unavailable forecast type: " + subType);
            }

        });
    }

    @Override
    public void prepareApiResponseForChannelStateUpdate(JsonObject state) {
        if (!isInitialized || state.isJsonNull() || state.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
            return;
        }
        JsonArray forecastArray = new JsonArray();
        switch (subType) {
            case JSON_KEY_CO2, JSON_KEY_FEED_IN, JSON_KEY_GRID -> forecastArray = extractCorrespondingForecast(state);
            case JSON_KEY_SOLAR -> {
                forecastArray = extractCorrespondingForecast(state);
                JsonObject solar = state.getAsJsonObject(JSON_KEY_FORECAST).getAsJsonObject(subType);
                modifyJSON(solar);
                updateStatesFromApiResponse(solar);
                float scale = solar.get(JSON_KEY_SCALE).getAsFloat();
                propagate(forecastArray, getThingKey("scaled"),
                        obj -> parseScaledForecast(obj, getThingKey(subType), scale));
            }
            default -> {
                logger.warn("Unknown forecast type: {}", subType);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
            }
        }
        propagate(forecastArray, getThingKey(subType), obj -> parseForecast(obj, getThingKey(subType)));
        updateStatus(ThingStatus.ONLINE);
    }

    private JsonArray extractCorrespondingForecast(JsonObject state) {
        if (state.has(JSON_KEY_FORECAST) && state.getAsJsonObject(JSON_KEY_FORECAST).has(subType)) {
            if (JSON_KEY_SOLAR.equals(subType)) {
                JsonObject solarObject = state.getAsJsonObject(JSON_KEY_FORECAST).getAsJsonObject(subType);
                return solarObject.has("timeseries") ? solarObject.getAsJsonArray("timeseries") : new JsonArray();
            } else {
                return state.getAsJsonObject(JSON_KEY_FORECAST).getAsJsonArray(subType);
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
            return new JsonArray();
        }
    }

    private void modifyJSON(JsonObject state) {
        for (String key : List.of(JSON_KEY_TODAY, JSON_KEY_TOMORROW, JSON_KEY_DAY_AFTER_TOMORROW)) {
            if (state.has(key) && state.get(key) instanceof JsonObject obj) {
                JsonElement completeEl = obj.get("complete");
                // If "complete" is false, drop the accumulated energy data for that day since it is incomplete
                if (completeEl instanceof JsonPrimitive primitive && primitive.isBoolean()
                        && primitive.getAsBoolean()) {
                    state.add(key, obj.get(JSON_KEY_ENERGY));
                }
            }
        }
    }

    private void propagate(JsonArray array, String key, Function<JsonObject, @Nullable ForecastData> parser) {
        ChannelUID uid = new ChannelUID(thing.getUID(), key);
        if (!isLinked(uid)) {
            return;
        }
        TimeSeries ts = getTimeSeries(array, parser);
        setForecastChannelState(ts, uid);
        sendTimeSeries(uid, ts);
    }

    private TimeSeries getTimeSeries(JsonArray forecastArray, Function<JsonObject, @Nullable ForecastData> parser) {
        TimeSeries timeSeries = new TimeSeries(TimeSeries.Policy.REPLACE);

        for (JsonElement data : forecastArray) {
            if (data instanceof JsonObject obj) {
                ForecastData parsed = parser.apply(obj);
                if (parsed != null) {
                    Instant time = OffsetDateTime.parse(parsed.timestamp()).toInstant();
                    timeSeries.add(time, parsed.value());
                }
            }
        }
        return timeSeries;
    }

    private void setForecastChannelState(TimeSeries timeSeries, ChannelUID channelUID) {
        if (timeSeries.size() == 0) {
            return;
        }
        Instant now = Instant.now();
        TimeSeries.Entry current = timeSeries.getStates().filter(e -> !e.timestamp().isAfter(now))
                .max(Comparator.comparing(TimeSeries.Entry::timestamp))
                .orElse(timeSeries.getStates().min(Comparator.comparing(TimeSeries.Entry::timestamp))
                        .orElse(new TimeSeries.Entry(now, UnDefType.UNDEF)));

        if (current != null && current.state() != UnDefType.UNDEF) {
            updateState(channelUID, current.state());
        }
    }

    @Nullable
    private ForecastData parseForecast(JsonObject obj, String thingKey) {
        FieldMapping m = getFieldMapping();
        if (m == null) {
            return null;
        }
        return parseValueAndTimestamp(obj.get(m.valueField()), obj.get(m.timestampField()), thingKey);
    }

    @Nullable
    private ForecastData parseScaledForecast(JsonObject obj, String thingKey, float scale) {
        FieldMapping m = getFieldMapping();
        if (m == null) {
            return null;
        }
        return parseValueAndTimestamp(new JsonPrimitive(obj.get(m.valueField()).getAsInt() * scale),
                obj.get(m.timestampField()), thingKey);
    }

    @Nullable
    private ForecastData parseValueAndTimestamp(@Nullable JsonElement valEl, @Nullable JsonElement tsEl,
            String thingKey) {
        if (valEl == null || valEl.isJsonNull() || tsEl == null || tsEl.isJsonNull()) {
            return null;
        }
        StateResolver resolver = StateResolver.getInstance();
        State value = resolver.resolveState(thingKey, valEl);
        if (value == null) {
            return null;
        }
        String ts = tsEl.getAsString();
        if (ts.isEmpty()) {
            return null;
        }
        return new ForecastData(value, ts);
    }

    @Override
    public JsonObject getStateFromCachedState(JsonObject state) {
        return state.has(JSON_KEY_FORECAST) ? state.getAsJsonObject(JSON_KEY_FORECAST).has(subType)
                ? state.getAsJsonObject(JSON_KEY_FORECAST).getAsJsonObject(subType)
                : new JsonObject() : new JsonObject();
    }

    private record FieldMapping(String valueField, String timestampField) {
    }

    @Nullable
    private FieldMapping getFieldMapping() {
        return switch (subType) {
            case JSON_KEY_CO2, JSON_KEY_FEED_IN, JSON_KEY_GRID -> new FieldMapping("value", "start");
            case JSON_KEY_SOLAR -> new FieldMapping("val", "ts");
            default -> null;
        };
    }

    private record ForecastData(State value, String timestamp) {
    }
}
