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
package org.openhab.binding.e3dc.internal.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Tests the energy a manual charge still has to charge.
 *
 * <p>
 * The device reports the energy the charge was started with and the energy charged so far, but not what is left,
 * so the binding calculates it. The values below follow the answers of a real device.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
class E3DCManualChargeRemainingTest {

    @Test
    void theRemainingEnergyIsTheTargetLessTheChargedEnergy() {
        assertEquals(218.8470775, E3DCPowerPlantHandler.remainingWh(true, 500, 281.1529225), 1e-6);
    }

    @Test
    void nothingIsLeftWhenNoChargeIsRunning() {
        assertEquals(0, E3DCPowerPlantHandler.remainingWh(false, 2000, 0));
        assertEquals(0, E3DCPowerPlantHandler.remainingWh(false, 500, 120));
    }

    @Test
    void theRemainingEnergyNeverExceedsTheTargetBecauseTheCounterStartsBelowZero() {
        assertEquals(500, E3DCPowerPlantHandler.remainingWh(true, 500, -0.116175));
    }

    @Test
    void theRemainingEnergyIsZeroOnceTheTargetIsReached() {
        assertEquals(0, E3DCPowerPlantHandler.remainingWh(true, 500, 500.4));
    }
}
