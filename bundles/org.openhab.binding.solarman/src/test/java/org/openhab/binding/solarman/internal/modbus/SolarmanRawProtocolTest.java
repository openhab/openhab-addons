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
class SolarmanRawProtocolTest {
    SolarmanLoggerConnection solarmanLoggerConnection = (@NotNull SolarmanLoggerConnection) mock(
            SolarmanLoggerConnection.class);

    private SolarmanLoggerConfiguration loggerConfiguration = new SolarmanLoggerConfiguration("192.168.1.1", 8899,
            "1234567890", "sg04lp3", 60, SolarmanLoggerMode.RAWMODBUS.toString(), null);

    private SolarmanRawProtocol solarmanRawProtocol = new SolarmanRawProtocol(loggerConfiguration);

    @Test
    void testbuildSolarmanRawFrame() {
        byte[] requestFrame = solarmanRawProtocol.buildSolarmanRawFrame((byte) 0x03, 0x0063, 0x006D);
        byte[] expectedFrame = { (byte) 0x03, (byte) 0xE8, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x08,
                (byte) 0x01, (byte) 0x03, (byte) 0x00, (byte) 0x63, (byte) 0x00, (byte) 0x0B, (byte) 0xF4,
                (byte) 0x13 };

        assertArrayEquals(requestFrame, expectedFrame);
    }

    @Test
    void testReadRegister0x01() throws SolarmanException {
        // given
        when(solarmanLoggerConnection.sendRequest(any()))
                .thenReturn(hexStringToByteArray("03E800000019010316168016801590012C11940014005A000000050096007D"));

        // when
        Map<Integer, byte[]> regValues = solarmanRawProtocol.readRegisters(solarmanLoggerConnection, (byte) 0x03, 1, 1);

        // then
        assertEquals(1, regValues.size());
        assertTrue(regValues.containsKey(1));
        assertEquals("1680", bytesToHex(regValues.get(1)));
    }

    @Test
    void testReadRegisters0x02to0x03() throws SolarmanException {
        // given
        when(solarmanLoggerConnection.sendRequest(any()))
                .thenReturn(hexStringToByteArray("03E800000019010316168016801590012C11940014005A000000050096007D"));

        // when
        Map<Integer, byte[]> regValues = solarmanRawProtocol.readRegisters(solarmanLoggerConnection, (byte) 0x03, 2, 3);

        // then
        assertEquals(2, regValues.size());
        assertTrue(regValues.containsKey(2));
        assertTrue(regValues.containsKey(3));
        assertEquals("1680", bytesToHex(regValues.get(2)));
        assertEquals("1680", bytesToHex(regValues.get(3)));
    }

    @Test
    void testReadRegisterSUN10KSG04LP3EUPart1() throws SolarmanException {
        // given
        when(solarmanLoggerConnection.sendRequest(any())).thenReturn(hexStringToByteArray(
                "03E80000005101034E091A08FD092700000000000000020003000000050000138800800037002800A5004A003D000600010003000A00000000000600010003000A0000091B08F6091C006E00500014010E00C9003E0215"));

        // when
        Map<Integer, byte[]> regValues = solarmanRawProtocol.readRegisters(solarmanLoggerConnection, (byte) 0x03, 0x3c,
                0x4f);

        // then
        assertEquals(20, regValues.size());
        assertTrue(regValues.containsKey(0x3c));
        assertTrue(regValues.containsKey(0x4f));
        assertEquals("091A", bytesToHex(regValues.get(0x3c)));
        assertEquals("0001", bytesToHex(regValues.get(0x4f)));
    }

    @Test
    void testReadRegisterSUN10KSG04LP3EUPart2() throws SolarmanException {
        // given
        when(solarmanLoggerConnection.sendRequest(any())).thenReturn(hexStringToByteArray(
                "03E80000005101034E091A08FD092700000000000000020003000000050000138800800037002800A5004A003D000600010003000A00000000000600010003000A0000091B08F6091C006E00500014010E00C9003E0215"));

        // when
        Map<Integer, byte[]> regValues = solarmanRawProtocol.readRegisters(solarmanLoggerConnection, (byte) 0x03, 0x50,
                0x5f);

        // then
        assertEquals(16, regValues.size());
        assertTrue(regValues.containsKey(0x50));
        assertTrue(regValues.containsKey(0x5f));
        assertEquals("091A", bytesToHex(regValues.get(0x50)));
        assertEquals("00A5", bytesToHex(regValues.get(0x5f)));
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
