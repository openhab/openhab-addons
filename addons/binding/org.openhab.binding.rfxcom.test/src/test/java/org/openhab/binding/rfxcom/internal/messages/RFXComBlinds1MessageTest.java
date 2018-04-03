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
import org.openhab.binding.rfxcom.internal.messages.RFXComBlinds1Message.Commands;
import org.openhab.binding.rfxcom.internal.messages.RFXComBlinds1Message.SubType;

/**
 * Test for RFXCom-binding
 *
 * @author Martin van Wingerden
 * @since 1.9.0
 */
public class RFXComBlinds1MessageTest {
    private void testMessage(String hexMsg, SubType subType, int seqNbr, String deviceId, int signalLevel,
            RFXComBlinds1Message.Commands command) throws RFXComException {
        final RFXComBlinds1Message msg = (RFXComBlinds1Message) RFXComMessageFactory
                .createMessage(HexUtils.hexToBytes(hexMsg));
        assertEquals("SubType", subType, msg.subType);
        assertEquals("Seq Number", seqNbr, (short) (msg.seqNbr & 0xFF));
        assertEquals("Sensor Id", deviceId, msg.getDeviceId());
        assertEquals("Command", command, msg.command);
        assertEquals("Signal Level", signalLevel, msg.signalLevel);

        byte[] decoded = msg.decodeMessage();

        assertEquals("Message converted back", hexMsg, HexUtils.bytesToHex(decoded));
    }

    @Test
    public void testSomeMessages() throws RFXComException {
        testMessage("0919040600A21B010280", SubType.T4, 6, "41499.1", 8, Commands.STOP);

        testMessage("091905021A6280010000", SubType.T5, 2, "1729152.1", 0, Commands.OPEN);
    }
}
