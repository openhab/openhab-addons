/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.rfxcom.internal.messages;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.openhab.binding.rfxcom.internal.messages.RFXComBBQTemperatureMessage.SubType.BBQ1;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;
import org.openhab.core.util.HexUtils;

/**
 * Test for RFXCom-binding
 *
 * @author Mike Jagdis - Initial contribution
 */
@NonNullByDefault
public class RFXComBBQTemperatureMessageTest {
    @Test
    public void testSomeMessages() throws RFXComException {
        String hexMessage = "0A4E012B2955001A002179";
        byte[] message = HexUtils.hexToBytes(hexMessage);
        RFXComBBQTemperatureMessage msg = (RFXComBBQTemperatureMessage) RFXComMessageFactoryImpl.INSTANCE
                .createMessage(message);
        assertEquals(BBQ1, msg.subType, "SubType");
        assertEquals(43, msg.seqNbr, "Seq Number");
        assertEquals("10581", msg.getDeviceId(), "Sensor Id");
        assertEquals(26, msg.foodTemperature, 0.1, "Food Temperature");
        assertEquals(33, msg.bbqTemperature, 0.1, "BBQ Temperature");
        assertEquals(7, msg.signalLevel, "Signal Level");
        assertEquals(9, msg.batteryLevel, "Battery Level");

        byte[] decoded = msg.decodeMessage();

        assertEquals(hexMessage, HexUtils.bytesToHex(decoded), "Message converted back");
    }
}
