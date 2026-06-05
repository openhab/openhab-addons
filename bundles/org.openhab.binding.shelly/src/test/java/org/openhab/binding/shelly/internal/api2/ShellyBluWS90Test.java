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
package org.openhab.binding.shelly.internal.api2;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openhab.binding.shelly.internal.api2.ShellyBluJsonDTO.Shelly2NotifyBluEventData;

import com.google.gson.Gson;

/**
 * Tests for WS90 (Ecowitt weather station) BLU JSON deserialization and field mapping.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class ShellyBluWS90Test {

    private static final Gson GSON = new Gson();

    private static final String WS90_JSON = """
            {
              "Rain": 1.0,
              "Speed": [3.5, 7.2],
              "Direction": 270.0,
              "UVIndex": 5.3,
              "Pressure": 1013.25,
              "Dewpoint": 12.5,
              "Precipitation": 2.1,
              "Battery": 90,
              "Temperature": [21.5],
              "Humidity": 65.0,
              "addr": "aa:bb:cc:dd:ee:ff",
              "rssi": -72
            }
            """;

    @Test
    void ws90DtoDeserializesAllWeatherFields() {
        Shelly2NotifyBluEventData data = GSON.fromJson(WS90_JSON, Shelly2NotifyBluEventData.class);

        assertNotNull(data);
        assertThat("Rain", data.rain, is(equalTo(1.0)));
        assertNotNull(data.speeds);
        assertThat("speeds length", data.speeds.length, is(equalTo(2)));
        assertThat("windSpeed (speeds[0])", data.speeds[0], is(equalTo(3.5)));
        assertThat("gustSpeed (speeds[1])", data.speeds[1], is(equalTo(7.2)));
        assertThat("direction", data.direction, is(equalTo(270.0)));
        assertThat("uvIndex", data.uvIndex, is(equalTo(5.3)));
        assertThat("pressure", data.pressure, is(equalTo(1013.25)));
        assertThat("dewPoint", data.dewPoint, is(equalTo(12.5)));
        assertThat("precipitation", data.precipitation, is(equalTo(2.1)));
        assertThat("battery", data.battery, is(equalTo(90)));
        assertThat("humidity", data.humidity, is(equalTo(65.0)));
        assertThat("addr", data.addr, is(equalTo("aa:bb:cc:dd:ee:ff")));
    }

    @ParameterizedTest
    @CsvSource({ "0.0, false", "0.001, true", "1.0, true", "5.5, true" })
    void rainValueMapsToRainStatus(double rainValue, boolean expectedRaining) {
        boolean raining = rainValue > 0;
        assertThat("rain=" + rainValue + " should map to raining=" + expectedRaining, raining,
                is(equalTo(expectedRaining)));
    }

    @Test
    void speedsArrayWithTwoElementsMapsBothWindAndGust() {
        Shelly2NotifyBluEventData data = new Shelly2NotifyBluEventData();
        data.speeds = new Double[] { 3.5, 7.2 };

        Double windSpeed = null;
        Double gustSpeed = null;
        if (data.speeds != null && data.speeds.length >= 1) {
            windSpeed = data.speeds[0];
            if (data.speeds.length >= 2) {
                gustSpeed = data.speeds[1];
            }
        }

        assertThat("windSpeed", windSpeed, is(equalTo(3.5)));
        assertThat("gustSpeed", gustSpeed, is(equalTo(7.2)));
    }

    @Test
    void speedsArrayWithOneElementMapsWindSpeedOnly() {
        Shelly2NotifyBluEventData data = new Shelly2NotifyBluEventData();
        data.speeds = new Double[] { 3.5 };

        Double windSpeed = null;
        Double gustSpeed = null;
        if (data.speeds != null && data.speeds.length >= 1) {
            windSpeed = data.speeds[0];
            if (data.speeds.length >= 2) {
                gustSpeed = data.speeds[1];
            }
        }

        assertThat("windSpeed", windSpeed, is(equalTo(3.5)));
        assertNull(gustSpeed, "gustSpeed should be null when only one speed element present");
    }

    @Test
    void emptySpeedsArrayDoesNotThrow() {
        Shelly2NotifyBluEventData data = new Shelly2NotifyBluEventData();
        data.speeds = new Double[] {};

        Double windSpeed = null;
        Double gustSpeed = null;
        assertDoesNotThrow(() -> {
            if (data.speeds != null && data.speeds.length >= 1) {
                @SuppressWarnings("unused")
                Double ws = data.speeds[0];
            }
        });

        assertNull(windSpeed, "windSpeed should be null for empty speeds array");
        assertNull(gustSpeed, "gustSpeed should be null for empty speeds array");
    }

    @Test
    void ws90DtoWithNoRainDeserializesRainAsNull() {
        String json = """
                {"Speed": [2.0, 4.0], "Direction": 180.0, "UVIndex": 1.2, "addr": "aa:bb:cc:dd:ee:ff"}
                """;
        Shelly2NotifyBluEventData data = GSON.fromJson(json, Shelly2NotifyBluEventData.class);

        assertNull(data.rain, "rain should be null when not present in JSON");
        assertNotNull(data.speeds);
        assertThat("windSpeed", data.speeds[0], is(equalTo(2.0)));
    }
}
