/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.rfxcom.internal.messages;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;
import org.openhab.core.util.HexUtils;

/**
 * Test for RFXCom-binding
 *
 * @author Martin van Wingerden - Initial contribution
 * @author Mike Jagdis - Added actual functional tests
 */
@NonNullByDefault
public class RFXComUVMessageTest {
    @Test
    public void testMessage1() throws RFXComException {
        String hexMessage = "095703123421194731E9";

        byte[] message = HexUtils.hexToBytes(hexMessage);
        RFXComUVMessage msg = (RFXComUVMessage) RFXComMessageFactoryImpl.INSTANCE.createMessage(message);

        assertEquals(RFXComUVMessage.SubType.UV3, msg.subType, "SubType");
        assertEquals(18, msg.seqNbr, "Seq Number");
        assertEquals("13345", msg.getDeviceId(), "Sensor Id");

        assertEquals(2.5, msg.uv, 0.001, "UV");
        assertEquals(1822.5, msg.temperature, 0.001, "Temperature");

        assertEquals(14, msg.signalLevel, "Signal Level");
        assertEquals(9, msg.batteryLevel, "Battery Level");

        byte[] decoded = msg.decodeMessage();

        assertEquals(hexMessage, HexUtils.bytesToHex(decoded), "Message converted back");
    }

    @Test
    public void testMessage2() throws RFXComException {
        String hexMessage = "09570312342119C731E9";

        byte[] message = HexUtils.hexToBytes(hexMessage);
        RFXComUVMessage msg = (RFXComUVMessage) RFXComMessageFactoryImpl.INSTANCE.createMessage(message);

        assertEquals(RFXComUVMessage.SubType.UV3, msg.subType, "SubType");
        assertEquals(18, msg.seqNbr, "Seq Number");
        assertEquals("13345", msg.getDeviceId(), "Sensor Id");

        assertEquals(2.5, msg.uv, 0.001, "UV");
        assertEquals(-1822.5, msg.temperature, 0.001, "Temperature");

        assertEquals(14, msg.signalLevel, "Signal Level");
        assertEquals(9, msg.batteryLevel, "Battery Level");

        byte[] decoded = msg.decodeMessage();

        assertEquals(hexMessage, HexUtils.bytesToHex(decoded), "Message converted back");
    }
}
