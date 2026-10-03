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

    static Stream<Arguments> overlays() {
        return Stream.of(Arguments.of(0, 0x1356, 0x1357), Arguments.of(3, 0x1357, 0x135F),
                Arguments.of(15, 0x1357, 0x9357));
    }

    @ParameterizedTest
    @MethodSource("overlays")
    public void overlaysRequestedBitWithoutChangingCachedPollImage(int bit, int original, int expected) {
        PollerRegisterCache cache = new PollerRegisterCache();
        cache.acceptSuccessfulPoll(100, registers(original, 0x2468));

        assertEquals(Optional.of(registers(expected)), cache.overlayRegisterBit(100, bit, true));
        assertEquals(Optional.of(registers(original, 0x2468)), cache.read(100, 2));
    }

    @Test
    public void refusesOverlayWithoutSuccessfulPoll() {
        PollerRegisterCache cache = new PollerRegisterCache();

        assertTrue(cache.overlayRegisterBit(100, 3, true).isEmpty());
    }
}
