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
import static org.openhab.binding.rfxcom.internal.messages.RFXComSecurity1Message.SubType.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.util.HexUtils;
import org.junit.Test;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComUnsupportedValueException;
import org.openhab.binding.rfxcom.internal.messages.RFXComSecurity1Message.Contact;
import org.openhab.binding.rfxcom.internal.messages.RFXComSecurity1Message.Motion;
import org.openhab.binding.rfxcom.internal.messages.RFXComSecurity1Message.Status;
import org.openhab.binding.rfxcom.internal.messages.RFXComSecurity1Message.SubType;

/**
 * Test for RFXCom-binding
 *
 * @author Martin van Wingerden - Initial contribution
 */
@NonNullByDefault
public class RFXComSecurity1MessageTest {
    private void testSomeMessages(String hexMessage, @Nullable SubType subType, int sequenceNumber,
            @Nullable String deviceId, int batteryLevel, @Nullable Contact contact, @Nullable Motion motion,
            @Nullable Status status, int signalLevel) throws RFXComException {
        byte[] message = HexUtils.hexToBytes(hexMessage);
        RFXComSecurity1Message msg = (RFXComSecurity1Message) RFXComMessageFactory.createMessage(message);
        assertEquals("SubType", subType, msg.subType);
        assertEquals("Seq Number", sequenceNumber, (short) (msg.seqNbr & 0xFF));
        assertEquals("Sensor Id", deviceId, msg.getDeviceId());
        assertEquals("Battery level", batteryLevel, msg.batteryLevel);
        assertEquals("Contact", contact, msg.contact);
        assertEquals("Motion", motion, msg.motion);
        assertEquals("Status", status, msg.status);
        assertEquals("Signal Level", signalLevel, msg.signalLevel);

        byte[] decoded = msg.decodeMessage();

        assertEquals("Message converted back", hexMessage, HexUtils.bytesToHex(decoded));
    }

    @Test
    public void testX10SecurityMessage() throws RFXComException {
        testSomeMessages("0820004DD3DC540089", X10_SECURITY, 77, "13884500", 8, Contact.NORMAL, Motion.UNKNOWN,
                Status.NORMAL, 9);
    }

    @Test
    public void testRM174RFSecurityMessage() throws RFXComException {
        testSomeMessages("08200A0E8000200650", RM174RF, 14, "8388640", 5, Contact.UNKNOWN, Motion.UNKNOWN, Status.PANIC,
                0);
        testSomeMessages("08200A081450450650", RM174RF, 8, "1331269", 5, Contact.UNKNOWN, Motion.UNKNOWN, Status.PANIC,
                0);
    }

    @Test(expected = RFXComUnsupportedValueException.class)
    public void testSomeInvalidSecurityMessage() throws RFXComException {
        testSomeMessages("08FF0A1F0000000650", null, 0, null, 0, null, null, null, 0);
    }
}
