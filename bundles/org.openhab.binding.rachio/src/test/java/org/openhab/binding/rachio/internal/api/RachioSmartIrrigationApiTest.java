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
package org.openhab.binding.rachio.internal.api;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.rachio.internal.api.json.RachioSmartIrrigationGsonDTO.RachioCurrentScheduleResponse;
import org.openhab.binding.rachio.internal.api.json.RachioSmartIrrigationGsonDTO.RachioDeviceEvent;
import org.openhab.binding.rachio.internal.api.json.RachioSmartIrrigationGsonDTO.RachioDeviceEventListResponse;
import org.openhab.binding.rachio.internal.api.json.RachioSmartIrrigationGsonDTO.RachioForecastEntry;
import org.openhab.binding.rachio.internal.api.json.RachioSmartIrrigationGsonDTO.RachioForecastResponse;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Tests Smart Irrigation Controller API payload and DTO helpers.
 *
 * @author openHAB Contributors - Initial contribution
 */
@NonNullByDefault
class RachioSmartIrrigationApiTest {
    @Test
    void moistureLevelPayloadContainsDocumentedFields() {
        JsonObject json = JsonParser.parseString(RachioApi.buildMoistureLevelPayload("zone-id", 12.5))
                .getAsJsonObject();

        assertThat(json.get("id").getAsString(), is("zone-id"));
        assertThat(json.get("level").getAsDouble(), is(12.5));
    }

    @Test
    void moisturePercentPayloadContainsDocumentedFields() {
        JsonObject json = JsonParser.parseString(RachioApi.buildMoisturePercentPayload("zone-id", 0.42))
                .getAsJsonObject();

        assertThat(json.get("id").getAsString(), is("zone-id"));
        assertThat(json.get("percent").getAsDouble(), is(0.42));
    }

    @Test
    void scheduleCommandPayloadContainsId() {
        JsonObject json = JsonParser.parseString(RachioApi.buildScheduleRuleCommandPayload("schedule-id"))
                .getAsJsonObject();

        assertThat(json.get("id").getAsString(), is("schedule-id"));
    }

    @Test
    void seasonalAdjustmentPayloadContainsAdjustment() {
        JsonObject json = JsonParser.parseString(RachioApi.buildSeasonalAdjustmentPayload("schedule-id", 0.75))
                .getAsJsonObject();

        assertThat(json.get("id").getAsString(), is("schedule-id"));
        assertThat(json.get("adjustment").getAsDouble(), is(0.75));
    }

    @Test
    void currentScheduleDtoReadsNestedScheduleData() {
        String json = """
                {
                  "running": true,
                  "currentSchedule": {
                    "scheduleId": "schedule-id",
                    "scheduleName": "Morning Water",
                    "scheduleType": "FIXED",
                    "startTime": "2026-05-17T05:00:00Z",
                    "endTime": "2026-05-17T05:30:00Z",
                    "duration": 1800
                  }
                }
                """;

        RachioCurrentScheduleResponse response = Objects
                .requireNonNull(new Gson().fromJson(json, RachioCurrentScheduleResponse.class));

        assertThat(response.isRunning(), is(true));
        assertThat(response.getScheduleId(), is("schedule-id"));
        assertThat(response.getScheduleName(), is("Morning Water"));
        assertThat(response.getDurationSeconds(), is(1800));
    }

    @Test
    void deviceEventDtoReadsLatestEventFromArrayWrapper() {
        String json = """
                {
                  "events": [
                    {"eventType":"OLDER_EVENT","summary":"Old","createDate":1000},
                    {"eventType":"NEWER_EVENT","summary":"New","createDate":2000}
                  ]
                }
                """;

        RachioDeviceEventListResponse response = RachioDeviceEventListResponse.fromJson(json);

        assertThat(response.events.size(), is(2));
        RachioDeviceEvent latestEvent = Objects.requireNonNull(response.getLatestEvent());
        assertThat(latestEvent.getEventType(), is("NEWER_EVENT"));
        assertThat(latestEvent.getSummary(), is("New"));
    }

    @Test
    void forecastDtoReadsFirstDailyForecast() {
        String json = """
                {
                  "summary": "Cloudy",
                  "updated": "2026-05-17T03:00:00Z",
                  "dailyForecasts": [
                    {
                      "highTemperature": 24.5,
                      "lowTemperature": 12.1,
                      "precipitation": 4.2,
                      "precipitationProbability": 0.7,
                      "windSpeed": 10.5
                    }
                  ]
                }
                """;

        RachioForecastResponse response = Objects
                .requireNonNull(new Gson().fromJson(json, RachioForecastResponse.class));
        RachioForecastEntry todayForecast = Objects.requireNonNull(response.getTodayForecast());

        assertThat(response.getSummary(), is("Cloudy"));
        assertThat(response.getUpdated(), is("2026-05-17T03:00:00Z"));
        assertThat(todayForecast.getHighTemperature(), is(24.5));
        assertThat(todayForecast.getWind(), is(10.5));
    }
}
