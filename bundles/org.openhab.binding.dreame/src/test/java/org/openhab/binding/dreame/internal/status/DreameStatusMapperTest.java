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
package org.openhab.binding.dreame.internal.status;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Tests mappings from Dreame protocol codes to stable channel values.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
class DreameStatusMapperTest {

    @Test
    void mapsKnownMowerStates() {
        assertEquals("mowing", DreameStatusMapper.stateName(1));
        assertEquals("idle", DreameStatusMapper.stateName(2));
        assertEquals("paused", DreameStatusMapper.stateName(3));
        assertEquals("paused_due_to_error", DreameStatusMapper.stateName(4));
        assertEquals("returning", DreameStatusMapper.stateName(5));
        assertEquals("charging", DreameStatusMapper.stateName(6));
        assertEquals("mapping", DreameStatusMapper.stateName(11));
        assertEquals("charging_completed", DreameStatusMapper.stateName(13));
        assertEquals("upgrading", DreameStatusMapper.stateName(14));
        assertEquals("charging_paused_hot", DreameStatusMapper.stateName(15));
        assertEquals("charging_paused_cold", DreameStatusMapper.stateName(16));
        assertEquals("paused_at_maintenance_point", DreameStatusMapper.stateName(75));
        assertEquals("unknown", DreameStatusMapper.stateName(99));
    }

    @Test
    void mapsObservedTaskOperations() {
        assertEquals("charging", DreameStatusMapper.taskOperationName(6));
        assertEquals("mowing", DreameStatusMapper.taskOperationName(100));
        assertEquals("unknown", DreameStatusMapper.taskOperationName(42));
    }

    @Test
    void derivesTaskActivityFromSafeMowerStates() {
        assertEquals(Boolean.TRUE, DreameStatusMapper.taskActiveFromState(1));
        assertEquals(Boolean.TRUE, DreameStatusMapper.taskActiveFromState(5));
        for (int state : new int[] { 2, 3, 4, 6, 13, 75 }) {
            assertEquals(Boolean.FALSE, DreameStatusMapper.taskActiveFromState(state));
        }
        assertNull(DreameStatusMapper.taskActiveFromState(11));
        assertNull(DreameStatusMapper.taskActiveFromState(99));
    }

    @Test
    void mapsChargingStates() {
        assertEquals("not_charging", DreameStatusMapper.chargingName(0));
        assertEquals("charging", DreameStatusMapper.chargingName(1));
        assertEquals("not_charging", DreameStatusMapper.chargingName(2));
        assertEquals("charging_completed", DreameStatusMapper.chargingName(3));
        assertEquals("return_to_charge", DreameStatusMapper.chargingName(5));
        assertEquals("unknown", DreameStatusMapper.chargingName(99));
    }

    @Test
    void mapsDockingStates() {
        assertEquals("in_station", DreameStatusMapper.dockingName(0));
        assertEquals("out_of_station", DreameStatusMapper.dockingName(1));
        assertEquals("pause_docking", DreameStatusMapper.dockingName(2));
        assertEquals("finish_docking", DreameStatusMapper.dockingName(3));
        assertEquals("docking_failed", DreameStatusMapper.dockingName(4));
        assertEquals("docking_in_base", DreameStatusMapper.dockingName(5));
        assertEquals("unknown", DreameStatusMapper.dockingName(99));
    }

    @Test
    void identifiesDockArrivalAndUnavailableRssi() {
        assertTrue(DreameStatusMapper.isDockedState(6));
        assertTrue(DreameStatusMapper.isDockedState(13));
        assertFalse(DreameStatusMapper.isDockedState(5));
        assertFalse(DreameStatusMapper.isRssiAvailable(-128));
        assertFalse(DreameStatusMapper.isRssiAvailable(127));
        assertTrue(DreameStatusMapper.isRssiAvailable(-69));
    }
}
