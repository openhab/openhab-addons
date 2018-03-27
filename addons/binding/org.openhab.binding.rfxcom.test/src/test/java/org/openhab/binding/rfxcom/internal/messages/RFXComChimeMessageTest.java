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
import org.openhab.binding.rfxcom.internal.messages.RFXComChimeMessage.SubType;


/**
 * Test for RFXCom-binding
 *
 * @author Mike Jagdis
 * @author Martin van Wingerden
 * @since 1.9.0
 */
public class RFXComChimeMessageTest {

    @Test
    public void testSomeMessages() throws RFXComException {
        String hexMessage = "0716020900A1F350";
        byte[] message = DatatypeConverter.parseHexBinary(hexMessage);
        RFXComChimeMessage msg = (RFXComChimeMessage) RFXComMessageFactory.createMessage(message);
        assertEquals("SubType", SubType.SELECTPLUS, msg.subType);
        assertEquals("Seq Number", 9, msg.seqNbr);
        assertEquals("Sensor Id", "41459", msg.getDeviceId());
        assertEquals("Signal Level", 5, msg.signalLevel);

        byte[] decoded = msg.decodeMessage();

        assertEquals("Message converted back", hexMessage, DatatypeConverter.printHexBinary(decoded));
    }
}
