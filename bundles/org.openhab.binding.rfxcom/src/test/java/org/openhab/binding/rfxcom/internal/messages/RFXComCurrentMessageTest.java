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
import org.openhab.binding.rfxcom.internal.messages.RFXComCurrentMessage.SubType;
import org.openhab.core.util.HexUtils;

/**
 * Test for RFXCom-binding
 *
 * @author Martin van Wingerden - Initial Contribution
 */
@NonNullByDefault
public class RFXComCurrentMessageTest {

    @Test
    public void testSomeMessages() throws RFXComException {
        String message = "0D59010F860004001D0000000049";

        final RFXComCurrentMessage msg = (RFXComCurrentMessage) RFXComMessageFactoryImpl.INSTANCE
                .createMessage(HexUtils.hexToBytes(message));
        assertEquals(SubType.ELEC1, msg.subType, "SubType");
        assertEquals(15, (short) (msg.seqNbr & 0xFF), "Seq Number");
        assertEquals("34304", msg.getDeviceId(), "Sensor Id");
        assertEquals(4, msg.count, "Count");
        assertEquals(2.9d, msg.channel1Amps, 0.01, "Channel 1");
        assertEquals(0d, msg.channel2Amps, 0.01, "Channel 2");
        assertEquals(0d, msg.channel3Amps, 0.01, "Channel 3");
        assertEquals(4, msg.signalLevel, "Signal Level");
        assertEquals(9, msg.batteryLevel, "Battery Level");

        byte[] decoded = msg.decodeMessage();

        assertEquals(message, HexUtils.bytesToHex(decoded), "Message converted back");
    }
}
