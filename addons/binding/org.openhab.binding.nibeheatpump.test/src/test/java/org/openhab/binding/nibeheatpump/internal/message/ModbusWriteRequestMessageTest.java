/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
        assertEquals(okMessage, HexUtils.bytesToHex(byteMessage));
    }

    @Test
    public void parseMessageTest() throws NibeHeatPumpException {
        final byte[] byteMessage = HexUtils.hexToBytes(okMessage);
        ModbusWriteRequestMessage m = new ModbusWriteRequestMessage(byteMessage);
        assertEquals(coilAddress, m.getCoilAddress());
        assertEquals(value, m.getValue());
    }

    @Test(expected = NibeHeatPumpException.class)
    public void badCrcTest() throws NibeHeatPumpException {
        final String strMessage = "C06B06393006120F00BA";
        final byte[] msg = HexUtils.hexToBytes(strMessage);
        new ModbusWriteRequestMessage(msg);
    }

    @Test(expected = NibeHeatPumpException.class)
    public void notWriteRequestMessageTest() throws NibeHeatPumpException {
        final String strMessage = "C06A06393006120F00BF";
        final byte[] byteMessage = HexUtils.hexToBytes(strMessage);
        new ModbusWriteRequestMessage(byteMessage);
    }
}
