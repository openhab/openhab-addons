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

import static org.openhab.binding.astro.internal.model.Circadian.*;

import java.util.Calendar;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.astro.internal.model.Circadian;
import org.openhab.binding.astro.internal.model.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Calculates the color temperature and brightness depending upon sun positional information
 *
 * @author Gaël L'hopital - Initial contribution
 * @implNote based on the calculations of
 *           https://github.com/claytonjn/hass-circadian_lighting/blob/ed03e159b9a1db8f08a94b7de8c3b6b73fa0eb92/README.md
 */
@NonNullByDefault
public class CircadianCalc {
    private static final long DELTA_TEMP = MAX_COLOR_TEMP - MIN_COLOR_TEMP;
    private static final long TWELVE_HOURS_MS = 12 * 60 * 60 * 1000;

    private static final Logger LOGGER = LoggerFactory.getLogger(CircadianCalc.class);

    public static Circadian calculate(Calendar calendar, Range riseRange, Range setRange, @Nullable Range noonRange) {
        var rise = riseRange.getStart();
        var set = setRange.getStart();
        var noon = noonRange != null ? noonRange.getStart() : null;

        // If we have no rise or no set, there's no point calculating a Circadian Cycle
        if (rise == null || set == null || noon == null) {
            return Circadian.DEFAULT;
        }
        return calculate(calendar, rise, set, noon);
    }

    public static Circadian calculate(Calendar calendar, Calendar rise, Calendar set, Calendar noon) {
        // Figure out where we are in time so we know which half of the parabola to calculate.
        // We're generating a different sunset-sunrise parabola for before and after solar midnight,
        // because solar midnight might not be exactly halfway between sunrise and sunset.
        // We're also generating a different parabola for sunrise-sunset.

        var now = calendar.getTimeInMillis();
        var sunRise = rise.getTimeInMillis();
        var sunSet = set.getTimeInMillis();

        long h, x;
        double k;

        if (sunRise < now && now < sunSet) {
            // Sunrise -> Sunset parabola
            k = 100.0;
            h = noon.getTimeInMillis();
            // parabola before solar_noon else after solar_noon
            x = now < h ? sunRise : sunSet;
        } else {
            k = -100.0;
            if (now < sunRise) {
                // Before sunrise we are still in the sunset -> sunrise cycle of the previous day
                h = noon.getTimeInMillis() - TWELVE_HOURS_MS;
                x = sunRise;
            } else {
                // sunset -> sunrise parabola
                h = noon.getTimeInMillis() + TWELVE_HOURS_MS;
                x = sunSet;
            }
        }

        double y = 0.0;
        long dx = h - x;
        if (dx == 0L) {
            LOGGER.debug("Degenerate circadian parabola (h == x), returning default values");
            return Circadian.DEFAULT;
        }
        double a = (y - k) / (dx * dx);
        double percentage = a * Math.pow(now - h, 2) + k;
        double colorTemp = percentage > 0 ? (DELTA_TEMP * percentage / 100) + MIN_COLOR_TEMP : MIN_COLOR_TEMP;

        LOGGER.debug("Percentage: {}, ColorTemp: {}", percentage, colorTemp);

        return new Circadian(Math.min(100, Math.abs(percentage)), colorTemp);
    }
}
