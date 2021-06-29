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
package org.openhab.binding.cul.max.internal.messages.constants;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * @author Johannes Goehr (johgoe) - Initial contribution
 */
@NonNullByDefault
public enum MaxCulWeekdays {

    SATURDAY(0, "Sat"),
    SUNDAY(1, "Sun"),
    MONDAY(2, "Mon"),
    TUESDAY(3, "Tue"),
    WENDSDAY(4, "Wen"),
    THURSDAY(5, "Thu"),
    FRIDAY(6, "Fri"),
    UNKNOWN(-1, "n/a");

    private final int weekDayIndex;
    private final String shortName;

    private MaxCulWeekdays(int idx, String shortName) {
        weekDayIndex = idx;
        this.shortName = shortName;
    }

    public int getDayIndexInt() {
        return weekDayIndex;
    }

    public String getDayShortName() {
        return shortName;
    }

    public static MaxCulWeekdays getWeekDayFromInt(int idx) {
        for (int i = 0; i < MaxCulWeekdays.values().length; i++) {
            if (MaxCulWeekdays.values()[i].getDayIndexInt() == idx)
                return MaxCulWeekdays.values()[i];
        }
        return UNKNOWN;
    }

    public static MaxCulWeekdays getWeekDayFromShortName(String shortName) {
        for (int i = 0; i < MaxCulWeekdays.values().length; i++) {
            if (MaxCulWeekdays.values()[i].getDayShortName().equals(shortName))
                return MaxCulWeekdays.values()[i];
        }
        return UNKNOWN;
    }
}
