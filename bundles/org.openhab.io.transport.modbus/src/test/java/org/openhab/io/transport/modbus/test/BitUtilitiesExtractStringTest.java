/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.io.transport.modbus.test;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.Stream.Builder;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.openhab.io.transport.modbus.ModbusBitUtilities;
import org.openhab.io.transport.modbus.ModbusRegisterArray;

/**
 * @author Sami Salonen - Initial contribution
 */
public class BitUtilitiesExtractStringTest {

    private static ModbusRegisterArray shortArrayToRegisterArray(int... arr) {
        return new ModbusRegisterArray(arr);
    }

    public static Collection<Object[]> data() {
        return Collections.unmodifiableList(Stream.of(
                new Object[] { "", shortArrayToRegisterArray(0), 0, 0, StandardCharsets.UTF_8 },
                new Object[] { "hello", shortArrayToRegisterArray(0x6865, 0x6c6c, 0x6f00), 0, 5,
                        StandardCharsets.UTF_8 },
                new Object[] { "he", shortArrayToRegisterArray(0x6865, 0x6c6c, 0x6f00), 0, 2, StandardCharsets.UTF_8 }, // limited
                                                                                                                        // by
                                                                                                                        // count=2
                new Object[] { "hello ", shortArrayToRegisterArray(0, 0, 0x6865, 0x6c6c, 0x6f20, 0, 0), 2, 6,
                        StandardCharsets.UTF_8 },
                new Object[] { "hello", shortArrayToRegisterArray(0x6865, 0x6c6c, 0x6f00, 0x0000, 0x0000), 0, 10,
                        StandardCharsets.UTF_8 },
                new Object[] { "árvíztűrő tükörfúrógép",
                        shortArrayToRegisterArray(0xc3a1, 0x7276, 0xc3ad, 0x7a74, 0xc5b1, 0x72c5, 0x9120, 0x74c3,
                                0xbc6b, 0xc3b6, 0x7266, 0xc3ba, 0x72c3, 0xb367, 0xc3a9, 0x7000),
                        0, 32, StandardCharsets.UTF_8 },
                new Object[] { "你好，世界",
                        shortArrayToRegisterArray(0xe4bd, 0xa0e5, 0xa5bd, 0xefbc, 0x8ce4, 0xb896, 0xe795, 0x8c00), 0,
                        16, StandardCharsets.UTF_8 },
                new Object[] { "árvíztűrő tükörfúrógép",
                        shortArrayToRegisterArray(0xe172, 0x76ed, 0x7a74, 0xfb72, 0xf520, 0x74fc, 0x6bf6, 0x7266,
                                0xfa72, 0xf367, 0xe970),
                        0, 22, Charset.forName("ISO-8859-2") },
                // Example where registers contain 0 byte in between -- only the data preceding zero byte is parsed
                new Object[] { "hello", shortArrayToRegisterArray(0x6865, 0x6c6c, 0x6f00, 0x776f, 0x726c, 0x64), 0, 10,
                        StandardCharsets.UTF_8 },

                // Invalid values
                // 0xe4 = "ä" in extended ascii but not covered by US_ASCII. Will be replaced by �
                new Object[] { "�", shortArrayToRegisterArray(0xe400), 0, 2, StandardCharsets.US_ASCII },
                // out of bounds
                new Object[] { IllegalArgumentException.class, shortArrayToRegisterArray(0, 0), 2, 4,
                        StandardCharsets.UTF_8 },
                // negative index
                new Object[] { IllegalArgumentException.class, shortArrayToRegisterArray(0, 0), 0, -1,
                        StandardCharsets.UTF_8 },
                // out of bounds
                new Object[] { IllegalArgumentException.class, shortArrayToRegisterArray(0, 0), 0, 5,
                        StandardCharsets.UTF_8 })
                .collect(Collectors.toList()));
    }

    public static Stream<Object[]> dataWithByteVariations() {
        return data().stream().flatMap(vals -> {
            Object expected = vals[0];
            ModbusRegisterArray registers = (ModbusRegisterArray) vals[1];
            int index = (int) vals[2];
            int length = (int) vals[3];
            Charset charset = (Charset) vals[4];

            byte[] origBytes = registers.getBytes();
            int origRegisterIndex = index;
            int origByteIndex = origRegisterIndex * 2;

            Builder<Object[]> streamBuilder = Stream.builder();
            for (int offset = 0; offset < 5; offset++) {
                byte[] bytesOffsetted = new byte[origBytes.length + offset];
                System.arraycopy(origBytes, 0, bytesOffsetted, offset, origBytes.length);
                streamBuilder.add(
                        new Object[] { expected, offset, bytesOffsetted, origByteIndex + offset, length, charset });
            }
            Stream<Object[]> variations = streamBuilder.build();
            return variations;
        });
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @ParameterizedTest
    @MethodSource("data")
    public void testExtractStringFromRegisters(Object expectedResult, ModbusRegisterArray registers, int index,
            int length, Charset charset) {
        if (expectedResult instanceof Class && Exception.class.isAssignableFrom((Class) expectedResult)) {
            assertThrows((Class) expectedResult,
                    () -> ModbusBitUtilities.extractStringFromRegisters(registers, index, length, charset));
            return;
        } else {
            String actualState = ModbusBitUtilities.extractStringFromRegisters(registers, index, length, charset);
            assertEquals(actualState, expectedResult,
                    String.format("registers=%s, index=%d, length=%d", registers, index, length));
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @ParameterizedTest
    @MethodSource("dataWithByteVariations")
    public void testExtractStringFromBytes(Object expectedResult, int byteOffset, byte[] bytes, int byteIndex,
            int length, Charset charset) {
        if (expectedResult instanceof Class && Exception.class.isAssignableFrom((Class) expectedResult)) {
            assertThrows((Class) expectedResult,
                    () -> ModbusBitUtilities.extractStringFromBytes(bytes, byteIndex, length, charset));
            return;
        } else {
            String actualState = ModbusBitUtilities.extractStringFromBytes(bytes, byteIndex, length, charset);
            assertEquals(actualState, expectedResult, String.format("registers=%s, index=%d, length=%d, byteIndex=%d",
                    bytes, byteIndex, length, byteIndex));
        }
    }
}
