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

import static org.junit.jupiter.api.Assertions.*;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.astro.internal.model.Circadian;
import org.openhab.binding.astro.internal.model.Range;

/**
 * Tests for {@link CircadianCalc}.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class CircadianCalcTest {

    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");

    @Test
    public void calculateUsesPreviousSolarMidnightBeforeSunrise() {
        Calendar noon = newCalendar(2024, Calendar.JANUARY, 1, 13, 0);
        Calendar sunrise = newCalendar(2024, Calendar.JANUARY, 1, 7, 0);
        Calendar sunset = newCalendar(2024, Calendar.JANUARY, 1, 19, 0);

        Circadian beforeSunrise = CircadianCalc.calculate(newCalendar(2024, Calendar.JANUARY, 1, 5, 0), sunrise, sunset,
                noon);
        assertEquals(56, beforeSunrise.brightness());
        Circadian afterSunset = CircadianCalc.calculate(newCalendar(2024, Calendar.JANUARY, 1, 21, 0), sunrise, sunset,
                noon);
        assertEquals(afterSunset, beforeSunrise);

        assertEquals(2500, beforeSunrise.temperature());
    }

    @Test
    public void calculateUsesRiseAndSetRangeStarts() {
        Calendar noon = newCalendar(2024, Calendar.JANUARY, 1, 13, 0);
        Calendar sunrise = newCalendar(2024, Calendar.JANUARY, 1, 7, 0);
        Calendar sunset = newCalendar(2024, Calendar.JANUARY, 1, 19, 0);
        Calendar now = newCalendar(2024, Calendar.JANUARY, 1, 21, 0);

        Range riseRange = new Range(sunrise, newCalendar(2024, Calendar.JANUARY, 1, 8, 0));
        Range setRange = new Range(sunset, newCalendar(2024, Calendar.JANUARY, 1, 20, 0));
        Range noonRange = new Range(noon, newCalendar(2024, Calendar.JANUARY, 1, 14, 0));

        Circadian expected = CircadianCalc.calculate(now, sunrise, sunset, noon);
        Circadian actual = CircadianCalc.calculate(now, riseRange, setRange, noonRange);

        assertEquals(expected, actual);
        assertNotEquals(CircadianCalc.calculate(now, sunrise, sunrise, noon), actual);

        actual = CircadianCalc.calculate(now, new Range(), new Range(), null);
        assertEquals(Circadian.DEFAULT, actual);
    }

    private static Calendar newCalendar(int year, int month, int day, int hour, int minute) {
        Calendar calendar = new GregorianCalendar(UTC, Locale.ROOT);
        calendar.clear();
        calendar.set(year, month, day, hour, minute, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }
}
