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
package org.openhab.binding.astro.internal.calc;

import java.time.Instant;
import java.util.TimeZone;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.astro.internal.model.Season;
import org.openhab.binding.astro.internal.util.DateTimeUtils;
import org.openhab.binding.astro.internal.util.MathUtils;

/**
 * Calculates the seasons of the year.
 *
 * @author Gerhard Riegler - Initial contribution
 * @implNote based on the calculations of http://stellafane.org/misc/equinox.html
 */
@NonNullByDefault
public class SeasonCalc {
    private static final int[] AMPLITUDE = new int[] { 485, 203, 199, 182, 156, 136, 77, 74, 70, 58, 52, 50, 45, 44, 29,
            18, 17, 16, 14, 12, 12, 12, 9, 8 };
    private static final double[] PHASE = new double[] { 324.96, 337.23, 342.08, 27.85, 73.14, 171.52, 222.54, 296.72,
            243.58, 119.81, 297.17, 21.02, 247.54, 325.15, 60.93, 155.12, 288.79, 198.04, 199.76, 95.39, 287.11, 320.81,
            227.73, 15.45 };
    private static final double[] FREQUENCY = new double[] { 1934.136, 32964.467, 20.186, 445267.112, 45036.886,
            22518.443, 65928.934, 3034.906, 9037.513, 33718.147, 150.678, 2281.226, 29929.562, 31555.956, 4443.417,
            67555.328, 4562.452, 62894.029, 31436.921, 14577.848, 31931.756, 34777.259, 1222.114, 16859.074 };

    /**
     * Returns the seasons of the year of the specified calendar.
     */
    public static Season calculate(int year, double latitude, boolean useMeteorologicalSeason, TimeZone zone) {
        return new Season(latitude, useMeteorologicalSeason, zone, calcEquiSol(3, year - 1), calcEquiSol(0, year),
                calcEquiSol(1, year), calcEquiSol(2, year), calcEquiSol(3, year), calcEquiSol(0, year + 1));
    }

    /**
     * Calculates the date of the season.
     */
    private static Instant calcEquiSol(int season, int year) {
        double estimate = calcInitial(season, year);
        double t = DateTimeUtils.toJulianCenturies(estimate);
        double w = 35999.373 * t - 2.47;
        double dl = 1 + 0.0334 * MathUtils.cosDeg(w) + 0.0007 * MathUtils.cosDeg(2 * w);
        double s = periodic24(t);
        double julianDate = estimate + ((0.00001 * s) / dl);
        return DateTimeUtils.jdToInstant(julianDate);
    }

    /**
     * Calculate an initial guess of the Equinox or Solstice of a given year.
     */
    private static double calcInitial(int season, int year) {
        double y = (year - 2000) / 1000d;
        return switch (season) {
            case 0 -> 2451623.80984 + 365242.37404 * y + 0.05169 * Math.pow(y, 2) - 0.00411 * Math.pow(y, 3)
                    - 0.00057 * Math.pow(y, 4);
            case 1 -> 2451716.56767 + 365241.62603 * y + 0.00325 * Math.pow(y, 2) + 0.00888 * Math.pow(y, 3)
                    - 0.00030 * Math.pow(y, 4);
            case 2 -> 2451810.21715 + 365242.01767 * y - 0.11575 * Math.pow(y, 2) + 0.00337 * Math.pow(y, 3)
                    + 0.00078 * Math.pow(y, 4);
            case 3 -> 2451900.05952 + 365242.74049 * y - 0.06223 * Math.pow(y, 2) - 0.00823 * Math.pow(y, 3)
                    + 0.00032 * Math.pow(y, 4);
            default -> throw new IllegalArgumentException("Unexpected value: " + season);
        };
    }

    /**
     * Calculate 24 periodic terms
     */
    private static double periodic24(double t) {
        double result = 0;
        for (int i = 0; i < 24; i++) {
            result += AMPLITUDE[i] * MathUtils.cosDeg(PHASE[i] + (FREQUENCY[i] * t));
        }
        return result;
    }
}
