/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nibeheatpump.internal.message;

import static org.junit.Assert.*;

import javax.xml.bind.DatatypeConverter;

import org.junit.Before;
import org.junit.Test;
import org.openhab.binding.nibeheatpump.internal.NibeHeatPumpException;

/**
 * Tests cases for {@link ModbusReadRequestMessage}.
 *
 * @author Pauli Anttila
 */
public class ModbusReadResponseMessageTest {

    @Before
    public void Before() {
    }

    final int coilAddress = 513;
    final int value = 100992003;
    final String okMessage = "5C00206A060102030405064B";

    @Test
    public void createMessageTest() throws NibeHeatPumpException {
        ModbusReadResponseMessage m = new ModbusReadResponseMessage.MessageBuilder().coilAddress(coilAddress)
                .value(value).build();
        byte[] byteMessage = m.decodeMessage();
        assertEquals(okMessage, DatatypeConverter.printHexBinary(byteMessage));
    }

    @Test
    public void parseMessageTest() throws NibeHeatPumpException {
        byte[] msg = DatatypeConverter.parseHexBinary(okMessage);
        ModbusReadResponseMessage m = (ModbusReadResponseMessage) MessageFactory.getMessage(msg);
        assertEquals(coilAddress, m.getCoilAddress());
        assertEquals(value, m.getValue());
    }

    @Test
    public void badCrcTest() {
        final String strMessage = "5C00206A060102030405064C";
        final byte[] byteMessage = DatatypeConverter.parseHexBinary(strMessage);
        try {
            @SuppressWarnings("unused")
            ModbusReadResponseMessage m = (ModbusReadResponseMessage) MessageFactory.getMessage(byteMessage);
            fail("Method didn't throw NibeHeatPumpException when expected");
        } catch (NibeHeatPumpException e) {
            assertTrue(e.getMessage().startsWith("Checksum does not match"));
        }
    }

    @Test
    public void notReadResponseMessageTest() {
        final String strMessage = "5C00206B060102030405064A";
        final byte[] byteMessage = DatatypeConverter.parseHexBinary(strMessage);
        try {
            @SuppressWarnings("unused")
            ModbusReadResponseMessage m = new ModbusReadResponseMessage(byteMessage);
            fail("Method didn't throw NibeHeatPumpException when expected");
        } catch (NibeHeatPumpException e) {
            assertEquals(e.getMessage(), "Not Read Response message");
        }
    }
}
