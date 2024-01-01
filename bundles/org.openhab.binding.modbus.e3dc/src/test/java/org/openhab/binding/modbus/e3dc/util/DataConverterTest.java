/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.modbus.e3dc.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.BitSet;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.modbus.e3dc.internal.dto.DataConverter;
import org.openhab.core.io.transport.modbus.ValueBuffer;

/**
 * The {@link DataConverterTest} Test data conversions
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class DataConverterTest {

    @Test
    public void testRoundPositive() {
        assertEquals(2.3, DataConverter.round(2.34, 1), 0.01);
    }

    @Test
    public void testRoundPositive2() {
        assertEquals(2.4, DataConverter.round(2.37, 1), 0.01);
    }

    @Test
    public void testRoundPositive3() {
        assertEquals(2.4, DataConverter.round(2.35, 1), 0.01);
    }

    @Test
    public void testRoundNegative() {
        assertEquals(-2.3, DataConverter.round(-2.34, 1), 0.01);
    }

    @Test
    public void testRoundNegative2() {
        assertEquals(-2.4, DataConverter.round(-2.37, 1), 0.01);
    }

    @Test
    public void testRoundNegative3() {
        // rounding towards positive infinity. Note difference to testRoundPositive3
        assertEquals(-2.3, DataConverter.round(-2.35, 1), 0.01);
    }

    @Test
    public void testUDoubleValue() {
        assertEquals(0.5, DataConverter.getUDoubleValue(ValueBuffer.wrap(new byte[] { 0, 5 }), 0.1), 0.01);
    }

    @Test
    public void testUDoubleValue2() {
        assertEquals(6159.9,
                DataConverter.getUDoubleValue(ValueBuffer.wrap(new byte[] { (byte) 0xf0, (byte) 0x9f }), 0.1), 0.01);
    }

    @Test
    public void testUDoubleValue3() {
        assertEquals(123198,
                DataConverter.getUDoubleValue(ValueBuffer.wrap(new byte[] { (byte) 0xf0, (byte) 0x9f }), 2), 0.01);
    }

    @Test
    public void testBitsetToInt() {
        byte[] b = new byte[] { 3, 16 };
        BitSet s = BitSet.valueOf(b);
        // Bit0 is the least significant bit to DataConverter.toInt
        assertEquals(true, s.get(0), "Bit0");
        assertEquals(true, s.get(1), "Bit1");
        assertEquals(false, s.get(2), "Bit2");
        assertEquals(false, s.get(3), "Bit3");
        assertEquals(false, s.get(4), "Bit4");
        assertEquals(false, s.get(5), "Bit5");
        assertEquals(false, s.get(6), "Bit6");
        assertEquals(false, s.get(7), "Bit7");
        assertEquals(false, s.get(8), "Bit8");
        assertEquals(false, s.get(9), "Bit9");
        assertEquals(false, s.get(10), "Bit10");
        assertEquals(false, s.get(11), "Bit11");
        assertEquals(true, s.get(12), "Bit12");
        assertEquals(false, s.get(13), "Bit13");
        assertEquals(false, s.get(14), "Bit14");
        assertEquals(false, s.get(15), "Bit15");

        int bitsAsInt = DataConverter.toInt(s);
        int expected = 0b0001000000000011;
        assertEquals(Integer.toBinaryString(expected), Integer.toBinaryString(bitsAsInt));
        assertEquals(expected, bitsAsInt);
    }
}
