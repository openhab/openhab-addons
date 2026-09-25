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
package org.openhab.binding.hasslink.internal.entity.impl;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hasslink.internal.ItemType;
import org.openhab.binding.hasslink.internal.api.dto.EntityState;
import org.openhab.binding.hasslink.internal.entity.ChannelSpec;
import org.openhab.binding.hasslink.internal.entity.EntityContext;
import org.openhab.binding.hasslink.internal.entity.EntityType;
import org.openhab.binding.hasslink.internal.entity.ParsedData;
import org.openhab.binding.hasslink.internal.entity.util.ChannelSpecsBuilder;
import org.openhab.binding.hasslink.internal.entity.util.StateMapBuilder;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.State;
import org.openhab.core.types.TimeSeries;

import com.google.gson.JsonElement;

/**
 * The {@link WeatherEntity} class represents a weather entity type in the Home Assistant binding.
 * Maps weather conditions, temperature, humidity, and pressure attributes to openHAB String and Number channels.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public class WeatherEntity implements EntityType {

    public static final long FORECAST_DAILY = 1L;
    public static final long FORECAST_HOURLY = 2L;
    public static final long FORECAST_TWICE_DAILY = 4L;

    /**
     * A record to hold the units of measurement for forecast attributes.
     *
     * @param temperatureUnit The unit of measurement for temperature attributes.
     * @param precipitationUnit The unit of measurement for precipitation attributes.
     * @param pressureUnit The unit of measurement for pressure attributes.
     * @param windSpeedUnit The unit of measurement for wind speed attributes.
     * @param visibilityUnit The unit of measurement for visibility attributes.
     * @param isWindBearingNumeric A flag indicating whether the wind bearing attribute is numeric.
     */
    private record StateUnits(@Nullable String temperatureUnit, @Nullable String precipitationUnit,
            @Nullable String pressureUnit, @Nullable String windSpeedUnit, @Nullable String visibilityUnit,
            boolean isWindBearingNumeric) {
    }

    @Override
    public String getType() {
        return "weather";
    }

    @Override
    public List<ChannelSpec> getChannelSpecs(EntityState entityState, EntityContext context) {

        ItemType windBearingItemType = isWindBearingNumeric(entityState) ? ItemType.number("Angle") : ItemType.STRING;

        ChannelSpecsBuilder builder = ChannelSpecsBuilder.create(entityState, context) //
                .addPrimaryChannel(ItemType.STRING) //
                .addAttr("cloud_coverage", ItemType.number("Dimensionless")) //
                .addAttr("humidity", ItemType.number("Humidity")) //
                .addAttr("apparent_temperature", ItemType.number("Temperature")) //
                .addAttr("dew_point", ItemType.number("Temperature")) //
                .addAttr("precipitation", ItemType.number("Length")) //
                .addAttr("pressure", ItemType.number("Pressure")) //
                .addAttr("temperature", ItemType.number("Temperature")) //
                .addAttr("visibility", ItemType.number("Length")) //
                .addAttr("wind_gust_speed", ItemType.number("Speed")) //
                .addAttr("wind_speed", ItemType.number("Speed")) //
                .addAttr("ozone", ItemType.NUMBER) //
                .addAttr("uv_index", ItemType.NUMBER) //
                .addAttr("wind_bearing", windBearingItemType);

        if (builder.isSupportedFeature(FORECAST_DAILY)) {
            buildForecastChannelSpecs(builder, "forecast", windBearingItemType);
        }
        if (builder.isSupportedFeature(FORECAST_HOURLY)) {
            buildForecastChannelSpecs(builder, "forecast_hourly", windBearingItemType);
        }
        if (builder.isSupportedFeature(FORECAST_TWICE_DAILY)) {
            buildForecastChannelSpecs(builder, "forecast_twice_daily", windBearingItemType);
        }

        return builder.build();
    }

    private boolean isWindBearingNumeric(EntityState entityState) {
        JsonElement windElem = entityState.getAttribute("wind_bearing");
        return windElem != null && windElem.isJsonPrimitive() && windElem.getAsJsonPrimitive().isNumber();
    }

    private void buildForecastChannelSpecs(ChannelSpecsBuilder builder, String forecastType,
            ItemType windBearingItemType) {
        ItemType dimensionless = ItemType.number("Dimensionless");
        ItemType temperature = ItemType.number("Temperature");
        ItemType length = ItemType.number("Length");
        ItemType pressure = ItemType.number("Pressure");
        ItemType speed = ItemType.number("Speed");

        Map.ofEntries( //
                Map.entry("cloud_coverage", dimensionless), //
                Map.entry("condition", ItemType.STRING), //
                Map.entry("humidity", dimensionless), //
                Map.entry("apparent_temperature", temperature), //
                Map.entry("dew_point", temperature), //
                Map.entry("precipitation", length), //
                Map.entry("pressure", pressure), //
                Map.entry("temperature", temperature), //
                Map.entry("templow", temperature), //
                Map.entry("wind_gust_speed", speed), //
                Map.entry("wind_speed", speed), //
                Map.entry("precipitation_probability", dimensionless), //
                Map.entry("uv_index", dimensionless), //
                Map.entry("wind_bearing", windBearingItemType) //
        ).forEach((attribute, itemType) -> {
            String channelId = forecastType + "_" + attribute;
            builder.add(channelId, itemType);
        });
    }

    @Override
    public Map<String, ParsedData> parseState(EntityState entityState, EntityContext context) {

        StateUnits units = new StateUnits( //
                entityState.getAttributeAsString("temperature_unit"), //
                entityState.getAttributeAsString("precipitation_unit"), //
                entityState.getAttributeAsString("pressure_unit"), //
                entityState.getAttributeAsString("wind_speed_unit"), //
                entityState.getAttributeAsString("visibility_unit"), //
                isWindBearingNumeric(entityState) //
        );

        StateMapBuilder builder = StateMapBuilder.create(entityState, context) //
                .putPrimaryStringState() //
                .putQuantity("cloud_coverage", Units.PERCENT) //
                .putQuantity("humidity", Units.PERCENT) //
                .putQuantity("apparent_temperature", units.temperatureUnit()) //
                .putQuantity("dew_point", units.temperatureUnit()) //
                .putQuantity("precipitation", units.precipitationUnit()) //
                .putQuantity("pressure", units.pressureUnit()) //
                .putQuantity("temperature", units.temperatureUnit()) //
                .putQuantity("visibility", units.visibilityUnit()) //
                .putQuantity("wind_gust_speed", units.windSpeedUnit()) //
                .putQuantity("wind_speed", units.windSpeedUnit()) //
                .putDecimal("ozone") //
                .putDecimal("uv_index");

        if (units.isWindBearingNumeric()) {
            builder.putQuantity("wind_bearing", Units.DEGREE_ANGLE);
        } else {
            builder.putString("wind_bearing");
        }

        long forecastFeatures = FORECAST_DAILY | FORECAST_HOURLY | FORECAST_TWICE_DAILY;
        if (entityState.isSupportedFeature(forecastFeatures)) {
            for (String forecastType : List.of("forecast", "forecast_hourly", "forecast_twice_daily")) {
                // Note that if the forecastType is not supported, buildForecastTimeSeries will return an empty map,
                // so it's safe to call it for all three types.
                buildForecastTimeSeries(forecastType, entityState, units).forEach((forecastKey, timeSeries) -> {
                    String attribute = forecastType + "_" + forecastKey;
                    builder.putTimeSeries(attribute, timeSeries);
                });
            }
        }

        return builder.build();
    }

    private Map<String, TimeSeries> buildForecastTimeSeries(String forecastType, EntityState entityState,
            StateUnits units) {

        List<Map<String, Object>> forecastList = getForecastList(entityState.getAttributeAsList(forecastType));
        if (forecastList == null || forecastList.isEmpty()) {
            return Map.of();
        }

        Map<String, TimeSeries> timeSeriesMap = new HashMap<>();
        for (String forecastKey : forecastList.getFirst().keySet()) {
            timeSeriesMap.put(forecastKey, new TimeSeries(TimeSeries.Policy.REPLACE));
        }

        // Build the time series data for each forecast key (i.e. transpose the list of maps into a map of time series)
        for (Map<String, Object> forecast : forecastList) {
            Instant timestamp = getForecastTimestamp(forecast);
            if (timestamp == null) {
                continue;
            }
            for (Map.Entry<String, Object> entry : forecast.entrySet()) {
                String forecastKey = entry.getKey();
                Object value = entry.getValue();
                TimeSeries timeSeries = timeSeriesMap.get(forecastKey);
                if (timeSeries == null) {
                    continue;
                }

                State state = convertForecastDataToState(forecastKey, value, units);
                timeSeries.add(timestamp, state);
            }
        }
        return timeSeriesMap;
    }

    private State convertForecastDataToState(String forecastKey, Object value, StateUnits units) {
        switch (forecastKey) {
            case "cloud_coverage":
            case "humidity":
            case "precipitation_probability":
                if (value instanceof Number num) {
                    return new QuantityType<>(num.doubleValue(), Units.PERCENT);
                }
                break;
            case "precipitation":
                if (value instanceof Number num) {
                    return QuantityType.valueOf(num.toString() + " " + units.precipitationUnit());
                }
                break;
            case "pressure":
                if (value instanceof Number num) {
                    return QuantityType.valueOf(num.toString() + " " + units.pressureUnit());
                }
                break;
            case "wind_gust_speed":
            case "wind_speed":
                if (value instanceof Number num) {
                    return QuantityType.valueOf(num.toString() + " " + units.windSpeedUnit());
                }
                break;
            case "apparent_temperature":
            case "dew_point":
            case "temperature":
            case "templow":
                if (value instanceof Number num) {
                    return QuantityType.valueOf(num.toString() + " " + units.temperatureUnit());
                }
                break;
            case "condition":
                if (value instanceof String str) {
                    return new StringType(str);
                }
                break;
            case "uv_index":
            case "ozone":
                if (value instanceof Number num) {
                    return new DecimalType(num.doubleValue());
                }
                break;
            case "wind_bearing":
                if (units.isWindBearingNumeric() && value instanceof Number num) {
                    return new QuantityType<>(num.doubleValue(), Units.DEGREE_ANGLE);
                } else {
                    return new StringType(value.toString());
                }
        }

        return new StringType(value.toString());
    }

    private @Nullable List<Map<String, Object>> getForecastList(@Nullable List<?> rawForecastList) {
        if (rawForecastList == null || rawForecastList.isEmpty()) {
            return null;
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> forecastList = rawForecastList.stream().filter(item -> item instanceof Map<?, ?>)
                .map(item -> (Map<String, Object>) item).toList();
        return forecastList;
    }

    private @Nullable Instant getForecastTimestamp(Map<String, Object> forecast) {
        Object dtObj = forecast.get("datetime");
        if (dtObj instanceof String dtStr) {
            try {
                return Instant.parse(dtStr);
            } catch (Exception ignored) {
            }
        }
        return null;
    }
}
