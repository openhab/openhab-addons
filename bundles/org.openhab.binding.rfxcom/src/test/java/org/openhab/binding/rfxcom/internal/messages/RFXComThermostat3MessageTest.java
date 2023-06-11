/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
import static org.openhab.binding.rfxcom.internal.RFXComBindingConstants.*;
import static org.openhab.binding.rfxcom.internal.RFXComTestHelper.commandChannelUID;
import static org.openhab.binding.rfxcom.internal.messages.RFXComBaseMessage.PacketType.THERMOSTAT3;
import static org.openhab.binding.rfxcom.internal.messages.RFXComThermostat3Message.SubType.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.openhab.binding.rfxcom.internal.RFXComTestHelper;
import org.openhab.binding.rfxcom.internal.config.RFXComGenericDeviceConfiguration;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.openhab.core.util.HexUtils;

/**
 * Test for RFXCom-binding
 *
 * @author Martin van Wingerden - Initial contribution
 */
@NonNullByDefault
public class RFXComThermostat3MessageTest {
    private static RFXComGenericDeviceConfiguration config = new RFXComGenericDeviceConfiguration();

    static {
        config.deviceId = "106411";
        config.subType = RFXComThermostat3Message.SubType.MERTIK__G6R_H4S_TRANSMIT_ONLY.toString();
    }

    private final MockDeviceState deviceState = new MockDeviceState();

    @Test
    public void checkForSupportTest() throws RFXComException {
        RFXComMessageFactoryImpl.INSTANCE.createMessage(THERMOSTAT3, config, commandChannelUID, OnOffType.ON);
    }

    @Test
    public void basicBoundaryCheck() throws RFXComException {
        RFXComThermostat3Message message = (RFXComThermostat3Message) RFXComMessageFactoryImpl.INSTANCE
                .createMessage(THERMOSTAT3, config, commandChannelUID, OnOffType.ON);

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
            @Nullable State secondCommandChannel, State controlChannel, State commandStringChannel)
            throws RFXComException {
        byte[] message = HexUtils.hexToBytes(hexMessage);
        RFXComThermostat3Message msg = (RFXComThermostat3Message) RFXComMessageFactoryImpl.INSTANCE
                .createMessage(message);
        assertEquals(subtype, msg.subType, "SubType");
        assertEquals(sequenceNumber, (short) (msg.seqNbr & 0xFF), "Seq Number");
        assertEquals(sensorId, msg.getDeviceId(), "Sensor Id");
        assertEquals(command, msg.command, CHANNEL_COMMAND);
        assertEquals(signalLevel, msg.signalLevel, "Signal Level");

        assertEquals(commandChannel, msg.convertToState(CHANNEL_COMMAND, config, deviceState));
        assertEquals(secondCommandChannel, msg.convertToState(CHANNEL_COMMAND_SECOND, config, deviceState));
        assertEquals(controlChannel, msg.convertToState(CHANNEL_CONTROL, config, deviceState));
        assertEquals(commandStringChannel, msg.convertToState(CHANNEL_COMMAND_STRING, config, deviceState));

        byte[] decoded = msg.decodeMessage();

        assertEquals(hexMessage, HexUtils.bytesToHex(decoded), "Message converted back");
    }

    @Test
    public void testCommandChannelOn() throws RFXComException {
        RFXComThermostat3Message msg = new RFXComThermostat3Message();

        msg.convertFromState(CHANNEL_COMMAND, OnOffType.ON);

        assertEquals(OnOffType.ON, msg.convertToState(CHANNEL_COMMAND, config, deviceState));
        assertEquals(OnOffType.ON, msg.convertToState(CHANNEL_CONTROL, config, deviceState));
        assertEquals(StringType.valueOf("ON"), msg.convertToState(CHANNEL_COMMAND_STRING, config, deviceState));
        assertNull(msg.convertToState(CHANNEL_COMMAND_SECOND, config, deviceState));
    }

    @Test
    public void testCommandChannelOff() throws RFXComException {
        RFXComThermostat3Message msg = new RFXComThermostat3Message();

        msg.convertFromState(CHANNEL_COMMAND, OnOffType.OFF);

        assertEquals(OnOffType.OFF, msg.convertToState(CHANNEL_COMMAND, config, deviceState));
        assertEquals(OnOffType.OFF, msg.convertToState(CHANNEL_CONTROL, config, deviceState));
        assertNull(msg.convertToState(CHANNEL_COMMAND_SECOND, config, deviceState));
        assertEquals(StringType.valueOf("OFF"), msg.convertToState(CHANNEL_COMMAND_STRING, config, deviceState));
    }

    @Test
    public void testSecondCommandChannelOn() throws RFXComException {
        RFXComThermostat3Message msg = new RFXComThermostat3Message();

        msg.convertFromState(CHANNEL_COMMAND_SECOND, OnOffType.ON);

        assertNull(msg.convertToState(CHANNEL_COMMAND, config, deviceState));
        assertEquals(OnOffType.ON, msg.convertToState(CHANNEL_COMMAND_SECOND, config, deviceState));
        assertNull(msg.convertToState(CHANNEL_CONTROL, config, deviceState));
        assertEquals(StringType.valueOf("SECOND_ON"), msg.convertToState(CHANNEL_COMMAND_STRING, config, deviceState));
    }

    @Test
    public void testSecondCommandChannelOff() throws RFXComException {
        RFXComThermostat3Message msg = new RFXComThermostat3Message();

        msg.convertFromState(CHANNEL_COMMAND_SECOND, OnOffType.OFF);

        assertNull(msg.convertToState(CHANNEL_COMMAND, config, deviceState));
        assertEquals(OnOffType.OFF, msg.convertToState(CHANNEL_COMMAND_SECOND, config, deviceState));
        assertNull(msg.convertToState(CHANNEL_CONTROL, config, deviceState));
        assertEquals(StringType.valueOf("SECOND_OFF"), msg.convertToState(CHANNEL_COMMAND_STRING, config, deviceState));
    }

    @Test
    public void testControlUp() throws RFXComException {
        RFXComThermostat3Message msg = new RFXComThermostat3Message();

        msg.convertFromState(CHANNEL_CONTROL, UpDownType.UP);

        assertEquals(OnOffType.ON, msg.convertToState(CHANNEL_COMMAND, config, deviceState));
        assertEquals(UpDownType.UP, msg.convertToState(CHANNEL_CONTROL, config, deviceState));
        assertEquals(StringType.valueOf("UP"), msg.convertToState(CHANNEL_COMMAND_STRING, config, deviceState));

        assertNull(msg.convertToState(CHANNEL_COMMAND_SECOND, config, deviceState));
    }

    @Test
    public void testControlDown() throws RFXComException {
        RFXComThermostat3Message msg = new RFXComThermostat3Message();

        msg.convertFromState(CHANNEL_CONTROL, UpDownType.DOWN);

        assertEquals(UnDefType.UNDEF, msg.convertToState(CHANNEL_COMMAND, config, deviceState));
        assertEquals(UpDownType.DOWN, msg.convertToState(CHANNEL_CONTROL, config, deviceState));
        assertEquals(StringType.valueOf("DOWN"), msg.convertToState(CHANNEL_COMMAND_STRING, config, deviceState));

        assertNull(msg.convertToState(CHANNEL_COMMAND_SECOND, config, deviceState));
    }

    @Test
    public void testControlStop() throws RFXComException {
        RFXComThermostat3Message msg = new RFXComThermostat3Message();

        msg.convertFromState(CHANNEL_CONTROL, StopMoveType.STOP);

        assertEquals(UnDefType.UNDEF, msg.convertToState(CHANNEL_COMMAND, config, deviceState));
        assertEquals(StringType.valueOf("STOP"), msg.convertToState(CHANNEL_COMMAND_STRING, config, deviceState));

        assertNull(msg.convertToState(CHANNEL_CONTROL, config, deviceState));
        assertNull(msg.convertToState(CHANNEL_COMMAND_SECOND, config, deviceState));
    }

    @Test
    public void testCommandStringOff() throws RFXComException {
        RFXComThermostat3Message msg = new RFXComThermostat3Message();

        msg.convertFromState(CHANNEL_COMMAND_STRING, StringType.valueOf("OFF"));

        assertEquals(OnOffType.OFF, msg.convertToState(CHANNEL_COMMAND, config, deviceState));
        assertEquals(OnOffType.OFF, msg.convertToState(CHANNEL_CONTROL, config, deviceState));
        assertEquals(StringType.valueOf("OFF"), msg.convertToState(CHANNEL_COMMAND_STRING, config, deviceState));
        assertNull(msg.convertToState(CHANNEL_COMMAND_SECOND, config, deviceState));
    }

    @Test
    public void testCommandStringOn() throws RFXComException {
        RFXComThermostat3Message msg = new RFXComThermostat3Message();

        msg.convertFromState(CHANNEL_COMMAND_STRING, StringType.valueOf("On"));

        assertEquals(OnOffType.ON, msg.convertToState(CHANNEL_COMMAND, config, deviceState));
        assertEquals(OnOffType.ON, msg.convertToState(CHANNEL_CONTROL, config, deviceState));
        assertEquals(StringType.valueOf("ON"), msg.convertToState(CHANNEL_COMMAND_STRING, config, deviceState));
        assertNull(msg.convertToState(CHANNEL_COMMAND_SECOND, config, deviceState));
    }

    @Test
    public void testCommandStringUp() throws RFXComException {
        RFXComThermostat3Message msg = new RFXComThermostat3Message();

        msg.convertFromState(CHANNEL_COMMAND_STRING, StringType.valueOf("UP"));

        assertEquals(OnOffType.ON, msg.convertToState(CHANNEL_COMMAND, config, deviceState));
        assertEquals(UpDownType.UP, msg.convertToState(CHANNEL_CONTROL, config, deviceState));
        assertEquals(StringType.valueOf("UP"), msg.convertToState(CHANNEL_COMMAND_STRING, config, deviceState));

        assertNull(msg.convertToState(CHANNEL_COMMAND_SECOND, config, deviceState));
    }

    @Test
    public void testCommandStringDown() throws RFXComException {
        RFXComThermostat3Message msg = new RFXComThermostat3Message();

        msg.convertFromState(CHANNEL_COMMAND_STRING, StringType.valueOf("down"));

        assertEquals(UnDefType.UNDEF, msg.convertToState(CHANNEL_COMMAND, config, deviceState));
        assertEquals(UpDownType.DOWN, msg.convertToState(CHANNEL_CONTROL, config, deviceState));
        assertEquals(StringType.valueOf("DOWN"), msg.convertToState(CHANNEL_COMMAND_STRING, config, deviceState));
        assertNull(msg.convertToState(CHANNEL_COMMAND_SECOND, config, deviceState));
    }

    @Test
    public void testCommandStringRunUp() throws RFXComException {
        RFXComThermostat3Message msg = new RFXComThermostat3Message();

        msg.convertFromState(CHANNEL_COMMAND_STRING, StringType.valueOf("RUN_UP"));

        assertEquals(OnOffType.ON, msg.convertToState(CHANNEL_COMMAND, config, deviceState));
        assertEquals(UpDownType.UP, msg.convertToState(CHANNEL_CONTROL, config, deviceState));
        assertEquals(StringType.valueOf("RUN_UP"), msg.convertToState(CHANNEL_COMMAND_STRING, config, deviceState));
        assertNull(msg.convertToState(CHANNEL_COMMAND_SECOND, config, deviceState));
    }

    @Test
    public void testCommandStringRunDown() throws RFXComException {
        RFXComThermostat3Message msg = new RFXComThermostat3Message();

        msg.convertFromState(CHANNEL_COMMAND_STRING, StringType.valueOf("RUN_DOWN"));

        assertEquals(OnOffType.OFF, msg.convertToState(CHANNEL_COMMAND, config, deviceState));
        assertEquals(UpDownType.DOWN, msg.convertToState(CHANNEL_CONTROL, config, deviceState));
        assertEquals(StringType.valueOf("RUN_DOWN"), msg.convertToState(CHANNEL_COMMAND_STRING, config, deviceState));
        assertNull(msg.convertToState(CHANNEL_COMMAND_SECOND, config, deviceState));
    }

    @Test
    public void testCommandStringStop() throws RFXComException {
        RFXComThermostat3Message msg = new RFXComThermostat3Message();

        msg.convertFromState(CHANNEL_COMMAND_STRING, StringType.valueOf("STOP"));

        assertEquals(UnDefType.UNDEF, msg.convertToState(CHANNEL_COMMAND, config, deviceState));
        assertEquals(StringType.valueOf("STOP"), msg.convertToState(CHANNEL_COMMAND_STRING, config, deviceState));

        assertNull(msg.convertToState(CHANNEL_CONTROL, config, deviceState));
        assertNull(msg.convertToState(CHANNEL_COMMAND_SECOND, config, deviceState));
    }

    @Test
    public void testCommandStringSecondOn() throws RFXComException {
        RFXComThermostat3Message msg = new RFXComThermostat3Message();

        msg.convertFromState(CHANNEL_COMMAND_STRING, StringType.valueOf("SECOND_ON"));

        assertEquals(OnOffType.ON, msg.convertToState(CHANNEL_COMMAND_SECOND, config, deviceState));
        assertEquals(StringType.valueOf("SECOND_ON"), msg.convertToState(CHANNEL_COMMAND_STRING, config, deviceState));

        assertNull(msg.convertToState(CHANNEL_COMMAND, config, deviceState));
        assertNull(msg.convertToState(CHANNEL_CONTROL, config, deviceState));
    }

    @Test
    public void testCommandStringSecondOff() throws RFXComException {
        RFXComThermostat3Message msg = new RFXComThermostat3Message();

        msg.convertFromState(CHANNEL_COMMAND_STRING, StringType.valueOf("SECOND_OFF"));

        assertEquals(OnOffType.OFF, msg.convertToState(CHANNEL_COMMAND_SECOND, config, deviceState));
        assertEquals(StringType.valueOf("SECOND_OFF"), msg.convertToState(CHANNEL_COMMAND_STRING, config, deviceState));

        assertNull(msg.convertToState(CHANNEL_COMMAND, config, deviceState));
        assertNull(msg.convertToState(CHANNEL_CONTROL, config, deviceState));
    }
}
