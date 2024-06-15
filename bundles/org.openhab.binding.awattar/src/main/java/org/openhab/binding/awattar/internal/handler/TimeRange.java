/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.awattar.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link TimeRange} defines a time range (defined by two timestamps)
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public record TimeRange(long start, long end) implements Comparable<TimeRange> {
    /**
     * Check if a given timestamp is in this time range
     *
     * @param timestamp the timestamp
     * @return {@code true} if the timestamp is equal to or greater than {@link #start} and less than {@link #end}
     */
    public boolean contains(long timestamp) {
        return timestamp >= start && timestamp < end;
    }

    /**
     * Check if another time range is inside this time range
     *
     * @param other the other time range
     * @return {@code true} if {@link #start} of this time range is the same or before the other time range's
     *         {@link #start} and this {@link #end} is the same or after the other time range's {@link #end}
     */
    public boolean contains(TimeRange other) {
        return start <= other.start && end >= other.end;
    }

    /**
     * Compare two time ranges by their start timestamp
     *
     * @param o the object to be compared
     * @return the result of {@link Long#compare(long, long)} for the {@link #start} timestamps
     */
    public int compareTo(TimeRange o) {
        return Long.compare(start, o.start);
    }
}
