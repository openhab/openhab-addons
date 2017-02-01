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
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveAlarmSensorCommandClass.AlarmType;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;

/**
 * Test cases for {@link ZWaveAlarmSensorCommandClass}.
 *
 * @author Chris Jackson - Initial version
 */
public class ZWaveAlarmSensorCommandClassTest extends ZWaveCommandClassTest {

    @Test
    public void getSupportedMessage() {
        ZWaveAlarmSensorCommandClass cls = (ZWaveAlarmSensorCommandClass) getCommandClass(CommandClass.SENSOR_ALARM);
        SerialMessage msg;

        byte[] expectedResponseV1 = { 1, 9, 0, 19, 99, 2, -100, 3, 0, 0, 27 };
        cls.setVersion(1);
        msg = cls.getSupportedMessage();
        assertTrue(Arrays.equals(msg.getMessageBuffer(), expectedResponseV1));
    }

    @Test
    public void getMessage() {
        ZWaveAlarmSensorCommandClass cls = (ZWaveAlarmSensorCommandClass) getCommandClass(CommandClass.SENSOR_ALARM);
        SerialMessage msg;

        byte[] expectedResponseV1 = { 1, 10, 0, 19, 99, 3, -100, 1, 6, 0, 0, 29 };
        cls.setVersion(1);
        msg = cls.getMessage(AlarmType.ACCESS_CONTROL);
        assertTrue(Arrays.equals(msg.getMessageBuffer(), expectedResponseV1));
    }
}
