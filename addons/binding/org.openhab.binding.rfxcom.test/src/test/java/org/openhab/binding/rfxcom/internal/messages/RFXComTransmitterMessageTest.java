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
import static org.openhab.binding.rfxcom.internal.messages.RFXComTransmitterMessage.Response.ACK;
import static org.openhab.binding.rfxcom.internal.messages.RFXComTransmitterMessage.SubType.RESPONSE;

import org.eclipse.smarthome.core.util.HexUtils;
import org.junit.Test;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;
import org.openhab.binding.rfxcom.internal.messages.RFXComTransmitterMessage.Response;
import org.openhab.binding.rfxcom.internal.messages.RFXComTransmitterMessage.SubType;

/**
 * Test for RFXCom-binding
 *
 * @author Martin van Wingerden
 * @since 1.9.0
 */
public class RFXComTransmitterMessageTest {
    private void testMessage(String hexMsg, Response response, SubType subType, int seqNbr) throws RFXComException {
        final RFXComTransmitterMessage msg = (RFXComTransmitterMessage) RFXComMessageFactory
                .createMessage(HexUtils.hexToBytes(hexMsg));
        assertEquals("SubType", subType, msg.subType);
        assertEquals("Response", response, msg.response);
        assertEquals("Seq Number", seqNbr, (short) (msg.seqNbr & 0xFF));

        byte[] decoded = msg.decodeMessage();

        assertEquals("Message converted back", hexMsg, HexUtils.bytesToHex(decoded));
    }

    @Test
    public void testSomeMessages() throws RFXComException {
        testMessage("0402014300", ACK, RESPONSE, 67);
    }
}
