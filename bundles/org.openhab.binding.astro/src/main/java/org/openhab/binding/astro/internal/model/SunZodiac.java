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
package org.openhab.binding.astro.internal.model;

import java.util.Calendar;

/**
 * Extends the zodiac with a date range.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class SunZodiac extends Zodiac {
    private Range range;

    /**
     * Creates a Zodiac with a sign and a range.
     */
    public SunZodiac(ZodiacSign sign, Range range) {
        super(sign);
        this.range = range;
    }

    /**
     * Returns she start of the zodiac.
     */
    public Calendar getStart() {
        return range == null ? null : range.getStart();
    }

    /**
     * Returns the end of the zodiac.
     */
    public Calendar getEnd() {
        return range == null ? null : range.getEnd();
    }

    /**
     * Returns true, if the zodiac is valid on the specified calendar object.
     */
    public boolean isValid(Calendar calendar) {
        if (range == null || range.getStart() == null || range.getEnd() == null) {
            return false;
        }

        return range.getStart().getTimeInMillis() <= calendar.getTimeInMillis()
                && range.getEnd().getTimeInMillis() >= calendar.getTimeInMillis();
    }
}
