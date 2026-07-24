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

import java.math.BigDecimal;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        public @Nullable JsonObject schedule;
        public @Nullable JsonObject currentSchedule;

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
            return "RUNNING".equalsIgnoreCase(currentStatus) || "STARTED".equalsIgnoreCase(currentStatus)
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
        private final Logger logger = LoggerFactory.getLogger(RachioForecastResponse.class);

        public String summary = "";
        public String updated = "";
        public String updatedAt = "";
        public ArrayList<RachioForecastEntry> forecast = new ArrayList<>();
        public ArrayList<RachioForecastEntry> forecasts = new ArrayList<>();
        public ArrayList<RachioForecastEntry> dailyForecasts = new ArrayList<>();
        public @Nullable RachioForecastEntry today;
        public ArrayList<String> topLevelFieldNames = new ArrayList<>();
        public ArrayList<String> matchedAliases = new ArrayList<>();

        public static RachioForecastResponse fromJson(String json) {
            RachioForecastResponse response = new RachioForecastResponse();
            try {
                JsonElement root = JsonParser.parseString(json);
                if (root.isJsonArray()) {
                    addForecastEntries(response.dailyForecasts, root);
                } else if (root.isJsonObject()) {
                    parseForecastObject(response, root.getAsJsonObject());
                }
            } catch (RuntimeException e) {
                return response;
            }
            return response;
        }

        public String getSummary() {
            ZoneId zoneId = ZoneId.systemDefault();
            return getSummary(LocalDate.now(zoneId), zoneId);
        }

        public String getSummary(LocalDate currentDate, ZoneId zoneId) {
            ForecastSelection selection = selectTodayForecast(currentDate, zoneId);
            return getSummary(selection, currentDate, zoneId);
        }

        public String getUpdated() {
            return firstNonBlank(updated, updatedAt);
        }

        public String buildSummary(String forecastUnits) {
            ZoneId zoneId = ZoneId.systemDefault();
            return buildSummary(forecastUnits, LocalDate.now(zoneId), zoneId);
        }

        public String buildSummary(String forecastUnits, LocalDate currentDate, ZoneId zoneId) {
            RachioForecastEntry todayForecast = getTodayForecast(currentDate, zoneId);
            if (todayForecast == null) {
                return "";
            }
            List<String> parts = new ArrayList<>();
            boolean usUnits = "US".equalsIgnoreCase(forecastUnits);
            String temperatureUnit = usUnits ? "\u00B0F" : "\u00B0C";
            double low = todayForecast.getLowTemperature();
            double high = todayForecast.getHighTemperature();
            if (!Double.isNaN(low) && !Double.isNaN(high)) {
                parts.add(formatForecastNumber(low) + "-" + formatForecastNumber(high) + " " + temperatureUnit);
            } else if (!Double.isNaN(high)) {
                parts.add("high " + formatForecastNumber(high) + " " + temperatureUnit);
            } else if (!Double.isNaN(low)) {
                parts.add("low " + formatForecastNumber(low) + " " + temperatureUnit);
            }
            if (!Double.isNaN(todayForecast.precipitationProbability)) {
                parts.add("precipitation chance " + formatProbability(todayForecast.precipitationProbability) + "%");
            }
            if (!Double.isNaN(todayForecast.precipitation)) {
                String precipitationUnit = usUnits ? "in" : "mm";
                parts.add(
                        "precipitation " + formatForecastNumber(todayForecast.precipitation) + " " + precipitationUnit);
            }
            double wind = todayForecast.getWind();
            if (!Double.isNaN(wind)) {
                String windUnit = usUnits ? "mph" : "km/h";
                parts.add("wind " + formatForecastNumber(wind) + " " + windUnit);
            }
            return String.join(", ", parts);
        }

        public @Nullable RachioForecastEntry getTodayForecast() {
            ZoneId zoneId = ZoneId.systemDefault();
            return getTodayForecast(LocalDate.now(zoneId), zoneId);
        }

        public @Nullable RachioForecastEntry getTodayForecast(LocalDate currentDate, ZoneId zoneId) {
            return selectTodayForecast(currentDate, zoneId).entry;
        }

        public boolean hasUsefulData() {
            ZoneId zoneId = ZoneId.systemDefault();
            return hasUsefulData(LocalDate.now(zoneId), zoneId);
        }

        public boolean hasUsefulData(LocalDate currentDate, ZoneId zoneId) {
            ForecastSelection selection = selectTodayForecast(currentDate, zoneId);
            RachioForecastEntry todayForecast = selection.entry;
            return !getSummary(selection, currentDate, zoneId).isBlank()
                    || (todayForecast != null && todayForecast.hasUsefulData());
        }

        public String parsedFieldSummary() {
            ZoneId zoneId = ZoneId.systemDefault();
            return parsedFieldSummary(LocalDate.now(zoneId), zoneId);
        }

        public String parsedFieldSummary(LocalDate currentDate, ZoneId zoneId) {
            ForecastSelection selection = selectTodayForecast(currentDate, zoneId);
            RachioForecastEntry todayForecast = selection.entry;
            return "todayFound=" + (todayForecast != null) + ", summary="
                    + !getSummary(selection, currentDate, zoneId).isBlank() + ", high="
                    + (todayForecast != null && !Double.isNaN(todayForecast.getHighTemperature())) + ", low="
                    + (todayForecast != null && !Double.isNaN(todayForecast.getLowTemperature())) + ", precipitation="
                    + (todayForecast != null && !Double.isNaN(todayForecast.precipitation)) + ", probability="
                    + (todayForecast != null && !Double.isNaN(todayForecast.precipitationProbability)) + ", wind="
                    + (todayForecast != null && !Double.isNaN(todayForecast.getWind())) + ", updated="
                    + !getUpdated().isBlank();
        }

        public String shapeSummary() {
            ZoneId zoneId = ZoneId.systemDefault();
            return shapeSummary(LocalDate.now(zoneId), zoneId);
        }

        public String shapeSummary(LocalDate currentDate, ZoneId zoneId) {
            RachioForecastEntry todayForecast = getTodayForecast(currentDate, zoneId);
            List<String> aliases = new ArrayList<>(matchedAliases);
            if (todayForecast != null) {
                aliases.addAll(todayForecast.matchedAliases);
            }
            return "topLevelKeys=" + String.join(",", topLevelFieldNames) + ", selectedEntryKeys="
                    + (todayForecast != null ? String.join(",", todayForecast.sourceFieldNames) : "")
                    + ", matchedAliases=" + String.join(",", aliases);
        }

        private String getSummary(ForecastSelection selection, LocalDate currentDate, ZoneId zoneId) {
            RachioForecastEntry todayForecast = selection.entry;
            if (summaryBelongsToIgnoredDatedTopLevelEntry(todayForecast, currentDate, zoneId)
                    || (todayForecast == null && selection.foundParsedDate)) {
                return todayForecast != null ? todayForecast.summary : "";
            }
            return firstNonBlank(summary, todayForecast != null ? todayForecast.summary : "");
        }

        private boolean summaryBelongsToIgnoredDatedTopLevelEntry(@Nullable RachioForecastEntry selectedForecast,
                LocalDate currentDate, ZoneId zoneId) {
            @Nullable
            RachioForecastEntry topLevelEntry = today;
            if (topLevelEntry == null || Objects.equals(topLevelEntry, selectedForecast) || summary.isBlank()
                    || !summary.equals(topLevelEntry.summary)) {
                return false;
            }
            LocalDate topLevelDate = topLevelEntry.getForecastDate(zoneId);
            return topLevelDate != null && !topLevelDate.equals(currentDate);
        }

        private ForecastSelection selectTodayForecast(LocalDate currentDate, ZoneId zoneId) {
            ArrayList<ForecastCandidate> candidates = new ArrayList<>();
            @Nullable
            RachioForecastEntry todayEntry = today;
            if (todayEntry != null) {
                candidates.add(new ForecastCandidate(todayEntry, "today", true));
            }
            addForecastCandidates(candidates, forecast, "forecast");
            addForecastCandidates(candidates, forecasts, "forecasts");
            addForecastCandidates(candidates, dailyForecasts, "dailyForecasts");

            @Nullable
            RachioForecastEntry firstUsefulFallback = null;
            @Nullable
            RachioForecastEntry firstUndatedFallback = null;
            boolean foundParsedDate = false;

            for (ForecastCandidate candidate : candidates) {
                RachioForecastEntry entry = candidate.entry;
                if (!entry.hasUsefulData()) {
                    continue;
                }
                if (firstUsefulFallback == null) {
                    firstUsefulFallback = entry;
                }

                LocalDate entryDate = entry.getForecastDate(zoneId);
                if (entryDate != null) {
                    foundParsedDate = true;
                    if (entryDate.equals(currentDate)) {
                        logger.debug("Selected today forecast by matching local date {} from {} entry date/time '{}'",
                                currentDate, candidate.source, entry.getForecastDateTimeForLog());
                        return new ForecastSelection(entry, true);
                    }
                    logger.trace(
                            "Ignored dated forecast entry from {} with date/time '{}' because date {} is not controller-local today {}",
                            candidate.source, entry.getForecastDateTimeForLog(), entryDate, currentDate);
                    continue;
                }

                if (!entry.hasForecastDateTimeValue()) {
                    if (candidate.explicitToday) {
                        logger.trace("Selected explicit today forecast without date/time for local date {}",
                                currentDate);
                        return new ForecastSelection(entry, foundParsedDate);
                    }
                    if (firstUndatedFallback == null) {
                        firstUndatedFallback = entry;
                    }
                }
            }

            if (foundParsedDate) {
                if (firstUndatedFallback != null) {
                    logger.trace(
                            "Selected undated forecast entry because no dated entry matched controller-local date {}",
                            currentDate);
                    return new ForecastSelection(firstUndatedFallback, true);
                }
                logger.debug("No dated today forecast entry found for local date {}; retaining previous values",
                        currentDate);
                return new ForecastSelection(null, true);
            }

            if (firstUsefulFallback != null) {
                logger.trace("Selected first useful forecast entry because no forecast date/time could be parsed");
            }
            return new ForecastSelection(firstUsefulFallback, false);
        }

        private static void addForecastCandidates(List<ForecastCandidate> candidates, List<RachioForecastEntry> entries,
                String source) {
            for (RachioForecastEntry entry : entries) {
                candidates.add(new ForecastCandidate(entry, source, false));
            }
        }

        private record ForecastCandidate(RachioForecastEntry entry, String source, boolean explicitToday) {
        }

        private record ForecastSelection(@Nullable RachioForecastEntry entry, boolean foundParsedDate) {
        }

        private static void parseForecastObject(RachioForecastResponse response, JsonObject object) {
            response.topLevelFieldNames.clear();
            response.topLevelFieldNames.addAll(object.keySet());
            response.summary = firstNonBlank(readStringWithAlias(object, response.matchedAliases, "summary",
                    "weatherSummary", "forecastSummary", "description"), response.summary);
            response.updated = firstNonBlank(readStringWithAlias(object, response.matchedAliases, "updated",
                    "updatedAt", "forecastUpdated", "lastUpdated"), response.updated);
            response.updatedAt = firstNonBlank(readStringWithAlias(object, response.matchedAliases, "updatedAt"),
                    response.updatedAt);

            RachioForecastEntry todayEntry = readForecastEntry(object.get("today"));
            if (todayEntry != null) {
                response.today = todayEntry;
            }
            addForecastEntries(response.forecast, object.get("forecast"));
            addForecastEntries(response.forecasts, object.get("forecasts"));
            addForecastEntries(response.dailyForecasts, object.get("dailyForecasts"));
            addForecastEntries(response.dailyForecasts, object.get("daily"));
            addNestedForecastEntries(response, object.get("weather"));

            if (response.today == null) {
                RachioForecastEntry topLevelEntry = parseForecastEntry(object);
                if (topLevelEntry.hasUsefulData()) {
                    response.today = topLevelEntry;
                }
            }
        }

        private static void addNestedForecastEntries(RachioForecastResponse response, @Nullable JsonElement element) {
            if (element == null || !element.isJsonObject()) {
                return;
            }
            JsonObject object = element.getAsJsonObject();
            addForecastEntries(response.dailyForecasts, object.get("daily"));
            addForecastEntries(response.dailyForecasts, object.get("forecast"));
            addForecastEntries(response.dailyForecasts, object.get("forecasts"));
            addForecastEntries(response.dailyForecasts, object.get("dailyForecasts"));
        }

        private static void addForecastEntries(List<RachioForecastEntry> entries, @Nullable JsonElement element) {
            if (element == null || element.isJsonNull()) {
                return;
            }
            if (element.isJsonArray()) {
                for (JsonElement child : element.getAsJsonArray()) {
                    addForecastEntries(entries, child);
                }
                return;
            }
            if (!element.isJsonObject()) {
                return;
            }
            JsonObject object = element.getAsJsonObject();
            int before = entries.size();
            for (String nestedName : List.of("daily", "forecast", "forecasts", "dailyForecasts", "data", "items",
                    "results")) {
                addForecastEntries(entries, object.get(nestedName));
            }
            if (entries.size() == before) {
                RachioForecastEntry entry = parseForecastEntry(object);
                if (entry.hasUsefulData()) {
                    entries.add(entry);
                }
            }
        }

        private static @Nullable RachioForecastEntry readForecastEntry(@Nullable JsonElement element) {
            if (element == null || !element.isJsonObject()) {
                return null;
            }
            RachioForecastEntry entry = parseForecastEntry(element.getAsJsonObject());
            return entry.hasUsefulData() ? entry : null;
        }

        private static RachioForecastEntry parseForecastEntry(JsonObject object) {
            RachioForecastEntry entry = new RachioForecastEntry();
            entry.sourceFieldNames.addAll(object.keySet());
            entry.summary = readStringWithAlias(object, entry.matchedAliases, "summary", "weatherSummary", "condition",
                    "conditions", "description", "shortForecast");
            entry.date = firstNonBlank(readString(object, "date"), entry.date);
            // These identify the selected forecast entry, not when the API response was retrieved.
            entry.time = firstNonBlank(readStringWithAlias(object, entry.matchedAliases, "localizedTimeStamp", "time"),
                    entry.time);
            entry.highTemperature = firstDouble(object, entry.matchedAliases, "highTemperature", "temperatureHigh",
                    "high", "maxTemperature", "temperatureMax");
            entry.lowTemperature = firstDouble(object, entry.matchedAliases, "lowTemperature", "temperatureLow", "low",
                    "minTemperature", "temperatureMin");
            entry.precipitation = firstDouble(object, entry.matchedAliases, "precipitation", "precip",
                    "precipitationAmount", "calculatedPrecip");
            // precipIntensity is a rate, not the selected period's accumulated amount.
            recordNumericAlias(object, entry.matchedAliases, "precipIntensity");
            entry.precipitationProbability = firstDouble(object, entry.matchedAliases, "precipitationProbability",
                    "precipProbability", "probabilityOfPrecipitation", "pop");
            entry.wind = firstDouble(object, entry.matchedAliases, "wind", "windSpeed");
            entry.windSpeed = firstDouble(object, entry.matchedAliases, "windSpeed");
            return entry;
        }

        private static String formatForecastNumber(double value) {
            if (Math.rint(value) == value) {
                return Long.toString(Math.round(value));
            }
            return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
        }

        private static String formatProbability(double value) {
            double percentage = value <= 1 ? value * 100 : value;
            return formatForecastNumber(percentage);
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
        public ArrayList<String> sourceFieldNames = new ArrayList<>();
        public ArrayList<String> matchedAliases = new ArrayList<>();

        public double getHighTemperature() {
            return Double.isNaN(highTemperature) ? high : highTemperature;
        }

        public double getLowTemperature() {
            return Double.isNaN(lowTemperature) ? low : lowTemperature;
        }

        public double getWind() {
            return Double.isNaN(wind) ? windSpeed : wind;
        }

        public boolean hasUsefulData() {
            return !summary.isBlank() || !Double.isNaN(getHighTemperature()) || !Double.isNaN(getLowTemperature())
                    || !Double.isNaN(precipitation) || !Double.isNaN(precipitationProbability)
                    || !Double.isNaN(getWind());
        }

        private boolean hasForecastDateTimeValue() {
            return !date.isBlank() || !time.isBlank();
        }

        private String getForecastDateTimeForLog() {
            return firstNonBlank(date, time);
        }

        private @Nullable LocalDate getForecastDate(ZoneId zoneId) {
            LocalDate parsedDate = parseForecastDate(date, zoneId);
            return parsedDate != null ? parsedDate : parseForecastDate(time, zoneId);
        }

        private static @Nullable LocalDate parseForecastDate(String value, ZoneId zoneId) {
            String trimmed = value.trim();
            if (trimmed.isBlank()) {
                return null;
            }

            LocalDate numericDate = parseNumericForecastDate(trimmed, zoneId);
            if (numericDate != null) {
                return numericDate;
            }
            try {
                return Instant.parse(trimmed).atZone(zoneId).toLocalDate();
            } catch (DateTimeException e) {
                // Try the next supported timestamp shape.
            }
            try {
                return OffsetDateTime.parse(trimmed).atZoneSameInstant(zoneId).toLocalDate();
            } catch (DateTimeException e) {
                // Try the next supported timestamp shape.
            }
            try {
                return ZonedDateTime.parse(trimmed).withZoneSameInstant(zoneId).toLocalDate();
            } catch (DateTimeException e) {
                // Try the next supported timestamp shape.
            }
            try {
                return LocalDateTime.parse(trimmed).toLocalDate();
            } catch (DateTimeException e) {
                // Try the next supported timestamp shape.
            }
            if (trimmed.contains(" ")) {
                try {
                    return LocalDateTime.parse(trimmed.replace(' ', 'T')).toLocalDate();
                } catch (DateTimeException e) {
                    // Try the next supported timestamp shape.
                }
            }
            try {
                return LocalDate.parse(trimmed);
            } catch (DateTimeException e) {
                // Try the date prefix as a final compatibility fallback.
            }
            if (trimmed.length() >= 10) {
                try {
                    return LocalDate.parse(trimmed.substring(0, 10));
                } catch (DateTimeException e) {
                    return null;
                }
            }
            return null;
        }

        private static @Nullable LocalDate parseNumericForecastDate(String value, ZoneId zoneId) {
            if (!value.matches("-?\\d+")) {
                return null;
            }
            try {
                long epoch = Long.parseLong(value);
                long epochMillis = Math.abs(epoch) < 10_000_000_000L ? epoch * 1000 : epoch;
                return Instant.ofEpochMilli(epochMillis).atZone(zoneId).toLocalDate();
            } catch (DateTimeException | NumberFormatException e) {
                return null;
            }
        }
    }

    public static class RachioScheduleRuleResponse {
        public String id = "";
        public String name = "";
        public String externalName = "";
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

    private static String readStringWithAlias(JsonObject object, List<String> matchedAliases, String... memberNames) {
        for (String memberName : memberNames) {
            String value = readString(object, memberName);
            if (!value.isBlank()) {
                addMatchedAlias(matchedAliases, memberName);
                return value;
            }
        }
        return "";
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

    private static double firstDouble(JsonObject object, List<String> matchedAliases, String... memberNames) {
        for (String memberName : memberNames) {
            JsonElement element = object.get(memberName);
            if (element != null && element.isJsonPrimitive()) {
                try {
                    addMatchedAlias(matchedAliases, memberName);
                    return element.getAsDouble();
                } catch (RuntimeException e) {
                    // Try the next documented alias.
                }
            }
        }
        return Double.NaN;
    }

    private static void recordNumericAlias(JsonObject object, List<String> matchedAliases, String memberName) {
        JsonElement element = object.get(memberName);
        if (element != null && element.isJsonPrimitive()) {
            try {
                element.getAsDouble();
                addMatchedAlias(matchedAliases, memberName);
            } catch (RuntimeException e) {
                // Ignore malformed diagnostic-only fields.
            }
        }
    }

    private static void addMatchedAlias(List<String> matchedAliases, String memberName) {
        if (!matchedAliases.contains(memberName)) {
            matchedAliases.add(memberName);
        }
    }
}
