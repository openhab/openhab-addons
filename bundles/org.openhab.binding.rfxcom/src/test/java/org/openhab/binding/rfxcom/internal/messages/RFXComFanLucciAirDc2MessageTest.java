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

import static org.openhab.binding.rfxcom.internal.RFXComBindingConstants.*;
import static org.openhab.binding.rfxcom.internal.messages.RFXComBaseMessage.PacketType.FAN_LUCCI_DC_II;
import static org.openhab.binding.rfxcom.internal.messages.RFXComFanMessage.SubType.LUCCI_AIR_DC_II;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * Test for RFXCom-binding
 *
 * @author Martin van Wingerden - Initial contribution
 */
@NonNullByDefault
public class RFXComFanLucciAirDc2MessageTest {

    @Test
    public void testCommandOn() throws RFXComException {
        testCommand(CHANNEL_COMMAND, OnOffType.ON, OnOffType.ON, UnDefType.UNDEF, new DecimalType(3),
                StringType.valueOf("SPEED_3"));
    }

    @Test
    public void testCommandOff() throws RFXComException {
        testCommand(CHANNEL_COMMAND, OnOffType.OFF, OnOffType.OFF, UnDefType.UNDEF, new DecimalType(0),
                StringType.valueOf("POWER_OFF"));
    }

    @Test
    public void testFanLightOn() throws RFXComException {
        testCommand(CHANNEL_FAN_LIGHT, OnOffType.ON, null, OnOffType.ON, null, StringType.valueOf("LIGHT"));
    }

    @Test
    public void testFanSpeed0() throws RFXComException {
        testFanSpeed(0, OnOffType.OFF);
    }

    @Test
    public void testFanSpeed1() throws RFXComException {
        testFanSpeed(1, OnOffType.ON);
    }

    @Test
    public void testFanSpeed2() throws RFXComException {
        testFanSpeed(2, OnOffType.ON);
    }

    @Test
    public void testFanSpeed3() throws RFXComException {
        testFanSpeed(3, OnOffType.ON);
    }

    @Test
    public void testFanSpeed4() throws RFXComException {
        testFanSpeed(4, OnOffType.ON);
    }

    @Test
    public void testCommandString() throws RFXComException {
        testCommand(CHANNEL_COMMAND_STRING, StringType.valueOf("POWER_OFF"), OnOffType.OFF, UnDefType.UNDEF,
                new DecimalType(0), StringType.valueOf("POWER_OFF"));
        testCommand(CHANNEL_COMMAND_STRING, StringType.valueOf("LIGHT"), null, OnOffType.ON, null,
                StringType.valueOf("LIGHT"));
        testCommand(CHANNEL_COMMAND_STRING, StringType.valueOf("REVERSE"), null, UnDefType.UNDEF, null,
                StringType.valueOf("REVERSE"));
        testCommand(CHANNEL_COMMAND_STRING, StringType.valueOf("SPEED_1"), OnOffType.ON, UnDefType.UNDEF,
                new DecimalType(1), StringType.valueOf("SPEED_1"));
        testCommand(CHANNEL_COMMAND_STRING, StringType.valueOf("SPEED_2"), OnOffType.ON, UnDefType.UNDEF,
                new DecimalType(2), StringType.valueOf("SPEED_2"));
        testCommand(CHANNEL_COMMAND_STRING, StringType.valueOf("SPEED_3"), OnOffType.ON, UnDefType.UNDEF,
                new DecimalType(3), StringType.valueOf("SPEED_3"));
        testCommand(CHANNEL_COMMAND_STRING, StringType.valueOf("SPEED_4"), OnOffType.ON, UnDefType.UNDEF,
                new DecimalType(4), StringType.valueOf("SPEED_4"));
        testCommand(CHANNEL_COMMAND_STRING, StringType.valueOf("SPEED_5"), OnOffType.ON, UnDefType.UNDEF,
                new DecimalType(5), StringType.valueOf("SPEED_5"));
        testCommand(CHANNEL_COMMAND_STRING, StringType.valueOf("SPEED_6"), OnOffType.ON, UnDefType.UNDEF,
                new DecimalType(6), StringType.valueOf("SPEED_6"));
    }

    private void testFanSpeed(int value, OnOffType expectedCommand) throws RFXComException {
        StringType expectedCommandString;
        if (value == 0) {
            expectedCommandString = StringType.valueOf("POWER_OFF");
        } else {
            expectedCommandString = StringType.valueOf("SPEED_" + value);
        }
        testCommand(CHANNEL_FAN_SPEED, new DecimalType(value), expectedCommand, UnDefType.UNDEF, new DecimalType(value),
                expectedCommandString);
    }

    private void testCommand(String channel, State inputValue, @Nullable OnOffType expectedCommand,
            State expectedLightCommand, @Nullable State expectedFanSpeed, State expectedCommandString)
            throws RFXComException {
        RFXComFanMessageTest.testCommand(LUCCI_AIR_DC_II, channel, inputValue, expectedCommand, expectedLightCommand,
                expectedFanSpeed, expectedCommandString, FAN_LUCCI_DC_II);
    }
}
