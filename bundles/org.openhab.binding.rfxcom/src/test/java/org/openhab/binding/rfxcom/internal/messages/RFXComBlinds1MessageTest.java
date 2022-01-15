/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
import org.openhab.binding.rfxcom.internal.messages.RFXComBlinds1Message.Commands;
import org.openhab.binding.rfxcom.internal.messages.RFXComBlinds1Message.SubType;
import org.openhab.core.util.HexUtils;

/**
 * Test for RFXCom-binding
 *
 * @author Martin van Wingerden - Initial contribution
 */
@NonNullByDefault
public class RFXComBlinds1MessageTest {
    private void testMessage(String hexMsg, SubType subType, int seqNbr, String deviceId, int signalLevel,
            RFXComBlinds1Message.Commands command) throws RFXComException {
        final RFXComBlinds1Message msg = (RFXComBlinds1Message) RFXComMessageFactoryImpl.INSTANCE
                .createMessage(HexUtils.hexToBytes(hexMsg));
        assertEquals(subType, msg.subType, "SubType");
        assertEquals(seqNbr, (short) (msg.seqNbr & 0xFF), "Seq Number");
        assertEquals(deviceId, msg.getDeviceId(), "Sensor Id");
        assertEquals(command, msg.command, "Command");
        assertEquals(signalLevel, msg.signalLevel, "Signal Level");

        byte[] decoded = msg.decodeMessage();

        assertEquals(hexMsg, HexUtils.bytesToHex(decoded), "Message converted back");
    }

    @Test
    public void testSomeMessages() throws RFXComException {
        testMessage("0919040600A21B010280", SubType.T4, 6, "41499.1", 8, Commands.STOP);

        testMessage("091905021A6280010000", SubType.T5, 2, "1729152.1", 0, Commands.OPEN);
    }
}
