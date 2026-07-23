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

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
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
              "Moisture": 1.0,
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
        Shelly2NotifyBluEventData data = Objects
                .requireNonNull(GSON.fromJson(WS90_JSON, Shelly2NotifyBluEventData.class));

        assertThat("Rain", data.rain, is(equalTo(1.0)));
        assertThat("speeds", data.speeds, is(not(nullValue())));
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
        assertThat("temperatures", data.temperatures, is(not(nullValue())));
        assertThat("temperatures[0]", Objects.requireNonNull(data.temperatures)[0], is(equalTo(21.5)));
    }

    @Test
    void ws90AtmosphericPacketDeserializesWithoutWindFields() {
        String json = """
                {"Temperature": [18.0], "Humidity": 72.0, "Pressure": 1008.5, "Dewpoint": 13.2,
                 "Battery": 85, "addr": "aa:bb:cc:dd:ee:ff"}
                """;
        Shelly2NotifyBluEventData data = Objects.requireNonNull(GSON.fromJson(json, Shelly2NotifyBluEventData.class));

        assertThat("temperatures", data.temperatures, is(not(nullValue())));
        assertThat("temperatures[0]", Objects.requireNonNull(data.temperatures)[0], is(equalTo(18.0)));
        assertThat("humidity", data.humidity, is(equalTo(72.0)));
        assertThat("pressure", data.pressure, is(equalTo(1008.5)));
        assertThat("dewPoint", data.dewPoint, is(equalTo(13.2)));
        assertThat("speeds should be null for atmospheric-only packet", data.speeds, is(nullValue()));
        assertThat("direction should be null for atmospheric-only packet", data.direction, is(nullValue()));
        assertThat("uvIndex should be null for atmospheric-only packet", data.uvIndex, is(nullValue()));
        assertThat("rain should be null when Moisture key absent", data.rain, is(nullValue()));
    }

    @Test
    void ws90WindPacketDeserializesWithoutAtmosphericFields() {
        String json = """
                {"Moisture": 0.0, "Speed": [4.2, 8.1], "Direction": 135.0, "UVIndex": 3.7,
                 "Precipitation": 0.5, "addr": "aa:bb:cc:dd:ee:ff"}
                """;
        Shelly2NotifyBluEventData data = Objects.requireNonNull(GSON.fromJson(json, Shelly2NotifyBluEventData.class));

        assertThat("rain", data.rain, is(equalTo(0.0)));
        assertThat("speeds", data.speeds, is(not(nullValue())));
        assertThat("windSpeed", Objects.requireNonNull(data.speeds)[0], is(equalTo(4.2)));
        assertThat("gustSpeed", data.speeds[1], is(equalTo(8.1)));
        assertThat("direction", data.direction, is(equalTo(135.0)));
        assertThat("uvIndex", data.uvIndex, is(equalTo(3.7)));
        assertThat("precipitation", data.precipitation, is(equalTo(0.5)));
        assertThat("temperatures should be null for wind-only packet", data.temperatures, is(nullValue()));
        assertThat("humidity should be null for wind-only packet", data.humidity, is(nullValue()));
        assertThat("pressure should be null for wind-only packet", data.pressure, is(nullValue()));
    }

    @Test
    void ws90DtoWithNoRainDeserializesRainAsNull() {
        String json = """
                {"Speed": [2.0, 4.0], "Direction": 180.0, "UVIndex": 1.2, "addr": "aa:bb:cc:dd:ee:ff"}
                """;
        Shelly2NotifyBluEventData data = Objects.requireNonNull(GSON.fromJson(json, Shelly2NotifyBluEventData.class));

        assertThat("rain should be null when not present in JSON", data.rain, is(nullValue()));
        assertThat("speeds", data.speeds, is(not(nullValue())));
        assertThat("windSpeed", data.speeds[0], is(equalTo(2.0)));
    }
}
