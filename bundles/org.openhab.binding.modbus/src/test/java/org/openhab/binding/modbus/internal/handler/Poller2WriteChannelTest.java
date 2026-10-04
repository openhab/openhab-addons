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
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openhab.core.io.transport.modbus.ModbusConstants.ValueType;
import org.openhab.core.io.transport.modbus.ModbusRegisterArray;
import org.openhab.core.io.transport.modbus.ModbusWriteCoilRequestBlueprint;
import org.openhab.core.io.transport.modbus.ModbusWriteFunctionCode;
import org.openhab.core.io.transport.modbus.ModbusWriteRegisterRequestBlueprint;
import org.openhab.core.io.transport.modbus.ModbusWriteRequestBlueprint;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;

/**
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
public class Poller2WriteChannelTest {

    @Test
    public void createsHoldingWriteFromQuantityMagnitudeAndConstantTransform() {
        Poller2WriteChannel channel = new Poller2WriteChannel(42, ValueType.INT16, List.of("17"), 2, false);

        ModbusWriteRequestBlueprint request = channel.requestFor(new QuantityType<>("4.5 kW"), 9).orElseThrow();

        ModbusWriteRegisterRequestBlueprint registerRequest = assertInstanceOf(
                ModbusWriteRegisterRequestBlueprint.class, request);
        assertEquals(42, registerRequest.getReference());
        assertEquals(2, registerRequest.getMaxTries());
        assertEquals(17, registerRequest.getRegisters().getRegister(0));
    }

    @Test
    public void usesWriteMultipleForExplicitScalarHoldingWrite() {
        Poller2WriteChannel channel = new Poller2WriteChannel(42, ValueType.INT16, List.of("default"), 2, false);

        ModbusWriteRequestBlueprint request = channel
                .requestFor(new org.openhab.core.library.types.DecimalType("17"), 9, null, true).orElseThrow();

        assertEquals(ModbusWriteFunctionCode.WRITE_MULTIPLE_REGISTERS, request.getFunctionCode());
    }

    @Test
    public void usesWriteMultipleForExplicitScalarCoilWrite() {
        Poller2WriteChannel channel = new Poller2WriteChannel(42, ValueType.BIT, List.of("default"), 2, false, true);

        ModbusWriteCoilRequestBlueprint request = assertInstanceOf(ModbusWriteCoilRequestBlueprint.class,
                channel.requestFor(OnOffType.ON, 9, null, true).orElseThrow());

        assertEquals(ModbusWriteFunctionCode.WRITE_MULTIPLE_COILS, request.getFunctionCode());
    }

    @Test
    public void doesNotCreateWriteWhenTransformOutputIsNotACommand() {
        Poller2WriteChannel channel = new Poller2WriteChannel(42, ValueType.INT16, List.of("not-a-command"), 2, false);

        assertTrue(channel.requestFor(new QuantityType<>("4.5 kW"), 9).isEmpty());
    }

    static Stream<Arguments> bitOverlays() {
        return Stream.of(Arguments.of(0, 0x1356, 0x1357), Arguments.of(3, 0x1357, 0x135F),
                Arguments.of(15, 0x1357, 0x9357));
    }

    @ParameterizedTest
    @MethodSource("bitOverlays")
    public void overlaysHoldingRegisterBitFromSuccessfulPoll(int bit, int original, int expected) {
        Poller2WriteChannel channel = new Poller2WriteChannel(100, bit, List.of("default"), 2, false);
        PollerRegisterCache cache = new PollerRegisterCache();
        cache.acceptSuccessfulPoll(100, new ModbusRegisterArray(original));

        ModbusWriteRegisterRequestBlueprint request = assertInstanceOf(ModbusWriteRegisterRequestBlueprint.class,
                channel.requestFor(OnOffType.ON, 9, cache).orElseThrow());

        assertEquals(100, request.getReference());
        assertEquals(expected, request.getRegisters().getRegister(0));
        assertEquals(Optional.of(new ModbusRegisterArray(original)), cache.read(100, 1));
    }

    @Test
    public void refusesHoldingRegisterBitWriteWithoutSuccessfulPoll() {
        Poller2WriteChannel channel = new Poller2WriteChannel(100, 3, List.of("default"), 2, false);

        assertTrue(channel.requestFor(OnOffType.ON, 9, new PollerRegisterCache()).isEmpty());
    }
}
