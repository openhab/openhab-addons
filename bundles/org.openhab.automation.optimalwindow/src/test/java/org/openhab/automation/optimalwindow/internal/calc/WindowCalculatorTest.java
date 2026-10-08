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
import java.time.ZoneId;
import java.time.ZonedDateTime;
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
        return new TimeRange(time(start, zone).toInstant().toEpochMilli(), time(end, zone).toInstant().toEpochMilli());
    }

    @Test
    void rangeLaterToday() {
        assertEquals(range("2026-10-07T22:00", "2026-10-08T06:00", VIENNA),
                WindowCalculator.getRange(22, 8, time("2026-10-07T10:00", VIENNA)));
    }

    @Test
    void rangeStartedYesterday() {
        assertEquals(range("2026-10-06T22:00", "2026-10-07T06:00", VIENNA),
                WindowCalculator.getRange(22, 8, time("2026-10-07T01:00", VIENNA)));
    }

    @Test
    void rangeStartsAtLocalHourOnDstChange() {
        // the clocks go back at 03:00 on 2026-10-25, the range must still start at 04:00 local time
        assertEquals(time("2026-10-25T04:00", VIENNA).toInstant().toEpochMilli(),
                WindowCalculator.getRange(4, 4, time("2026-10-25T00:30", VIENNA)).start());
        // the clocks go forward at 02:00 on 2026-03-29
        assertEquals(time("2026-03-29T04:00", VIENNA).toInstant().toEpochMilli(),
                WindowCalculator.getRange(4, 4, time("2026-03-29T00:30", VIENNA)).start());
    }

    @Test
    void intervalsFromTimestamps() {
        long hour = Duration.ofHours(1).toMillis();
        SortedMap<Long, Double> values = new TreeMap<>(Map.of(0L, 1.0, hour, 2.0, 2 * hour, 3.0));

        // the last value is valid as long as the previous one, the intervals are clipped to the range
        var intervals = WindowCalculator.toIntervals(values, new TimeRange(hour / 2, 3 * hour));
        assertEquals(3, intervals.size());
        assertEquals(new TimeRange(hour / 2, hour), intervals.get(0).timerange());
        assertEquals(new TimeRange(2 * hour, 3 * hour), intervals.get(2).timerange());
    }

    @Test
    void calculateFromForecastSource() {
        ForecastSource source = (item, service, begin, end) -> {
            SortedMap<Long, Double> values = new TreeMap<>();
            ZonedDateTime t = time("2026-10-07T00:00", GMT);
            for (int i = 0; i < 48 * 4; i++) {
                // cheapest from 02:00 to 04:00 each day
                int hour = t.getHour();
                values.put(t.toInstant().toEpochMilli(), hour >= 2 && hour < 4 ? 5.0 : 20.0);
                t = t.plusMinutes(15);
            }
            return values;
        };

        WindowConfiguration config = WindowConfiguration
                .from(new Configuration(Map.of("forecastItem", "Price", "length", "2h")));
        WindowResult result = new WindowCalculator(source).calculate(config, time("2026-10-07T01:00", GMT));

        assertNotNull(result);
        assertEquals(time("2026-10-07T02:00", GMT).toInstant().toEpochMilli(), result.getStart());
        assertEquals(time("2026-10-07T04:00", GMT).toInstant().toEpochMilli(), result.getEnd());
        assertEquals(5.0, result.getAverage(), 1e-9);
        assertTrue(result.isActive(Instant.parse("2026-10-07T03:00:00Z")));
    }

    @Test
    void calculateWithoutFullCoverage() {
        ForecastSource source = (item, service, begin,
                end) -> new TreeMap<>(Map.of(time("2026-10-07T00:00", GMT).toInstant().toEpochMilli(), 1.0,
                        time("2026-10-07T06:00", GMT).toInstant().toEpochMilli(), 2.0));

        WindowConfiguration config = WindowConfiguration
                .from(new Configuration(Map.of("forecastItem", "Price", "length", "1h")));
        @Nullable
        WindowResult result = new WindowCalculator(source).calculate(config, time("2026-10-07T01:00", GMT));
        assertNull(result);
    }

    @Test
    void configuration() {
        WindowConfiguration config = WindowConfiguration.from(new Configuration(Map.of("forecastItem", "Price",
                "rangeStart", 22, "rangeDuration", 8, "length", "1h30m", "consecutive", false, "goal", "maximum")));
        assertEquals(22, config.rangeStart);
        assertEquals(8, config.rangeDuration);
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
        assertEquals(0, config.rangeStart);
        assertEquals(24, config.rangeDuration);
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
                .from(new Configuration(Map.of("forecastItem", "Price", "rangeDuration", 2, "length", "3h"))));
        assertThrows(IllegalArgumentException.class, () -> WindowConfiguration
                .from(new Configuration(Map.of("forecastItem", "Price", "length", "1h", "goal", "cheap"))));
    }
}
