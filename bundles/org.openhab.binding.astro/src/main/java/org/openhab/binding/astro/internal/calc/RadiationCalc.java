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

import java.time.LocalDate;
import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.astro.internal.model.Radiation;
import org.openhab.binding.astro.internal.util.MathUtils;

/**
 * Calculates the color temperature and brightness depending upon sun positional information
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class RadiationCalc {
    private static final double SC = 1367; // Solar constant in W/m²

    public static Radiation calculate(ZonedDateTime now, double elevation, @Nullable Double altitude) {
        double altitudeRatio = (altitude != null) ? 1 / Math.pow((1 - (6.5 / 288) * (altitude / 1000.0)), 5.256) : 1;
        double sinAlpha = MathUtils.sinDeg(elevation);

        LocalDate date = now.toLocalDate();

        int dayOfYear = date.getDayOfYear();
        int daysInYear = date.lengthOfYear(); // 365 or 366

        // Direct Solar Radiation (in W/m²) at the atmosphere entry
        // At sunrise/sunset - calculations limits are reached
        double rOut = (elevation > 3 ? SC * (0.034 * Math.cos(MathUtils.TWO_PI * dayOfYear / daysInYear) + 1) : 0)
                * sinAlpha;

        // 0.6 = transmissivity coefficient
        double m = Math.pow(0.6, (Math.sqrt(1229 + Math.pow(614 * sinAlpha, 2)) - 614 * sinAlpha) * altitudeRatio);

        // Direct radiation after atmospheric layer
        double rDir = rOut * m;

        // Diffuse Radiation
        double rDiff = rOut * (0.271 - 0.294 * m);

        return new Radiation(rDir, rDiff);
    }
}
