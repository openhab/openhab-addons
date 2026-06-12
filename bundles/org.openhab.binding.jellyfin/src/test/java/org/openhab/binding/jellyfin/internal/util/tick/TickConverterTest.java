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
package org.openhab.binding.jellyfin.internal.util.tick;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link TickConverter}.
 *
 * @author Patrik Gfeller - Initial contribution
 */
@NonNullByDefault
class TickConverterTest {

    @Test
    void testSecondsToTicks() {
        assertEquals(10_000_000L, TickConverter.secondsToTicks(1));
        assertEquals(0L, TickConverter.secondsToTicks(0));
        assertEquals(600_000_000L, TickConverter.secondsToTicks(60));
    }

    @Test
    void testTicksToSeconds() {
        assertEquals(1L, TickConverter.ticksToSeconds(10_000_000L));
        assertEquals(0L, TickConverter.ticksToSeconds(0L));
        assertEquals(60L, TickConverter.ticksToSeconds(600_000_000L));
    }

    @Test
    void testTicksToSecondsRoundsCorrectly() {
        // 10_500_000 ticks == 1.05 s, rounds to 1
        assertEquals(1L, TickConverter.ticksToSeconds(10_500_000L));
        // 15_000_000 ticks == 1.5 s, rounds to 2
        assertEquals(2L, TickConverter.ticksToSeconds(15_000_000L));
    }

    @Test
    void testPercentToTicks() {
        long runtime = 100_000_000L; // 10 s
        assertEquals(50_000_000L, TickConverter.percentToTicks(runtime, 50));
        assertEquals(0L, TickConverter.percentToTicks(runtime, 0));
        assertEquals(100_000_000L, TickConverter.percentToTicks(runtime, 100));
    }

    @Test
    void testTicksToPercentClamped() {
        long runtime = 100_000_000L;
        assertEquals(50, TickConverter.ticksToPercent(50_000_000L, runtime));
        assertEquals(0, TickConverter.ticksToPercent(0L, runtime));
        assertEquals(100, TickConverter.ticksToPercent(100_000_000L, runtime));
    }

    @Test
    void testTicksToPercentClampsBelowZero() {
        long runtime = 100_000_000L;
        assertEquals(0, TickConverter.ticksToPercent(-1_000_000L, runtime));
    }

    @Test
    void testTicksToPercentClampsAbove100() {
        long runtime = 100_000_000L;
        assertEquals(100, TickConverter.ticksToPercent(200_000_000L, runtime));
    }

    @Test
    void testTicksToPercentWithZeroRuntime() {
        assertEquals(0, TickConverter.ticksToPercent(50_000_000L, 0L));
    }

    @Test
    void testTicksPerSecondConstant() {
        assertEquals(10_000_000L, TickConverter.TICKS_PER_SECOND);
    }
}
