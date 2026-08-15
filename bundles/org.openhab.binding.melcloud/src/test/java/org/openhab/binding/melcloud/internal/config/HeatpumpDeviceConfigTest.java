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
package org.openhab.binding.melcloud.internal.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link HeatpumpDeviceConfig#toString()}: {@code deviceID}/{@code buildingID} must never appear
 * unmasked, since this config is logged directly on handler {@code initialize()} (see ADR-010).
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
class HeatpumpDeviceConfigTest {

    @Test
    void whenDeviceAndBuildingIdAreSetThenToStringMasksBoth() {
        // Arrange
        HeatpumpDeviceConfig config = new HeatpumpDeviceConfig();
        config.deviceID = 1234567;
        config.buildingID = 654321;
        config.pollingInterval = 360;

        // Act
        String result = config.toString();

        // Assert
        assertFalse(result.contains("1234567"));
        assertFalse(result.contains("654321"));
        assertEquals("[deviceID=...4567, buildingID=...4321, pollingInterval=360]", result);
    }

    @Test
    void whenBuildingIdIsNullThenToStringPrintsNullWithoutMasking() {
        // Arrange
        HeatpumpDeviceConfig config = new HeatpumpDeviceConfig();
        config.deviceID = 1234567;
        config.buildingID = null;

        // Act
        String result = config.toString();

        // Assert
        assertEquals("[deviceID=...4567, buildingID=null, pollingInterval=360]", result);
    }
}
