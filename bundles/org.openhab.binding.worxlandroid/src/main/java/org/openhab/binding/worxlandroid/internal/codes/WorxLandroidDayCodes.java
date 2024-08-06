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
package org.openhab.binding.worxlandroid.internal.codes;

import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link WorxLandroidDayCodes} hosts Landroid days of week
 *
 * @author Nils - Initial contribution
 */
@NonNullByDefault
public enum WorxLandroidDayCodes {
    SUNDAY(0, DayOfWeek.SUNDAY),
    MONDAY(1, DayOfWeek.MONDAY),
    TUESDAY(2, DayOfWeek.TUESDAY),
    WEDNESDAY(3, DayOfWeek.WEDNESDAY),
    THURSDAY(4, DayOfWeek.THURSDAY),
    FRIDAY(5, DayOfWeek.FRIDAY),
    SATURDAY(6, DayOfWeek.SATURDAY);

    public final int code;
    public final DayOfWeek dayOfWeek;

    WorxLandroidDayCodes(int code, DayOfWeek dayOfWeek) {
        this.code = code;
        this.dayOfWeek = dayOfWeek;
    }

    public String getDescription() {
        return dayOfWeek.getDisplayName(TextStyle.FULL, Locale.US);
    }
}
