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
package org.openhab.binding.bydbatterybox.internal.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link CellStatistics}.
 *
 * @author Michael Weger - Initial contribution
 */
@NonNullByDefault
class CellStatisticsTest {

    private static final List<Integer> VALUES = List.of(3200, 3100, 3300, 3100, 3300, 3250);

    @Test
    void wholeRange() {
        CellStatistics stats = CellStatistics.of(VALUES, 0, VALUES.size());
        assertNotNull(stats);
        assertEquals(3100, stats.min());
        assertEquals(2, stats.minPosition());
        assertEquals(3300, stats.max());
        assertEquals(3, stats.maxPosition());
        assertEquals(200, stats.spread());
        assertEquals(19_250, stats.sum());
        assertEquals(0, new BigDecimal("3208.3").compareTo(stats.average()));
    }

    @Test
    void positionsReferToTheWholeList() {
        CellStatistics stats = CellStatistics.of(VALUES, 3, 6);
        assertNotNull(stats);
        assertEquals(4, stats.minPosition());
        assertEquals(5, stats.maxPosition());
    }

    @Test
    void rangeBeyondTheListIsTruncatedOrEmpty() {
        CellStatistics truncated = CellStatistics.of(VALUES, 4, 10);
        assertNotNull(truncated);
        assertEquals(6_550, truncated.sum());
        assertNull(CellStatistics.of(VALUES, 6, 10));
        assertNull(CellStatistics.of(List.of(), 0, 0));
    }
}
