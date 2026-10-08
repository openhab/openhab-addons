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
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Base class for results
 *
 * @author Wolfgang Klimt - Initial contribution
 * @author Thomas Leber - Adapted for the Optimal Window automation
 */
@NonNullByDefault
public abstract class WindowResult {
    private final TimeRange searchRange;
    private long start;
    private long end;
    private double weightedSum;
    private long totalDuration;

    protected WindowResult(TimeRange searchRange) {
        this.searchRange = searchRange;
    }

    /**
     * @return the range that was searched for this result
     */
    public TimeRange getSearchRange() {
        return searchRange;
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }

    /**
     * @return the average forecast value within the window, weighted by duration
     */
    public double getAverage() {
        return totalDuration > 0 ? weightedSum / totalDuration : Double.NaN;
    }

    /**
     * Add a time range with its value to this result.
     *
     * @param range the time range
     * @param value the forecast value of the range
     */
    protected void add(TimeRange range, double value) {
        if (start == 0 || start > range.start()) {
            start = range.start();
        }
        if (end == 0 || end < range.end()) {
            end = range.end();
        }
        weightedSum += value * range.duration();
        totalDuration += range.duration();
    }

    /**
     * Returns true if the window is active.
     *
     * @param pointInTime the current time
     *
     * @return true if the window is active, false otherwise
     */
    public boolean isActive(Instant pointInTime) {
        long millis = pointInTime.toEpochMilli();
        return getRanges().stream().anyMatch(range -> range.contains(millis));
    }

    /**
     * Returns the window as compact text, e.g. {@code 10:45–14:45} or {@code 02:00–04:00, 23:00–00:00} when the
     * window is not consecutive.
     *
     * @param zoneId the time zone to show the times in
     * 
     * @return the window as text
     */
    public String getText(ZoneId zoneId) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm").withZone(zoneId);
        return getRanges().stream().map(range -> formatter.format(Instant.ofEpochMilli(range.start())) + "\u2013"
                + formatter.format(Instant.ofEpochMilli(range.end()))).collect(Collectors.joining(", "));
    }

    /**
     * Returns the time ranges of the window, sorted and with adjacent ranges merged.
     *
     * @return the time ranges of the window
     */
    public abstract List<TimeRange> getRanges();
}
