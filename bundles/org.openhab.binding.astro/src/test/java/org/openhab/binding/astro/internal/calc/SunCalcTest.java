/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
import java.util.TimeZone;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openhab.binding.astro.internal.model.Sun;
import org.openhab.binding.astro.internal.model.SunPhaseName;

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

    private static final TimeZone TIME_ZONE = TimeZone.getTimeZone("Europe/Amsterdam");
    private static final Calendar FEB_27_2019 = SunCalcTest.newCalendar(2019, Calendar.FEBRUARY, 27, 1, 0, TIME_ZONE);
    private static final double AMSTERDAM_LATITUDE = 52.367607;
    private static final double AMSTERDAM_LONGITUDE = 4.8978293;
    private static final double AMSTERDAM_ALTITUDE = 0.0;
    private static final int ACCURACY_IN_MILLIS = 3 * 60 * 1000;

    private SunCalc sunCalc;

    @BeforeEach
    public void init() {
        sunCalc = new SunCalc();
    }

    @Test
    public void testGetSunInfoForOldDate() {
        Sun sun = sunCalc.getSunInfo(FEB_27_2019, AMSTERDAM_LATITUDE, AMSTERDAM_LONGITUDE, AMSTERDAM_ALTITUDE, false);

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

        // for an old date the phase is always null
        assertNull(sun.getPhase().getName());
    }

    @Test
    public void testGetSunInfoForAstronomicalDawnAccuracy() {
        Sun sun = sunCalc.getSunInfo(FEB_27_2019, AMSTERDAM_LATITUDE, AMSTERDAM_LONGITUDE, AMSTERDAM_ALTITUDE, false);

        // expected result from haevens-above.com is 27 Feb 2019 05:39 till 06:18
        assertEquals(SunCalcTest.newCalendar(2019, Calendar.FEBRUARY, 27, 5, 39, TIME_ZONE).getTimeInMillis(),
                sun.getAstroDawn().getStart().getTimeInMillis(), ACCURACY_IN_MILLIS);
        assertEquals(SunCalcTest.newCalendar(2019, Calendar.FEBRUARY, 27, 6, 18, TIME_ZONE).getTimeInMillis(),
                sun.getAstroDawn().getEnd().getTimeInMillis(), ACCURACY_IN_MILLIS);
    }

    @Test
    public void testGetSunInfoForNauticDawnAccuracy() {
        Sun sun = sunCalc.getSunInfo(FEB_27_2019, AMSTERDAM_LATITUDE, AMSTERDAM_LONGITUDE, AMSTERDAM_ALTITUDE, false);

        // expected result from haevens-above.com is 27 Feb 2019 06:18 till 06:58
        assertEquals(SunCalcTest.newCalendar(2019, Calendar.FEBRUARY, 27, 6, 18, TIME_ZONE).getTimeInMillis(),
                sun.getNauticDawn().getStart().getTimeInMillis(), ACCURACY_IN_MILLIS);
        assertEquals(SunCalcTest.newCalendar(2019, Calendar.FEBRUARY, 27, 6, 58, TIME_ZONE).getTimeInMillis(),
                sun.getNauticDawn().getEnd().getTimeInMillis(), ACCURACY_IN_MILLIS);
    }

    @Test
    public void testGetSunInfoForCivilDawnAccuracy() {
        Sun sun = sunCalc.getSunInfo(FEB_27_2019, AMSTERDAM_LATITUDE, AMSTERDAM_LONGITUDE, AMSTERDAM_ALTITUDE, false);

        // expected result from haevens-above.com is 27 Feb 2019 06:58 till 07:32
        assertEquals(SunCalcTest.newCalendar(2019, Calendar.FEBRUARY, 27, 6, 58, TIME_ZONE).getTimeInMillis(),
                sun.getCivilDawn().getStart().getTimeInMillis(), ACCURACY_IN_MILLIS);
        assertEquals(SunCalcTest.newCalendar(2019, Calendar.FEBRUARY, 27, 7, 32, TIME_ZONE).getTimeInMillis(),
                sun.getCivilDawn().getEnd().getTimeInMillis(), ACCURACY_IN_MILLIS);
    }

    @Test
    public void testGetSunInfoForRiseAccuracy() {
        Sun sun = sunCalc.getSunInfo(FEB_27_2019, AMSTERDAM_LATITUDE, AMSTERDAM_LONGITUDE, AMSTERDAM_ALTITUDE, false);

        // expected result from haevens-above.com is 27 Feb 2019 07:32
        assertEquals(SunCalcTest.newCalendar(2019, Calendar.FEBRUARY, 27, 7, 32, TIME_ZONE).getTimeInMillis(),
                sun.getRise().getStart().getTimeInMillis(), ACCURACY_IN_MILLIS);
    }

    @Test
    public void testGetSunInfoForSunNoonAccuracy() {
        Sun sun = sunCalc.getSunInfo(FEB_27_2019, AMSTERDAM_LATITUDE, AMSTERDAM_LONGITUDE, AMSTERDAM_ALTITUDE, false);

        // expected result from haevens-above.com is 27 Feb 2019 12:54
        assertEquals(SunCalcTest.newCalendar(2019, Calendar.FEBRUARY, 27, 12, 54, TIME_ZONE).getTimeInMillis(),
                sun.getNoon().getStart().getTimeInMillis(), ACCURACY_IN_MILLIS);
    }

    @Test
    public void testGetSunInfoForSetAccuracy() {
        Sun sun = sunCalc.getSunInfo(FEB_27_2019, AMSTERDAM_LATITUDE, AMSTERDAM_LONGITUDE, AMSTERDAM_ALTITUDE, false);

        // expected result from haevens-above.com is 27 Feb 2019 18:15
        assertEquals(SunCalcTest.newCalendar(2019, Calendar.FEBRUARY, 27, 18, 15, TIME_ZONE).getTimeInMillis(),
                sun.getSet().getStart().getTimeInMillis(), ACCURACY_IN_MILLIS);
    }

    @Test
    public void testGetSunInfoForCivilDuskAccuracy() {
        Sun sun = sunCalc.getSunInfo(FEB_27_2019, AMSTERDAM_LATITUDE, AMSTERDAM_LONGITUDE, AMSTERDAM_ALTITUDE, false);

        // expected result from haevens-above.com is 27 Feb 2019 18:15 till 18:50
        assertEquals(SunCalcTest.newCalendar(2019, Calendar.FEBRUARY, 27, 18, 15, TIME_ZONE).getTimeInMillis(),
                sun.getCivilDusk().getStart().getTimeInMillis(), ACCURACY_IN_MILLIS);
        assertEquals(SunCalcTest.newCalendar(2019, Calendar.FEBRUARY, 27, 18, 50, TIME_ZONE).getTimeInMillis(),
                sun.getCivilDusk().getEnd().getTimeInMillis(), ACCURACY_IN_MILLIS);
    }

    @Test
    public void testGetSunInfoForNauticDuskAccuracy() {
        Sun sun = sunCalc.getSunInfo(FEB_27_2019, AMSTERDAM_LATITUDE, AMSTERDAM_LONGITUDE, AMSTERDAM_ALTITUDE, false);

        // expected result from haevens-above.com is 27 Feb 2019 18:50 till 19:29
        assertEquals(SunCalcTest.newCalendar(2019, Calendar.FEBRUARY, 27, 18, 50, TIME_ZONE).getTimeInMillis(),
                sun.getNauticDusk().getStart().getTimeInMillis(), ACCURACY_IN_MILLIS);
        assertEquals(SunCalcTest.newCalendar(2019, Calendar.FEBRUARY, 27, 19, 29, TIME_ZONE).getTimeInMillis(),
                sun.getNauticDusk().getEnd().getTimeInMillis(), ACCURACY_IN_MILLIS);
    }

    @Test
    public void testGetSunInfoForAstronomicalDuskAccuracy() {
        Sun sun = sunCalc.getSunInfo(FEB_27_2019, AMSTERDAM_LATITUDE, AMSTERDAM_LONGITUDE, AMSTERDAM_ALTITUDE, false);

        // expected result from haevens-above.com is 27 Feb 2019 19:29 till 20:09
        assertEquals(SunCalcTest.newCalendar(2019, Calendar.FEBRUARY, 27, 19, 29, TIME_ZONE).getTimeInMillis(),
                sun.getAstroDusk().getStart().getTimeInMillis(), ACCURACY_IN_MILLIS);
        assertEquals(SunCalcTest.newCalendar(2019, Calendar.FEBRUARY, 27, 20, 9, TIME_ZONE).getTimeInMillis(),
                sun.getAstroDusk().getEnd().getTimeInMillis(), ACCURACY_IN_MILLIS);
    }

    @Test
    @Disabled
    public void testRangesForCoherenceBetweenNightEndAndAstroDawnStart() {
        Sun sun = sunCalc.getSunInfo(FEB_27_2019, AMSTERDAM_LATITUDE, AMSTERDAM_LONGITUDE, AMSTERDAM_ALTITUDE, false);

        assertEquals(sun.getAllRanges().get(SunPhaseName.NIGHT).getEnd(),
                sun.getAllRanges().get(SunPhaseName.ASTRO_DAWN).getStart());
    }

    @Test
    public void testRangesForCoherenceBetweenMorningNightEndAndAstroDawnStart() {
        Sun sun = sunCalc.getSunInfo(FEB_27_2019, AMSTERDAM_LATITUDE, AMSTERDAM_LONGITUDE, AMSTERDAM_ALTITUDE, false);

        assertEquals(sun.getAllRanges().get(SunPhaseName.MORNING_NIGHT).getEnd(),
                sun.getAllRanges().get(SunPhaseName.ASTRO_DAWN).getStart());
    }

    @Test
    public void testRangesForCoherenceBetweenAstroDownEndAndNauticDawnStart() {
        Sun sun = sunCalc.getSunInfo(FEB_27_2019, AMSTERDAM_LATITUDE, AMSTERDAM_LONGITUDE, AMSTERDAM_ALTITUDE, false);

        assertEquals(sun.getAllRanges().get(SunPhaseName.ASTRO_DAWN).getEnd(),
                sun.getAllRanges().get(SunPhaseName.NAUTIC_DAWN).getStart());
    }

    @Test
    public void testRangesForCoherenceBetweenNauticDawnEndAndCivilDawnStart() {
        Sun sun = sunCalc.getSunInfo(FEB_27_2019, AMSTERDAM_LATITUDE, AMSTERDAM_LONGITUDE, AMSTERDAM_ALTITUDE, false);

        assertEquals(sun.getAllRanges().get(SunPhaseName.NAUTIC_DAWN).getEnd(),
                sun.getAllRanges().get(SunPhaseName.CIVIL_DAWN).getStart());
    }

    @Test
    public void testRangesForCoherenceBetweenCivilDawnEndAndSunRiseStart() {
        Sun sun = sunCalc.getSunInfo(FEB_27_2019, AMSTERDAM_LATITUDE, AMSTERDAM_LONGITUDE, AMSTERDAM_ALTITUDE, false);

        assertEquals(sun.getAllRanges().get(SunPhaseName.CIVIL_DAWN).getEnd(),
                sun.getAllRanges().get(SunPhaseName.SUN_RISE).getStart());
    }

    @Test
    public void testRangesForCoherenceBetweenSunRiseEndAndDaylightStart() {
        Sun sun = sunCalc.getSunInfo(FEB_27_2019, AMSTERDAM_LATITUDE, AMSTERDAM_LONGITUDE, AMSTERDAM_ALTITUDE, false);

        assertEquals(sun.getAllRanges().get(SunPhaseName.SUN_RISE).getEnd(),
                sun.getAllRanges().get(SunPhaseName.DAYLIGHT).getStart());
    }

    @Test
    public void testRangesForCoherenceBetweenDaylightEndAndSunSetStart() {
        Sun sun = sunCalc.getSunInfo(FEB_27_2019, AMSTERDAM_LATITUDE, AMSTERDAM_LONGITUDE, AMSTERDAM_ALTITUDE, false);

        assertEquals(sun.getAllRanges().get(SunPhaseName.DAYLIGHT).getEnd(),
                sun.getAllRanges().get(SunPhaseName.SUN_SET).getStart());
    }

    @Test
    public void testRangesForCoherenceBetweenSunSetEndAndCivilDuskStart() {
        Sun sun = sunCalc.getSunInfo(FEB_27_2019, AMSTERDAM_LATITUDE, AMSTERDAM_LONGITUDE, AMSTERDAM_ALTITUDE, false);

        assertEquals(sun.getAllRanges().get(SunPhaseName.SUN_SET).getEnd(),
                sun.getAllRanges().get(SunPhaseName.CIVIL_DUSK).getStart());
    }

    @Test
    public void testRangesForCoherenceBetweenCivilDuskEndAndNauticDuskStart() {
        Sun sun = sunCalc.getSunInfo(FEB_27_2019, AMSTERDAM_LATITUDE, AMSTERDAM_LONGITUDE, AMSTERDAM_ALTITUDE, false);

        assertEquals(sun.getAllRanges().get(SunPhaseName.CIVIL_DUSK).getEnd(),
                sun.getAllRanges().get(SunPhaseName.NAUTIC_DUSK).getStart());
    }

    @Test
    public void testRangesForCoherenceBetweenNauticDuskEndAndAstroDuskStart() {
        Sun sun = sunCalc.getSunInfo(FEB_27_2019, AMSTERDAM_LATITUDE, AMSTERDAM_LONGITUDE, AMSTERDAM_ALTITUDE, false);

        assertEquals(sun.getAllRanges().get(SunPhaseName.NAUTIC_DUSK).getEnd(),
                sun.getAllRanges().get(SunPhaseName.ASTRO_DUSK).getStart());
    }

    @Test
    public void testRangesForCoherenceBetweenAstroDuskEndAndNightStart() {
        Sun sun = sunCalc.getSunInfo(FEB_27_2019, AMSTERDAM_LATITUDE, AMSTERDAM_LONGITUDE, AMSTERDAM_ALTITUDE, false);

        assertEquals(sun.getAllRanges().get(SunPhaseName.ASTRO_DUSK).getEnd(),
                sun.getAllRanges().get(SunPhaseName.NIGHT).getStart());
    }

    @Test
    public void testRangesForCoherenceBetweenAstroDuskEndAndEveningNightStart() {
        Sun sun = sunCalc.getSunInfo(FEB_27_2019, AMSTERDAM_LATITUDE, AMSTERDAM_LONGITUDE, AMSTERDAM_ALTITUDE, false);

        assertEquals(sun.getAllRanges().get(SunPhaseName.ASTRO_DUSK).getEnd(),
                sun.getAllRanges().get(SunPhaseName.EVENING_NIGHT).getStart());
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
        Calendar result = new GregorianCalendar(year, month, dayOfMonth, hourOfDay, minute);
        result.setTimeZone(zone);

        return result;
    }

    @Test
    public void testAstroAndMeteoSeasons() {
        Sun meteoSun = sunCalc.getSunInfo(FEB_27_2019, AMSTERDAM_LATITUDE, AMSTERDAM_LONGITUDE, AMSTERDAM_ALTITUDE,
                true);
        Sun equiSun = sunCalc.getSunInfo(FEB_27_2019, AMSTERDAM_LATITUDE, AMSTERDAM_LONGITUDE, AMSTERDAM_ALTITUDE,
                false);

        assertEquals(meteoSun.getSeason().getSpring().get(Calendar.MONTH),
                equiSun.getSeason().getSpring().get(Calendar.MONTH));
        assertEquals(meteoSun.getSeason().getSpring().get(Calendar.YEAR),
                equiSun.getSeason().getSpring().get(Calendar.YEAR));
        assertEquals(1, meteoSun.getSeason().getSpring().get(Calendar.DAY_OF_MONTH));
        assertFalse(meteoSun.getSeason().getSpring().get(Calendar.DAY_OF_MONTH) == equiSun.getSeason().getSpring()
                .get(Calendar.DAY_OF_MONTH));
    }
}
