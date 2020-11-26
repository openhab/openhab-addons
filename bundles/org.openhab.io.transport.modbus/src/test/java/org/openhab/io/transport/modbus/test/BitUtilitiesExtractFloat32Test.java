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

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.Test;
import org.openhab.io.transport.modbus.ModbusBitUtilities;

/**
 *
 * Tests for 'special' float values such as infinity and NaN. These are not covered in detail in
 * {@link BitUtilitiesExtractIndividualMethodsTest} and
 * {@link BitUtilitiesExtractStateFromRegistersTest}
 *
 * @author Sami Salonen - Initial contribution
 */
public class BitUtilitiesExtractFloat32Test {

    /**
     * Creates a byte array with byteOffset number of zeroes, followed by 32bit of data represented by data
     *
     * @param data actual data payload
     * @param byteOffset number of zeros padded
     * @return byte array of size 4 + byteOffset
     */
    private static byte[] bytes(int data, int byteOffset) {
        ByteBuffer buffer = ByteBuffer.allocate(4 + byteOffset);
        for (int i = 0; i < byteOffset; i++) {
            buffer.put((byte) 0);
        }
        buffer.putInt(data);
        return buffer.array();
    }

    private static void testFloat(float number) {
        int data = Float.floatToIntBits(number);
        for (int byteOffset = 0; byteOffset < 5; byteOffset++) {
            byte[] bytes = bytes(data, byteOffset);
            float actual = ModbusBitUtilities.extractFloat32(bytes, byteOffset);
            float expected = Float.intBitsToFloat(data);
            // Strict comparison of the float values with the exception of NaN
            assertTrue(Float.isNaN(expected) ? Float.isNaN(actual) : expected == actual,
                    String.format("Testing %f (%s) with offset %d, got %f (%s)", expected, Integer.toBinaryString(data),
                            byteOffset, actual, Integer.toBinaryString(Float.floatToRawIntBits(actual))));
        }
    }

    @Test
    public void testExtractFloat32Inf() {
        testFloat(Float.POSITIVE_INFINITY);
    }

    @Test
    public void testExtractFloat32NegInf() {
        testFloat(Float.NEGATIVE_INFINITY);
    }

    @Test
    public void testExtractFloat32NaN() {
        testFloat(Float.NaN);
    }

    @Test
    public void testExtractFloat32Regular() {
        testFloat(1.3f);
    }
}
