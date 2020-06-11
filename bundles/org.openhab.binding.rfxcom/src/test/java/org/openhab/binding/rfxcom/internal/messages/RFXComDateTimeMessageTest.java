/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import static org.junit.Assert.assertEquals;
import static org.openhab.binding.rfxcom.internal.RFXComBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.util.HexUtils;
import org.junit.Test;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;

/**
 * Test for RFXCom-binding
 *
 * @author Martin van Wingerden - Initial contribution
 */
@NonNullByDefault
public class RFXComDateTimeMessageTest {
    @Test
    public void testSomeMessages() throws RFXComException {
        String hexMessage = "0D580117B90003041D030D150A69";
        byte[] message = HexUtils.hexToBytes(hexMessage);
        RFXComDateTimeMessage msg = (RFXComDateTimeMessage) RFXComMessageFactory.createMessage(message);
        assertEquals("SubType", RFXComDateTimeMessage.SubType.RTGR328N, msg.subType);
        assertEquals("Seq Number", 23, (short) (msg.seqNbr & 0xFF));
        assertEquals("Sensor Id", "47360", msg.getDeviceId());
        assertEquals("Date time", "2003-04-29T13:21:10", msg.dateTime);
        assertEquals("Signal Level", 2, RFXComTestHelper.getActualIntValue(msg, CHANNEL_SIGNAL_LEVEL));

        assertEquals("Converted value", DateTimeType.valueOf("2003-04-29T13:21:10"),
                msg.convertToState(CHANNEL_DATE_TIME, new MockDeviceState()));

        byte[] decoded = msg.decodeMessage();

        assertEquals("Message converted back", hexMessage, HexUtils.bytesToHex(decoded));
    }
}
