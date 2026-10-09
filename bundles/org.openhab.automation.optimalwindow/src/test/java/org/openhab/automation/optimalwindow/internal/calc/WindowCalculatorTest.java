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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.openhab.core.config.core.Configuration;

/**
 * Tests the {@link WindowCalculator} and the {@link WindowConfiguration}.
 *
 * @author Thomas Leber - Initial contribution
 */
@NonNullByDefault
public class WindowCalculatorTest {
    private static final ZoneId VIENNA = ZoneId.of("Europe/Vienna");
    private static final ZoneId GMT = ZoneId.of("GMT");

    private static ZonedDateTime time(String dateTime, ZoneId zone) {
        return ZonedDateTime.of(LocalDateTime.parse(dateTime), zone);
    }

    private static TimeRange range(String start, String end, ZoneId zone) {
        return new TimeRange(time(start, zone).toInstant(), time(end, zone).toInstant());
    }

    private static Instant minute(int minutes) {
        return Instant.EPOCH.plus(Duration.ofMinutes(minutes));
    }

    @Test
    void rangeLaterToday() {
        assertEquals(range("2026-10-07T22:00", "2026-10-08T06:00", VIENNA),
                WindowCalculator.getRange(LocalTime.of(22, 0), Duration.ofHours(8), time("2026-10-07T10:00", VIENNA)));
    }

    @Test
    void rangeStartedYesterday() {
        assertEquals(range("2026-10-06T22:00", "2026-10-07T06:00", VIENNA),
                WindowCalculator.getRange(LocalTime.of(22, 0), Duration.ofHours(8), time("2026-10-07T01:00", VIENNA)));
    }

    @Test
    void rangeWithMinutes() {
        assertEquals(range("2026-10-07T22:30", "2026-10-08T06:15", VIENNA), WindowCalculator
                .getRange(LocalTime.of(22, 30), Duration.ofMinutes(7 * 60 + 45), time("2026-10-07T22:30", VIENNA)));
        assertEquals(range("2026-10-06T22:30", "2026-10-07T06:15", VIENNA), WindowCalculator
                .getRange(LocalTime.of(22, 30), Duration.ofMinutes(7 * 60 + 45), time("2026-10-07T06:14", VIENNA)));
    }

    @Test
    void rangeStartsAtLocalHourOnDstChange() {
        // the clocks go back at 03:00 on 2026-10-25, the range must still start at 04:00 local time
        assertEquals(time("2026-10-25T04:00", VIENNA).toInstant(), WindowCalculator
                .getRange(LocalTime.of(4, 0), Duration.ofHours(4), time("2026-10-25T00:30", VIENNA)).start());
        // the clocks go forward at 02:00 on 2026-03-29
        assertEquals(time("2026-03-29T04:00", VIENNA).toInstant(), WindowCalculator
                .getRange(LocalTime.of(4, 0), Duration.ofHours(4), time("2026-03-29T00:30", VIENNA)).start());
    }

    @Test
    void intervalsFromTimestamps() {
        SortedMap<Instant, Double> values = new TreeMap<>(Map.of(minute(0), 1.0, minute(60), 2.0, minute(120), 3.0));

        // the last value is valid as long as the previous one, the intervals are clipped to the range
        var intervals = WindowCalculator.toIntervals(values, new TimeRange(minute(30), minute(180)));
        assertEquals(3, intervals.size());
        assertEquals(new TimeRange(minute(30), minute(60)), intervals.get(0).timerange());
        assertEquals(new TimeRange(minute(120), minute(180)), intervals.get(2).timerange());
    }

    @Test
    void intervalsWithMissingValues() {
        // the value of 02:00 is missing
        SortedMap<Instant, Double> values = new TreeMap<>(
                Map.of(minute(0), 1.0, minute(60), 2.0, minute(180), 3.0, minute(240), 4.0));

        var intervals = WindowCalculator.toIntervals(values, new TimeRange(minute(0), minute(300)));
        assertEquals(
                List.of(new TimeRange(minute(0), minute(60)), new TimeRange(minute(60), minute(120)),
                        new TimeRange(minute(180), minute(240)), new TimeRange(minute(240), minute(300))),
                intervals.stream().map(ForecastInterval::timerange).toList());
    }

    @Test
    void intervalsWithChangingSpacing() {
        // quarter-hourly values followed by hourly values are no gap
        SortedMap<Instant, Double> values = new TreeMap<>(Map.of(minute(0), 1.0, minute(15), 2.0, minute(30), 3.0,
                minute(45), 4.0, minute(60), 5.0, minute(120), 6.0));

        var intervals = WindowCalculator.toIntervals(values, new TimeRange(minute(0), minute(180)));
        assertEquals(6, intervals.size());
        assertEquals(new TimeRange(minute(60), minute(120)), intervals.get(4).timerange());
        assertEquals(new TimeRange(minute(120), minute(180)), intervals.get(5).timerange());
    }

    @Test
    void calculateFromForecastSource() {
        ForecastSource source = (item, service, begin, end) -> {
            SortedMap<Instant, Double> values = new TreeMap<>();
            ZonedDateTime t = time("2026-10-07T00:00", GMT);
            for (int i = 0; i < 48 * 4; i++) {
                // cheapest from 02:00 to 04:00 each day
                int hour = t.getHour();
                values.put(t.toInstant(), hour >= 2 && hour < 4 ? 5.0 : 20.0);
                t = t.plusMinutes(15);
            }
            return values;
        };

        WindowConfiguration config = WindowConfiguration
                .from(new Configuration(Map.of("forecastItem", "Price", "length", "2h")));
        WindowResult result = new WindowCalculator(source).calculate(config, time("2026-10-07T01:00", GMT));

        assertNotNull(result);
        assertEquals(time("2026-10-07T02:00", GMT).toInstant(), result.getStart());
        assertEquals(time("2026-10-07T04:00", GMT).toInstant(), result.getEnd());
        assertEquals(5.0, result.getAverage(), 1e-9);
        assertTrue(result.isActive(Instant.parse("2026-10-07T03:00:00Z")));
    }

    @Test
    void calculateWithRangeNotMatchingIntervals() {
        ForecastSource source = (item, service, begin, end) -> {
            SortedMap<Instant, Double> values = new TreeMap<>();
            ZonedDateTime t = time("2026-10-07T00:00", GMT);
            for (int hour = 0; hour < 24; hour++) {
                values.put(t.plusHours(hour).toInstant(), hour == 1 ? 1.0 : hour == 4 ? 2.0 : 20.0);
            }
            return values;
        };

        // the range starts and ends within an hour, only the parts of 01:00 and 04:00 within the range can be used
        WindowConfiguration config = WindowConfiguration.from(new Configuration(Map.of("forecastItem", "Price",
                "rangeStart", "01:30", "rangeDuration", "3h", "length", "1h", "consecutive", false)));
        WindowResult result = new WindowCalculator(source).calculate(config, time("2026-10-07T01:00", GMT));

        assertNotNull(result);
        assertEquals(List.of(range("2026-10-07T01:30", "2026-10-07T02:00", GMT),
                range("2026-10-07T04:00", "2026-10-07T04:30", GMT)), result.getRanges());
        assertEquals(1.5, result.getAverage(), 1e-9);
    }

    @Test
    void calculateWithoutFullCoverage() {
        ForecastSource source = (item, service, begin, end) -> new TreeMap<>(
                Map.of(time("2026-10-07T00:00", GMT).toInstant(), 1.0, time("2026-10-07T06:00", GMT).toInstant(), 2.0));

        WindowConfiguration config = WindowConfiguration
                .from(new Configuration(Map.of("forecastItem", "Price", "length", "1h")));
        @Nullable
        WindowResult result = new WindowCalculator(source).calculate(config, time("2026-10-07T01:00", GMT));
        assertNull(result);
    }

    @Test
    void configuration() {
        WindowConfiguration config = WindowConfiguration
                .from(new Configuration(Map.of("forecastItem", "Price", "rangeStart", "22:30", "rangeDuration", "7h45m",
                        "length", "1h30m", "consecutive", false, "goal", "maximum")));
        assertEquals(LocalTime.of(22, 30), config.rangeStart);
        assertEquals(Duration.ofMinutes(7 * 60 + 45), config.rangeDuration);
        assertEquals(Duration.ofMinutes(90), config.length);
        assertFalse(config.consecutive);
        assertTrue(config.maximum);
    }

    @Test
    void unresolvedTemplateReferencesAreNotSet() {
        WindowConfiguration config = WindowConfiguration.from(new Configuration(Map.of("forecastItem", "Price",
                "persistenceService", "{{persistenceService}}", "rangeStart", "{{rangeStart}}", "rangeDuration", " ",
                "length", "2h", "goal", "{{goal}}", "preferStart", "{{preferStart}}")));
        assertNull(config.persistenceService);
        assertEquals(LocalTime.MIDNIGHT, config.rangeStart);
        assertEquals(Duration.ofHours(24), config.rangeDuration);
        assertEquals(Duration.ofHours(2), config.length);
        assertFalse(config.maximum);
        assertFalse(config.preferStart);

        assertThrows(IllegalArgumentException.class,
                () -> WindowConfiguration.from(new Configuration(Map.of("forecastItem", "{{forecastItem}}"))));
        // length is required, an unresolved reference is not enough
        assertThrows(IllegalArgumentException.class, () -> WindowConfiguration
                .from(new Configuration(Map.of("forecastItem", "Price", "length", "{{length}}"))));
        assertThrows(IllegalArgumentException.class, () -> WindowConfiguration
                .from(new Configuration(Map.of("forecastItem", "Price", "length", "1h", "rangeStart", "late"))));
    }

    @Test
    void invalidConfiguration() {
        assertThrows(IllegalArgumentException.class,
                () -> WindowConfiguration.from(new Configuration(Map.of("forecastItem", "Price"))));
        assertThrows(IllegalArgumentException.class, () -> WindowConfiguration.from(new Configuration()));
        assertThrows(IllegalArgumentException.class, () -> WindowConfiguration
                .from(new Configuration(Map.of("forecastItem", "Price", "rangeDuration", "2h", "length", "3h"))));
        assertThrows(IllegalArgumentException.class, () -> WindowConfiguration
                .from(new Configuration(Map.of("forecastItem", "Price", "length", "1h", "goal", "cheap"))));
        assertThrows(IllegalArgumentException.class, () -> WindowConfiguration
                .from(new Configuration(Map.of("forecastItem", "Price", "length", "1h", "rangeDuration", "49h"))));
        assertThrows(IllegalArgumentException.class, () -> WindowConfiguration
                .from(new Configuration(Map.of("forecastItem", "Price", "length", "1h", "rangeStart", "25:00"))));
        // a duration needs a unit, and a time needs minutes
        assertThrows(IllegalArgumentException.class, () -> WindowConfiguration
                .from(new Configuration(Map.of("forecastItem", "Price", "length", "1h", "rangeDuration", "8"))));
        assertThrows(IllegalArgumentException.class,
                () -> WindowConfiguration.from(new Configuration(Map.of("forecastItem", "Price", "length", 1))));
        assertThrows(IllegalArgumentException.class, () -> WindowConfiguration
                .from(new Configuration(Map.of("forecastItem", "Price", "length", "1h", "rangeStart", 22))));
        // the smallest unit is a minute
        assertThrows(IllegalArgumentException.class, () -> WindowConfiguration
                .from(new Configuration(Map.of("forecastItem", "Price", "length", "1h", "rangeStart", "22:00:30"))));
        assertThrows(IllegalArgumentException.class,
                () -> WindowConfiguration.from(new Configuration(Map.of("forecastItem", "Price", "length", "1m30s"))));
    }
}
