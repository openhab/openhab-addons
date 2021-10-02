/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.miele.internal;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.test.java.JavaTest;
import org.openhab.core.types.UnDefType;

/**
 * This class provides test cases for {@link
 * org.openhab.binding.miele.internal.ExtendedDeviceStateUtil}
 *
 * @author Jacob Laursen - Added power/water consumption channels
 */

public class ExtendedDeviceStateUtilTest extends JavaTest {

    @Test
    public void bytesToHexWhenTopBitIsUsedReturnsCorrectString() {
        String actual = ExtendedDeviceStateUtil
                .bytesToHex(new byte[] { (byte) 0xde, (byte) 0xad, (byte) 0xbe, (byte) 0xef });
        assertEquals("DEADBEEF", actual);
    }

    /**
     * This test guards that the UTF-16 returned by the RPC-JSON API will be
     * considered as a sequence of 8-bit characters and converted into bytes
     * accordingly. Default behaviour of String.getBytes() assumes UTF-8
     * and adds a 0xc2 byte before any character out of ASCII range.
     */
    @Test
    public void stringToBytesWhenTopBitIsUsedReturnsSingleByte() {
        byte[] expected = new byte[] { (byte) 0x00, (byte) 0x80, (byte) 0x00 };
        byte[] actual = ExtendedDeviceStateUtil.stringToBytes("\u0000\u0080\u0000");
        assertArrayEquals(expected, actual);
    }

    @Test
    public void getTemperatureStateWellFormedValueReturnsQuantityType() throws NumberFormatException {
        assertEquals(new QuantityType<>(42, SIUnits.CELSIUS), ExtendedDeviceStateUtil.getTemperatureState("42"));
    }

    @Test
    public void getTemperatureStateMagicValueReturnsUndefined() throws NumberFormatException {
        assertEquals(UnDefType.UNDEF, ExtendedDeviceStateUtil.getTemperatureState("32768"));
    }

    @Test
    public void getTemperatureStateNonNumericValueThrowsNumberFormatException() {
        assertThrows(NumberFormatException.class, () -> ExtendedDeviceStateUtil.getTemperatureState("A"));
    }

    @Test
    public void getTemperatureStateNullValueThrowsNumberFormatException() {
        assertThrows(NumberFormatException.class, () -> ExtendedDeviceStateUtil.getTemperatureState(null));
    }
}
