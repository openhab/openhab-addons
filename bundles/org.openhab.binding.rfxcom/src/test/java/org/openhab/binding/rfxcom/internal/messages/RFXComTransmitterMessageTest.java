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
import static org.openhab.binding.rfxcom.internal.messages.RFXComTransmitterMessage.Response.ACK;
import static org.openhab.binding.rfxcom.internal.messages.RFXComTransmitterMessage.SubType.RESPONSE;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;
import org.openhab.binding.rfxcom.internal.messages.RFXComTransmitterMessage.Response;
import org.openhab.binding.rfxcom.internal.messages.RFXComTransmitterMessage.SubType;
import org.openhab.core.util.HexUtils;

/**
 * Test for RFXCom-binding
 *
 * @author Martin van Wingerden - Initial contribution
 */
@NonNullByDefault
public class RFXComTransmitterMessageTest {
    private void testMessage(String hexMsg, Response response, SubType subType, int seqNbr) throws RFXComException {
        final RFXComTransmitterMessage msg = (RFXComTransmitterMessage) RFXComMessageFactoryImpl.INSTANCE
                .createMessage(HexUtils.hexToBytes(hexMsg));
        assertEquals(subType, msg.subType, "SubType");
        assertEquals(response, msg.response, "Response");
        assertEquals(seqNbr, (short) (msg.seqNbr & 0xFF), "Seq Number");

        byte[] decoded = msg.decodeMessage();

        assertEquals(hexMsg, HexUtils.bytesToHex(decoded), "Message converted back");
    }

    @Test
    public void testSomeMessages() throws RFXComException {
        testMessage("0402014300", ACK, RESPONSE, 67);
    }
}
