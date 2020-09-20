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

import java.nio.ByteBuffer;
import java.util.BitSet;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.Test;
import org.openhab.binding.modbus.e3dc.internal.dto.DataConverter;

/**
 * The {@link DataConverterTest} Test data conversions
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class DataConverterTest {

    @Test
    public void testE3DCSwapValueNegative() {
        // Reg 69 value 65098 bytes [-2, 74]
        // Reg 70 value 65535 bytes [-1, -1]
        byte[] b = new byte[] { -2, -74, -1, -1 };
        assertEquals("Negative Value", -330, DataConverter.getInt32Swap(ByteBuffer.wrap(b)));
    }

    @Test
    public void testBitset() {
        byte[] b = new byte[] { 3, 16 };
        BitSet s = BitSet.valueOf(b);
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
    }
}
