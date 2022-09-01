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
package org.openhab.binding.hdpowerview.internal.api.v3;

import java.time.DayOfWeek;
import java.util.EnumSet;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hdpowerview.internal.api.responses.ScheduledEvent;

/**
 * class for scheduled event as returned by an HD PowerView Generation 3 hub.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class ScheduledEventV3 extends ScheduledEvent {
    // fields specific to Generation 3
    public int days;

    @Override
    public boolean equals(@Nullable Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof ScheduledEventV3)) {
            return false;
        }
        ScheduledEventV3 other = (ScheduledEventV3) o;

        return this.id == other.id && this.enabled == other.enabled && this.sceneId == other.sceneId
                && this.days == other.days && this.hour == other.hour && this.minute == other.minute;
    }

    @Override
    public final int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id;
        result = prime * result + (enabled ? 1 : 0);
        result = prime * result + sceneId;
        result = prime * result + days;
        result = prime * result + hour;
        result = prime * result + minute;
        return result;
    }

    @Override
    public EnumSet<DayOfWeek> getDays() {
        EnumSet<DayOfWeek> daySet = EnumSet.noneOf(DayOfWeek.class);
        if ((days & 0x01) != 0) {
            daySet.add(DayOfWeek.MONDAY);
        }
        if ((days & 0x02) != 0) {
            daySet.add(DayOfWeek.TUESDAY);
        }
        if ((days & 0x04) != 0) {
            daySet.add(DayOfWeek.WEDNESDAY);
        }
        if ((days & 0x08) != 0) {
            daySet.add(DayOfWeek.THURSDAY);
        }
        if ((days & 0x10) != 0) {
            daySet.add(DayOfWeek.FRIDAY);
        }
        if ((days & 0x20) != 0) {
            daySet.add(DayOfWeek.SATURDAY);
        }
        if ((days & 0x40) != 0) {
            daySet.add(DayOfWeek.SUNDAY);
        }
        return daySet;
    }
}
