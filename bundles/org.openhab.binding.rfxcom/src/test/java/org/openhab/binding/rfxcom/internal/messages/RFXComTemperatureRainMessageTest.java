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
package org.openhab.binding.rfxcom.internal.messages;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.openhab.binding.rfxcom.internal.messages.RFXComTemperatureRainMessage.SubType.WS1200;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;
import org.openhab.core.util.HexUtils;

/**
 * Test for RFXCom-binding
 *
 * @author Martin van Wingerden - Initial contribution
 */
@NonNullByDefault
public class RFXComTemperatureRainMessageTest {
    @Test
    public void testSomeMessages() throws RFXComException {
        String hexMessage = "0A4F01CCF001004F03B759";
        byte[] message = HexUtils.hexToBytes(hexMessage);
        RFXComTemperatureRainMessage msg = (RFXComTemperatureRainMessage) RFXComMessageFactoryImpl.INSTANCE
                .createMessage(message);
        assertEquals(WS1200, msg.subType, "SubType");
        assertEquals(204, (short) (msg.seqNbr & 0xFF), "Seq Number");
        assertEquals("61441", msg.getDeviceId(), "Sensor Id");
        assertEquals(7.9, msg.temperature, 0.001, "Temperature");
        assertEquals(95.1, msg.rainTotal, 0.001, "Rain total");
        assertEquals((byte) 5, msg.signalLevel, "Signal Level");

        byte[] decoded = msg.decodeMessage();

        assertEquals(hexMessage, HexUtils.bytesToHex(decoded), "Message converted back");
    }
}
