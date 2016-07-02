/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.test.internal.protocol.commandclass;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;
import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveBasicCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveThermostatFanModeCommandClass;

/**
 * Test cases for {@link ZWaveBasicCommandClass}.
 *
 * @author Chris Jackson - Initial version
 */
public class ZWaveThermostatFanModeCommandClassTest extends ZWaveCommandClassTest {

    @Test
    public void getValueMessage() {
        ZWaveThermostatFanModeCommandClass cls = (ZWaveThermostatFanModeCommandClass) getCommandClass(
                CommandClass.THERMOSTAT_FAN_MODE);
        SerialMessage msg;

        byte[] expectedResponseV1 = { 1, 9, 0, 19, 99, 2, 68, 2, 0, 0, -62 };
        cls.setVersion(1);
        msg = cls.getValueMessage();
        byte[] x = msg.getMessageBuffer();
        assertTrue(Arrays.equals(msg.getMessageBuffer(), expectedResponseV1));
    }

    @Test
    public void setValueMessage() {
        ZWaveThermostatFanModeCommandClass cls = (ZWaveThermostatFanModeCommandClass) getCommandClass(
                CommandClass.THERMOSTAT_FAN_MODE);
        SerialMessage msg;

        byte[] expectedResponseV1 = { 1, 9, 0, 19, 99, 2, 68, 4, 0, 0, -60 };
        cls.setVersion(1);
        msg = cls.setValueMessage(34);
        // byte[] x = msg.getMessageBuffer();
        // assertTrue(Arrays.equals(msg.getMessageBuffer(), expectedResponseV1));
    }

    @Test
    public void getSupportedMessage() {
        ZWaveThermostatFanModeCommandClass cls = (ZWaveThermostatFanModeCommandClass) getCommandClass(
                CommandClass.THERMOSTAT_FAN_MODE);
        SerialMessage msg;

        byte[] expectedResponseV1 = { 1, 9, 0, 19, 99, 2, 68, 4, 0, 0, -60 };
        cls.setVersion(1);
        msg = cls.getSupportedMessage();
        byte[] x = msg.getMessageBuffer();
        assertTrue(Arrays.equals(msg.getMessageBuffer(), expectedResponseV1));
    }
}
