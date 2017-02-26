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
public class ModbusWriteRequestMessageTest {

    @Before
    public void Before() {
    }

    final int coilAddress = 12345;
    final int value = 987654;
    final String okMessage = "C06B06393006120F00BF";

    @Test
    public void createMessageTest() throws NibeHeatPumpException {
        ModbusWriteRequestMessage m = new ModbusWriteRequestMessage.MessageBuilder().coilAddress(coilAddress)
                .value(value).build();
        byte[] byteMessage = m.decodeMessage();
        assertEquals(okMessage, DatatypeConverter.printHexBinary(byteMessage));
    }

    @Test
    public void parseMessageTest() throws NibeHeatPumpException {
        final byte[] byteMessage = DatatypeConverter.parseHexBinary(okMessage);
        ModbusWriteRequestMessage m = new ModbusWriteRequestMessage(byteMessage);
        assertEquals(coilAddress, m.getCoilAddress());
        assertEquals(value, m.getValue());
    }

    @Test
    public void badCrcTest() {
        final String strMessage = "C06B06393006120F00BA";
        final byte[] msg = DatatypeConverter.parseHexBinary(strMessage);
        try {
            @SuppressWarnings("unused")
            final ModbusWriteRequestMessage m = new ModbusWriteRequestMessage(msg);
            fail("Method didn't throw NibeHeatPumpException when expected");
        } catch (NibeHeatPumpException e) {
            assertTrue(e.getMessage().startsWith("Checksum does not match"));
        }
    }

    @Test
    public void notWriteRequestMessageTest() {
        final String strMessage = "C06A06393006120F00BF";
        final byte[] byteMessage = DatatypeConverter.parseHexBinary(strMessage);
        try {
            @SuppressWarnings("unused")
            ModbusWriteRequestMessage m = new ModbusWriteRequestMessage(byteMessage);
            fail("Method didn't throw NibeHeatPumpException when expected");
        } catch (NibeHeatPumpException e) {
            assertEquals(e.getMessage(), "Not Write Request message");
        }
    }
}
