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

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
        ZonedDateTime begin = ZonedDateTime.ofInstant(Instant.ofEpochMilli(range.start()), now.getZone()).minusDays(1);
        ZonedDateTime end = ZonedDateTime.ofInstant(Instant.ofEpochMilli(range.end()), now.getZone()).plusDays(1);
        SortedMap<Long, Double> values = forecastSource.getValues(config.forecastItem, config.persistenceService, begin,
                end);

        List<ForecastInterval> intervals = toIntervals(values, range);
        if (intervals.isEmpty() || (intervals.getFirst().timerange().start() != range.start())
                || (intervals.getLast().timerange().end() != range.end())) {
            return null;
        }

        long length = config.length.toMillis();
        WindowResult result = config.consecutive
                ? new ConsecutiveWindowResult(intervals, range, length, config.maximum, config.preferStart)
                : new NonConsecutiveWindowResult(intervals, range, length, config.maximum);
        return result.getRanges().isEmpty() ? null : result;
    }

    /**
     * Convert the values into intervals, clipped to the given range. Each value is valid until the next timestamp.
     * The last value is valid for as long as the previous one.
     *
     * @param values the values by their timestamp in epoch milliseconds
     * @param range the range to clip the intervals to
     *
     * @return the intervals overlapping the range, sorted by time
     */
    static List<ForecastInterval> toIntervals(SortedMap<Long, Double> values, TimeRange range) {
        List<ForecastInterval> result = new ArrayList<>();
        List<Map.Entry<Long, Double>> entries = new ArrayList<>(values.entrySet());

        for (int i = 0; i < entries.size(); i++) {
            long start = entries.get(i).getKey();
            long end;
            if (i + 1 < entries.size()) {
                end = entries.get(i + 1).getKey();
            } else if (i > 0) {
                end = start + (start - entries.get(i - 1).getKey());
            } else {
                continue;
            }

            long clippedStart = Math.max(start, range.start());
            long clippedEnd = Math.min(end, range.end());
            if (clippedStart < clippedEnd) {
                result.add(new ForecastInterval(entries.get(i).getValue(), new TimeRange(clippedStart, clippedEnd)));
            }
        }
        return result;
    }

    /**
     * Returns the search range for the given start hour and duration.
     *
     * @param start the start hour (0-23)
     * @param duration the duration in hours
     * @param now the current time
     * 
     * @return the range
     */
    public static TimeRange getRange(int start, int duration, ZonedDateTime now) {
        ZonedDateTime startTime = now.toLocalDate().atTime(start, 0).atZone(now.getZone());
        if (now.getHour() < start) {
            // we are before the range, so we might be still within the last range
            startTime = startTime.minusDays(1);
        }
        ZonedDateTime endTime = startTime.plusHours(duration);
        if (!endTime.isAfter(now)) {
            // span is in the past, add one day
            startTime = startTime.plusDays(1);
            endTime = startTime.plusHours(duration);
        }
        return new TimeRange(startTime.toInstant().toEpochMilli(), endTime.toInstant().toEpochMilli());
    }
}
