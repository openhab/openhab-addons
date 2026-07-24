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

import static org.openhab.binding.rachio.internal.RachioBindingConstants.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.rachio.internal.RachioBindingConstants;
import org.openhab.core.thing.Thing;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * DTOs for the Rachio Smart Hose Timer ValveService.
 *
 * @author openHAB Contributors - Initial contribution
 */
public class RachioSmartHoseTimerGsonDTO {
    private static final Gson GSON = new Gson();

    public static class RachioBaseStationListResponse {
        public ArrayList<RachioBaseStation> baseStations = new ArrayList<>();

        public static RachioBaseStationListResponse fromJson(String json) {
            RachioBaseStationListResponse response = new RachioBaseStationListResponse();
            response.baseStations
                    .addAll(parseArray(json, RachioBaseStation.class, "baseStations", "items", "data", "results"));
            return response;
        }
    }

    public static class RachioValveListResponse {
        public ArrayList<RachioValve> valves = new ArrayList<>();

        public static RachioValveListResponse fromJson(String json) {
            RachioValveListResponse response = new RachioValveListResponse();
            response.valves.addAll(parseArray(json, RachioValve.class, "valves", "items", "data", "results"));
            return response;
        }
    }

    public static class RachioBaseStation {
        public String id = "";
        public String name = "";
        public String displayName = "";
        public String nickname = "";
        public String serialNumber = "";
        public String model = "";
        public String firmwareVersion = "";
        public String hardwareVersion = "";
        public String status = "";
        public @Nullable Boolean online;
        public @Nullable Boolean connected;

        public String getThingID() {
            return firstNonBlank(id, serialNumber, getThingName());
        }

        public String getThingName() {
            return firstNonBlank(name, displayName, nickname, "Rachio BaseStation");
        }

        public boolean isOnline() {
            Boolean online = this.online;
            if (online != null) {
                return online.booleanValue();
            }
            Boolean connected = this.connected;
            if (connected != null) {
                return connected.booleanValue();
            }
            return "ONLINE".equalsIgnoreCase(status) || "CONNECTED".equalsIgnoreCase(status);
        }

        public boolean hasOnlineState() {
            return online != null || connected != null || !status.isBlank();
        }

        public Map<String, String> fillProperties() {
            Map<String, String> properties = new HashMap<>();
            properties.put(Thing.PROPERTY_VENDOR, RachioBindingConstants.BINDING_VENDOR);
            properties.put(PROPERTY_BASE_STATION_ID, id);
            putIfNotBlank(properties, PROPERTY_NAME, getThingName());
            putIfNotBlank(properties, Thing.PROPERTY_SERIAL_NUMBER, serialNumber);
            putIfNotBlank(properties, PROPERTY_MODEL, model);
            putIfNotBlank(properties, "firmwareVersion", firmwareVersion);
            putIfNotBlank(properties, "hardwareVersion", hardwareVersion);
            return properties;
        }
    }

    public static class RachioValve {
        public String id = "";
        public String baseStationId = "";
        public String name = "";
        public String displayName = "";
        public String nickname = "";
        public String serialNumber = "";
        public String model = "";
        public String firmwareVersion = "";
        public String hardwareVersion = "";
        public String status = "";
        public @Nullable Boolean online;
        public @Nullable Boolean connected;
        public @Nullable Double batteryLevel;
        public @Nullable Integer defaultRuntimeSeconds;
        public @Nullable RachioValveState state;
        public @Nullable RachioValveState valveState;

        public String getThingID() {
            return firstNonBlank(id, serialNumber, getThingName());
        }

        public String getThingName() {
            return firstNonBlank(name, displayName, nickname, "Rachio Valve");
        }

        public RachioValveState getState() {
            RachioValveState valveState = this.valveState;
            if (valveState != null) {
                return valveState;
            }
            RachioValveState state = this.state;
            return state != null ? state : new RachioValveState();
        }

        public boolean isOnline() {
            Boolean online = this.online;
            if (online != null) {
                return online.booleanValue();
            }
            Boolean connected = this.connected;
            if (connected != null) {
                return connected.booleanValue();
            }
            RachioValveState state = getState();
            Boolean stateOnline = state.online;
            if (stateOnline != null) {
                return stateOnline.booleanValue();
            }
            Boolean stateConnected = state.connected;
            if (stateConnected != null) {
                return stateConnected.booleanValue();
            }
            return "ONLINE".equalsIgnoreCase(status) || "CONNECTED".equalsIgnoreCase(status);
        }

        public boolean hasOnlineState() {
            RachioValveState state = getState();
            return online != null || connected != null || state.online != null || state.connected != null
                    || !status.isBlank();
        }

        public boolean stateMatches() {
            Boolean matches = getState().matches;
            return matches != null && matches.booleanValue();
        }

        public boolean hasStateMatches() {
            return getState().matches != null;
        }

        public boolean flowDetected() {
            return getState().getFlowDetected();
        }

        public boolean hasFlowDetected() {
            return getState().hasFlowDetected();
        }

        public int getDefaultRuntimeSeconds() {
            Integer defaultRuntimeSeconds = this.defaultRuntimeSeconds;
            if (defaultRuntimeSeconds != null && defaultRuntimeSeconds.intValue() > 0) {
                return defaultRuntimeSeconds.intValue();
            }
            Integer runtime = getState().defaultRuntimeSeconds;
            return runtime != null ? Math.max(0, runtime.intValue()) : 0;
        }

        public Map<String, String> fillProperties() {
            Map<String, String> properties = new HashMap<>();
            properties.put(Thing.PROPERTY_VENDOR, RachioBindingConstants.BINDING_VENDOR);
            properties.put(PROPERTY_VALVE_ID, id);
            putIfNotBlank(properties, PROPERTY_BASE_STATION_ID, baseStationId);
            putIfNotBlank(properties, PROPERTY_NAME, getThingName());
            putIfNotBlank(properties, Thing.PROPERTY_SERIAL_NUMBER, serialNumber);
            putIfNotBlank(properties, PROPERTY_MODEL, model);
            putIfNotBlank(properties, "firmwareVersion", firmwareVersion);
            putIfNotBlank(properties, "hardwareVersion", hardwareVersion);
            return properties;
        }
    }

    public static class RachioValveState {
        public @Nullable Boolean matches;
        public @Nullable Boolean online;
        public @Nullable Boolean connected;
        public @Nullable Boolean flowDetected;
        public String flowDetectedText = "";
        public @Nullable Integer defaultRuntimeSeconds;

        public boolean getFlowDetected() {
            Boolean flowDetected = this.flowDetected;
            if (flowDetected != null) {
                return flowDetected.booleanValue();
            }
            return Boolean.parseBoolean(flowDetectedText);
        }

        public boolean hasFlowDetected() {
            return flowDetected != null || !flowDetectedText.isBlank();
        }
    }

    public static class RachioValveDefaultRuntimeRequest {
        public String valveId;
        public int defaultRuntimeSeconds;

        public RachioValveDefaultRuntimeRequest(String valveId, int defaultRuntimeSeconds) {
            this.valveId = valveId;
            this.defaultRuntimeSeconds = defaultRuntimeSeconds;
        }
    }

    public static class RachioValveStartWateringRequest {
        public String valveId;
        public int durationSeconds;

        public RachioValveStartWateringRequest(String valveId, int durationSeconds) {
            this.valveId = valveId;
            this.durationSeconds = durationSeconds;
        }
    }

    public static class RachioValveStopWateringRequest {
        public String valveId;

        public RachioValveStopWateringRequest(String valveId) {
            this.valveId = valveId;
        }
    }

    public static class RachioValveProgramListResponse {
        public ArrayList<RachioValveProgram> programs = new ArrayList<>();

        public static RachioValveProgramListResponse fromJson(String json) {
            RachioValveProgramListResponse response = new RachioValveProgramListResponse();
            response.programs.addAll(
                    parseArray(json, RachioValveProgram.class, "programs", "items", "data", "results", "programsV2"));
            return response;
        }
    }

    public static class RachioValveProgram {
        public String id = "";
        public String name = "";
        public String displayName = "";
        public String nickname = "";
        public String type = "";
        public String programType = "";
        public @Nullable Boolean enabled;
        public String baseStationId = "";
        public String valveId = "";
        public ArrayList<String> valveIds = new ArrayList<>();
        public @Nullable RachioResourceId resourceId;
        public String startTime = "";
        public String nextRunTime = "";
        public String lastRunTime = "";
        public String updatedAt = "";
        public String lastUpdateDate = "";
        public int duration = 0;
        public int durationSeconds = 0;
        public int intervalDays = 0;
        public double seasonalAdjustment = Double.NaN;
        public @Nullable JsonElement daysOfWeek;
        public ArrayList<RachioValveDayRun> plannedRuns = new ArrayList<>();

        public String getThingID() {
            return firstNonBlank(id, getThingName());
        }

        public String getThingName() {
            return firstNonBlank(name, displayName, nickname, "Rachio Valve Program");
        }

        public String getProgramType() {
            return firstNonBlank(programType, type);
        }

        public String getValveId() {
            RachioResourceId resourceId = this.resourceId;
            String candidate = firstNonBlank(valveId, resourceId != null ? resourceId.valveId : "");
            if (!candidate.isBlank()) {
                return candidate;
            }
            if (!valveIds.isEmpty()) {
                return valveIds.get(0);
            }
            for (RachioValveDayRun run : plannedRuns) {
                candidate = run.getValveId();
                if (!candidate.isBlank()) {
                    return candidate;
                }
            }
            return "";
        }

        public String getBaseStationId() {
            RachioResourceId resourceId = this.resourceId;
            return firstNonBlank(baseStationId, resourceId != null ? resourceId.baseStationId : "");
        }

        public int getDurationSeconds() {
            return durationSeconds > 0 ? durationSeconds : duration;
        }

        public String getDaysOfWeek() {
            JsonElement days = daysOfWeek;
            return days != null && !days.isJsonNull() ? days.toString() : "";
        }

        public Map<String, String> fillProperties() {
            Map<String, String> properties = new HashMap<>();
            properties.put(Thing.PROPERTY_VENDOR, RachioBindingConstants.BINDING_VENDOR);
            properties.put(PROPERTY_VALVE_PROGRAM_ID, id);
            putIfNotBlank(properties, PROPERTY_VALVE_ID, getValveId());
            putIfNotBlank(properties, PROPERTY_BASE_STATION_ID, getBaseStationId());
            putIfNotBlank(properties, PROPERTY_NAME, getThingName());
            properties.put(PROPERTY_VALVE_PROGRAM_API_VERSION, "V2");
            return properties;
        }
    }

    public static class RachioResourceId {
        public String valveId = "";
        public String baseStationId = "";
        public String programId = "";
    }

    public static class RachioValveDayViewsRequest {
        public RachioDateRequest start;
        public RachioDateRequest end;
        public RachioResourceId resourceId;

        public RachioValveDayViewsRequest(LocalDate start, LocalDate end, String valveId) {
            this.start = new RachioDateRequest(start);
            this.end = new RachioDateRequest(end);
            this.resourceId = new RachioResourceId();
            this.resourceId.valveId = valveId;
        }
    }

    public static class RachioDateRequest {
        public String date;

        public RachioDateRequest(LocalDate date) {
            this.date = date.toString();
        }
    }

    public static class RachioValveDayViewsResponse {
        public ArrayList<RachioValveDayView> dayViews = new ArrayList<>();

        public static RachioValveDayViewsResponse fromJson(String json) {
            RachioValveDayViewsResponse response = new RachioValveDayViewsResponse();
            response.dayViews.addAll(parseArray(json, RachioValveDayView.class, "dayViews", "valveDayViews", "days",
                    "items", "data", "results"));
            return response;
        }

        public List<RachioValveDayRun> getRuns() {
            ArrayList<RachioValveDayRun> runs = new ArrayList<>();
            for (RachioValveDayView dayView : dayViews) {
                runs.addAll(dayView.getRuns());
            }
            return runs;
        }

        public Optional<RachioValveDayRun> findNextPlannedRun() {
            long now = System.currentTimeMillis();
            return getRuns().stream().filter(run -> run.getStartEpochMillis() >= now)
                    .min(Comparator.comparingLong(RachioValveDayRun::getStartEpochMillis));
        }

        public Optional<RachioValveDayRun> findNextSkippedRun() {
            long now = System.currentTimeMillis();
            return getRuns().stream().filter(RachioValveDayRun::isSkipped)
                    .filter(run -> run.getStartEpochMillis() >= now)
                    .min(Comparator.comparingLong(RachioValveDayRun::getStartEpochMillis));
        }

        public Optional<RachioValveDayRun> findLastCompletedRun() {
            long now = System.currentTimeMillis();
            return getRuns().stream().filter(run -> !run.isSkipped()).filter(run -> run.getStartEpochMillis() <= now)
                    .max(Comparator.comparingLong(RachioValveDayRun::getStartEpochMillis));
        }
    }

    public static class RachioValveDayView {
        public String date = "";
        public ArrayList<RachioValveDayRun> runs = new ArrayList<>();
        public ArrayList<RachioValveDayRun> valveRuns = new ArrayList<>();
        public ArrayList<RachioValveDayRun> plannedRuns = new ArrayList<>();
        public ArrayList<RachioValveDayRun> completedRuns = new ArrayList<>();

        public List<RachioValveDayRun> getRuns() {
            ArrayList<RachioValveDayRun> allRuns = new ArrayList<>();
            allRuns.addAll(runs);
            allRuns.addAll(valveRuns);
            allRuns.addAll(plannedRuns);
            allRuns.addAll(completedRuns);
            return allRuns;
        }
    }

    public static class RachioValveDayRun {
        public String id = "";
        public String plannedRunId = "";
        public String programId = "";
        public String valveId = "";
        public @Nullable RachioResourceId resourceId;
        public String startTime = "";
        public String plannedRunStartTime = "";
        public String timestamp = "";
        public String date = "";
        public String status = "";
        public String runStatus = "";
        public String type = "";
        public @Nullable Boolean skipped;
        public int duration = 0;
        public int durationSeconds = 0;

        public String getValveId() {
            RachioResourceId resourceId = this.resourceId;
            return firstNonBlank(valveId, resourceId != null ? resourceId.valveId : "");
        }

        public String getProgramId() {
            RachioResourceId resourceId = this.resourceId;
            return firstNonBlank(programId, resourceId != null ? resourceId.programId : "");
        }

        public String getPlannedRunId() {
            return firstNonBlank(plannedRunId, id);
        }

        public String getStartTime() {
            return firstNonBlank(plannedRunStartTime, startTime, timestamp, date);
        }

        public int getDurationSeconds() {
            return durationSeconds > 0 ? durationSeconds : duration;
        }

        public String getStatus() {
            return firstNonBlank(status, runStatus, type);
        }

        public boolean isSkipped() {
            Boolean skipped = this.skipped;
            if (skipped != null) {
                return skipped.booleanValue();
            }
            String normalizedStatus = getStatus().toUpperCase(Locale.ROOT);
            return normalizedStatus.contains("SKIP");
        }

        public String getSkipOverrideDate() {
            String value = getStartTime();
            if (value.length() >= 10) {
                return value.substring(0, 10);
            }
            return value;
        }

        public long getStartEpochMillis() {
            String value = getStartTime();
            if (value.isBlank()) {
                return Long.MAX_VALUE;
            }
            try {
                if (value.chars().allMatch(Character::isDigit)) {
                    long epoch = Long.parseLong(value);
                    return value.length() > 10 ? epoch : epoch * 1000L;
                }
                if (value.length() == 10) {
                    return LocalDate.parse(value).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()
                            .toEpochMilli();
                }
                return Instant.parse(value).toEpochMilli();
            } catch (DateTimeParseException | NumberFormatException e) {
                return Long.MAX_VALUE;
            }
        }
    }

    public static class RachioProgramSkipOverrideRequest {
        public String programId;
        public String timestamp;

        public RachioProgramSkipOverrideRequest(String programId, String timestamp) {
            this.programId = programId;
            this.timestamp = timestamp;
        }
    }

    public static class RachioPlannedRunSkipOverrideRequest {
        public String plannedRunId;
        public String date;

        public RachioPlannedRunSkipOverrideRequest(String plannedRunId, String date) {
            this.plannedRunId = plannedRunId;
            this.date = date;
        }
    }

    public static RachioBaseStation parseBaseStation(String json) {
        RachioBaseStation baseStation = parseObject(json, RachioBaseStation.class, "baseStation", "data", "result");
        return baseStation != null ? baseStation : new RachioBaseStation();
    }

    public static RachioValve parseValve(String json) {
        RachioValve valve = parseObject(json, RachioValve.class, "valve", "data", "result");
        return valve != null ? valve : new RachioValve();
    }

    public static RachioValveProgram parseValveProgram(String json) {
        RachioValveProgram program = parseObject(json, RachioValveProgram.class, "program", "programV2", "data",
                "result");
        return program != null ? program : new RachioValveProgram();
    }

    private static <T> List<T> parseArray(String json, Class<T> valueType, String... arrayNames) {
        List<T> values = new ArrayList<>();
        JsonElement root = JsonParser.parseString(json);
        if (root.isJsonArray()) {
            addArrayEntries(values, root.getAsJsonArray(), valueType);
            return values;
        }
        if (!root.isJsonObject()) {
            return values;
        }
        JsonObject object = root.getAsJsonObject();
        for (String arrayName : arrayNames) {
            JsonElement arrayElement = object.get(arrayName);
            if (arrayElement != null && arrayElement.isJsonArray()) {
                addArrayEntries(values, arrayElement.getAsJsonArray(), valueType);
            }
        }
        return values;
    }

    private static <T> void addArrayEntries(List<T> values, JsonArray array, Class<T> valueType) {
        for (JsonElement element : array) {
            if (element != null && element.isJsonObject()) {
                var value = GSON.fromJson(element, valueType);
                if (value != null) {
                    values.add(value);
                }
            }
        }
    }

    private static <T> @Nullable T parseObject(String json, Class<T> valueType, String... objectNames) {
        JsonElement root = JsonParser.parseString(json);
        if (!root.isJsonObject()) {
            return null;
        }
        JsonObject object = root.getAsJsonObject();
        for (String objectName : objectNames) {
            JsonElement nestedObject = object.get(objectName);
            if (nestedObject != null && nestedObject.isJsonObject()) {
                return GSON.fromJson(nestedObject, valueType);
            }
        }
        return GSON.fromJson(object, valueType);
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (!value.isBlank()) {
                return value;
            }
        }
        return "";
    }

    private static void putIfNotBlank(Map<String, String> properties, String key, String value) {
        if (!value.isBlank()) {
            properties.put(key, value);
        }
    }
}
