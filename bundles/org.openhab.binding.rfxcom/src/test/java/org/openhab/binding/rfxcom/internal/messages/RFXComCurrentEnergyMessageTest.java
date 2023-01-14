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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;
import org.openhab.core.util.HexUtils;

/**
 * Test for RFXCom-binding
 *
 * @author Martin van Wingerden - Initial contribution
 */
@NonNullByDefault
public class RFXComCurrentEnergyMessageTest {
    private void testMessage(String hexMsg, RFXComCurrentEnergyMessage.SubType subType, int seqNbr, String deviceId,
            int count, double channel1, double channel2, double channel3, double totalUsage, int signalLevel,
            int batteryLevel) throws RFXComException {
        final RFXComCurrentEnergyMessage msg = (RFXComCurrentEnergyMessage) RFXComMessageFactoryImpl.INSTANCE
                .createMessage(HexUtils.hexToBytes(hexMsg));
        assertEquals(subType, msg.subType, "SubType");
        assertEquals(seqNbr, (short) (msg.seqNbr & 0xFF), "Seq Number");
        assertEquals(deviceId, msg.getDeviceId(), "Sensor Id");
        assertEquals(count, msg.count, "Count");
        assertEquals(channel1, msg.channel1Amps, 0.01, "Channel 1");
        assertEquals(channel2, msg.channel2Amps, 0.01, "Channel 2");
        assertEquals(channel3, msg.channel3Amps, 0.01, "Channel 3");
        assertEquals(totalUsage, msg.totalUsage, 0.05, "Total usage");
        assertEquals(signalLevel, msg.signalLevel, "Signal Level");
        assertEquals(batteryLevel, msg.batteryLevel, "Battery Level");

        byte[] decoded = msg.decodeMessage();

        assertEquals(hexMsg, HexUtils.bytesToHex(decoded), "Message converted back");
    }

    @Test
    public void testSomeMessages() throws RFXComException {
        testMessage("135B0106B800000016000000000000006F148889", RFXComCurrentEnergyMessage.SubType.ELEC4, 6, "47104", 0,
                2.2d, 0d, 0d, 32547.4d, 8, 9);
        testMessage("135B014FB80002001D0000000000000000000079", RFXComCurrentEnergyMessage.SubType.ELEC4, 79, "47104",
                2, 2.9d, 0d, 0d, 0d, 7, 9);
    }
}
