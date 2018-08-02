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
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.eclipse.smarthome.core.library.types.StringType;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.openhab.io.transport.modbus.ModbusBitUtilities;
import org.openhab.io.transport.modbus.ModbusRegister;
import org.openhab.io.transport.modbus.ModbusRegisterArray;
import org.openhab.io.transport.modbus.BasicModbusRegisterArray;
import org.openhab.io.transport.modbus.BasicModbusRegister;

import com.google.common.collect.ImmutableList;

@RunWith(Parameterized.class)
public class BitUtilitiesExtractStringFromRegistersTest {

    final ModbusRegisterArray registers;
    final int index;
    final int length;
    final Object expectedResult;
    final Charset charset;

    @Rule
    public final ExpectedException shouldThrow = ExpectedException.none();

    public BitUtilitiesExtractStringFromRegistersTest(Object expectedResult, ModbusRegisterArray registers, int index,
            int length, Charset charset) {
        this.registers = registers;
        this.index = index;
        this.length = length;
        this.charset = charset;
        this.expectedResult = expectedResult;
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
                new Object[] { new StringType(""), shortArrayToRegisterArray(0), 0, 0, Charset.forName("UTF-8") },
                new Object[] { new StringType("hello"), shortArrayToRegisterArray(0x6865, 0x6c6c, 0x6f00), 0, 5,
                        Charset.forName("UTF-8") },
                new Object[] { new StringType("hello "), shortArrayToRegisterArray(0, 0, 0x6865, 0x6c6c, 0x6f20, 0, 0),
                        2, 6, Charset.forName("UTF-8") },
                new Object[] { new StringType("hello"),
                        shortArrayToRegisterArray(0x6865, 0x6c6c, 0x6f00, 0x0000, 0x0000), 0, 10,
                        Charset.forName("UTF-8") },
                new Object[] { new StringType("árvíztűrő tükörfúrógép"),
                        shortArrayToRegisterArray(0xc3a1, 0x7276, 0xc3ad, 0x7a74, 0xc5b1, 0x72c5, 0x9120, 0x74c3,
                                0xbc6b, 0xc3b6, 0x7266, 0xc3ba, 0x72c3, 0xb367, 0xc3a9, 0x7000),
                        0, 32, Charset.forName("UTF-8") },
                new Object[] { new StringType("árvíztűrő tükörfúrógép"),
                        shortArrayToRegisterArray(0xe172, 0x76ed, 0x7a74, 0xfb72, 0xf520, 0x74fc, 0x6bf6, 0x7266,
                                0xfa72, 0xf367, 0xe970),
                        0, 22, Charset.forName("ISO-8859-2") },

                // Invalid values
                new Object[] { IllegalArgumentException.class, shortArrayToRegisterArray(0, 0), 2, 4,
                        Charset.forName("UTF-8") },
                new Object[] { IllegalArgumentException.class, shortArrayToRegisterArray(0, 0), 0, -1,
                        Charset.forName("UTF-8") },
                new Object[] { IllegalArgumentException.class, shortArrayToRegisterArray(0, 0), 0, 5,
                        Charset.forName("UTF-8") });
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testExtractStringFromRegisters() {
        if (expectedResult instanceof Class && Exception.class.isAssignableFrom((Class) expectedResult)) {
            shouldThrow.expect((Class) expectedResult);
        }

        StringType actualState = ModbusBitUtilities.extractStringFromRegisters(this.registers, this.index, this.length,
                this.charset);
        assertThat(String.format("registers=%s, index=%d, length=%d", registers, index, length), actualState,
                is(equalTo(expectedResult)));
    }

}
