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

import org.junit.jupiter.api.Test;
import org.openhab.io.transport.modbus.ModbusBitUtilities;

/**
 *
 * Tests for extractBit
 *
 * @author Sami Salonen - Initial contribution
 */
public class BitUtilitiesExtractBitTest {

    @Test
    public void testExtractBitWithRegisterIndexAndBitIndex() {
        byte[] bytes = new byte[] { 0b00100001, // hi byte of 1st register
                0b00100101, // lo byte of 1st register
                0b00110001, // hi byte of 2nd register
                0b00101001 }; // lo byte of 2nd register

        {
            int registerIndex = 0;
            int[] expectedBitsFromLSBtoMSB = new int[] { //
                    1, 0, 1, 0, 0, 1, 0, 0, // lo byte, with increasing significance
                    1, 0, 0, 0, 0, 1, 0, 0 // hi byte, with increasing significance
            };
            for (int bitIndex = 0; bitIndex < expectedBitsFromLSBtoMSB.length; bitIndex++) {
                assertEquals(expectedBitsFromLSBtoMSB[bitIndex],
                        ModbusBitUtilities.extractBit(bytes, registerIndex, bitIndex),
                        String.format("bitIndex=%d", bitIndex));
            }
        }
        {
            int registerIndex = 1;
            int[] expectedBitsFromLSBtoMSB = new int[] { //
                    1, 0, 0, 1, 0, 1, 0, 0, // lo byte, with increasing significance
                    1, 0, 0, 0, 1, 1, 0, 0 // hi byte, with increasing significance
            };
            for (int bitIndex = 0; bitIndex < expectedBitsFromLSBtoMSB.length; bitIndex++) {
                assertEquals(expectedBitsFromLSBtoMSB[bitIndex],
                        ModbusBitUtilities.extractBit(bytes, registerIndex, bitIndex),
                        String.format("bitIndex=%d", bitIndex));
            }
        }
    }

    @Test
    public void testExtractBitWithRegisterIndexAndBitIndexOOB() {

        byte[] bytes = new byte[] { 0b00100001, // hi byte of 1st register
                0b00100101, // lo byte of 1st register
                0b00110001, // hi byte of 2nd register
                0b00101001 }; // lo byte of 2nd register
        assertThrows(IllegalArgumentException.class, () -> ModbusBitUtilities.extractBit(bytes, 3, 0));
    }

    @Test
    public void testExtractBitWithRegisterIndexAndBitIndexOOB2() {
        byte[] bytes = new byte[] { 0b00100001, // hi byte of 1st register
                0b00100101, // lo byte of 1st register
                0b00110001, // hi byte of 2nd register
                0b00101001 }; // lo byte of 2nd register
        assertThrows(IllegalArgumentException.class, () -> ModbusBitUtilities.extractBit(bytes, 0, 17));
    }

    @Test
    public void testExtractBitWithRegisterIndexAndBitIndexOOB3() {
        byte[] bytes = new byte[] { 0b00100001, // hi byte of 1st register
                0b00100101, // lo byte of 1st register
                0b00110001, // hi byte of 2nd register
                0b00101001 }; // lo byte of 2nd register
        assertThrows(IllegalArgumentException.class, () -> ModbusBitUtilities.extractBit(bytes, 0, -1));
    }

    @Test
    public void testExtractBitWithRegisterIndexAndBitIndexOOB4() {
        byte[] bytes = new byte[] { 0b00100001, // hi byte of 1st register
                0b00100101, // lo byte of 1st register
                0b00110001, // hi byte of 2nd register
                0b00101001 }; // lo byte of 2nd register
        assertThrows(IllegalArgumentException.class, () -> ModbusBitUtilities.extractBit(bytes, -1, 0));
    }

    @Test
    public void testExtractBitWithSingleIndex() {
        byte[] bytes = new byte[] { 0b00100001, // hi byte of 1st register
                0b00100101, // lo byte of 1st register
                0b00110001, // hi byte of 2nd register
                0b00101001 }; // lo byte of 2nd register
        int[] expectedBits = new int[] { //
                1, 0, 1, 0, 0, 1, 0, 0, // 1st register: lo byte, with increasing significance
                1, 0, 0, 0, 0, 1, 0, 0, // 1st register: hi byte, with increasing significance
                1, 0, 0, 1, 0, 1, 0, 0, // 2nd register: lo byte, with increasing significance
                1, 0, 0, 0, 1, 1, 0, 0 // 2nd register: hi byte, with increasing significance
        };
        for (int bitIndex = 0; bitIndex < expectedBits.length; bitIndex++) {
            assertEquals(expectedBits[bitIndex], ModbusBitUtilities.extractBit(bytes, bitIndex),
                    String.format("bitIndex=%d", bitIndex));
            assertEquals(expectedBits[bitIndex], ModbusBitUtilities.extractBit(bytes, bitIndex),
                    String.format("bitIndex=%d", bitIndex));
        }
    }

    @Test
    public void testExtractBitWithSingleIndexOOB() {
        byte[] bytes = new byte[] { 0b00100001, // hi byte of 1st register
                0b00100101, // lo byte of 1st register
                0b00110001, // hi byte of 2nd register
                0b00101001 }; // lo byte of 2nd register
        assertThrows(IllegalArgumentException.class, () -> ModbusBitUtilities.extractBit(bytes, 32));
    }

    @Test
    public void testExtractBitWithSingleIndexOOB2() {
        byte[] bytes = new byte[] { 0b00100001, // hi byte of 1st register
                0b00100101, // lo byte of 1st register
                0b00110001, // hi byte of 2nd register
                0b00101001 }; // lo byte of 2nd register
        assertThrows(IllegalArgumentException.class, () -> ModbusBitUtilities.extractBit(bytes, -1));
    }
}
