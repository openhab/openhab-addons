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
package org.openhab.binding.astro.internal.calc.zodiac;

import java.util.Calendar;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.astro.internal.model.Range;
import org.openhab.binding.astro.internal.model.ZodiacSign;

/**
 * Extends the zodiac with a date range.
 *
 * @author Gerhard Riegler - Initial contribution
 */
@NonNullByDefault
public class SunZodiac {
    private final @Nullable ZodiacSign sign;

    private Range range;

    /**
     * Creates a Zodiac with a sign and a range.
     */
    public SunZodiac(ZodiacSign sign, Range range) {
        this.sign = sign;
        this.range = range;
    }

    /**
     * Returns she start of the zodiac.
     */
    @Nullable
    public Calendar getStart() {
        return range.getStart();
    }

    /**
     * Returns the end of the zodiac.
     */
    @Nullable
    public Calendar getEnd() {
        return range.getEnd();
    }

    @Nullable
    public ZodiacSign getSign() {
        return sign;
    }

    /**
     * Returns true, if the zodiac is valid on the specified calendar object.
     */
    public boolean isValid(Calendar calendar) {
        Calendar start = range.getStart();
        Calendar end = range.getEnd();
        if (start == null || end == null) {
            return false;
        }

        return start.getTimeInMillis() <= calendar.getTimeInMillis()
                && end.getTimeInMillis() >= calendar.getTimeInMillis();
    }
}
