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
package org.openhab.binding.e3dc;

import static org.junit.Assert.assertEquals;

import java.util.BitSet;

import org.junit.Test;
import org.openhab.binding.e3dc.internal.dto.DataConverter;

/**
 * The {@link DataConverterTest} Test data conversions
 *
 * @author Bernd Weymann - Initial contribution
 */
public class DataConverterTest {

    @Test
    public void testE3DCSwapValues() {
        // Reg 69 value 65098 bytes [-2, 74]
        // Reg 70 value 65535 bytes [-1, -1]
        byte[] b1 = new byte[] { -2, -128 };
        byte[] b2 = new byte[] { -1, -1 };
        System.out.println("b1: " + DataConverter.getIntValue(b1, 0));
        System.out.println("b2: " + DataConverter.getIntValue(b2, 0));
        b1 = new byte[] { -2, -128, -1, -1 };
        System.out.println(b1.toString());
        System.out.println("b2: " + DataConverter.getInt32_swap(b1, 0));
    }

    @Test
    public void testInt() {
        byte[] b = new byte[] { 0, -128 };
        int i = DataConverter.getIntValue(b, 0);
        System.out.println(i);
        assertEquals("Int value", 128, i);
    }

    @Test
    public void testLong() {
        byte[] b = new byte[] { 0, 1, 0, 0 };
        long l = DataConverter.getLongValue(b, 0);
        System.out.println(l);
        assertEquals("Long value", 65536, l);
        l = DataConverter.getLongValue(b, 0);
        System.out.println(l);
    }

    @Test
    public void testBitset() {
        byte[] b = new byte[] { 3, 16 };
        BitSet s = BitSet.valueOf(b);
        for (int i = 0; i < 16; i++) {
            System.out.println("b" + i + ": " + s.get(i));

        }

    }
}
