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
import org.openhab.binding.zwave.internal.protocol.ZWaveConfigurationParameter;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveConfigurationCommandClass;

/**
 * Test cases for {@link ZWaveConfigurationCommandClass}.
 *
 * @author Chris Jackson - Initial version
 */
public class ZWaveConfigurationCommandClassTest extends ZWaveCommandClassTest {

    @Test
    public void getValueMessage() {
        ZWaveConfigurationCommandClass cls = (ZWaveConfigurationCommandClass) getCommandClass(
                CommandClass.CONFIGURATION);
        SerialMessage msg;

        byte[] expectedResponseV1 = { 1, 10, 0, 19, 99, 3, 112, 5, 12, 0, 0, -1 };
        cls.setVersion(1);
        msg = cls.getConfigMessage(12);
        byte[] x = msg.getMessageBuffer();
        assertTrue(Arrays.equals(msg.getMessageBuffer(), expectedResponseV1));
    }

    @Test
    public void setValueMessage() {
        ZWaveConfigurationCommandClass cls = (ZWaveConfigurationCommandClass) getCommandClass(
                CommandClass.CONFIGURATION);
        SerialMessage msg;

        ZWaveConfigurationParameter parameter = new ZWaveConfigurationParameter(12, 34, 4);

        byte[] expectedResponseV1 = { 1, 15, 0, 19, 99, 8, 112, 4, 12, 4, 0, 0, 0, 34, 0, 0, -42 };
        cls.setVersion(1);
        msg = cls.setConfigMessage(parameter);
        byte[] x = msg.getMessageBuffer();
        assertTrue(Arrays.equals(msg.getMessageBuffer(), expectedResponseV1));
    }
}
