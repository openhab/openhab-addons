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

import java.time.Duration;
import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.astro.internal.model.Zodiac;
import org.openhab.binding.astro.internal.model.ZodiacSign;
import org.openhab.binding.astro.internal.util.AstroConstants;
import org.openhab.binding.astro.internal.util.MathUtils;

/**
 * Calculates the zodiac sign from the current ecliptic longitude of the object (sun/moon).
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class ZodiacCalc {
    public static Zodiac calculateSun(double eclipticLongitude, Instant referenceInstant) {
        return calculate(eclipticLongitude, referenceInstant, AstroConstants.SOLAR_MEAN_MOTION_PER_SECOND);
    }

    public static Zodiac calculateMoon(double eclipticLongitude, Instant referenceInstant) {
        return calculate(eclipticLongitude, referenceInstant, AstroConstants.LUNAR_MEAN_MOTION_PER_SECOND);
    }

    /**
     * Body-specific calculation: uses the provided mean motion (radians per second)
     * to estimate start/end instants for the sign range.
     */
    private static Zodiac calculate(double eclipticLongitude, Instant referenceInstant, double meanMotionPerSecond) {
        double normalizedLongitude = MathUtils.mod2Pi(eclipticLongitude);
        double radiansPerSign = ZodiacSign.getRadiansPerSign();
        int index = (int) (normalizedLongitude / radiansPerSign);

        double radiansIntoSign = normalizedLongitude - index * radiansPerSign;
        Instant start = referenceInstant.minus(angleToDuration(radiansIntoSign, meanMotionPerSecond));
        Instant end = referenceInstant.plus(angleToDuration(radiansPerSign - radiansIntoSign, meanMotionPerSecond));
        return new Zodiac(index, start, end);
    }

    private static Duration angleToDuration(double angle, double meanMotionPerSecond) {
        long seconds = Math.round(angle / meanMotionPerSecond);
        seconds = Math.max(0, seconds);
        return Duration.ofSeconds(seconds);
    }
}
