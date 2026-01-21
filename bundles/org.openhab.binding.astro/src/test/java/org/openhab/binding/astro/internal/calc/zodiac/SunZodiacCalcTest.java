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
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
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
 * Tests for {@link ZodiacCalc}.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class SunZodiacCalcTest {
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
        assertEquals(expected, ZodiacCalc.calculate(longitude, REFERENCE_INSTANT).sign());
    }

    @Test
    public void testCalcZodiacCreatesSunZodiacWithRangeFromInstant() {
        Zodiac zodiac = ZodiacCalc.calculate(Math.toRadians(120), REFERENCE_INSTANT);
        assertEquals(ZodiacSign.LEO, zodiac.sign());
        var start = zodiac.start();
        var end = zodiac.end();
        assertNotNull(start);
        assertNotNull(end);
        assertFalse(start.isAfter(REFERENCE_INSTANT));
        assertTrue(end.isAfter(REFERENCE_INSTANT));

        Duration length = Duration.between(start, end);
        assertTrue(length.toDays() >= 29 && length.toDays() <= 32);
    }

    private static Stream<Arguments> sunZodiacCalcComparison() {
        double radiansPerSign = ZodiacSign.getRadiansPerSign();
        double offset = radiansPerSign / 2;
        return Stream.of(arguments(ZodiacSign.ARIES, "2025-04-05T12:00:00Z", offset + 0 * radiansPerSign),
                arguments(ZodiacSign.TAURUS, "2025-05-05T12:00:00Z", offset + 1 * radiansPerSign),
                arguments(ZodiacSign.GEMINI, "2025-06-05T12:00:00Z", offset + 2 * radiansPerSign),
                arguments(ZodiacSign.CANCER, "2025-07-05T12:00:00Z", offset + 3 * radiansPerSign),
                arguments(ZodiacSign.LEO, "2025-08-05T12:00:00Z", offset + 4 * radiansPerSign),
                arguments(ZodiacSign.VIRGO, "2025-09-05T12:00:00Z", offset + 5 * radiansPerSign),
                arguments(ZodiacSign.LIBRA, "2025-10-05T12:00:00Z", offset + 6 * radiansPerSign),
                arguments(ZodiacSign.SCORPIO, "2025-11-05T12:00:00Z", offset + 7 * radiansPerSign),
                arguments(ZodiacSign.SAGITTARIUS, "2025-12-05T12:00:00Z", offset + 8 * radiansPerSign),
                arguments(ZodiacSign.CAPRICORN, "2025-01-10T12:00:00Z", offset + 9 * radiansPerSign),
                arguments(ZodiacSign.AQUARIUS, "2025-02-05T12:00:00Z", offset + 10 * radiansPerSign),
                arguments(ZodiacSign.PISCES, "2025-03-05T12:00:00Z", offset + 11 * radiansPerSign));
    }

    @ParameterizedTest
    @MethodSource("sunZodiacCalcComparison")
    public void testPositionBasedResultMatchesDateBased(ZodiacSign expectedSign, Instant moment, double longitude) {
        var utc = TimeZone.getTimeZone("UTC");
        OldSunZodiacCalc dateCalc = new OldSunZodiacCalc(utc, Locale.ROOT);

        ZodiacSign dateBasedSign = dateCalc.getZodiac(toCalendar(moment, utc.toZoneId())).map(SunZodiac::getSign)
                .orElseThrow();
        ZodiacSign positionBasedSign = ZodiacCalc.calculate(longitude, Instant.EPOCH).sign();

        assertEquals(expectedSign, dateBasedSign);
        assertEquals(expectedSign, positionBasedSign);
    }

    private static Calendar toCalendar(Instant instant, ZoneId zoneId) {
        ZonedDateTime zdt = ZonedDateTime.ofInstant(instant, zoneId);
        return GregorianCalendar.from(zdt);
    }
}
