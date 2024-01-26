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
import static org.openhab.binding.rfxcom.internal.RFXComBindingConstants.*;
import static org.openhab.binding.rfxcom.internal.RFXComTestHelper.thingUID;
import static org.openhab.binding.rfxcom.internal.messages.RFXComBaseMessage.PacketType.FAN;
import static org.openhab.binding.rfxcom.internal.messages.RFXComFanMessage.SubType.CASAFAN;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.openhab.binding.rfxcom.internal.RFXComBindingConstants;
import org.openhab.binding.rfxcom.internal.RFXComTestHelper;
import org.openhab.binding.rfxcom.internal.config.RFXComGenericDeviceConfiguration;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.openhab.core.util.HexUtils;

/**
 * Test for RFXCom-binding
 *
 * @author Martin van Wingerden - Initial contribution
 */
@NonNullByDefault
public class RFXComFanMessageTest {
    private static RFXComGenericDeviceConfiguration config = new RFXComGenericDeviceConfiguration();
    private static ChannelUID fanSpeedChannelUID = new ChannelUID(thingUID, RFXComBindingConstants.CHANNEL_FAN_SPEED);
    private static StringType fanSpeedOff = StringType.valueOf("OFF");

    static {
        config.deviceId = "5428224";
        config.subType = RFXComFanMessage.SubType.CASAFAN.toString();
    }

    private static final MockDeviceState DEVICE_STATE = new MockDeviceState();

    @Test
    public void checkForSupportTest() throws RFXComException {
        RFXComMessageFactoryImpl.INSTANCE.createMessage(FAN, config, fanSpeedChannelUID, fanSpeedOff);
    }

    @Test
    public void basicBoundaryCheck() throws RFXComException {
        RFXComFanMessage message = (RFXComFanMessage) RFXComMessageFactoryImpl.INSTANCE.createMessage(FAN, config,
                fanSpeedChannelUID, fanSpeedOff);

        RFXComTestHelper.basicBoundaryCheck(FAN, message);
    }

    private void testMessage(String hexMsg, int seqNbr, String deviceId, int signalLevel,
            @Nullable State expectedCommand, State expectedLightCommand, @Nullable State expectedFanSpeed,
            RFXComBaseMessage.PacketType packetType) throws RFXComException {
        final RFXComFanMessage msg = (RFXComFanMessage) RFXComMessageFactoryImpl.INSTANCE
                .createMessage(HexUtils.hexToBytes(hexMsg));
        assertEquals(seqNbr, (short) (msg.seqNbr & 0xFF), "Seq Number");
        assertEquals(deviceId, msg.getDeviceId(), "Sensor Id");
        assertEquals(signalLevel, msg.signalLevel, "Signal Level");

        assertEquals(expectedCommand, msg.convertToState(CHANNEL_COMMAND, config, DEVICE_STATE));
        assertEquals(expectedLightCommand, msg.convertToState(CHANNEL_FAN_LIGHT, config, DEVICE_STATE));
        assertEquals(expectedFanSpeed, msg.convertToState(CHANNEL_FAN_SPEED, config, DEVICE_STATE));

        assertEquals(packetType, msg.getPacketType());

        byte[] decoded = msg.decodeMessage();

        assertEquals(hexMsg, HexUtils.bytesToHex(decoded), "Message converted back");
    }

    @Test
    public void testSomeMessages() throws RFXComException {
        testMessage("0817060052D4000500", 0, "5428224", 0, null, OnOffType.ON, null, FAN);
    }

    @Test
    public void testCommandOn() throws RFXComException {
        testCommand(CASAFAN, CHANNEL_COMMAND, OnOffType.ON, OnOffType.ON, UnDefType.UNDEF, StringType.valueOf("MED"),
                StringType.valueOf("MED"), FAN);
    }

    @Test
    public void testCommandOff() throws RFXComException {
        testCommand(CASAFAN, CHANNEL_COMMAND, OnOffType.OFF, OnOffType.OFF, UnDefType.UNDEF, StringType.valueOf("OFF"),
                StringType.valueOf("OFF"), FAN);
    }

    @Test
    public void testFanSpeedStringOff() throws RFXComException {
        testFanSpeedString("OFF", OnOffType.OFF, StringType.valueOf("OFF"));
    }

    @Test
    public void testFanSpeedStringHi() throws RFXComException {
        testFanSpeedString("HI", OnOffType.ON, StringType.valueOf("HI"));
    }

    @Test
    public void testFanSpeedStringMed() throws RFXComException {
        testFanSpeedString("MED", OnOffType.ON, StringType.valueOf("MED"));
    }

    @Test
    public void testFanSpeedStringLow() throws RFXComException {
        testFanSpeedString("LOW", OnOffType.ON, StringType.valueOf("LOW"));
    }

    @Test
    public void testFanLightOn() throws RFXComException {
        testCommand(CASAFAN, CHANNEL_FAN_LIGHT, OnOffType.ON, null, OnOffType.ON, null, StringType.valueOf("LIGHT"),
                FAN);
    }

    private void testFanSpeedString(String value, OnOffType expectedCommand, State expectedFanSpeed)
            throws RFXComException {
        testCommand(CASAFAN, CHANNEL_FAN_SPEED, StringType.valueOf(value), expectedCommand, UnDefType.UNDEF,
                expectedFanSpeed, expectedFanSpeed, FAN);
    }

    static void testCommand(RFXComFanMessage.SubType subType, String channel, State inputValue,
            @Nullable OnOffType expectedCommand, State expectedLightCommand, @Nullable State expectedFanSpeed,
            State expectedCommandString, RFXComBaseMessage.PacketType packetType) throws RFXComException {
        RFXComFanMessage msg = new RFXComFanMessage();

        msg.setSubType(subType);
        msg.convertFromState(channel, inputValue);

        assertValues(msg, expectedCommand, expectedLightCommand, expectedFanSpeed, packetType, expectedCommandString);

        RFXComFanMessage result = new RFXComFanMessage();
        result.encodeMessage(msg.decodeMessage());

        assertValues(msg, expectedCommand, expectedLightCommand, expectedFanSpeed, packetType, expectedCommandString);
    }

    private static void assertValues(RFXComFanMessage msg, @Nullable OnOffType expectedCommand,
            State expectedLightCommand, @Nullable State expectedFanSpeed, RFXComBaseMessage.PacketType packetType,
            State expectedCommandString) throws RFXComException {
        assertEquals(expectedCommand, msg.convertToState(CHANNEL_COMMAND, config, DEVICE_STATE));
        assertEquals(expectedLightCommand, msg.convertToState(CHANNEL_FAN_LIGHT, config, DEVICE_STATE));
        assertEquals(expectedFanSpeed, msg.convertToState(CHANNEL_FAN_SPEED, config, DEVICE_STATE));
        assertEquals(expectedCommandString, msg.convertToState(CHANNEL_COMMAND_STRING, config, DEVICE_STATE));
        assertEquals(packetType, msg.getPacketType());
    }
}
