/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.astro.internal.calc;

import java.util.Calendar;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.astro.internal.model.Circadian;
import org.openhab.binding.astro.internal.model.Range;

/**
 * Calculates the color temperature and brightness depending upon sun positional information
 *
 * @author Gaël L'hopital - Initial contribution
 * @implNote based on the calculations of
 *           https://github.com/claytonjn/hass-circadian_lighting/blob/ed03e159b9a1db8f08a94b7de8c3b6b73fa0eb92/README.md
 */
@NonNullByDefault
public class CircadianCalc {
    private static final long MIN_COLOR_TEMP = 2500;
    private static final long MAX_COLOR_TEMP = 5500;
    private static final long DELTA_TEMP = MAX_COLOR_TEMP - MIN_COLOR_TEMP;

    public Circadian calculate(Calendar calendar, @Nullable Calendar rise, @Nullable Calendar set,
            @Nullable Range noonRange, @Nullable Range midnightRange) {
        if (rise == null || set == null) {
            return Circadian.DEFAULT;
        }

        // Figure out where we are in time so we know which half of the parabola to calculate.
        // We're generating a different sunset-sunrise parabola for before and after solar midnight.
        // because it might not be half way between sunrise and sunset.
        // We're also generating a different parabola for sunrise-sunset.

        var now = calendar.getTimeInMillis();
        var sunRise = rise.getTimeInMillis();
        var sunSet = set.getTimeInMillis();

        long h, x;
        double k;

        if (sunRise < now && now < sunSet && noonRange instanceof Range range
                && range.getStart() instanceof Calendar noon) {
            // Sunrise -> Sunset parabola
            h = noon.getTimeInMillis();
            k = 100.0;
            // parabola before solar_noon else after solar_noon
            x = now < h ? sunRise : sunSet;
        } else if (sunSet < now && now < sunRise && midnightRange instanceof Range range
                && range.getStart() instanceof Calendar midnight) {
            // sunset -> sunrise parabola
            h = midnight.getTimeInMillis();
            k = -100.0;
            // parabola before solar_midnight else after solar_midnight
            x = now < h ? sunSet : sunRise;
        } else {
            return Circadian.DEFAULT;
        }

        double y = 0.0;
        double a = (y - k) / Math.pow(h - x, 2);
        double percentage = a * Math.pow(now - h, 2) + k;
        double colorTemp = percentage > 0 ? (DELTA_TEMP * percentage / 100) + MIN_COLOR_TEMP : MIN_COLOR_TEMP;

        return new Circadian(Math.abs(percentage), colorTemp);
    }
}
