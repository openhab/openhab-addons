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
public class ModbusReadRequestMessageTest {

    @Before
    public void Before() {
    }

    final String okMessage = "C069023930A2";
    final int coilAddress = 12345;

    @Test
    public void createMessageTest() throws NibeHeatPumpException {
        ModbusReadRequestMessage m = new ModbusReadRequestMessage.MessageBuilder().coilAddress(coilAddress).build();
        byte[] byteMessage = m.decodeMessage();
        assertEquals(okMessage, DatatypeConverter.printHexBinary(byteMessage));
    }

    @Test
    public void parseMessageTest() throws NibeHeatPumpException {
        byte[] byteMessage = DatatypeConverter.parseHexBinary(okMessage);
        ModbusReadRequestMessage m = new ModbusReadRequestMessage(byteMessage);
        assertEquals(coilAddress, m.getCoilAddress());
    }

    @Test
    public void badCrcTest() {
        final String strMessage = "C069023930A1";

        final byte[] msg = DatatypeConverter.parseHexBinary(strMessage);
        try {
            @SuppressWarnings("unused")
            ModbusReadRequestMessage m = new ModbusReadRequestMessage(msg);
            fail("Method didn't throw NibeHeatPumpException when expected");
        } catch (NibeHeatPumpException e) {
            assertTrue(e.getMessage().startsWith("Checksum does not match"));
        }
    }

    @Test
    public void notReadRequestMessageTest() {
        final String strMessage = "C169023930A2";
        final byte[] byteMessage = DatatypeConverter.parseHexBinary(strMessage);
        try {
            @SuppressWarnings("unused")
            ModbusReadRequestMessage m = new ModbusReadRequestMessage(byteMessage);
            fail("Method didn't throw NibeHeatPumpException when expected");
        } catch (NibeHeatPumpException e) {
            assertEquals(e.getMessage(), "Not Read Request message");
        }
    }
}
