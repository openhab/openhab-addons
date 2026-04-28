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
package org.openhab.binding.smhi.internal;

import static org.junit.jupiter.api.Assertions.*;
import static org.openhab.binding.smhi.internal.SmhiBindingConstants.*;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.StreamSupport;

import javax.measure.MetricPrefix;
import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.smhi.provider.ParameterMetadata;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.State;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * @author Anders Alfredsson - Initial contribution
 */
@NonNullByDefault
public class SmhiTest {

    private static final ZonedDateTime BASE_TIME = ZonedDateTime.parse("2026-04-22T07:15:00Z");
    private static final ZonedDateTime DAY_0_END = ZonedDateTime.parse("2026-04-23T00:00:00Z");
    private @NonNullByDefault({}) SmhiTimeSeries timeSeries;
    private @NonNullByDefault({}) MockSmhiChannelTypeProvider channelTypeProvider;
    private @NonNullByDefault({}) JsonObject json;

    @BeforeEach
    public void setUp() throws ExecutionException, InterruptedException, TimeoutException {
        try (InputStream isTimeseries = SmhiTest.class.getResourceAsStream("snow1g1.json");
                InputStream isParameters = SmhiTest.class.getResourceAsStream("parameters.json")) {
            assertNotNull(isTimeseries);
            assertNotNull(isParameters);
            String strTimeseries = new String(isTimeseries.readAllBytes());
            String strParameters = new String(isParameters.readAllBytes());
            timeSeries = Parser.parseTimeSeries(strTimeseries);
            json = JsonParser.parseString(strTimeseries).getAsJsonObject();
            List<ParameterMetadata> parameterMetadata = Parser.parseParameterMetadata(strParameters);
            channelTypeProvider = new MockSmhiChannelTypeProvider();
            parameterMetadata.forEach(channelTypeProvider::putParameterMetadata);
            AGGREGATE_CHANNELS_METADATA.forEach(channelTypeProvider::putParameterMetadata);
        } catch (IOException e) {
            throw new AssertionError("Couldn't read forecast example");
        }
    }

    private BigDecimal sumBetween(JsonObject json, String param, ZonedDateTime start, ZonedDateTime end) {
        JsonArray timeSeries = json.get("timeSeries").getAsJsonArray();

        return StreamSupport.stream(timeSeries.spliterator(), true).map(JsonElement::getAsJsonObject)
                .filter(forecast -> {
                    ZonedDateTime time = ZonedDateTime.parse(forecast.get("time").getAsString());
                    return time.isAfter(start) && !time.isAfter(end);
                }).map(forecast -> {
                    ZonedDateTime time = ZonedDateTime.parse(forecast.get("time").getAsString());
                    ZonedDateTime intervalStartTime = ZonedDateTime
                            .parse(forecast.get("intervalParametersStartTime").getAsString());
                    BigDecimal hours = BigDecimal.valueOf(intervalStartTime.until(time, ChronoUnit.HOURS));
                    BigDecimal value = forecast.get("data").getAsJsonObject().get(param).getAsBigDecimal();
                    return hours.multiply(value);
                }).reduce(BigDecimal::add).orElseThrow(AssertionError::new);
    }

    private BigDecimal maxBetween(JsonObject json, String param, ZonedDateTime start, ZonedDateTime end) {
        JsonArray timeSeries = json.get("timeSeries").getAsJsonArray();

        return StreamSupport.stream(timeSeries.spliterator(), true).map(JsonElement::getAsJsonObject)
                .filter(forecast -> {
                    ZonedDateTime time = ZonedDateTime.parse(forecast.get("time").getAsString());
                    return !time.isBefore(start) && time.isBefore(end);
                }).map(jsonObject -> jsonObject.get("data").getAsJsonObject())
                .map(jsonObject -> jsonObject.get(param).getAsBigDecimal()).max(BigDecimal::compareTo)
                .orElseThrow(AssertionError::new);
    }

    private BigDecimal minBetween(JsonObject json, String param, ZonedDateTime start, ZonedDateTime end) {
        JsonArray timeSeries = json.get("timeSeries").getAsJsonArray();

        return StreamSupport.stream(timeSeries.spliterator(), true).map(JsonElement::getAsJsonObject)
                .filter(forecast -> {
                    ZonedDateTime time = ZonedDateTime.parse(forecast.get("time").getAsString());
                    return !time.isBefore(start) && time.isBefore(end);
                }).map(jsonObject -> jsonObject.get("data").getAsJsonObject())
                .map(jsonObject -> jsonObject.get(param).getAsBigDecimal()).min(BigDecimal::compareTo)
                .orElseThrow(AssertionError::new);
    }

    private BigDecimal noonOrFirst(JsonObject json, String param, ZonedDateTime start) {
        JsonArray timeSeries = json.get("timeSeries").getAsJsonArray();

        return StreamSupport.stream(timeSeries.spliterator(), false).map(JsonElement::getAsJsonObject)
                .filter(forecast -> {
                    ZonedDateTime time = ZonedDateTime.parse(forecast.get("time").getAsString());
                    return !time.isBefore(start.withHour(12).withMinute(0));
                }).findFirst().map(jsonObject -> jsonObject.get("data").getAsJsonObject().get(param).getAsBigDecimal())
                .orElseThrow(AssertionError::new);
    }

    @Test
    public void parameterTest() {
        assertNotNull(timeSeries);
        Forecast forecast1 = timeSeries.getForecast(BASE_TIME, 0).orElseThrow(AssertionError::new);
        Forecast forecast2 = timeSeries.getForecast(DAY_0_END.plusDays(2), 11).orElseThrow(AssertionError::new);
        Forecast forecast3 = timeSeries.getForecast(DAY_0_END.plusDays(2), 18).orElseThrow(AssertionError::new);

        Map<String, State> expected1 = Map.ofEntries(Map.entry(TEMPERATURE, new QuantityType<>(7, SIUnits.CELSIUS)),
                Map.entry(WIND_DIRECTION, new QuantityType<>(326, Units.DEGREE_ANGLE)),
                Map.entry(WIND_SPEED, new QuantityType<>(5.8, Units.METRE_PER_SECOND)),
                Map.entry(GUST, new QuantityType<>(14.8, Units.METRE_PER_SECOND)),
                Map.entry(RELATIVE_HUMIDITY, new QuantityType<>(58, Units.PERCENT)),
                Map.entry(PRESSURE, new QuantityType<>(1019.4, MetricPrefix.HECTO(SIUnits.PASCAL))),
                Map.entry(VISIBILITY, new QuantityType<>(36.3, MetricPrefix.KILO(SIUnits.METRE))),
                Map.entry(THUNDER_PROBABILITY, new QuantityType<>(0, Units.PERCENT)),
                Map.entry(FROZEN_PROBABILITY, new QuantityType<>(0, Units.PERCENT)),
                Map.entry(TOTAL_CLOUD_COVER, new QuantityType<>(62.5, Units.PERCENT)),
                Map.entry(LOW_CLOUD_COVER, new QuantityType<>(12.5, Units.PERCENT)),
                Map.entry(MEDIUM_CLOUD_COVER, new QuantityType<>(0, Units.PERCENT)),
                Map.entry(HIGH_CLOUD_COVER, new QuantityType<>(87.5, Units.PERCENT)),
                Map.entry(CLOUD_BASE_ALTITUDE, new QuantityType<>(9071, SIUnits.METRE)),
                Map.entry(CLOUD_TOP_ALTITUDE, new QuantityType<>(10759, SIUnits.METRE)),
                Map.entry(PRECIPITATION_MEAN, new QuantityType<>(0, Units.MILLIMETRE_PER_HOUR)),
                Map.entry(PRECIPITATION_MIN, new QuantityType<>(0, Units.MILLIMETRE_PER_HOUR)),
                Map.entry(PRECIPITATION_MAX, new QuantityType<>(0, Units.MILLIMETRE_PER_HOUR)),
                Map.entry(PRECIPITATION_MEDIAN, new QuantityType<>(0, Units.MILLIMETRE_PER_HOUR)),
                Map.entry(PRECIPITATION_PROBABILITY, new QuantityType<>(0, Units.PERCENT)),
                Map.entry(PERCENT_FROZEN, new QuantityType<>(-1, Units.PERCENT)),
                Map.entry(PRECIPITATION_CATEGORY, new DecimalType(0)), Map.entry(WEATHER_SYMBOL, new DecimalType(3)));

        Map<String, State> expected2 = Map.ofEntries(Map.entry(TEMPERATURE, new QuantityType<>(4.5, SIUnits.CELSIUS)),
                Map.entry(WIND_DIRECTION, new QuantityType<>(347, Units.DEGREE_ANGLE)),
                Map.entry(WIND_SPEED, new QuantityType<>(7.4, Units.METRE_PER_SECOND)),
                Map.entry(GUST, new QuantityType<>(17.4, Units.METRE_PER_SECOND)),
                Map.entry(RELATIVE_HUMIDITY, new QuantityType<>(45, Units.PERCENT)),
                Map.entry(PRESSURE, new QuantityType<>(1003.4, MetricPrefix.HECTO(SIUnits.PASCAL))),
                Map.entry(VISIBILITY, new QuantityType<>(47, MetricPrefix.KILO(SIUnits.METRE))),
                Map.entry(THUNDER_PROBABILITY, new QuantityType<>(1, Units.PERCENT)),
                Map.entry(FROZEN_PROBABILITY, new QuantityType<>(0, Units.PERCENT)),
                Map.entry(TOTAL_CLOUD_COVER, new QuantityType<>(50, Units.PERCENT)),
                Map.entry(LOW_CLOUD_COVER, new QuantityType<>(25, Units.PERCENT)),
                Map.entry(MEDIUM_CLOUD_COVER, new QuantityType<>(50, Units.PERCENT)),
                Map.entry(HIGH_CLOUD_COVER, new QuantityType<>(0, Units.PERCENT)),
                Map.entry(CLOUD_BASE_ALTITUDE, new QuantityType<>(-8, SIUnits.METRE)),
                Map.entry(CLOUD_TOP_ALTITUDE, new QuantityType<>(-8, SIUnits.METRE)),
                Map.entry(PRECIPITATION_MEAN, new QuantityType<>(0.5, Units.MILLIMETRE_PER_HOUR)),
                Map.entry(PRECIPITATION_MIN, new QuantityType<>(0.4, Units.MILLIMETRE_PER_HOUR)),
                Map.entry(PRECIPITATION_MAX, new QuantityType<>(1, Units.MILLIMETRE_PER_HOUR)),
                Map.entry(PRECIPITATION_MEDIAN, new QuantityType<>(0.4, Units.MILLIMETRE_PER_HOUR)),
                Map.entry(PRECIPITATION_PROBABILITY, new QuantityType<>(63, Units.PERCENT)),
                Map.entry(PERCENT_FROZEN, new QuantityType<>(100, Units.PERCENT)),
                Map.entry(PRECIPITATION_CATEGORY, new DecimalType(5)), Map.entry(WEATHER_SYMBOL, new DecimalType(15)));

        Map<String, State> expected3 = Map.ofEntries(Map.entry(TEMPERATURE, new QuantityType<>(-0.8, SIUnits.CELSIUS)),
                Map.entry(WIND_DIRECTION, new QuantityType<>(333, Units.DEGREE_ANGLE)),
                Map.entry(WIND_SPEED, new QuantityType<>(6.5, Units.METRE_PER_SECOND)),
                Map.entry(GUST, new QuantityType<>(14.4, Units.METRE_PER_SECOND)),
                Map.entry(RELATIVE_HUMIDITY, new QuantityType<>(68, Units.PERCENT)),
                Map.entry(PRESSURE, new QuantityType<>(1013.6, MetricPrefix.HECTO(SIUnits.PASCAL))),
                Map.entry(VISIBILITY, new QuantityType<>(22.9, MetricPrefix.KILO(SIUnits.METRE))),
                Map.entry(THUNDER_PROBABILITY, new QuantityType<>(0, Units.PERCENT)),
                Map.entry(FROZEN_PROBABILITY, new QuantityType<>(1, Units.PERCENT)),
                Map.entry(TOTAL_CLOUD_COVER, new QuantityType<>(0, Units.PERCENT)),
                Map.entry(LOW_CLOUD_COVER, new QuantityType<>(0, Units.PERCENT)),
                Map.entry(MEDIUM_CLOUD_COVER, new QuantityType<>(0, Units.PERCENT)),
                Map.entry(HIGH_CLOUD_COVER, new QuantityType<>(0, Units.PERCENT)),
                Map.entry(CLOUD_BASE_ALTITUDE, new QuantityType<>(-8, SIUnits.METRE)),
                Map.entry(CLOUD_TOP_ALTITUDE, new QuantityType<>(-8, SIUnits.METRE)),
                Map.entry(PRECIPITATION_MEAN, new QuantityType<>(0, Units.MILLIMETRE_PER_HOUR)),
                Map.entry(PRECIPITATION_MIN, new QuantityType<>(1.1, Units.MILLIMETRE_PER_HOUR)),
                Map.entry(PRECIPITATION_MAX, new QuantityType<>(1.2, Units.MILLIMETRE_PER_HOUR)),
                Map.entry(PRECIPITATION_MEDIAN, new QuantityType<>(0, Units.MILLIMETRE_PER_HOUR)),
                Map.entry(PRECIPITATION_PROBABILITY, new QuantityType<>(4, Units.PERCENT)),
                Map.entry(PERCENT_FROZEN, new QuantityType<>(100, Units.PERCENT)),
                Map.entry(PRECIPITATION_CATEGORY, new DecimalType(5)), Map.entry(WEATHER_SYMBOL, new DecimalType(1)));

        Map.of(forecast1, expected1, forecast2, expected2, forecast3, expected3)
                .forEach((forecast, expected) -> expected.forEach((s, v) -> {
                    ParameterMetadata meta = channelTypeProvider.getParameterMetadata(s);
                    assertNotNull(meta);
                    State state = forecast.getParameterAsState(meta);
                    assertEquals(v, state);
                }));
    }

    @Test
    public void forecastAggregatorTest() {
        assertNotNull(timeSeries);

        for (int dayOffset = 0; dayOffset < 10; dayOffset++) {
            State maxTemp = ForecastAggregator.max(timeSeries, dayOffset,
                    channelTypeProvider.getParameterMetadata(TEMPERATURE));
            State minTemp = ForecastAggregator.min(timeSeries, dayOffset,
                    channelTypeProvider.getParameterMetadata(TEMPERATURE));
            State maxWind = ForecastAggregator.max(timeSeries, dayOffset,
                    channelTypeProvider.getParameterMetadata(WIND_SPEED));
            State minWind = ForecastAggregator.min(timeSeries, dayOffset,
                    channelTypeProvider.getParameterMetadata(WIND_SPEED));
            State totalPrecip = ForecastAggregator.total(timeSeries, dayOffset,
                    channelTypeProvider.getParameterMetadata(PRECIPITATION_MEAN),
                    channelTypeProvider.getParameterMetadata(PRECIPITATION_TOTAL));

            ZonedDateTime startTime = dayOffset == 0 ? BASE_TIME : DAY_0_END.plusDays(dayOffset - 1);
            ZonedDateTime endTime = DAY_0_END.plusDays(dayOffset);

            assertInstanceOf(QuantityType.class, maxTemp);
            assertEquals(new QuantityType<>(maxBetween(json, TEMPERATURE, startTime, endTime), SIUnits.CELSIUS),
                    maxTemp.as(QuantityType.class));
            assertInstanceOf(QuantityType.class, minTemp);
            assertEquals(new QuantityType<>(minBetween(json, TEMPERATURE, startTime, endTime), SIUnits.CELSIUS),
                    minTemp.as(QuantityType.class));
            assertInstanceOf(QuantityType.class, maxWind);
            assertEquals(new QuantityType<>(maxBetween(json, WIND_SPEED, startTime, endTime), Units.METRE_PER_SECOND),
                    maxWind.as(QuantityType.class));
            assertInstanceOf(QuantityType.class, minWind);
            assertEquals(new QuantityType<>(minBetween(json, WIND_SPEED, startTime, endTime), Units.METRE_PER_SECOND),
                    minWind.as(QuantityType.class));
            assertInstanceOf(QuantityType.class, totalPrecip);
            assertEquals(new QuantityType<>(sumBetween(json, PRECIPITATION_MEAN, startTime, endTime),
                    MetricPrefix.MILLI(SIUnits.METRE)), totalPrecip.as(QuantityType.class));

            for (ParameterMetadata metadata : channelTypeProvider.getAllParameterMetadata()) {
                if (AGGREGATE_CHANNELS.contains(metadata.name())) {
                    continue;
                }
                Unit<?> expectedUnit = UNIT_MAP.get(metadata.unit());
                BigDecimal expectedValue = noonOrFirst(json, metadata.name(), startTime);
                if (expectedValue.equals(metadata.missingValue())
                        || (metadata.name().equals(PERCENT_FROZEN) && expectedValue.equals(BigDecimal.valueOf(-9)))) {
                    expectedValue = BigDecimal.valueOf(-1);
                } else if (metadata.unit().equals("octas")) {
                    expectedValue = OCTAS_TO_PERCENT.multiply(expectedValue);
                } else if (metadata.unit().equals("fraction")) {
                    expectedValue = FRACTION_TO_PERCENT.multiply(expectedValue);
                }
                State expectedState;
                if (expectedUnit != null) {
                    expectedState = new QuantityType<>(expectedValue, expectedUnit);
                } else {
                    expectedState = new DecimalType(expectedValue);
                }

                State actualState = ForecastAggregator.noonOrFirst(timeSeries, dayOffset, metadata);

                assertInstanceOf(expectedState.getClass(), actualState);
                assertEquals(expectedState, actualState);
            }
        }
    }

    @Test
    public void backwardsCompParameterTest() {
        assertNotNull(timeSeries);
        Forecast forecast1 = timeSeries.getForecast(BASE_TIME, 0).orElseThrow(AssertionError::new);
        Forecast forecast2 = timeSeries.getForecast(DAY_0_END.plusDays(2), 11).orElseThrow(AssertionError::new);
        Forecast forecast3 = timeSeries.getForecast(DAY_0_END.plusDays(2), 18).orElseThrow(AssertionError::new);

        Map<String, State> expected1 = Map.ofEntries(
                Map.entry(PMP3G_TEMPERATURE, new QuantityType<>(7, SIUnits.CELSIUS)),
                Map.entry(PMP3G_WIND_DIRECTION, new QuantityType<>(326, Units.DEGREE_ANGLE)),
                Map.entry(PMP3G_WIND_SPEED, new QuantityType<>(5.8, Units.METRE_PER_SECOND)),
                Map.entry(PMP3G_GUST, new QuantityType<>(14.8, Units.METRE_PER_SECOND)),
                Map.entry(PMP3G_RELATIVE_HUMIDITY, new QuantityType<>(58, Units.PERCENT)),
                Map.entry(PMP3G_PRESSURE, new QuantityType<>(1019.4, MetricPrefix.HECTO(SIUnits.PASCAL))),
                Map.entry(PMP3G_VISIBILITY, new QuantityType<>(36.3, MetricPrefix.KILO(SIUnits.METRE))),
                Map.entry(PMP3G_THUNDER_PROBABILITY, new QuantityType<>(0, Units.PERCENT)),
                Map.entry(PMP3G_TOTAL_CLOUD_COVER, new QuantityType<>(62.5, Units.PERCENT)),
                Map.entry(PMP3G_LOW_CLOUD_COVER, new QuantityType<>(12.5, Units.PERCENT)),
                Map.entry(PMP3G_MEDIUM_CLOUD_COVER, new QuantityType<>(0, Units.PERCENT)),
                Map.entry(PMP3G_HIGH_CLOUD_COVER, new QuantityType<>(87.5, Units.PERCENT)),
                Map.entry(PMP3G_PRECIPITATION_MEAN, new QuantityType<>(0, Units.MILLIMETRE_PER_HOUR)),
                Map.entry(PMP3G_PRECIPITATION_MIN, new QuantityType<>(0, Units.MILLIMETRE_PER_HOUR)),
                Map.entry(PMP3G_PRECIPITATION_MAX, new QuantityType<>(0, Units.MILLIMETRE_PER_HOUR)),
                Map.entry(PMP3G_PRECIPITATION_MEDIAN, new QuantityType<>(0, Units.MILLIMETRE_PER_HOUR)),
                Map.entry(PMP3G_PERCENT_FROZEN, new QuantityType<>(-1, Units.PERCENT)),
                Map.entry(PMP3G_PRECIPITATION_CATEGORY, new DecimalType(0)),
                Map.entry(PMP3G_WEATHER_SYMBOL, new DecimalType(3)));

        Map<String, State> expected2 = Map.ofEntries(
                Map.entry(PMP3G_TEMPERATURE, new QuantityType<>(4.5, SIUnits.CELSIUS)),
                Map.entry(PMP3G_WIND_DIRECTION, new QuantityType<>(347, Units.DEGREE_ANGLE)),
                Map.entry(PMP3G_WIND_SPEED, new QuantityType<>(7.4, Units.METRE_PER_SECOND)),
                Map.entry(PMP3G_GUST, new QuantityType<>(17.4, Units.METRE_PER_SECOND)),
                Map.entry(PMP3G_RELATIVE_HUMIDITY, new QuantityType<>(45, Units.PERCENT)),
                Map.entry(PMP3G_PRESSURE, new QuantityType<>(1003.4, MetricPrefix.HECTO(SIUnits.PASCAL))),
                Map.entry(PMP3G_VISIBILITY, new QuantityType<>(47, MetricPrefix.KILO(SIUnits.METRE))),
                Map.entry(PMP3G_THUNDER_PROBABILITY, new QuantityType<>(1, Units.PERCENT)),
                Map.entry(PMP3G_TOTAL_CLOUD_COVER, new QuantityType<>(50, Units.PERCENT)),
                Map.entry(PMP3G_LOW_CLOUD_COVER, new QuantityType<>(25, Units.PERCENT)),
                Map.entry(PMP3G_MEDIUM_CLOUD_COVER, new QuantityType<>(50, Units.PERCENT)),
                Map.entry(PMP3G_HIGH_CLOUD_COVER, new QuantityType<>(0, Units.PERCENT)),
                Map.entry(PMP3G_PRECIPITATION_MEAN, new QuantityType<>(0.5, Units.MILLIMETRE_PER_HOUR)),
                Map.entry(PMP3G_PRECIPITATION_MIN, new QuantityType<>(0.4, Units.MILLIMETRE_PER_HOUR)),
                Map.entry(PMP3G_PRECIPITATION_MAX, new QuantityType<>(1, Units.MILLIMETRE_PER_HOUR)),
                Map.entry(PMP3G_PRECIPITATION_MEDIAN, new QuantityType<>(0.4, Units.MILLIMETRE_PER_HOUR)),
                Map.entry(PMP3G_PERCENT_FROZEN, new QuantityType<>(100, Units.PERCENT)),
                Map.entry(PMP3G_PRECIPITATION_CATEGORY, new DecimalType(1)),
                Map.entry(PMP3G_WEATHER_SYMBOL, new DecimalType(15)));

        Map<String, State> expected3 = Map.ofEntries(
                Map.entry(PMP3G_TEMPERATURE, new QuantityType<>(-0.8, SIUnits.CELSIUS)),
                Map.entry(PMP3G_WIND_DIRECTION, new QuantityType<>(333, Units.DEGREE_ANGLE)),
                Map.entry(PMP3G_WIND_SPEED, new QuantityType<>(6.5, Units.METRE_PER_SECOND)),
                Map.entry(PMP3G_GUST, new QuantityType<>(14.4, Units.METRE_PER_SECOND)),
                Map.entry(PMP3G_RELATIVE_HUMIDITY, new QuantityType<>(68, Units.PERCENT)),
                Map.entry(PMP3G_PRESSURE, new QuantityType<>(1013.6, MetricPrefix.HECTO(SIUnits.PASCAL))),
                Map.entry(PMP3G_VISIBILITY, new QuantityType<>(22.9, MetricPrefix.KILO(SIUnits.METRE))),
                Map.entry(PMP3G_THUNDER_PROBABILITY, new QuantityType<>(0, Units.PERCENT)),
                Map.entry(PMP3G_TOTAL_CLOUD_COVER, new QuantityType<>(0, Units.PERCENT)),
                Map.entry(PMP3G_LOW_CLOUD_COVER, new QuantityType<>(0, Units.PERCENT)),
                Map.entry(PMP3G_MEDIUM_CLOUD_COVER, new QuantityType<>(0, Units.PERCENT)),
                Map.entry(PMP3G_HIGH_CLOUD_COVER, new QuantityType<>(0, Units.PERCENT)),
                Map.entry(PMP3G_PRECIPITATION_MEAN, new QuantityType<>(0, Units.MILLIMETRE_PER_HOUR)),
                Map.entry(PMP3G_PRECIPITATION_MIN, new QuantityType<>(1.1, Units.MILLIMETRE_PER_HOUR)),
                Map.entry(PMP3G_PRECIPITATION_MAX, new QuantityType<>(1.2, Units.MILLIMETRE_PER_HOUR)),
                Map.entry(PMP3G_PRECIPITATION_MEDIAN, new QuantityType<>(0, Units.MILLIMETRE_PER_HOUR)),
                Map.entry(PMP3G_PERCENT_FROZEN, new QuantityType<>(100, Units.PERCENT)),
                Map.entry(PMP3G_PRECIPITATION_CATEGORY, new DecimalType(1)),
                Map.entry(PMP3G_WEATHER_SYMBOL, new DecimalType(1)));

        Map.of(forecast1, expected1, forecast2, expected2, forecast3, expected3)
                .forEach((forecast, expected) -> expected.forEach((s, v) -> {
                    String parameter = PMP3G_BACKWARD_COMP.get(s);
                    assertNotNull(parameter);
                    ParameterMetadata meta = channelTypeProvider.getParameterMetadata(parameter);
                    assertNotNull(meta);
                    State state = forecast.getParameterAsState(meta);
                    if (s.equals(PMP3G_PRECIPITATION_CATEGORY)) {
                        state = new DecimalType(PMP3G_PCAT_BACKWARD_COMP.getOrDefault(((DecimalType) state).intValue(),
                                ((DecimalType) state).intValue()));
                    }
                    assertEquals(v, state);
                }));
    }
}
