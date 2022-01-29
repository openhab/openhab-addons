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
import static org.openhab.binding.rfxcom.internal.messages.RFXComWindMessage.SubType.WIND1;

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
public class RFXComWindMessageTest {
    @Test
    public void testSomeMessages() throws RFXComException {
        String hexMessage = "105601122F000087000000140000000079";
        byte[] message = HexUtils.hexToBytes(hexMessage);
        RFXComWindMessage msg = (RFXComWindMessage) RFXComMessageFactoryImpl.INSTANCE.createMessage(message);
        assertEquals(WIND1, msg.subType, "SubType");
        assertEquals(18, msg.seqNbr, "Seq Number");
        assertEquals("12032", msg.getDeviceId(), "Sensor Id");
        assertEquals(135.0, msg.windDirection, 0.001, "Direction");
        // assertEquals(0.0, msg.w9j, 0.001, "Average speed");
        assertEquals(2.0, msg.windSpeed, 0.001, "Wind Gust");
        assertEquals(7, msg.signalLevel, "Signal Level");
        assertEquals(9, msg.batteryLevel, "Battery Level");

        byte[] decoded = msg.decodeMessage();

        assertEquals(hexMessage, HexUtils.bytesToHex(decoded), "Message converted back");
    }
}
