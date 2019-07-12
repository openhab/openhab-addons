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
import org.openhab.binding.rfxcom.internal.exceptions.RFXComUnsupportedChannelException;

import static org.junit.Assert.assertEquals;
import static org.openhab.binding.rfxcom.internal.RFXComBindingConstants.*;
import static org.openhab.binding.rfxcom.internal.messages.RFXComBaseMessage.PacketType.FAN;

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
                             int signalLevel, State expectedCommand, State expectedLightCommand, State expectedFanSpeed) throws RFXComException {
        final RFXComFanMessage msg = (RFXComFanMessage) RFXComMessageFactory
                .createMessage(HexUtils.hexToBytes(hexMsg));
        assertEquals("Seq Number", seqNbr, (short) (msg.seqNbr & 0xFF));
        assertEquals("Sensor Id", deviceId, msg.getDeviceId());
        assertEquals("Signal Level", signalLevel, msg.signalLevel);

        assertEquals(expectedCommand, msg.convertToState(CHANNEL_COMMAND));
        assertEquals(expectedLightCommand, msg.convertToState(CHANNEL_FAN_LIGHT));
        assertEquals(expectedFanSpeed, msg.convertToState(CHANNEL_FAN_SPEED));

        byte[] decoded = msg.decodeMessage();

        assertEquals("Message converted back", hexMsg, HexUtils.bytesToHex(decoded));
    }

    @Test
    public void testSomeMessages() throws RFXComException {
        testMessage("0817060052D4000500", 0, "5428224", 0, null, OnOffType.ON, null);
    }

    @Test
    public void testCommandOn() throws RFXComUnsupportedChannelException {
        testCommand(CHANNEL_COMMAND, OnOffType.ON, OnOffType.ON, UnDefType.UNDEF, StringType.valueOf("MED"));
    }

    @Test
    public void testCommandOff() throws RFXComUnsupportedChannelException {
        testCommand(CHANNEL_COMMAND, OnOffType.OFF, OnOffType.OFF, UnDefType.UNDEF, StringType.valueOf("OFF"));
    }

    @Test
    public void testFanSpeedStringOff() throws RFXComUnsupportedChannelException {
        testFanSpeedString("OFF", OnOffType.OFF, StringType.valueOf("OFF"));
    }

    @Test
    public void testFanSpeedStringHi() throws RFXComUnsupportedChannelException {
        testFanSpeedString("HI", OnOffType.ON, StringType.valueOf("HI"));
    }

    @Test
    public void testFanSpeedStringMed() throws RFXComUnsupportedChannelException {
        testFanSpeedString("MED", OnOffType.ON, StringType.valueOf("MED"));
    }

    @Test
    public void testFanSpeedStringLow() throws RFXComUnsupportedChannelException {
        testFanSpeedString("LOW", OnOffType.ON, StringType.valueOf("LOW"));
    }

    @Test
    public void testFanLightOn() throws RFXComUnsupportedChannelException {
        testCommand(CHANNEL_FAN_LIGHT, OnOffType.ON, null, OnOffType.ON, null);
    }

    private void testFanSpeedString(String value, OnOffType expectedCommand, State expectedFanSpeed) throws RFXComUnsupportedChannelException {
        testCommand(CHANNEL_FAN_SPEED, StringType.valueOf(value), expectedCommand, UnDefType.UNDEF, expectedFanSpeed);
    }

    private void testCommand(String channel, State inputValue, OnOffType expectedCommand, State expectedLightCommand, State expectedFanSpeed) throws RFXComUnsupportedChannelException {
        RFXComFanMessage msg = new RFXComFanMessage();

        msg.convertFromState(channel, inputValue);

        assertEquals(expectedCommand, msg.convertToState(CHANNEL_COMMAND));
        assertEquals(expectedLightCommand, msg.convertToState(CHANNEL_FAN_LIGHT));
        assertEquals(expectedFanSpeed, msg.convertToState(CHANNEL_FAN_SPEED));
    }
}
