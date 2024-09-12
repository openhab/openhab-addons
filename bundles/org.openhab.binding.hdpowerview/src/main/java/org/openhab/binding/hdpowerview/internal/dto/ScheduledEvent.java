/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.hdpowerview.internal.dto;

import java.time.DayOfWeek;
import java.util.EnumSet;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * State of a single Scheduled Event, as returned by an HD PowerView Hub
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class ScheduledEvent {
    public static final EnumSet<DayOfWeek> WEEKDAYS = EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY);

    public static final EnumSet<DayOfWeek> WEEKENDS = EnumSet.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);

    public static final int SCHEDULED_EVENT_TYPE_TIME = 0;
    public static final int SCHEDULED_EVENT_TYPE_SUNRISE = 1;
    public static final int SCHEDULED_EVENT_TYPE_SUNSET = 2;

    public int id;
    public boolean enabled;
    public int sceneId;
    public int sceneCollectionId;
    public boolean daySunday;
    public boolean dayMonday;
    public boolean dayTuesday;
    public boolean dayWednesday;
    public boolean dayThursday;
    public boolean dayFriday;
    public boolean daySaturday;
    public int eventType;
    public int hour;
    public int minute;

    @Override
    public boolean equals(@Nullable Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof ScheduledEvent)) {
            return false;
        }
        ScheduledEvent other = (ScheduledEvent) o;

        return this.id == other.id && this.enabled == other.enabled && this.sceneId == other.sceneId
                && this.sceneCollectionId == other.sceneCollectionId && this.daySunday == other.daySunday
                && this.dayMonday == other.dayMonday && this.dayTuesday == other.dayTuesday
                && this.dayWednesday == other.dayWednesday && this.dayThursday == other.dayThursday
                && this.dayFriday == other.dayFriday && this.daySaturday == other.daySaturday
                && this.eventType == other.eventType && this.hour == other.hour && this.minute == other.minute;
    }

    @Override
    public final int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id;
        result = prime * result + (enabled ? 1 : 0);
        result = prime * result + sceneId;
        result = prime * result + sceneCollectionId;
        result = prime * result + (daySunday ? 1 : 0);
        result = prime * result + (dayMonday ? 1 : 0);
        result = prime * result + (dayTuesday ? 1 : 0);
        result = prime * result + (dayWednesday ? 1 : 0);
        result = prime * result + (dayThursday ? 1 : 0);
        result = prime * result + (dayFriday ? 1 : 0);
        result = prime * result + (daySaturday ? 1 : 0);
        result = prime * result + eventType;
        result = prime * result + hour;
        result = prime * result + minute;

        return result;
    }

    public EnumSet<DayOfWeek> getDays() {
        EnumSet<DayOfWeek> days = EnumSet.noneOf(DayOfWeek.class);
        if (daySunday) {
            days.add(DayOfWeek.SUNDAY);
        }
        if (dayMonday) {
            days.add(DayOfWeek.MONDAY);
        }
        if (dayTuesday) {
            days.add(DayOfWeek.TUESDAY);
        }
        if (dayWednesday) {
            days.add(DayOfWeek.WEDNESDAY);
        }
        if (dayThursday) {
            days.add(DayOfWeek.THURSDAY);
        }
        if (dayFriday) {
            days.add(DayOfWeek.FRIDAY);
        }
        if (daySaturday) {
            days.add(DayOfWeek.SATURDAY);
        }
        return days;
    }
}
