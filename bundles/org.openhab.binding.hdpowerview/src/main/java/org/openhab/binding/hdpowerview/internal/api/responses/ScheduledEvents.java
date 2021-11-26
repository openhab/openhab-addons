/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.hdpowerview.internal.api.responses;

import java.time.DayOfWeek;
import java.util.EnumSet;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * State of all Scheduled Events in an HD PowerView hub
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class ScheduledEvents {

    public static final EnumSet<DayOfWeek> WEEKDAYS = EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY);

    public static final EnumSet<DayOfWeek> WEEKENDS = EnumSet.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);

    public static final int SCHEDULED_EVENT_TYPE_TIME = 0;
    public static final int SCHEDULED_EVENT_TYPE_SUNRISE = 1;
    public static final int SCHEDULED_EVENT_TYPE_SUNSET = 2;

    public @Nullable List<ScheduledEvent> scheduledEventData;
    public @Nullable List<Integer> scheduledEventIds;

    /*
     * the following SuppressWarnings annotation is because the Eclipse compiler
     * does NOT expect a NonNullByDefault annotation on the inner class, since it is
     * implicitly inherited from the outer class, whereas the Maven compiler always
     * requires an explicit NonNullByDefault annotation on all classes
     */
    @SuppressWarnings("null")
    @NonNullByDefault
    public static class ScheduledEvent {
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
}
