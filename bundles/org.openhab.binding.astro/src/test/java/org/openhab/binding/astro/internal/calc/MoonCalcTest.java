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
package org.openhab.binding.astro.internal.calc;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.astro.internal.model.Moon;
import org.openhab.binding.astro.internal.model.ZodiacSign;

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
public class MoonCalcTest {

    private static final TimeZone TIME_ZONE = TimeZone.getTimeZone("Europe/Amsterdam");
    private static final Calendar FEB_27_2019 = MoonCalcTest.newCalendar(2019, Calendar.FEBRUARY, 27, 1, 0, TIME_ZONE);
    private static final double AMSTERDAM_LATITUDE = 52.367607;
    private static final double AMSTERDAM_LONGITUDE = 4.8978293;

    private static final int ACCURACY_IN_MILLIS = 5 * 60 * 1000;
    private static final int ACCURACY_IN_KILOMETRES = 4;
    private static final double ACCURACY_IN_DEGREE = 0.3;

    private MoonCalc moonCalc;

    @BeforeEach
    public void init() {
        moonCalc = new MoonCalc();
    }

    @Test
    public void testGetMoonInfoForOldDate() {
        Moon moon = moonCalc.getMoonInfo(FEB_27_2019, AMSTERDAM_LATITUDE, AMSTERDAM_LONGITUDE);

        assertNotNull(moon.getApogee());
        assertNotNull(moon.getPerigee());

        assertNotNull(moon.getDistance());
        assertNotNull(moon.getEclipse());

        assertNotNull(moon.getPhase());
        assertNotNull(moon.getPosition());
        assertNotNull(moon.getRise());
        assertNotNull(moon.getSet());
        assertNotNull(moon.getZodiac());

        // for an old date the phase should not be calculated
        assertNull(moon.getPhase().getName());
    }

    @Test
    public void testGetMoonInfoForApogeeAccuracy() {
        Moon moon = moonCalc.getMoonInfo(FEB_27_2019, AMSTERDAM_LATITUDE, AMSTERDAM_LONGITUDE);

        // expected result from haevens-above.com is 406,391 km @ 04 March 2019 12:27
        assertEquals(406391, moon.getApogee().getDistance().doubleValue(), ACCURACY_IN_KILOMETRES);
        assertEquals(MoonCalcTest.newCalendar(2019, Calendar.MARCH, 4, 12, 27, TIME_ZONE).getTimeInMillis(),
                moon.getApogee().getDate().getTimeInMillis(), ACCURACY_IN_MILLIS);
    }

    @Test
    public void testGetMoonInfoForPerigeeAccuracy() {
        Moon moon = moonCalc.getMoonInfo(FEB_27_2019, AMSTERDAM_LATITUDE, AMSTERDAM_LONGITUDE);

        // expected result from haevens-above.com is 359,377 km @ 19 February 2019 20:44
        assertEquals(359377, moon.getPerigee().getDistance().doubleValue(), ACCURACY_IN_KILOMETRES);

        assertEquals(MoonCalcTest.newCalendar(2019, Calendar.MARCH, 19, 20, 48, TIME_ZONE).getTimeInMillis(),
                moon.getPerigee().getDate().getTimeInMillis(), ACCURACY_IN_MILLIS);
    }

    @Test
    public void testGetMoonInfoForRiseAccuracy() {
        Moon moon = moonCalc.getMoonInfo(FEB_27_2019, AMSTERDAM_LATITUDE, AMSTERDAM_LONGITUDE);

        // expected result from haevens-above.com is 03:00
        assertEquals(MoonCalcTest.newCalendar(2019, Calendar.FEBRUARY, 27, 3, 0, TIME_ZONE).getTimeInMillis(),
                moon.getRise().getStart().getTimeInMillis(), ACCURACY_IN_MILLIS);
    }

    @Test
    public void testGetMoonInfoForSetAccuracy() {
        Moon moon = moonCalc.getMoonInfo(FEB_27_2019, AMSTERDAM_LATITUDE, AMSTERDAM_LONGITUDE);

        // expected result from haevens-above.com is 11:35
        assertEquals(MoonCalcTest.newCalendar(2019, Calendar.FEBRUARY, 27, 11, 35, TIME_ZONE).getTimeInMillis(),
                moon.getSet().getStart().getTimeInMillis(), ACCURACY_IN_MILLIS);
    }

    @Test
    public void testGetMoonInfoForZodiac() {
        Moon moon = moonCalc.getMoonInfo(FEB_27_2019, AMSTERDAM_LATITUDE, AMSTERDAM_LONGITUDE);
        moonCalc.setPositionalInfo(FEB_27_2019, AMSTERDAM_LATITUDE, AMSTERDAM_LONGITUDE, moon);

        assertEquals(ZodiacSign.SAGITTARIUS, moon.getZodiac().getSign());
    }

    @Test
    public void testGetMoonInfoForMoonPositionAccuracy() {
        Moon moon = moonCalc.getMoonInfo(FEB_27_2019, AMSTERDAM_LATITUDE, AMSTERDAM_LONGITUDE);
        moonCalc.setPositionalInfo(FEB_27_2019, AMSTERDAM_LATITUDE, AMSTERDAM_LONGITUDE, moon);

        // expected result from haevens-above.com is Azimuth: 100.5, altitude -17
        assertEquals(100.5, moon.getPosition().getAzimuth().doubleValue(), ACCURACY_IN_DEGREE);
        assertEquals(-17, moon.getPosition().getElevation().doubleValue(), ACCURACY_IN_DEGREE);
    }

    @Test
    public void testGetMoonInfoForMoonDistanceAccuracy() {
        Moon moon = moonCalc.getMoonInfo(FEB_27_2019, AMSTERDAM_LATITUDE, AMSTERDAM_LONGITUDE);
        moonCalc.setPositionalInfo(FEB_27_2019, AMSTERDAM_LATITUDE, AMSTERDAM_LONGITUDE, moon);

        // expected result from haevens-above.com is 392612 km
        assertEquals(392612, moon.getDistance().getDistance().doubleValue(), ACCURACY_IN_KILOMETRES);
    }

    @Test
    public void testGetMoonInfoForMoonPhaseAccuracy() {
        Moon moon = moonCalc.getMoonInfo(FEB_27_2019, AMSTERDAM_LATITUDE, AMSTERDAM_LONGITUDE);
        moonCalc.setPositionalInfo(FEB_27_2019, AMSTERDAM_LATITUDE, AMSTERDAM_LONGITUDE, moon);

        // New moon 06 March 2019 17:04
        // First quarter 14 March 2019 11:27
        // Full moon 21 March 2019 02:43
        // Last quarter 28 March 2019 05:10
        assertEquals(MoonCalcTest.newCalendar(2019, Calendar.MARCH, 06, 17, 04, TIME_ZONE).getTimeInMillis(),
                moon.getPhase().getNew().getTimeInMillis(), ACCURACY_IN_MILLIS);
        assertEquals(MoonCalcTest.newCalendar(2019, Calendar.MARCH, 14, 11, 27, TIME_ZONE).getTimeInMillis(),
                moon.getPhase().getFirstQuarter().getTimeInMillis(), ACCURACY_IN_MILLIS);
        assertEquals(MoonCalcTest.newCalendar(2019, Calendar.MARCH, 21, 02, 43, TIME_ZONE).getTimeInMillis(),
                moon.getPhase().getFull().getTimeInMillis(), ACCURACY_IN_MILLIS);
        assertEquals(MoonCalcTest.newCalendar(2019, Calendar.MARCH, 28, 05, 10, TIME_ZONE).getTimeInMillis(),
                moon.getPhase().getThirdQuarter().getTimeInMillis(), ACCURACY_IN_MILLIS);
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
}
