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

import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openhab.binding.astro.internal.model.Range;
import org.openhab.binding.astro.internal.model.Sun;
import org.openhab.binding.astro.internal.model.SunPhaseName;
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
public class SunCalcTest {

    private static final TimeZone AMSTERDAM_TIME_ZONE = TimeZone.getTimeZone("Europe/Amsterdam");
    private static final Calendar FEB_27_2019 = SunCalcTest.newCalendar(2019, Calendar.FEBRUARY, 27, 1, 0,
            AMSTERDAM_TIME_ZONE);
    private static final double AMSTERDAM_LATITUDE = 52.367607;
    private static final double AMSTERDAM_LONGITUDE = 4.8978293;
    private static final double AMSTERDAM_ALTITUDE = 0.0;
    private static final int ACCURACY_IN_MILLIS = 3 * 60 * 1000;
    private static final TimeZone BARROW_TIME_ZONE = TimeZone.getTimeZone("America/Anchorage");
    private static final double BARROW_LATITUDE = 71.2906;
    private static final double BARROW_LONGITUDE = -156.7886;
    private static final double BARROW_ALTITUDE = 0.0;
    private static final Calendar SUMMER_SOLSTICE_AMSTERDAM = SunCalcTest.newCalendar(2024, Calendar.JUNE, 21, 12, 0,
            AMSTERDAM_TIME_ZONE);
    private static final Calendar SUMMER_SOLSTICE_BARROW = SunCalcTest.newCalendar(2024, Calendar.JUNE, 21, 12, 0,
            BARROW_TIME_ZONE);

    private SunCalc sunCalc;

    @BeforeEach
    public void init() {
        sunCalc = new SunCalc();
    }

    @Test
    public void testGetSunInfoForOldDate() {
        Sun sun = sunCalc.getSunInfo(FEB_27_2019, AMSTERDAM_LATITUDE, AMSTERDAM_LONGITUDE, AMSTERDAM_ALTITUDE, false,
                AMSTERDAM_TIME_ZONE, Locale.ROOT);

        assertNotNull(sun.getNight());

        assertNotNull(sun.getAstroDawn());
        assertNotNull(sun.getNauticDawn());
        assertNotNull(sun.getCivilDawn());

        assertNotNull(sun.getRise());

        assertNotNull(sun.getDaylight());
        assertNotNull(sun.getNoon());
        assertNotNull(sun.getSet());

        assertNotNull(sun.getCivilDusk());
        assertNotNull(sun.getNauticDusk());
        assertNotNull(sun.getAstroDusk());
        assertNotNull(sun.getNight());

        assertNotNull(sun.getMorningNight());
        assertNotNull(sun.getEveningNight());

        // for an old date the phase should also be calculated
        assertNotNull(sun.getPhase().getName());
    }

    @Test
    public void testGetSunInfoForAstronomicalDawnAccuracy() {
        Sun sun = sunCalc.getSunInfo(FEB_27_2019, AMSTERDAM_LATITUDE, AMSTERDAM_LONGITUDE, AMSTERDAM_ALTITUDE, false,
                AMSTERDAM_TIME_ZONE, Locale.ROOT);

        Range range = sun.getAstroDawn();
        assertNotNull(range);
        Calendar cal = range.getStart();
        assertNotNull(cal);
        // expected result from haevens-above.com is 27 Feb 2019 05:39 till 06:18
        assertEquals(SunCalcTest.newCalendar(2019, Calendar.FEBRUARY, 27, 5, 39, AMSTERDAM_TIME_ZONE).getTimeInMillis(),
                cal.getTimeInMillis(), ACCURACY_IN_MILLIS);
        cal = range.getEnd();
        assertNotNull(cal);
        assertEquals(SunCalcTest.newCalendar(2019, Calendar.FEBRUARY, 27, 6, 18, AMSTERDAM_TIME_ZONE).getTimeInMillis(),
                cal.getTimeInMillis(), ACCURACY_IN_MILLIS);
    }

    @Test
    public void testGetSunInfoForNauticDawnAccuracy() {
        Sun sun = sunCalc.getSunInfo(FEB_27_2019, AMSTERDAM_LATITUDE, AMSTERDAM_LONGITUDE, AMSTERDAM_ALTITUDE, false,
                AMSTERDAM_TIME_ZONE, Locale.ROOT);

        Range range = sun.getNauticDawn();
        assertNotNull(range);
        Calendar cal = range.getStart();
        assertNotNull(cal);
        // expected result from haevens-above.com is 27 Feb 2019 06:18 till 06:58
        assertEquals(SunCalcTest.newCalendar(2019, Calendar.FEBRUARY, 27, 6, 18, AMSTERDAM_TIME_ZONE).getTimeInMillis(),
                cal.getTimeInMillis(), ACCURACY_IN_MILLIS);
        cal = range.getEnd();
        assertNotNull(cal);
        assertEquals(SunCalcTest.newCalendar(2019, Calendar.FEBRUARY, 27, 6, 58, AMSTERDAM_TIME_ZONE).getTimeInMillis(),
                cal.getTimeInMillis(), ACCURACY_IN_MILLIS);
    }

    @Test
    public void testGetSunInfoForCivilDawnAccuracy() {
        Sun sun = sunCalc.getSunInfo(FEB_27_2019, AMSTERDAM_LATITUDE, AMSTERDAM_LONGITUDE, AMSTERDAM_ALTITUDE, false,
                AMSTERDAM_TIME_ZONE, Locale.ROOT);

        Range range = sun.getCivilDawn();
        assertNotNull(range);
        Calendar cal = range.getStart();
        assertNotNull(cal);
        // expected result from haevens-above.com is 27 Feb 2019 06:58 till 07:32
        assertEquals(SunCalcTest.newCalendar(2019, Calendar.FEBRUARY, 27, 6, 58, AMSTERDAM_TIME_ZONE).getTimeInMillis(),
                cal.getTimeInMillis(), ACCURACY_IN_MILLIS);
        cal = range.getEnd();
        assertNotNull(cal);
        assertEquals(SunCalcTest.newCalendar(2019, Calendar.FEBRUARY, 27, 7, 32, AMSTERDAM_TIME_ZONE).getTimeInMillis(),
                cal.getTimeInMillis(), ACCURACY_IN_MILLIS);
    }

    @Test
    public void testGetSunInfoForRiseAccuracy() {
        Sun sun = sunCalc.getSunInfo(FEB_27_2019, AMSTERDAM_LATITUDE, AMSTERDAM_LONGITUDE, AMSTERDAM_ALTITUDE, false,
                AMSTERDAM_TIME_ZONE, Locale.ROOT);

        Range range = sun.getRise();
        assertNotNull(range);
        Calendar cal = range.getStart();
        assertNotNull(cal);
        // expected result from haevens-above.com is 27 Feb 2019 07:32
        assertEquals(SunCalcTest.newCalendar(2019, Calendar.FEBRUARY, 27, 7, 32, AMSTERDAM_TIME_ZONE).getTimeInMillis(),
                cal.getTimeInMillis(), ACCURACY_IN_MILLIS);
    }

    @Test
    public void testGetSunInfoForSunNoonAccuracy() {
        Sun sun = sunCalc.getSunInfo(FEB_27_2019, AMSTERDAM_LATITUDE, AMSTERDAM_LONGITUDE, AMSTERDAM_ALTITUDE, false,
                AMSTERDAM_TIME_ZONE, Locale.ROOT);

        Range range = sun.getNoon();
        assertNotNull(range);
        Calendar cal = range.getStart();
        assertNotNull(cal);
        // expected result from haevens-above.com is 27 Feb 2019 12:54
        assertEquals(
                SunCalcTest.newCalendar(2019, Calendar.FEBRUARY, 27, 12, 54, AMSTERDAM_TIME_ZONE).getTimeInMillis(),
                cal.getTimeInMillis(), ACCURACY_IN_MILLIS);
    }

    @Test
    public void testGetSunInfoForSetAccuracy() {
        Sun sun = sunCalc.getSunInfo(FEB_27_2019, AMSTERDAM_LATITUDE, AMSTERDAM_LONGITUDE, AMSTERDAM_ALTITUDE, false,
                AMSTERDAM_TIME_ZONE, Locale.ROOT);

        Range range = sun.getSet();
        assertNotNull(range);
        Calendar cal = range.getStart();
        assertNotNull(cal);
        // expected result from haevens-above.com is 27 Feb 2019 18:15
        assertEquals(
                SunCalcTest.newCalendar(2019, Calendar.FEBRUARY, 27, 18, 15, AMSTERDAM_TIME_ZONE).getTimeInMillis(),
                cal.getTimeInMillis(), ACCURACY_IN_MILLIS);
    }

    @Test
    public void testGetSunInfoForCivilDuskAccuracy() {
        Sun sun = sunCalc.getSunInfo(FEB_27_2019, AMSTERDAM_LATITUDE, AMSTERDAM_LONGITUDE, AMSTERDAM_ALTITUDE, false,
                AMSTERDAM_TIME_ZONE, Locale.ROOT);

        Range range = sun.getCivilDusk();
        assertNotNull(range);
        Calendar cal = range.getStart();
        assertNotNull(cal);
        // expected result from haevens-above.com is 27 Feb 2019 18:15 till 18:50
        assertEquals(
                SunCalcTest.newCalendar(2019, Calendar.FEBRUARY, 27, 18, 15, AMSTERDAM_TIME_ZONE).getTimeInMillis(),
                cal.getTimeInMillis(), ACCURACY_IN_MILLIS);
        cal = range.getEnd();
        assertNotNull(cal);
        assertEquals(
                SunCalcTest.newCalendar(2019, Calendar.FEBRUARY, 27, 18, 50, AMSTERDAM_TIME_ZONE).getTimeInMillis(),
                cal.getTimeInMillis(), ACCURACY_IN_MILLIS);
    }

    @Test
    public void testGetSunInfoForNauticDuskAccuracy() {
        Sun sun = sunCalc.getSunInfo(FEB_27_2019, AMSTERDAM_LATITUDE, AMSTERDAM_LONGITUDE, AMSTERDAM_ALTITUDE, false,
                AMSTERDAM_TIME_ZONE, Locale.ROOT);

        Range range = sun.getNauticDusk();
        assertNotNull(range);
        Calendar cal = range.getStart();
        assertNotNull(cal);
        // expected result from haevens-above.com is 27 Feb 2019 18:50 till 19:29
        assertEquals(
                SunCalcTest.newCalendar(2019, Calendar.FEBRUARY, 27, 18, 50, AMSTERDAM_TIME_ZONE).getTimeInMillis(),
                cal.getTimeInMillis(), ACCURACY_IN_MILLIS);
        cal = range.getEnd();
        assertNotNull(cal);
        assertEquals(
                SunCalcTest.newCalendar(2019, Calendar.FEBRUARY, 27, 19, 29, AMSTERDAM_TIME_ZONE).getTimeInMillis(),
                cal.getTimeInMillis(), ACCURACY_IN_MILLIS);
    }

    @Test
    public void testGetSunInfoForAstronomicalDuskAccuracy() {
        Sun sun = sunCalc.getSunInfo(FEB_27_2019, AMSTERDAM_LATITUDE, AMSTERDAM_LONGITUDE, AMSTERDAM_ALTITUDE, false,
                AMSTERDAM_TIME_ZONE, Locale.ROOT);

        Range range = sun.getAstroDusk();
        assertNotNull(range);
        Calendar cal = range.getStart();
        assertNotNull(cal);
        // expected result from haevens-above.com is 27 Feb 2019 19:29 till 20:09
        assertEquals(
                SunCalcTest.newCalendar(2019, Calendar.FEBRUARY, 27, 19, 29, AMSTERDAM_TIME_ZONE).getTimeInMillis(),
                cal.getTimeInMillis(), ACCURACY_IN_MILLIS);
        cal = range.getEnd();
        assertNotNull(cal);
        assertEquals(SunCalcTest.newCalendar(2019, Calendar.FEBRUARY, 27, 20, 9, AMSTERDAM_TIME_ZONE).getTimeInMillis(),
                cal.getTimeInMillis(), ACCURACY_IN_MILLIS);
    }

    @Test
    @Disabled
    public void testRangesForCoherenceBetweenNightEndAndAstroDawnStart() {
        Sun sun = sunCalc.getSunInfo(FEB_27_2019, AMSTERDAM_LATITUDE, AMSTERDAM_LONGITUDE, AMSTERDAM_ALTITUDE, false,
                AMSTERDAM_TIME_ZONE, Locale.ROOT);

        Range range = sun.getAllRanges().get(SunPhaseName.NIGHT);
        assertNotNull(range);
        Range range2 = sun.getAllRanges().get(SunPhaseName.ASTRO_DAWN);
        assertNotNull(range2);
        assertEquals(range.getEnd(), range2.getStart());
    }

    @Test
    public void testRangesForCoherenceBetweenMorningNightEndAndAstroDawnStart() {
        Sun sun = sunCalc.getSunInfo(FEB_27_2019, AMSTERDAM_LATITUDE, AMSTERDAM_LONGITUDE, AMSTERDAM_ALTITUDE, false,
                AMSTERDAM_TIME_ZONE, Locale.ROOT);

        Range range = sun.getAllRanges().get(SunPhaseName.MORNING_NIGHT);
        assertNotNull(range);
        Range range2 = sun.getAllRanges().get(SunPhaseName.ASTRO_DAWN);
        assertNotNull(range2);
        assertEquals(range.getEnd(), range2.getStart());
    }

    @Test
    public void testRangesForCoherenceBetweenAstroDownEndAndNauticDawnStart() {
        Sun sun = sunCalc.getSunInfo(FEB_27_2019, AMSTERDAM_LATITUDE, AMSTERDAM_LONGITUDE, AMSTERDAM_ALTITUDE, false,
                AMSTERDAM_TIME_ZONE, Locale.ROOT);

        Range range = sun.getAllRanges().get(SunPhaseName.ASTRO_DAWN);
        assertNotNull(range);
        Range range2 = sun.getAllRanges().get(SunPhaseName.NAUTIC_DAWN);
        assertNotNull(range2);
        assertEquals(range.getEnd(), range2.getStart());
    }

    @Test
    public void testRangesForCoherenceBetweenNauticDawnEndAndCivilDawnStart() {
        Sun sun = sunCalc.getSunInfo(FEB_27_2019, AMSTERDAM_LATITUDE, AMSTERDAM_LONGITUDE, AMSTERDAM_ALTITUDE, false,
                AMSTERDAM_TIME_ZONE, Locale.ROOT);

        Range range = sun.getAllRanges().get(SunPhaseName.NAUTIC_DAWN);
        assertNotNull(range);
        Range range2 = sun.getAllRanges().get(SunPhaseName.CIVIL_DAWN);
        assertNotNull(range2);
        assertEquals(range.getEnd(), range2.getStart());
    }

    @Test
    public void testRangesForCoherenceBetweenCivilDawnEndAndSunRiseStart() {
        Sun sun = sunCalc.getSunInfo(FEB_27_2019, AMSTERDAM_LATITUDE, AMSTERDAM_LONGITUDE, AMSTERDAM_ALTITUDE, false,
                AMSTERDAM_TIME_ZONE, Locale.ROOT);

        Range range = sun.getAllRanges().get(SunPhaseName.CIVIL_DAWN);
        assertNotNull(range);
        Range range2 = sun.getAllRanges().get(SunPhaseName.SUN_RISE);
        assertNotNull(range2);
        assertEquals(range.getEnd(), range2.getStart());
    }

    @Test
    public void testRangesForCoherenceBetweenSunRiseEndAndDaylightStart() {
        Sun sun = sunCalc.getSunInfo(FEB_27_2019, AMSTERDAM_LATITUDE, AMSTERDAM_LONGITUDE, AMSTERDAM_ALTITUDE, false,
                AMSTERDAM_TIME_ZONE, Locale.ROOT);

        Range range = sun.getAllRanges().get(SunPhaseName.SUN_RISE);
        assertNotNull(range);
        Range range2 = sun.getAllRanges().get(SunPhaseName.DAYLIGHT);
        assertNotNull(range2);
        assertEquals(range.getEnd(), range2.getStart());
    }

    @Test
    public void testRangesForCoherenceBetweenDaylightEndAndSunSetStart() {
        Sun sun = sunCalc.getSunInfo(FEB_27_2019, AMSTERDAM_LATITUDE, AMSTERDAM_LONGITUDE, AMSTERDAM_ALTITUDE, false,
                AMSTERDAM_TIME_ZONE, Locale.ROOT);

        Range range = sun.getAllRanges().get(SunPhaseName.DAYLIGHT);
        assertNotNull(range);
        Range range2 = sun.getAllRanges().get(SunPhaseName.SUN_SET);
        assertNotNull(range2);
        assertEquals(range.getEnd(), range2.getStart());
    }

    @Test
    public void testRangesForCoherenceBetweenSunSetEndAndCivilDuskStart() {
        Sun sun = sunCalc.getSunInfo(FEB_27_2019, AMSTERDAM_LATITUDE, AMSTERDAM_LONGITUDE, AMSTERDAM_ALTITUDE, false,
                AMSTERDAM_TIME_ZONE, Locale.ROOT);

        Range range = sun.getAllRanges().get(SunPhaseName.SUN_SET);
        assertNotNull(range);
        Range range2 = sun.getAllRanges().get(SunPhaseName.CIVIL_DUSK);
        assertNotNull(range2);
        assertEquals(range.getEnd(), range2.getStart());
    }

    @Test
    public void testRangesForCoherenceBetweenCivilDuskEndAndNauticDuskStart() {
        Sun sun = sunCalc.getSunInfo(FEB_27_2019, AMSTERDAM_LATITUDE, AMSTERDAM_LONGITUDE, AMSTERDAM_ALTITUDE, false,
                AMSTERDAM_TIME_ZONE, Locale.ROOT);

        Range range = sun.getAllRanges().get(SunPhaseName.CIVIL_DUSK);
        assertNotNull(range);
        Range range2 = sun.getAllRanges().get(SunPhaseName.NAUTIC_DUSK);
        assertNotNull(range2);
        assertEquals(range.getEnd(), range2.getStart());
    }

    @Test
    public void testRangesForCoherenceBetweenNauticDuskEndAndAstroDuskStart() {
        Sun sun = sunCalc.getSunInfo(FEB_27_2019, AMSTERDAM_LATITUDE, AMSTERDAM_LONGITUDE, AMSTERDAM_ALTITUDE, false,
                AMSTERDAM_TIME_ZONE, Locale.ROOT);

        Range range = sun.getAllRanges().get(SunPhaseName.NAUTIC_DUSK);
        assertNotNull(range);
        Range range2 = sun.getAllRanges().get(SunPhaseName.ASTRO_DUSK);
        assertNotNull(range2);
        assertEquals(range.getEnd(), range2.getStart());
    }

    @Test
    public void testRangesForCoherenceBetweenAstroDuskEndAndNightStart() {
        Sun sun = sunCalc.getSunInfo(FEB_27_2019, AMSTERDAM_LATITUDE, AMSTERDAM_LONGITUDE, AMSTERDAM_ALTITUDE, false,
                AMSTERDAM_TIME_ZONE, Locale.ROOT);

        Range range = sun.getAllRanges().get(SunPhaseName.ASTRO_DUSK);
        assertNotNull(range);
        Range range2 = sun.getAllRanges().get(SunPhaseName.NIGHT);
        assertNotNull(range2);
        assertEquals(range.getEnd(), range2.getStart());
    }

    @Test
    public void testRangesForCoherenceBetweenAstroDuskEndAndEveningNightStart() {
        Sun sun = sunCalc.getSunInfo(FEB_27_2019, AMSTERDAM_LATITUDE, AMSTERDAM_LONGITUDE, AMSTERDAM_ALTITUDE, false,
                AMSTERDAM_TIME_ZONE, Locale.ROOT);

        Range range = sun.getAllRanges().get(SunPhaseName.ASTRO_DUSK);
        assertNotNull(range);
        Range range2 = sun.getAllRanges().get(SunPhaseName.EVENING_NIGHT);
        assertNotNull(range2);
        assertEquals(range.getEnd(), range2.getStart());
    }

    @Test
    public void testIssue7642CivilDawnEnd() {
        TimeZone tZone = TimeZone.getTimeZone("Europe/London");
        Calendar tDate = SunCalcTest.newCalendar(2020, Calendar.MAY, 13, 5, 12, tZone);

        Sun sun = sunCalc.getSunInfo(tDate, 53.524695, -2.4, 0.0, true, AMSTERDAM_TIME_ZONE, Locale.ROOT);
        assertEquals(SunPhaseName.CIVIL_DAWN, sun.getPhase().getName());
    }

    @Test
    public void testIssue7642SunRiseStart() {
        // SunCalc.ranges was not sorted, causing unexpected output in corner cases.
        TimeZone tZone = TimeZone.getTimeZone("Europe/London");
        Calendar tDate = SunCalcTest.newCalendar(2020, Calendar.MAY, 13, 5, 13, tZone);
        tDate.set(Calendar.SECOND, 4);

        Sun sun = sunCalc.getSunInfo(tDate, 53.524695, -2.4, 0.0, true, AMSTERDAM_TIME_ZONE, Locale.ROOT);
        assertEquals(SunPhaseName.SUN_RISE, sun.getPhase().getName());
    }

    @Test
    public void testIssue7642DaylightStart() {
        TimeZone tZone = TimeZone.getTimeZone("Europe/London");
        Calendar tDate = SunCalcTest.newCalendar(2020, Calendar.MAY, 13, 5, 18, tZone);

        Sun sun = sunCalc.getSunInfo(tDate, 53.524695, -2.4, 0.0, true, AMSTERDAM_TIME_ZONE, Locale.ROOT);
        assertEquals(SunPhaseName.DAYLIGHT, sun.getPhase().getName());
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

    @Test
    public void testAstroAndMeteoSeasons() {
        Sun meteoSun = sunCalc.getSunInfo(FEB_27_2019, AMSTERDAM_LATITUDE, AMSTERDAM_LONGITUDE, AMSTERDAM_ALTITUDE,
                true, AMSTERDAM_TIME_ZONE, Locale.ROOT);
        Sun equiSun = sunCalc.getSunInfo(FEB_27_2019, AMSTERDAM_LATITUDE, AMSTERDAM_LONGITUDE, AMSTERDAM_ALTITUDE,
                false, AMSTERDAM_TIME_ZONE, Locale.ROOT);

        Calendar cal = meteoSun.getSeason().getSpring();
        assertNotNull(cal);
        Calendar cal2 = equiSun.getSeason().getSpring();
        assertNotNull(cal2);
        assertEquals(cal.get(Calendar.MONTH), cal2.get(Calendar.MONTH));
        assertEquals(cal.get(Calendar.YEAR), cal2.get(Calendar.YEAR));
        assertEquals(1, cal.get(Calendar.DAY_OF_MONTH));
        assertFalse(cal.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH));
    }

    @SuppressWarnings("null")
    @Test
    public void testIsSunUpAllDayIsFalseForBarrowAlaska() throws Exception {
        Method isSunUpAllDay = SunCalc.class.getDeclaredMethod("isSunUpAllDay", Calendar.class, double.class,
                double.class, Double.class);
        isSunUpAllDay.setAccessible(true);

        // At summer solstice in Barrow, Alaska, sun stay up all day
        boolean result = (boolean) isSunUpAllDay.invoke(sunCalc, SUMMER_SOLSTICE_BARROW, BARROW_LATITUDE,
                BARROW_LONGITUDE, BARROW_ALTITUDE);

        assertTrue(result);

        // It's not the case in Amsterdam
        result = (boolean) isSunUpAllDay.invoke(sunCalc, SUMMER_SOLSTICE_AMSTERDAM, AMSTERDAM_LATITUDE,
                AMSTERDAM_LONGITUDE, AMSTERDAM_ALTITUDE);

        assertFalse(result);
    }
}
