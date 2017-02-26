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
public class ModbusWriteResponseMessageTest {

    @Before
    public void Before() {
    }

    @Test
    public void createMessage1Test() throws NibeHeatPumpException {
        final String okMessage = "5C00206C01014C";
        ModbusWriteResponseMessage m = new ModbusWriteResponseMessage.MessageBuilder().result(true).build();
        byte[] byteMessage = m.decodeMessage();
        assertEquals(okMessage, DatatypeConverter.printHexBinary(byteMessage));
    }

    @Test
    public void createMessage2Test() throws NibeHeatPumpException {
        final String okMessage = "5C00206C01004D";
        ModbusWriteResponseMessage m = new ModbusWriteResponseMessage.MessageBuilder().result(false).build();
        byte[] byteMessage = m.decodeMessage();
        assertEquals(okMessage, DatatypeConverter.printHexBinary(byteMessage));
    }

    @Test
    public void parseSuccMessageTest() throws NibeHeatPumpException {
        final String message = "5C00206C01014C";
        byte[] byteMessage = DatatypeConverter.parseHexBinary(message);
        ModbusWriteResponseMessage m = new ModbusWriteResponseMessage(byteMessage);
        assertEquals(true, m.isSuccessfull());
    }

    @Test
    public void parseFailMessageTest() throws NibeHeatPumpException {
        final String strMessage = "5C00206C01004D";
        final byte[] byteMessage = DatatypeConverter.parseHexBinary(strMessage);
        ModbusWriteResponseMessage m = new ModbusWriteResponseMessage(byteMessage);
        assertEquals(false, m.isSuccessfull());
    }

    @Test
    public void badCrcTest() {
        final String strMessage = "5C00206C01004A";
        final byte[] msg = DatatypeConverter.parseHexBinary(strMessage);
        try {
            @SuppressWarnings("unused")
            ModbusWriteResponseMessage m = new ModbusWriteResponseMessage(msg);
            fail("Method didn't throw NibeHeatPumpException when expected");
        } catch (NibeHeatPumpException e) {
            assertTrue(e.getMessage().startsWith("Checksum does not match"));
        }
    }

    @Test
    public void notWriteResponseMessageTest() {
        final String strMessage = "5C00206B060102030405064A";
        final byte[] byteMessage = DatatypeConverter.parseHexBinary(strMessage);
        try {
            @SuppressWarnings("unused")
            ModbusWriteResponseMessage m = new ModbusWriteResponseMessage(byteMessage);
            fail("Method didn't throw NibeHeatPumpException when expected");
        } catch (NibeHeatPumpException e) {
            assertEquals(e.getMessage(), "Not Write Response message");
        }
    }
}
