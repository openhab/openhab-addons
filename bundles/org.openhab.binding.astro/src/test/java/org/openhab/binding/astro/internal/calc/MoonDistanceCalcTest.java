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
import java.util.TimeZone;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.astro.internal.model.DistanceType;
import org.openhab.binding.astro.internal.model.MoonDistance;
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
public class MoonDistanceCalcTest {

    private static final TimeZone TIME_ZONE = TimeZone.getTimeZone("Europe/Amsterdam");
    private static final Calendar FEB_27_2019 = newCalendar(2019, Calendar.FEBRUARY, 27, 1, 0, TIME_ZONE);

    private static final int ACCURACY_IN_MILLIS = 5 * 60 * 1000;
    private static final int ACCURACY_IN_KILOMETRES = 4;

    @Test
    public void testGetMoonInfoForApogeeAccuracy() {
        double jdate = DateTimeUtils.dateToJulianDate(FEB_27_2019);
        MoonDistance distance = MoonDistanceCalc.get(DistanceType.APOGEE, jdate);

        // expected result from haevens-above.com is 406,391 km @ 04 March 2019 12:27
        var apogeeDistance = distance.getDistance();
        assertNotNull(apogeeDistance);
        var kmDistance = apogeeDistance.toUnit(KILO(METRE));
        assertNotNull(kmDistance);
        assertEquals(406391, kmDistance.doubleValue(), ACCURACY_IN_KILOMETRES);
        Instant apogeeDate = distance.getDate();
        assertNotNull(apogeeDate);
        assertEquals(newCalendar(2019, Calendar.MARCH, 4, 12, 27, TIME_ZONE).getTimeInMillis(),
                apogeeDate.toEpochMilli(), ACCURACY_IN_MILLIS);
    }

    @Test
    public void testGetMoonInfoForPerigeeAccuracy() {
        double jdate = DateTimeUtils.dateToJulianDate(FEB_27_2019);
        MoonDistance distance = MoonDistanceCalc.get(DistanceType.PERIGEE, jdate);

        // expected result from haevens-above.com is 359,377 km @ 19 February 2019 20:44
        var perigeeDistance = distance.getDistance();
        assertNotNull(perigeeDistance);
        var kmDistance = perigeeDistance.toUnit(KILO(METRE));
        assertNotNull(kmDistance);
        assertEquals(359377, kmDistance.doubleValue(), ACCURACY_IN_KILOMETRES);

        Instant perigeeDate = distance.getDate();
        assertNotNull(perigeeDate);
        assertEquals(newCalendar(2019, Calendar.MARCH, 19, 20, 48, TIME_ZONE).getTimeInMillis(),
                perigeeDate.toEpochMilli(), ACCURACY_IN_MILLIS);
    }

    @Test
    public void testGetMoonInfoForMoonDistanceAccuracy() {
        double jdate = DateTimeUtils.dateToJulianDate(FEB_27_2019);
        MoonDistance distance = MoonDistanceCalc.calculate(jdate);

        // expected result from haevens-above.com is 392612 km
        var currentDistance = distance.getDistance();
        assertNotNull(currentDistance);
        var kmDistance = currentDistance.toUnit(KILO(METRE));
        assertNotNull(kmDistance);
        assertEquals(392612, kmDistance.doubleValue(), ACCURACY_IN_KILOMETRES);
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
