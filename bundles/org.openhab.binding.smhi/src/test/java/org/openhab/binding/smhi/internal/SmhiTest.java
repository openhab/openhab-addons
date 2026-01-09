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
import java.util.stream.StreamSupport;

import javax.measure.MetricPrefix;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

    private static final ZonedDateTime BASE_TIME = ZonedDateTime.parse("2025-11-03T14:20:00Z");
    private static final ZonedDateTime DAY_0_END = ZonedDateTime.parse("2025-11-04T00:00:00Z");
    private static final ZonedDateTime DAY_8_END = DAY_0_END.plusDays(8);
    private static final ZonedDateTime DAY_9_END = DAY_0_END.plusDays(9);
    private @NonNullByDefault({}) SmhiTimeSeries timeSeries1;
    private @NonNullByDefault({}) SmhiTimeSeries timeSeries2;
    private @NonNullByDefault({}) SmhiTimeSeries timeSeries3;
    private @NonNullByDefault({}) JsonObject json1;
    private @NonNullByDefault({}) JsonObject json2;
    private @NonNullByDefault({}) JsonObject json3;

    @BeforeEach
    public void setUp() {
        try (InputStream is1 = SmhiTest.class.getResourceAsStream("snow1g1.json");
                InputStream is2 = SmhiTest.class.getResourceAsStream("snow1g2.json");
                InputStream is3 = SmhiTest.class.getResourceAsStream("snow1g3.json")) {
            assertNotNull(is1);
            assertNotNull(is2);
            assertNotNull(is3);
            String str1 = new String(is1.readAllBytes());
            String str2 = new String(is2.readAllBytes());
            String str3 = new String(is3.readAllBytes());
            timeSeries1 = Parser.parseTimeSeries(str1);
            timeSeries2 = Parser.parseTimeSeries(str2);
            timeSeries3 = Parser.parseTimeSeries(str3);
            json1 = JsonParser.parseString(str1).getAsJsonObject();
            json2 = JsonParser.parseString(str2).getAsJsonObject();
            json3 = JsonParser.parseString(str3).getAsJsonObject();
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
        assertNotNull(timeSeries1);
        Forecast forecast0 = timeSeries1.getForecast(BASE_TIME, 0).orElseThrow(AssertionError::new);

        State pres0 = forecast0.getParameterAsState(PRESSURE);
        State t0 = forecast0.getParameterAsState(TEMPERATURE);
        State vis0 = forecast0.getParameterAsState(VISIBILITY);
        State wd0 = forecast0.getParameterAsState(WIND_DIRECTION);
        State ws0 = forecast0.getParameterAsState(WIND_SPEED);
        State r0 = forecast0.getParameterAsState(RELATIVE_HUMIDITY);
        State tstm0 = forecast0.getParameterAsState(THUNDER_PROBABILITY);
        State cdcb0 = forecast0.getParameterAsState(CLOUD_BASE_ALTITUDE);
        State cdct0 = forecast0.getParameterAsState(CLOUD_TOP_ALTITUDE);
        State tcc0 = forecast0.getParameterAsState(TOTAL_CLOUD_COVER);
        State lcc0 = forecast0.getParameterAsState(LOW_CLOUD_COVER);
        State mcc0 = forecast0.getParameterAsState(MEDIUM_CLOUD_COVER);
        State hcc0 = forecast0.getParameterAsState(HIGH_CLOUD_COVER);
        State gust0 = forecast0.getParameterAsState(GUST);
        State pmin0 = forecast0.getParameterAsState(PRECIPITATION_MIN);
        State pmax0 = forecast0.getParameterAsState(PRECIPITATION_MAX);
        State pmean0 = forecast0.getParameterAsState(PRECIPITATION_MEAN);
        State pmedian0 = forecast0.getParameterAsState(PRECIPITATION_MEDIAN);
        State spp0 = forecast0.getParameterAsState(PERCENT_FROZEN);
        State fzpr0 = forecast0.getParameterAsState(FROZEN_PROBABILITY);
        State tp0 = forecast0.getParameterAsState(PRECIPITATION_PROBABILITY);
        State pcat0 = forecast0.getParameterAsState(PRECIPITATION_CATEGORY);
        State wsymb0 = forecast0.getParameterAsState(WEATHER_SYMBOL);

        assertInstanceOf(QuantityType.class, pres0);
        assertEquals(new QuantityType<>(1010.9, MetricPrefix.HECTO(SIUnits.PASCAL)), pres0);
        assertInstanceOf(QuantityType.class, t0);
        assertEquals(new QuantityType<>(7.5, SIUnits.CELSIUS), t0);
        assertInstanceOf(QuantityType.class, vis0);
        assertEquals(new QuantityType<>(13.3, MetricPrefix.KILO(SIUnits.METRE)), vis0);
        assertInstanceOf(QuantityType.class, wd0);
        assertEquals(new QuantityType<>(219, Units.DEGREE_ANGLE), wd0);
        assertInstanceOf(QuantityType.class, ws0);
        assertEquals(new QuantityType<>(1.5, Units.METRE_PER_SECOND), ws0);
        assertInstanceOf(QuantityType.class, r0);
        assertEquals(new QuantityType<>(86, Units.PERCENT), r0);
        assertInstanceOf(QuantityType.class, tstm0);
        assertEquals(new QuantityType<>(0, Units.PERCENT), tstm0);
        assertInstanceOf(QuantityType.class, cdcb0);
        assertEquals(new QuantityType<>(-1, SIUnits.METRE), cdcb0);
        assertInstanceOf(QuantityType.class, cdct0);
        assertEquals(new QuantityType<>(-1, SIUnits.METRE), cdct0);
        assertInstanceOf(QuantityType.class, tcc0);
        assertEquals(new QuantityType<>(50, Units.PERCENT), tcc0);
        assertInstanceOf(QuantityType.class, lcc0);
        assertEquals(new QuantityType<>(37.5, Units.PERCENT), lcc0);
        assertInstanceOf(QuantityType.class, mcc0);
        assertEquals(new QuantityType<>(12.5, Units.PERCENT), mcc0);
        assertInstanceOf(QuantityType.class, hcc0);
        assertEquals(new QuantityType<>(0, Units.PERCENT), hcc0);
        assertInstanceOf(QuantityType.class, gust0);
        assertEquals(new QuantityType<>(4.8, Units.METRE_PER_SECOND), gust0);
        assertInstanceOf(QuantityType.class, pmin0);
        assertEquals(new QuantityType<>(0, Units.MILLIMETRE_PER_HOUR), pmin0);
        assertInstanceOf(QuantityType.class, pmax0);
        assertEquals(new QuantityType<>(0, Units.MILLIMETRE_PER_HOUR), pmax0);
        assertInstanceOf(QuantityType.class, pmean0);
        assertEquals(new QuantityType<>(0, Units.MILLIMETRE_PER_HOUR), pmean0);
        assertInstanceOf(QuantityType.class, pmedian0);
        assertEquals(new QuantityType<>(0, Units.MILLIMETRE_PER_HOUR), pmedian0);
        assertInstanceOf(QuantityType.class, spp0);
        assertEquals(new QuantityType<>(-1, Units.PERCENT), spp0);
        assertInstanceOf(QuantityType.class, fzpr0);
        assertEquals(new QuantityType<>(0, Units.PERCENT), fzpr0);
        assertInstanceOf(QuantityType.class, tp0);
        assertEquals(new QuantityType<>(0, Units.PERCENT), tp0);
        assertInstanceOf(DecimalType.class, pcat0);
        assertEquals(new DecimalType(0), pcat0);
        assertInstanceOf(DecimalType.class, wsymb0);
        assertEquals(new DecimalType(3), wsymb0);
    }

    @Test
    public void forecastAggregatorTest() {
        assertNotNull(timeSeries1);
        assertNotNull(timeSeries2);
        State maxTempT1F0 = ForecastAggregator.max(timeSeries1, 0, TEMPERATURE);
        State minTempT1F0 = ForecastAggregator.min(timeSeries1, 0, TEMPERATURE);
        State maxWindT1F0 = ForecastAggregator.max(timeSeries1, 0, WIND_SPEED);
        State minWindT1F0 = ForecastAggregator.min(timeSeries1, 0, WIND_SPEED);
        State totalPrecipT1F0 = ForecastAggregator.total(timeSeries1, 0, PRECIPITATION_MEAN);
        State noonPressureT1F0 = ForecastAggregator.noonOrFirst(timeSeries1, 0, PRESSURE);

        assertInstanceOf(QuantityType.class, maxTempT1F0);
        assertEquals(new QuantityType<>(maxBetween(json1, TEMPERATURE, BASE_TIME, DAY_0_END), SIUnits.CELSIUS),
                maxTempT1F0.as(QuantityType.class));
        assertInstanceOf(QuantityType.class, minTempT1F0);
        assertEquals(new QuantityType<>(minBetween(json1, TEMPERATURE, BASE_TIME, DAY_0_END), SIUnits.CELSIUS),
                minTempT1F0.as(QuantityType.class));
        assertInstanceOf(QuantityType.class, maxWindT1F0);
        assertEquals(new QuantityType<>(maxBetween(json1, WIND_SPEED, BASE_TIME, DAY_0_END), Units.METRE_PER_SECOND),
                maxWindT1F0.as(QuantityType.class));
        assertInstanceOf(QuantityType.class, minWindT1F0);
        assertEquals(new QuantityType<>(minBetween(json1, WIND_SPEED, BASE_TIME, DAY_0_END), Units.METRE_PER_SECOND),
                minWindT1F0.as(QuantityType.class));
        assertInstanceOf(QuantityType.class, totalPrecipT1F0);
        assertEquals(new QuantityType<>(sumBetween(json1, PRECIPITATION_MEAN, BASE_TIME, DAY_0_END),
                MetricPrefix.MILLI(SIUnits.METRE)), totalPrecipT1F0.as(QuantityType.class));
        assertInstanceOf(QuantityType.class, noonPressureT1F0);
        assertEquals(new QuantityType<>(noonOrFirst(json1, PRESSURE, BASE_TIME), MetricPrefix.HECTO(SIUnits.PASCAL)),
                noonPressureT1F0.as(QuantityType.class));

        State maxTempT1F9 = ForecastAggregator.max(timeSeries1, 9, TEMPERATURE);
        State minTempT1F9 = ForecastAggregator.min(timeSeries1, 9, TEMPERATURE);
        State maxWindT1F9 = ForecastAggregator.max(timeSeries1, 9, WIND_SPEED);
        State minWindT1F9 = ForecastAggregator.min(timeSeries1, 9, WIND_SPEED);
        State totalPrecipT1F9 = ForecastAggregator.total(timeSeries1, 9, PRECIPITATION_MEAN);
        State noonPressureT1F9 = ForecastAggregator.noonOrFirst(timeSeries1, 9, PRESSURE);

        assertInstanceOf(QuantityType.class, maxTempT1F9);
        assertEquals(new QuantityType<>(maxBetween(json1, TEMPERATURE, DAY_8_END, DAY_9_END), SIUnits.CELSIUS),
                maxTempT1F9.as(QuantityType.class));
        assertInstanceOf(QuantityType.class, minTempT1F9);
        assertEquals(new QuantityType<>(minBetween(json1, TEMPERATURE, DAY_8_END, DAY_9_END), SIUnits.CELSIUS),
                minTempT1F9.as(QuantityType.class));
        assertInstanceOf(QuantityType.class, maxWindT1F9);
        assertEquals(new QuantityType<>(maxBetween(json1, WIND_SPEED, DAY_8_END, DAY_9_END), Units.METRE_PER_SECOND),
                maxWindT1F9.as(QuantityType.class));
        assertInstanceOf(QuantityType.class, minWindT1F9);
        assertEquals(new QuantityType<>(minBetween(json1, WIND_SPEED, DAY_8_END, DAY_9_END), Units.METRE_PER_SECOND),
                minWindT1F9.as(QuantityType.class));
        assertInstanceOf(QuantityType.class, totalPrecipT1F9);
        assertEquals(new QuantityType<>(sumBetween(json1, PRECIPITATION_MEAN, DAY_8_END, DAY_9_END),
                MetricPrefix.MILLI(SIUnits.METRE)), totalPrecipT1F9.as(QuantityType.class));
        assertInstanceOf(QuantityType.class, noonPressureT1F9);
        assertEquals(new QuantityType<>(noonOrFirst(json1, PRESSURE, DAY_8_END), MetricPrefix.HECTO(SIUnits.PASCAL)),
                noonPressureT1F9.as(QuantityType.class));

        State maxTempT2F0 = ForecastAggregator.max(timeSeries2, 0, TEMPERATURE);
        State minTempT2F0 = ForecastAggregator.min(timeSeries2, 0, TEMPERATURE);
        State maxWindT2F0 = ForecastAggregator.max(timeSeries2, 0, WIND_SPEED);
        State minWindT2F0 = ForecastAggregator.min(timeSeries2, 0, WIND_SPEED);
        State totalPrecipT2F0 = ForecastAggregator.total(timeSeries2, 0, PRECIPITATION_MEAN);
        State noonPressureT2F0 = ForecastAggregator.noonOrFirst(timeSeries2, 0, PRESSURE);

        assertInstanceOf(QuantityType.class, maxTempT2F0);
        assertEquals(new QuantityType<>(maxBetween(json2, TEMPERATURE, BASE_TIME, DAY_0_END), SIUnits.CELSIUS),
                maxTempT2F0.as(QuantityType.class));
        assertInstanceOf(QuantityType.class, minTempT2F0);
        assertEquals(new QuantityType<>(minBetween(json2, TEMPERATURE, BASE_TIME, DAY_0_END), SIUnits.CELSIUS),
                minTempT2F0.as(QuantityType.class));
        assertInstanceOf(QuantityType.class, maxWindT2F0);
        assertEquals(new QuantityType<>(maxBetween(json2, WIND_SPEED, BASE_TIME, DAY_0_END), Units.METRE_PER_SECOND),
                maxWindT2F0.as(QuantityType.class));
        assertInstanceOf(QuantityType.class, minWindT2F0);
        assertEquals(new QuantityType<>(minBetween(json2, WIND_SPEED, BASE_TIME, DAY_0_END), Units.METRE_PER_SECOND),
                minWindT2F0.as(QuantityType.class));
        assertInstanceOf(QuantityType.class, totalPrecipT2F0);
        assertEquals(new QuantityType<>(sumBetween(json2, PRECIPITATION_MEAN, BASE_TIME, DAY_0_END),
                MetricPrefix.MILLI(SIUnits.METRE)), totalPrecipT2F0.as(QuantityType.class));
        assertInstanceOf(QuantityType.class, noonPressureT2F0);
        assertEquals(new QuantityType<>(noonOrFirst(json2, PRESSURE, BASE_TIME), MetricPrefix.HECTO(SIUnits.PASCAL)),
                noonPressureT2F0.as(QuantityType.class));

        State maxTempT2F9 = ForecastAggregator.max(timeSeries2, 9, TEMPERATURE);
        State minTempT2F9 = ForecastAggregator.min(timeSeries2, 9, TEMPERATURE);
        State maxWindT2F9 = ForecastAggregator.max(timeSeries2, 9, WIND_SPEED);
        State minWindT2F9 = ForecastAggregator.min(timeSeries2, 9, WIND_SPEED);
        State totalPrecipT2F9 = ForecastAggregator.total(timeSeries2, 9, PRECIPITATION_MEAN);
        State noonPressureT2F9 = ForecastAggregator.noonOrFirst(timeSeries2, 9, PRESSURE);

        assertInstanceOf(QuantityType.class, maxTempT2F9);
        assertEquals(new QuantityType<>(maxBetween(json2, TEMPERATURE, DAY_8_END, DAY_9_END), SIUnits.CELSIUS),
                maxTempT2F9.as(QuantityType.class));
        assertInstanceOf(QuantityType.class, minTempT2F9);
        assertEquals(new QuantityType<>(minBetween(json2, TEMPERATURE, DAY_8_END, DAY_9_END), SIUnits.CELSIUS),
                minTempT2F9.as(QuantityType.class));
        assertInstanceOf(QuantityType.class, maxWindT2F9);
        assertEquals(new QuantityType<>(maxBetween(json2, WIND_SPEED, DAY_8_END, DAY_9_END), Units.METRE_PER_SECOND),
                maxWindT2F9.as(QuantityType.class));
        assertInstanceOf(QuantityType.class, minWindT2F9);
        assertEquals(new QuantityType<>(minBetween(json2, WIND_SPEED, DAY_8_END, DAY_9_END), Units.METRE_PER_SECOND),
                minWindT2F9.as(QuantityType.class));
        assertInstanceOf(QuantityType.class, totalPrecipT2F9);
        assertEquals(new QuantityType<>(sumBetween(json2, PRECIPITATION_MEAN, DAY_8_END, DAY_9_END),
                MetricPrefix.MILLI(SIUnits.METRE)), totalPrecipT2F9.as(QuantityType.class));
        assertInstanceOf(QuantityType.class, noonPressureT2F9);
        assertEquals(new QuantityType<>(noonOrFirst(json2, PRESSURE, DAY_8_END), MetricPrefix.HECTO(SIUnits.PASCAL)),
                noonPressureT2F9.as(QuantityType.class));

        State maxTempT3F0 = ForecastAggregator.max(timeSeries3, 0, TEMPERATURE);
        State minTempT3F0 = ForecastAggregator.min(timeSeries3, 0, TEMPERATURE);
        State maxWindT3F0 = ForecastAggregator.max(timeSeries3, 0, WIND_SPEED);
        State minWindT3F0 = ForecastAggregator.min(timeSeries3, 0, WIND_SPEED);
        State totalPrecipT3F0 = ForecastAggregator.total(timeSeries3, 0, PRECIPITATION_MEAN);
        State noonPressureT3F0 = ForecastAggregator.noonOrFirst(timeSeries3, 0, PRESSURE);

        assertInstanceOf(QuantityType.class, maxTempT3F0);
        assertEquals(new QuantityType<>(maxBetween(json3, TEMPERATURE, BASE_TIME, DAY_0_END), SIUnits.CELSIUS),
                maxTempT3F0.as(QuantityType.class));
        assertInstanceOf(QuantityType.class, minTempT3F0);
        assertEquals(new QuantityType<>(minBetween(json3, TEMPERATURE, BASE_TIME, DAY_0_END), SIUnits.CELSIUS),
                minTempT3F0.as(QuantityType.class));
        assertInstanceOf(QuantityType.class, maxWindT3F0);
        assertEquals(new QuantityType<>(maxBetween(json3, WIND_SPEED, BASE_TIME, DAY_0_END), Units.METRE_PER_SECOND),
                maxWindT3F0.as(QuantityType.class));
        assertInstanceOf(QuantityType.class, minWindT3F0);
        assertEquals(new QuantityType<>(minBetween(json3, WIND_SPEED, BASE_TIME, DAY_0_END), Units.METRE_PER_SECOND),
                minWindT3F0.as(QuantityType.class));
        assertInstanceOf(QuantityType.class, totalPrecipT3F0);
        assertEquals(new QuantityType<>(sumBetween(json3, PRECIPITATION_MEAN, BASE_TIME, DAY_0_END),
                MetricPrefix.MILLI(SIUnits.METRE)), totalPrecipT3F0.as(QuantityType.class));
        assertInstanceOf(QuantityType.class, noonPressureT3F0);
        assertEquals(new QuantityType<>(noonOrFirst(json3, PRESSURE, BASE_TIME), MetricPrefix.HECTO(SIUnits.PASCAL)),
                noonPressureT3F0.as(QuantityType.class));

        State maxTempT3F9 = ForecastAggregator.max(timeSeries3, 9, TEMPERATURE);
        State minTempT3F9 = ForecastAggregator.min(timeSeries3, 9, TEMPERATURE);
        State maxWindT3F9 = ForecastAggregator.max(timeSeries3, 9, WIND_SPEED);
        State minWindT3F9 = ForecastAggregator.min(timeSeries3, 9, WIND_SPEED);
        State totalPrecipT3F9 = ForecastAggregator.total(timeSeries3, 9, PRECIPITATION_MEAN);
        State noonPressureT3F9 = ForecastAggregator.noonOrFirst(timeSeries3, 9, PRESSURE);

        assertInstanceOf(QuantityType.class, maxTempT3F9);
        assertEquals(new QuantityType<>(maxBetween(json3, TEMPERATURE, DAY_8_END, DAY_9_END), SIUnits.CELSIUS),
                maxTempT3F9.as(QuantityType.class));
        assertInstanceOf(QuantityType.class, minTempT3F9);
        assertEquals(new QuantityType<>(minBetween(json3, TEMPERATURE, DAY_8_END, DAY_9_END), SIUnits.CELSIUS),
                minTempT3F9.as(QuantityType.class));
        assertInstanceOf(QuantityType.class, maxWindT3F9);
        assertEquals(new QuantityType<>(maxBetween(json3, WIND_SPEED, DAY_8_END, DAY_9_END), Units.METRE_PER_SECOND),
                maxWindT3F9.as(QuantityType.class));
        assertInstanceOf(QuantityType.class, minWindT3F9);
        assertEquals(new QuantityType<>(minBetween(json3, WIND_SPEED, DAY_8_END, DAY_9_END), Units.METRE_PER_SECOND),
                minWindT3F9.as(QuantityType.class));
        assertInstanceOf(QuantityType.class, totalPrecipT3F9);
        assertEquals(new QuantityType<>(sumBetween(json3, PRECIPITATION_MEAN, DAY_8_END, DAY_9_END),
                MetricPrefix.MILLI(SIUnits.METRE)), totalPrecipT3F9.as(QuantityType.class));
        assertInstanceOf(QuantityType.class, noonPressureT3F9);
        assertEquals(new QuantityType<>(noonOrFirst(json3, PRESSURE, DAY_8_END), MetricPrefix.HECTO(SIUnits.PASCAL)),
                noonPressureT3F9.as(QuantityType.class));
    }

    @Test
    public void backwardsCompParameterTest() {
        assertNotNull(timeSeries1);
        Forecast forecast0 = timeSeries1.getForecast(BASE_TIME, 0).orElseThrow(AssertionError::new);

        State msl0 = forecast0.getParameterAsState(PMP3G_PRESSURE);
        State t0 = forecast0.getParameterAsState(PMP3G_TEMPERATURE);
        State vis0 = forecast0.getParameterAsState(PMP3G_VISIBILITY);
        State wd0 = forecast0.getParameterAsState(PMP3G_WIND_DIRECTION);
        State ws0 = forecast0.getParameterAsState(PMP3G_WIND_SPEED);
        State r0 = forecast0.getParameterAsState(PMP3G_RELATIVE_HUMIDITY);
        State tstm0 = forecast0.getParameterAsState(PMP3G_THUNDER_PROBABILITY);
        State tcc0 = forecast0.getParameterAsState(PMP3G_TOTAL_CLOUD_COVER);
        State lcc0 = forecast0.getParameterAsState(PMP3G_LOW_CLOUD_COVER);
        State mcc0 = forecast0.getParameterAsState(PMP3G_MEDIUM_CLOUD_COVER);
        State hcc0 = forecast0.getParameterAsState(PMP3G_HIGH_CLOUD_COVER);
        State gust0 = forecast0.getParameterAsState(PMP3G_GUST);
        State pmin0 = forecast0.getParameterAsState(PMP3G_PRECIPITATION_MIN);
        State pmax0 = forecast0.getParameterAsState(PMP3G_PRECIPITATION_MAX);
        State spp0 = forecast0.getParameterAsState(PMP3G_PERCENT_FROZEN);
        State pcat0 = forecast0.getParameterAsState(PMP3G_PRECIPITATION_CATEGORY);
        State pmean0 = forecast0.getParameterAsState(PMP3G_PRECIPITATION_MEAN);
        State pmedian0 = forecast0.getParameterAsState(PMP3G_PRECIPITATION_MEDIAN);
        State wsymb0 = forecast0.getParameterAsState(PMP3G_WEATHER_SYMBOL);

        assertInstanceOf(QuantityType.class, msl0);
        assertEquals(MetricPrefix.HECTO(SIUnits.PASCAL), ((QuantityType<?>) msl0).getUnit());
        assertEquals(new QuantityType<>(1010.9, MetricPrefix.HECTO(SIUnits.PASCAL)), msl0);
        assertInstanceOf(QuantityType.class, t0);
        assertEquals(SIUnits.CELSIUS, ((QuantityType<?>) t0).getUnit());
        assertEquals(new QuantityType<>(7.5, SIUnits.CELSIUS), t0);
        assertInstanceOf(QuantityType.class, vis0);
        assertEquals(MetricPrefix.KILO(SIUnits.METRE), ((QuantityType<?>) vis0).getUnit());
        assertEquals(new QuantityType<>(13.3, MetricPrefix.KILO(SIUnits.METRE)), vis0);
        assertInstanceOf(QuantityType.class, wd0);
        assertEquals(Units.DEGREE_ANGLE, ((QuantityType<?>) wd0).getUnit());
        assertEquals(new QuantityType<>(219, Units.DEGREE_ANGLE), wd0);
        assertInstanceOf(QuantityType.class, ws0);
        assertEquals(Units.METRE_PER_SECOND, ((QuantityType<?>) ws0).getUnit());
        assertEquals(new QuantityType<>(1.5, Units.METRE_PER_SECOND), ws0);
        assertInstanceOf(QuantityType.class, r0);
        assertEquals(Units.PERCENT, ((QuantityType<?>) r0).getUnit());
        assertEquals(new QuantityType<>(86, Units.PERCENT), r0);
        assertInstanceOf(QuantityType.class, tstm0);
        assertEquals(Units.PERCENT, ((QuantityType<?>) tstm0).getUnit());
        assertEquals(new QuantityType<>(0, Units.PERCENT), tstm0);
        assertInstanceOf(QuantityType.class, tcc0);
        assertEquals(Units.PERCENT, ((QuantityType<?>) tcc0).getUnit());
        assertEquals(new QuantityType<>(50, Units.PERCENT), tcc0);
        assertInstanceOf(QuantityType.class, lcc0);
        assertEquals(Units.PERCENT, ((QuantityType<?>) lcc0).getUnit());
        assertEquals(new QuantityType<>(37.5, Units.PERCENT), lcc0);
        assertInstanceOf(QuantityType.class, mcc0);
        assertEquals(Units.PERCENT, ((QuantityType<?>) mcc0).getUnit());
        assertEquals(new QuantityType<>(12.5, Units.PERCENT), mcc0);
        assertInstanceOf(QuantityType.class, hcc0);
        assertEquals(Units.PERCENT, ((QuantityType<?>) hcc0).getUnit());
        assertEquals(new QuantityType<>(0, Units.PERCENT), hcc0);
        assertInstanceOf(QuantityType.class, gust0);
        assertEquals(Units.METRE_PER_SECOND, ((QuantityType<?>) gust0).getUnit());
        assertEquals(new QuantityType<>(4.8, Units.METRE_PER_SECOND), gust0);
        assertInstanceOf(QuantityType.class, pmin0);
        assertEquals(Units.MILLIMETRE_PER_HOUR, ((QuantityType<?>) pmin0).getUnit());
        assertEquals(new QuantityType<>(0, Units.MILLIMETRE_PER_HOUR), pmin0);
        assertInstanceOf(QuantityType.class, pmax0);
        assertEquals(Units.MILLIMETRE_PER_HOUR, ((QuantityType<?>) pmax0).getUnit());
        assertEquals(new QuantityType<>(0, Units.MILLIMETRE_PER_HOUR), pmax0);
        assertInstanceOf(QuantityType.class, pmean0);
        assertEquals(Units.MILLIMETRE_PER_HOUR, ((QuantityType<?>) pmean0).getUnit());
        assertEquals(new QuantityType<>(0, Units.MILLIMETRE_PER_HOUR), pmean0);
        assertInstanceOf(QuantityType.class, pmedian0);
        assertEquals(Units.MILLIMETRE_PER_HOUR, ((QuantityType<?>) pmedian0).getUnit());
        assertEquals(new QuantityType<>(0, Units.MILLIMETRE_PER_HOUR), pmedian0);
        assertInstanceOf(QuantityType.class, spp0);
        assertEquals(new QuantityType<>(-1, Units.PERCENT), spp0);
        assertInstanceOf(DecimalType.class, pcat0);
        assertEquals(new DecimalType(0), pcat0);
        assertInstanceOf(DecimalType.class, wsymb0);
        assertEquals(new DecimalType(3), wsymb0);
    }
}
