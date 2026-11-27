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
import static org.openhab.binding.rachio.internal.RachioUtils.firstNonBlank;
import static org.openhab.binding.rachio.internal.RachioUtils.putIfNotBlank;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.rachio.internal.RachioBindingConstants;
import org.openhab.core.thing.Thing;

import com.google.gson.JsonElement;

/**
 * Smart Hose valve-program response.
 *
 * @author Kovacs Istvan - Initial contribution
 */
@NonNullByDefault
public class RachioValveProgram {
    public String id = "";
    public String name = "";
    public String displayName = "";
    public String nickname = "";
    public String type = "";
    public String programType = "";
    public @Nullable Boolean enabled;
    public String baseStationId = "";
    public String valveId = "";
    public @Nullable List<String> valveIds = new ArrayList<>();
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
    // Program responses can contain an object instead of a day-run array.
    public @Nullable JsonElement plannedRuns;

    public static RachioValveProgram fromJson(String json) {
        RachioValveProgram program = RachioSmartHoseJsonParser.parseObject(json, RachioValveProgram.class, "program",
                "programV2", "data", "result");
        return program != null ? program : new RachioValveProgram();
    }

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
        List<String> valveIds = this.valveIds;
        if (valveIds != null && !valveIds.isEmpty()) {
            return firstNonBlank(valveIds.get(0));
        }
        JsonElement plannedRuns = this.plannedRuns;
        if (plannedRuns != null && plannedRuns.isJsonArray()) {
            List<RachioValveDayRun> runs = new ArrayList<>();
            RachioSmartHoseJsonParser.addArrayEntries(runs, plannedRuns.getAsJsonArray(), RachioValveDayRun.class);
            for (RachioValveDayRun run : runs) {
                candidate = run.getValveId();
                if (!candidate.isBlank()) {
                    return candidate;
                }
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
        properties.put(PROPERTY_VALVE_PROGRAM_ID, firstNonBlank(id));
        putIfNotBlank(properties, PROPERTY_VALVE_ID, getValveId());
        putIfNotBlank(properties, PROPERTY_BASE_STATION_ID, getBaseStationId());
        putIfNotBlank(properties, PROPERTY_NAME, getThingName());
        properties.put(PROPERTY_VALVE_PROGRAM_API_VERSION, "V2");
        return properties;
    }
}
