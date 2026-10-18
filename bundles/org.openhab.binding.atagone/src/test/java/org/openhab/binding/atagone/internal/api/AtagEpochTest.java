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
package org.openhab.binding.atagone.internal.api;

import static org.junit.jupiter.api.Assertions.*;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link AtagEpoch} epoch conversion.
 *
 * @author Florian Lettner - Initial contribution
 */
@NonNullByDefault
class AtagEpochTest {

    @Test
    void epochOriginMapsTo2000() {
        ZonedDateTime result = AtagEpoch.toZonedDateTime(0L);
        assertEquals(2000, result.getYear());
        assertEquals(1, result.getMonthValue());
        assertEquals(1, result.getDayOfMonth());
        assertEquals(0, result.getHour());
        assertEquals(0, result.getMinute());
        assertEquals(0, result.getSecond());
    }

    @Test
    void roundTripPreservesInstant() {
        ZonedDateTime original = ZonedDateTime.of(2026, 3, 15, 10, 30, 0, 0, ZoneId.of("UTC"));
        long atagEpoch = AtagEpoch.fromZonedDateTime(original);
        ZonedDateTime roundTripped = AtagEpoch.toZonedDateTime(atagEpoch);
        assertEquals(original.toInstant(), roundTripped.toInstant());
    }

    @Test
    void nonUtcInputNormalisedCorrectly() {
        ZonedDateTime cetNoon = ZonedDateTime.of(2026, 1, 1, 13, 0, 0, 0, ZoneId.of("Europe/Amsterdam"));
        ZonedDateTime utcNoon = ZonedDateTime.of(2026, 1, 1, 12, 0, 0, 0, ZoneId.of("UTC"));
        assertEquals(AtagEpoch.fromZonedDateTime(utcNoon), AtagEpoch.fromZonedDateTime(cetNoon));
    }

    @Test
    void sevenDayVacationDuration() {
        ZonedDateTime start = ZonedDateTime.of(2026, 8, 1, 0, 0, 0, 0, ZoneId.of("UTC"));
        ZonedDateTime end = start.plusDays(7);
        long durationSeconds = AtagEpoch.fromZonedDateTime(end) - AtagEpoch.fromZonedDateTime(start);
        assertEquals(7L * 24 * 3600, durationSeconds);
    }

    @Test
    void offsetConstantIsCorrect() {
        // 1970-01-01 to 2000-01-01 = 10957 days (23 regular + 7 leap years between 1970-1999)
        assertEquals(10957L * 86400L, AtagEpoch.OFFSET);
    }
}
