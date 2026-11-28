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
package org.openhab.binding.solarman.internal.util;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Exercises full-width signed boundaries and low-word-first wire order.
 *
 * @author Ricardo Costa - Initial contribution
 */
@NonNullByDefault
class ModbusRegisterCodecTest {
    static Stream<Arguments> boundaries() {
        return Stream.of(Arguments.of(32767L, 1, true), Arguments.of(-32768L, 1, true), Arguments.of(65535L, 1, false),
                Arguments.of(-1L, 1, true), Arguments.of(2147483647L, 2, true), Arguments.of(-2147483648L, 2, true),
                Arguments.of(4294967295L, 2, false), Arguments.of(-1L, 2, true), Arguments.of(32768L, 2, true),
                Arguments.of(65535L, 2, true));
    }

    @ParameterizedTest
    @MethodSource("boundaries")
    void fullWidthRoundTrip(long value, int count, boolean signed) {
        byte[] bytes = ModbusRegisterCodec.encode(BigInteger.valueOf(value), count, signed);
        Map<Integer, byte[]> snapshot = count == 1 ? Map.of(78, bytes)
                : Map.of(78, new byte[] { bytes[0], bytes[1] }, 80, new byte[] { bytes[2], bytes[3] });
        assertEquals(BigInteger.valueOf(value),
                ModbusRegisterCodec.decode(count == 1 ? List.of(78) : List.of(78, 80), snapshot, signed));
    }

    @Test
    void usesLowWordFirstAndRejectsOverflow() {
        assertArrayEquals(new byte[] { 0x56, 0x78, 0x12, 0x34 },
                ModbusRegisterCodec.encode(new BigInteger("12345678", 16), 2, false));
        assertEquals(new BigInteger("32768"), ModbusRegisterCodec.decode(List.of(1, 2),
                Map.of(1, ByteBuffer.allocate(2).putShort((short) 0x8000).array(), 2, new byte[2]), true));
        assertThrows(IllegalArgumentException.class,
                () -> ModbusRegisterCodec.encode(BigInteger.valueOf(-1), 1, false));
        assertThrows(IllegalArgumentException.class,
                () -> ModbusRegisterCodec.encode(BigInteger.valueOf(65536), 1, false));
        assertThrows(IllegalArgumentException.class,
                () -> ModbusRegisterCodec.encode(BigInteger.valueOf(32768), 1, true));
        assertThrows(IllegalArgumentException.class,
                () -> ModbusRegisterCodec.encode(BigInteger.ONE.shiftLeft(32), 2, false));
    }
}
