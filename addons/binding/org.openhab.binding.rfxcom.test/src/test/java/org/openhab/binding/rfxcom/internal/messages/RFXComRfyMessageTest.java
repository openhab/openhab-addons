/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.rfxcom.internal.messages;

import static org.openhab.binding.rfxcom.internal.messages.RFXComBaseMessage.PacketType.RFY;

import org.junit.Test;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;

/**
 * Test for RFXCom-binding
 *
 * @author Martin van Wingerden
 * @since 1.9.0
 */
public class RFXComRfyMessageTest {
    @Test
    public void checkForSupportTest() throws RFXComException {
        RFXComMessageFactory.createMessage(RFY);
    }

    @Test
    public void basicBoundaryCheck() throws RFXComException {
        RFXComRfyMessage message = (RFXComRfyMessage) RFXComMessageFactory.createMessage(RFY);

        message.subType = RFXComRfyMessage.SubType.RFY;
        message.command = RFXComRfyMessage.Commands.OPEN;

        RFXComTestHelper.basicBoundaryCheck(RFY, message);
    }

    // TODO please add tests for real messages
}
