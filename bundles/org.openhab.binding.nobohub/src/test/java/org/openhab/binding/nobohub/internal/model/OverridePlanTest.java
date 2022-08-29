/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

package org.openhab.binding.nobohub.internal.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDateTime;
import java.time.Month;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for Override model object.
 *
 * @author Jørgen Austvik - Initial contribution
 */
@NonNullByDefault
public class OverridePlanTest {

    @Test
    public void testParseH04DefaultOverride() throws NoboDataException {
        OverridePlan parsed = OverridePlan.fromH04("H04 4 0 0 -1 -1 0 -1");
        assertEquals(4, parsed.getId());
        assertEquals(OverrideMode.NORMAL, parsed.getMode());
        assertEquals(OverrideType.NOW, parsed.getType());
        assertEquals(OverrideTarget.HUB, parsed.getTarget());
        assertEquals(-1, parsed.getTargetId());
        assertNull(parsed.startTime());
        assertNull(parsed.endTime());
    }

    @Test
    public void testParseB03WithStartDate() throws NoboDataException {
        OverridePlan parsed = OverridePlan.fromH04("B03 9 3 1 202001221930 -1 0 -1");
        assertEquals(9, parsed.getId());
        assertEquals(OverrideMode.AWAY, parsed.getMode());
        assertEquals(OverrideType.TIMER, parsed.getType());
        assertEquals(OverrideTarget.HUB, parsed.getTarget());
        assertEquals(-1, parsed.getTargetId());
        LocalDateTime date = LocalDateTime.of(2020, Month.JANUARY, 22, 19, 30);
        assertEquals(date, parsed.startTime());
        assertNull(parsed.endTime());
    }

    @Test
    public void testParseS03NoDate() throws NoboDataException {
        OverridePlan parsed = OverridePlan.fromH04("S03 13 0 0 -1 -1 0 -1");
        assertEquals(13, parsed.getId());
        assertEquals(OverrideMode.NORMAL, parsed.getMode());
        assertEquals(OverrideType.NOW, parsed.getType());
        assertEquals(OverrideTarget.HUB, parsed.getTarget());
        assertEquals(-1, parsed.getTargetId());
        assertNull(parsed.startTime());
        assertNull(parsed.endTime());
    }

    @Test
    public void testAddA03WithStartDate() throws NoboDataException {
        OverridePlan parsed = OverridePlan.fromH04("B03 9 3 1 202001221930 -1 0 -1");
        assertEquals("A03 9 3 1 202001221930 -1 0 -1", parsed.generateCommandString("A03"));
    }

    @Test
    public void testFromMode() {
        LocalDateTime date = LocalDateTime.of(2020, Month.FEBRUARY, 21, 21, 42);
        OverridePlan overridePlan = OverridePlan.fromMode(OverrideMode.AWAY, date);
        assertEquals("A03 1 3 0 -1 -1 0 -1", overridePlan.generateCommandString("A03"));
    }

    @Test
    public void testModeNames() throws NoboDataException {
        assertEquals(OverrideMode.AWAY, OverrideMode.getByName("Away"));
        assertEquals(OverrideMode.ECO, OverrideMode.getByName("ECO"));
        assertEquals(OverrideMode.NORMAL, OverrideMode.getByName("Normal"));
        assertEquals(OverrideMode.COMFORT, OverrideMode.getByName("COMFORT"));
    }
}
