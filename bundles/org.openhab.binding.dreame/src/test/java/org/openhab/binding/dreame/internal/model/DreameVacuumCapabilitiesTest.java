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
package org.openhab.binding.dreame.internal.model;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Covers model and firmware selection for the two Dreame vacuum state schemas.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
class DreameVacuumCapabilitiesTest {
    @Test
    void selectsOldAndNewStateSchemasByModel() {
        assertFalse(DreameVacuumCapabilities.usesNewStateSchema(device("dreame.vacuum.r2228o", "4.3.9_1609")));
        assertTrue(DreameVacuumCapabilities.usesNewStateSchema(device("dreame.vacuum.r9445d", "4.3.9_1609")));
        assertTrue(DreameVacuumCapabilities.usesNewStateSchema(device("dreame.vacuum.unknown", "")));
    }

    @Test
    void appliesFirmwareThresholdForTransitionalModels() {
        assertFalse(DreameVacuumCapabilities.usesNewStateSchema(device("dreame.vacuum.r2316", "4.3.3_1018")));
        assertTrue(DreameVacuumCapabilities.usesNewStateSchema(device("dreame.vacuum.r2316", "4.3.3_1019")));
        assertFalse(DreameVacuumCapabilities.usesNewStateSchema(device("dreame.vacuum.r2316", "unknown")));
    }

    private static DreameDevice device(String model, String version) {
        return new DreameDevice("test", "test", model, version, "", "", "");
    }
}
