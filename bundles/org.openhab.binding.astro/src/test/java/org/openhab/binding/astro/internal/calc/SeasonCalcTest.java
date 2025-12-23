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

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.astro.internal.model.Season;
import org.openhab.binding.astro.internal.model.Sun;
import org.openhab.binding.astro.internal.util.DateTimeUtils;

/***
 * Specific unit tests to check if {@link SunCalc} generates correct data for
 * Amsterdam city on 27 February 2019. In particular the following cases are
 * covered:
 * <ul>
 * <li>checks if generated data are the same (with some accuracy) as produced by
 * haevens-above.com</li>
 * <li>checks if the generated {@link Sun#getAllRanges()} are consistent with
 * each other</li>
 * </ul>
 *
 * @author Witold Markowski - Initial contribution
 * @see <a href="https://github.com/openhab/openhab-addons/issues/5006">[astro]
 *      Sun Phase returns UNDEF</a>
 * @see <a href="https://www.heavens-above.com/sun.aspx">Heavens Above Sun</a>
 */
public class SeasonCalcTest {
    private static final TimeZone AMSTERDAM_TZ = TimeZone.getTimeZone("Europe/Amsterdam");
    private static final TimeZone SYDNEY_TZ = TimeZone.getTimeZone("Australia/Sydney");
    private static final Instant MAY_20_2020 = Instant.parse("2020-05-20T01:00:00Z");
    private static final Instant JAN_20_2020 = Instant.parse("2020-01-20T01:00:00Z");
    private static final Instant SEPT_20_2020 = Instant.parse("2020-09-20T01:00:00Z");
    private static final Instant DEC_10_2020 = Instant.parse("2020-12-10T01:00:00Z");
    private static final double AMSTERDAM_LATITUDE = 52.367607;
    private static final double SYDNEY_LATITUDE = -33.87;

    private static final Calendar FEB_27_2019 = SeasonCalcTest.newCalendar(2019, Calendar.FEBRUARY, 27, 1, 0,
            AMSTERDAM_TZ);
    private static final double AMSTERDAM_LONGITUDE = 4.8978293;
    private static final double AMSTERDAM_ALTITUDE = 0.0;

    private SunCalc sunCalc;

    @BeforeEach
    public void init() {
        sunCalc = new SunCalc();
    }

    /***
     * Constructs a <code>GregorianCalendar</code> with the given date and time set
     * for the provided time zone.
     *
     * @param year
     *            the value used to set the <code>YEAR</code> calendar field in the
     *            calendar.
     * @param month
     *            the value used to set the <code>MONTH</code> calendar field in the
     *            calendar. Month value is 0-based. e.g., 0 for January.
     * @param dayOfMonth
     *            the value used to set the <code>DAY_OF_MONTH</code> calendar field
     *            in the calendar.
     * @param hourOfDay
     *            the value used to set the <code>HOUR_OF_DAY</code> calendar field
     *            in the calendar.
     * @param minute
     *            the value used to set the <code>MINUTE</code> calendar field in
     *            the calendar.
     * @param zone
     *            the given time zone.
     * @return
     */
    private static Calendar newCalendar(int year, int month, int dayOfMonth, int hourOfDay, int minute, TimeZone zone) {
        Calendar result = new GregorianCalendar(zone, Locale.ROOT);
        result.set(year, month, dayOfMonth, hourOfDay, minute);

        return DateTimeUtils.truncateToMinute(result);
    }

    private static void assertNextSeason(Instant expectedSeason, int expectedYear, Instant date, Season season,
            TimeZone tz) {
        final Instant nextSeason = season.getNext(date);
        assertEquals(expectedSeason, nextSeason, "Should return the expected season name.");
        assertNotNull(nextSeason);
        int year = nextSeason.atZone(tz.toZoneId()).getYear();
        assertEquals(expectedYear, year, "Should return the year matching the next season.");
    }

    @Test
    public void testAstroAndMeteoSeasons() {
        Sun meteoSun = sunCalc.getSunInfo(FEB_27_2019, AMSTERDAM_LATITUDE, AMSTERDAM_LONGITUDE, AMSTERDAM_ALTITUDE,
                true, AMSTERDAM_TZ, Locale.ROOT);
        Sun equiSun = sunCalc.getSunInfo(FEB_27_2019, AMSTERDAM_LATITUDE, AMSTERDAM_LONGITUDE, AMSTERDAM_ALTITUDE,
                false, AMSTERDAM_TZ, Locale.ROOT);

        ZonedDateTime cal = meteoSun.getSeason().getSpring().atZone(AMSTERDAM_TZ.toZoneId());
        assertNotNull(cal);
        ZonedDateTime cal2 = equiSun.getSeason().getSpring().atZone(AMSTERDAM_TZ.toZoneId());
        assertNotNull(cal2);
        assertEquals(cal.getMonth(), cal2.getMonth());
        assertEquals(cal.getYear(), cal2.getYear());
        assertEquals(1, cal.getDayOfMonth());
        assertFalse(cal.getDayOfMonth() == cal2.getDayOfMonth());
    }

    @Test
    public void testGetSeasonAmsterdam() {
        final Season season = SeasonCalc.calculate(2020, AMSTERDAM_LATITUDE, true, AMSTERDAM_TZ);

        assertNextSeason(season.getSpring(), 2020, JAN_20_2020, season, AMSTERDAM_TZ);
        assertNextSeason(season.getSummer(), 2020, MAY_20_2020, season, AMSTERDAM_TZ);
        assertNextSeason(season.getWinter(), 2020, SEPT_20_2020, season, AMSTERDAM_TZ);
        assertNextSeason(SeasonCalc.calculate(2021, AMSTERDAM_LATITUDE, true, AMSTERDAM_TZ).getSpring(), 2021,
                DEC_10_2020, season, AMSTERDAM_TZ);
    }

    @Test
    public void testGetSeasonSydney() {
        final Season season = SeasonCalc.calculate(2020, SYDNEY_LATITUDE, true, AMSTERDAM_TZ);

        assertNextSeason(season.getAutumn(), 2020, JAN_20_2020, season, SYDNEY_TZ);
        assertNextSeason(season.getWinter(), 2020, MAY_20_2020, season, SYDNEY_TZ);
        assertNextSeason(season.getSummer(), 2020, SEPT_20_2020, season, SYDNEY_TZ);
        assertNextSeason(SeasonCalc.calculate(2021, SYDNEY_LATITUDE, true, AMSTERDAM_TZ).getAutumn(), 2021, DEC_10_2020,
                season, SYDNEY_TZ);
    }
}
