/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.util.HexUtils;
import org.junit.Test;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComUnsupportedChannelException;
import org.openhab.binding.rfxcom.internal.messages.RFXComLighting1Message.Commands;

/**
 * Test for RFXCom-binding
 *
 * @author Martin van Wingerden - Initial contribution
 */
@NonNullByDefault
public class RFXComLighting1MessageTest {
    private final MockDeviceState deviceState = new MockDeviceState();

    private void testMessage(String hexMsg, RFXComLighting1Message.SubType subType, int seqNbr, String deviceId,
            byte signalLevel, Commands command, String commandString) throws RFXComException {
        final RFXComLighting1Message msg = (RFXComLighting1Message) RFXComMessageFactory
                .createMessage(HexUtils.hexToBytes(hexMsg));
        assertEquals("SubType", subType, msg.subType);
        assertEquals("Seq Number", seqNbr, (short) (msg.seqNbr & 0xFF));
        assertEquals("Sensor Id", deviceId, msg.getDeviceId());
        assertEquals("Signal Level", signalLevel, msg.signalLevel);
        assertEquals("Command", command, msg.command);
        assertEquals("Command String", commandString,
                msg.convertToState(CHANNEL_COMMAND_STRING, deviceState).toString());

        byte[] decoded = msg.decodeMessage();

        assertEquals("Message converted back", hexMsg, HexUtils.bytesToHex(decoded));
    }

    @Test
    public void testSomeMessages() throws RFXComException {
        testMessage("0710015242080780", RFXComLighting1Message.SubType.ARC, 82, "B.8", (byte) 8, Commands.CHIME,
                "CHIME");

        testMessage("0710010047010070", RFXComLighting1Message.SubType.ARC, 0, "G.1", (byte) 7, Commands.OFF, "OFF");
        testMessage("071001014D090160", RFXComLighting1Message.SubType.ARC, 1, "M.9", (byte) 6, Commands.ON, "ON");
        testMessage("0710010543080060", RFXComLighting1Message.SubType.ARC, 5, "C.8", (byte) 6, Commands.OFF, "OFF");
        testMessage("0710010B43080160", RFXComLighting1Message.SubType.ARC, 11, "C.8", (byte) 6, Commands.ON, "ON");

        testMessage("0710000843010150", RFXComLighting1Message.SubType.X10, 8, "C.1", (byte) 5, Commands.ON, "ON");
        testMessage("0710007F41010000", RFXComLighting1Message.SubType.X10, 127, "A.1", (byte) 0, Commands.OFF, "OFF");
        testMessage("0710009A41010170", RFXComLighting1Message.SubType.X10, 154, "A.1", (byte) 7, Commands.ON, "ON");
    }

    @Test
    public void testCommandStringOff() throws RFXComUnsupportedChannelException {
        RFXComLighting1Message msg = new RFXComLighting1Message();

        msg.convertFromState(CHANNEL_COMMAND_STRING, StringType.valueOf("OFF"));

        assertEquals(OnOffType.OFF, msg.convertToState(CHANNEL_COMMAND, deviceState));
        assertEquals(OpenClosedType.CLOSED, msg.convertToState(CHANNEL_CONTACT, deviceState));
        assertEquals(StringType.valueOf("OFF"), msg.convertToState(CHANNEL_COMMAND_STRING, deviceState));
    }

    @Test
    public void testCommandStringChime() throws RFXComUnsupportedChannelException {
        RFXComLighting1Message msg = new RFXComLighting1Message();

        msg.convertFromState(CHANNEL_COMMAND_STRING, StringType.valueOf("chime"));

        assertEquals(OnOffType.ON, msg.convertToState(CHANNEL_COMMAND, deviceState));
        assertEquals(OpenClosedType.OPEN, msg.convertToState(CHANNEL_CONTACT, deviceState));
        assertEquals(StringType.valueOf("CHIME"), msg.convertToState(CHANNEL_COMMAND_STRING, deviceState));
    }

    @Test
    public void testCommandStringBright() throws RFXComUnsupportedChannelException {
        RFXComLighting1Message msg = new RFXComLighting1Message();

        msg.convertFromState(CHANNEL_COMMAND_STRING, StringType.valueOf("bright"));

        assertEquals(OnOffType.ON, msg.convertToState(CHANNEL_COMMAND, deviceState));
        assertEquals(OpenClosedType.OPEN, msg.convertToState(CHANNEL_CONTACT, deviceState));
        assertEquals(StringType.valueOf("BRIGHT"), msg.convertToState(CHANNEL_COMMAND_STRING, deviceState));
    }

    @Test
    public void testCommandStringDim() throws RFXComUnsupportedChannelException {
        RFXComLighting1Message msg = new RFXComLighting1Message();

        msg.convertFromState(CHANNEL_COMMAND_STRING, StringType.valueOf("dim"));

        assertEquals(OnOffType.OFF, msg.convertToState(CHANNEL_COMMAND, deviceState));
        assertEquals(OpenClosedType.CLOSED, msg.convertToState(CHANNEL_CONTACT, deviceState));
        assertEquals(StringType.valueOf("DIM"), msg.convertToState(CHANNEL_COMMAND_STRING, deviceState));
    }
}
