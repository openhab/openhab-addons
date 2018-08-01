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

import java.util.Collection;

import org.apache.commons.lang.NotImplementedException;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.types.Command;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.openhab.io.transport.modbus.ModbusBitUtilities;
import org.openhab.io.transport.modbus.ModbusConstants.ValueType;
import org.openhab.io.transport.modbus.ModbusRegisterArray;

import com.google.common.collect.ImmutableList;

@RunWith(Parameterized.class)
public class BitUtilitiesCommandToRegistersTest {

    private final Command command;
    private final ValueType type;
    private final Object expectedResult;

    @Rule
    public final ExpectedException shouldThrow = ExpectedException.none();

    public BitUtilitiesCommandToRegistersTest(Command command, ValueType type, Object expectedResult) {
        this.command = command;
        this.type = type;
        this.expectedResult = expectedResult; // Exception or array of 16bit integers
    }

    private static short[] shorts(int... ints) {
        short[] shorts = new short[ints.length];
        for (int i = 0; i < ints.length; i++) {
            short s = (short) ints[i];
            shorts[i] = s;
        }
        return shorts;
    }

    @Parameters
    public static Collection<Object[]> data() {
        return ImmutableList
                .of(new Object[] { new DecimalType("1.0"), ValueType.BIT, IllegalArgumentException.class },
                        new Object[] { new DecimalType("1.0"), ValueType.INT8, IllegalArgumentException.class },
                        //
                        // INT16
                        //
                        new Object[] { new DecimalType("1.0"), ValueType.INT16, shorts(1) },
                        new Object[] { new DecimalType("1.6"), ValueType.INT16, shorts(1) },
                        new Object[] { new DecimalType("2.6"), ValueType.INT16, shorts(2) },
                        new Object[] { new DecimalType("-1004.4"), ValueType.INT16, shorts(-1004), },
                        new Object[] { new DecimalType("64000"), ValueType.INT16, shorts(64000), }, new Object[] {
                                // out of bounds of unsigned 16bit (0 to 65,535)
                                new DecimalType("70004.4"),
                                // 70004 -> 0x00011174 (int) -> 0x1174 (short) = 4468
                                ValueType.INT16, shorts(4468), },
                        //
                        // UINT16 (same as INT16)
                        //
                        new Object[] { new DecimalType("1.0"), ValueType.UINT16, shorts(1) },
                        new Object[] { new DecimalType("1.6"), ValueType.UINT16, shorts(1) },
                        new Object[] { new DecimalType("2.6"), ValueType.UINT16, shorts(2) },
                        new Object[] { new DecimalType("-1004.4"), ValueType.UINT16, shorts(-1004), },
                        new Object[] { new DecimalType("64000"), ValueType.UINT16, shorts(64000), }, new Object[] {
                                // out of bounds of unsigned 16bit (0 to 65,535)
                                new DecimalType("70004.4"),
                                // 70004 -> 0x00011174 (32bit) -> 0x1174 (16bit)
                                ValueType.UINT16, shorts(0x1174), },
                        //
                        // INT32
                        //
                        new Object[] { new DecimalType("1.0"), ValueType.INT32, shorts(0, 1) },
                        new Object[] { new DecimalType("1.6"), ValueType.INT32, shorts(0, 1) },
                        new Object[] { new DecimalType("2.6"), ValueType.INT32, shorts(0, 2) },
                        new Object[] { new DecimalType("-1004.4"), ValueType.INT32,
                                // -1004 = 0xFFFFFC14 (32bit) =
                                shorts(0xFFFF, 0xFC14), },
                        new Object[] { new DecimalType("64000"), ValueType.INT32, shorts(0, 64000), }, new Object[] {
                                // out of bounds of unsigned 16bit (0 to 65,535)
                                new DecimalType("70004.4"),
                                // 70004 -> 0x00011174 (32bit) -> 0x1174 (16bit)
                                ValueType.INT32, shorts(1, 4468), },
                        new Object[] {
                                // out of bounds of unsigned 32bit (0 to 4,294,967,295)
                                new DecimalType("5000000000"),
                                // 5000000000 -> 0x12a05f200 () -> 0x1174 (16bit)
                                ValueType.INT32, shorts(0x2a05, 0xf200), },
                        //
                        // UINT32 (same as INT32)
                        //
                        new Object[] { new DecimalType("1.0"), ValueType.UINT32, shorts(0, 1) },
                        new Object[] { new DecimalType("1.6"), ValueType.UINT32, shorts(0, 1) },
                        new Object[] { new DecimalType("2.6"), ValueType.UINT32, shorts(0, 2) },
                        new Object[] { new DecimalType("-1004.4"), ValueType.UINT32,
                                // -1004 = 0xFFFFFC14 (32bit) =
                                shorts(0xFFFF, 0xFC14), },
                        new Object[] { new DecimalType("64000"), ValueType.UINT32, shorts(0, 64000), }, new Object[] {
                                // out of bounds of unsigned 16bit (0 to 65,535)
                                new DecimalType("70004.4"),
                                // 70004 -> 0x00011174 (32bit) -> 0x1174 (16bit)
                                ValueType.UINT32, shorts(1, 4468), },
                        new Object[] {
                                // out of bounds of unsigned 32bit (0 to 4,294,967,295)
                                new DecimalType("5000000000"),
                                // 5000000000 -> 0x12a05f200 () -> 0x1174 (16bit)
                                ValueType.UINT32, shorts(0x2a05, 0xf200), },
                        //
                        // INT32_SWAP
                        //
                        new Object[] { new DecimalType("1.0"), ValueType.INT32_SWAP, shorts(1, 0) },
                        new Object[] { new DecimalType("1.6"), ValueType.INT32_SWAP, shorts(1, 0) },
                        new Object[] { new DecimalType("2.6"), ValueType.INT32_SWAP, shorts(2, 0) },
                        new Object[] { new DecimalType("-1004.4"), ValueType.INT32_SWAP,
                                // -1004 = 0xFFFFFC14 (32bit)
                                shorts(0xFC14, 0xFFFF), },
                        new Object[] { new DecimalType("64000"), ValueType.INT32_SWAP, shorts(64000, 0), },
                        new Object[] {
                                // out of bounds of unsigned 16bit (0 to 65,535)
                                new DecimalType("70004.4"),
                                // 70004 -> 0x00011174 (32bit)
                                ValueType.INT32_SWAP, shorts(4468, 1), },
                        new Object[] {
                                // out of bounds of unsigned 32bit (0 to 4,294,967,295)
                                new DecimalType("5000000000"),
                                // 5000000000 -> 0x12a05f200
                                ValueType.INT32_SWAP, shorts(0xf200, 0x2a05), },
                        //
                        // UINT32_SWAP (same as INT32_SWAP)
                        //
                        new Object[] { new DecimalType("1.0"), ValueType.UINT32_SWAP, shorts(1, 0) },
                        new Object[] { new DecimalType("1.6"), ValueType.UINT32_SWAP, shorts(1, 0) },
                        new Object[] { new DecimalType("2.6"), ValueType.UINT32_SWAP, shorts(2, 0) },
                        new Object[] { new DecimalType("-1004.4"), ValueType.UINT32_SWAP,
                                // -1004 = 0xFFFFFC14 (32bit)
                                shorts(0xFC14, 0xFFFF), },
                        new Object[] { new DecimalType("64000"), ValueType.UINT32_SWAP, shorts(64000, 0), },
                        new Object[] {
                                // out of bounds of unsigned 16bit (0 to 65,535)
                                new DecimalType("70004.4"),
                                // 70004 -> 0x00011174 (32bit)
                                ValueType.UINT32_SWAP, shorts(4468, 1), },
                        new Object[] {
                                // out of bounds of unsigned 32bit (0 to 4,294,967,295)
                                new DecimalType("5000000000"),
                                // 5000000000 -> 0x12a05f200
                                ValueType.UINT32_SWAP, shorts(0xf200, 0x2a05), },
                        //
                        // FLOAT32
                        //
                        new Object[] { new DecimalType("1.0"), ValueType.FLOAT32, shorts(0x3F80, 0x0000) },
                        new Object[] { new DecimalType("1.6"), ValueType.FLOAT32, shorts(0x3FCC, 0xCCCD) },
                        new Object[] { new DecimalType("2.6"), ValueType.FLOAT32, shorts(0x4026, 0x6666) },
                        new Object[] { new DecimalType("-1004.4"), ValueType.FLOAT32, shorts(0xC47B, 0x199A), },
                        new Object[] { new DecimalType("64000"), ValueType.FLOAT32, shorts(0x477A, 0x0000), },
                        new Object[] {
                                // out of bounds of unsigned 16bit (0 to 65,535)
                                new DecimalType("70004.4"), ValueType.FLOAT32, shorts(0x4788, 0xBA33), },
                        new Object[] {
                                // out of bounds of unsigned 32bit (0 to 4,294,967,295)
                                new DecimalType("5000000000"), ValueType.FLOAT32, shorts(0x4F95, 0x02F9), },
                        //
                        // FLOAT32_SWAP
                        //
                        new Object[] { new DecimalType("1.0"), ValueType.FLOAT32_SWAP, shorts(0x0000, 0x3F80) },
                        new Object[] { new DecimalType("1.6"), ValueType.FLOAT32_SWAP, shorts(0xCCCD, 0x3FCC) },
                        new Object[] { new DecimalType("2.6"), ValueType.FLOAT32_SWAP, shorts(0x6666, 0x4026) },
                        new Object[] { new DecimalType("-1004.4"), ValueType.FLOAT32_SWAP, shorts(0x199A, 0xC47B), },
                        new Object[] { new DecimalType("64000"), ValueType.FLOAT32_SWAP, shorts(0x0000, 0x477A), },
                        new Object[] {
                                // out of bounds of unsigned 16bit (0 to 65,535)
                                new DecimalType("70004.4"), ValueType.FLOAT32_SWAP, shorts(0xBA33, 0x4788), },
                        new Object[] {
                                // out of bounds of unsigned 32bit (0 to 4,294,967,295)
                                new DecimalType("5000000000"), ValueType.FLOAT32_SWAP, shorts(0x02F9, 0x4F95) },
                        // ON/OFF
                        new Object[] { OnOffType.ON, ValueType.FLOAT32_SWAP, shorts(0x0000, 0x3F80) },
                        new Object[] { OnOffType.OFF, ValueType.FLOAT32_SWAP, shorts(0x0000, 0x0000) },
                        // OPEN
                        new Object[] { OpenClosedType.OPEN, ValueType.FLOAT32_SWAP, shorts(0x0000, 0x3F80) },
                        new Object[] { OpenClosedType.OPEN, ValueType.INT16, shorts(1) },
                        // CLOSED
                        new Object[] { OpenClosedType.CLOSED, ValueType.FLOAT32_SWAP, shorts(0x0000, 0x0000) },
                        new Object[] { OpenClosedType.CLOSED, ValueType.INT16, shorts(0x0000) },
                        // Unsupported command
                        new Object[] { IncreaseDecreaseType.INCREASE, ValueType.FLOAT32_SWAP,
                                NotImplementedException.class });

    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testCommandToRegisters() {
        if (expectedResult instanceof Class && Exception.class.isAssignableFrom((Class) expectedResult)) {
            shouldThrow.expect((Class) expectedResult);
        }

        ModbusRegisterArray registers = ModbusBitUtilities.commandToRegisters(this.command, this.type);
        short[] expectedRegisters = (short[]) expectedResult;

        assertThat(registers.size(), is(equalTo(expectedRegisters.length)));
        for (int i = 0; i < expectedRegisters.length; i++) {
            int expectedRegisterDataUnsigned = expectedRegisters[i] & 0xffff;
            int actual = registers.getRegister(i).getValue();

            assertThat(String.format("register index i=%d, command=%s, type=%s", i, command, type), actual,
                    is(equalTo(expectedRegisterDataUnsigned)));
        }
    }
}
