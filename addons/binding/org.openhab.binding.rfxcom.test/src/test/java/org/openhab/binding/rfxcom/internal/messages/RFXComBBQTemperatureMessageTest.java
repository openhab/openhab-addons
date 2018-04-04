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
import static org.openhab.binding.rfxcom.internal.messages.RFXComBBQTemperatureMessage.SubType.BBQ1;

import org.eclipse.smarthome.core.util.HexUtils;
import org.junit.Test;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;

/**
 * Test for RFXCom-binding
 *
 * @author Mike Jagdis
 * @since 2.2.0
 */
public class RFXComBBQTemperatureMessageTest {
    @Test
    public void testSomeMessages() throws RFXComException {
        String hexMessage = "0A4E012B2955001A002179";
        byte[] message = HexUtils.hexToBytes(hexMessage);
        RFXComBBQTemperatureMessage msg = (RFXComBBQTemperatureMessage) RFXComMessageFactory.createMessage(message);
        assertEquals("SubType", BBQ1, msg.subType);
        assertEquals("Seq Number", 43, msg.seqNbr);
        assertEquals("Sensor Id", "10581", msg.getDeviceId());
        assertEquals("Food Temperature", 26, msg.foodTemperature, 0.1);
        assertEquals("BBQ Temperature", 33, msg.bbqTemperature, 0.1);
        assertEquals("Signal Level", 7, msg.signalLevel);
        assertEquals("Battery Level", 9, msg.batteryLevel);

        byte[] decoded = msg.decodeMessage();

        assertEquals("Message converted back", hexMessage, HexUtils.bytesToHex(decoded));
    }
}
