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
import org.openhab.binding.rfxcom.internal.messages.RFXComChimeMessage.SubType;
import org.openhab.core.util.HexUtils;

/**
 * Test for RFXCom-binding
 *
 * @author Martin van Wingerden - Initial contribution
 * @author Mike Jagdis - Added actual functional tests
 */
@NonNullByDefault
public class RFXComChimeMessageTest {

    @Test
    public void testSomeMessages() throws RFXComException {
        String hexMessage = "0716020900A1F350";
        byte[] message = HexUtils.hexToBytes(hexMessage);
        RFXComChimeMessage msg = (RFXComChimeMessage) RFXComMessageFactoryImpl.INSTANCE.createMessage(message);
        assertEquals(SubType.SELECTPLUS, msg.subType, "SubType");
        assertEquals(9, msg.seqNbr, "Seq Number");
        assertEquals("41459", msg.getDeviceId(), "Sensor Id");
        assertEquals(5, msg.signalLevel, "Signal Level");

        byte[] decoded = msg.decodeMessage();

        assertEquals(hexMessage, HexUtils.bytesToHex(decoded), "Message converted back");
    }
}
