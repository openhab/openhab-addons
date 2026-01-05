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
import static org.openhab.core.library.unit.MetricPrefix.KILO;
import static org.openhab.core.library.unit.SIUnits.METRE;

import java.time.Instant;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.astro.internal.model.Moon;
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
@NonNullByDefault
public class MoonCalcTest {

    private static final TimeZone TIME_ZONE = TimeZone.getTimeZone("Europe/Amsterdam");
    private static final Calendar FEB_27_2019 = MoonCalcTest.newCalendar(2019, Calendar.FEBRUARY, 27, 1, 0, TIME_ZONE);
    private static final double AMSTERDAM_LATITUDE = 52.367607;
    private static final double AMSTERDAM_LONGITUDE = 4.8978293;

    private static final int ACCURACY_IN_MILLIS = 5 * 60 * 1000;
    private static final int ACCURACY_IN_KILOMETRES = 4;
    private static final double ACCURACY_IN_DEGREE = 0.3;

    private @Nullable MoonCalc moonCalc;

    @BeforeEach
    public void init() {
        moonCalc = new MoonCalc();
    }

    @Test
    public void testGetMoonInfoForOldDate() {
        Moon moon = Objects.requireNonNull(moonCalc).getMoonInfo(FEB_27_2019, AMSTERDAM_LATITUDE, AMSTERDAM_LONGITUDE,
                TIME_ZONE, Locale.ROOT);

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
        Moon moon = Objects.requireNonNull(moonCalc).getMoonInfo(FEB_27_2019, AMSTERDAM_LATITUDE, AMSTERDAM_LONGITUDE,
                TIME_ZONE, Locale.ROOT);

        // expected result from haevens-above.com is 406,391 km @ 04 March 2019 12:27
        var apogeeDistance = moon.getApogee().getDistance();
        assertNotNull(apogeeDistance);
        var kmDistance = apogeeDistance.toUnit(KILO(METRE));
        assertNotNull(kmDistance);
        assertEquals(406391, kmDistance.doubleValue(), ACCURACY_IN_KILOMETRES);
        Instant apogeeDate = moon.getApogee().getDate();
        assertNotNull(apogeeDate);
        assertEquals(MoonCalcTest.newCalendar(2019, Calendar.MARCH, 4, 12, 27, TIME_ZONE).getTimeInMillis(),
                apogeeDate.toEpochMilli(), ACCURACY_IN_MILLIS);
    }

    @Test
    public void testGetMoonInfoForPerigeeAccuracy() {
        Moon moon = Objects.requireNonNull(moonCalc).getMoonInfo(FEB_27_2019, AMSTERDAM_LATITUDE, AMSTERDAM_LONGITUDE,
                TIME_ZONE, Locale.ROOT);

        // expected result from haevens-above.com is 359,377 km @ 19 February 2019 20:44
        var perigeeDistance = moon.getPerigee().getDistance();
        assertNotNull(perigeeDistance);
        var kmDistance = perigeeDistance.toUnit(KILO(METRE));
        assertNotNull(kmDistance);
        assertEquals(359377, kmDistance.doubleValue(), ACCURACY_IN_KILOMETRES);

        Instant perigeeDate = moon.getPerigee().getDate();
        assertNotNull(perigeeDate);
        assertEquals(MoonCalcTest.newCalendar(2019, Calendar.MARCH, 19, 20, 48, TIME_ZONE).getTimeInMillis(),
                perigeeDate.toEpochMilli(), ACCURACY_IN_MILLIS);
    }

    @Test
    public void testGetMoonInfoForRiseAccuracy() {
        Moon moon = Objects.requireNonNull(moonCalc).getMoonInfo(FEB_27_2019, AMSTERDAM_LATITUDE, AMSTERDAM_LONGITUDE,
                TIME_ZONE, Locale.ROOT);

        Calendar riseStart = moon.getRise().getStart();
        assertNotNull(riseStart);
        // expected result from haevens-above.com is 03:00
        assertEquals(MoonCalcTest.newCalendar(2019, Calendar.FEBRUARY, 27, 3, 0, TIME_ZONE).getTimeInMillis(),
                riseStart.getTimeInMillis(), ACCURACY_IN_MILLIS);
    }

    @Test
    public void testGetMoonInfoForSetAccuracy() {
        Moon moon = Objects.requireNonNull(moonCalc).getMoonInfo(FEB_27_2019, AMSTERDAM_LATITUDE, AMSTERDAM_LONGITUDE,
                TIME_ZONE, Locale.ROOT);

        Calendar setStart = moon.getSet().getStart();
        assertNotNull(setStart);
        // expected result from haevens-above.com is 11:35
        assertEquals(MoonCalcTest.newCalendar(2019, Calendar.FEBRUARY, 27, 11, 35, TIME_ZONE).getTimeInMillis(),
                setStart.getTimeInMillis(), ACCURACY_IN_MILLIS);
    }

    @Test
    public void testGetMoonInfoForMoonPositionAccuracy() {
        MoonCalc moonCalc = this.moonCalc;
        assertNotNull(moonCalc);
        Moon moon = moonCalc.getMoonInfo(FEB_27_2019, AMSTERDAM_LATITUDE, AMSTERDAM_LONGITUDE, TIME_ZONE, Locale.ROOT);
        moonCalc.setPositionalInfo(FEB_27_2019, AMSTERDAM_LATITUDE, AMSTERDAM_LONGITUDE, moon, TIME_ZONE, Locale.ROOT);

        // expected result from haevens-above.com is Azimuth: 100.5, altitude -17
        assertEquals(100.5, moon.getPosition().getAzimuth().doubleValue(), ACCURACY_IN_DEGREE);
        assertEquals(-17, moon.getPosition().getElevation().doubleValue(), ACCURACY_IN_DEGREE);
    }

    @Test
    public void testGetMoonInfoForMoonDistanceAccuracy() {
        MoonCalc moonCalc = this.moonCalc;
        assertNotNull(moonCalc);
        Moon moon = moonCalc.getMoonInfo(FEB_27_2019, AMSTERDAM_LATITUDE, AMSTERDAM_LONGITUDE, TIME_ZONE, Locale.ROOT);
        moonCalc.setPositionalInfo(FEB_27_2019, AMSTERDAM_LATITUDE, AMSTERDAM_LONGITUDE, moon, TIME_ZONE, Locale.ROOT);

        // expected result from haevens-above.com is 392612 km
        var currentDistance = moon.getDistance().getDistance();
        assertNotNull(currentDistance);
        var kmDistance = currentDistance.toUnit(KILO(METRE));
        assertNotNull(kmDistance);
        assertEquals(392612, kmDistance.doubleValue(), ACCURACY_IN_KILOMETRES);
    }

    @Test
    public void testGetMoonInfoForMoonPhaseAccuracy() {
        MoonCalc moonCalc = this.moonCalc;
        assertNotNull(moonCalc);
        Moon moon = moonCalc.getMoonInfo(FEB_27_2019, AMSTERDAM_LATITUDE, AMSTERDAM_LONGITUDE, TIME_ZONE, Locale.ROOT);
        moonCalc.setPositionalInfo(FEB_27_2019, AMSTERDAM_LATITUDE, AMSTERDAM_LONGITUDE, moon, TIME_ZONE, Locale.ROOT);

        // New moon 06 March 2019 17:04
        // First quarter 14 March 2019 11:27
        // Full moon 21 March 2019 02:43
        // Last quarter 28 March 2019 05:10
        Calendar phaseCal = moon.getPhase().getNew();
        assertNotNull(phaseCal);
        assertEquals(MoonCalcTest.newCalendar(2019, Calendar.MARCH, 06, 17, 04, TIME_ZONE).getTimeInMillis(),
                phaseCal.getTimeInMillis(), ACCURACY_IN_MILLIS);
        phaseCal = moon.getPhase().getFirstQuarter();
        assertNotNull(phaseCal);
        assertEquals(MoonCalcTest.newCalendar(2019, Calendar.MARCH, 14, 11, 27, TIME_ZONE).getTimeInMillis(),
                phaseCal.getTimeInMillis(), ACCURACY_IN_MILLIS);
        phaseCal = moon.getPhase().getFull();
        assertNotNull(phaseCal);
        assertEquals(MoonCalcTest.newCalendar(2019, Calendar.MARCH, 21, 02, 43, TIME_ZONE).getTimeInMillis(),
                phaseCal.getTimeInMillis(), ACCURACY_IN_MILLIS);
        phaseCal = moon.getPhase().getThirdQuarter();
        assertNotNull(phaseCal);
        assertEquals(MoonCalcTest.newCalendar(2019, Calendar.MARCH, 28, 05, 10, TIME_ZONE).getTimeInMillis(),
                phaseCal.getTimeInMillis(), ACCURACY_IN_MILLIS);
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
}
