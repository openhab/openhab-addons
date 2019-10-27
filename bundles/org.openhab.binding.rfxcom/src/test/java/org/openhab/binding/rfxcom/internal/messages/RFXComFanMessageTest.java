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

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.eclipse.smarthome.core.util.HexUtils;
import org.junit.Test;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;

import static org.junit.Assert.assertEquals;
import static org.openhab.binding.rfxcom.internal.RFXComBindingConstants.*;
import static org.openhab.binding.rfxcom.internal.messages.RFXComBaseMessage.PacketType.FAN;
import static org.openhab.binding.rfxcom.internal.messages.RFXComFanMessage.SubType.CASAFAN;

/**
 * Test for RFXCom-binding
 *
 * @author Martin van Wingerden
 */
public class RFXComFanMessageTest {
    @Test
    public void checkForSupportTest() throws RFXComException {
        RFXComMessageFactory.createMessage(FAN);
    }

    @Test
    public void basicBoundaryCheck() throws RFXComException {
        RFXComFanMessage message = (RFXComFanMessage) RFXComMessageFactory.createMessage(FAN);

        message.setSubType(RFXComFanMessage.SubType.CASAFAN);
        message.convertFromState(CHANNEL_FAN_SPEED, StringType.valueOf("OFF"));

        RFXComTestHelper.basicBoundaryCheck(FAN, message);
    }

    private void testMessage(String hexMsg, int seqNbr, String deviceId,
                             int signalLevel, State expectedCommand, State expectedLightCommand, State expectedFanSpeed, RFXComBaseMessage.PacketType packetType) throws RFXComException {
        final RFXComFanMessage msg = (RFXComFanMessage) RFXComMessageFactory
                .createMessage(HexUtils.hexToBytes(hexMsg));
        assertEquals("Seq Number", seqNbr, (short) (msg.seqNbr & 0xFF));
        assertEquals("Sensor Id", deviceId, msg.getDeviceId());
        assertEquals("Signal Level", signalLevel, msg.signalLevel);

        assertEquals(expectedCommand, msg.convertToState(CHANNEL_COMMAND));
        assertEquals(expectedLightCommand, msg.convertToState(CHANNEL_FAN_LIGHT));
        assertEquals(expectedFanSpeed, msg.convertToState(CHANNEL_FAN_SPEED));

        assertEquals(packetType, msg.getPacketType());

        byte[] decoded = msg.decodeMessage();

        assertEquals("Message converted back", hexMsg, HexUtils.bytesToHex(decoded));
    }

    @Test
    public void testSomeMessages() throws RFXComException {
        testMessage("0817060052D4000500", 0, "5428224", 0, null, OnOffType.ON, null, FAN);
    }

    @Test
    public void testCommandOn() throws RFXComException {
        testCommand(CASAFAN, CHANNEL_COMMAND, OnOffType.ON, OnOffType.ON, UnDefType.UNDEF, StringType.valueOf("MED"), StringType.valueOf("MED"), FAN);
    }

    @Test
    public void testCommandOff() throws RFXComException {
        testCommand(CASAFAN, CHANNEL_COMMAND, OnOffType.OFF, OnOffType.OFF, UnDefType.UNDEF, StringType.valueOf("OFF"), StringType.valueOf("OFF"), FAN);
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
        testCommand(CASAFAN, CHANNEL_FAN_LIGHT, OnOffType.ON, null, OnOffType.ON, null, StringType.valueOf("LIGHT"), FAN);
    }

    private void testFanSpeedString(String value, OnOffType expectedCommand, State expectedFanSpeed) throws RFXComException {
        testCommand(CASAFAN, CHANNEL_FAN_SPEED, StringType.valueOf(value), expectedCommand, UnDefType.UNDEF, expectedFanSpeed, expectedFanSpeed, FAN);
    }

    static void testCommand(RFXComFanMessage.SubType subType, String channel, State inputValue, OnOffType expectedCommand, State expectedLightCommand, State expectedFanSpeed, State expectedCommandString, RFXComBaseMessage.PacketType packetType) throws RFXComException {
        RFXComFanMessage msg = new RFXComFanMessage();

        msg.setSubType(subType);
        msg.convertFromState(channel, inputValue);

        assertValues(msg, expectedCommand, expectedLightCommand, expectedFanSpeed, packetType, expectedCommandString);

        RFXComFanMessage result = new RFXComFanMessage();
        result.encodeMessage(msg.decodeMessage());

        assertValues(msg, expectedCommand, expectedLightCommand, expectedFanSpeed, packetType, expectedCommandString);
    }

    static void assertValues(RFXComFanMessage msg, OnOffType expectedCommand, State expectedLightCommand, State expectedFanSpeed, RFXComBaseMessage.PacketType packetType, State expectedCommandString) throws RFXComException {
        assertEquals(expectedCommand, msg.convertToState(CHANNEL_COMMAND));
        assertEquals(expectedLightCommand, msg.convertToState(CHANNEL_FAN_LIGHT));
        assertEquals(expectedFanSpeed, msg.convertToState(CHANNEL_FAN_SPEED));
        assertEquals(expectedCommandString, msg.convertToState(CHANNEL_COMMAND_STRING));
        assertEquals(packetType, msg.getPacketType());
    }
}
