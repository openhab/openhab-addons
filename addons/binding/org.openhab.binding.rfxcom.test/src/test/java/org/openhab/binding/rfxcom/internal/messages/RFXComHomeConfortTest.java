/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.rfxcom.internal.messages;

import static org.junit.Assert.assertArrayEquals;
import static org.openhab.binding.rfxcom.internal.messages.RFXComBaseMessage.PacketType.HOME_CONFORT;

import javax.xml.bind.DatatypeConverter;

import org.junit.Test;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;
import org.openhab.binding.rfxcom.internal.messages.RFXComHomeConfortMessage.Commands;
import org.openhab.binding.rfxcom.internal.messages.RFXComHomeConfortMessage.SubType;


/**
 * Test for RFXCom-binding
 *
 * @author Martin van Wingerden - Initial contribution of empty test
 * @author Mike Jagdis - added message handling and real test
 * @since 2.0.0
 */
public class RFXComHomeConfortTest {
    private void testMessage(SubType subType, Commands command, String deviceId, String data)
            throws RFXComException {

        RFXComHomeConfortMessage message = (RFXComHomeConfortMessage) RFXComMessageFactory.createMessage(HOME_CONFORT);
        message.setSubType(subType);
	message.command = command;
        message.setDeviceId(deviceId);

        assertArrayEquals(DatatypeConverter.parseHexBinary(data), message.decodeMessage());
    }

    @Test
    public void testMessage1() throws RFXComException {
        testMessage(SubType.TEL_010, Commands.GROUP_ON, "1118739.A.4", "0C1B0000111213410403000000");
    }
}
