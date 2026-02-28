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

import java.time.Instant;
import java.time.InstantSource;
import java.time.ZoneId;
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
import org.openhab.binding.astro.internal.model.MoonPhase;
import org.openhab.binding.astro.internal.model.MoonPhaseSet;
import org.openhab.binding.astro.internal.util.DateTimeUtils;
import org.openhab.core.types.UnDefType;

/***
 * Specific unit tests to check if {@link MoonCalc} generates correct data for
 * Amsterdam city on 27 February 2019. In particular the following cases are
 * covered:
 * <ul>
 * <li>checks if generated data are the same (with some accuracy) as produced by
 * heavens-above.com</li>
 * </ul>
 *
 * @author Leo Siepel - Initial contribution
 * @see <a href="https://www.heavens-above.com/Moon.aspx">Heavens Above Moon</a>
 */
@NonNullByDefault
public class MoonPhaseCalcTest {
    private static final TimeZone TIME_ZONE = TimeZone.getTimeZone("Europe/Amsterdam");
    private static final ZoneId ZONE = TIME_ZONE.toZoneId();
    private static final Calendar FEB_27_2019 = newCalendar(2019, Calendar.FEBRUARY, 27, 1, 0, TIME_ZONE);
    private static final double AMSTERDAM_LATITUDE = 52.367607;
    private static final double AMSTERDAM_LONGITUDE = 4.8978293;

    private static final int ACCURACY_IN_MILLIS = 5 * 60 * 1000;

    private @Nullable MoonCalc moonCalc;

    @BeforeEach
    public void init() {
        moonCalc = new MoonCalc(InstantSource.fixed(Instant.ofEpochMilli(1551225600000L)));
    }

    @Test
    public void testGetMoonInfoForOldDate() {
        Moon moon = Objects.requireNonNull(moonCalc).getMoonInfo(FEB_27_2019, AMSTERDAM_LATITUDE, AMSTERDAM_LONGITUDE);

        assertNotNull(moon.getPhaseSet());
        // for an old date the phase should not be calculated
        assertTrue(moon.getPhaseSet().getName() instanceof UnDefType);
    }

    @Test
    public void testGetMoonInfoForMoonPhaseAccuracy() {
        InstantSource instantSource = InstantSource.fixed(Instant.ofEpochMilli(1551225600000L));
        MoonPhaseSet moonPhase = MoonPhaseCalc.calculate(instantSource, DateTimeUtils.dateToJulianDate(FEB_27_2019),
                MoonPhaseSet.NONE, ZONE);

        // New moon 06 March 2019 17:04 - jd : 2458549.1702492456
        // First quarter 14 March 2019 11:27 - jd : 2458556.936169754
        // Full moon 21 March 2019 02:43 - jd : 2458563.572182703
        // Last quarter 28 March 2019 05:10 - jd : 2458570.6742177564
        Instant phaseNew = moonPhase.getPhase(MoonPhase.NEW);
        assertNotNull(phaseNew);
        assertEquals(newCalendar(2019, Calendar.MARCH, 06, 17, 04, TIME_ZONE).getTimeInMillis(),
                phaseNew.toEpochMilli(), ACCURACY_IN_MILLIS);
        Instant phaseFQ = moonPhase.getPhase(MoonPhase.FIRST_QUARTER);
        assertNotNull(phaseFQ);
        assertEquals(newCalendar(2019, Calendar.MARCH, 14, 11, 27, TIME_ZONE).getTimeInMillis(), phaseFQ.toEpochMilli(),
                ACCURACY_IN_MILLIS);
        Instant phaseFull = moonPhase.getPhase(MoonPhase.FULL);
        assertNotNull(phaseFull);
        assertEquals(newCalendar(2019, Calendar.MARCH, 21, 02, 43, TIME_ZONE).getTimeInMillis(),
                phaseFull.toEpochMilli(), ACCURACY_IN_MILLIS);
        Instant phaseTQ = moonPhase.getPhase(MoonPhase.THIRD_QUARTER);
        assertNotNull(phaseTQ);
        assertEquals(newCalendar(2019, Calendar.MARCH, 28, 05, 10, TIME_ZONE).getTimeInMillis(), phaseTQ.toEpochMilli(),
                ACCURACY_IN_MILLIS);

        moonPhase = MoonPhaseCalc.calculate(InstantSource.fixed(phaseNew), DateTimeUtils.instantToJulianDay(phaseNew),
                MoonPhaseSet.NONE, ZONE);
        assertEquals(0, moonPhase.getIllumination().doubleValue(), 0.01);

        moonPhase = MoonPhaseCalc.calculate(InstantSource.fixed(phaseFull), DateTimeUtils.instantToJulianDay(phaseFull),
                MoonPhaseSet.NONE, ZONE);
        assertEquals(1, moonPhase.getIllumination().doubleValue(), 0.01);

        moonPhase = MoonPhaseCalc.calculate(InstantSource.fixed(phaseTQ), DateTimeUtils.instantToJulianDay(phaseTQ),
                MoonPhaseSet.NONE, ZONE);
        assertEquals(0.5, moonPhase.getIllumination().doubleValue(), 0.01);

        moonPhase = MoonPhaseCalc.calculate(InstantSource.fixed(phaseFQ), DateTimeUtils.instantToJulianDay(phaseFQ),
                MoonPhaseSet.NONE, ZONE);
        assertEquals(0.5, moonPhase.getIllumination().doubleValue(), 0.01);
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
     * @return a {@link Calendar} set to the given date and time in the specified
     *         time zone, truncated to minute precision.
     */
    private static Calendar newCalendar(int year, int month, int dayOfMonth, int hourOfDay, int minute, TimeZone zone) {
        Calendar result = new GregorianCalendar(zone, Locale.ROOT);
        result.set(year, month, dayOfMonth, hourOfDay, minute);

        return DateTimeUtils.truncateToMinute(result);
    }
}
