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
import org.openhab.core.library.items.ContactItem;
import org.openhab.core.library.items.SwitchItem;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;

/**
 * Unit tests for the downsampling semantics of Switch (ON/OFF) and Contact (OPEN/CLOSED).
 *
 * <p>
 * Both types are stored numerically: ON/OPEN = 1.0, OFF/CLOSED = 0.0.
 * The DB-side aggregation (MAX, MIN, AVG, SUM) produces a numeric result that
 * {@link TimescaleDBMapper#toState} reconstructs via threshold: value >= 0.5 → ON/OPEN.
 *
 * <p>
 * Reference sequence for all tests: [OFF, ON, OFF] = [0.0, 1.0, 0.0]
 * <ul>
 * <li>MAX = 1.0 → ON / OPEN (was the state ever active?)</li>
 * <li>MIN = 0.0 → OFF / CLOSED (was the state ever inactive?)</li>
 * <li>AVG = 0.333 → OFF / CLOSED (minority of the time active)</li>
 * <li>SUM = 1.0 → ON / OPEN (at least one active event in the bucket)</li>
 * </ul>
 *
 * @author René Ulbricht - Initial contribution
 */
@NonNullByDefault({ DefaultLocation.RETURN_TYPE, DefaultLocation.PARAMETER })
class TimescaleDBDownsampleSemanticsTest {

    // ------------------------------------------------------------------
    // Encoding: toRow
    // ------------------------------------------------------------------

    @Test
    void encodingSwitchOnIs1() {
        var row = TimescaleDBMapper.toRow(OnOffType.ON);
        assertNotNull(row);
        assertEquals(1.0, row.value());
        assertNull(row.string());
        assertNull(row.unit());
    }

    @Test
    void encodingSwitchOffIs0() {
        var row = TimescaleDBMapper.toRow(OnOffType.OFF);
        assertNotNull(row);
        assertEquals(0.0, row.value());
        assertNull(row.string());
        assertNull(row.unit());
    }

    @Test
    void encodingContactOpenIs1() {
        var row = TimescaleDBMapper.toRow(OpenClosedType.OPEN);
        assertNotNull(row);
        assertEquals(1.0, row.value());
        assertNull(row.string());
        assertNull(row.unit());
    }

    @Test
    void encodingContactClosedIs0() {
        var row = TimescaleDBMapper.toRow(OpenClosedType.CLOSED);
        assertNotNull(row);
        assertEquals(0.0, row.value());
        assertNull(row.string());
        assertNull(row.unit());
    }

    // ------------------------------------------------------------------
    // Switch: MAX — sequence [OFF, ON, OFF] → MAX(0,1,0) = 1.0 → ON
    // ------------------------------------------------------------------

    @Test
    void switchMaxOffonoffReturnson() {
        double aggregated = max(0.0, 1.0, 0.0);
        assertEquals(OnOffType.ON, TimescaleDBMapper.toState(new SwitchItem("s"), aggregated, null, null));
    }

    @Test
    void switchMaxAlloffReturnsoff() {
        double aggregated = max(0.0, 0.0, 0.0);
        assertEquals(OnOffType.OFF, TimescaleDBMapper.toState(new SwitchItem("s"), aggregated, null, null));
    }

    @Test
    void switchMaxAllonReturnson() {
        double aggregated = max(1.0, 1.0, 1.0);
        assertEquals(OnOffType.ON, TimescaleDBMapper.toState(new SwitchItem("s"), aggregated, null, null));
    }

    // ------------------------------------------------------------------
    // Switch: MIN — sequence [OFF, ON, OFF] → MIN(0,1,0) = 0.0 → OFF
    // ------------------------------------------------------------------

    @Test
    void switchMinOffonoffReturnsoff() {
        double aggregated = min(0.0, 1.0, 0.0);
        assertEquals(OnOffType.OFF, TimescaleDBMapper.toState(new SwitchItem("s"), aggregated, null, null));
    }

    @Test
    void switchMinAllonReturnson() {
        double aggregated = min(1.0, 1.0, 1.0);
        assertEquals(OnOffType.ON, TimescaleDBMapper.toState(new SwitchItem("s"), aggregated, null, null));
    }

    @Test
    void switchMinAlloffReturnsoff() {
        double aggregated = min(0.0, 0.0, 0.0);
        assertEquals(OnOffType.OFF, TimescaleDBMapper.toState(new SwitchItem("s"), aggregated, null, null));
    }

    // ------------------------------------------------------------------
    // Switch: AVG — sequence [OFF, ON, OFF] → AVG(0,1,0) = 0.333 → OFF
    // ------------------------------------------------------------------

    @Test
    void switchAvgOffonoffReturnsoff() {
        double aggregated = avg(0.0, 1.0, 0.0); // = 0.333
        assertEquals(OnOffType.OFF, TimescaleDBMapper.toState(new SwitchItem("s"), aggregated, null, null));
    }

    @Test
    void switchAvgMajorityonReturnson() {
        // ON, ON, OFF → AVG = 0.666 → ON
        double aggregated = avg(1.0, 1.0, 0.0);
        assertEquals(OnOffType.ON, TimescaleDBMapper.toState(new SwitchItem("s"), aggregated, null, null));
    }

    @Test
    void switchAvgExactlyhalfReturnson() {
        // Boundary: 0.5 exactly → ON (threshold is >= 0.5)
        assertEquals(OnOffType.ON, TimescaleDBMapper.toState(new SwitchItem("s"), 0.5, null, null));
    }

    @Test
    void switchAvgJustbelowhalfReturnsoff() {
        assertEquals(OnOffType.OFF, TimescaleDBMapper.toState(new SwitchItem("s"), 0.499, null, null));
    }

    // ------------------------------------------------------------------
    // Switch: SUM — sequence [OFF, ON, OFF] → SUM(0,1,0) = 1.0 → ON
    // ------------------------------------------------------------------

    @Test
    void switchSumOffonoffReturnson() {
        double aggregated = sum(0.0, 1.0, 0.0); // = 1.0
        assertEquals(OnOffType.ON, TimescaleDBMapper.toState(new SwitchItem("s"), aggregated, null, null));
    }

    @Test
    void switchSumAlloffReturnsoff() {
        double aggregated = sum(0.0, 0.0, 0.0); // = 0.0
        assertEquals(OnOffType.OFF, TimescaleDBMapper.toState(new SwitchItem("s"), aggregated, null, null));
    }

    @Test
    void switchSumMultipleonReturnson() {
        // Multiple ON events sum to > 1 — still reads as ON
        double aggregated = sum(1.0, 1.0, 0.0); // = 2.0
        assertEquals(OnOffType.ON, TimescaleDBMapper.toState(new SwitchItem("s"), aggregated, null, null));
    }

    // ------------------------------------------------------------------
    // Contact: MAX — sequence [CLOSED, OPEN, CLOSED] → MAX(0,1,0) = 1.0 → OPEN
    // ------------------------------------------------------------------

    @Test
    void contactMaxClosedopenclosedReturnsopen() {
        double aggregated = max(0.0, 1.0, 0.0);
        assertEquals(OpenClosedType.OPEN, TimescaleDBMapper.toState(new ContactItem("c"), aggregated, null, null));
    }

    @Test
    void contactMaxAllclosedReturnsclosed() {
        double aggregated = max(0.0, 0.0, 0.0);
        assertEquals(OpenClosedType.CLOSED, TimescaleDBMapper.toState(new ContactItem("c"), aggregated, null, null));
    }

    @Test
    void contactMaxAllopenReturnsopen() {
        double aggregated = max(1.0, 1.0, 1.0);
        assertEquals(OpenClosedType.OPEN, TimescaleDBMapper.toState(new ContactItem("c"), aggregated, null, null));
    }

    // ------------------------------------------------------------------
    // Contact: MIN — sequence [CLOSED, OPEN, CLOSED] → MIN(0,1,0) = 0.0 → CLOSED
    // ------------------------------------------------------------------

    @Test
    void contactMinClosedopenclosedReturnsclosed() {
        double aggregated = min(0.0, 1.0, 0.0);
        assertEquals(OpenClosedType.CLOSED, TimescaleDBMapper.toState(new ContactItem("c"), aggregated, null, null));
    }

    @Test
    void contactMinAllopenReturnsopen() {
        double aggregated = min(1.0, 1.0, 1.0);
        assertEquals(OpenClosedType.OPEN, TimescaleDBMapper.toState(new ContactItem("c"), aggregated, null, null));
    }

    // ------------------------------------------------------------------
    // Contact: AVG — sequence [CLOSED, OPEN, CLOSED] → AVG(0,1,0) = 0.333 → CLOSED
    // ------------------------------------------------------------------

    @Test
    void contactAvgClosedopenclosedReturnsclosed() {
        double aggregated = avg(0.0, 1.0, 0.0); // = 0.333
        assertEquals(OpenClosedType.CLOSED, TimescaleDBMapper.toState(new ContactItem("c"), aggregated, null, null));
    }

    @Test
    void contactAvgMajorityopenReturnsopen() {
        double aggregated = avg(1.0, 1.0, 0.0); // = 0.666
        assertEquals(OpenClosedType.OPEN, TimescaleDBMapper.toState(new ContactItem("c"), aggregated, null, null));
    }

    @Test
    void contactAvgExactlyhalfReturnsopen() {
        assertEquals(OpenClosedType.OPEN, TimescaleDBMapper.toState(new ContactItem("c"), 0.5, null, null));
    }

    @Test
    void contactAvgJustbelowhalfReturnsclosed() {
        assertEquals(OpenClosedType.CLOSED, TimescaleDBMapper.toState(new ContactItem("c"), 0.499, null, null));
    }

    // ------------------------------------------------------------------
    // Contact: SUM — sequence [CLOSED, OPEN, CLOSED] → SUM(0,1,0) = 1.0 → OPEN
    // ------------------------------------------------------------------

    @Test
    void contactSumClosedopenclosedReturnsopen() {
        double aggregated = sum(0.0, 1.0, 0.0); // = 1.0
        assertEquals(OpenClosedType.OPEN, TimescaleDBMapper.toState(new ContactItem("c"), aggregated, null, null));
    }

    @Test
    void contactSumAllclosedReturnsclosed() {
        double aggregated = sum(0.0, 0.0, 0.0);
        assertEquals(OpenClosedType.CLOSED, TimescaleDBMapper.toState(new ContactItem("c"), aggregated, null, null));
    }

    @Test
    void contactSumMultipleopenReturnsopen() {
        double aggregated = sum(1.0, 1.0, 0.0); // = 2.0
        assertEquals(OpenClosedType.OPEN, TimescaleDBMapper.toState(new ContactItem("c"), aggregated, null, null));
    }

    // ------------------------------------------------------------------
    // Helpers — simulate DB-side aggregation on a value series
    // ------------------------------------------------------------------

    private static double max(double... values) {
        double result = Double.NEGATIVE_INFINITY;
        for (double v : values) {
            result = Math.max(result, v);
        }
        return result;
    }

    private static double min(double... values) {
        double result = Double.POSITIVE_INFINITY;
        for (double v : values) {
            result = Math.min(result, v);
        }
        return result;
    }

    private static double avg(double... values) {
        double sum = 0;
        for (double v : values) {
            sum += v;
        }
        return sum / values.length;
    }

    private static double sum(double... values) {
        double result = 0;
        for (double v : values) {
            result += v;
        }
        return result;
    }
}
