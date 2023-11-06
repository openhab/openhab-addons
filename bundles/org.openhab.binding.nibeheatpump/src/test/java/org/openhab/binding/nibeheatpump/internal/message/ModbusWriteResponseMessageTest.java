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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.nibeheatpump.internal.NibeHeatPumpException;
import org.openhab.core.util.HexUtils;

/**
 * Tests cases for {@link ModbusReadRequestMessage}.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class ModbusWriteResponseMessageTest {

    @BeforeEach
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

    @Test
    public void badCrcTest() {
        final String strMessage = "5C00206C01004A";
        final byte[] msg = HexUtils.hexToBytes(strMessage);
        assertThrows(NibeHeatPumpException.class, () -> new ModbusWriteResponseMessage(msg));
    }

    @Test
    public void notWriteResponseMessageTest() {
        final String strMessage = "5C00206B060102030405064A";
        final byte[] byteMessage = HexUtils.hexToBytes(strMessage);
        assertThrows(NibeHeatPumpException.class, () -> new ModbusWriteResponseMessage(byteMessage));
    }
}
