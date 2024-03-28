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
package org.openhab.binding.broadlink.internal;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Tests the generic utility functions.
 * 
 * @author John Marshall/Cato Sognen - Initial contribution
 */
@NonNullByDefault
public class UtilsTest {
    @Test
    public void padToDoesNothingOnQuotientSizedBuffer() {
        byte[] source = { 0x01, 0x02, 0x03, 0x04 };

        byte[] result = Utils.padTo(source, 4);
        assertEquals(source, result);
    }

    @Test
    public void padToExtendsOversizedBuffer() {
        byte[] source = { 0x01, 0x02, 0x03, 0x04 };

        byte[] result = Utils.padTo(source, 3);
        assertEquals(6, result.length);
        byte[] expected = { 0x01, 0x02, 0x03, 0x04, 0x0, 0x0, };

        assertArrayEquals(expected, result);
    }

    @Test
    public void padToExtendsUndersizedBuffer() {
        byte[] source = { 0x01, 0x02, 0x03, 0x04 };

        byte[] result = Utils.padTo(source, 8);
        assertEquals(8, result.length);
        byte[] expected = { 0x01, 0x02, 0x03, 0x04, 0x0, 0x0, 0x0, 0x0, };

        assertArrayEquals(expected, result);
    }

    @Test
    public void padToExtendsUndersizedBufferMultiple() {
        byte[] source = { 0x01, 0x02, 0x03, 0x04 };

        byte[] result = Utils.padTo(source, 16);
        assertEquals(16, result.length);
        byte[] expected = { 0x01, 0x02, 0x03, 0x04, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, };

        assertArrayEquals(expected, result);
    }

    @Test
    public void toHexStringWorksOnSingleByte() {
        byte[] source = { (byte) 0x81 };

        String result = Utils.toHexString(source);

        assertEquals("81", result);
    }

    @Test
    public void toHexStringWorksOnMultipleBytesLowValues() {
        byte[] source = { 0x01, 0x02, 0x03, 0x04 };

        String result = Utils.toHexString(source);

        assertEquals("01020304", result);
    }

    @Test
    public void toHexStringWorksOnMultipleBytesMixedValues() {
        byte[] source = { (byte) 0x81, (byte) 0xC2, 0x03, (byte) 0xA3, (byte) 0xF4 };

        String result = Utils.toHexString(source);

        assertEquals("81c203a3f4", result);
    }
}
