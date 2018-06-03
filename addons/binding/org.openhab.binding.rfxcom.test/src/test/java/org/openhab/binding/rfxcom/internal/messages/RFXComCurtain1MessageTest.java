/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.rfxcom.internal.messages;

import static org.openhab.binding.rfxcom.internal.messages.RFXComBaseMessage.PacketType.CURTAIN1;

import org.junit.Test;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;

/**
 * Test for RFXCom-binding
 *
 * @author Martin van Wingerden
 * @since 1.9.0
 */
public class RFXComCurtain1MessageTest {
    @Test
    public void checkForSupportTest() throws RFXComException {
        RFXComMessageFactory.createMessage(CURTAIN1);
    }

    @Test
    public void basicBoundaryCheck() throws RFXComException {
        RFXComCurtain1Message message = (RFXComCurtain1Message) RFXComMessageFactory.createMessage(CURTAIN1);

        message.subType = RFXComCurtain1Message.SubType.HARRISON;
        message.command = RFXComCurtain1Message.Commands.OPEN;

        RFXComTestHelper.basicBoundaryCheck(CURTAIN1, message);
    }

    // TODO please add tests for real messages
}
