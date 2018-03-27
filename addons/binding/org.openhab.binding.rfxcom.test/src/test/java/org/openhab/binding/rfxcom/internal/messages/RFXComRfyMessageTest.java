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
import static org.openhab.binding.rfxcom.internal.messages.RFXComBaseMessage.PacketType.RFY;

import javax.xml.bind.DatatypeConverter;

import org.junit.Test;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;
import org.openhab.binding.rfxcom.internal.messages.RFXComRfyMessage.Commands;
import org.openhab.binding.rfxcom.internal.messages.RFXComRfyMessage.SubType;

/**
 * Test for RFXCom-binding
 *
 * @author Martin van Wingerden
 * @since 1.9.0
 */
public class RFXComRfyMessageTest {

    @Test
    public void basicBoundaryCheck() throws RFXComException {
        RFXComRfyMessage message = (RFXComRfyMessage) RFXComMessageFactory.createMessage(RFY);

        message.subType = SubType.RFY;
        message.command = Commands.UP;

        RFXComTestHelper.basicBoundaryCheck(RFY, message);
    }

    private void testMessage(SubType subType, Commands command, String deviceId, String data)
            throws RFXComException {

        RFXComRfyMessage message = (RFXComRfyMessage) RFXComMessageFactory.createMessage(RFY);
        message.setSubType(subType);
	message.command = command;
        message.setDeviceId(deviceId);

        assertArrayEquals(DatatypeConverter.parseHexBinary(data), message.decodeMessage());
    }

    @Test
    public void testMessage1() throws RFXComException {
        testMessage(SubType.RFY, Commands.UP_SHORT, "66051.4", "0C1A0000010203040F00000000");
    }

    @Test
    public void testMessage2() throws RFXComException {
        testMessage(SubType.ASA, Commands.DOWN_LONG, "66051.4", "0C1A0300010203041200000000");
    }
}
