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
package org.openhab.binding.solaredge.internal.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.UnDefType;

/**
 * Tests calculations shared by live and aggregate Monitoring API V2 data.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
public class SolarEdgeGenericHandlerTest {
    @Test
    public void calculatesConsumptionFromEnergyBalance() {
        assertEquals(594.3, SolarEdgeGenericHandler.calculateConsumption(0, 1.75, 3.95, 0, 596.5), 0.0001);
    }

    @Test
    public void preventsNegativeConsumptionFromMeasurementRounding() {
        assertEquals(0, SolarEdgeGenericHandler.calculateConsumption(0, 1, 2, 0, 0));
    }

    @Test
    public void calculatesSelfConsumptionAndCoverage() {
        assertEquals(600, SolarEdgeGenericHandler.calculateSelfConsumption(1500, 100, 800), 0.0001);
        assertEquals(75, SolarEdgeGenericHandler.calculateCoverage(600, 800), 0.0001);
        assertEquals(0, SolarEdgeGenericHandler.calculateCoverage(0, 0), 0.0001);
    }

    @Test
    public void derivesLiveStatuses() {
        assertEquals("Active", SolarEdgeGenericHandler.activeStatus(1));
        assertEquals("Idle", SolarEdgeGenericHandler.activeStatus(0));
        assertEquals("Active", SolarEdgeGenericHandler.activeStatus(100, 0));
        assertEquals("Active", SolarEdgeGenericHandler.activeStatus(0, 100));
        assertEquals("Idle", SolarEdgeGenericHandler.activeStatus(0, 0));
    }

    @Test
    public void derivesCriticalBatteryStateOnlyFromKnownLevel() {
        assertEquals(new StringType("true"), SolarEdgeGenericHandler.batteryCriticalState(9.9, 10));
        assertEquals(new StringType("false"), SolarEdgeGenericHandler.batteryCriticalState(10.0, 10));
        assertEquals(UnDefType.UNDEF, SolarEdgeGenericHandler.batteryCriticalState(null, 10));
    }
}
