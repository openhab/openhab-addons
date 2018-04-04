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

import org.eclipse.smarthome.core.util.HexUtils;
import org.junit.Test;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;
import org.openhab.binding.rfxcom.internal.messages.RFXComSecurity2Message.SubType;

/**
 * Test for RFXCom-binding
 *
 * @author Martin van Wingerden - Initial contribution of empty test
 * @author Mike Jagdis - added message handling and real test
 * @since 2.0.0
 */
public class RFXComSecurity2MessageTest {

    @Test
    public void testSomeMessages() throws RFXComException {
        String hexMessage = "1C21020000000000131211C30000000000000000000000000000000045";
        byte[] message = HexUtils.hexToBytes(hexMessage);

        RFXComSecurity2Message msg = (RFXComSecurity2Message) RFXComMessageFactory.createMessage(message);
        assertEquals("SubType", SubType.RAW_AES_KEELOQ, msg.subType);
        assertEquals("Seq Number", 0, msg.seqNbr);
        assertEquals("Sensor Id", "51450387", msg.getDeviceId());
        assertEquals("Button Status", 12, msg.buttonStatus);
        assertEquals("Battery Level", 4, msg.batteryLevel);
        assertEquals("Signal Level", 5, msg.signalLevel);

        byte[] decoded = msg.decodeMessage();
        assertEquals("Message converted back", hexMessage, HexUtils.bytesToHex(decoded));
    }
}
