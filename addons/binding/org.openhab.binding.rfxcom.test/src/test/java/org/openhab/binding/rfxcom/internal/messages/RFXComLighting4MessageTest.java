/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.rfxcom.internal.messages;

import static org.openhab.binding.rfxcom.internal.messages.RFXComBaseMessage.PacketType.LIGHTING4;

import org.junit.Test;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;

/**
 * Test for RFXCom-binding
 *
 * @author Martin van Wingerden
 * @since 1.9.0
 */
public class RFXComLighting4MessageTest {
    @Test
    public void checkForSupportTest() throws RFXComException {
        RFXComMessageFactory.createMessage(LIGHTING4);
    }

    @Test
    public void basicBoundaryCheck() throws RFXComException {
        RFXComLighting4Message message = (RFXComLighting4Message) RFXComMessageFactory.createMessage(LIGHTING4);

        message.subType = RFXComLighting4Message.SubType.PT2262;
        message.command = RFXComLighting4Message.Commands.ON;

        RFXComTestHelper.basicBoundaryCheck(LIGHTING4, message);
    }

    // TODO please add tests for real messages
}
