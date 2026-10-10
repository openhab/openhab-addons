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
    private static final Instant START = Instant.parse("2024-11-11T00:00:00Z");
    private static final Duration HOUR = Duration.ofHours(1);
    private static final TimeRange DAY = new TimeRange(START, at(24, 0));

    // aWATTar prices of 2024-11-11, as used by the former aWATTar best price tests
    private static final double[] PRICES = { 103.87, 100.06, 99.06, 99.12, 105.16, 124.96, 143.91, 141.95, 135.95,
            130.39, 124.5, 119.79, 131.13, 133.72, 141.58, 146.94, 150.08, 146.9, 139.87, 123.78, 119.02, 116.87,
            109.72, 107.89 };

    private static Instant at(int hour, int minute) {
        return START.plus(Duration.ofHours(hour).plusMinutes(minute));
    }

    /**
     * Create consecutive intervals starting at {@link #START}, one for each value.
     */
    private static List<ForecastInterval> intervals(Duration step, double... values) {
        List<ForecastInterval> intervals = new ArrayList<>();
        Instant start = START;
        for (double value : values) {
            intervals.add(new ForecastInterval(value, new TimeRange(start, start.plus(step))));
            start = start.plus(step);
        }
        return intervals;
    }

    static List<ForecastInterval> hourly() {
        return intervals(HOUR, PRICES);
    }

    static List<ForecastInterval> quarterHourly() {
        double[] values = new double[PRICES.length * 4];
        for (int i = 0; i < values.length; i++) {
            values[i] = PRICES[i / 4];
        }
        return intervals(Duration.ofMinutes(15), values);
    }

    /**
     * Format the ranges of a result as a list of start hours, like the former aWATTar "hours" channel.
     */
    static String hours(WindowResult result) {
        StringJoiner joiner = new StringJoiner(",");
        for (TimeRange range : result.getRanges()) {
            for (Instant t = range.start(); t.isBefore(range.end()); t = t.plus(HOUR)) {
                joiner.add(String.format("%02d", t.atZone(ZONE).getHour()));
            }
        }
        return joiner.toString();
    }

    @Test
    void consecutive() {
        WindowResult result = new ConsecutiveWindowResult(hourly(), DAY, Duration.ofHours(8), false, false);
        assertEquals("00,01,02,03,04,05,06,07", hours(result));
        assertEquals(START, result.getStart());
        assertEquals(at(8, 0), result.getEnd());
    }

    @Test
    void consecutivePreferStart() {
        // without weighting, 00-03 has the lowest sum, but starts with the most expensive hour
        assertEquals("00,01,02,03",
                hours(new ConsecutiveWindowResult(hourly(), DAY, Duration.ofHours(4), false, false)));

        // with weighting, the range starting with the cheaper hours is preferred
        assertEquals("01,02,03,04",
                hours(new ConsecutiveWindowResult(hourly(), DAY, Duration.ofHours(4), false, true)));
    }

    @Test
    void consecutivePreferStartWithinInterval() {
        // starting 10 minutes before the cheapest hour gives the expensive end of the window a lower weight
        WindowResult result = new ConsecutiveWindowResult(intervals(HOUR, 8, 2, 1, 5), DAY, Duration.ofMinutes(90),
                false, true);
        assertEquals("01:50–03:20", result.getText(ZONE));
    }

    @Test
    void consecutiveMaximum() {
        assertEquals("15,16,17", hours(new ConsecutiveWindowResult(hourly(), DAY, Duration.ofHours(3), true, false)));
    }

    @Test
    void consecutiveQuarterHourlyGivesSameResult() {
        assertEquals("00,01,02,03,04,05,06,07",
                hours(new ConsecutiveWindowResult(quarterHourly(), DAY, Duration.ofHours(8), false, false)));
        assertEquals("01,02,03,04",
                hours(new ConsecutiveWindowResult(quarterHourly(), DAY, Duration.ofHours(4), false, true)));
    }

    @Test
    void consecutivePartialLastInterval() {
        WindowResult result = new ConsecutiveWindowResult(hourly(), DAY, Duration.ofMinutes(90), false, false);
        // 02:00 - 03:30 is cheaper than 01:00 - 02:30
        assertEquals(at(2, 0), result.getStart());
        assertEquals(at(3, 30), result.getEnd());
        assertEquals((99.06 * 60 + 99.12 * 30) / 90, result.getAverage(), 1e-9);
    }

    @Test
    void consecutiveEndingAtIntervalEnd() {
        // 02:30 - 04:00 is cheaper than any window starting at a full hour
        WindowResult result = new ConsecutiveWindowResult(intervals(HOUR, 20, 20, 5, 1, 10, 20), DAY,
                Duration.ofMinutes(90), false, false);
        assertEquals(List.of(new TimeRange(at(2, 30), at(4, 0))), result.getRanges());
        assertEquals((5.0 * 30 + 1.0 * 60) / 90, result.getAverage(), 1e-9);
    }

    @Test
    void consecutiveMinuteIntervals() {
        double[] values = new double[48 * 60];
        for (int i = 0; i < values.length; i++) {
            // cheapest from 13:07 to 13:52
            values[i] = i >= 13 * 60 + 7 && i < 13 * 60 + 52 ? 1.0 : 10.0;
        }
        WindowResult result = new ConsecutiveWindowResult(intervals(Duration.ofMinutes(1), values),
                new TimeRange(START, at(48, 0)), Duration.ofMinutes(45), false, false);
        assertEquals(List.of(new TimeRange(at(13, 7), at(13, 52))), result.getRanges());
    }

    @Test
    void consecutiveDoesNotSpanGaps() {
        List<ForecastInterval> intervals = hourly();
        // remove 02:00, so 01:00, 03:00 and 04:00 must not be treated as consecutive
        intervals.remove(2);
        WindowResult result = new ConsecutiveWindowResult(intervals, DAY, Duration.ofHours(3), false, false);
        assertEquals("03,04,05", hours(result));
    }

    @Test
    void consecutiveWithoutEnoughData() {
        WindowResult result = new ConsecutiveWindowResult(hourly().subList(0, 2), DAY, Duration.ofHours(3), false,
                false);
        assertTrue(result.getRanges().isEmpty());
    }

    @Test
    void nonConsecutive() {
        WindowResult result = new NonConsecutiveWindowResult(hourly(), DAY, Duration.ofHours(6), false);
        assertEquals("00,01,02,03,04,23", hours(result));
        assertEquals(2, result.getRanges().size());
        assertTrue(result.isActive(at(23, 0).plusMillis(1)));
        assertFalse(result.isActive(at(5, 0)));
    }

    @Test
    void windowText() {
        // ending at 04:00 is cheaper than starting at 01:00
        assertEquals("01:30–04:00",
                new ConsecutiveWindowResult(hourly(), DAY, Duration.ofMinutes(150), false, false).getText(ZONE));
        assertEquals("00:00–05:00, 23:00–00:00",
                new NonConsecutiveWindowResult(hourly(), DAY, Duration.ofHours(6), false).getText(ZONE));
    }

    @Test
    void nonConsecutivePartialLastInterval() {
        WindowResult result = new NonConsecutiveWindowResult(hourly(), DAY, Duration.ofMinutes(90), false);
        // 02:00 and 03:00 are the cheapest hours, the second one is only used for 30 minutes
        assertEquals(List.of(new TimeRange(at(2, 0), at(3, 30))), result.getRanges());
        assertEquals((99.06 * 60 + 99.12 * 30) / 90, result.getAverage(), 1e-9);
    }

    @Test
    void nonConsecutivePartialIntervalBeforeSelected() {
        // 03:00 is selected first, so the end of 02:00 is used and the window is not split up
        WindowResult result = new NonConsecutiveWindowResult(intervals(HOUR, 20, 20, 5, 1, 20, 20), DAY,
                Duration.ofMinutes(90), false);
        assertEquals(List.of(new TimeRange(at(2, 30), at(4, 0))), result.getRanges());
    }

    @Test
    void nonConsecutiveMaximum() {
        assertEquals("06,15,16,17", hours(new NonConsecutiveWindowResult(hourly(), DAY, Duration.ofHours(4), true)));
    }
}
