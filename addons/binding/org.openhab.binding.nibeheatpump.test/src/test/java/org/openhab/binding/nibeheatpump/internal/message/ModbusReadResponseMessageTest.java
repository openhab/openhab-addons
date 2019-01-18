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

import org.junit.Test;
import org.openhab.binding.nibeheatpump.internal.NibeHeatPumpException;

/**
 * Tests cases for {@link ModbusReadRequestMessage}.
 *
 * @author Pauli Anttila
 */
public class ModbusReadResponseMessageTest {

    private final int coilAddress = 513;
    private final int value = 100992003;
    private final String okMessage = "5C00206A060102030405064B";

    @Test
    public void createMessageTest() throws NibeHeatPumpException {
        ModbusReadResponseMessage m = new ModbusReadResponseMessage.MessageBuilder().coilAddress(coilAddress)
                .value(value).build();
        byte[] byteMessage = m.decodeMessage();
        assertEquals(okMessage, HexUtils.bytesToHex(byteMessage));
    }

    @Test
    public void parseMessageTest() throws NibeHeatPumpException {
        byte[] msg = HexUtils.hexToBytes(okMessage);
        ModbusReadResponseMessage m = (ModbusReadResponseMessage) MessageFactory.getMessage(msg);
        assertEquals(coilAddress, m.getCoilAddress());
        assertEquals(value, m.getValue());
    }

    @Test(expected = NibeHeatPumpException.class)
    public void badCrcTest() throws NibeHeatPumpException {
        final String strMessage = "5C00206A060102030405064C";
        final byte[] byteMessage = HexUtils.hexToBytes(strMessage);
        MessageFactory.getMessage(byteMessage);
    }

    @Test(expected = NibeHeatPumpException.class)
    public void notReadResponseMessageTest() throws NibeHeatPumpException {
        final String strMessage = "5C00206B060102030405064A";
        final byte[] byteMessage = HexUtils.hexToBytes(strMessage);
        new ModbusReadResponseMessage(byteMessage);
    }
}
