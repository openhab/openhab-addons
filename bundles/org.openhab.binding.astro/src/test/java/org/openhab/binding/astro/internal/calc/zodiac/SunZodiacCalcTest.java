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
package org.openhab.binding.astro.internal.calc.zodiac;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.time.Duration;
import java.time.Instant;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.stream.Stream;

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
public class SunZodiacCalcTest {

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
        assertEquals(expected, ZodiacCalc.calculate(longitude, null).getSign());
    }

    @Test
    public void testCalcZodiacCreatesSunZodiacWithRangeFromInstant() {
        Instant referenceInstant = Instant.parse("2025-08-05T12:00:00Z");
        Zodiac zodiac = ZodiacCalc.calculate(Math.toRadians(120), referenceInstant);
        assertEquals(ZodiacSign.LEO, zodiac.getSign());
        var start = zodiac.getStart();
        var end = zodiac.getEnd();
        assertNotNull(start);
        assertNotNull(end);
        assertFalse(start.isAfter(referenceInstant));
        assertTrue(end.isAfter(referenceInstant));

        Duration length = Duration.between(start, end);
        assertTrue(length.toDays() >= 29 && length.toDays() <= 32);
    }

    private static Stream<Arguments> sunZodiacCalcComparison() {
        double offset = ZodiacSign.getRadiansPerSign() / 2;
        return Stream.of(
                arguments(ZodiacSign.ARIES, newCalendar(2025, Calendar.APRIL, 5),
                        offset + 0 * ZodiacSign.getRadiansPerSign()),
                arguments(ZodiacSign.TAURUS, newCalendar(2025, Calendar.MAY, 5),
                        offset + 1 * ZodiacSign.getRadiansPerSign()),
                arguments(ZodiacSign.GEMINI, newCalendar(2025, Calendar.JUNE, 5),
                        offset + 2 * ZodiacSign.getRadiansPerSign()),
                arguments(ZodiacSign.CANCER, newCalendar(2025, Calendar.JULY, 5),
                        offset + 3 * ZodiacSign.getRadiansPerSign()),
                arguments(ZodiacSign.LEO, newCalendar(2025, Calendar.AUGUST, 5),
                        offset + 4 * ZodiacSign.getRadiansPerSign()),
                arguments(ZodiacSign.VIRGO, newCalendar(2025, Calendar.SEPTEMBER, 5),
                        offset + 5 * ZodiacSign.getRadiansPerSign()),
                arguments(ZodiacSign.LIBRA, newCalendar(2025, Calendar.OCTOBER, 5),
                        offset + 6 * ZodiacSign.getRadiansPerSign()),
                arguments(ZodiacSign.SCORPIO, newCalendar(2025, Calendar.NOVEMBER, 5),
                        offset + 7 * ZodiacSign.getRadiansPerSign()),
                arguments(ZodiacSign.SAGITTARIUS, newCalendar(2025, Calendar.DECEMBER, 5),
                        offset + 8 * ZodiacSign.getRadiansPerSign()),
                arguments(ZodiacSign.CAPRICORN, newCalendar(2025, Calendar.JANUARY, 10),
                        offset + 9 * ZodiacSign.getRadiansPerSign()),
                arguments(ZodiacSign.AQUARIUS, newCalendar(2025, Calendar.FEBRUARY, 5),
                        offset + 10 * ZodiacSign.getRadiansPerSign()),
                arguments(ZodiacSign.PISCES, newCalendar(2025, Calendar.MARCH, 5),
                        offset + 11 * ZodiacSign.getRadiansPerSign()));
    }

    @ParameterizedTest
    @MethodSource("sunZodiacCalcComparison")
    public void testPositionBasedResultMatchesDateBased(ZodiacSign expectedSign, Calendar date, double longitude) {
        OldSunZodiacCalc dateCalc = new OldSunZodiacCalc(TimeZone.getTimeZone("UTC"), Locale.ROOT);

        ZodiacSign dateBasedSign = dateCalc.getZodiac(date).map(SunZodiac::getSign).orElseThrow();
        ZodiacSign positionBasedSign = ZodiacCalc.calculate(longitude, null).getSign();

        assertEquals(expectedSign, dateBasedSign);
        assertEquals(expectedSign, positionBasedSign);
    }

    private static Calendar newCalendar(int year, int month, int day) {
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"), Locale.ROOT);
        cal.set(year, month, day, 12, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal;
    }
}
