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
package org.openhab.binding.energycharts.internal;

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link TimeRange} defines a time range (defined by two instants)
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public record TimeRange(Instant start, Instant end) implements Comparable<TimeRange> {
    /**
     * Check if a given instant is in this time range
     *
     * @param instant the instant
     * @return {@code true} if the instant is equal to or after {@link #start} and before {@link #end}
     */
    public boolean contains(Instant instant) {
        return !instant.isBefore(start) && instant.isBefore(end);
    }

    /**
     * Compare two time ranges by their start timestamp
     *
     * @param o the object to be compared
     * @return the result of {@link Instant#compareTo(Instant)} for the {@link #start} instants
     */
    @Override
    public int compareTo(TimeRange o) {
        return start.compareTo(o.start);
    }
}
