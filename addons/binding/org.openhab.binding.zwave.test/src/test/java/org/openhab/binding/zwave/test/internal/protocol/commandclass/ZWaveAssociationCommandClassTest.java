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
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveAlarmSensorCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveAssociationCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;

/**
 * Test cases for {@link ZWaveAlarmSensorCommandClass}.
 *
 * @author Chris Jackson - Initial version
 */
public class ZWaveAssociationCommandClassTest extends ZWaveCommandClassTest {

    @Test
    public void getAssociationMessage() {
        ZWaveAssociationCommandClass cls = (ZWaveAssociationCommandClass) getCommandClass(CommandClass.ASSOCIATION);
        SerialMessage msg;

        byte[] expectedResponseV1 = { 1, 10, 0, 19, 99, 3, -123, 2, 1, 0, 0, 0 };
        cls.setVersion(1);
        msg = cls.getAssociationMessage(1);
        byte[] x = msg.getMessageBuffer();
        assertTrue(Arrays.equals(msg.getMessageBuffer(), expectedResponseV1));
    }

    @Test
    public void getGroupingsMessage() {
        ZWaveAssociationCommandClass cls = (ZWaveAssociationCommandClass) getCommandClass(CommandClass.ASSOCIATION);
        SerialMessage msg;

        byte[] expectedResponseV1 = { 1, 9, 0, 19, 99, 2, -123, 5, 0, 0, 4 };
        cls.setVersion(1);
        msg = cls.getGroupingsMessage();
        byte[] x = msg.getMessageBuffer();
        assertTrue(Arrays.equals(msg.getMessageBuffer(), expectedResponseV1));
    }

    @Test
    public void removeAssociationMessage() {
        ZWaveAssociationCommandClass cls = (ZWaveAssociationCommandClass) getCommandClass(CommandClass.ASSOCIATION);
        SerialMessage msg;

        byte[] expectedResponseV1 = { 1, 11, 0, 19, 99, 4, -123, 4, 1, 1, 0, 0, 1 };
        cls.setVersion(1);
        msg = cls.removeAssociationMessage(1, 1);
        byte[] x = msg.getMessageBuffer();
        assertTrue(Arrays.equals(msg.getMessageBuffer(), expectedResponseV1));
    }

    @Test
    public void setAssociationMessage() {
        ZWaveAssociationCommandClass cls = (ZWaveAssociationCommandClass) getCommandClass(CommandClass.ASSOCIATION);
        SerialMessage msg;

        byte[] expectedResponseV1 = { 1, 11, 0, 19, 99, 4, -123, 1, 1, 1, 0, 0, 4 };
        cls.setVersion(1);
        msg = cls.setAssociationMessage(1, 1);
        byte[] x = msg.getMessageBuffer();
        assertTrue(Arrays.equals(msg.getMessageBuffer(), expectedResponseV1));
    }
}
