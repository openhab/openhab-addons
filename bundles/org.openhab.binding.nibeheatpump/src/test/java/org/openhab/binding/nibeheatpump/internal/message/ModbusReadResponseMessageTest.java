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
package org.openhab.binding.nibeheatpump.internal.message;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.openhab.binding.nibeheatpump.internal.NibeHeatPumpException;
import org.openhab.core.util.HexUtils;

/**
 * Tests cases for {@link ModbusReadRequestMessage}.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class ModbusReadResponseMessageTest {

    @Test
    public void createMessageTest() throws NibeHeatPumpException {
        final int coilAddress = 513;
        final int value = 100992003;
        final String okMessage = "5C00206A060102030405064B";
        ModbusReadResponseMessage m = new ModbusReadResponseMessage.MessageBuilder().coilAddress(coilAddress)
                .value(value).build();
        byte[] byteMessage = m.decodeMessage();
        assertEquals(okMessage, HexUtils.bytesToHex(byteMessage));
    }

    @Test
    public void parseMessageTest() throws NibeHeatPumpException {
        final int coilAddress = 513;
        final int value = 100992003;
        final String message = "5C00206A060102030405064B";
        ModbusReadResponseMessage m = (ModbusReadResponseMessage) MessageFactory
                .getMessage(HexUtils.hexToBytes(message));
        assertEquals(coilAddress, m.getCoilAddress());
        assertEquals(value, m.getValue());
    }

    @Test
    public void badCrcTest() {
        final String message = "5C00206A060102030405064C";
        assertThrows(NibeHeatPumpException.class, () -> MessageFactory.getMessage(HexUtils.hexToBytes(message)));
    }

    @Test
    public void notReadResponseMessageTest() {
        final String message = "5C00206B060102030405064A";
        assertThrows(NibeHeatPumpException.class, () -> new ModbusReadResponseMessage(HexUtils.hexToBytes(message)));
    }

    @Test
    public void parseEscapedMessageTest() throws NibeHeatPumpException {
        final int coilAddress = 513;
        final int value = 0x05E65C;
        final String message = "5C00206A0701025C5CE60500AD";
        ModbusReadResponseMessage m = (ModbusReadResponseMessage) MessageFactory
                .getMessage(HexUtils.hexToBytes(message));
        assertEquals(coilAddress, m.getCoilAddress());
        assertEquals(value, m.getValue());
    }
}
