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
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Field;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.openhab.binding.rachio.internal.api.json.RachioSmartIrrigationGsonDTO.RachioCurrentScheduleResponse;
import org.openhab.binding.rachio.internal.api.json.RachioSmartIrrigationGsonDTO.RachioDeviceEvent;
import org.openhab.binding.rachio.internal.api.json.RachioSmartIrrigationGsonDTO.RachioDeviceEventListResponse;
import org.openhab.binding.rachio.internal.api.json.RachioSmartIrrigationGsonDTO.RachioForecastEntry;
import org.openhab.binding.rachio.internal.api.json.RachioSmartIrrigationGsonDTO.RachioForecastResponse;
import org.openhab.binding.rachio.internal.utils.ClientRateLimitManager;
import org.openhab.binding.rachio.internal.utils.ClientRateLimitManager.PRIORITY;
import org.openhab.binding.rachio.internal.utils.ClientRateLimitManager.RequestPurpose;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Tests Smart Irrigation Controller API payload and DTO helpers.
 *
 * @author openHAB Contributors - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings({ "null" })
class RachioSmartIrrigationApiTest {
    @Test
    void currentScheduleRemainsEssentialWhenOptionalForecastIsLocallyThrottled() throws Exception {
        RachioApi api = new RachioApi("person-id");
        RecordingRachioHttp http = new RecordingRachioHttp();
        LowPriorityThrottlingRateLimitManager rateLimitManager = new LowPriorityThrottlingRateLimitManager();
        setField(api, "httpApi", http);
        setField(api, "rateLimitManager", rateLimitManager);

        RachioCurrentScheduleResponse currentSchedule = api.getCurrentSchedule("device-id",
                RequestPurpose.CORE_STATUS_POLL);
        assertThrows(RachioApiThrottledException.class, () -> api.getDeviceForecast("device-id", "US"));

        assertThat(currentSchedule.isRunning(), is(true));
        assertThat(rateLimitManager.priorities, contains(PRIORITY.MED, PRIORITY.LOW));
        assertThat(rateLimitManager.requestPurposes,
                contains(RequestPurpose.CORE_STATUS_POLL, RequestPurpose.BACKGROUND_REFRESH));
        assertThat(http.getUrls.size(), is(1));
    }

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

    @Test
    void forecastDtoReadsNestedWeatherDailyForecast() {
        String json = """
                {
                  "updatedAt": "2026-05-17T03:00:00Z",
                  "weather": {
                    "daily": [
                      {
                        "conditions": "Partly cloudy",
                        "temperatureHigh": 26.0,
                        "temperatureLow": 14.0,
                        "precip": 2.5,
                        "precipProbability": 0.4,
                        "windSpeed": 7.5
                      }
                    ]
                  }
                }
                """;

        RachioForecastResponse response = RachioForecastResponse.fromJson(json);
        RachioForecastEntry todayForecast = Objects.requireNonNull(response.getTodayForecast());

        assertThat(response.hasUsefulData(), is(true));
        assertThat(response.getSummary(), is("Partly cloudy"));
        assertThat(response.getUpdated(), is("2026-05-17T03:00:00Z"));
        assertThat(todayForecast.getHighTemperature(), is(26.0));
        assertThat(todayForecast.getLowTemperature(), is(14.0));
        assertThat(todayForecast.precipitation, is(2.5));
        assertThat(todayForecast.precipitationProbability, is(0.4));
        assertThat(todayForecast.getWind(), is(7.5));
        assertThat(response.shapeSummary().contains("topLevelKeys=updatedAt,weather"), is(true));
        assertThat(response.shapeSummary().contains("selectedEntryKeys=conditions,temperatureHigh"), is(true));
        assertThat(response.shapeSummary().contains("matchedAliases=updatedAt,conditions"), is(true));
    }

    @Test
    void forecastDtoMapsRuntimeDailyAliases() {
        RachioForecastResponse response = RachioForecastResponse.fromJson("""
                {
                  "weather": {
                    "daily": [
                      {
                        "localizedTimeStamp": "2026-06-20T08:00:00Z",
                        "calculatedPrecip": 3.4,
                        "precipIntensity": 0.2,
                        "precipProbability": 0.6,
                        "temperatureMin": 15.0,
                        "temperatureMax": 27.0,
                        "weatherSummary": "Scattered showers"
                      }
                    ]
                  }
                }
                """);
        RachioForecastEntry todayForecast = Objects.requireNonNull(response.getTodayForecast());

        assertThat(response.getSummary(), is("Scattered showers"));
        assertThat(response.getUpdated(), is("2026-06-20T08:00:00Z"));
        assertThat(todayForecast.precipitation, is(3.4));
        assertThat(response.parsedFieldSummary().contains("summary=true"), is(true));
        assertThat(response.parsedFieldSummary().contains("precipitation=true"), is(true));
        assertThat(response.parsedFieldSummary().contains("updated=true"), is(true));
        assertThat(response.shapeSummary().contains(
                "matchedAliases=weatherSummary,localizedTimeStamp,temperatureMax,temperatureMin,calculatedPrecip,precipIntensity,precipProbability"),
                is(true));
    }

    @Test
    void forecastDtoPreservesAliasPrecedenceAndUsesTimeAsTimestampFallback() {
        RachioForecastResponse response = RachioForecastResponse.fromJson("""
                {
                  "today": {
                    "summary": "Preferred summary",
                    "weatherSummary": "Secondary summary",
                    "time": 1781942400,
                    "precipitationAmount": 1.2,
                    "calculatedPrecip": 3.4
                  }
                }
                """);
        RachioForecastEntry todayForecast = Objects.requireNonNull(response.getTodayForecast());

        assertThat(response.getSummary(), is("Preferred summary"));
        assertThat(response.getUpdated(), is("1781942400"));
        assertThat(todayForecast.precipitation, is(1.2));
        assertThat(response.shapeSummary().contains("matchedAliases=summary,time,precipitationAmount"), is(true));
    }

    @Test
    void forecastDtoKeepsPrecipitationIntensityDiagnosticOnly() {
        RachioForecastResponse response = RachioForecastResponse.fromJson("""
                {
                  "today": {
                    "temperatureMax": 27.0,
                    "precipIntensity": 0.2
                  }
                }
                """);
        RachioForecastEntry todayForecast = Objects.requireNonNull(response.getTodayForecast());

        assertThat(Double.isNaN(todayForecast.precipitation), is(true));
        assertThat(response.parsedFieldSummary().contains("precipitation=false"), is(true));
        assertThat(response.shapeSummary().contains("matchedAliases=temperatureMax,precipIntensity"), is(true));
    }

    @Test
    void forecastDtoTreatsTimestampOnlyResponseAsNoUsefulData() {
        RachioForecastResponse response = RachioForecastResponse.fromJson("""
                {
                  "updatedAt": "2026-06-20T08:05:00Z",
                  "weather": {
                    "daily": [
                      {"localizedTimeStamp": "2026-06-20T08:00:00Z"}
                    ]
                  }
                }
                """);

        assertThat(response.getUpdated(), is("2026-06-20T08:05:00Z"));
        assertThat(response.hasUsefulData(), is(false));
        assertThat(response.getTodayForecast() == null, is(true));
    }

    @Test
    void forecastDtoDoesNotTreatPrecipitationIntensityAloneAsUseful() {
        RachioForecastResponse response = RachioForecastResponse.fromJson("""
                {
                  "today": {
                    "precipIntensity": 0.2
                  }
                }
                """);

        assertThat(response.hasUsefulData(), is(false));
        assertThat(response.getTodayForecast() == null, is(true));
        assertThat(response.parsedFieldSummary().contains("precipitation=false"), is(true));
    }

    @Test
    void forecastDtoTreatsEmptyObjectAsNoUsefulData() {
        RachioForecastResponse response = RachioForecastResponse.fromJson("""
                {
                  "forecast": {
                    "daily": []
                  }
                }
                """);

        assertThat(response.hasUsefulData(), is(false));
    }

    private static void setField(Object target, String fieldName, Object value) throws ReflectiveOperationException {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static class LowPriorityThrottlingRateLimitManager extends ClientRateLimitManager {
        private final List<PRIORITY> priorities = new ArrayList<>();
        private final List<RequestPurpose> requestPurposes = new ArrayList<>();

        LowPriorityThrottlingRateLimitManager() {
            super(10, Duration.ofSeconds(30));
        }

        @Override
        public void tryThrottle(PRIORITY priority, ClientRateLimitManager.RequestPurpose requestPurpose)
                throws RateLimitThrottleException {
            priorities.add(priority);
            requestPurposes.add(requestPurpose);
            if (priority == PRIORITY.LOW) {
                throw new RateLimitThrottleException(priority, requestPurpose, 0.1, 0.2);
            }
        }
    }

    private static class RecordingRachioHttp extends RachioHttp {
        private final List<String> getUrls = new ArrayList<>();

        @Override
        public RachioApiResult httpGet(String url, @Nullable String urlParameters) {
            getUrls.add(url);
            RachioApiResult result = new RachioApiResult();
            result.url = url;
            result.requestMethod = "GET";
            result.responseCode = 200;
            result.resultString = """
                    {
                      "running": true,
                      "scheduleId": "schedule-id"
                    }
                    """;
            return result;
        }
    }
}
