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
package org.openhab.binding.modbus.e3dc.util;

import static org.junit.Assert.assertEquals;

import java.util.BitSet;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.Test;
import org.openhab.binding.modbus.e3dc.internal.dto.DataConverter;
import org.openhab.io.transport.modbus.ValueBuffer;

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
        assertEquals("Bit0", true, s.get(0));
        assertEquals("Bit1", true, s.get(1));
        assertEquals("Bit2", false, s.get(2));
        assertEquals("Bit3", false, s.get(3));
        assertEquals("Bit4", false, s.get(4));
        assertEquals("Bit5", false, s.get(5));
        assertEquals("Bit6", false, s.get(6));
        assertEquals("Bit7", false, s.get(7));
        assertEquals("Bit8", false, s.get(8));
        assertEquals("Bit9", false, s.get(9));
        assertEquals("Bit10", false, s.get(10));
        assertEquals("Bit11", false, s.get(11));
        assertEquals("Bit12", true, s.get(12));
        assertEquals("Bit13", false, s.get(13));
        assertEquals("Bit14", false, s.get(14));
        assertEquals("Bit15", false, s.get(15));

        int bitsAsInt = DataConverter.toInt(s);
        int expected = 0b0001000000000011;
        assertEquals(Integer.toBinaryString(expected), Integer.toBinaryString(bitsAsInt));
        assertEquals(expected, bitsAsInt);
    }
}
