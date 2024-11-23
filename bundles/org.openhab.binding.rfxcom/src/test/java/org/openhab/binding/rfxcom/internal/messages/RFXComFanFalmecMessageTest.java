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
import static org.openhab.binding.rfxcom.internal.messages.RFXComBaseMessage.PacketType.FAN_FALMEC;
import static org.openhab.binding.rfxcom.internal.messages.RFXComFanMessage.SubType.FALMEC;
import static org.openhab.binding.rfxcom.internal.messages.RFXComFanMessageTest.testCommand;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.UnDefType;

/**
 * Test for RFXCom-binding
 *
 * @author Martin van Wingerden - Initial contribution
 */
@NonNullByDefault
public class RFXComFanFalmecMessageTest {

    @Test
    public void testFalmecCommandOn() throws RFXComException {
        testCommand(FALMEC, CHANNEL_COMMAND, OnOffType.ON, OnOffType.ON, UnDefType.UNDEF, new DecimalType(2),
                StringType.valueOf("SPEED_2"), FAN_FALMEC);
    }

    @Test
    public void testFalmecCommandOff() throws RFXComException {
        testCommand(FALMEC, CHANNEL_COMMAND, OnOffType.OFF, OnOffType.OFF, UnDefType.UNDEF, new DecimalType(0),
                StringType.valueOf("POWER_OFF"), FAN_FALMEC);
    }

    @Test
    public void testFanSpeed0() throws RFXComException {
        testFalmecFanSpeed(0, OnOffType.OFF);
    }

    @Test
    public void testFanSpeed1() throws RFXComException {
        testFalmecFanSpeed(1, OnOffType.ON);
    }

    @Test
    public void testFanSpeed2() throws RFXComException {
        testFalmecFanSpeed(2, OnOffType.ON);
    }

    @Test
    public void testFanSpeed3() throws RFXComException {
        testFalmecFanSpeed(3, OnOffType.ON);
    }

    @Test
    public void testFanSpeed4() throws RFXComException {
        testFalmecFanSpeed(4, OnOffType.ON);
    }

    @Test
    public void testFalmecFanLightOn() throws RFXComException {
        testCommand(FALMEC, CHANNEL_FAN_LIGHT, OnOffType.ON, null, OnOffType.ON, null, StringType.valueOf("LIGHT_ON"),
                FAN_FALMEC);
    }

    @Test
    public void testFalmecFanLightOff() throws RFXComException {
        testCommand(FALMEC, CHANNEL_FAN_LIGHT, OnOffType.OFF, null, OnOffType.OFF, null,
                StringType.valueOf("LIGHT_OFF"), FAN_FALMEC);
    }

    private void testFalmecFanSpeed(int value, OnOffType expectedCommand) throws RFXComException {
        StringType expectedCommandString;
        if (value == 0) {
            expectedCommandString = StringType.valueOf("POWER_OFF");
        } else {
            expectedCommandString = StringType.valueOf("SPEED_" + value);
        }
        testCommand(FALMEC, CHANNEL_FAN_SPEED, new DecimalType(value), expectedCommand, UnDefType.UNDEF,
                new DecimalType(value), expectedCommandString, FAN_FALMEC);
    }
}
