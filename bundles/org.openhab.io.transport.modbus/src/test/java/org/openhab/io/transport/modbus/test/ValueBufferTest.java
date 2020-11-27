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

import java.nio.BufferOverflowException;
import java.nio.InvalidMarkException;

import org.junit.jupiter.api.Test;
import org.openhab.io.transport.modbus.ValueBuffer;

/**
 * @author Sami Salonen - Initial contribution
 */
public class ValueBufferTest {

    @Test
    public void testInt32Int8() {
        ValueBuffer wrap = ValueBuffer.wrap(new byte[] { (byte) 0xFF, (byte) 0xFF, (byte) 0xFC, 0x14, 3, -1, -2 });
        assertEquals(7, wrap.remaining());
        assertTrue(wrap.hasRemaining());

        assertEquals(-1004, wrap.getSInt32());
        assertEquals(3, wrap.remaining());
        assertTrue(wrap.hasRemaining());

        assertEquals(3, wrap.getSInt8());
        assertEquals(2, wrap.remaining());
        assertTrue(wrap.hasRemaining());

        assertEquals(-1, wrap.getSInt8());
        assertEquals(1, wrap.remaining());
        assertTrue(wrap.hasRemaining());

        assertEquals(254, wrap.getUInt8());
        assertEquals(0, wrap.remaining());
        assertFalse(wrap.hasRemaining());
    }

    @Test
    public void testOutOfBounds() {
        ValueBuffer wrap = ValueBuffer.wrap(new byte[] { (byte) 0xFF, (byte) 0xFF, (byte) 0xFC, 0x14, 3, -1, -2 });
        wrap.position(7);
        assertThrows(IllegalArgumentException.class, () -> wrap.getSInt8());
    }

    @Test
    public void testOutOfBound2s() {
        ValueBuffer wrap = ValueBuffer.wrap(new byte[] { (byte) 0xFF, (byte) 0xFF, (byte) 0xFC, 0x14, 3, -1, -2 });
        wrap.position(6);
        assertThrows(IllegalArgumentException.class, () -> wrap.getSInt16());
    }

    @Test
    public void testMarkReset() {
        ValueBuffer wrap = ValueBuffer.wrap(new byte[] { (byte) 0xFF, (byte) 0xFF, (byte) 0xFC, 0x14, 3, -1, -2 });
        wrap.mark();
        assertEquals(-1004, wrap.getSInt32());
        wrap.reset();
        assertEquals(4294966292L, wrap.getUInt32());
        wrap.mark();
        assertEquals(3, wrap.getSInt8());
        wrap.reset();
        assertEquals(3, wrap.getSInt8());
        assertEquals(-1, wrap.getSInt8());
        assertEquals(254, wrap.getUInt8());
    }

    @Test
    public void testMarkHigherThanPosition() {
        ValueBuffer wrap = ValueBuffer.wrap(new byte[] { (byte) 0xFF, (byte) 0xFF, (byte) 0xFC, 0x14, 3, -1, -2 });
        assertEquals(-1004, wrap.getSInt32());
        wrap.position(4);
        wrap.mark();
        assertEquals(4, wrap.position());

        // mark = position
        wrap.position(4);
        assertEquals(4, wrap.position());
        wrap.reset();
        assertEquals(4, wrap.position());

        // position < mark
        wrap.position(3); // Mark is removed here
        assertEquals(3, wrap.position());
        boolean caughtException = false;
        try {
            wrap.reset();
        } catch (InvalidMarkException e) {
            // OK, expected
            caughtException = true;
        }
        assertTrue(caughtException);
        assertEquals(3, wrap.position());

        // Mark is removed. Reset unaccessible even with original position of 4
        wrap.position(4);
        assertEquals(4, wrap.position());
        caughtException = false;
        try {
            wrap.reset();
        } catch (InvalidMarkException e) {
            // OK, expected
            caughtException = true;
        }
        assertTrue(caughtException);
    }

    @Test
    public void testMarkLowerThanPosition() {
        ValueBuffer wrap = ValueBuffer.wrap(new byte[] { (byte) 0xFF, (byte) 0xFF, (byte) 0xFC, 0x14, 3, -1, -2 });
        assertEquals(-1004, wrap.getSInt32());
        wrap.position(4);
        wrap.mark();
        assertEquals(4, wrap.position());

        // mark = position
        wrap.position(4);
        assertEquals(4, wrap.position());
        wrap.reset();
        assertEquals(4, wrap.position());

        // mark > position
        wrap.position(6);
        assertEquals(6, wrap.position());
        wrap.reset();
        assertEquals(4, wrap.position());
    }

    @Test
    public void testPosition() {
        ValueBuffer wrap = ValueBuffer.wrap(new byte[] { 0, 0, 0, 1, 3, -1, -2 });
        assertEquals(0, wrap.position());

        wrap.position(4);
        assertEquals(4, wrap.position());
        assertEquals(3, wrap.getSInt8());
        assertEquals(5, wrap.position());
    }

    @Test
    public void testBulkGetBufferOverflow() {
        ValueBuffer wrap = ValueBuffer.wrap(new byte[] { 0, 0 });
        byte[] threeBytes = new byte[3];
        assertThrows(BufferOverflowException.class, () -> wrap.get(threeBytes));
    }

    @Test
    public void testBulkGetAtCapacity() {
        ValueBuffer wrap = ValueBuffer.wrap(new byte[] { 1, 2 });
        byte[] twoBytes = new byte[2];
        wrap.get(twoBytes);
        assertEquals(1, twoBytes[0]);
        assertEquals(2, twoBytes[1]);
        assertEquals(2, wrap.position());
        assertFalse(wrap.hasRemaining());
    }

    @Test
    public void testBulkGet() {
        ValueBuffer wrap = ValueBuffer.wrap(new byte[] { 1, 2, 3 });
        byte[] onebyte = new byte[1];
        wrap.get(onebyte);
        assertEquals(1, onebyte[0]);
        assertEquals(1, wrap.position());

        // non-zero position
        byte[] twoBytes = new byte[2];
        wrap.position(1);
        wrap.get(twoBytes);
        assertEquals(2, twoBytes[0]);
        assertEquals(3, twoBytes[1]);
        assertEquals(3, wrap.position());
    }
}
