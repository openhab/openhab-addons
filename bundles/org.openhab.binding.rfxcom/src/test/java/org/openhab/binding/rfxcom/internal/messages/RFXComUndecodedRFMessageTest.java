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

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComMessageTooLongException;
import org.openhab.core.util.HexUtils;

/**
 * Test for RFXCom-binding
 *
 * @author Martin van Wingerden - Initial contribution
 * @author James Hewitt-Thomas - Added first actual tests
 */
@NonNullByDefault
public class RFXComUndecodedRFMessageTest {

    private void testMessage(String hexMsg, RFXComUndecodedRFMessage.SubType subType, int seqNbr, String rawPayload)
            throws RFXComException {
        final RFXComUndecodedRFMessage msg = (RFXComUndecodedRFMessage) RFXComMessageFactoryImpl.INSTANCE
                .createMessage(HexUtils.hexToBytes(hexMsg));
        assertEquals(subType, msg.subType, "SubType");
        assertEquals(seqNbr, (short) (msg.seqNbr & 0xFF), "Seq Number");
        assertEquals("UNDECODED", msg.getDeviceId(), "Device Id");
        assertEquals(rawPayload, HexUtils.bytesToHex(msg.rawPayload), "Payload");

        byte[] decoded = msg.decodeMessage();

        assertEquals(hexMsg, HexUtils.bytesToHex(decoded), "Message converted back");
    }

    @Test
    public void testSomeMessages() throws RFXComException {
        testMessage("070301271356ECC0", RFXComUndecodedRFMessage.SubType.ARC, 0x27, "1356ECC0");
    }

    @Test
    public void testLongMessage() throws RFXComException {
        assertThrows(RFXComMessageTooLongException.class,
                () -> testMessage("25030101000102030405060708090A0B0C0D0E0F101112131415161718191A1B1C1D1E1F2021",
                        RFXComUndecodedRFMessage.SubType.ARC, 0x01,
                        "000102030405060708090A0B0C0D0E0F101112131415161718191A1B1C1D1E1F2021"));
    }
}
