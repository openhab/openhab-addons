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
package org.openhab.binding.e3dc.internal;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Checks the conversion of the battery capacities from Ah to kWh.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
class E3DCBatteryEnergyTest {

    @Test
    void multipliesCapacityWithVoltage() {
        // values of a LG battery: 252 Ah, 228.1 Ah and 112.0 Ah at 51.8 V
        assertEquals(13.0536, E3DCThingHandler.energyKwh(252.0, 51.8), 1e-6);
        assertEquals(11.81558, E3DCThingHandler.energyKwh(228.1, 51.8), 1e-6);
        assertEquals(5.8016, E3DCThingHandler.energyKwh(112.0, 51.8), 1e-6);
    }

    @Test
    void reportsNothingWithoutCapacity() {
        assertNull(E3DCThingHandler.energyKwh(null, 51.8));
    }
}
