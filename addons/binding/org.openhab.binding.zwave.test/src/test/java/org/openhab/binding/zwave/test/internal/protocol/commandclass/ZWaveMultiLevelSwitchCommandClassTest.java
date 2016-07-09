/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.test.internal.protocol.commandclass;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveMultiLevelSwitchCommandClass;

/**
 * Test cases for {@link ZWaveMultiLevelSwitchCommandClass}.
 *
 * @author Chris Jackson - Initial version
 */
public class ZWaveMultiLevelSwitchCommandClassTest extends ZWaveCommandClassTest {
    @Test
    public void getValueMessage() {
        ZWaveMultiLevelSwitchCommandClass cls = (ZWaveMultiLevelSwitchCommandClass) getCommandClass(
                CommandClass.SWITCH_MULTILEVEL);
        SerialMessage msg;

        byte[] expectedResponseV1 = { 1, 9, 0, 19, 99, 2, 38, 2, 0, 0, -96 };
        cls.setVersion(1);
        msg = cls.getValueMessage();
        assertTrue(Arrays.equals(msg.getMessageBuffer(), expectedResponseV1));
    }

    @Test
    public void setValueMessage() {
        ZWaveMultiLevelSwitchCommandClass cls = (ZWaveMultiLevelSwitchCommandClass) getCommandClass(
                CommandClass.SWITCH_MULTILEVEL);
        SerialMessage msg;

        byte[] expectedResponseV1 = { 1, 10, 0, 19, 99, 3, 38, 1, 56, 0, 0, -103 };
        cls.setVersion(1);
        msg = cls.setValueMessage(56);
        assertTrue(Arrays.equals(msg.getMessageBuffer(), expectedResponseV1));
    }

    @Test
    public void startLevelChangeMessage() {
        ZWaveMultiLevelSwitchCommandClass cls = (ZWaveMultiLevelSwitchCommandClass) getCommandClass(
                CommandClass.SWITCH_MULTILEVEL);
        SerialMessage msg;

        byte[] expectedResponseV1 = { 1, 12, 0, 19, 99, 5, 38, 4, 32, 0, 43, 0, 0, -81 };
        cls.setVersion(1);
        msg = cls.startLevelChangeMessage(true, 43);
        assertTrue(Arrays.equals(msg.getMessageBuffer(), expectedResponseV1));
    }

    @Test
    public void stopLevelChangeMessage() {
        ZWaveMultiLevelSwitchCommandClass cls = (ZWaveMultiLevelSwitchCommandClass) getCommandClass(
                CommandClass.SWITCH_MULTILEVEL);
        SerialMessage msg;

        byte[] expectedResponseV1 = { 1, 9, 0, 19, 99, 2, 38, 5, 0, 0, -89 };
        cls.setVersion(1);
        msg = cls.stopLevelChangeMessage();
        assertTrue(Arrays.equals(msg.getMessageBuffer(), expectedResponseV1));
    }

    @Test
    public void getSupportedMessage() {
        ZWaveMultiLevelSwitchCommandClass cls = (ZWaveMultiLevelSwitchCommandClass) getCommandClass(
                CommandClass.SWITCH_MULTILEVEL);
        SerialMessage msg;

        byte[] expectedResponseV3 = { 1, 9, 0, 19, 99, 2, 38, 6, 0, 0, -92 };
        cls.setVersion(3);
        msg = cls.getSupportedMessage();
        assertTrue(Arrays.equals(msg.getMessageBuffer(), expectedResponseV3));
    }

    @Test
    public void initialize() {
        ZWaveMultiLevelSwitchCommandClass cls = (ZWaveMultiLevelSwitchCommandClass) getCommandClass(
                CommandClass.SWITCH_MULTILEVEL);
        Collection<SerialMessage> msgs;

        cls.setVersion(1);
        msgs = cls.initialize(true);
        assertEquals(0, msgs.size());

        cls.setVersion(3);
        msgs = cls.initialize(true);
        assertEquals(1, msgs.size());
    }
}
