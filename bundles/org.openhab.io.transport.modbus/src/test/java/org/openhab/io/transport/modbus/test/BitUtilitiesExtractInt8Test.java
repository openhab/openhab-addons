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
 * Tests for extractSInt8 and extractUInt8
 *
 * @author Sami Salonen - Initial contribution
 */
public class BitUtilitiesExtractInt8Test {

    @Test
    public void extractSInt8WithSingleIndex() {
        byte[] bytes = new byte[] { -1, 2, 3 };
        assertEquals(-1, ModbusBitUtilities.extractSInt8(bytes, 0));
        assertEquals(2, ModbusBitUtilities.extractSInt8(bytes, 1));
        assertEquals(3, ModbusBitUtilities.extractSInt8(bytes, 2));
    }

    @Test
    public void extractSInt8WithSingleIndexOOB() {
        byte[] bytes = new byte[] { -1, 2, 3 };
        assertThrows(IllegalArgumentException.class, () -> ModbusBitUtilities.extractSInt8(bytes, 3));
    }

    @Test
    public void extractSInt8WithSingleIndexOOB2() {
        byte[] bytes = new byte[] { -1, 2, 3 };
        assertThrows(IllegalArgumentException.class, () -> ModbusBitUtilities.extractSInt8(bytes, -1));
    }

    @Test
    public void extractSInt8WithRegisterIndexAndHiByte() {
        byte[] bytes = new byte[] { -1, 2, 3, 4 };
        assertEquals(-1, ModbusBitUtilities.extractSInt8(bytes, 0, true));
        assertEquals(2, ModbusBitUtilities.extractSInt8(bytes, 0, false));
        assertEquals(3, ModbusBitUtilities.extractSInt8(bytes, 1, true));
        assertEquals(4, ModbusBitUtilities.extractSInt8(bytes, 1, false));
    }

    @Test
    public void extractSInt8WithRegisterIndexAndHiByteOOB() {
        byte[] bytes = new byte[] { -1, 2, 3, 4 };
        assertThrows(IllegalArgumentException.class, () -> ModbusBitUtilities.extractSInt8(bytes, 2, true));
    }

    @Test
    public void extractSInt8WithRegisterIndexAndHiByteOOB2() {
        byte[] bytes = new byte[] { -1, 2, 3, 4 };
        assertThrows(IllegalArgumentException.class, () -> ModbusBitUtilities.extractSInt8(bytes, -1, true));
    }

    //
    // unsigned int8 follows
    //

    @Test
    public void extractUInt8WithSingleIndex() {
        byte[] bytes = new byte[] { -1, 2, 3 };
        assertEquals(255, ModbusBitUtilities.extractUInt8(bytes, 0));
        assertEquals(2, ModbusBitUtilities.extractUInt8(bytes, 1));
        assertEquals(3, ModbusBitUtilities.extractUInt8(bytes, 2));
    }

    @Test
    public void extractUInt8WithSingleIndexOOB() {
        byte[] bytes = new byte[] { -1, 2, 3 };
        assertThrows(IllegalArgumentException.class, () -> ModbusBitUtilities.extractUInt8(bytes, 3));
    }

    @Test
    public void extractUInt8WithSingleIndexOOB2() {
        byte[] bytes = new byte[] { -1, 2, 3 };
        assertThrows(IllegalArgumentException.class, () -> ModbusBitUtilities.extractUInt8(bytes, -1));
    }

    @Test
    public void extractUInt8WithRegisterIndexAndHiByte() {
        byte[] bytes = new byte[] { -1, 2, 3, 4 };
        assertEquals(255, ModbusBitUtilities.extractUInt8(bytes, 0, true));
        assertEquals(2, ModbusBitUtilities.extractUInt8(bytes, 0, false));
        assertEquals(3, ModbusBitUtilities.extractUInt8(bytes, 1, true));
        assertEquals(4, ModbusBitUtilities.extractUInt8(bytes, 1, false));
    }

    @Test
    public void extractUInt8WithRegisterIndexAndHiByteOOB() {
        byte[] bytes = new byte[] { -1, 2, 3, 4 };
        assertThrows(IllegalArgumentException.class, () -> ModbusBitUtilities.extractUInt8(bytes, 2, true));
    }

    @Test
    public void extractUInt8WithRegisterIndexAndHiByteOOB2() {
        byte[] bytes = new byte[] { -1, 2, 3, 4 };
        assertThrows(IllegalArgumentException.class, () -> ModbusBitUtilities.extractUInt8(bytes, 255, true));
    }
}
