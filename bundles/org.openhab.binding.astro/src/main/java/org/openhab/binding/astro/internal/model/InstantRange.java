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
package org.openhab.binding.astro.internal.model;

import static org.openhab.core.library.unit.MetricPrefix.MILLI;

import java.time.Duration;
import java.time.Instant;

import javax.measure.quantity.Time;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;

/**
 * Immutable range class which holds a start and an end instant.
 *
 * @author Gaël L'hopital- Initial contribution
 */
@NonNullByDefault
public class InstantRange {
    private final Instant start;
    private final Instant end;

    public InstantRange(Instant start, Instant end) {
        if (!start.isBefore(end)) {
            throw new IllegalArgumentException("'start' must be before 'end'");
        }
        this.start = start;
        this.end = end;
    }

    /**
     * Returns the start of the range.
     */
    public Instant getStart() {
        return start;
    }

    /**
     * Returns the end of the range.
     */
    public Instant getEnd() {
        return end;
    }

    /**
     * Returns the duration of the range.
     */
    @Nullable
    public QuantityType<Time> getDuration() {
        return new QuantityType<>(Duration.between(start, end).toMillis(), MILLI(Units.SECOND));
    }
}
