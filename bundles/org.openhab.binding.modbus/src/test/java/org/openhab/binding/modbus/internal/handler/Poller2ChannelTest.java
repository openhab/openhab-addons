/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and accompanying materials are made available under the terms of the
 * Eclipse Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0.
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
}
