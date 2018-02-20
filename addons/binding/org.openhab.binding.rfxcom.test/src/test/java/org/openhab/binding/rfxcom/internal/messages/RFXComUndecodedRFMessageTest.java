/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.rfxcom.internal.messages;

import static org.junit.Assert.assertEquals;

import javax.xml.bind.DatatypeConverter;

import org.junit.Test;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComMessageTooLongException;
import org.openhab.binding.rfxcom.internal.messages.RFXComBaseMessage.PacketType;

/**
 * Test for RFXCom-binding
 *
 * @author Martin van Wingerden
 * @author James Hewitt-Thomas
 * @since 1.9.0
 */
public class RFXComUndecodedRFMessageTest {

    private void testMessage(String hexMsg, RFXComUndecodedRFMessage.SubType subType, int seqNbr, String rawPayload)
            throws RFXComException {
        final RFXComUndecodedRFMessage msg = (RFXComUndecodedRFMessage) RFXComMessageFactory
                .createMessage(DatatypeConverter.parseHexBinary(hexMsg));
        assertEquals("SubType", subType, msg.subType);
        assertEquals("Seq Number", seqNbr, (short) (msg.seqNbr & 0xFF));
        assertEquals("Device Id", "UNDECODED", msg.getDeviceId());
        assertEquals("Payload", rawPayload, DatatypeConverter.printHexBinary(msg.rawPayload));

        byte[] decoded = msg.decodeMessage();

        assertEquals("Message converted back", hexMsg, DatatypeConverter.printHexBinary(decoded));
    }

    @Test
    public void testSomeMessages() throws RFXComException {
        testMessage("070301271356ECC0", RFXComUndecodedRFMessage.SubType.ARC, 0x27, "1356ECC0");
    }

    @Test(expected = RFXComMessageTooLongException.class)
    public void testLongMessage() throws RFXComException {
        RFXComUndecodedRFMessage msg = (RFXComUndecodedRFMessage) RFXComMessageFactory
                .createMessage(PacketType.UNDECODED_RF_MESSAGE);
        msg.subType = RFXComUndecodedRFMessage.SubType.ARC;
        msg.seqNbr = 1;
        msg.rawPayload = DatatypeConverter
                .parseHexBinary("000102030405060708090A0B0C0D0E0F101112131415161718191A1B1C1D1E1F2021");
        msg.decodeMessage();
    }
}
