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
package org.openhab.binding.melcloud.internal.home.api.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link MelCloudHomeAtwUnit} (see ADR-003), in particular the zone-2 gating logic and the
 * {@code holidayMode}/{@code frostProtection} top-level toggle fields.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
class MelCloudHomeAtwUnitTest {

    private static MelCloudHomeAtwUnit unitWithSettings(String... nameValuePairs) {
        MelCloudHomeAtwUnit unit = new MelCloudHomeAtwUnit();
        List<MelCloudHomeSetting> settings = new ArrayList<>(unit.settings);
        for (int i = 0; i < nameValuePairs.length; i += 2) {
            MelCloudHomeSetting setting = new MelCloudHomeSetting();
            setting.name = nameValuePairs[i];
            setting.value = nameValuePairs[i + 1];
            settings.add(setting);
        }
        unit.settings = settings;
        return unit;
    }

    @Test
    void whenHasZone2SettingIsFalse_thenZone2AccessorsReturnEmpty() {
        // Arrange
        MelCloudHomeAtwUnit unit = unitWithSettings("HasZone2", "False", "OperationModeZone2", "HeatCurve",
                "SetTemperatureZone2", "21.0", "RoomTemperatureZone2", "20.5");

        // Act & Assert
        assertFalse(unit.hasZone2());
        assertEquals(Optional.empty(), unit.getOperationModeZone2());
        assertEquals(Optional.empty(), unit.getSetTemperatureZone2());
        assertEquals(Optional.empty(), unit.getRoomTemperatureZone2());
    }

    @Test
    void whenHasZone2SettingIsTrue_thenZone2AccessorsReturnTheirValues() {
        // Arrange
        MelCloudHomeAtwUnit unit = unitWithSettings("HasZone2", "True", "OperationModeZone2", "HeatCurve",
                "SetTemperatureZone2", "21.0", "RoomTemperatureZone2", "20.5");

        // Act & Assert
        assertTrue(unit.hasZone2());
        assertEquals(Optional.of("HeatCurve"), unit.getOperationModeZone2());
        assertEquals(Optional.of(21.0), unit.getSetTemperatureZone2());
        assertEquals(Optional.of(20.5), unit.getRoomTemperatureZone2());
    }

    @Test
    void whenHolidayModeIsNull_thenIsHolidayModeEnabledReturnsFalse() {
        // Arrange
        MelCloudHomeAtwUnit unit = new MelCloudHomeAtwUnit();

        // Act
        boolean holidayModeEnabled = unit.isHolidayModeEnabled();

        // Assert
        assertFalse(holidayModeEnabled);
    }

    @Test
    void whenHolidayModeIsSetAndEnabled_thenIsHolidayModeEnabledReturnsTrue() {
        // Arrange
        MelCloudHomeAtwUnit unit = new MelCloudHomeAtwUnit();
        MelCloudHomeToggleState toggleState = new MelCloudHomeToggleState();
        toggleState.enabled = true;
        unit.holidayMode = toggleState;

        // Act
        boolean holidayModeEnabled = unit.isHolidayModeEnabled();

        // Assert
        assertTrue(holidayModeEnabled);
    }

    @Test
    void whenFrostProtectionIsNull_thenIsFrostProtectionEnabledReturnsFalse() {
        // Arrange
        MelCloudHomeAtwUnit unit = new MelCloudHomeAtwUnit();

        // Act
        boolean frostProtectionEnabled = unit.isFrostProtectionEnabled();

        // Assert
        assertFalse(frostProtectionEnabled);
    }

    @Test
    void whenOutdoorTemperatureSettingIsPresent_thenGetOutdoorTemperatureReturnsIt() {
        // Arrange
        MelCloudHomeAtwUnit unit = unitWithSettings("OutdoorTemperature", "5.5");

        // Act
        Optional<Double> outdoorTemperature = unit.getOutdoorTemperature();

        // Assert
        assertEquals(Optional.of(5.5), outdoorTemperature);
    }

    @Test
    void whenOperationModeSettingIsMissing_thenGetOperationStatusDefaultsToStop() {
        // Arrange
        MelCloudHomeAtwUnit unit = new MelCloudHomeAtwUnit();

        // Act
        String operationStatus = unit.getOperationStatus();

        // Assert
        assertEquals("Stop", operationStatus);
    }
}
