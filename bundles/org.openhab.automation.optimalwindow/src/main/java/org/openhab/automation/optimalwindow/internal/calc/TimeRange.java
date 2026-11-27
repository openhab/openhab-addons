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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link TimeRange} defines a time range (defined by two timestamps)
 *
 * @author Jan N. Klug - Initial contribution
 * @author Thomas Leber - Use {@link Instant}
 */
@NonNullByDefault
public record TimeRange(Instant start, Instant end) implements Comparable<TimeRange> {
    /**
     * Check if a given point in time is in this time range
     *
     * @param pointInTime the point in time
     *
     * @return {@code true} if the point in time is equal to or after {@link #start} and before {@link #end}
     */
    public boolean contains(Instant pointInTime) {
        return !pointInTime.isBefore(start) && pointInTime.isBefore(end);
    }

    /**
     * Returns the part of this time range that is also within the other time range.
     *
     * @param other the other time range
     *
     * @return the overlapping part, or {@code null} if the time ranges do not overlap
     */
    public @Nullable TimeRange intersection(TimeRange other) {
        Instant overlapStart = start.isAfter(other.start) ? start : other.start;
        Instant overlapEnd = end.isBefore(other.end) ? end : other.end;
        return overlapStart.isBefore(overlapEnd) ? new TimeRange(overlapStart, overlapEnd) : null;
    }

    /**
     * @return the duration of this time range
     */
    public Duration duration() {
        return Duration.between(start, end);
    }

    /**
     * Compare two time ranges by their start
     *
     * @param o the object to be compared
     *
     * @return the result of {@link Instant#compareTo(Instant)} for the {@link #start} timestamps
     */
    @Override
    public int compareTo(TimeRange o) {
        return start.compareTo(o.start);
    }
}
