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
package org.openhab.automation.optimalwindow.internal.calc;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Tests the {@link ConsecutiveWindowResult} and {@link NonConsecutiveWindowResult} logic.
 *
 * @author Thomas Leber - Initial contribution
 */
@NonNullByDefault
public class WindowResultTest {
    private static final ZoneId ZONE = ZoneId.of("GMT");
    private static final long START = 1731283200000L;
    private static final long HOUR = Duration.ofHours(1).toMillis();
    private static final TimeRange DAY = new TimeRange(START, START + 24 * HOUR);

    // aWATTar prices of 2024-11-11, as used by the former aWATTar best price tests
    private static final double[] PRICES = { 103.87, 100.06, 99.06, 99.12, 105.16, 124.96, 143.91, 141.95, 135.95,
            130.39, 124.5, 119.79, 131.13, 133.72, 141.58, 146.94, 150.08, 146.9, 139.87, 123.78, 119.02, 116.87,
            109.72, 107.89 };

    static List<ForecastInterval> hourly() {
        List<ForecastInterval> intervals = new ArrayList<>();
        for (int i = 0; i < PRICES.length; i++) {
            intervals.add(new ForecastInterval(PRICES[i], new TimeRange(START + i * HOUR, START + (i + 1) * HOUR)));
        }
        return intervals;
    }

    static List<ForecastInterval> quarterHourly() {
        List<ForecastInterval> intervals = new ArrayList<>();
        long quarter = HOUR / 4;
        for (int i = 0; i < PRICES.length * 4; i++) {
            intervals.add(
                    new ForecastInterval(PRICES[i / 4], new TimeRange(START + i * quarter, START + (i + 1) * quarter)));
        }
        return intervals;
    }

    /**
     * Format the ranges of a result as a list of start hours, like the former aWATTar "hours" channel.
     */
    static String hours(WindowResult result) {
        StringJoiner joiner = new StringJoiner(",");
        for (TimeRange range : result.getRanges()) {
            for (long t = range.start(); t < range.end(); t += HOUR) {
                joiner.add(String.format("%02d", ZonedDateTime.ofInstant(Instant.ofEpochMilli(t), ZONE).getHour()));
            }
        }
        return joiner.toString();
    }

    @Test
    void consecutive() {
        WindowResult result = new ConsecutiveWindowResult(hourly(), DAY, 8 * HOUR, false, false);
        assertEquals("00,01,02,03,04,05,06,07", hours(result));
        assertEquals(START, result.getStart());
        assertEquals(START + 8 * HOUR, result.getEnd());
    }

    @Test
    void consecutivePreferStart() {
        // without weighting, 00-03 has the lowest sum, but starts with the most expensive hour
        assertEquals("00,01,02,03", hours(new ConsecutiveWindowResult(hourly(), DAY, 4 * HOUR, false, false)));

        // with weighting, the range starting with the cheaper hours is preferred
        assertEquals("01,02,03,04", hours(new ConsecutiveWindowResult(hourly(), DAY, 4 * HOUR, false, true)));
    }

    @Test
    void consecutiveMaximum() {
        assertEquals("15,16,17", hours(new ConsecutiveWindowResult(hourly(), DAY, 3 * HOUR, true, false)));
    }

    @Test
    void consecutiveQuarterHourlyGivesSameResult() {
        assertEquals("00,01,02,03,04,05,06,07",
                hours(new ConsecutiveWindowResult(quarterHourly(), DAY, 8 * HOUR, false, false)));
        assertEquals("01,02,03,04", hours(new ConsecutiveWindowResult(quarterHourly(), DAY, 4 * HOUR, false, true)));
    }

    @Test
    void consecutivePartialLastInterval() {
        WindowResult result = new ConsecutiveWindowResult(hourly(), DAY, 90 * 60000L, false, false);
        // 02:00 - 03:30 is cheaper than 01:00 - 02:30
        assertEquals(START + 2 * HOUR, result.getStart());
        assertEquals(START + 2 * HOUR + 90 * 60000L, result.getEnd());
        assertEquals((99.06 * 60 + 99.12 * 30) / 90, result.getAverage(), 1e-9);
    }

    @Test
    void consecutiveDoesNotSpanGaps() {
        List<ForecastInterval> intervals = hourly();
        // remove 02:00, so 01:00, 03:00 and 04:00 must not be treated as consecutive
        intervals.remove(2);
        WindowResult result = new ConsecutiveWindowResult(intervals, DAY, 3 * HOUR, false, false);
        assertEquals("03,04,05", hours(result));
    }

    @Test
    void consecutiveWithoutEnoughData() {
        WindowResult result = new ConsecutiveWindowResult(hourly().subList(0, 2), DAY, 3 * HOUR, false, false);
        assertTrue(result.getRanges().isEmpty());
    }

    @Test
    void nonConsecutive() {
        WindowResult result = new NonConsecutiveWindowResult(hourly(), DAY, 6 * HOUR, false);
        assertEquals("00,01,02,03,04,23", hours(result));
        assertEquals(2, result.getRanges().size());
        assertTrue(result.isActive(Instant.ofEpochMilli(START + 23 * HOUR + 1)));
        assertFalse(result.isActive(Instant.ofEpochMilli(START + 5 * HOUR)));
    }

    @Test
    void windowText() {
        assertEquals("01:00\u201303:30",
                new ConsecutiveWindowResult(hourly(), DAY, 150 * 60000L, false, false).getText(ZONE));
        assertEquals("00:00\u201305:00, 23:00\u201300:00",
                new NonConsecutiveWindowResult(hourly(), DAY, 6 * HOUR, false).getText(ZONE));
    }

    @Test
    void nonConsecutiveMaximum() {
        assertEquals("06,15,16,17", hours(new NonConsecutiveWindowResult(hourly(), DAY, 4 * HOUR, true)));
    }
}
