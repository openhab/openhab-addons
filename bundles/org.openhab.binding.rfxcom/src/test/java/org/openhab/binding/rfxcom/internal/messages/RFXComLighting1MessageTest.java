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
import org.openhab.binding.rfxcom.internal.config.RFXComGenericDeviceConfiguration;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComUnsupportedChannelException;
import org.openhab.binding.rfxcom.internal.messages.RFXComLighting1Message.Commands;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.util.HexUtils;

/**
 * Test for RFXCom-binding
 *
 * @author Martin van Wingerden - Initial contribution
 */
@NonNullByDefault
public class RFXComLighting1MessageTest {
    private final MockDeviceState deviceState = new MockDeviceState();
    private static final RFXComGenericDeviceConfiguration config = new RFXComGenericDeviceConfiguration();

    static {
        config.deviceId = "A.1";
        config.subType = RFXComLighting1Message.SubType.ARC.toString();
    }

    private void testMessage(String hexMsg, RFXComLighting1Message.SubType subType, int seqNbr, String deviceId,
            byte signalLevel, Commands command, String commandString) throws RFXComException {
        final RFXComLighting1Message msg = (RFXComLighting1Message) RFXComMessageFactoryImpl.INSTANCE
                .createMessage(HexUtils.hexToBytes(hexMsg));
        assertEquals(subType, msg.subType, "SubType");
        assertEquals(seqNbr, (short) (msg.seqNbr & 0xFF), "Seq Number");
        assertEquals(deviceId, msg.getDeviceId(), "Sensor Id");
        assertEquals(signalLevel, msg.signalLevel, "Signal Level");
        assertEquals(command, msg.command, "Command");

        RFXComGenericDeviceConfiguration config = new RFXComGenericDeviceConfiguration();
        config.deviceId = deviceId;
        config.subType = subType.toString();
        assertEquals(commandString, msg.convertToState(CHANNEL_COMMAND_STRING, config, deviceState).toString(),
                "Command String");

        byte[] decoded = msg.decodeMessage();

        assertEquals(hexMsg, HexUtils.bytesToHex(decoded), "Message converted back");
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

        assertEquals(OnOffType.OFF, msg.convertToState(CHANNEL_COMMAND, config, deviceState));
        assertEquals(OpenClosedType.CLOSED, msg.convertToState(CHANNEL_CONTACT, config, deviceState));
        assertEquals(StringType.valueOf("OFF"), msg.convertToState(CHANNEL_COMMAND_STRING, config, deviceState));
    }

    @Test
    public void testCommandStringChime() throws RFXComUnsupportedChannelException {
        RFXComLighting1Message msg = new RFXComLighting1Message();

        msg.convertFromState(CHANNEL_COMMAND_STRING, StringType.valueOf("chime"));

        assertEquals(OnOffType.ON, msg.convertToState(CHANNEL_COMMAND, config, deviceState));
        assertEquals(OpenClosedType.OPEN, msg.convertToState(CHANNEL_CONTACT, config, deviceState));
        assertEquals(StringType.valueOf("CHIME"), msg.convertToState(CHANNEL_COMMAND_STRING, config, deviceState));
    }

    @Test
    public void testCommandStringBright() throws RFXComUnsupportedChannelException {
        RFXComLighting1Message msg = new RFXComLighting1Message();

        msg.convertFromState(CHANNEL_COMMAND_STRING, StringType.valueOf("bright"));

        assertEquals(OnOffType.ON, msg.convertToState(CHANNEL_COMMAND, config, deviceState));
        assertEquals(OpenClosedType.OPEN, msg.convertToState(CHANNEL_CONTACT, config, deviceState));
        assertEquals(StringType.valueOf("BRIGHT"), msg.convertToState(CHANNEL_COMMAND_STRING, config, deviceState));
    }

    @Test
    public void testCommandStringDim() throws RFXComUnsupportedChannelException {
        RFXComLighting1Message msg = new RFXComLighting1Message();

        msg.convertFromState(CHANNEL_COMMAND_STRING, StringType.valueOf("dim"));

        assertEquals(OnOffType.OFF, msg.convertToState(CHANNEL_COMMAND, config, deviceState));
        assertEquals(OpenClosedType.CLOSED, msg.convertToState(CHANNEL_CONTACT, config, deviceState));
        assertEquals(StringType.valueOf("DIM"), msg.convertToState(CHANNEL_COMMAND_STRING, config, deviceState));
    }
}
