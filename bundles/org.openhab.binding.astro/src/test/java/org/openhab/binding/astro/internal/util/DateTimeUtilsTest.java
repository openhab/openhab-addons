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
package org.openhab.binding.astro.internal.util;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import org.junit.jupiter.api.Test;
import org.openhab.binding.astro.internal.config.AstroChannelConfig;

/**
 * Test class for {@link DateTimeUtils}.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 * @author Gaël L'hopital - Added tests for Instant usage
 */
public class DateTimeUtilsTest {

    private static final TimeZone TIME_ZONE = TimeZone.getTimeZone("Europe/Amsterdam");
    private static final Calendar JAN_20_2020 = newCalendar(2020, Calendar.JANUARY, 20, 1, 0, TIME_ZONE);

    @Test
    void testTruncate() {
        Calendar cal = newCalendar(2021, 9, 30, 11, 54, TIME_ZONE);
        Calendar target = newCalendar(2021, 9, 30, 0, 0, TIME_ZONE);
        Calendar truncated = DateTimeUtils.truncateToMidnight(cal);
        assertEquals(truncated, target);
        Calendar endOfDay = DateTimeUtils.endOfDayDate(cal);
        Calendar target2 = newCalendar(2021, 9, 30, 23, 59, TIME_ZONE);
        target2.set(Calendar.SECOND, 59);
        target2.set(Calendar.MILLISECOND, 999);
        assertEquals(endOfDay, target2);
    }

    @Test
    void testTruncateInstant() {
        Instant instant = Instant.parse("2024-03-12T10:15:30.123456Z");
        assertEquals(Instant.parse("2024-03-12T10:15:30Z"), DateTimeUtils.truncateToSecond(instant));
        assertEquals(Instant.parse("2024-03-12T10:15:00Z"), DateTimeUtils.truncateToMinute(instant));
        assertEquals(Instant.parse("2024-03-12T00:00:00Z"), DateTimeUtils.truncateToMidnight(instant));
    }

    @Test
    void testEndOfDayDateInstant() {
        Instant instant = Instant.parse("2024-03-12T10:15:30Z");
        assertEquals(Instant.parse("2024-03-12T23:59:59.999Z"), DateTimeUtils.endOfDayDate(instant));
    }

    @Test
    public void testCreateCalendarForToday() {
        Calendar cal = DateTimeUtils.createCalendarForToday(8, 0, TIME_ZONE, Locale.ROOT);
        assertEquals(8, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, cal.get(Calendar.MINUTE));
        assertEquals(0, cal.get(Calendar.SECOND));
        assertEquals(0, cal.get(Calendar.MILLISECOND));
        cal = DateTimeUtils.createCalendarForToday(22, 59, TIME_ZONE, Locale.ROOT);
        assertEquals(22, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(59, cal.get(Calendar.MINUTE));
        assertEquals(0, cal.get(Calendar.SECOND));
        assertEquals(0, cal.get(Calendar.MILLISECOND));
        cal = DateTimeUtils.createCalendarForToday(0, 0, TIME_ZONE, Locale.ROOT);
        assertEquals(0, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, cal.get(Calendar.MINUTE));
        assertEquals(0, cal.get(Calendar.SECOND));
        assertEquals(0, cal.get(Calendar.MILLISECOND));
    }

    @Test
    public void testAdjustTime() {
        assertEquals(JAN_20_2020, DateTimeUtils.adjustTime(JAN_20_2020, 60));
        assertNotSame(JAN_20_2020, DateTimeUtils.adjustTime(JAN_20_2020, 60));
        assertEquals(JAN_20_2020, DateTimeUtils.adjustTime(JAN_20_2020, -1));
        assertSame(JAN_20_2020, DateTimeUtils.adjustTime(JAN_20_2020, -2));
    }

    @Test
    public void testAdjustTimeInstant() {
        Instant instant = Instant.parse("2024-03-12T10:15:30Z");
        assertEquals(Instant.parse("2024-03-12T01:00:00Z"), DateTimeUtils.adjustTime(instant, 60));
        assertEquals(Instant.parse("2024-03-12T00:00:00Z"), DateTimeUtils.adjustTime(instant, 0));
        assertSame(instant, DateTimeUtils.adjustTime(instant, -1));
    }

    @Test
    public void testGetAdjustedLatestInstant() {
        AstroChannelConfig config = new AstroChannelConfig();
        Instant instant = Instant.parse("2024-03-12T10:15:30Z");
        config.latest = "02:30";
        assertEquals(Instant.parse("2024-03-12T02:30:00Z"), DateTimeUtils.getAdjustedLatest(instant, config));
        config.latest = "00:00";
        assertSame(instant, DateTimeUtils.getAdjustedLatest(instant, config));
        config.latest = null;
        assertSame(instant, DateTimeUtils.getAdjustedLatest(instant, config));
    }

    @Test
    public void testApplyConfig() {
        AstroChannelConfig config = new AstroChannelConfig();
        assertEquals(JAN_20_2020.getTime(), DateTimeUtils.applyConfig(JAN_20_2020, config).getTime());
        assertSame(JAN_20_2020, DateTimeUtils.applyConfig(JAN_20_2020, config));
        config.earliest = "00:00";
        assertEquals(JAN_20_2020, DateTimeUtils.applyConfig(JAN_20_2020, config));
        assertSame(JAN_20_2020, DateTimeUtils.applyConfig(JAN_20_2020, config));
        config.earliest = "00:01";
        assertEquals(JAN_20_2020, DateTimeUtils.applyConfig(JAN_20_2020, config));
        config.earliest = "03:33";
        assertEquals(newCalendar(2020, Calendar.JANUARY, 20, 3, 33, TIME_ZONE),
                DateTimeUtils.applyConfig(JAN_20_2020, config));
        config.earliest = null;
        config.latest = "00:50";
        assertEquals(newCalendar(2020, Calendar.JANUARY, 20, 0, 50, TIME_ZONE),
                DateTimeUtils.applyConfig(JAN_20_2020, config));

        config.latest = null;
        config.offset = -79;
        assertEquals(newCalendar(2020, Calendar.JANUARY, 19, 23, 41, TIME_ZONE),
                DateTimeUtils.applyConfig(JAN_20_2020, config));
        config.earliest = "03:33";
        assertEquals(newCalendar(2020, Calendar.JANUARY, 20, 3, 33, TIME_ZONE),
                DateTimeUtils.applyConfig(JAN_20_2020, config));
        config.earliest = null;
        config.latest = "00:50";
        assertEquals(newCalendar(2020, Calendar.JANUARY, 19, 23, 41, TIME_ZONE),
                DateTimeUtils.applyConfig(JAN_20_2020, config));
        config.latest = null;
        config.offset = 1504;
        assertEquals(newCalendar(2020, Calendar.JANUARY, 21, 2, 4, TIME_ZONE),
                DateTimeUtils.applyConfig(JAN_20_2020, config));
        config.earliest = "03:33";
        assertEquals(newCalendar(2020, Calendar.JANUARY, 21, 2, 4, TIME_ZONE),
                DateTimeUtils.applyConfig(JAN_20_2020, config));
        config.earliest = null;
        config.latest = "21:12";
        assertEquals(newCalendar(2020, Calendar.JANUARY, 20, 21, 12, TIME_ZONE),
                DateTimeUtils.applyConfig(JAN_20_2020, config));
        config.offset = 135;
        assertEquals(newCalendar(2020, Calendar.JANUARY, 20, 3, 15, TIME_ZONE),
                DateTimeUtils.applyConfig(JAN_20_2020, config));
    }

    @Test
    public void testGetMinutesFromTime() {
        assertEquals(-1, DateTimeUtils.getMinutesFromTime(null));
        assertEquals(-1, DateTimeUtils.getMinutesFromTime(" "));
        assertEquals(-1, DateTimeUtils.getMinutesFromTime("2023"));
        assertEquals(1223, DateTimeUtils.getMinutesFromTime("20:23"));
    }

    private static Calendar newCalendar(int year, int month, int dayOfMonth, int hourOfDay, int minute, TimeZone zone) {
        Calendar result = new GregorianCalendar(zone, Locale.ROOT);
        result.set(Calendar.MILLISECOND, 0);
        result.set(year, month, dayOfMonth, hourOfDay, minute, 0);

        return result;
    }
}
