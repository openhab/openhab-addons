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

import org.junit.Test;
import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveBinarySensorCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveBinarySensorCommandClass.SensorType;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;

/**
 * Test cases for {@link ZWaveBinarySensorCommandClass}.
 *
 * @author Chris Jackson - Initial version
 * @author Jorg de Jong
 */
public class ZWaveBinarySensorCommandClassTest extends ZWaveCommandClassTest {

    @Test
    public void getValueMessage() {
        ZWaveBinarySensorCommandClass cls = (ZWaveBinarySensorCommandClass) getCommandClass(CommandClass.SENSOR_BINARY);
        SerialMessage msg;

        byte[] expectedResponseV1 = { 1, 9, 0, 19, 99, 2, 48, 2, 0, 0, -74 };
        cls.setVersion(1);
        msg = cls.getValueMessage();
        assertTrue(Arrays.equals(msg.getMessageBuffer(), expectedResponseV1));
    }

    @Test
    public void getValueMessageType() {
        ZWaveBinarySensorCommandClass cls = (ZWaveBinarySensorCommandClass) getCommandClass(CommandClass.SENSOR_BINARY);
        SerialMessage msg;

        byte[] expectedResponseV1 = { 1, 10, 0, 19, 99, 3, 48, 2, 10, 0, 0, -66 };
        cls.setVersion(2);
        msg = cls.getValueMessage(SensorType.DOORWINDOW);
        assertTrue(Arrays.equals(msg.getMessageBuffer(), expectedResponseV1));
    }

    @Test
    public void getSupportedMessage() {
        ZWaveBinarySensorCommandClass cls = (ZWaveBinarySensorCommandClass) getCommandClass(CommandClass.SENSOR_BINARY);
        SerialMessage msg;

        byte[] expectedResponseV2 = { 1, 9, 0, 19, 99, 2, 48, 1, 0, 0, -75 };
        cls.setVersion(2);
        msg = cls.getSupportedMessage();
        assertTrue(Arrays.equals(msg.getMessageBuffer(), expectedResponseV2));
    }

    @Test
    public void reportSupportedSensors() throws Exception {
        byte[] packetData = { 0x01, 0x0A, 0x00, 0x04, 0x00, 0x2F, 0x04, 0x30, 0x04, 0x00, 0x15, (byte) 0xFB };
        SerialMessage msg = new SerialMessage(packetData);

        ZWaveBinarySensorCommandClass cls = (ZWaveBinarySensorCommandClass) getCommandClass(CommandClass.SENSOR_BINARY);
        cls.setVersion(2);
        cls.handleApplicationCommandRequest(msg, 4, 0);
        assertEquals(3, cls.getSupportedTypes().size());
        assertTrue(cls.getSupportedTypes().contains(SensorType.DOORWINDOW));
        assertTrue(cls.getSupportedTypes().contains(SensorType.TAMPER));
        assertTrue(cls.getSupportedTypes().contains(SensorType.MOTION));
    }

}
