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
package org.openhab.binding.hdpowerview.internal.api.responses._v3;

import java.time.DayOfWeek;
import java.util.EnumSet;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hdpowerview.internal.api.responses.ScheduledEvent;
import org.openhab.binding.hdpowerview.internal.api.responses.ScheduledEvents;

/**
 * class for scheduled event as returned by an HD PowerView Generation 3 hub.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class ScheduledEventV3 extends ScheduledEvent {
    // fields specific to Generation 3
    public @Nullable String days;

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
        if (!(o instanceof ScheduledEventV3)) {
            return false;
        }
        ScheduledEventV3 other = (ScheduledEventV3) o;
        String days = this.days;

        return this.id == other.id && this.enabled == other.enabled && this.sceneId == other.sceneId
                && (days != null && days.equals(other.days)) && this.hour == other.hour && this.minute == other.minute;
    }

    @Override
    public final int hashCode() {
        final int prime = 31;
        int result = 1;
        String days = this.days;
        result = prime * result + id;
        result = prime * result + (enabled ? 1 : 0);
        result = prime * result + sceneId;
        result = prime * result + (days != null ? days.hashCode() : 0);
        result = prime * result + hour;
        result = prime * result + minute;
        return result;
    }

    @Override
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

    @Override
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

    @Override
    public int version() {
        return 3;
    }
}
