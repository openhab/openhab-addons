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
import static org.openhab.binding.rfxcom.internal.messages.RFXComInterfaceMessage.Commands.*;
import static org.openhab.binding.rfxcom.internal.messages.RFXComInterfaceMessage.SubType.*;
import static org.openhab.binding.rfxcom.internal.messages.RFXComInterfaceMessage.SubType.START_RECEIVER;
import static org.openhab.binding.rfxcom.internal.messages.RFXComInterfaceMessage.TransceiverType._433_92MHZ_TRANSCEIVER;

import javax.xml.bind.DatatypeConverter;

import org.junit.Test;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;
import org.openhab.binding.rfxcom.internal.messages.RFXComInterfaceMessage.Commands;
import org.openhab.binding.rfxcom.internal.messages.RFXComInterfaceMessage.SubType;

/**
 * Test for RFXCom-binding
 *
 * @author Martin van Wingerden
 * @since 2.0.0
 */
public class RFXComInterfaceMessageTest {
    private RFXComInterfaceMessage testMessage(String hexMsg, SubType subType, int seqNbr, Commands command,
            boolean skipDecode) throws RFXComException {
        RFXComInterfaceMessage msg = (RFXComInterfaceMessage) RFXComMessageFactory
                .createMessage(DatatypeConverter.parseHexBinary(hexMsg));
        assertEquals("SubType", subType, msg.subType);
        assertEquals("Seq Number", seqNbr, (short) (msg.seqNbr & 0xFF));
        assertEquals("Command", command, msg.command);

        return msg;
    }

    @Test
    public void testWelcomeCopyRightMessage() throws RFXComException {
        RFXComInterfaceMessage msg = testMessage("1401070307436F7079726967687420524658434F4D", START_RECEIVER, 3,
                Commands.START_RECEIVER, true);

        assertEquals("text", "Copyright RFXCOM", msg.text);
    }

    @Test
    public void testRespondOnUnknownMessage() throws RFXComException {
        testMessage("0D01FF190053E2000C2701020000", UNKNOWN_COMMAND, 25, UNSUPPORTED_COMMAND, true);
    }

    @Test
    public void testStatusMessage() throws RFXComException {
        RFXComInterfaceMessage msg = testMessage("1401000102530C0800270001031C04524658434F4D", RESPONSE, 1, GET_STATUS,
                false);

        assertEquals("Command", _433_92MHZ_TRANSCEIVER, msg.transceiverType);

        // TODO this is not correct, improvements for this have been made in the OH1 repo
        assertEquals("firmwareVersion", 12, msg.firmwareVersion);
    }
}
