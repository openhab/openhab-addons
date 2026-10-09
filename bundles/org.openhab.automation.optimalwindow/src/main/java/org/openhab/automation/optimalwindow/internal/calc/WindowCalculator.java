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

import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Calculates the optimal window for a {@link WindowConfiguration}.
 *
 * @author Wolfgang Klimt - Initial contribution
 * @author Thomas Leber - Adapted for the Optimal Window automation
 */
@NonNullByDefault
public class WindowCalculator {
    private final ForecastSource forecastSource;

    public WindowCalculator(ForecastSource forecastSource) {
        this.forecastSource = forecastSource;
    }

    /**
     * Calculate the window for the current search range.
     *
     * @param config the window configuration
     * @param now the current time
     *
     * @return the result, or {@code null} if the forecast does not cover the search range
     *
     * @throws IllegalStateException if the forecast cannot be retrieved
     */
    public @Nullable WindowResult calculate(WindowConfiguration config, ZonedDateTime now)
            throws IllegalStateException {
        TimeRange range = getRange(config.rangeStart, config.rangeDuration, now);

        // start one day earlier, so the value that is valid at the start of the range is included
        ZonedDateTime begin = range.start().atZone(now.getZone()).minusDays(1);
        ZonedDateTime end = range.end().atZone(now.getZone()).plusDays(1);
        SortedMap<Instant, Double> values = forecastSource.getValues(config.forecastItem, config.persistenceService,
                begin, end);

        List<ForecastInterval> intervals = toIntervals(values, range);
        if (intervals.isEmpty() || !intervals.getFirst().timerange().start().equals(range.start())
                || !intervals.getLast().timerange().end().equals(range.end())) {
            return null;
        }

        WindowResult result = config.consecutive
                ? new ConsecutiveWindowResult(intervals, range, config.length, config.maximum, config.preferStart)
                : new NonConsecutiveWindowResult(intervals, range, config.length, config.maximum);
        return result.getRanges().isEmpty() ? null : result;
    }

    /**
     * Convert the values into intervals, clipped to the given range. Each value is valid until the next timestamp,
     * unless values are missing in between. The last value is valid for as long as the previous one.
     *
     * @param values the values by their timestamp
     * @param range the range to clip the intervals to
     *
     * @return the intervals overlapping the range, sorted by time
     */
    static List<ForecastInterval> toIntervals(SortedMap<Instant, Double> values, TimeRange range) {
        List<ForecastInterval> result = new ArrayList<>();
        List<Double> forecast = new ArrayList<>(values.values());
        List<Instant> timestamps = new ArrayList<>(values.keySet());

        for (int i = 0; i < timestamps.size(); i++) {
            TimeRange clipped = new TimeRange(timestamps.get(i), getEnd(timestamps, i)).intersection(range);
            if (clipped != null) {
                result.add(new ForecastInterval(forecast.get(i), clipped));
            }
        }
        return result;
    }

    /**
     * Returns the end of the interval starting at the given timestamp. A spacing that is longer than both neighboring
     * spacings means that values are missing, so the value is only valid as long as the previous spacing, leaving a gap
     * until the next timestamp. The first and the last spacing are never treated as gap.
     *
     * @return the end of the interval, or the start if its length is unknown
     */
    private static Instant getEnd(List<Instant> timestamps, int i) {
        Instant start = timestamps.get(i);
        Duration previous = i > 0 ? Duration.between(timestamps.get(i - 1), start) : Duration.ZERO;
        if (i + 1 >= timestamps.size()) {
            return start.plus(previous);
        }
        Instant end = timestamps.get(i + 1);
        if (i > 0 && i + 2 < timestamps.size()) {
            Duration spacing = Duration.between(start, end);
            Duration next = Duration.between(end, timestamps.get(i + 2));
            if (spacing.compareTo(previous) > 0 && spacing.compareTo(next) > 0) {
                return start.plus(previous);
            }
        }
        return end;
    }

    /**
     * Returns the search range for the given start time and duration.
     *
     * @param start the local start time of the range
     * @param duration the duration of the range
     * @param now the current time
     * 
     * @return the range
     */
    public static TimeRange getRange(LocalTime start, Duration duration, ZonedDateTime now) {
        ZonedDateTime startTime = now.toLocalDate().atTime(start).atZone(now.getZone());
        if (now.toLocalTime().isBefore(start)) {
            // we are before the range, so we might be still within the last range
            startTime = startTime.minusDays(1);
        }
        ZonedDateTime endTime = startTime.plus(duration);
        if (!endTime.isAfter(now)) {
            // span is in the past, add one day
            startTime = startTime.plusDays(1);
            endTime = startTime.plus(duration);
        }
        return new TimeRange(startTime.toInstant(), endTime.toInstant());
    }
}
