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

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Stores a consecutive window result
 *
 * @author Wolfgang Klimt - Initial contribution
 * @author Thomas Leber - Adapted for the Optimal Window automation, add preferStart
 */
@NonNullByDefault
public class ConsecutiveWindowResult extends WindowResult {
    private final List<TimeRange> ranges;

    /**
     * Calculates the consecutive window of the given length with the lowest (or highest) value.
     *
     * Candidate windows start at the start of an interval. A window must not contain gaps in the forecast, and its
     * last interval may be used partially.
     *
     * @param intervals the forecast intervals within the search range, sorted by time
     * @param searchRange the range that was searched
     * @param length the window length in milliseconds
     * @param maximum if true, the window with the highest value is searched instead of the lowest
     * @param preferStart if true, earlier parts of a window are weighted higher, decreasing linearly towards the end,
     *            so windows starting with the best values are preferred
     */
    public ConsecutiveWindowResult(List<ForecastInterval> intervals, TimeRange searchRange, long length,
            boolean maximum, boolean preferStart) {
        super(searchRange);

        int bestIndex = -1;
        double bestScore = 0;
        for (int i = 0; i < intervals.size(); i++) {
            double score = score(intervals, i, length, preferStart);
            if (Double.isNaN(score)) {
                continue;
            }
            if (bestIndex < 0 || (maximum ? score > bestScore : score < bestScore)) {
                bestScore = score;
                bestIndex = i;
            }
        }

        if (bestIndex < 0) {
            ranges = List.of();
            return;
        }

        long windowStart = intervals.get(bestIndex).timerange().start();
        long windowEnd = windowStart + length;
        for (int j = bestIndex; j < intervals.size(); j++) {
            ForecastInterval interval = intervals.get(j);
            long end = Math.min(interval.timerange().end(), windowEnd);
            add(new TimeRange(interval.timerange().start(), end), interval.value());
            if (end >= windowEnd) {
                break;
            }
        }
        ranges = List.of(new TimeRange(windowStart, windowEnd));
    }

    /**
     * Calculate the score of the window starting at the given interval.
     *
     * @return the score, or {@link Double#NaN} if the window does not fit into the forecast
     */
    private static double score(List<ForecastInterval> intervals, int startIndex, long length, boolean preferStart) {
        long windowStart = intervals.get(startIndex).timerange().start();
        long windowEnd = windowStart + length;
        long expectedStart = windowStart;
        double score = 0;

        for (int j = startIndex; j < intervals.size(); j++) {
            TimeRange range = intervals.get(j).timerange();
            if (range.start() != expectedStart) {
                // gap in the forecast
                return Double.NaN;
            }
            long end = Math.min(range.end(), windowEnd);
            // weight by the remaining window length at the start of the interval, e.g. 4, 3, 2, 1 for four hours
            double weight = preferStart ? (double) (windowEnd - range.start()) / length : 1;
            score += weight * intervals.get(j).value() * (end - range.start());
            if (end >= windowEnd) {
                return score;
            }
            expectedStart = range.end();
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
