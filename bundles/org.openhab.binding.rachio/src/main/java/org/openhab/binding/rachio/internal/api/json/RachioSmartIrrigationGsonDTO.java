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
package org.openhab.binding.rachio.internal.api.json;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * DTOs for Smart Irrigation Controller endpoints whose response schemas may contain undocumented fields.
 *
 * @author openHAB Contributors - Initial contribution
 */
@NonNullByDefault
public class RachioSmartIrrigationGsonDTO {
    private static final Gson GSON = new Gson();

    public static class RachioCurrentScheduleResponse {
        public String id = "";
        public String scheduleId = "";
        public String routingId = "";
        public String name = "";
        public String scheduleName = "";
        public String type = "";
        public String scheduleType = "";
        public String status = "";
        public String startTime = "";
        public String endTime = "";
        public String summary = "";
        public int duration = 0;
        public int durationInMinutes = 0;
        public boolean running = false;
        @Nullable
        public JsonObject schedule;
        @Nullable
        public JsonObject currentSchedule;

        public String getScheduleId() {
            return firstNonBlank(id, scheduleId, routingId, readString(schedule, "id"),
                    readString(schedule, "scheduleId"), readString(currentSchedule, "id"),
                    readString(currentSchedule, "scheduleId"));
        }

        public String getScheduleName() {
            return firstNonBlank(name, scheduleName, readString(schedule, "name"), readString(schedule, "scheduleName"),
                    readString(currentSchedule, "name"), readString(currentSchedule, "scheduleName"));
        }

        public String getScheduleType() {
            return firstNonBlank(type, scheduleType, readString(schedule, "type"), readString(schedule, "scheduleType"),
                    readString(currentSchedule, "type"), readString(currentSchedule, "scheduleType"));
        }

        public String getStartTime() {
            return firstNonBlank(startTime, readString(schedule, "startTime"),
                    readString(currentSchedule, "startTime"));
        }

        public String getEndTime() {
            return firstNonBlank(endTime, readString(schedule, "endTime"), readString(currentSchedule, "endTime"));
        }

        public int getDurationSeconds() {
            int nestedDuration = firstPositive(readInt(schedule, "duration"), readInt(currentSchedule, "duration"));
            if (duration > 0) {
                return duration;
            }
            if (nestedDuration > 0) {
                return nestedDuration;
            }
            int minutes = durationInMinutes > 0 ? durationInMinutes
                    : firstPositive(readInt(schedule, "durationInMinutes"),
                            readInt(currentSchedule, "durationInMinutes"));
            return minutes > 0 ? minutes * 60 : 0;
        }

        public boolean isRunning() {
            if (running) {
                return true;
            }
            String currentStatus = firstNonBlank(status, readString(schedule, "status"),
                    readString(currentSchedule, "status"));
            return currentStatus.equalsIgnoreCase("RUNNING") || currentStatus.equalsIgnoreCase("STARTED")
                    || !getScheduleId().isBlank() || !getScheduleName().isBlank();
        }
    }

    public static class RachioDeviceEventListResponse {
        public ArrayList<RachioDeviceEvent> events = new ArrayList<>();

        public static RachioDeviceEventListResponse fromJson(String json) {
            RachioDeviceEventListResponse response = new RachioDeviceEventListResponse();
            JsonElement root = JsonParser.parseString(json);
            if (root.isJsonArray()) {
                response.events.addAll(parseEvents(root.getAsJsonArray()));
            } else if (root.isJsonObject()) {
                JsonObject object = root.getAsJsonObject();
                for (String arrayName : List.of("events", "items", "data", "results")) {
                    JsonElement arrayElement = object.get(arrayName);
                    if (arrayElement != null && arrayElement.isJsonArray()) {
                        response.events.addAll(parseEvents(arrayElement.getAsJsonArray()));
                    }
                }
                if (response.events.isEmpty()) {
                    @Nullable
                    RachioDeviceEvent event = GSON.fromJson(object, RachioDeviceEvent.class);
                    if (event != null) {
                        response.events.add(event);
                    }
                }
            }
            return response;
        }

        public @Nullable RachioDeviceEvent getLatestEvent() {
            RachioDeviceEvent latest = null;
            long latestTime = Long.MIN_VALUE;
            for (RachioDeviceEvent event : events) {
                long eventTime = event.getSortTimestamp();
                if (latest == null || eventTime >= latestTime) {
                    latest = event;
                    latestTime = eventTime;
                }
            }
            return latest;
        }

        private static List<RachioDeviceEvent> parseEvents(JsonArray array) {
            List<RachioDeviceEvent> events = new ArrayList<>();
            for (JsonElement element : array) {
                if (element.isJsonObject()) {
                    @Nullable
                    RachioDeviceEvent event = GSON.fromJson(element, RachioDeviceEvent.class);
                    if (event != null) {
                        events.add(event);
                    }
                }
            }
            return events;
        }
    }

    public static class RachioDeviceEvent {
        public String id = "";
        public String type = "";
        public String subType = "";
        public String eventType = "";
        public String summary = "";
        public String description = "";
        public String title = "";
        public String timestamp = "";
        public String startTime = "";
        public long createDate = -1;
        public long eventDate = -1;

        public String getEventType() {
            return firstNonBlank(subType, eventType, type);
        }

        public String getEventTime() {
            return firstNonBlank(timestamp, startTime, createDate > 0 ? Long.toString(createDate) : "",
                    eventDate > 0 ? Long.toString(eventDate) : "");
        }

        public String getSummary() {
            return firstNonBlank(summary, description, title, getEventType());
        }

        private long getSortTimestamp() {
            if (eventDate > 0) {
                return eventDate;
            }
            if (createDate > 0) {
                return createDate;
            }
            return -1;
        }
    }

    public static class RachioForecastResponse {
        public String summary = "";
        public String updated = "";
        public String updatedAt = "";
        public ArrayList<RachioForecastEntry> forecast = new ArrayList<>();
        public ArrayList<RachioForecastEntry> forecasts = new ArrayList<>();
        public ArrayList<RachioForecastEntry> dailyForecasts = new ArrayList<>();
        @Nullable
        public RachioForecastEntry today;

        public String getSummary() {
            RachioForecastEntry todayForecast = getTodayForecast();
            return firstNonBlank(summary, todayForecast != null ? todayForecast.summary : "");
        }

        public String getUpdated() {
            return firstNonBlank(updated, updatedAt);
        }

        public @Nullable RachioForecastEntry getTodayForecast() {
            if (today != null) {
                return today;
            }
            if (!forecast.isEmpty()) {
                return forecast.get(0);
            }
            if (!forecasts.isEmpty()) {
                return forecasts.get(0);
            }
            if (!dailyForecasts.isEmpty()) {
                return dailyForecasts.get(0);
            }
            return null;
        }
    }

    public static class RachioForecastEntry {
        public String summary = "";
        public String date = "";
        public String time = "";
        public double highTemperature = Double.NaN;
        public double lowTemperature = Double.NaN;
        public double high = Double.NaN;
        public double low = Double.NaN;
        public double precipitation = Double.NaN;
        public double precipitationProbability = Double.NaN;
        public double wind = Double.NaN;
        public double windSpeed = Double.NaN;

        public double getHighTemperature() {
            return Double.isNaN(highTemperature) ? high : highTemperature;
        }

        public double getLowTemperature() {
            return Double.isNaN(lowTemperature) ? low : lowTemperature;
        }

        public double getWind() {
            return Double.isNaN(wind) ? windSpeed : wind;
        }
    }

    public static class RachioScheduleRuleResponse {
        public String id = "";
        public String name = "";
        public boolean enabled = false;
        public String type = "";
        public String startDate = "";
        public String startTime = "";
        public String lastRun = "";
        public String lastRunDate = "";
        public String lastRunTime = "";
        public String lastRunAt = "";
        public String nextRun = "";
        public String nextRunDate = "";
        public String nextRunTime = "";
        public String nextRunAt = "";
        public String nextScheduledRun = "";
        public String nextScheduledStart = "";
        public double seasonalAdjustment = 0;
        public ArrayList<RachioScheduleRuleZone> zones = new ArrayList<>();

        public String getZoneSummary() {
            List<String> zoneIds = new ArrayList<>();
            for (RachioScheduleRuleZone zone : zones) {
                zoneIds.add(zone.zoneId);
            }
            return String.join(",", zoneIds);
        }
    }

    public static class RachioFlexScheduleRuleResponse extends RachioScheduleRuleResponse {
    }

    public static class RachioScheduleRuleZone {
        public String zoneId = "";
        public int duration = 0;
        public int sortOrder = 0;
    }

    public static class RachioMoistureLevelRequest {
        public final String id;
        public final double level;

        public RachioMoistureLevelRequest(String id, double level) {
            this.id = id;
            this.level = level;
        }
    }

    public static class RachioMoisturePercentRequest {
        public final String id;
        public final double percent;

        public RachioMoisturePercentRequest(String id, double percent) {
            this.id = id;
            this.percent = percent;
        }
    }

    public static class RachioScheduleRuleCommandRequest {
        public final String id;

        public RachioScheduleRuleCommandRequest(String id) {
            this.id = id;
        }
    }

    public static class RachioSeasonalAdjustmentRequest {
        public final String id;
        public final double adjustment;

        public RachioSeasonalAdjustmentRequest(String id, double adjustment) {
            this.id = id;
            this.adjustment = adjustment;
        }
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (!value.isBlank()) {
                return value;
            }
        }
        return "";
    }

    private static int firstPositive(int... values) {
        for (int value : values) {
            if (value > 0) {
                return value;
            }
        }
        return 0;
    }

    private static String readString(@Nullable JsonObject object, String memberName) {
        if (object == null) {
            return "";
        }
        JsonElement element = object.get(memberName);
        return element != null && element.isJsonPrimitive() ? element.getAsString() : "";
    }

    private static int readInt(@Nullable JsonObject object, String memberName) {
        if (object == null) {
            return 0;
        }
        JsonElement element = object.get(memberName);
        try {
            return element != null && element.isJsonPrimitive() ? element.getAsInt() : 0;
        } catch (RuntimeException e) {
            return 0;
        }
    }
}
