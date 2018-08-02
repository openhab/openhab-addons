/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.transport.modbus.test;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.openhab.io.transport.modbus.ModbusBitUtilities;
import org.openhab.io.transport.modbus.ModbusConstants.ValueType;
import org.openhab.io.transport.modbus.ModbusRegister;
import org.openhab.io.transport.modbus.ModbusRegisterArray;
import org.openhab.io.transport.modbus.BasicModbusRegisterArray;
import org.openhab.io.transport.modbus.BasicModbusRegister;

import com.google.common.collect.ImmutableList;

@RunWith(Parameterized.class)
public class BitUtilitiesExtractStateFromRegistersTest {

    final ModbusRegisterArray registers;
    final ValueType type;
    final int index;
    final Object expectedResult;

    @Rule
    public final ExpectedException shouldThrow = ExpectedException.none();

    public BitUtilitiesExtractStateFromRegistersTest(Object expectedResult, ValueType type,
            ModbusRegisterArray registers, int index) {
        this.registers = registers;
        this.index = index;
        this.type = type;
        this.expectedResult = expectedResult; // Exception or DecimalType
    }

    private static ModbusRegisterArray shortArrayToRegisterArray(int... arr) {
        ModbusRegister[] tmp = new ModbusRegister[0];
        return new BasicModbusRegisterArray(IntStream.of(arr).mapToObj(val -> {
            ByteBuffer buffer = ByteBuffer.allocate(2);
            buffer.putShort((short) val);
            return new BasicModbusRegister(buffer.get(0), buffer.get(1));
        }).collect(Collectors.toList()).toArray(tmp));
    }

    @Parameters
    public static Collection<Object[]> data() {
        return ImmutableList.of(
                //
                // BIT
                //
                new Object[] { new DecimalType("1.0"), ValueType.BIT,
                        shortArrayToRegisterArray(1 << 5 | 1 << 4 | 1 << 15), 4 },
                new Object[] { new DecimalType("1.0"), ValueType.BIT,
                        shortArrayToRegisterArray(1 << 5 | 1 << 4 | 1 << 15), 15 },
                new Object[] { new DecimalType("0.0"), ValueType.BIT, shortArrayToRegisterArray(1 << 5), 7 },
                new Object[] { new DecimalType("1.0"), ValueType.BIT, shortArrayToRegisterArray(1 << 5), 5 },
                new Object[] { new DecimalType("0.0"), ValueType.BIT, shortArrayToRegisterArray(1 << 5), 4 },
                new Object[] { new DecimalType("0.0"), ValueType.BIT, shortArrayToRegisterArray(1 << 5), 0 },
                new Object[] { new DecimalType("0.0"), ValueType.BIT, shortArrayToRegisterArray(0, 0), 15 },
                new Object[] { new DecimalType("1.0"), ValueType.BIT, shortArrayToRegisterArray(1 << 5, 1 << 4), 5 },
                new Object[] { new DecimalType("1.0"), ValueType.BIT, shortArrayToRegisterArray(1 << 5, 1 << 4), 20 },
                new Object[] { IllegalArgumentException.class, ValueType.BIT, shortArrayToRegisterArray(1 << 5), 16 },
                new Object[] { IllegalArgumentException.class, ValueType.BIT, shortArrayToRegisterArray(1 << 5), 200 },
                new Object[] { IllegalArgumentException.class, ValueType.BIT, shortArrayToRegisterArray(), 0 },
                new Object[] { IllegalArgumentException.class, ValueType.BIT, shortArrayToRegisterArray(0, 0), 32 },
                //
                // INT8
                //
                new Object[] { new DecimalType("5.0"), ValueType.INT8, shortArrayToRegisterArray(5), 0 },
                new Object[] { new DecimalType("-5.0"), ValueType.INT8, shortArrayToRegisterArray(-5), 0 },
                new Object[] { new DecimalType("3.0"), ValueType.INT8,
                        shortArrayToRegisterArray(((byte) 6 << 8) | (byte) 3), 0 },
                new Object[] { new DecimalType("6.0"), ValueType.INT8,
                        shortArrayToRegisterArray(((byte) 6 << 8) | (byte) 3), 1 },
                new Object[] { new DecimalType("4.0"), ValueType.INT8,
                        shortArrayToRegisterArray(((byte) 6 << 8) | (byte) 3, 4), 2 },
                new Object[] { new DecimalType("6.0"), ValueType.INT8,
                        shortArrayToRegisterArray(55, ((byte) 6 << 8) | (byte) 3), 3 },
                new Object[] { IllegalArgumentException.class, ValueType.INT8, shortArrayToRegisterArray(1), 2 },
                new Object[] { IllegalArgumentException.class, ValueType.INT8, shortArrayToRegisterArray(1, 2), 4 },
                //
                // UINT8
                //
                new Object[] { new DecimalType("5.0"), ValueType.UINT8, shortArrayToRegisterArray(5), 0 },
                new Object[] { new DecimalType("251.0"), ValueType.UINT8, shortArrayToRegisterArray(-5), 0 },
                new Object[] { new DecimalType("3.0"), ValueType.UINT8,
                        shortArrayToRegisterArray(((byte) 6 << 8) | (byte) 3), 0 },
                new Object[] { new DecimalType("6.0"), ValueType.UINT8,
                        shortArrayToRegisterArray(((byte) 6 << 8) | (byte) 3), 1 },
                new Object[] { new DecimalType("4.0"), ValueType.UINT8,
                        shortArrayToRegisterArray(((byte) 6 << 8) | (byte) 3, 4), 2 },
                new Object[] { new DecimalType("6.0"), ValueType.UINT8,
                        shortArrayToRegisterArray(55, ((byte) 6 << 8) | (byte) 3), 3 },
                new Object[] { IllegalArgumentException.class, ValueType.UINT8, shortArrayToRegisterArray(1), 2 },
                new Object[] { IllegalArgumentException.class, ValueType.UINT8, shortArrayToRegisterArray(1, 2), 4 },

                //
                // INT16
                //
                new Object[] { new DecimalType("1.0"), ValueType.INT16, shortArrayToRegisterArray(1), 0 },
                new Object[] { new DecimalType("2.0"), ValueType.INT16, shortArrayToRegisterArray(2), 0 },
                new Object[] { new DecimalType("-1004"), ValueType.INT16, shortArrayToRegisterArray(-1004), 0 },
                new Object[] { new DecimalType("-1536"), ValueType.INT16, shortArrayToRegisterArray(64000), 0 },
                new Object[] { new DecimalType("-1004"), ValueType.INT16, shortArrayToRegisterArray(4, -1004), 1 },
                new Object[] { new DecimalType("-1004"), ValueType.INT16, shortArrayToRegisterArray(-1004, 4), 0 },
                new Object[] { IllegalArgumentException.class, ValueType.INT16, shortArrayToRegisterArray(4, -1004),
                        2 },
                //
                // UINT16
                //
                new Object[] { new DecimalType("1.0"), ValueType.UINT16, shortArrayToRegisterArray(1), 0 },
                new Object[] { new DecimalType("2.0"), ValueType.UINT16, shortArrayToRegisterArray(2), 0 },
                new Object[] { new DecimalType("64532"), ValueType.UINT16, shortArrayToRegisterArray(-1004), 0 },
                new Object[] { new DecimalType("64000"), ValueType.UINT16, shortArrayToRegisterArray(64000), 0 },
                new Object[] { new DecimalType("64532"), ValueType.UINT16, shortArrayToRegisterArray(4, -1004), 1 },
                new Object[] { new DecimalType("64532"), ValueType.UINT16, shortArrayToRegisterArray(-1004, 4), 0 },
                new Object[] { IllegalArgumentException.class, ValueType.INT16, shortArrayToRegisterArray(4, -1004),
                        2 },
                //
                // INT32
                //
                new Object[] { new DecimalType("1.0"), ValueType.INT32, shortArrayToRegisterArray(0, 1), 0 },
                new Object[] { new DecimalType("2.0"), ValueType.INT32, shortArrayToRegisterArray(0, 2), 0 },
                new Object[] { new DecimalType("-1004"), ValueType.INT32,
                        // -1004 = 0xFFFFFC14 (32bit) =
                        shortArrayToRegisterArray(0xFFFF, 0xFC14), 0 },
                new Object[] { new DecimalType("64000"), ValueType.INT32, shortArrayToRegisterArray(0, 64000), 0 },
                new Object[] { new DecimalType("-1004"), ValueType.INT32,
                        // -1004 = 0xFFFFFC14 (32bit) =
                        shortArrayToRegisterArray(0x4, 0xFFFF, 0xFC14), 1 },
                new Object[] { new DecimalType("-1004"), ValueType.INT32,
                        // -1004 = 0xFFFFFC14 (32bit) =
                        shortArrayToRegisterArray(0xFFFF, 0xFC14, 0x4), 0 },
                new Object[] { IllegalArgumentException.class, ValueType.INT32, shortArrayToRegisterArray(4, -1004),
                        1 },
                new Object[] { IllegalArgumentException.class, ValueType.INT32, shortArrayToRegisterArray(4, -1004),
                        2 },
                new Object[] { IllegalArgumentException.class, ValueType.INT32, shortArrayToRegisterArray(0, 0, 0), 2 },
                //
                // UINT32
                //
                new Object[] { new DecimalType("1.0"), ValueType.UINT32, shortArrayToRegisterArray(0, 1), 0 },
                new Object[] { new DecimalType("2.0"), ValueType.UINT32, shortArrayToRegisterArray(0, 2), 0 },
                new Object[] { new DecimalType("4294966292"), ValueType.UINT32,
                        // 4294966292 = 0xFFFFFC14 (32bit) =
                        shortArrayToRegisterArray(0xFFFF, 0xFC14), 0 },
                new Object[] { new DecimalType("64000"), ValueType.UINT32, shortArrayToRegisterArray(0, 64000), 0 },
                new Object[] {
                        // out of bounds of unsigned 16bit (0 to 65,535)
                        new DecimalType("70004"),
                        // 70004 -> 0x00011174 (32bit) -> 0x1174 (16bit)
                        ValueType.UINT32, shortArrayToRegisterArray(1, 4468), 0 },
                new Object[] { new DecimalType("4294966292"), ValueType.UINT32,
                        // 4294966292 = 0xFFFFFC14 (32bit) =
                        shortArrayToRegisterArray(0xFFFF, 0xFC14, 0x5), 0 },
                new Object[] { new DecimalType("4294966292"), ValueType.UINT32,
                        // 4294966292 = 0xFFFFFC14 (32bit) =
                        shortArrayToRegisterArray(0x5, 0xFFFF, 0xFC14), 1 },
                new Object[] { IllegalArgumentException.class, ValueType.UINT32, shortArrayToRegisterArray(4, -1004),
                        1 },
                new Object[] { IllegalArgumentException.class, ValueType.UINT32, shortArrayToRegisterArray(4, -1004),
                        2 },
                new Object[] { IllegalArgumentException.class, ValueType.UINT32, shortArrayToRegisterArray(0, 0, 0),
                        2 },
                //
                // INT32_SWAP
                //
                new Object[] { new DecimalType("1.0"), ValueType.INT32_SWAP, shortArrayToRegisterArray(1, 0), 0 },
                new Object[] { new DecimalType("2.0"), ValueType.INT32_SWAP, shortArrayToRegisterArray(2, 0), 0 },
                new Object[] { new DecimalType("-1004"), ValueType.INT32_SWAP,
                        // -1004 = 0xFFFFFC14 (32bit) =
                        shortArrayToRegisterArray(0xFC14, 0xFFFF), 0 },
                new Object[] { new DecimalType("64000"), ValueType.INT32_SWAP, shortArrayToRegisterArray(64000, 0), 0 },
                new Object[] { new DecimalType("-1004"), ValueType.INT32_SWAP,
                        // -1004 = 0xFFFFFC14 (32bit) =
                        shortArrayToRegisterArray(0x4, 0xFC14, 0xFFFF), 1 },
                new Object[] { new DecimalType("-1004"), ValueType.INT32_SWAP,
                        // -1004 = 0xFFFFFC14 (32bit) =
                        shortArrayToRegisterArray(0xFC14, 0xFFFF, 0x4), 0 },
                new Object[] { IllegalArgumentException.class, ValueType.INT32_SWAP,
                        shortArrayToRegisterArray(4, -1004), 1 },
                new Object[] { IllegalArgumentException.class, ValueType.INT32_SWAP,
                        shortArrayToRegisterArray(4, -1004), 2 },
                new Object[] { IllegalArgumentException.class, ValueType.INT32_SWAP, shortArrayToRegisterArray(0, 0, 0),
                        2 },
                //
                // UINT32_SWAP
                //
                new Object[] { new DecimalType("1.0"), ValueType.UINT32_SWAP, shortArrayToRegisterArray(1, 0), 0 },
                new Object[] { new DecimalType("2.0"), ValueType.UINT32_SWAP, shortArrayToRegisterArray(2, 0), 0 },
                new Object[] { new DecimalType("4294966292"), ValueType.UINT32_SWAP,
                        // 4294966292 = 0xFFFFFC14 (32bit) =
                        shortArrayToRegisterArray(0xFC14, 0xFFFF), 0 },
                new Object[] { new DecimalType("64000"), ValueType.UINT32_SWAP, shortArrayToRegisterArray(64000, 0),
                        0 },
                new Object[] {
                        // out of bounds of unsigned 16bit (0 to 65,535)
                        new DecimalType("70004"),
                        // 70004 -> 0x00011174 (32bit) -> 0x1174 (16bit)
                        ValueType.UINT32_SWAP, shortArrayToRegisterArray(4468, 1), 0 },
                new Object[] { new DecimalType("4294966292"), ValueType.UINT32_SWAP,
                        // 4294966292 = 0xFFFFFC14 (32bit) =
                        shortArrayToRegisterArray(0xFC14, 0xFFFF, 0x5), 0 },
                new Object[] { new DecimalType("4294966292"), ValueType.UINT32_SWAP,
                        // 4294966292 = 0xFFFFFC14 (32bit) =
                        shortArrayToRegisterArray(0x5, 0xFC14, 0xFFFF), 1 },
                new Object[] { IllegalArgumentException.class, ValueType.UINT32_SWAP,
                        shortArrayToRegisterArray(4, -1004), 1 },
                new Object[] { IllegalArgumentException.class, ValueType.UINT32_SWAP,
                        shortArrayToRegisterArray(4, -1004), 2 },
                new Object[] { IllegalArgumentException.class, ValueType.UINT32_SWAP,
                        shortArrayToRegisterArray(0, 0, 0), 2 },
                //
                // FLOAT32
                //
                new Object[] { new DecimalType("1.0"), ValueType.FLOAT32, shortArrayToRegisterArray(0x3F80, 0x0000),
                        0 },
                new Object[] { new DecimalType(1.6f), ValueType.FLOAT32, shortArrayToRegisterArray(0x3FCC, 0xCCCD), 0 },
                new Object[] { new DecimalType(2.6f), ValueType.FLOAT32, shortArrayToRegisterArray(0x4026, 0x6666), 0 },
                new Object[] { new DecimalType(-1004.4f), ValueType.FLOAT32, shortArrayToRegisterArray(0xC47B, 0x199A),
                        0 },
                new Object[] { new DecimalType("64000"), ValueType.FLOAT32, shortArrayToRegisterArray(0x477A, 0x0000),
                        0 },
                new Object[] {
                        // out of bounds of unsigned 16bit (0 to 65,535)
                        new DecimalType(70004.4f), ValueType.FLOAT32, shortArrayToRegisterArray(0x4788, 0xBA33), 0 },
                new Object[] {
                        // out of bounds of unsigned 32bit (0 to 4,294,967,295)
                        new DecimalType("5000000000"), ValueType.FLOAT32, shortArrayToRegisterArray(0x4F95, 0x02F9),
                        0 },
                new Object[] { new DecimalType(-1004.4f), ValueType.FLOAT32,
                        shortArrayToRegisterArray(0x4, 0xC47B, 0x199A), 1 },
                new Object[] { new DecimalType(-1004.4f), ValueType.FLOAT32,
                        shortArrayToRegisterArray(0xC47B, 0x199A, 0x4), 0 },
                new Object[] { new DecimalType(-1004.4f), ValueType.FLOAT32,
                        shortArrayToRegisterArray(0x4, 0x0, 0x0, 0x0, 0xC47B, 0x199A), 4 },
                new Object[] { IllegalArgumentException.class, ValueType.FLOAT32, shortArrayToRegisterArray(4, -1004),
                        1 },
                new Object[] { IllegalArgumentException.class, ValueType.FLOAT32, shortArrayToRegisterArray(4, -1004),
                        2 },
                new Object[] { IllegalArgumentException.class, ValueType.FLOAT32, shortArrayToRegisterArray(0, 0, 0),
                        2 },
                //
                // FLOAT32_SWAP
                //
                new Object[] { new DecimalType("1.0"), ValueType.FLOAT32_SWAP,
                        shortArrayToRegisterArray(0x0000, 0x3F80), 0 },
                new Object[] { new DecimalType(1.6f), ValueType.FLOAT32_SWAP, shortArrayToRegisterArray(0xCCCD, 0x3FCC),
                        0 },
                new Object[] { new DecimalType(2.6f), ValueType.FLOAT32_SWAP, shortArrayToRegisterArray(0x6666, 0x4026),
                        0 },
                new Object[] { new DecimalType(-1004.4f), ValueType.FLOAT32_SWAP,
                        shortArrayToRegisterArray(0x199A, 0xC47B), 0 },
                new Object[] { new DecimalType("64000"), ValueType.FLOAT32_SWAP,
                        shortArrayToRegisterArray(0x0000, 0x477A), 0 },
                new Object[] {
                        // out of bounds of unsigned 16bit (0 to 65,535)
                        new DecimalType(70004.4f), ValueType.FLOAT32_SWAP, shortArrayToRegisterArray(0xBA33, 0x4788),
                        0 },
                new Object[] {
                        // out of bounds of unsigned 32bit (0 to 4,294,967,295)
                        new DecimalType("5000000000"), ValueType.FLOAT32_SWAP,
                        shortArrayToRegisterArray(0x02F9, 0x4F95), 0 },
                new Object[] { new DecimalType(-1004.4f), ValueType.FLOAT32_SWAP,
                        shortArrayToRegisterArray(0x4, 0x199A, 0xC47B), 1 },
                new Object[] { new DecimalType(-1004.4f), ValueType.FLOAT32_SWAP,
                        shortArrayToRegisterArray(0x199A, 0xC47B, 0x4), 0 },
                new Object[] { IllegalArgumentException.class, ValueType.FLOAT32_SWAP,
                        shortArrayToRegisterArray(4, -1004), 1 },
                new Object[] { IllegalArgumentException.class, ValueType.FLOAT32_SWAP,
                        shortArrayToRegisterArray(4, -1004), 2 },
                new Object[] { IllegalArgumentException.class, ValueType.FLOAT32_SWAP,
                        shortArrayToRegisterArray(0, 0, 0), 2 });

    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testCommandToRegisters() {
        if (expectedResult instanceof Class && Exception.class.isAssignableFrom((Class) expectedResult)) {
            shouldThrow.expect((Class) expectedResult);
        }

        DecimalType actualState = ModbusBitUtilities.extractStateFromRegisters(this.registers, this.index, this.type);
        assertThat(String.format("registers=%s, index=%d, type=%s", registers, index, type), actualState,
                is(equalTo(expectedResult)));
    }
}
