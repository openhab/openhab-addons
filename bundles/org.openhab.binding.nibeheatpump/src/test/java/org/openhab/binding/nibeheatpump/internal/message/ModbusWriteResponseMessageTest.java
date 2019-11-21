/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import static org.junit.Assert.assertEquals;

import org.eclipse.smarthome.core.util.HexUtils;
import org.junit.Before;
import org.junit.Test;
import org.openhab.binding.nibeheatpump.internal.NibeHeatPumpException;

/**
 * Tests cases for {@link ModbusReadRequestMessage}.
 *
 * @author Pauli Anttila - Initial contribution
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
        assertEquals(okMessage, HexUtils.bytesToHex(byteMessage));
    }

    @Test
    public void createMessage2Test() throws NibeHeatPumpException {
        final String okMessage = "5C00206C01004D";
        ModbusWriteResponseMessage m = new ModbusWriteResponseMessage.MessageBuilder().result(false).build();
        byte[] byteMessage = m.decodeMessage();
        assertEquals(okMessage, HexUtils.bytesToHex(byteMessage));
    }

    @Test
    public void parseSuccMessageTest() throws NibeHeatPumpException {
        final String message = "5C00206C01014C";
        byte[] byteMessage = HexUtils.hexToBytes(message);
        ModbusWriteResponseMessage m = new ModbusWriteResponseMessage(byteMessage);
        assertEquals(true, m.isSuccessfull());
    }

    @Test
    public void parseFailMessageTest() throws NibeHeatPumpException {
        final String strMessage = "5C00206C01004D";
        final byte[] byteMessage = HexUtils.hexToBytes(strMessage);
        ModbusWriteResponseMessage m = new ModbusWriteResponseMessage(byteMessage);
        assertEquals(false, m.isSuccessfull());
    }

    @Test(expected = NibeHeatPumpException.class)
    public void badCrcTest() throws NibeHeatPumpException {
        final String strMessage = "5C00206C01004A";
        final byte[] msg = HexUtils.hexToBytes(strMessage);
        new ModbusWriteResponseMessage(msg);
    }

    @Test(expected = NibeHeatPumpException.class)
    public void notWriteResponseMessageTest() throws NibeHeatPumpException {
        final String strMessage = "5C00206B060102030405064A";
        final byte[] byteMessage = HexUtils.hexToBytes(strMessage);
        new ModbusWriteResponseMessage(byteMessage);
    }
}
