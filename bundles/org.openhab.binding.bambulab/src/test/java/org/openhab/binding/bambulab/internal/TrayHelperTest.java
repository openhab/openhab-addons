/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.bambulab.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.openhab.core.types.UnDefType.UNDEF;

import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openhab.core.library.types.StringType;

/**
 * @author Martin Grześlowski - Initial contribution
 */
@SuppressWarnings("OptionalGetWithoutIsPresent")
@NonNullByDefault
class TrayHelperTest {

    @Test
    @DisplayName("String '255' returns EMPTY state")
    public void testString255ReturnsEmpty() {
        // Given
        var tray = "255";

        // When
        var result = TrayHelper.findStateForTrayLoaded(tray).get();

        // Then
        assertThat(result).isInstanceOf(StringType.class);
        assertThat(result.toString()).isEqualTo("EMPTY");
    }

    @Test
    @DisplayName("String '254' returns VTRAY state")
    public void testString254ReturnsVtray() {
        // Given
        var tray = "254";

        // When
        var result = TrayHelper.findStateForTrayLoaded(tray).get();

        // Then
        assertThat(result).isInstanceOf(StringType.class);
        assertThat(result.toString()).isEqualTo("VTRAY");
    }

    @ParameterizedTest(name = "{index}: should parse {0} as {1}")
    @MethodSource
    public void happyPath(int tray, String expected) {
        // When
        var result = TrayHelper.findStateForTrayLoaded(tray + "").get();

        // Then
        assertThat(result.toString()).isEqualTo(expected);
    }

    static Stream<Arguments> happyPath() {
        return Stream.of(
                // AMS 1
                Arguments.of(0, "AMS_1_1"), //
                Arguments.of(1, "AMS_1_2"), //
                Arguments.of(2, "AMS_1_3"), //
                Arguments.of(3, "AMS_1_4"), //
                // AMS 2
                Arguments.of(0 + 4, "AMS_2_1"), //
                Arguments.of(1 + 4, "AMS_2_2"), //
                Arguments.of(2 + 4, "AMS_2_3"), //
                Arguments.of(3 + 4, "AMS_2_4"), //
                // AMS 3
                Arguments.of(0 + 8, "AMS_3_1"), //
                Arguments.of(1 + 8, "AMS_3_2"), //
                Arguments.of(2 + 8, "AMS_3_3"), //
                Arguments.of(3 + 8, "AMS_3_4"), //
                // AMS 4
                Arguments.of(0 + 12, "AMS_4_1"), //
                Arguments.of(1 + 12, "AMS_4_2"), //
                Arguments.of(2 + 12, "AMS_4_3"), //
                Arguments.of(3 + 12, "AMS_4_4"));
    }

    @Test
    @DisplayName("Null input returns UNDEF")
    public void testNullInputReturnsUndef() {
        // Given
        String tray = null;

        // When
        var result = TrayHelper.findStateForTrayLoaded(tray);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Non-numeric string returns UNDEF")
    public void testNonNumericStringReturnsUndef() {
        // Given
        var tray = "not-a-number";

        // When
        var result = TrayHelper.findStateForTrayLoaded(tray).get();

        // Then
        assertThat(result).isEqualTo(UNDEF);
    }

    @Test
    @DisplayName("Empty string returns UNDEF")
    public void testEmptyStringReturnsUndef() {
        // Given
        var tray = "";

        // When
        var result = TrayHelper.findStateForTrayLoaded(tray).get();

        // Then
        assertThat(result).isEqualTo(UNDEF);
    }

    @Test
    @DisplayName("Negative numbers are processed as undef")
    public void negatives() {
        // Given
        var tray = "-5";

        // When
        var result = TrayHelper.findStateForTrayLoaded(tray).get();

        // Then
        assertThat(result).isEqualTo(UNDEF);
    }

    @Test
    @DisplayName("Exceeding maximum tray value returns UNDEF with warning")
    public void testExceedingMaxValueReturnsUndefWithWarning() {
        // Given
        var tray = (TrayHelper.MAX_TRAY_VALUE + 1) + "";

        // When
        var result = TrayHelper.findStateForTrayLoaded(tray).get();

        // Then
        assertThat(result).isEqualTo(UNDEF);
    }

    @Test
    @DisplayName("Value for `MAX_TRAY_VALUE` should return `AMS_4_4`")
    public void testForMaxTray() {
        // Given
        var tray = TrayHelper.MAX_TRAY_VALUE + "";

        // When
        var result = TrayHelper.findStateForTrayLoaded(tray).get();

        // Then
        assertThat(result.toString()).isEqualTo("AMS_4_4");
    }
}
