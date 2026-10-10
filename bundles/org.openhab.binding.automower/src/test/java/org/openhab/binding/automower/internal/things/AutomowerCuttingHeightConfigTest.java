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
package org.openhab.binding.automower.internal.things;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class AutomowerCuttingHeightConfigTest {

    @Test
    void calculateCuttingHeightCmSkipsInvalidInput() {
        assertNull(AutomowerHandler.calculateCuttingHeightCm(null, null, (byte) 5));
        assertNull(AutomowerHandler.calculateCuttingHeightCm(8.0, 3.0, (byte) 5));
        assertNull(AutomowerHandler.calculateCuttingHeightCm(3.0, 8.0, null));
        assertNull(AutomowerHandler.calculateCuttingHeightCm(3.0, 8.0, (byte) 0));
        assertNull(AutomowerHandler.calculateCuttingHeightCm(3.0, 8.0, (byte) 10));
    }

    @Test
    void calculateCuttingHeightCmUsesConfiguredRange() {
        assertEquals(4.5, AutomowerHandler.calculateCuttingHeightCm(3.0, 8.0, (byte) 3), 0.01);
        assertEquals(5.5, AutomowerHandler.calculateCuttingHeightCm(3.0, 8.0, (byte) 5), 0.01);
        assertEquals(7.0, AutomowerHandler.calculateCuttingHeightCm(3.0, 8.0, (byte) 7), 0.01);
    }

    @Test
    void calculateCuttingHeightCmUsesRoundHalfUpBehavior() {
        assertEquals(6.0, AutomowerHandler.calculateCuttingHeightCm(3.0, 8.0, (byte) 6), 0.01);
        assertEquals(5.0, AutomowerHandler.calculateCuttingHeightCm(3.0, 8.0, (byte) 4), 0.01);
    }

    @Test
    void calculateCuttingHeightCmIgnoresUnsetConfig() {
        assertNull(AutomowerHandler.calculateCuttingHeightCm(null, 8.0, (byte) 5));
        assertNull(AutomowerHandler.calculateCuttingHeightCm(3.0, null, (byte) 5));
    }

    @Test
    void calculateCuttingHeightCmSupportsWorkAreaScaling() {
        assertEquals(5.5, AutomowerHandler.calculateCuttingHeightCm(3.0, 8.0, (byte) 50, 0.0, 100.0), 0.01);
        assertEquals(4.0, AutomowerHandler.calculateCuttingHeightCm(3.0, 8.0, (byte) 20, 0.0, 100.0), 0.01);
    }
}
