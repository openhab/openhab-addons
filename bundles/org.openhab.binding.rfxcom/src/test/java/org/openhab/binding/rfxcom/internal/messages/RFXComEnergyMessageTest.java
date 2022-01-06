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
import static org.openhab.binding.rfxcom.internal.messages.RFXComEnergyMessage.SubType.ELEC2;

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
public class RFXComEnergyMessageTest {
    @Test
    public void testSomeMessages() throws RFXComException {
        String hexMessage = "115A01071A7300000003F600000000350B89";
        byte[] message = HexUtils.hexToBytes(hexMessage);
        RFXComEnergyMessage msg = (RFXComEnergyMessage) RFXComMessageFactoryImpl.INSTANCE.createMessage(message);
        assertEquals(ELEC2, msg.subType, "SubType");
        assertEquals(7, msg.seqNbr, "Seq Number");
        assertEquals("6771", msg.getDeviceId(), "Sensor Id");
        assertEquals(0, msg.count, "Count");
        assertEquals(1014d / 230, msg.instantAmp, 0.01, "Instant usage");
        assertEquals(60.7d / 230, msg.totalAmpHour, 0.01, "Total usage");
        assertEquals((byte) 8, msg.signalLevel, "Signal Level");
        assertEquals((byte) 9, msg.batteryLevel, "Battery Level");

        byte[] decoded = msg.decodeMessage();

        assertEquals(hexMessage, HexUtils.bytesToHex(decoded), "Message converted back");
    }
}
