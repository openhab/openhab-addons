/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
public class RFXComLighting2MessageTest {

    @Test
    public void testSomeMessages() throws RFXComException {
        String hexMessage = "0B11000600109B520B000080";
        byte[] message = HexUtils.hexToBytes(hexMessage);
        RFXComLighting2Message msg = (RFXComLighting2Message) RFXComMessageFactoryImpl.INSTANCE.createMessage(message);
        assertEquals(RFXComLighting2Message.SubType.AC, msg.subType, "SubType");
        assertEquals(6, (short) (msg.seqNbr & 0xFF), "Seq Number");
        assertEquals("1088338.11", msg.getDeviceId(), "Sensor Id");
        assertEquals(RFXComLighting2Message.Commands.OFF, msg.command, "Command");
        assertEquals((byte) 8, msg.signalLevel, "Signal Level");

        byte[] decoded = msg.decodeMessage();

        assertEquals(hexMessage, HexUtils.bytesToHex(decoded), "Message converted back");
    }
}
