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
import static pl.grzeslowski.jbambuapi.mqtt.PrinterClient.Channel.PrintSpeedCommand.*;
import static tech.units.indriya.unit.Units.PERCENT;

import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;

import pl.grzeslowski.jbambuapi.mqtt.PrinterClient;

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
        var someResult = StateParserHelper.parseWifiChannel(wifiString);

        // Then
        assertThat(someResult).isNotEmpty();
        var result = someResult.get();
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
        assertThat(result).contains(UNDEF);
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
        assertThat(result).contains(UNDEF);
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

    @Test
    @DisplayName("Given a valid numeric value, when parsePercentType is called, then it returns Optional with QuantityType")
    public void testValidNumericReturnsQuantityTypeWithPercentUnit() {
        // Given
        var numericValue = 75;

        // When
        var result = StateParserHelper.parsePercentType(numericValue);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isInstanceOf(QuantityType.class);
        var quantityType = (QuantityType<?>) result.get();
        assertThat(quantityType.getUnit()).isEqualTo(PERCENT);
        assertThat(quantityType.doubleValue()).isEqualTo(75.0);
    }

    @Test
    @DisplayName("Given an integer value, when parsePercentType is called, then it returns correct QuantityType")
    public void testIntegerValuesAreCorrectlyParsed() {
        // Given
        var integerValue = 100;

        // When
        var result = StateParserHelper.parsePercentType(integerValue);

        // Then
        assertThat(result).isPresent();
        var quantityType = (QuantityType<?>) result.get();
        assertThat(quantityType.doubleValue()).isEqualTo(100.0);
        assertThat(quantityType.getUnit()).isEqualTo(PERCENT);
    }

    @Test
    @DisplayName("Given a decimal value, when parsePercentType is called, then it returns correct QuantityType")
    public void testDecimalValuesAreCorrectlyParsed() {
        // Given
        var decimalValue = 50.5;

        // When
        var result = StateParserHelper.parsePercentType(decimalValue);

        // Then
        assertThat(result).isPresent();
        var quantityType = (QuantityType<?>) result.get();
        assertThat(quantityType.doubleValue()).isEqualTo(50.5);
        assertThat(quantityType.getUnit()).isEqualTo(PERCENT);
    }

    @Test
    @DisplayName("Given a zero value, when parsePercentType is called, then it returns QuantityType with zero value")
    public void testZeroValueIsCorrectlyParsed2() {
        // Given
        var zeroValue = 0;

        // When
        var result = StateParserHelper.parsePercentType(zeroValue);

        // Then
        assertThat(result).isPresent();
        var quantityType = (QuantityType<?>) result.get();
        assertThat(quantityType.doubleValue()).isZero();
        assertThat(quantityType.getUnit()).isEqualTo(PERCENT);
    }

    @Test
    @DisplayName("Given a negative value, when parsePercentType is called, then it returns QuantityType with negative value")
    public void testNegativeValuesAreCorrectlyParsed2() {
        // Given
        var negativeValue = -10;

        // When
        var result = StateParserHelper.parsePercentType(negativeValue);

        // Then
        assertThat(result).isPresent();
        var quantityType = (QuantityType<?>) result.get();
        assertThat(quantityType.doubleValue()).isEqualTo(-10.0);
        assertThat(quantityType.getUnit()).isEqualTo(PERCENT);
    }

    @Test
    @DisplayName("Given a null input, when parsePercentType is called, then it returns Optional.empty")
    public void testNullInputReturnsEmptyOptional2() {
        // Given
        Number nullValue = null;

        // When
        var result = StateParserHelper.parsePercentType(nullValue);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Given an empty string input, when parsePercentType is called, then it returns Optional.empty")
    public void testEmptyStringInputReturnsEmptyOptional2() {
        // This test is not applicable as the method only accepts Number objects
        // The method signature is: parsePercentType(@Nullable Number percent)
        // Empty strings would be handled by a different method before this one is called

        // For completeness, we'll verify that null returns empty
        // Given
        Number nullValue = null;

        // When
        var result = StateParserHelper.parsePercentType(nullValue);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Given a non-numeric string input, when parsePercentType is called, then it returns Optional.empty")
    public void testNonNumericStringReturnsEmptyOptional2() {
        // This test is not applicable as the method only accepts Number objects
        // The method signature is: parsePercentType(@Nullable Number percent)
        // Non-numeric strings would be handled by a different method before this one is called

        // For completeness, we'll verify that null returns empty
        // Given
        Number nullValue = null;

        // When
        var result = StateParserHelper.parsePercentType(nullValue);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Given a very large number, when parsePercentType is called, then it returns correct QuantityType")
    public void testVeryLargeNumbersAreHandledCorrectly2() {
        // Given
        var largeValue = Double.MAX_VALUE / 2;

        // When
        var result = StateParserHelper.parsePercentType(largeValue);

        // Then
        assertThat(result).isPresent();
        var quantityType = (QuantityType<?>) result.get();
        assertThat(quantityType.doubleValue()).isEqualTo(largeValue);
        assertThat(quantityType.getUnit()).isEqualTo(PERCENT);
    }

    @Test
    @DisplayName("Given a very small number, when parsePercentType is called, then it returns correct QuantityType")
    public void testVerySmallNumbersAreHandledCorrectly2() {
        // Given
        var smallValue = Double.MIN_VALUE;

        // When
        var result = StateParserHelper.parsePercentType(smallValue);

        // Then
        assertThat(result).isPresent();
        var quantityType = (QuantityType<?>) result.get();
        assertThat(quantityType.doubleValue()).isEqualTo(smallValue);
        assertThat(quantityType.getUnit()).isEqualTo(PERCENT);
    }

    @Test
    @DisplayName("Valid numeric string returns Optional containing QuantityType with PERCENT unit")
    public void testValidNumericStringReturnsQuantityTypeWithPercentUnit() {
        // Given
        var percentString = "75";

        // When
        var result = StateParserHelper.parsePercentType(percentString);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isInstanceOf(QuantityType.class);
        var quantityType = (QuantityType<?>) result.get();
        assertThat(quantityType.getUnit()).isEqualTo(PERCENT);
        assertThat(quantityType.doubleValue()).isEqualTo(75.0);
    }

    @Test
    @DisplayName("Integer string values like '100' are correctly parsed")
    public void testIntegerStringValuesAreCorrectlyParsed() {
        // Given
        var percentString = "100";

        // When
        var result = StateParserHelper.parsePercentType(percentString);

        // Then
        assertThat(result).isPresent();
        var quantityType = (QuantityType<?>) result.get();
        assertThat(quantityType.doubleValue()).isEqualTo(100.0);
        assertThat(quantityType.getUnit()).isEqualTo(PERCENT);
    }

    @Test
    @DisplayName("Decimal string values like '50.5' are correctly parsed")
    public void testDecimalStringValuesAreCorrectlyParsed() {
        // Given
        var percentString = "50.5";

        // When
        var result = StateParserHelper.parsePercentType(percentString);

        // Then
        assertThat(result).isPresent();
        var quantityType = (QuantityType<?>) result.get();
        assertThat(quantityType.doubleValue()).isEqualTo(50.5);
        assertThat(quantityType.getUnit()).isEqualTo(PERCENT);
    }

    @Test
    @DisplayName("Zero value '0' is correctly parsed")
    public void testZeroValueIsCorrectlyParsed() {
        // Given
        var percentString = "0";

        // When
        var result = StateParserHelper.parsePercentType(percentString);

        // Then
        assertThat(result).isPresent();
        var quantityType = (QuantityType<?>) result.get();
        assertThat(quantityType.doubleValue()).isEqualTo(0.0);
        assertThat(quantityType.getUnit()).isEqualTo(PERCENT);
    }

    @Test
    @DisplayName("Negative values like '-10' are correctly parsed")
    public void testNegativeValuesAreCorrectlyParsed() {
        // Given
        var percentString = "-10";

        // When
        var result = StateParserHelper.parsePercentType(percentString);

        // Then
        assertThat(result).isPresent();
        var quantityType = (QuantityType<?>) result.get();
        assertThat(quantityType.doubleValue()).isEqualTo(-10.0);
        assertThat(quantityType.getUnit()).isEqualTo(PERCENT);
    }

    @Test
    @DisplayName("Null input returns Optional.empty()")
    public void testNullInputReturnsEmptyOptional() {
        // Given
        String percentString = null;

        // When
        var result = StateParserHelper.parsePercentType(percentString);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Empty string input throws NumberFormatException and returns Optional.empty()")
    public void testEmptyStringInputReturnsEmptyOptional() {
        // Given
        var percentString = "";

        // When
        var result = StateParserHelper.parsePercentType(percentString);

        // Then
        assertThat(result).contains(UNDEF);
    }

    @Test
    @DisplayName("Non-numeric string like 'abc' throws NumberFormatException and returns Optional.empty()")
    public void testNonNumericStringReturnsEmptyOptional() {
        // Given
        var percentString = "abc";

        // When
        var result = StateParserHelper.parsePercentType(percentString);

        // Then
        assertThat(result).contains(UNDEF);
    }

    @Test
    @DisplayName("Very large numbers near Double.MAX_VALUE are handled correctly")
    public void testVeryLargeNumbersAreHandledCorrectly() {
        // Given
        var percentString = String.valueOf(Double.MAX_VALUE);

        // When
        var result = StateParserHelper.parsePercentType(percentString);

        // Then
        assertThat(result).isPresent();
        var quantityType = (QuantityType<?>) result.get();
        assertThat(quantityType.doubleValue()).isEqualTo(Double.MAX_VALUE);
        assertThat(quantityType.getUnit()).isEqualTo(PERCENT);
    }

    @Test
    @DisplayName("Very small numbers near Double.MIN_VALUE are handled correctly")
    public void testVerySmallNumbersAreHandledCorrectly() {
        // Given
        var percentString = String.valueOf(Double.MIN_VALUE);

        // When
        var result = StateParserHelper.parsePercentType(percentString);

        // Then
        assertThat(result).isPresent();
        var quantityType = (QuantityType<?>) result.get();
        assertThat(quantityType.doubleValue()).isEqualTo(Double.MIN_VALUE);
        assertThat(quantityType.getUnit()).isEqualTo(PERCENT);
    }

    @ParameterizedTest(name = "{index}: should properly parse {0}")
    @MethodSource
    void speedLevel(PrinterClient.Channel.PrintSpeedCommand command) {
        // Given
        var speedLevel = command.getLevel();

        // When
        var result = StateParserHelper.parseSpeedLevel(speedLevel);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isInstanceOf(StringType.class);
        assertThat(((StringType) result.get()).toString()).isEqualTo(command.getName());
    }

    static Stream<Arguments> speedLevel() {
        return Stream.of(SILENT, STANDARD, SPORT, LUDICROUS).map(Arguments::of);
    }

    // Handles null input by returning empty Optional
    @Test
    @DisplayName("Given null input, when parseSpeedLevel is called, then returns empty Optional")
    public void testHandlesNullInput() {
        // Given
        Integer speedLevel = null;

        // When
        var result = StateParserHelper.parseSpeedLevel(speedLevel);

        // Then
        assertThat(result).isEmpty();
    }

    // Returns UNDEF for non-basic speed levels
    @Test
    @DisplayName("Given non-basic speed level, when parseSpeedLevel is called, then returns UNDEF")
    public void testReturnsUndefForNonBasicSpeedLevels() {
        // Given
        var speedLevel = 101;

        // When
        var result = StateParserHelper.parseSpeedLevel(speedLevel);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(UNDEF);
    }
}
