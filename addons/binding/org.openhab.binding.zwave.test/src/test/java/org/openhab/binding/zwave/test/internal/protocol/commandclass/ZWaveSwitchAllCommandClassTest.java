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
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveSwitchAllCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveSwitchAllCommandClass.SwitchAllMode;

/**
 * Test cases for {@link ZWaveSwitchAllCommandClass}.
 *
 * @author Chris Jackson - Initial version
 */
public class ZWaveSwitchAllCommandClassTest extends ZWaveCommandClassTest {

    @Test
    public void getValueMessage() {
        ZWaveSwitchAllCommandClass cls = (ZWaveSwitchAllCommandClass) getCommandClass(CommandClass.SWITCH_ALL);
        SerialMessage msg;

        byte[] expectedResponseV1 = { 1, 9, 0, 19, 99, 2, 39, 2, 0, 0, -95 };
        cls.setVersion(1);
        msg = cls.getValueMessage();
        assertTrue(Arrays.equals(msg.getMessageBuffer(), expectedResponseV1));
    }

    @Test
    public void setValueMessage() {
        ZWaveSwitchAllCommandClass cls = (ZWaveSwitchAllCommandClass) getCommandClass(CommandClass.SWITCH_ALL);
        SerialMessage msg;

        byte[] expectedResponseV1 = { 1, 10, 0, 19, 99, 3, 39, 1, 2, 0, 0, -94 };
        cls.setVersion(1);
        msg = cls.setValueMessage(SwitchAllMode.SWITCH_ALL_INCLUDE_OFF_ONLY.ordinal());
        assertTrue(Arrays.equals(msg.getMessageBuffer(), expectedResponseV1));
    }

    @Test
    public void allOnMessage() {
        ZWaveSwitchAllCommandClass cls = (ZWaveSwitchAllCommandClass) getCommandClass(CommandClass.SWITCH_ALL);
        SerialMessage msg;

        byte[] expectedResponseV1 = { 1, 9, 0, 19, 99, 2, 39, 4, 0, 0, -89 };
        cls.setVersion(1);
        msg = cls.allOnMessage();
        assertTrue(Arrays.equals(msg.getMessageBuffer(), expectedResponseV1));
    }

    @Test
    public void allOffMessage() {
        ZWaveSwitchAllCommandClass cls = (ZWaveSwitchAllCommandClass) getCommandClass(CommandClass.SWITCH_ALL);
        SerialMessage msg;

        byte[] expectedResponseV1 = { 1, 9, 0, 19, 99, 2, 39, 5, 0, 0, -90 };
        cls.setVersion(1);
        msg = cls.allOffMessage();
        assertTrue(Arrays.equals(msg.getMessageBuffer(), expectedResponseV1));
    }
}
