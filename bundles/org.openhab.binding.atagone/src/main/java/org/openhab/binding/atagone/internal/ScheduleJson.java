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
package org.openhab.binding.atagone.internal;

import static org.openhab.binding.atagone.internal.AtagOneBindingConstants.WEEKDAY_BY_NAME;
import static org.openhab.binding.atagone.internal.AtagOneBindingConstants.WEEKDAY_NAMES;

import java.util.Locale;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.atagone.internal.dto.ScheduleDTO;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

/**
 * JSON codec for the whole-week CH/DHW schedule channels and Thing Actions.
 *
 * @author Florian Lettner - Initial contribution
 */
@NonNullByDefault
public final class ScheduleJson {

    private static final Gson GSON = new GsonBuilder().create();

    private static final int MAX_INPUT_LENGTH = 16 * 1024;
    private static final int MAX_PERIODS_PER_DAY = 100;

    private ScheduleJson() {
    }

    public static String toJson(double baseTemp, double[][][] entries) {
        JsonObject root = new JsonObject();
        root.addProperty("baseTemp", baseTemp);
        JsonObject days = new JsonObject();
        for (int dayIndex = 0; dayIndex < 7; dayIndex++) {
            JsonArray periods = new JsonArray();
            double @Nullable [][] dayEntries = dayIndex < entries.length ? entries[dayIndex] : null;
            if (dayEntries != null) {
                for (double[] period : dayEntries) {
                    JsonObject p = new JsonObject();
                    p.addProperty("start", (long) period[0]);
                    p.addProperty("end", (long) period[1]);
                    p.addProperty("temp", period[2]);
                    periods.add(p);
                }
            }
            days.add(WEEKDAY_NAMES.get(dayIndex + 1), periods);
        }
        root.add("days", days);
        return GSON.toJson(root);
    }

    @Nullable
    public static ScheduleDTO parse(String json, double currentBaseTemp, double[][][] currentEntries) {
        if (json.length() > MAX_INPUT_LENGTH) {
            return null;
        }
        JsonObject root;
        try {
            JsonElement parsed = JsonParser.parseString(json);
            if (!parsed.isJsonObject()) {
                return null;
            }
            root = parsed.getAsJsonObject();
        } catch (JsonParseException e) {
            return null;
        } catch (StackOverflowError e) {
            // Gson has no nesting-depth limit; a deeply-nested payload would blow the stack without this catch.
            return null;
        }

        double baseTemp = currentBaseTemp;
        if (root.has("baseTemp")) {
            Double parsedBaseTemp = numberOrNull(root, "baseTemp");
            if (parsedBaseTemp == null) {
                return null;
            }
            baseTemp = parsedBaseTemp;
        }

        double[][][] entries = currentEntries.clone();
        if (root.has("days")) {
            JsonElement daysElement = root.get("days");
            if (!daysElement.isJsonObject()) {
                return null;
            }
            for (Map.Entry<String, JsonElement> dayEntry : daysElement.getAsJsonObject().entrySet()) {
                Integer weekdayNumber = WEEKDAY_BY_NAME.get(dayEntry.getKey().toLowerCase(Locale.ROOT));
                if (weekdayNumber == null) {
                    return null;
                }
                int dayIndex = weekdayNumber - 1;
                if (dayIndex < 0 || dayIndex >= currentEntries.length) {
                    return null;
                }
                double[][] dayPeriods = parseDayPeriods(dayEntry.getValue());
                if (dayPeriods == null) {
                    return null;
                }
                entries[dayIndex] = dayPeriods;
            }
        }

        ScheduleDTO schedule = new ScheduleDTO();
        schedule.base_temp = baseTemp;
        schedule.entries = entries;
        return schedule;
    }

    private static double @Nullable [][] parseDayPeriods(JsonElement periodsElement) {
        if (!periodsElement.isJsonArray()) {
            return null;
        }
        JsonArray periodsJson = periodsElement.getAsJsonArray();
        if (periodsJson.size() > MAX_PERIODS_PER_DAY) {
            return null;
        }
        double[][] dayPeriods = new double[periodsJson.size()][];
        for (int i = 0; i < periodsJson.size(); i++) {
            JsonElement periodElement = periodsJson.get(i);
            if (!periodElement.isJsonObject()) {
                return null;
            }
            JsonObject p = periodElement.getAsJsonObject();
            Double start = numberOrNull(p, "start");
            Double end = numberOrNull(p, "end");
            Double temp = numberOrNull(p, "temp");
            if (start == null || end == null || temp == null || !isValidPeriod(start, end)) {
                return null;
            }
            dayPeriods[i] = new double[] { start, end, temp };
        }
        for (int i = 0; i < dayPeriods.length; i++) {
            for (int j = i + 1; j < dayPeriods.length; j++) {
                if (periodsOverlap(dayPeriods[i][0], dayPeriods[i][1], dayPeriods[j][0], dayPeriods[j][1])) {
                    return null;
                }
            }
        }
        return dayPeriods;
    }

    @Nullable
    private static Double numberOrNull(JsonObject obj, String field) {
        if (!obj.has(field)) {
            return null;
        }
        JsonElement element = obj.get(field);
        if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isNumber()) {
            return null;
        }
        return element.getAsDouble();
    }

    public static boolean isValidPeriod(double startMinutes, double endMinutes) {
        return startMinutes >= 0 && endMinutes <= 1440 && startMinutes < endMinutes
                && startMinutes == Math.rint(startMinutes) && endMinutes == Math.rint(endMinutes);
    }

    public static boolean periodsOverlap(double aStart, double aEnd, double bStart, double bEnd) {
        return aStart < bEnd && aEnd > bStart;
    }
}
