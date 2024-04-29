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
package org.openhab.binding.nibeheatpump.internal.message;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;
import org.openhab.binding.nibeheatpump.internal.NibeHeatPumpException;
import org.openhab.core.util.HexUtils;

/**
 * Tests cases for {@link ModbusDataReadOutMessage}.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class ModbusDataReadOutMessageTest {

    @Test
    public void createMessageTest() throws NibeHeatPumpException {
        final String okMessage = "5C0020685001A81F0100A86400FDA7D003449C1E004F9CA000509C7800519C0301529C1B01879C14014E9CC601479C010115B9B0FF3AB94B00C9AF0000489C0D014C9CE7004B9C0000FFFF0000FFFF0000FFFF000045";

        @SuppressWarnings("serial")
        final ArrayList<ModbusValue> values = new ArrayList<>() {
            {
                add(new ModbusValue(43009, 287));
                add(new ModbusValue(43008, 100));
                add(new ModbusValue(43005, 976));
                add(new ModbusValue(40004, 30));
                add(new ModbusValue(40015, 160));
                add(new ModbusValue(40016, 120));
                add(new ModbusValue(40017, 259));
                add(new ModbusValue(40018, 283));
                add(new ModbusValue(40071, 276));
                add(new ModbusValue(40014, 454));
                add(new ModbusValue(40007, 257));
                add(new ModbusValue(47381, -80));
                add(new ModbusValue(47418, 75));
                add(new ModbusValue(45001, 0));
                add(new ModbusValue(40008, 269));
                add(new ModbusValue(40012, 231));
                add(new ModbusValue(40011, 0));
                add(new ModbusValue(0xFFFF, 0));
                add(new ModbusValue(0xFFFF, 0));
                add(new ModbusValue(0xFFFF, 0));
            }
        };

        final ModbusDataReadOutMessage m = new ModbusDataReadOutMessage.MessageBuilder().values(values).build();
        final byte[] byteMessage = m.decodeMessage();
        assertEquals(okMessage, HexUtils.bytesToHex(byteMessage));
    }

    @Test
    public void parseModbusDataReadOutMessageTest() throws NibeHeatPumpException {
        final String message = "5C0020685001A81F0100A86400FDA7D003449C1E004F9CA000509C7800519C0301529C1B01879C14014E9CC601479C010115B9B0FF3AB94B00C9AF0000489C0D014C9CE7004B9C0000FFFF0000FFFF0000FFFF000045";

        @SuppressWarnings("serial")
        final ArrayList<ModbusValue> expectedValues = new ArrayList<>() {
            {
                add(new ModbusValue(43009, 287));
                add(new ModbusValue(43008, 100));
                add(new ModbusValue(43005, 976));
                add(new ModbusValue(40004, 30));
                add(new ModbusValue(40015, 160));
                add(new ModbusValue(40016, 120));
                add(new ModbusValue(40017, 259));
                add(new ModbusValue(40018, 283));
                add(new ModbusValue(40071, 276));
                add(new ModbusValue(40014, 454));
                add(new ModbusValue(40007, 257));
                add(new ModbusValue(47381, 65456));
                add(new ModbusValue(47418, 75));
                add(new ModbusValue(45001, 0));
                add(new ModbusValue(40008, 269));
                add(new ModbusValue(40012, 231));
                add(new ModbusValue(40011, 0));
            }
        };

        checkRegisters(message, expectedValues);
    }

    @Test
    public void parseHeavilyEscapedModbusDataReadOutMessageTest() throws NibeHeatPumpException {
        final String message = "5C0020685401A81F0100A86400FDA7D003449C1E004F9CA000509C7800519C0301529C1B01879C14014E9CC601479C010115B9B0FF3AB94B00C9AF0000489C0D014C9CE7004B9C0000FFFF0000FFFF00005C5C5C5C5C5C5C5C41";

        @SuppressWarnings("serial")
        final ArrayList<ModbusValue> expectedValues = new ArrayList<>() {
            {
                add(new ModbusValue(43009, 287));
                add(new ModbusValue(43008, 100));
                add(new ModbusValue(43005, 976));
                add(new ModbusValue(40004, 30));
                add(new ModbusValue(40015, 160));
                add(new ModbusValue(40016, 120));
                add(new ModbusValue(40017, 259));
                add(new ModbusValue(40018, 283));
                add(new ModbusValue(40071, 276));
                add(new ModbusValue(40014, 454));
                add(new ModbusValue(40007, 257));
                add(new ModbusValue(47381, 65456));
                add(new ModbusValue(47418, 75));
                add(new ModbusValue(45001, 0));
                add(new ModbusValue(40008, 269));
                add(new ModbusValue(40012, 231));
                add(new ModbusValue(40011, 0));
                add(new ModbusValue(23644, 23644));
            }
        };

        checkRegisters(message, expectedValues);
    }

    @Test
    public void specialLen1Test() throws NibeHeatPumpException {
        final String message = "5C00206851449C2500489CFC004C9CF1004E9CC7014D9C0B024F9C2500509C3300519C0B01529C5C5C01569C3100C9AF000001A80C01FDA716FAFAA9070098A91B1BFFFF0000A0A9CA02FFFF00009CA99212FFFF0000BE";

        @SuppressWarnings("serial")
        final ArrayList<ModbusValue> expectedValues = new ArrayList<>() {
            {
                add(new ModbusValue(40004, 37));
                add(new ModbusValue(40008, 252));
                add(new ModbusValue(40012, 241));
                add(new ModbusValue(40014, 455));
                add(new ModbusValue(40013, 523));
                add(new ModbusValue(40015, 37));
                add(new ModbusValue(40016, 51));
                add(new ModbusValue(40017, 267));
                add(new ModbusValue(40018, 348));
                add(new ModbusValue(40022, 49));
                add(new ModbusValue(45001, 0));
                add(new ModbusValue(43009, 268));
                add(new ModbusValue(43005, 64022));
                add(new ModbusValue(43514, 7));
                add(new ModbusValue(43416, 6939));
                add(new ModbusValue(43424, 714));
                add(new ModbusValue(43420, 4754));
            }
        };

        checkRegisters(message, expectedValues);
    }

    @Test
    public void specialLen2Test() throws NibeHeatPumpException {
        final String message = "5C00206852449C2500489CFE004C9CF2004E9CD4014D9CFB014F9C2500509C3700519C0D01529C5C5C01569C3200C9AF000001A80C01FDA712FAFAA9070098A95C5C1BFFFF0000A0A9D102FFFF00009CA9B412FFFF00007F";

        @SuppressWarnings("serial")
        final ArrayList<ModbusValue> expectedValues = new ArrayList<>() {
            {
                add(new ModbusValue(40004, 37));
                add(new ModbusValue(40008, 254));
                add(new ModbusValue(40012, 242));
                add(new ModbusValue(40014, 468));
                add(new ModbusValue(40013, 507));
                add(new ModbusValue(40015, 37));
                add(new ModbusValue(40016, 55));
                add(new ModbusValue(40017, 269));
                add(new ModbusValue(40018, 348));
                add(new ModbusValue(40022, 50));
                add(new ModbusValue(45001, 0));
                add(new ModbusValue(43009, 268));
                add(new ModbusValue(43005, 64018));
                add(new ModbusValue(43514, 7));
                add(new ModbusValue(43416, 7004));
                add(new ModbusValue(43424, 721));
                add(new ModbusValue(43420, 4788));
            }
        };

        checkRegisters(message, expectedValues);
    }

    @Test
    public void specialCrcTest() throws NibeHeatPumpException {
        final String message = "5C00206850449C2600489CF6004C9CF1004E9CD6014D9C0C024F9C4500509C3F00519CF100529C0401569CD500C9AF000001A80C01FDA799FAFAA9020098A91A1BFFFF0000A0A9CA02FFFF00009CA99212FFFF0000C5";

        @SuppressWarnings("serial")
        final ArrayList<ModbusValue> expectedValues = new ArrayList<>() {
            {
                add(new ModbusValue(40004, 38));
                add(new ModbusValue(40008, 246));
                add(new ModbusValue(40012, 241));
                add(new ModbusValue(40014, 470));
                add(new ModbusValue(40013, 524));
                add(new ModbusValue(40015, 69));
                add(new ModbusValue(40016, 63));
                add(new ModbusValue(40017, 241));
                add(new ModbusValue(40018, 260));
                add(new ModbusValue(40022, 213));
                add(new ModbusValue(45001, 0));
                add(new ModbusValue(43009, 268));
                add(new ModbusValue(43005, 64153));
                add(new ModbusValue(43514, 2));
                add(new ModbusValue(43416, 6938));
                add(new ModbusValue(43424, 714));
                add(new ModbusValue(43420, 4754));
            }
        };

        checkRegisters(message, expectedValues);
    }

    @Test
    public void badCrcTest() {
        final String message = "5C0020685001A81F0100A86400FDA7D003449C1E004F9CA000509C7800519C0301529C1B01879C14014E9CC601479C010115B9B0FF3AB94B00C9AF0000489C0D014C9CE7004B9C0000FFFF0000FFFF0000FFFF000044";

        final byte[] msg = HexUtils.hexToBytes(message);
        assertThrows(NibeHeatPumpException.class, () -> MessageFactory.getMessage(msg));
    }

    @Test
    public void notModbusDataReadOutMessageTest() {
        final String message = "519C0301529C1B01879C14014E9CC601479C010115B9B0FF3AB94B00C9AF0000489C0D014C9CE7004B9C0000FFFF0000FFFF0000FFFF000044";

        final byte[] msg = HexUtils.hexToBytes(message);
        assertThrows(NibeHeatPumpException.class, () -> new ModbusDataReadOutMessage(msg));
    }

    private void checkRegisters(final String message, final ArrayList<ModbusValue> expectedRegs)
            throws NibeHeatPumpException {
        final byte[] msg = HexUtils.hexToBytes(message);
        final ModbusDataReadOutMessage m = (ModbusDataReadOutMessage) MessageFactory.getMessage(msg);
        assertNotNull(m);

        final ArrayList<ModbusValue> actualValues = (ArrayList<ModbusValue>) m.getValues();

        assertNotNull(actualValues);
        assertEquals(expectedRegs.size(), actualValues.size());
        assertEquals(expectedRegs.toString(), actualValues.toString());
    }
}
