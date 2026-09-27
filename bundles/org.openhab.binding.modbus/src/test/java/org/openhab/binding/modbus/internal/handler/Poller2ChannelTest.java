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
package org.openhab.binding.modbus.internal.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openhab.core.io.transport.modbus.ModbusConstants.ValueType;
import org.openhab.core.io.transport.modbus.ModbusRegisterArray;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.types.UnDefType;

/**
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
public class Poller2ChannelTest {

    private static ModbusRegisterArray registers(int... values) {
        return new ModbusRegisterArray(values);
    }

    static Stream<Arguments> decodedValues() {
        return Stream.of(Arguments.of(ValueType.INT16, 100, registers(0x8000), new DecimalType("-32768")),
                Arguments.of(ValueType.INT16, 100, registers(0x7FFF), new DecimalType("32767")),
                Arguments.of(ValueType.UINT16, 100, registers(0x0000), DecimalType.ZERO),
                Arguments.of(ValueType.UINT16, 100, registers(0xFFFF), new DecimalType("65535")),
                Arguments.of(ValueType.INT32, 100, registers(0x8000, 0x0000), new DecimalType("-2147483648")),
                Arguments.of(ValueType.UINT32, 100, registers(0xFFFF, 0xFFFF), new DecimalType("4294967295")),
                Arguments.of(ValueType.FLOAT32, 100, registers(0x3F80, 0x0000), new DecimalType("1.0")),
                Arguments.of(ValueType.FLOAT32, 100, registers(0x7FC0, 0x0000), UnDefType.UNDEF));
    }

    static Stream<Arguments> indexedValues() {
        return Stream.of(Arguments.of("100.0", ValueType.BIT, registers(0x0001, 0x8000), new DecimalType("1")),
                Arguments.of("100.15", ValueType.BIT, registers(0x0001, 0x8000), DecimalType.ZERO),
                Arguments.of("101.15", ValueType.BIT, registers(0x0001, 0x8000), new DecimalType("1")),
                Arguments.of("100.0", ValueType.UINT8, registers(0x0603, 0x0405), new DecimalType("3")),
                Arguments.of("100.1", ValueType.UINT8, registers(0x0603, 0x0405), new DecimalType("6")),
                Arguments.of("101.0", ValueType.UINT8, registers(0x0603, 0x0405), new DecimalType("5")),
                Arguments.of("101.1", ValueType.UINT8, registers(0x0603, 0x0405), new DecimalType("4")),
                Arguments.of("101", ValueType.UINT16, registers(0x0603, 0x0405), new DecimalType("1029")));
    }

    static Stream<Arguments> invalidReadRanges() {
        return Stream.of(Arguments.of("99", ValueType.UINT16, "Address 99 is before poll window 100..103"),
                Arguments.of("104", ValueType.UINT16, "Address 104 is outside poll window 100..103"),
                Arguments.of("103", ValueType.INT32, "Address 103 with value type int32 exceeds poll window 100..103"),
                Arguments.of("100", ValueType.BIT, "Address X.Y must be used with value type bit"),
                Arguments.of("100.16", ValueType.BIT, "Sub-address 16 is invalid for value type bit"),
                Arguments.of("100.2", ValueType.UINT8, "Sub-address 2 is invalid for value type uint8"),
                Arguments.of("100.1", ValueType.UINT16, "Sub-address 1 is invalid for value type uint16"));
    }

    @ParameterizedTest
    @MethodSource("decodedValues")
    public void decodesTheConfiguredPointFromThePollerWindow(ValueType valueType, int address,
            ModbusRegisterArray registers, Object expectedState) {
        Poller2Channel channel = new Poller2Channel(address, valueType, PollerReadFailurePolicy.UNDEF);

        assertEquals(expectedState, channel.acceptRegisters(registers, 100));
    }

    @ParameterizedTest
    @MethodSource("decodedValues")
    public void returnsUndefWhenTheConfiguredPointIsOutsideThePollerWindow(ValueType valueType, int address,
            ModbusRegisterArray registers, Object ignored) {
        Poller2Channel channel = new Poller2Channel(address + registers.size(), valueType,
                PollerReadFailurePolicy.UNDEF);

        assertEquals(UnDefType.UNDEF, channel.acceptRegisters(registers, 100));
    }

    @ParameterizedTest
    @MethodSource("indexedValues")
    public void honorsLowHighBitAndByteIndexesAcrossRegisters(String address, ValueType valueType,
            ModbusRegisterArray registers, Object expectedState) {
        Poller2Channel channel = new Poller2Channel(address, valueType, PollerReadFailurePolicy.UNDEF);

        assertEquals(expectedState, channel.acceptRegisters(registers, 100));
    }

    @ParameterizedTest
    @MethodSource("invalidReadRanges")
    public void reportsAConfigurationErrorForInvalidAddressRanges(String address, ValueType valueType,
            String expectedMessage) {
        Poller2Channel channel = new Poller2Channel(address, valueType, PollerReadFailurePolicy.UNDEF);

        assertEquals(expectedMessage, channel.validateReadRange(100, 4).orElseThrow());
    }

    @ParameterizedTest
    @MethodSource("crossRegisterValues")
    public void decodesValuesThatSpanWholeRegisterBoundaries(String address, ValueType valueType,
            ModbusRegisterArray registers, Object expectedState) {
        Poller2Channel channel = new Poller2Channel(address, valueType, PollerReadFailurePolicy.UNDEF);

        assertEquals(expectedState, channel.acceptRegisters(registers, 100));
    }

    static Stream<Arguments> crossRegisterValues() {
        return Stream.of(
                Arguments.of("101", ValueType.INT32, registers(0xFFFF, 0x0001, 0x0002), new DecimalType("65538")),
                Arguments.of("101", ValueType.UINT32, registers(0xFFFF, 0x0001, 0x0002), new DecimalType("65538")));
    }
}
