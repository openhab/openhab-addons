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

import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Unit tests for {@link MelCloudHomeSettingsParser}.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
class MelCloudHomeSettingsParserTest {

    private static List<MelCloudHomeSetting> settingsOf(String name, String value) {
        MelCloudHomeSetting setting = new MelCloudHomeSetting();
        setting.name = name;
        setting.value = value;
        return List.of(setting);
    }

    @Test
    void whenSettingIsPresentThenFindStringReturnsItsValue() {
        // Arrange
        List<MelCloudHomeSetting> settings = settingsOf("Power", "True");

        // Act
        Optional<String> result = MelCloudHomeSettingsParser.findString(settings, "Power");

        // Assert
        assertEquals(Optional.of("True"), result);
    }

    @Test
    void whenSettingIsAbsentThenFindStringReturnsEmpty() {
        // Arrange
        List<MelCloudHomeSetting> settings = settingsOf("Power", "True");

        // Act
        Optional<String> result = MelCloudHomeSettingsParser.findString(settings, "SetTemperature");

        // Assert
        assertEquals(Optional.empty(), result);
    }

    @Test
    void whenSettingValueIsEmptyThenFindStringReturnsEmpty() {
        // Arrange
        List<MelCloudHomeSetting> settings = settingsOf("Power", "");

        // Act
        Optional<String> result = MelCloudHomeSettingsParser.findString(settings, "Power");

        // Assert
        assertEquals(Optional.empty(), result);
    }

    @ParameterizedTest
    @CsvSource({ "True, true", "False, false" })
    void whenSettingIsBooleanLikeThenFindBooleanParsesIt(String rawValue, boolean expected) {
        // Arrange
        List<MelCloudHomeSetting> settings = settingsOf("Power", rawValue);

        // Act
        boolean result = MelCloudHomeSettingsParser.findBoolean(settings, "Power");

        // Assert
        assertEquals(expected, result);
    }

    @Test
    void whenSettingIsAbsentThenFindBooleanDefaultsToFalse() {
        // Arrange
        List<MelCloudHomeSetting> settings = List.of();

        // Act
        boolean result = MelCloudHomeSettingsParser.findBoolean(settings, "Power");

        // Assert
        assertFalse(result);
    }

    @Test
    void whenSettingIsNumericThenFindDoubleParsesIt() {
        // Arrange
        List<MelCloudHomeSetting> settings = settingsOf("SetTemperature", "21.5");

        // Act
        Optional<Double> result = MelCloudHomeSettingsParser.findDouble(settings, "SetTemperature");

        // Assert
        assertEquals(Optional.of(21.5), result);
    }

    @Test
    void whenSettingIsNotNumericThenFindDoubleReturnsEmpty() {
        // Arrange
        List<MelCloudHomeSetting> settings = settingsOf("SetTemperature", "not-a-number");

        // Act
        Optional<Double> result = MelCloudHomeSettingsParser.findDouble(settings, "SetTemperature");

        // Assert
        assertTrue(result.isEmpty());
    }
}
