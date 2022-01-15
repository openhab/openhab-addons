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
import org.openhab.binding.rfxcom.internal.messages.RFXComSecurity2Message.SubType;
import org.openhab.core.util.HexUtils;

/**
 * Test for RFXCom-binding
 *
 * @author Martin van Wingerden - Initial contribution
 * @author Mike Jagdis - added message handling and real test
 */
@NonNullByDefault
public class RFXComSecurity2MessageTest {

    @Test
    public void testSomeMessages() throws RFXComException {
        String hexMessage = "1C21020000000000131211C30000000000000000000000000000000045";
        byte[] message = HexUtils.hexToBytes(hexMessage);

        RFXComSecurity2Message msg = (RFXComSecurity2Message) RFXComMessageFactoryImpl.INSTANCE.createMessage(message);
        assertEquals(SubType.RAW_AES_KEELOQ, msg.subType, "SubType");
        assertEquals(0, msg.seqNbr, "Seq Number");
        assertEquals("51450387", msg.getDeviceId(), "Sensor Id");
        assertEquals(12, msg.buttonStatus, "Button Status");
        assertEquals(4, msg.batteryLevel, "Battery Level");
        assertEquals(5, msg.signalLevel, "Signal Level");

        byte[] decoded = msg.decodeMessage();
        assertEquals(hexMessage, HexUtils.bytesToHex(decoded), "Message converted back");
    }
}
