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
package org.openhab.binding.volvocars.internal.api.dto;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;

/**
 * Tests the powertrain classification of {@link Vehicle}.
 *
 * @author Michael Weger - Initial contribution
 */
@NonNullByDefault
class VehicleTest {

    @Test
    void electric() {
        assertPowertrain("ELECTRIC", true, false);
    }

    @Test
    void petrolElectricHybrid() {
        assertPowertrain("PETROL/ELECTRIC", true, true);
    }

    @Test
    void dieselElectricHybrid() {
        assertPowertrain("DIESEL/ELECTRIC", true, true);
    }

    @Test
    void petrol() {
        assertPowertrain("PETROL", false, true);
    }

    @Test
    void diesel() {
        assertPowertrain("DIESEL", false, true);
    }

    @Test
    void none() {
        assertPowertrain("NONE", true, false);
    }

    @Test
    void unknown() {
        assertPowertrain(null, false, false);
    }

    private void assertPowertrain(@Nullable String fuelType, boolean battery, boolean combustionEngine) {
        Vehicle vehicle = new Vehicle();
        vehicle.fuelType = fuelType;
        assertEquals(battery, vehicle.hasBattery(), "hasBattery for " + fuelType);
        assertEquals(combustionEngine, vehicle.hasCombustionEngine(), "hasCombustionEngine for " + fuelType);
    }
}
