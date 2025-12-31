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
package org.openhab.binding.astro.internal.calc.zodiac;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.astro.internal.calc.MoonCalc;
import org.openhab.binding.astro.internal.model.Moon;
import org.openhab.binding.astro.internal.model.ZodiacSign;
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
public class MoonZodiacCalcTest {

    private static final TimeZone TIME_ZONE = TimeZone.getTimeZone("Europe/Amsterdam");
    private static final Calendar FEB_27_2019 = MoonZodiacCalcTest.newCalendar(2019, Calendar.FEBRUARY, 27, 1, 0,
            TIME_ZONE);
    private static final double AMSTERDAM_LATITUDE = 52.367607;
    private static final double AMSTERDAM_LONGITUDE = 4.8978293;

    private @Nullable MoonCalc moonCalc;

    @BeforeEach
    public void init() {
        moonCalc = new MoonCalc();
    }

    @Test
    public void testGetMoonInfoForZodiac() {
        MoonCalc moonCalc = this.moonCalc;
        assertNotNull(moonCalc);
        Moon moon = moonCalc.getMoonInfo(FEB_27_2019, AMSTERDAM_LATITUDE, AMSTERDAM_LONGITUDE, TIME_ZONE, Locale.ROOT);
        moonCalc.setPositionalInfo(FEB_27_2019, AMSTERDAM_LATITUDE, AMSTERDAM_LONGITUDE, moon, TIME_ZONE, Locale.ROOT);

        assertEquals(ZodiacSign.SAGITTARIUS, moon.getZodiac().getSign());
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
