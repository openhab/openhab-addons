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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openhab.core.io.transport.modbus.ModbusRegisterArray;

/**
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
public class PollerRegisterCacheTest {

    private static ModbusRegisterArray registers(int... values) {
        return new ModbusRegisterArray(values);
    }

    static Stream<Arguments> bitOverlays() {
        return Stream.of(Arguments.of(0x0000, 0, true, 0x0001), Arguments.of(0xFFFF, 0, false, 0xFFFE),
                Arguments.of(0x0000, 15, true, 0x8000), Arguments.of(0xFFFF, 15, false, 0x7FFF),
                Arguments.of(0xA55A, 4, true, 0xA55A), Arguments.of(0xA55A, 5, false, 0xA55A),
                Arguments.of(0xA55A, 7, true, 0xA5DA), Arguments.of(0xA55A, 8, false, 0xA45A));
    }

    @Test
    public void returnsNoDataBeforeSuccessfulPoll() {
        PollerRegisterCache cache = new PollerRegisterCache();

        assertTrue(cache.read(100, 1).isEmpty());
    }

    @Test
    public void returnsTheRequestedSubrangeFromLatestSuccessfulPoll() {
        PollerRegisterCache cache = new PollerRegisterCache();
        cache.acceptSuccessfulPoll(100, registers(0x1234, 0x5678, 0x9ABC));

        assertEquals(Optional.of(registers(0x5678, 0x9ABC)), cache.read(101, 2));
        assertTrue(cache.read(99, 1).isEmpty());
        assertTrue(cache.read(102, 2).isEmpty());
    }

    @ParameterizedTest
    @MethodSource("bitOverlays")
    public void overlaysOnlyTheRequestedBitInTheCachedRegister(int initialValue, int bit, boolean set,
            int expectedValue) {
        PollerRegisterCache cache = new PollerRegisterCache();
        cache.acceptSuccessfulPoll(100, registers(initialValue, 0x1357));

        assertEquals(Optional.of(registers(expectedValue)), cache.overlayRegisterBit(100, bit, set));
        assertEquals(Optional.of(registers(expectedValue, 0x1357)), cache.read(100, 2));
    }

    @Test
    public void refusesAnOverlayWhenTheRegisterWasNotRead() {
        PollerRegisterCache cache = new PollerRegisterCache();
        cache.acceptSuccessfulPoll(100, registers(0x1234));

        assertTrue(cache.overlayRegisterBit(101, 0, true).isEmpty());
        assertEquals(Optional.of(registers(0x1234)), cache.read(100, 1));
    }
}
