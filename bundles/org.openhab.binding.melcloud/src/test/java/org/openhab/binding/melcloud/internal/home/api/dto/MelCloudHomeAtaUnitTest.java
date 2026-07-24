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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Unit tests for {@link MelCloudHomeAtaUnit}'s settings-normalization accessors (see ADR-003).
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
class MelCloudHomeAtaUnitTest {

    private static MelCloudHomeAtaUnit unitWithSetting(String name, String value) {
        MelCloudHomeSetting setting = new MelCloudHomeSetting();
        setting.name = name;
        setting.value = value;
        MelCloudHomeAtaUnit unit = new MelCloudHomeAtaUnit();
        List<MelCloudHomeSetting> settings = new ArrayList<>(unit.settings);
        settings.add(setting);
        unit.settings = settings;
        return unit;
    }

    @ParameterizedTest
    @CsvSource({ "0, Auto", "1, One", "2, Two", "3, Three", "4, Four", "5, Five" })
    void whenFanSpeedIsNumeric_thenGetFanSpeedNormalizesToWord(String rawValue, String expectedWord) {
        // Arrange
        MelCloudHomeAtaUnit unit = unitWithSetting("SetFanSpeed", rawValue);

        // Act
        String fanSpeed = unit.getFanSpeed().orElseThrow();

        // Assert
        assertEquals(expectedWord, fanSpeed);
    }

    @Test
    void whenFanSpeedIsAlreadyAWord_thenGetFanSpeedPassesItThrough() {
        // Arrange
        MelCloudHomeAtaUnit unit = unitWithSetting("SetFanSpeed", "Auto");

        // Act
        String fanSpeed = unit.getFanSpeed().orElseThrow();

        // Assert
        assertEquals("Auto", fanSpeed);
    }

    @ParameterizedTest
    @CsvSource({ "0, Auto", "7, Swing" })
    void whenVaneVerticalIsNumeric_thenGetVaneVerticalDirectionNormalizesToWord(String rawValue, String expectedWord) {
        // Arrange
        MelCloudHomeAtaUnit unit = unitWithSetting("VaneVerticalDirection", rawValue);

        // Act
        String direction = unit.getVaneVerticalDirection().orElseThrow();

        // Assert
        assertEquals(expectedWord, direction);
    }

    @ParameterizedTest
    @CsvSource({ "CenterLeft, LeftCentre", "Center, Centre", "CenterRight, RightCentre" })
    void whenVaneHorizontalIsAmericanSpelling_thenGetVaneHorizontalDirectionNormalizesToBritish(String rawValue,
            String expectedWord) {
        // Arrange
        MelCloudHomeAtaUnit unit = unitWithSetting("VaneHorizontalDirection", rawValue);

        // Act
        String direction = unit.getVaneHorizontalDirection().orElseThrow();

        // Assert
        assertEquals(expectedWord, direction);
    }

    @Test
    void whenVaneHorizontalIsAlreadyBritishSpelling_thenGetVaneHorizontalDirectionPassesItThrough() {
        // Arrange
        MelCloudHomeAtaUnit unit = unitWithSetting("VaneHorizontalDirection", "Left");

        // Act
        String direction = unit.getVaneHorizontalDirection().orElseThrow();

        // Assert
        assertEquals("Left", direction);
    }

    @Test
    void whenPowerSettingIsMissing_thenIsPowerDefaultsToFalse() {
        // Arrange
        MelCloudHomeAtaUnit unit = new MelCloudHomeAtaUnit();

        // Act
        boolean power = unit.isPower();

        // Assert
        assertFalse(power);
    }

    @Test
    void whenOperationModeSettingIsMissing_thenGetOperationModeDefaultsToHeat() {
        // Arrange
        MelCloudHomeAtaUnit unit = new MelCloudHomeAtaUnit();

        // Act
        String operationMode = unit.getOperationMode();

        // Assert
        assertEquals("Heat", operationMode);
    }

    @Test
    void whenIsInErrorSettingIsTrue_thenIsInErrorReturnsTrue() {
        // Arrange
        MelCloudHomeAtaUnit unit = unitWithSetting("IsInError", "True");

        // Act
        boolean isInError = unit.isInError();

        // Assert
        assertTrue(isInError);
    }
}
