/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.test.internal.protocol;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageClass;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageType;
import org.openhab.binding.zwave.internal.protocol.ZWaveSerialMessageException;

/**
 * Test cases for {@link SerialMessage}. This performs basic checks on the serial message processing to ensure packets
 * are handled correctly.
 *
 * @author Chris Jackson - Initial version
 */
public class SerialMessageTest {

    @Test
    public void TestCreate() {
        byte[] packetData = { 0x01, 0x14, 0x00, 0x04, 0x00, 0x2C, 0x0E, 0x32, 0x02, 0x21, 0x34, 0x00, 0x00, 0x01,
                (byte) 0xB8, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x51 };
        byte[] packetDataFaulty = { 0x01, 0x14, 0x00, 0x04, 0x00, 0x2C, 0x0E, 0x32, 0x02, 0x21, 0x34, 0x00, 0x00, 0x01,
                (byte) 0xB8, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x52 };
        SerialMessage msg = new SerialMessage(packetData);

        assertEquals(msg.getMessageBuffer()[5], 44);
        assertEquals(msg.isValid, true);
        assertEquals(SerialMessageType.Request, msg.getMessageType());
        assertEquals(SerialMessageClass.ApplicationCommandHandler, msg.getMessageClass());

        try {
            assertEquals(msg.getMessagePayloadByte(5), 33);
            assertEquals(msg.getMessagePayloadByte(9), 1);
        } catch (ZWaveSerialMessageException e) {
        }

        boolean oob = false;
        try {
            msg.getMessagePayloadByte(17);
        } catch (ZWaveSerialMessageException e) {
            oob = true;
        }
        assertEquals(oob, true);

        // Make sure we correct detect a packet with a corrupt CRC
        msg = new SerialMessage(packetDataFaulty);
        assertEquals(msg.isValid, false);
    }
}
