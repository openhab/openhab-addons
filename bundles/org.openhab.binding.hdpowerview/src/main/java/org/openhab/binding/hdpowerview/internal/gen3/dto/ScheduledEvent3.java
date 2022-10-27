/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.hdpowerview.internal.gen3.dto;

import java.time.DayOfWeek;
import java.util.EnumSet;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hdpowerview.internal.api.responses.ScheduledEvents;

/**
 * class for scheduled event as returned by an HD PowerView Generation 3 Gateway.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class ScheduledEvent3 {
    public int id;
    public int type;
    public boolean enabled;
    public int hour;
    public int minute;
    public int sceneId;
    public @NonNullByDefault({}) String days;

    private static final int MON = 0x01;
    private static final int TUE = 0x02;
    private static final int WED = 0x04;
    private static final int THU = 0x08;
    private static final int FRI = 0x10;
    private static final int SAT = 0x20;
    private static final int SUN = 0x40;

    private static final int CLOCK_BASED = 0;
    private static final int BEFORE_SUNRISE = 2;
    private static final int BEFORE_SUNSET = 6;
    private static final int AFTER_SUNRISE = 10;
    private static final int AFTER_SUNSET = 14;

    @Override
    public boolean equals(@Nullable Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof ScheduledEvent3)) {
            return false;
        }
        ScheduledEvent3 other = (ScheduledEvent3) o;
        String days = this.days;

        return this.id == other.id && this.enabled == other.enabled && this.sceneId == other.sceneId
                && (days != null && days.equals(other.days)) && this.hour == other.hour && this.minute == other.minute;
    }

    public EnumSet<DayOfWeek> getDays() {
        EnumSet<DayOfWeek> daySet = EnumSet.noneOf(DayOfWeek.class);
        String days = this.days;
        if (days != null) {
            try {
                int daysInt = Integer.valueOf(days).intValue();
                if ((daysInt & MON) != 0) {
                    daySet.add(DayOfWeek.MONDAY);
                }
                if ((daysInt & TUE) != 0) {
                    daySet.add(DayOfWeek.TUESDAY);
                }
                if ((daysInt & WED) != 0) {
                    daySet.add(DayOfWeek.WEDNESDAY);
                }
                if ((daysInt & THU) != 0) {
                    daySet.add(DayOfWeek.THURSDAY);
                }
                if ((daysInt & FRI) != 0) {
                    daySet.add(DayOfWeek.FRIDAY);
                }
                if ((daysInt & SAT) != 0) {
                    daySet.add(DayOfWeek.SATURDAY);
                }
                if ((daysInt & SUN) != 0) {
                    daySet.add(DayOfWeek.SUNDAY);
                }
            } catch (NumberFormatException e) {
                // fall through
            }
        }
        return daySet;
    }

    public int getEventType() {
        switch (type) {
            case CLOCK_BASED:
                return ScheduledEvents.SCHEDULED_EVENT_TYPE_TIME;

            case BEFORE_SUNRISE:
            case AFTER_SUNRISE:
                // TODO handle before and after sunrise cases separately
                return ScheduledEvents.SCHEDULED_EVENT_TYPE_SUNRISE;

            case BEFORE_SUNSET:
            case AFTER_SUNSET:
                // TODO handle before and after sunset cases separately
                return ScheduledEvents.SCHEDULED_EVENT_TYPE_SUNSET;
        }
        return 0;
    }
}
