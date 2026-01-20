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

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Holds the sign of the zodiac.
 *
 * @author Gerhard Riegler - Initial contribution
 * @author Gaël L'hopital - Immutable & Instant
 */
@NonNullByDefault
public class Zodiac {
    private final ZodiacSign sign;
    private final Instant start;
    private final Instant end;

    public Zodiac(int index, Instant start, Instant end) {
        if (index < 0 || index >= ZodiacSign.values().length) {
            throw new IllegalArgumentException("Index value %d out of range".formatted(index));
        }
        if (!start.isBefore(end)) {
            throw new IllegalArgumentException("'start' must be before 'end'");
        }

        this.sign = ZodiacSign.values()[index];
        this.start = start;
        this.end = end;
    }

    /**
     * Returns the sign of the zodiac.
     */
    public ZodiacSign getSign() {
        return sign;
    }

    /**
     * Returns the start instant of the zodiac sign.
     */
    public Instant getStart() {
        return start;
    }

    /**
     * Returns the end instant of the zodiac sign.
     */
    public Instant getEnd() {
        return end;
    }
}
