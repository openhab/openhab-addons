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
import org.eclipse.jdt.annotation.Nullable;

/**
 * Holds the sign of the zodiac.
 *
 * @author Gerhard Riegler - Initial contribution
 */
@NonNullByDefault
public class Zodiac {
    public static final Zodiac NULL = new Zodiac();

    private final @Nullable ZodiacSign sign;
    private final @Nullable Instant start;
    private final @Nullable Instant end;

    private Zodiac() {
        this.sign = null;
        this.start = null;
        this.end = null;
    }

    public Zodiac(int index, @Nullable Instant start, @Nullable Instant end) {
        if (index < 0 || index >= ZodiacSign.values().length) {
            throw new IllegalArgumentException("Index value %d out of range".formatted(index));
        }
        this.sign = ZodiacSign.values()[index];
        this.start = start;
        this.end = end;
    }

    /**
     * Returns the sign of the zodiac.
     */
    @Nullable
    public ZodiacSign getSign() {
        return sign;
    }

    /**
     * Returns the start instant of the zodiac sign.
     */
    @Nullable
    public Instant getStart() {
        return start;
    }

    /**
     * Returns the end instant of the zodiac sign.
     */
    @Nullable
    public Instant getEnd() {
        return end;
    }
}
