/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.unifiaccess.internal.dto;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;

import com.google.gson.annotations.SerializedName;

/**
 * Door Access Schedule for Access Policies.
 *
 * @author Dan Cunningham - Initial contribution
 */
public class AccessPolicySchedule {
    public String id;
    public Boolean isDefault;
    public String name;
    public String type;
    @SerializedName(value = "weekly", alternate = { "week_schedule" })
    public WeekSchedule weekly;
    public String holidayGroupId;
    public AccessPolicyHolidayGroup holidayGroup;
    public List<TimeRange> holidaySchedule;

    public boolean allowsAt(ZonedDateTime when) {
        Objects.requireNonNull(when, "when");
        // Holiday override
        if (holidayGroup != null && holidayGroup.isHoliday(when)) {
            if (holidaySchedule == null || holidaySchedule.isEmpty()) {
                return false;
            }
            final LocalTime t = when.toLocalTime();
            for (TimeRange r : holidaySchedule) {
                if (r != null && r.contains(t)) {
                    return true;
                }
            }
            return false;
        }
        final WeekSchedule ws = weekly;
        if (ws == null) {
            return false;
        }
        final var ranges = ws.rangesFor(when.getDayOfWeek());
        if (ranges == null || ranges.isEmpty()) {
            return false;
        }
        final LocalTime t = when.toLocalTime();
        for (TimeRange r : ranges) {
            if (r != null && r.contains(t)) {
                return true;
            }
        }
        return false;
    }

    public static class WeekSchedule {
        public List<TimeRange> sunday;
        public List<TimeRange> monday;
        public List<TimeRange> tuesday;
        public List<TimeRange> wednesday;
        public List<TimeRange> thursday;
        public List<TimeRange> friday;
        public List<TimeRange> saturday;

        public List<TimeRange> rangesFor(DayOfWeek dow) {
            return switch (dow) {
                case MONDAY -> monday;
                case TUESDAY -> tuesday;
                case WEDNESDAY -> wednesday;
                case THURSDAY -> thursday;
                case FRIDAY -> friday;
                case SATURDAY -> saturday;
                case SUNDAY -> sunday;
            };
        }
    }

    public static class TimeRange {
        public String startTime;
        public String endTime;

        /** LocalTime membership test using {@link UaTime#within}. */
        public boolean contains(LocalTime t) {
            final LocalTime s = UaTime.parseHhmmss(startTime);
            final LocalTime e = UaTime.parseHhmmss(endTime);
            return UaTime.within(t, s, e);
        }
    }
}
