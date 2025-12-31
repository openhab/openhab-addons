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

import static org.junit.jupiter.api.Assertions.*;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.astro.internal.model.EclipseKind;
import org.openhab.binding.astro.internal.util.DateTimeUtils;

/***
 * Specific unit tests to check if {@link MoonCalc} generates correct data for
 * Amsterdam city on 27 February 2019. In particular the following cases are
 * covered:
 * <ul>
 * <li>checks if generated data are the same (with some accuracy) as produced by
 * haevens-above.com</li>
 * </ul>
 *
 * @author Leo Siepel - Initial contribution
 * @see <a href="https://www.heavens-above.com/Moon.aspx">Heavens Above Moon</a>
 */
public class SunMoonEclipseCalcTest {

    private EclipseCalc moonEclipseCalc;
    private EclipseCalc sunEclipseCalc;
    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");

    @BeforeEach
    public void init() {
        moonEclipseCalc = new MoonEclipseCalc();
        sunEclipseCalc = new SunEclipseCalc();
    }

    @Test
    public void testTotalLunarEclipse2019() {
        // Total Lunar Eclipse on January 21, 2019
        Calendar calendar = new GregorianCalendar(UTC, Locale.US);
        calendar.set(2018, Calendar.DECEMBER, 1, 0, 0, 0);
        double midnightJd = DateTimeUtils.midnightDateToJulianDate(calendar);

        double eclipseJd2 = moonEclipseCalc.calculate(calendar, midnightJd, EclipseKind.TOTAL);
        Calendar eclipseDate = DateTimeUtils.toCalendar(eclipseJd2, UTC, Locale.US);

        assertNotNull(eclipseDate);
        assertEquals(2019, eclipseDate.get(Calendar.YEAR));
        assertEquals(Calendar.JANUARY, eclipseDate.get(Calendar.MONTH));
        assertEquals(21, eclipseDate.get(Calendar.DAY_OF_MONTH));
        // Approximate time check (around 05:12 UTC)
        assertEquals(5, eclipseDate.get(Calendar.HOUR_OF_DAY));
    }

    @Test
    public void testTotalSolarEclipse2017() {
        // Total Solar Eclipse on August 21, 2017
        Calendar calendar = new GregorianCalendar(UTC, Locale.US);
        calendar.set(2017, Calendar.JANUARY, 1, 0, 0, 0);
        double midnightJd = DateTimeUtils.midnightDateToJulianDate(calendar);

        double eclipseJd2 = sunEclipseCalc.calculate(calendar, midnightJd, EclipseKind.TOTAL);
        Calendar eclipseDate = DateTimeUtils.toCalendar(eclipseJd2, UTC, Locale.US);

        assertNotNull(eclipseDate);
        assertEquals(2017, eclipseDate.get(Calendar.YEAR));
        assertEquals(Calendar.AUGUST, eclipseDate.get(Calendar.MONTH));
        assertEquals(21, eclipseDate.get(Calendar.DAY_OF_MONTH));
        // Approximate time check (around 18:26 UTC)
        assertEquals(18, eclipseDate.get(Calendar.HOUR_OF_DAY));
    }

    @Test
    public void testPartialSolarEclipse2018() {
        // Partial Solar Eclipse on August 11, 2018
        Calendar calendar = new GregorianCalendar(UTC, Locale.US);
        calendar.set(2018, Calendar.JULY, 15, 0, 0, 0);
        double midnightJd = DateTimeUtils.midnightDateToJulianDate(calendar);

        double eclipseJd = sunEclipseCalc.calculate(calendar, midnightJd, EclipseKind.PARTIAL);
        Calendar eclipseDate = DateTimeUtils.toCalendar(eclipseJd, UTC, Locale.US);

        assertNotNull(eclipseDate);
        assertEquals(2018, eclipseDate.get(Calendar.YEAR));
        assertEquals(Calendar.AUGUST, eclipseDate.get(Calendar.MONTH));
        assertEquals(11, eclipseDate.get(Calendar.DAY_OF_MONTH));
    }

    @Test
    public void testPartialLunarEclipse2019() {
        // Partial Lunar Eclipse on July 16, 2019
        Calendar calendar = new GregorianCalendar(UTC, Locale.US);
        calendar.set(2019, Calendar.JUNE, 1, 0, 0, 0);
        double midnightJd = DateTimeUtils.midnightDateToJulianDate(calendar);

        double eclipseJd = moonEclipseCalc.calculate(calendar, midnightJd, EclipseKind.PARTIAL);
        Calendar eclipseDate = DateTimeUtils.toCalendar(eclipseJd, UTC, Locale.US);

        assertNotNull(eclipseDate);
        assertEquals(2019, eclipseDate.get(Calendar.YEAR));
        assertEquals(Calendar.JULY, eclipseDate.get(Calendar.MONTH));
        assertEquals(16, eclipseDate.get(Calendar.DAY_OF_MONTH));
    }

    @Test
    public void testRingSolarEclipse2019() {
        // Annular (Ring) Solar Eclipse on December 26, 2019
        Calendar calendar = new GregorianCalendar(UTC, Locale.US);
        calendar.set(2019, Calendar.NOVEMBER, 1, 0, 0, 0);
        double midnightJd = DateTimeUtils.midnightDateToJulianDate(calendar);

        double eclipseJd = sunEclipseCalc.calculate(calendar, midnightJd, EclipseKind.RING);
        Calendar eclipseDate = DateTimeUtils.toCalendar(eclipseJd, UTC, Locale.US);

        assertNotNull(eclipseDate);
        assertEquals(2019, eclipseDate.get(Calendar.YEAR));
        assertEquals(Calendar.DECEMBER, eclipseDate.get(Calendar.MONTH));
        assertEquals(26, eclipseDate.get(Calendar.DAY_OF_MONTH));
    }
}
