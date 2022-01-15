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
import static org.openhab.binding.rfxcom.internal.messages.RFXComRainMessage.SubType.RAIN2;

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
public class RFXComRainMessageTest {

    @Test
    public void testSomeMessages() throws RFXComException {
        String hexMessage = "0B550217B6000000004D3C69";
        byte[] message = HexUtils.hexToBytes(hexMessage);
        RFXComRainMessage msg = (RFXComRainMessage) RFXComMessageFactoryImpl.INSTANCE.createMessage(message);
        assertEquals(RAIN2, msg.subType, "SubType");
        assertEquals(23, msg.seqNbr, "Seq Number");
        assertEquals("46592", msg.getDeviceId(), "Sensor Id");
        assertEquals(0.0, msg.rainRate, 0.001, "Rain rate");
        assertEquals(1977.2, msg.rainTotal, 0.001, "Total rain");
        assertEquals(6, msg.signalLevel, "Signal Level");
        assertEquals(9, msg.batteryLevel, "Battery Level");

        byte[] decoded = msg.decodeMessage();

        assertEquals(hexMessage, HexUtils.bytesToHex(decoded), "Message converted back");
    }
}
