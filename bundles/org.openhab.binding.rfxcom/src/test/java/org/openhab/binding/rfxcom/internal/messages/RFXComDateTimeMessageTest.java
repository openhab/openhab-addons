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
import static org.openhab.binding.rfxcom.internal.RFXComBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.rfxcom.internal.RFXComTestHelper;
import org.openhab.binding.rfxcom.internal.config.RFXComGenericDeviceConfiguration;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.util.HexUtils;

/**
 * Test for RFXCom-binding
 *
 * @author Martin van Wingerden - Initial contribution
 */
@NonNullByDefault
public class RFXComDateTimeMessageTest {
    @Test
    public void testSomeMessages() throws RFXComException {
        RFXComGenericDeviceConfiguration config = new RFXComGenericDeviceConfiguration();
        config.deviceId = "47360";
        config.subType = RFXComDateTimeMessage.SubType.RTGR328N.toString();

        String hexMessage = "0D580117B90003041D030D150A69";
        byte[] message = HexUtils.hexToBytes(hexMessage);
        RFXComDateTimeMessage msg = (RFXComDateTimeMessage) RFXComMessageFactoryImpl.INSTANCE.createMessage(message);
        assertEquals(RFXComDateTimeMessage.SubType.RTGR328N, msg.subType, "SubType");
        assertEquals(23, (short) (msg.seqNbr & 0xFF), "Seq Number");
        assertEquals("47360", msg.getDeviceId(), "Sensor Id");
        assertEquals("2003-04-29T13:21:10", msg.dateTime, "Date time");
        assertEquals(2, RFXComTestHelper.getActualIntValue(msg, config, CHANNEL_SIGNAL_LEVEL), "Signal Level");

        assertEquals(DateTimeType.valueOf("2003-04-29T13:21:10"),
                msg.convertToState(CHANNEL_DATE_TIME, config, new MockDeviceState()), "Converted value");

        byte[] decoded = msg.decodeMessage();

        assertEquals(hexMessage, HexUtils.bytesToHex(decoded), "Message converted back");
    }
}
