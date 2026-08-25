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

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.time.Duration;
import java.time.Instant;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openhab.binding.astro.internal.calc.ZodiacCalc;
import org.openhab.binding.astro.internal.model.Zodiac;
import org.openhab.binding.astro.internal.model.ZodiacSign;

/**
 * Tests for lunar-range behaviour in {@link ZodiacCalc}.
 */
@NonNullByDefault
public class MoonZodiacCalcTest {
    private static final Instant REFERENCE_INSTANT = Instant.parse("2025-08-05T12:00:00Z");

    private static Stream<Arguments> zodiacAngles() {
        return Stream.of(arguments(Math.toRadians(0), ZodiacSign.ARIES),
                arguments(Math.toRadians(29.9), ZodiacSign.ARIES), arguments(Math.toRadians(30), ZodiacSign.TAURUS),
                arguments(Math.toRadians(59.9), ZodiacSign.TAURUS), arguments(Math.toRadians(90), ZodiacSign.CANCER),
                arguments(Math.toRadians(210), ZodiacSign.SCORPIO), arguments(Math.toRadians(330), ZodiacSign.PISCES),
                arguments(Math.toRadians(-0.1), ZodiacSign.PISCES));
    }

    @ParameterizedTest
    @MethodSource("zodiacAngles")
    public void testCalcZodiacSignFromLongitude(double longitude, ZodiacSign expected) {
        assertEquals(expected, ZodiacCalc.calculateMoon(longitude, REFERENCE_INSTANT).sign());
    }

    @Test
    public void testCalcZodiacCreatesMoonZodiacWithReasonableRange() {
        Zodiac zodiac = ZodiacCalc.calculateMoon(Math.toRadians(120), REFERENCE_INSTANT);
        assertEquals(ZodiacSign.LEO, zodiac.sign());
        var start = zodiac.getStart();
        var end = zodiac.getEnd();
        assertNotNull(start);
        assertNotNull(end);
        assertFalse(start.isAfter(REFERENCE_INSTANT));
        assertTrue(end.isAfter(REFERENCE_INSTANT));

        Duration length = Duration.between(start, end);
        // Moon sign should be roughly ~2.3 days; assert hours within reasonable bounds
        long hours = length.toHours();
        assertTrue(hours >= 48 && hours <= 72, "Lunar sign duration should be between 48 and 72 hours, was " + hours);
    }
}
