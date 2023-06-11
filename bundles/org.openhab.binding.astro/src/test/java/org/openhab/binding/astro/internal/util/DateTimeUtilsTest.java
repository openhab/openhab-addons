/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.astro.internal.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.astro.internal.calc.SeasonCalc;
import org.openhab.binding.astro.internal.model.Season;

/**
 * Test class for {@link DateTimeUtils}.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
public class DateTimeUtilsTest {

    private static final TimeZone TIME_ZONE = TimeZone.getTimeZone("Europe/Amsterdam");
    private static final Calendar JAN_20_2020 = newCalendar(2020, Calendar.JANUARY, 20, 1, 0, TIME_ZONE);
    private static final Calendar MAY_20_2020 = newCalendar(2020, Calendar.MAY, 20, 1, 0, TIME_ZONE);
    private static final Calendar SEPT_20_2020 = newCalendar(2020, Calendar.SEPTEMBER, 20, 1, 0, TIME_ZONE);
    private static final Calendar DEC_10_2020 = newCalendar(2020, Calendar.DECEMBER, 1, 1, 0, TIME_ZONE);
    private static final Calendar DEC_10_2021 = newCalendar(2021, Calendar.DECEMBER, 1, 1, 0, TIME_ZONE);
    private static final double AMSTERDAM_LATITUDE = 52.367607;
    private static final double SYDNEY_LATITUDE = -33.87;

    private SeasonCalc seasonCalc;

    @BeforeEach
    public void init() {
        seasonCalc = new SeasonCalc();
    }

    @Test
    public void testGetSeasonAmsterdam() {
        final Season season = seasonCalc.getSeason(DEC_10_2020, AMSTERDAM_LATITUDE, true);

        assertNextSeason(season.getSpring(), 2020, JAN_20_2020, season);
        assertNextSeason(season.getSummer(), 2020, MAY_20_2020, season);
        assertNextSeason(season.getWinter(), 2020, SEPT_20_2020, season);
        assertNextSeason(seasonCalc.getSeason(DEC_10_2021, AMSTERDAM_LATITUDE, true).getSpring(), 2021, DEC_10_2020,
                season);
    }

    @Test
    public void testGetSeasonSydney() {
        final Season season = seasonCalc.getSeason(DEC_10_2020, SYDNEY_LATITUDE, true);

        assertNextSeason(season.getAutumn(), 2020, JAN_20_2020, season);
        assertNextSeason(season.getWinter(), 2020, MAY_20_2020, season);
        assertNextSeason(season.getSummer(), 2020, SEPT_20_2020, season);
        assertNextSeason(seasonCalc.getSeason(DEC_10_2021, SYDNEY_LATITUDE, true).getAutumn(), 2021, DEC_10_2020,
                season);
    }

    @Test
    void testTruncate() {
        Calendar cal = newCalendar(2021, 9, 30, 11, 54, TIME_ZONE);
        Calendar target = newCalendar(2021, 9, 30, 0, 0, TIME_ZONE);
        Calendar truncated = DateTimeUtils.truncateToMidnight(cal);
        assertEquals(truncated, target);
        Calendar endOfDay = DateTimeUtils.endOfDayDate(cal);
        Calendar target2 = new GregorianCalendar(2021, 9, 30, 23, 59, 59);
        target2.setTimeZone(TIME_ZONE);
        target2.set(Calendar.MILLISECOND, 999);
        assertEquals(endOfDay, target2);
    }

    private static void assertNextSeason(Calendar expectedSeason, int expectedYear, Calendar date, Season season) {
        final Calendar nextSeason = DateTimeUtils.getNext(date, season.getSpring(), season.getSummer(),
                season.getAutumn(), season.getWinter());
        assertEquals(expectedSeason, nextSeason, "Should return the expected season name.");
        assertEquals(expectedYear, nextSeason.get(Calendar.YEAR), "Should return the year matching the next season.");
    }

    private static Calendar newCalendar(int year, int month, int dayOfMonth, int hourOfDay, int minute, TimeZone zone) {
        Calendar result = new GregorianCalendar(year, month, dayOfMonth, hourOfDay, minute);
        result.setTimeZone(zone);

        return result;
    }
}
