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
package org.openhab.binding.solarman.internal.modbus;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;

import javax.validation.constraints.NotNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.solarman.internal.SolarmanLoggerConfiguration;
import org.openhab.binding.solarman.internal.SolarmanLoggerMode;
import org.openhab.binding.solarman.internal.modbus.exception.SolarmanException;

/**
 * @author Catalin Sanda - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@NonNullByDefault
class SolarmanV5ProtocolTest {
    SolarmanLoggerConnection solarmanLoggerConnection = (@NotNull SolarmanLoggerConnection) mock(
            SolarmanLoggerConnection.class);

    private SolarmanLoggerConfiguration loggerConfiguration = new SolarmanLoggerConfiguration("192.168.1.1", 8899,
            "1234567890", "sg04lp3", 60, SolarmanLoggerMode.V5MODBUS.toString(), null);

    private SolarmanV5Protocol solarmanV5Protocol = new SolarmanV5Protocol(loggerConfiguration);

    @Test
    void testbuildSolarmanV5Frame() {
        byte[] requestFrame = solarmanV5Protocol.buildSolarmanV5Frame((byte) 0x03, 0x0000, 0x0020);

        byte[] expectedFrame = { (byte) 0xA5, (byte) 0x17, (byte) 0x00, (byte) 0x10, (byte) 0x45, (byte) 0x00,
                (byte) 0x00, (byte) 0xD2, (byte) 0x02, (byte) 0x96, (byte) 0x49, (byte) 0x02, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x03, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x21, (byte) 0x85, (byte) 0xD2, (byte) 0x9D, (byte) 0x15 };

        assertArrayEquals(requestFrame, expectedFrame);
    }

    @Test
    void testReadRegister0x01() throws SolarmanException {
        // given
        when(solarmanLoggerConnection.sendRequest(any())).thenReturn(
                hexStringToByteArray("a5000000000000000000000000000000000000000000000000010301000ac84300000015"));

        // when
        Map<Integer, byte[]> regValues = solarmanV5Protocol.readRegisters(solarmanLoggerConnection, (byte) 0x03, 1, 1);

        // then
        assertEquals(1, regValues.size());
        assertTrue(regValues.containsKey(1));
        assertEquals("000A", bytesToHex(regValues.get(1)));
    }

    @Test
    void testReadRegisters0x02to0x03() throws SolarmanException {
        // given
        when(solarmanLoggerConnection.sendRequest(any())).thenReturn(
                hexStringToByteArray("a5000000000000000000000000000000000000000000000000010302000a000b13f600000015"));

        // when
        Map<Integer, byte[]> regValues = solarmanV5Protocol.readRegisters(solarmanLoggerConnection, (byte) 0x03, 2, 3);

        // then
        assertEquals(2, regValues.size());
        assertTrue(regValues.containsKey(2));
        assertTrue(regValues.containsKey(3));
        assertEquals("000A", bytesToHex(regValues.get(2)));
        assertEquals("000B", bytesToHex(regValues.get(3)));
    }

    @Test
    void testReadRegisterSUN10KSG04LP3EUPart1() throws SolarmanException {
        // given
        when(solarmanLoggerConnection.sendRequest(any())).thenReturn(hexStringToByteArray(
                "a53b0010150007482ee38d020121d0060091010000403e486301032800ffffff160a12162420ffffffffffffffffffffffffffffffffffff0001ffff0001ffff000003e81fa45115"));

        // when
        Map<Integer, byte[]> regValues = solarmanV5Protocol.readRegisters(solarmanLoggerConnection, (byte) 0x03, 0x3c,
                0x4f);

        // then
        assertEquals(20, regValues.size());
        assertTrue(regValues.containsKey(0x3c));
        assertTrue(regValues.containsKey(0x4f));
        assertEquals("00FF", bytesToHex(regValues.get(0x3c)));
        assertEquals("03E8", bytesToHex(regValues.get(0x4f)));
    }

    @Test
    void testReadRegisterSUN10KSG04LP3EUPart2() throws SolarmanException {
        // given
        when(solarmanLoggerConnection.sendRequest(any())).thenReturn(hexStringToByteArray(
                "a5330010150008482ee38d020122d0060091010000403e486301032000010000ffffffffffff0001ffffffffffffffffffff0000ffff0011ffffffff3a005715"));

        // when
        Map<Integer, byte[]> regValues = solarmanV5Protocol.readRegisters(solarmanLoggerConnection, (byte) 0x03, 0x50,
                0x5f);

        // then
        assertEquals(16, regValues.size());
        assertTrue(regValues.containsKey(0x50));
        assertTrue(regValues.containsKey(0x5f));
        assertEquals("0001", bytesToHex(regValues.get(0x50)));
        assertEquals("FFFF", bytesToHex(regValues.get(0x5f)));
    }

    private static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    @Nullable
    private static String bytesToHex(byte @Nullable [] bytes) {
        if (bytes == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }
}
