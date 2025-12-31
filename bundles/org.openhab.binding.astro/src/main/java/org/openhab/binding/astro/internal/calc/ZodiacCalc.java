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

import java.time.Duration;
import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.astro.internal.model.Zodiac;
import org.openhab.binding.astro.internal.model.ZodiacSign;
import org.openhab.binding.astro.internal.util.AstroConstants;
import org.openhab.binding.astro.internal.util.MathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Calculates the zodiac sign from the current ecliptic longitude of the object (sun/moon).
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class ZodiacCalc {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZodiacCalc.class);

    /**
     * Returns a {@link Zodiac} built from the calculated sign for the given instant. The start and end instants are
     * estimated using the mean motion along the ecliptic.
     */
    public static Zodiac calculate(double eclipticLongitude, @Nullable Instant referenceInstant) {
        double normalizedLongitude = MathUtils.mod2Pi(eclipticLongitude);
        int index = (int) (normalizedLongitude / ZodiacSign.getRadiansPerSign());

        Instant start = null;
        Instant end = null;
        if (referenceInstant != null) {
            double radiansIntoSign = normalizedLongitude - index * ZodiacSign.getRadiansPerSign();
            start = referenceInstant.minus(angleToDuration(radiansIntoSign));
            end = referenceInstant.plus(angleToDuration(ZodiacSign.getRadiansPerSign() - radiansIntoSign));
        }
        try {
            return new Zodiac(index, start, end);
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Error defining Zodiac: {}", e.getMessage());
            return Zodiac.NULL;
        }
    }

    private static Duration angleToDuration(double angle) {
        long millis = Math.round((angle / AstroConstants.SOLAR_MEAN_MOTION_PER_SECOND) * 1000);
        if (millis < 0) {
            millis = 0;
        }
        return Duration.ofMillis(millis);
    }
}
