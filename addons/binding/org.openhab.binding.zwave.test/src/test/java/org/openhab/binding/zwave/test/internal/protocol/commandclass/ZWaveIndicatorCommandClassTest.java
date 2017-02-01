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
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveIndicatorCommandClass;

/**
 * Test cases for {@link ZWaveIndicatorCommandClass}.
 *
 * @author Chris Jackson - Initial version
 */
public class ZWaveIndicatorCommandClassTest extends ZWaveCommandClassTest {

    @Test
    public void getValueMessage() {
        ZWaveIndicatorCommandClass cls = (ZWaveIndicatorCommandClass) getCommandClass(CommandClass.INDICATOR);
        SerialMessage msg;

        byte[] expectedResponseV1 = { 1, 9, 0, 19, 99, 2, -121, 2, 0, 0, 1 };
        cls.setVersion(1);
        msg = cls.getValueMessage();
        byte[] x = msg.getMessageBuffer();
        assertTrue(Arrays.equals(msg.getMessageBuffer(), expectedResponseV1));
    }

    @Test
    public void setValueMessage() {
        ZWaveIndicatorCommandClass cls = (ZWaveIndicatorCommandClass) getCommandClass(CommandClass.INDICATOR);
        SerialMessage msg;

        byte[] expectedResponseV1 = { 1, 10, 0, 19, 99, 3, -121, 1, 34, 0, 0, 34 };
        cls.setVersion(1);
        msg = cls.setValueMessage(34);
        byte[] x = msg.getMessageBuffer();
        assertTrue(Arrays.equals(msg.getMessageBuffer(), expectedResponseV1));
    }
}
