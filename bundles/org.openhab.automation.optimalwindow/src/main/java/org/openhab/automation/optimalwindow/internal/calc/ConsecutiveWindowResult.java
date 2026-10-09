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
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Stores a consecutive window result
 *
 * @author Wolfgang Klimt - Initial contribution
 * @author Thomas Leber - Adapted for the Optimal Window automation, add preferStart
 */
@NonNullByDefault
public class ConsecutiveWindowResult extends WindowResult {
    private static final Duration MINUTE = Duration.ofMinutes(1);

    private final List<TimeRange> ranges;

    /**
     * Calculates the consecutive window of the given length with the lowest (or highest) value.
     *
     * Every whole minute of the search range is tried as start of the window, so the window does not have to match the
     * forecast intervals. A window must not contain gaps in the forecast, and its first and last interval may be used
     * partially.
     *
     * @param intervals the forecast intervals within the search range, sorted by time
     * @param searchRange the range that was searched
     * @param length the window length
     * @param maximum if true, the window with the highest value is searched instead of the lowest
     * @param preferStart if true, earlier parts of a window are weighted higher, decreasing linearly towards the end,
     *            so windows starting with the best values are preferred
     */
    public ConsecutiveWindowResult(List<ForecastInterval> intervals, TimeRange searchRange, Duration length,
            boolean maximum, boolean preferStart) {
        super(searchRange);

        @Nullable
        TimeRange best = null;
        double bestScore = Double.NaN;
        int first = 0;
        for (Instant start = searchRange.start(); !start.plus(length).isAfter(searchRange.end()); start = start
                .plus(MINUTE)) {
            // skip the intervals that end before the window
            while (first < intervals.size() && !intervals.get(first).timerange().end().isAfter(start)) {
                first++;
            }
            TimeRange window = new TimeRange(start, start.plus(length));
            double score = score(intervals, first, window, preferStart);
            if (!Double.isNaN(score)
                    && (Double.isNaN(bestScore) || (maximum ? score > bestScore : score < bestScore))) {
                bestScore = score;
                best = window;
            }
        }

        if (best == null) {
            ranges = List.of();
            return;
        }

        for (ForecastInterval interval : intervals) {
            TimeRange part = interval.timerange().intersection(best);
            if (part != null) {
                add(part, interval.value());
            }
        }
        ranges = List.of(best);
    }

    /**
     * Calculate the score of the given window.
     *
     * @param first the index of the first interval that ends after the window start
     *
     * @return the score, or {@link Double#NaN} if the window does not fit into the forecast
     */
    private static double score(List<ForecastInterval> intervals, int first, TimeRange window, boolean preferStart) {
        double length = window.duration().toMillis();
        Instant expectedStart = window.start();
        double score = 0;

        for (int j = first; j < intervals.size(); j++) {
            TimeRange part = intervals.get(j).timerange().intersection(window);
            if (part == null || !part.start().equals(expectedStart)) {
                // gap in the forecast, or the window starts before the forecast
                return Double.NaN;
            }
            double partLength = part.duration().toMillis();
            // the weight decreases linearly from 1 at the start of the window to 0 at its end, the average weight of
            // this part is the weight at its middle, so the score does not depend on the length of the intervals
            double weight = preferStart
                    ? (Duration.between(part.start(), window.end()).toMillis() - partLength / 2) / length
                    : 1;
            score += weight * intervals.get(j).value() * partLength;
            if (part.end().equals(window.end())) {
                return score;
            }
            expectedStart = part.end();
        }

        // not enough forecast data for the full window
        return Double.NaN;
    }

    @Override
    public List<TimeRange> getRanges() {
        return ranges;
    }

    @Override
    public String toString() {
        return String.format("ConsecutiveWindowResult %s, average %.3f", ranges, getAverage());
    }
}
