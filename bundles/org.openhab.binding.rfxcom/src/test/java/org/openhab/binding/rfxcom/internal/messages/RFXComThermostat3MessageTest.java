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

import static org.junit.Assert.*;
import static org.openhab.binding.rfxcom.internal.RFXComBindingConstants.*;
import static org.openhab.binding.rfxcom.internal.messages.RFXComBaseMessage.PacketType.THERMOSTAT3;
import static org.openhab.binding.rfxcom.internal.messages.RFXComThermostat3Message.SubType.*;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StopMoveType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.eclipse.smarthome.core.util.HexUtils;
import org.junit.Test;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComUnsupportedChannelException;

/**
 * Test for RFXCom-binding
 *
 * @author Martin van Wingerden - Initial contribution
 */
public class RFXComThermostat3MessageTest {
    @Test
    public void checkForSupportTest() throws RFXComException {
        RFXComMessageFactory.createMessage(THERMOSTAT3);
    }

    @Test
    public void basicBoundaryCheck() throws RFXComException {
        RFXComThermostat3Message message = (RFXComThermostat3Message) RFXComMessageFactory.createMessage(THERMOSTAT3);

        message.subType = RFXComThermostat3Message.SubType.MERTIK__G6R_H4S_TRANSMIT_ONLY;
        message.command = RFXComThermostat3Message.Commands.ON;

        RFXComTestHelper.basicBoundaryCheck(THERMOSTAT3, message);
    }

    @Test
    public void testSomeMessages() throws RFXComException {
        testMessage("08420101019FAB0280", MERTIK__G6R_H4TB__G6R_H4T__G6R_H4T21_Z22, 1, "106411",
                RFXComThermostat3Message.Commands.UP, (byte) 8, OnOffType.ON, null, UpDownType.UP,
                StringType.valueOf("UP"));
        testMessage("084200000000410500", MERTIK__G6R_H4T1, 0, "65", RFXComThermostat3Message.Commands.RUN_DOWN,
                (byte) 0, OnOffType.OFF, null, UpDownType.DOWN, StringType.valueOf("RUN_DOWN"));
    }

    private void testMessage(String hexMessage, RFXComThermostat3Message.SubType subtype, int sequenceNumber,
            String sensorId, RFXComThermostat3Message.Commands command, byte signalLevel, State commandChannel,
            State secondCommandChannel, State controlChannel, State commandStringChannel) throws RFXComException {
        byte[] message = HexUtils.hexToBytes(hexMessage);
        RFXComThermostat3Message msg = (RFXComThermostat3Message) RFXComMessageFactory.createMessage(message);
        assertEquals("SubType", subtype, msg.subType);
        assertEquals("Seq Number", sequenceNumber, (short) (msg.seqNbr & 0xFF));
        assertEquals("Sensor Id", sensorId, msg.getDeviceId());
        assertEquals(CHANNEL_COMMAND, command, msg.command);
        assertEquals("Signal Level", signalLevel, msg.signalLevel);

        assertEquals(commandChannel, msg.convertToState(CHANNEL_COMMAND));
        assertEquals(secondCommandChannel, msg.convertToState(CHANNEL_COMMAND_SECOND));
        assertEquals(controlChannel, msg.convertToState(CHANNEL_CONTROL));
        assertEquals(commandStringChannel, msg.convertToState(CHANNEL_COMMAND_STRING));

        byte[] decoded = msg.decodeMessage();

        assertEquals("Message converted back", hexMessage, HexUtils.bytesToHex(decoded));
    }
    // TODO please add tests for real messages

    @Test
    public void testCommandChannelOn() throws RFXComUnsupportedChannelException {
        RFXComThermostat3Message msg = new RFXComThermostat3Message();

        msg.convertFromState(CHANNEL_COMMAND, OnOffType.ON);

        assertEquals(OnOffType.ON, msg.convertToState(CHANNEL_COMMAND));
        assertEquals(OnOffType.ON, msg.convertToState(CHANNEL_CONTROL));
        assertEquals(StringType.valueOf("ON"), msg.convertToState(CHANNEL_COMMAND_STRING));
        assertNull(msg.convertToState(CHANNEL_COMMAND_SECOND));
    }

    @Test
    public void testCommandChannelOff() throws RFXComUnsupportedChannelException {
        RFXComThermostat3Message msg = new RFXComThermostat3Message();

        msg.convertFromState(CHANNEL_COMMAND, OnOffType.OFF);

        assertEquals(OnOffType.OFF, msg.convertToState(CHANNEL_COMMAND));
        assertEquals(OnOffType.OFF, msg.convertToState(CHANNEL_CONTROL));
        assertNull(msg.convertToState(CHANNEL_COMMAND_SECOND));
        assertEquals(StringType.valueOf("OFF"), msg.convertToState(CHANNEL_COMMAND_STRING));
    }

    @Test
    public void testSecondCommandChannelOn() throws RFXComUnsupportedChannelException {
        RFXComThermostat3Message msg = new RFXComThermostat3Message();

        msg.convertFromState(CHANNEL_COMMAND_SECOND, OnOffType.ON);

        assertNull(msg.convertToState(CHANNEL_COMMAND));
        assertEquals(OnOffType.ON, msg.convertToState(CHANNEL_COMMAND_SECOND));
        assertNull(msg.convertToState(CHANNEL_CONTROL));
        assertEquals(StringType.valueOf("SECOND_ON"), msg.convertToState(CHANNEL_COMMAND_STRING));
    }

    @Test
    public void testSecondCommandChannelOff() throws RFXComUnsupportedChannelException {
        RFXComThermostat3Message msg = new RFXComThermostat3Message();

        msg.convertFromState(CHANNEL_COMMAND_SECOND, OnOffType.OFF);

        assertNull(msg.convertToState(CHANNEL_COMMAND));
        assertEquals(OnOffType.OFF, msg.convertToState(CHANNEL_COMMAND_SECOND));
        assertNull(msg.convertToState(CHANNEL_CONTROL));
        assertEquals(StringType.valueOf("SECOND_OFF"), msg.convertToState(CHANNEL_COMMAND_STRING));
    }

    @Test
    public void testControlUp() throws RFXComUnsupportedChannelException {
        RFXComThermostat3Message msg = new RFXComThermostat3Message();

        msg.convertFromState(CHANNEL_CONTROL, UpDownType.UP);

        assertEquals(OnOffType.ON, msg.convertToState(CHANNEL_COMMAND));
        assertEquals(UpDownType.UP, msg.convertToState(CHANNEL_CONTROL));
        assertEquals(StringType.valueOf("UP"), msg.convertToState(CHANNEL_COMMAND_STRING));

        assertNull(msg.convertToState(CHANNEL_COMMAND_SECOND));
    }

    @Test
    public void testControlDown() throws RFXComUnsupportedChannelException {
        RFXComThermostat3Message msg = new RFXComThermostat3Message();

        msg.convertFromState(CHANNEL_CONTROL, UpDownType.DOWN);

        assertEquals(UnDefType.UNDEF, msg.convertToState(CHANNEL_COMMAND));
        assertEquals(UpDownType.DOWN, msg.convertToState(CHANNEL_CONTROL));
        assertEquals(StringType.valueOf("DOWN"), msg.convertToState(CHANNEL_COMMAND_STRING));

        assertNull(msg.convertToState(CHANNEL_COMMAND_SECOND));
    }

    @Test
    public void testControlStop() throws RFXComUnsupportedChannelException {
        RFXComThermostat3Message msg = new RFXComThermostat3Message();

        msg.convertFromState(CHANNEL_CONTROL, StopMoveType.STOP);

        assertEquals(UnDefType.UNDEF, msg.convertToState(CHANNEL_COMMAND));
        assertEquals(StringType.valueOf("STOP"), msg.convertToState(CHANNEL_COMMAND_STRING));

        assertNull(msg.convertToState(CHANNEL_CONTROL));
        assertNull(msg.convertToState(CHANNEL_COMMAND_SECOND));
    }

    @Test
    public void testCommandStringOff() throws RFXComUnsupportedChannelException {
        RFXComThermostat3Message msg = new RFXComThermostat3Message();

        msg.convertFromState(CHANNEL_COMMAND_STRING, StringType.valueOf("OFF"));

        assertEquals(OnOffType.OFF, msg.convertToState(CHANNEL_COMMAND));
        assertEquals(OnOffType.OFF, msg.convertToState(CHANNEL_CONTROL));
        assertEquals(StringType.valueOf("OFF"), msg.convertToState(CHANNEL_COMMAND_STRING));
        assertNull(msg.convertToState(CHANNEL_COMMAND_SECOND));
    }

    @Test
    public void testCommandStringOn() throws RFXComUnsupportedChannelException {
        RFXComThermostat3Message msg = new RFXComThermostat3Message();

        msg.convertFromState(CHANNEL_COMMAND_STRING, StringType.valueOf("On"));

        assertEquals(OnOffType.ON, msg.convertToState(CHANNEL_COMMAND));
        assertEquals(OnOffType.ON, msg.convertToState(CHANNEL_CONTROL));
        assertEquals(StringType.valueOf("ON"), msg.convertToState(CHANNEL_COMMAND_STRING));
        assertNull(msg.convertToState(CHANNEL_COMMAND_SECOND));
    }

    @Test
    public void testCommandStringUp() throws RFXComUnsupportedChannelException {
        RFXComThermostat3Message msg = new RFXComThermostat3Message();

        msg.convertFromState(CHANNEL_COMMAND_STRING, StringType.valueOf("UP"));

        assertEquals(OnOffType.ON, msg.convertToState(CHANNEL_COMMAND));
        assertEquals(UpDownType.UP, msg.convertToState(CHANNEL_CONTROL));
        assertEquals(StringType.valueOf("UP"), msg.convertToState(CHANNEL_COMMAND_STRING));

        assertNull(msg.convertToState(CHANNEL_COMMAND_SECOND));
    }

    @Test
    public void testCommandStringDown() throws RFXComUnsupportedChannelException {
        RFXComThermostat3Message msg = new RFXComThermostat3Message();

        msg.convertFromState(CHANNEL_COMMAND_STRING, StringType.valueOf("down"));

        assertEquals(UnDefType.UNDEF, msg.convertToState(CHANNEL_COMMAND));
        assertEquals(UpDownType.DOWN, msg.convertToState(CHANNEL_CONTROL));
        assertEquals(StringType.valueOf("DOWN"), msg.convertToState(CHANNEL_COMMAND_STRING));
        assertNull(msg.convertToState(CHANNEL_COMMAND_SECOND));
    }

    @Test
    public void testCommandStringRunUp() throws RFXComUnsupportedChannelException {
        RFXComThermostat3Message msg = new RFXComThermostat3Message();

        msg.convertFromState(CHANNEL_COMMAND_STRING, StringType.valueOf("RUN_UP"));

        assertEquals(OnOffType.ON, msg.convertToState(CHANNEL_COMMAND));
        assertEquals(UpDownType.UP, msg.convertToState(CHANNEL_CONTROL));
        assertEquals(StringType.valueOf("RUN_UP"), msg.convertToState(CHANNEL_COMMAND_STRING));
        assertNull(msg.convertToState(CHANNEL_COMMAND_SECOND));
    }

    @Test
    public void testCommandStringRunDown() throws RFXComUnsupportedChannelException {
        RFXComThermostat3Message msg = new RFXComThermostat3Message();

        msg.convertFromState(CHANNEL_COMMAND_STRING, StringType.valueOf("RUN_DOWN"));

        assertEquals(OnOffType.OFF, msg.convertToState(CHANNEL_COMMAND));
        assertEquals(UpDownType.DOWN, msg.convertToState(CHANNEL_CONTROL));
        assertEquals(StringType.valueOf("RUN_DOWN"), msg.convertToState(CHANNEL_COMMAND_STRING));
        assertNull(msg.convertToState(CHANNEL_COMMAND_SECOND));
    }

    @Test
    public void testCommandStringStop() throws RFXComUnsupportedChannelException {
        RFXComThermostat3Message msg = new RFXComThermostat3Message();

        msg.convertFromState(CHANNEL_COMMAND_STRING, StringType.valueOf("STOP"));

        assertEquals(UnDefType.UNDEF, msg.convertToState(CHANNEL_COMMAND));
        assertEquals(StringType.valueOf("STOP"), msg.convertToState(CHANNEL_COMMAND_STRING));

        assertNull(msg.convertToState(CHANNEL_CONTROL));
        assertNull(msg.convertToState(CHANNEL_COMMAND_SECOND));
    }

    @Test
    public void testCommandStringSecondOn() throws RFXComUnsupportedChannelException {
        RFXComThermostat3Message msg = new RFXComThermostat3Message();

        msg.convertFromState(CHANNEL_COMMAND_STRING, StringType.valueOf("SECOND_ON"));

        assertEquals(OnOffType.ON, msg.convertToState(CHANNEL_COMMAND_SECOND));
        assertEquals(StringType.valueOf("SECOND_ON"), msg.convertToState(CHANNEL_COMMAND_STRING));

        assertNull(msg.convertToState(CHANNEL_COMMAND));
        assertNull(msg.convertToState(CHANNEL_CONTROL));
    }

    @Test
    public void testCommandStringSecondOff() throws RFXComUnsupportedChannelException {
        RFXComThermostat3Message msg = new RFXComThermostat3Message();

        msg.convertFromState(CHANNEL_COMMAND_STRING, StringType.valueOf("SECOND_OFF"));

        assertEquals(OnOffType.OFF, msg.convertToState(CHANNEL_COMMAND_SECOND));
        assertEquals(StringType.valueOf("SECOND_OFF"), msg.convertToState(CHANNEL_COMMAND_STRING));

        assertNull(msg.convertToState(CHANNEL_COMMAND));
        assertNull(msg.convertToState(CHANNEL_CONTROL));
    }
}
