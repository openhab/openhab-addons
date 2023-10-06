/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.max.internal.message;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.max.internal.Utils;

/**
 * Tests cases for {@link Utils}.
 *
 * @author Andreas Heil (info@aheil.de) - Initial contribution
 * @author Marcel Verpaalen - OH2 Version and updates
 */
@NonNullByDefault
public class UtilsTest {

    @Test
    public void fromHexTest() {
        assertEquals(0x00, Utils.fromHex("00"));
        assertEquals(0x01, Utils.fromHex("01"));
        assertEquals(0x1F, Utils.fromHex("1F"));
        assertEquals(0xFF, Utils.fromHex("FF"));
    }

    @Test
    public void fromByteTest() {
        byte b0 = 0;
        byte b127 = 127;
        byte b128 = (byte) 128; // overflow due to
        byte bn128 = -128; // signed bytes
        byte bn1 = -1;

        int ar0 = Utils.fromByte(b0);
        int ar127 = Utils.fromByte(b127);
        int ar128 = Utils.fromByte(b128);
        int arn128 = Utils.fromByte(bn128);
        int arn1 = Utils.fromByte(bn1);

        assertEquals(0, ar0);
        assertEquals(127, ar127);
        assertEquals(128, ar128);
        assertEquals(128, arn128);
        assertEquals(255, arn1);
    }

    @Test
    public void toHexNoArgTest() {
        String actualResult = Utils.toHex();
        assertEquals("", actualResult);
    }

    @Test
    public void toHexOneArgTest() {
        String actualResult = Utils.toHex(15);
        assertEquals("0F", actualResult);
    }

    @Test
    public void toHexMultipleArgTest() {
        String actualResult = Utils.toHex(4863);
        assertEquals("12FF", actualResult);
    }

    @SuppressWarnings("deprecation")
    @Test
    public void resolveDateTimeTest() {
        int date = Utils.fromHex("858B"); // 05-09-2011
        int time = Utils.fromHex("2E"); // 23:00

        Date result = Utils.resolveDateTime(date, time);

        assertEquals(5, result.getDate());
        assertEquals(9, result.getMonth());
        assertEquals(2011, result.getYear());
        assertEquals(23, result.getHours());
        assertEquals(00, result.getMinutes());
    }

    @Test
    public void getBitsTest() {
        boolean[] b1 = Utils.getBits(0xFF);

        assertEquals(b1.length, 8);
        for (int i = 0; i < 8; i++) {
            assertEquals(true, b1[i]);
        }

        boolean[] b2 = Utils.getBits(0x5A);

        assertEquals(b2.length, 8);
        assertEquals(false, b2[0]);
        assertEquals(true, b2[1]);
        assertEquals(false, b2[2]);
        assertEquals(true, b2[3]);
        assertEquals(true, b2[4]);
        assertEquals(false, b2[5]);
        assertEquals(true, b2[6]);
        assertEquals(false, b2[7]);
    }

    @Test
    public void hexStringToByteArrayTest() {
        String s = "000102030AFF";

        byte[] result = Utils.hexStringToByteArray(s);

        assertEquals(0, result[0] & 0xFF);
        assertEquals(1, result[1] & 0xFF);
        assertEquals(2, result[2] & 0xFF);
        assertEquals(3, result[3] & 0xFF);
        assertEquals(10, result[4] & 0xFF);
        assertEquals(255, result[5] & 0xFF);
    }
}
