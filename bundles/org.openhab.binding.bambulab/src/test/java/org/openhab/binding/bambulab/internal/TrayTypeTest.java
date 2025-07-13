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

import java.util.Arrays;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openhab.binding.bambulab.internal.BambuLabBindingConstants.AmsChannel.TrayType;

/**
 * @author Martin Grześlowski - Initial contribution
 */
@NonNullByDefault
class TrayTypeTest {
    @Test
    @DisplayName("Should find ABS when lowercase 'abs' is passed")
    public void testSuccessfullyFindsAbsWhenLowercaseAbsIsPassed() {
        // Given
        var input = "abs";

        // When
        var result = TrayType.findTrayType(input);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(TrayType.ABS);
    }

    @Test
    @DisplayName("Should find TPU when uppercase 'TPU' is passed")
    public void testSuccessfullyFindsTpuWhenUppercaseTpuIsPassed() {
        // Given
        var input = "TPU";

        // When
        var result = TrayType.findTrayType(input);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(TrayType.TPU);
    }

    @Test
    @DisplayName("Should return empty Optional when input doesn't match any TrayType")
    public void testReturnsEmptyOptionalWhenInputDoesntMatchAnyTrayType() {
        // Given
        var input = "UNKNOWN_MATERIAL";

        // When
        var result = TrayType.findTrayType(input);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should handle empty string input")
    public void testHandlesEmptyStringInput() {
        // Given
        var input = "";

        // When
        var result = TrayType.findTrayType(input);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should handle mixed case inputs like 'PeTg' correctly")
    public void testHandlesMixedCaseInputsCorrectly() {
        // Given
        var input = "PeTg";

        // When
        var result = TrayType.findTrayType(input);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(TrayType.PETG);
    }

    @Test
    @DisplayName("Should handle inputs with leading/trailing whitespace")
    public void testHandlesInputsWithLeadingAndTrailingWhitespace() {
        // Given
        var input = "  ABS  ";

        // When
        var result = TrayType.findTrayType(input);

        // Then
        assertThat(result).isEmpty();
    }

    @ParameterizedTest(name = "{index}: should parse {0} to {1}")
    @MethodSource
    void testPerformsCaseInsensitiveComparisonForAllEnumValues(String given, TrayType trayType) {
        assertThat(TrayType.findTrayType(given))//
                .isPresent()//
                .hasValueSatisfying(type -> assertThat(type).isEqualTo(trayType));
    }

    static Stream<Arguments> testPerformsCaseInsensitiveComparisonForAllEnumValues() {
        return Arrays.stream(TrayType.values())//
                .flatMap(trayType -> Stream.of(//
                        Arguments.of(trayType.name().toUpperCase(), trayType), //
                        Arguments.of(trayType.name().toLowerCase(), trayType), //
                        Arguments.of(trayType.name().charAt(0) + trayType.name().substring(1).toLowerCase(),
                                trayType)));
    }
}
