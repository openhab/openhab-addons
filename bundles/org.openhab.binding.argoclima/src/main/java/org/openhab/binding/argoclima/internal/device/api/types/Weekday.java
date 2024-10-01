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
package org.openhab.binding.argoclima.internal.device.api.types;

import java.time.DayOfWeek;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Custom Day of Week class implementation (with integer values matching Argo API) and support of stacking into
 * EnumSet (flags-like)
 *
 * @implNote Ordering is important! The ordinal values start from 0 (0-SUN, 1-MON, ...) and are also used - for
 *           {@link org.openhab.binding.argoclima.internal.device.api.protocol.elements.CurrentWeekdayParam}
 *
 * @author Mateusz Bronk - Initial contribution
 */
@NonNullByDefault
public enum Weekday implements IArgoApiEnum {
    SUN(0x01), // ordinal: 0
    MON(0x02), // ordinal: 1
    TUE(0x04), // ordinal: 2
    WED(0x08), // ordinal: 3
    THU(0x10), // ordinal: 4
    FRI(0x20), // ordinal: 5
    SAT(0x40); // ordinal: 6

    private int value;

    Weekday(int intValue) {
        this.value = intValue;
    }

    @Override
    public int getIntValue() {
        return this.value;
    }

    /**
     * Maps {@link java.time.DayOfWeek java.time.DayOfWeek} to Argo API custom enum ({@link Weekday})
     *
     * @param d The DayOfWeek to convert
     * @return Argo-compatible Weekday for {@code d}
     */
    public static Weekday ofDay(DayOfWeek d) {
        switch (d) {
            case SUNDAY:
                return Weekday.SUN;
            case MONDAY:
                return Weekday.MON;
            case TUESDAY:
                return Weekday.TUE;
            case WEDNESDAY:
                return Weekday.WED;
            case THURSDAY:
                return Weekday.THU;
            case FRIDAY:
                return Weekday.FRI;
            case SATURDAY:
                return Weekday.SAT;
            default:
                throw new IllegalArgumentException("Invalid day of week");
        }
    }
}
