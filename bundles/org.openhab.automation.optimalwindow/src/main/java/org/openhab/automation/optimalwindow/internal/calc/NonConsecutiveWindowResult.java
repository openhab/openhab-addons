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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Stores a non consecutive window result
 *
 * @author Wolfgang Klimt - Initial contribution
 * @author Thomas Leber - Adapted for the Optimal Window automation
 */
@NonNullByDefault
public class NonConsecutiveWindowResult extends WindowResult {
    private final List<TimeRange> ranges = new ArrayList<>();

    /**
     * Selects the intervals with the lowest (or highest) values until their total duration reaches the given length.
     *
     * @param intervals the forecast intervals within the search range
     * @param searchRange the range that was searched
     * @param length the total length in milliseconds
     * @param maximum if true, the intervals with the highest values are selected instead of the lowest
     */
    public NonConsecutiveWindowResult(List<ForecastInterval> intervals, TimeRange searchRange, long length,
            boolean maximum) {
        super(searchRange);

        Comparator<ForecastInterval> byValue = Comparator.comparingDouble(ForecastInterval::value);
        if (maximum) {
            byValue = byValue.reversed();
        }
        // on equal values, prefer earlier intervals
        List<ForecastInterval> sorted = new ArrayList<>(intervals);
        sorted.sort(byValue.thenComparing(ForecastInterval::timerange));

        List<TimeRange> members = new ArrayList<>();
        long total = 0;
        for (ForecastInterval interval : sorted) {
            if (total >= length) {
                break;
            }
            members.add(interval.timerange());
            add(interval.timerange(), interval.value());
            total += interval.timerange().duration();
        }

        // sort the members and merge adjacent ranges
        members.sort(Comparator.naturalOrder());
        for (TimeRange member : members) {
            int last = ranges.size() - 1;
            if (last >= 0 && ranges.get(last).end() == member.start()) {
                ranges.set(last, new TimeRange(ranges.get(last).start(), member.end()));
            } else {
                ranges.add(member);
            }
        }
    }

    @Override
    public List<TimeRange> getRanges() {
        return ranges;
    }

    @Override
    public String toString() {
        return String.format("NonConsecutiveWindowResult %s, average %.3f", ranges, getAverage());
    }
}
