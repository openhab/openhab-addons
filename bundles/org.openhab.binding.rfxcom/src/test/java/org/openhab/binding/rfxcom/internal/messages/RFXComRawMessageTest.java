/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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

import static org.junit.jupiter.api.Assertions.*;

import java.nio.ByteBuffer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComMessageTooLongException;
import org.openhab.binding.rfxcom.internal.messages.RFXComBaseMessage.PacketType;
import org.openhab.core.util.HexUtils;

/**
 * Test for RFXCom-binding
 *
 * @author James Hewitt-Thomas - New addition to the PRO RFXCom firmware
 */
@NonNullByDefault
public class RFXComRawMessageTest {

    private void testMessage(String hexMsg, RFXComRawMessage.SubType subType, int seqNbr, int repeat, String pulses)
            throws RFXComException {
        final RFXComRawMessage msg = (RFXComRawMessage) RFXComMessageFactory.createMessage(HexUtils.hexToBytes(hexMsg));
        assertEquals(subType, msg.subType, "SubType");
        assertEquals(seqNbr, (short) (msg.seqNbr & 0xFF), "Seq Number");
        assertEquals("RAW", msg.getDeviceId(), "Device Id");
        assertEquals(repeat, msg.repeat, "Repeat");
        byte[] payload = new byte[msg.pulses.length * 2];
        ByteBuffer.wrap(payload).asShortBuffer().put(msg.pulses);
        assertEquals(pulses, HexUtils.bytesToHex(payload), "Pulses");

        byte[] decoded = msg.decodeMessage();

        assertEquals(hexMsg, HexUtils.bytesToHex(decoded), "Message converted back");
    }

    @Test
    public void testSomeMessages() throws RFXComException {
        testMessage("087F0027051356ECC0", RFXComRawMessage.SubType.RAW_PACKET1, 0x27, 5, "1356ECC0");
    }

    @Test
    public void testLongMessage() throws RFXComException {
        RFXComRawMessage msg = (RFXComRawMessage) RFXComMessageFactory.createMessage(PacketType.RAW);
        msg.subType = RFXComRawMessage.SubType.RAW_PACKET1;
        msg.seqNbr = 1;
        msg.repeat = 5;
        msg.pulses = new short[125];

        assertThrows(RFXComMessageTooLongException.class, () -> msg.decodeMessage());
    }
}
