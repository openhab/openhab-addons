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

import static org.assertj.core.api.Assertions.*;
import static org.openhab.core.library.unit.SIUnits.CELSIUS;
import static org.openhab.core.library.unit.Units.DECIBEL_MILLIWATTS;
import static org.openhab.core.types.UnDefType.UNDEF;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;

/**
 * @author Martin Grześlowski - Initial contribution
 */
@NonNullByDefault
class StateParserHelperTest {

    @Test
    @DisplayName("Given a valid temperature string, when parseTemperatureType is called, then it returns QuantityType with CELSIUS unit")
    void testParseTemperatureTypeConvertsValidStringToQuantityType() {
        // Given
        var temperatureString = "23.5";

        // When
        var result = StateParserHelper.parseTemperatureType(temperatureString);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isInstanceOf(QuantityType.class);
        var quantityType = (QuantityType<?>) result.get();
        assertThat(quantityType.doubleValue()).isEqualTo(23.5);
        assertThat(quantityType.getUnit()).isEqualTo(CELSIUS);
    }

    @Test
    @DisplayName("Given a valid Number, when parseDecimalType is called, then it returns DecimalType")
    void testParseDecimalTypeConvertsValidNumberToDecimalType() {
        // Given
        var number = 42.5;

        // When
        var result = StateParserHelper.parseDecimalType(number);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isInstanceOf(DecimalType.class);
        assertThat(((DecimalType) result.get()).doubleValue()).isEqualTo(42.5);
    }

    @Test
    @DisplayName("Given a valid numeric string, when parseDecimalType is called, then it returns DecimalType")
    void testParseDecimalTypeConvertsValidStringToDecimalType() {
        // Given
        var decimalString = "123.45";

        // When
        var result = StateParserHelper.parseDecimalType(decimalString);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isInstanceOf(DecimalType.class);
        assertThat(((DecimalType) result.get()).doubleValue()).isEqualTo(123.45);
    }

    @Test
    @DisplayName("Given a non-null string, when parseStringType is called, then it returns StringType")
    void testParseStringTypeConvertsNonNullStringToStringType() {
        // Given
        var inputString = "test string";

        // When
        var result = StateParserHelper.parseStringType(inputString);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isInstanceOf(StringType.class);
        assertThat(result.get().toString()).isEqualTo("test string");
    }

    @Test
    @DisplayName("Given a valid dBm pattern string, when parseWifiChannel is called, then it returns QuantityType with DECIBEL_MILLIWATTS unit")
    void testParseWifiChannelConvertsValidDbmPatternToQuantityType() {
        // Given
        var wifiString = "-75dBm";

        // When
        var result = StateParserHelper.parseWifiChannel(wifiString);

        // Then
        assertThat(result).isInstanceOf(QuantityType.class);
        var quantityType = (QuantityType<?>) result;
        assertThat(quantityType.intValue()).isEqualTo(-75);
        assertThat(quantityType.getUnit()).isEqualTo(DECIBEL_MILLIWATTS);
    }

    @Test
    @DisplayName("Given a valid 6-character hex string, when parseColor is called, then it returns HSB color State")
    void testParseColorConvertsValidHexStringToHsbColor() {
        // Given
        var colorHex = "FF0000"; // Red in RGB

        // When
        var result = StateParserHelper.parseColor(colorHex);

        // Then
        assertThat(result).isNotEqualTo(UNDEF);

        // HSB for pure red should have H=0, S=100, B=100
        var hsbValues = result.toString().split(",");
        assertThat(Double.parseDouble(hsbValues[0])).isCloseTo(0.0, within(0.1));
        assertThat(Double.parseDouble(hsbValues[1])).isCloseTo(100.0, within(0.1));
        assertThat(Double.parseDouble(hsbValues[2])).isCloseTo(100.0, within(0.1));
    }

    @Test
    @DisplayName("Given a null input, when parseTemperatureType is called, then it returns empty Optional")
    void testParseTemperatureTypeReturnsEmptyOptionalForNullInput() {
        // Given
        String temperatureString = null;

        // When
        var result = StateParserHelper.parseTemperatureType(temperatureString);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Given a non-numeric string, when parseTemperatureType is called, then it returns empty Optional")
    void testParseTemperatureTypeReturnsEmptyOptionalForNonNumericString() {
        // Given
        var temperatureString = "not-a-number";

        // When
        var result = StateParserHelper.parseTemperatureType(temperatureString);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Given a null Number, when parseDecimalType is called, then it returns empty Optional")
    void testParseDecimalTypeReturnsEmptyOptionalForNullNumber() {
        // Given
        Number number = null;

        // When
        var result = StateParserHelper.parseDecimalType(number);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Given a null String, when parseDecimalType is called, then it returns empty Optional")
    void testParseDecimalTypeReturnsEmptyOptionalForNullString() {
        // Given
        String decimalString = null;

        // When
        var result = StateParserHelper.parseDecimalType(decimalString);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Given a non-numeric String, when parseDecimalType is called, then it returns empty Optional")
    void testParseDecimalTypeReturnsEmptyOptionalForNonNumericString() {
        // Given
        var decimalString = "not-a-decimal";

        // When
        var result = StateParserHelper.parseDecimalType(decimalString);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Given a null String, when parseStringType is called, then it returns empty Optional")
    void testParseStringTypeReturnsEmptyOptionalForNullString() {
        // Given
        String inputString = null;

        // When
        var result = StateParserHelper.parseStringType(inputString);

        // Then
        assertThat(result).isEmpty();
    }
}
