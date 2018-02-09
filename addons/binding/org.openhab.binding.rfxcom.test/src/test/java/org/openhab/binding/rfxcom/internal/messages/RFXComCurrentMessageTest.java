/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.rfxcom.internal.messages;

import org.junit.Test;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;

import javax.xml.bind.DatatypeConverter;

import static org.junit.Assert.assertEquals;
import static org.openhab.binding.rfxcom.internal.messages.RFXComCurrentMessage.*;

/**
 * Test for RFXCom-binding
 *
 * @author Martin van Wingerden - Initial Contribution
 */
public class RFXComCurrentMessageTest {

    @Test
    public void testSomeMessages() throws RFXComException {
        String message = "0D59010F860004001D0000000049";

        final RFXComCurrentMessage msg = (RFXComCurrentMessage) RFXComMessageFactory
                .createMessage(DatatypeConverter.parseHexBinary(message));
        assertEquals("SubType", SubType.ELEC1, msg.subType);
        assertEquals("Seq Number", 15, (short) (msg.seqNbr & 0xFF));
        assertEquals("Sensor Id", "34304", msg.getDeviceId());
        assertEquals("Count", 4, msg.count);
        assertEquals("Channel 1", 2.9d, msg.channel1Amps, 0.01);
        assertEquals("Channel 2", 0d, msg.channel2Amps, 0.01);
        assertEquals("Channel 3", 0d, msg.channel3Amps, 0.01);
        assertEquals("Signal Level", 4, msg.signalLevel);
        assertEquals("Battery Level", 9, msg.batteryLevel);

        byte[] decoded = msg.decodeMessage();

        assertEquals("Message converted back", message, DatatypeConverter.printHexBinary(decoded));

    }
}
