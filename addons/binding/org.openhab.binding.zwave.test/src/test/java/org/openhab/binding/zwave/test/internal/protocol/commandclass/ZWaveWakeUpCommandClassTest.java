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
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveWakeUpCommandClass;

/**
 * Test cases for {@link ZWaveWakeUpCommandClass}.
 *
 * @author Chris Jackson - Initial version
 */
public class ZWaveWakeUpCommandClassTest extends ZWaveCommandClassTest {

    @Test
    public void getNoMoreInformationMessage() {
        ZWaveWakeUpCommandClass cls = (ZWaveWakeUpCommandClass) getCommandClass(CommandClass.WAKE_UP);
        SerialMessage msg;

        byte[] expectedResponseV1 = { 1, 9, 0, 19, 99, 2, -124, 8, 0, 0, 8 };
        cls.setVersion(1);
        msg = cls.getNoMoreInformationMessage();
        assertTrue(Arrays.equals(msg.getMessageBuffer(), expectedResponseV1));
    }

    @Test
    public void getIntervalMessage() {
        ZWaveWakeUpCommandClass cls = (ZWaveWakeUpCommandClass) getCommandClass(CommandClass.WAKE_UP);
        SerialMessage msg;

        byte[] expectedResponseV1 = { 1, 9, 0, 19, 99, 2, -124, 5, 0, 0, 5 };
        cls.setVersion(1);
        msg = cls.getIntervalMessage();
        assertTrue(Arrays.equals(msg.getMessageBuffer(), expectedResponseV1));
    }

    @Test
    public void setInterval() {
        ZWaveWakeUpCommandClass cls = (ZWaveWakeUpCommandClass) getCommandClass(CommandClass.WAKE_UP);
        SerialMessage msg;

        byte[] expectedResponseV1 = { 1, 13, 0, 19, 99, 6, -124, 4, 0, 38, -108, 0, 0, 0, -74 };
        cls.setVersion(1);
        msg = cls.setInterval(9876);
        assertTrue(Arrays.equals(msg.getMessageBuffer(), expectedResponseV1));
    }

    @Test
    public void getIntervalCapabilitiesMessage() {
        ZWaveWakeUpCommandClass cls = (ZWaveWakeUpCommandClass) getCommandClass(CommandClass.WAKE_UP);
        SerialMessage msg;

        byte[] expectedResponseV1 = { 1, 9, 0, 19, 99, 2, -124, 9, 0, 0, 9 };
        cls.setVersion(1);
        msg = cls.getIntervalCapabilitiesMessage();
        byte[] x = msg.getMessageBuffer();
        assertTrue(Arrays.equals(msg.getMessageBuffer(), expectedResponseV1));
    }
}
