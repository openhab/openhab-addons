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
 * JSON codec for the whole-week CH/DHW schedule shape shared by the {@code heating#schedule} /
 * {@code hotwater#schedule} read channels and the {@code setChSchedule}/{@code setDhwSchedule} Thing
 * Actions.
 * <p>
 * {@code days.<weekday>} array order is exactly {@code entries[dayIndex]} order, unmodified — a
 * caller (the schedule-editing UI this exists for) resolves "which period did I just edit" purely
 * from its position in this array, then calls {@code setChSchedulePeriod}/etc. with that same index.
 * Reordering or filtering periods here would silently misdirect a caller's next edit onto the wrong
 * period on the live boiler.
 *
 * @author Florian Lettner - Initial contribution
 */
@NonNullByDefault
public final class ScheduleJson {

    private static final Gson GSON = new GsonBuilder().create();

    /**
     * Upper bound on {@code parse}'s input, generous over a real full week (a few KB at most) but
     * small enough to reject a deliberately oversized payload before it's even handed to the parser.
     */
    private static final int MAX_INPUT_LENGTH = 16 * 1024;

    /** Upper bound on periods accepted for a single weekday — real schedules hold at most a handful. */
    private static final int MAX_PERIODS_PER_DAY = 100;

    private ScheduleJson() {
    }

    /**
     * Serializes a schedule to the documented JSON shape — all seven weekdays always present, in
     * monday..sunday order, each day's periods in {@code entries[dayIndex]} order unmodified.
     */
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

    /**
     * Parses a (possibly partial) schedule write. Weekdays absent from {@code days} are filled from
     * {@code currentEntries} unchanged — the device requires the whole schedule object on every
     * write regardless, so this lets a caller save just the day(s) it actually edited in one call.
     *
     * @return the composed schedule ready to send, or {@code null} if the input is malformed, exceeds
     *         {@link #MAX_INPUT_LENGTH} or {@link #MAX_PERIODS_PER_DAY}, names an unrecognized
     *         weekday, names a weekday beyond {@code currentEntries.length}, contains a period failing
     *         {@link #isValidPeriod}, or contains two periods on the same weekday that
     *         {@link #periodsOverlap} — rejected outright rather than trimmed or reordered, since
     *         resolving a conflict is a caller policy decision, not this codec's to make
     */
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
            // Gson's recursive-descent parser has no nesting-depth limit of its own; a deliberately
            // deeply-nested payload (still well under MAX_INPUT_LENGTH — nesting is compact) would
            // otherwise blow the calling thread's stack instead of failing this call cleanly.
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
                Integer weekdayNumber = WEEKDAY_BY_NAME.get(dayEntry.getKey().toLowerCase());
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

    /**
     * {@code 0 <= start < end <= 1440} — matches every observed device schedule (never wraps
     * midnight, never zero-length). Applied to both this codec and the four per-period Thing Actions
     * in {@link AtagOneHandler#composeSchedulePeriodChange}, so both write paths reject the same
     * malformed period.
     */
    public static boolean isValidPeriod(double startMinutes, double endMinutes) {
        return startMinutes >= 0 && endMinutes <= 1440 && startMinutes < endMinutes;
    }

    /**
     * True if half-open intervals {@code [aStart, aEnd)} and {@code [bStart, bEnd)} overlap — a
     * period ending exactly when another starts does <em>not</em> count as overlapping. Rejection
     * only, never resolution: which period should yield on a conflict is a caller/UI policy decision,
     * not one either write path makes for the caller. Used both here (whole-schedule writes reject if
     * any two periods within the same weekday overlap each other) and by
     * {@link AtagOneHandler#composeSchedulePeriodChange} (a per-period write rejects if the new/edited
     * period overlaps any <em>other</em> period already on that weekday).
     */
    public static boolean periodsOverlap(double aStart, double aEnd, double bStart, double bEnd) {
        return aStart < bEnd && aEnd > bStart;
    }
}
