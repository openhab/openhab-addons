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
package org.openhab.persistence.timescaledb.internal;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.DefaultLocation;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link DownsampleConfig}.
 *
 * @author René Ulbricht - Initial contribution
 */
@NonNullByDefault({ DefaultLocation.RETURN_TYPE, DefaultLocation.PARAMETER })
class DownsampleConfigTest {

    // --- retentionOnly() guard (Fix 6) ---

    @Test
    void retentionOnlyWithPositiveDaysSucceeds() {
        DownsampleConfig cfg = DownsampleConfig.retentionOnly(30);
        assertFalse(cfg.hasDownsampling());
        assertNull(cfg.function());
        assertNull(cfg.sqlInterval());
        assertEquals(30, cfg.retentionDays());
        assertEquals(0, cfg.retainRawDays());
    }

    @Test
    void retentionOnlyWithZeroDaysThrows() {
        assertThrows(IllegalArgumentException.class, () -> DownsampleConfig.retentionOnly(0),
                "retentionDays=0 must throw — a zero-day retention policy would delete everything immediately");
    }

    @Test
    void retentionOnlyWithNegativeDaysThrows() {
        assertThrows(IllegalArgumentException.class, () -> DownsampleConfig.retentionOnly(-1),
                "Negative retentionDays must throw — would generate dangerous SQL window");
    }

    @Test
    void retentionOnlyExceptionMessageMentionsValue() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> DownsampleConfig.retentionOnly(-5));
        String msg = ex.getMessage();
        assertNotNull(msg, "Exception must have a message");
        assertTrue(msg.contains("-5"), "Exception message should include the rejected value");
    }

    // --- toSqlInterval() allowlist ---

    @Test
    void toSqlIntervalValidIntervalReturnsLiteral() {
        assertEquals("1 hour", DownsampleConfig.toSqlInterval("1h"));
        assertEquals("1 day", DownsampleConfig.toSqlInterval("1d"));
        assertEquals("15 minutes", DownsampleConfig.toSqlInterval("15m"));
    }

    @Test
    void toSqlIntervalUnknownIntervalThrows() {
        assertThrows(IllegalArgumentException.class, () -> DownsampleConfig.toSqlInterval("99x"));
    }

    // --- hasDownsampling() ---

    @Test
    void hasDownsamplingTrueForFullConfig() {
        DownsampleConfig cfg = new DownsampleConfig(AggregationFunction.AVG, "1 hour", 5, 365);
        assertTrue(cfg.hasDownsampling());
    }

    @Test
    void hasDownsamplingFalseForRetentionOnly() {
        DownsampleConfig cfg = DownsampleConfig.retentionOnly(90);
        assertFalse(cfg.hasDownsampling());
    }
}
