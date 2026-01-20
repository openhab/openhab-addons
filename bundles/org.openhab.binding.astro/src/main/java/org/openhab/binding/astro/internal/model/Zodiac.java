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
public record Zodiac(ZodiacSign sign, Instant start, Instant end) {

    public Zodiac {
        if (!start.isBefore(end)) {
            throw new IllegalArgumentException("'start' must be before 'end'");
        }
    }

    public Zodiac(int index, Instant start, Instant end) {
        this(ZodiacSign.values()[index], start, end);
    }
}
