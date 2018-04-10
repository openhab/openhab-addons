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

import org.junit.Test;
import org.openhab.binding.nibeheatpump.internal.NibeHeatPumpException;

/**
 * Tests cases for {@link ModbusReadRequestMessage}.
 *
 * @author Pauli Anttila
 */
public class ModbusReadRequestMessageTest {

    private final String okMessage = "C069023930A2";
    private final int coilAddress = 12345;

    @Test
    public void createMessageTest() throws NibeHeatPumpException {
        ModbusReadRequestMessage m = new ModbusReadRequestMessage.MessageBuilder().coilAddress(coilAddress).build();
        byte[] byteMessage = m.decodeMessage();
        assertEquals(okMessage, HexUtils.bytesToHex(byteMessage));
    }

    @Test
    public void parseMessageTest() throws NibeHeatPumpException {
        byte[] byteMessage = HexUtils.hexToBytes(okMessage);
        ModbusReadRequestMessage m = new ModbusReadRequestMessage(byteMessage);
        assertEquals(coilAddress, m.getCoilAddress());
    }

    @Test(expected = NibeHeatPumpException.class)
    public void badCrcTest() throws NibeHeatPumpException {
        final String strMessage = "C069023930A1";
        final byte[] msg = HexUtils.hexToBytes(strMessage);
        new ModbusReadRequestMessage(msg);
    }

    @Test(expected = NibeHeatPumpException.class)
    public void notReadRequestMessageTest() throws NibeHeatPumpException {
        final String strMessage = "C169023930A2";
        final byte[] byteMessage = HexUtils.hexToBytes(strMessage);
        new ModbusReadRequestMessage(byteMessage);
    }
}
