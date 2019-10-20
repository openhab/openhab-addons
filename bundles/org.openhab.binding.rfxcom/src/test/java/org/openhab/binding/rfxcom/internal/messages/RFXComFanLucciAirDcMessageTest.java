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
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.junit.Test;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;

import static org.openhab.binding.rfxcom.internal.RFXComBindingConstants.*;
import static org.openhab.binding.rfxcom.internal.messages.RFXComBaseMessage.PacketType.FAN_LUCCI_DC;
import static org.openhab.binding.rfxcom.internal.messages.RFXComFanMessage.SubType.LUCCI_AIR_DC;

/**
 * Test for RFXCom-binding
 *
 * @author Martin van Wingerden
 */
public class RFXComFanLucciAirDcMessageTest {

    @Test
    public void testFanLightOn() throws RFXComException {
        testCommand(CHANNEL_FAN_LIGHT, OnOffType.ON, OnOffType.ON, null, StringType.valueOf("LIGHT"));
    }

    @Test
    public void testUp() throws RFXComException {
        testCommand(CHANNEL_FAN_SPEED, UpDownType.UP, UnDefType.UNDEF, UpDownType.UP, StringType.valueOf("UP"));
    }

    @Test
    public void testDown() throws RFXComException {
        testCommand(CHANNEL_FAN_SPEED, UpDownType.DOWN, UnDefType.UNDEF, UpDownType.DOWN, StringType.valueOf("DOWN"));
    }

    @Test
    public void testCommandString() throws RFXComException {
        testCommand(CHANNEL_COMMAND_STRING, StringType.valueOf("POWER"), UnDefType.UNDEF, null, StringType.valueOf("POWER"));
        testCommand(CHANNEL_COMMAND_STRING, StringType.valueOf("UP"), UnDefType.UNDEF, UpDownType.UP, StringType.valueOf("UP"));
        testCommand(CHANNEL_COMMAND_STRING, StringType.valueOf("DOWN"), UnDefType.UNDEF, UpDownType.DOWN, StringType.valueOf("DOWN"));
        testCommand(CHANNEL_COMMAND_STRING, StringType.valueOf("LIGHT"), OnOffType.ON, null, StringType.valueOf("LIGHT"));
        testCommand(CHANNEL_COMMAND_STRING, StringType.valueOf("REVERSE"), UnDefType.UNDEF, null, StringType.valueOf("REVERSE"));
        testCommand(CHANNEL_COMMAND_STRING, StringType.valueOf("NATURAL_FLOW"), UnDefType.UNDEF, null, StringType.valueOf("NATURAL_FLOW"));
        testCommand(CHANNEL_COMMAND_STRING, StringType.valueOf("PAIR"), UnDefType.UNDEF, null, StringType.valueOf("PAIR"));
    }

    private void testCommand(String channel, State inputValue, State expectedLightCommand, State expectedFanSpeed, State expectedCommandString) throws RFXComException {
        RFXComFanMessageTest.testCommand(LUCCI_AIR_DC, channel, inputValue, null, expectedLightCommand, expectedFanSpeed, expectedCommandString, FAN_LUCCI_DC);
    }
}
