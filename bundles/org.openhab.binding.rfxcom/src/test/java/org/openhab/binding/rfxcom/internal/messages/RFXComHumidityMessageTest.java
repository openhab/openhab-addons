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
 */
@NonNullByDefault
public class RFXComHumidityMessageTest {

    @Test
    public void testSomeMessages() throws RFXComException {
        String hexMessage = "085101027700360189";
        byte[] message = HexUtils.hexToBytes(hexMessage);
        RFXComHumidityMessage msg = (RFXComHumidityMessage) RFXComMessageFactoryImpl.INSTANCE.createMessage(message);
        assertEquals(RFXComHumidityMessage.SubType.HUM1, msg.subType, "SubType");
        assertEquals(2, (short) (msg.seqNbr & 0xFF), "Seq Number");
        assertEquals("30464", msg.getDeviceId(), "Sensor Id");
        assertEquals(54, msg.humidity, "Humidity");
        assertEquals(RFXComHumidityMessage.HumidityStatus.COMFORT, msg.humidityStatus, "Humidity status");
        assertEquals((byte) 8, msg.signalLevel, "Signal Level");
        assertEquals((byte) 9, msg.batteryLevel, "Battery Level");

        byte[] decoded = msg.decodeMessage();

        assertEquals(hexMessage, HexUtils.bytesToHex(decoded), "Message converted back");
    }
}
