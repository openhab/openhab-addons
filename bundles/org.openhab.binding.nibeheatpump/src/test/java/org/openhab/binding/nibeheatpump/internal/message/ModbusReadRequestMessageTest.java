/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

    @Test
    public void badCrcTest() {
        final String strMessage = "C069023930A1";
        final byte[] msg = HexUtils.hexToBytes(strMessage);
        assertThrows(NibeHeatPumpException.class, () -> new ModbusReadRequestMessage(msg));
    }

    @Test
    public void notReadRequestMessageTest() {
        final String strMessage = "C169023930A2";
        final byte[] byteMessage = HexUtils.hexToBytes(strMessage);
        assertThrows(NibeHeatPumpException.class, () -> new ModbusReadRequestMessage(byteMessage));
    }
}
