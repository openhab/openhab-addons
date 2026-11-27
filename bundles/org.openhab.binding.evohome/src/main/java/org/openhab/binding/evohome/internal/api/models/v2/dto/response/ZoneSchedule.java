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
package org.openhab.binding.evohome.internal.api.models.v2.dto.response;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

/**
 * Weekly heating schedule and upcoming switchpoints in the location's time zone.
 *
 * @author Kovacs Istvan - Initial contribution
 * @author Kovacs Istvan - Weekly program data and scheduled temperatures
 */
public class ZoneSchedule {
    @SerializedName("dailySchedules")
    private @Nullable List<@Nullable DailySchedule> dailySchedules;

    @NonNullByDefault
    public record ScheduledSwitchpoint(Instant time, double temperature) {
    }

    @NonNullByDefault
    private record ParsedSwitchpoint(LocalTime time, @Nullable Double temperature) {
    }

    @NonNullByDefault
    private record UpcomingSwitchpoint(Instant time, @Nullable Double temperature) {
    }

    /**
     * Returns the next future time, retaining compatibility with time-only schedules.
     */
    @NonNullByDefault
    public Instant nextSwitchpoint(Instant now, ZoneId timeZone) {
        return findNextSwitchpoint(now, timeZone, false).time();
    }

    /**
     * Validates all seven days, switchpoint times and scheduled temperatures.
     */
    @NonNullByDefault
    public void validate() {
        validatedSchedule(true);
    }

    /**
     * Returns the next future scheduled time and its unmodified programmed temperature.
     */
    @NonNullByDefault
    public ScheduledSwitchpoint nextScheduledSwitchpoint(Instant now, ZoneId timeZone) {
        UpcomingSwitchpoint next = findNextSwitchpoint(now, timeZone, true);
        return new ScheduledSwitchpoint(next.time(), Objects.requireNonNull(next.temperature()));
    }

    /**
     * Returns a validated JSON schedule ordered Monday to Sunday and by local switchpoint time.
     */
    @NonNullByDefault
    public String toJson() {
        var days = validatedSchedule(true);
        JsonArray schedules = new JsonArray();
        for (DayOfWeek day : DayOfWeek.values()) {
            JsonObject dailySchedule = new JsonObject();
            dailySchedule.addProperty("dayOfWeek", day.getDisplayName(TextStyle.FULL, Locale.ENGLISH));
            JsonArray switchpoints = new JsonArray();
            for (ParsedSwitchpoint point : Objects.requireNonNull(days.get(day))) {
                JsonObject switchpoint = new JsonObject();
                switchpoint.addProperty("timeOfDay", point.time().format(DateTimeFormatter.ISO_LOCAL_TIME));
                switchpoint.addProperty("heatSetpoint", Objects.requireNonNull(point.temperature()));
                switchpoints.add(switchpoint);
            }
            dailySchedule.add("switchpoints", switchpoints);
            schedules.add(dailySchedule);
        }
        JsonObject result = new JsonObject();
        result.add("dailySchedules", schedules);
        return result.toString();
    }

    @NonNullByDefault
    private EnumMap<DayOfWeek, List<ParsedSwitchpoint>> validatedSchedule(boolean requireTemperature) {
        List<@Nullable DailySchedule> schedule = dailySchedules;
        if (schedule == null) {
            throw new IllegalArgumentException("The zone has no schedule");
        }
        var days = new EnumMap<DayOfWeek, List<ParsedSwitchpoint>>(DayOfWeek.class);
        for (DailySchedule day : schedule) {
            String dayName = day == null ? null : day.dayOfWeek;
            if (day == null || dayName == null) {
                throw new IllegalArgumentException("The zone schedule has a missing day or switchpoint");
            }
            List<@Nullable Switchpoint> switchpoints = day.switchpoints;
            if (switchpoints == null || switchpoints.isEmpty()) {
                throw new IllegalArgumentException("The zone schedule has a missing day or switchpoint");
            }
            DayOfWeek dayOfWeek = DayOfWeek.valueOf(dayName.toUpperCase(Locale.ROOT));
            if (days.containsKey(dayOfWeek)) {
                throw new IllegalArgumentException("The zone schedule has duplicate days");
            }
            List<ParsedSwitchpoint> parsed = new ArrayList<>();
            var times = new HashSet<LocalTime>();
            for (Switchpoint switchpoint : switchpoints) {
                String timeOfDay = switchpoint == null ? null : switchpoint.timeOfDay;
                if (switchpoint == null || timeOfDay == null) {
                    throw new IllegalArgumentException("The zone schedule has a missing switchpoint time");
                }
                LocalTime time = LocalTime.parse(timeOfDay);
                if (!times.add(time) && requireTemperature) {
                    throw new IllegalArgumentException("The zone schedule has duplicate switchpoint times");
                }
                Double temperature = switchpoint.heatSetpoint;
                if (requireTemperature && (temperature == null || !Double.isFinite(temperature))) {
                    throw new IllegalArgumentException("The zone schedule has a missing or invalid temperature");
                }
                parsed.add(new ParsedSwitchpoint(time, temperature));
            }
            parsed.sort(Comparator.comparing(ParsedSwitchpoint::time));
            days.put(dayOfWeek, List.copyOf(parsed));
        }
        if (days.size() != 7) {
            throw new IllegalArgumentException("The zone schedule must contain all seven days");
        }
        return days;
    }

    @NonNullByDefault
    private UpcomingSwitchpoint findNextSwitchpoint(Instant now, ZoneId timeZone, boolean requireTemperature) {
        var days = validatedSchedule(requireTemperature);
        LocalDate today = now.atZone(timeZone).toLocalDate();
        UpcomingSwitchpoint next = null;
        for (int offset = 0; offset <= 7; offset++) {
            LocalDate date = today.plusDays(offset);
            for (ParsedSwitchpoint point : Objects.requireNonNull(days.get(date.getDayOfWeek()))) {
                Instant candidate = date.atTime(point.time()).atZone(timeZone).toInstant();
                if (candidate.isAfter(now) && (next == null || candidate.isBefore(next.time()))) {
                    next = new UpcomingSwitchpoint(candidate, point.temperature());
                }
            }
        }
        if (next == null) {
            throw new IllegalArgumentException("The zone schedule has no future switchpoint");
        }
        return next;
    }

    private static class DailySchedule {
        @SerializedName("dayOfWeek")
        private @Nullable String dayOfWeek;

        @SerializedName("switchpoints")
        private @Nullable List<@Nullable Switchpoint> switchpoints;
    }

    private static class Switchpoint {
        @SerializedName("timeOfDay")
        private @Nullable String timeOfDay;

        @SerializedName("heatSetpoint")
        private @Nullable Double heatSetpoint;
    }
}
