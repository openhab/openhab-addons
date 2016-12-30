/**
 * Copyright (c) 2010-2016 by the respective copyright holders.
 * <p>
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.rfxcom.internal.messages;

import static org.junit.Assert.assertEquals;

import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComNotImpException;
import org.openhab.binding.rfxcom.internal.messages.RFXComBaseMessage.PacketType;

/**
 * Helper class for testing the RFXCom-binding
 *
 * @author Martin van Wingerden
 * @since 1.9.0
 */
public class RFXComTestHelper {
    static void basicBoundaryCheck(PacketType packetType) throws RFXComException, RFXComNotImpException {
        RFXComMessage intf = RFXComMessageFactory.createMessage(packetType);

        // This is a place where its easy to make mistakes in coding, and can result in errors, normally
        // array bounds errors
        byte[] message = intf.decodeMessage();
        assertEquals("Wrong packet length", message[0], message.length - 1);
        assertEquals("Wrong packet type", packetType.toByte(), message[1]);
    }
}
