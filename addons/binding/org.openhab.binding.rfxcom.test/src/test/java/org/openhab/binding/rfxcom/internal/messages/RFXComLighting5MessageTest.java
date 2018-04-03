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
import static org.openhab.binding.rfxcom.RFXComBindingConstants.CHANNEL_COMMAND;
import static org.openhab.binding.rfxcom.internal.messages.RFXComBaseMessage.PacketType.LIGHTING5;
import static org.openhab.binding.rfxcom.internal.messages.RFXComLighting5Message.Commands.ON;
import static org.openhab.binding.rfxcom.internal.messages.RFXComLighting5Message.SubType.IT;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.util.HexUtils;
import org.junit.Test;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;

/**
 * Test for RFXCom-binding
 *
 * @author Martin van Wingerden
 * @since 1.9.0
 */
public class RFXComLighting5MessageTest {

    @Test
    public void convertFromStateItMessage() throws RFXComException {
        RFXComDeviceMessage itMessageObject = (RFXComDeviceMessage) RFXComMessageFactory.createMessage(LIGHTING5);
        itMessageObject.setDeviceId("2061.1");
        itMessageObject.setSubType(IT);
        itMessageObject.convertFromState(CHANNEL_COMMAND, OnOffType.ON);
        byte[] message = itMessageObject.decodeMessage();
        String hexMessage = HexUtils.bytesToHex(message);
        assertEquals("Message is not as expected", "0A140F0000080D01010000", hexMessage);
        RFXComLighting5Message msg = (RFXComLighting5Message) RFXComMessageFactory.createMessage(message);
        assertEquals("SubType", IT, msg.subType);
        assertEquals("Sensor Id", "2061.1", msg.getDeviceId());
        assertEquals("Command", ON, msg.command);
    }

    @Test
    public void basicBoundaryCheck() throws RFXComException {
        RFXComLighting5Message message = (RFXComLighting5Message) RFXComMessageFactory.createMessage(LIGHTING5);

        message.subType = RFXComLighting5Message.SubType.LIGHTWAVERF;
        message.command = ON;

        RFXComTestHelper.basicBoundaryCheck(LIGHTING5, message);
    }

    // TODO please add more tests for different messages
}
