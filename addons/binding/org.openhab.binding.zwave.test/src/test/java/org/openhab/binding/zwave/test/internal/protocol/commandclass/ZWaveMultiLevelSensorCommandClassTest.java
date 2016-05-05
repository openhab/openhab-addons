/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.test.internal.protocol.commandclass;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.List;

import org.junit.Test;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveMultiLevelSensorCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveMultiLevelSensorCommandClass.ZWaveMultiLevelSensorValueEvent;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveEvent;

/**
 * Test cases for {@link ZWaveMultiLevelSensorCommandClass}.
 *
 * @author Chris Jackson - Initial version
 */
public class ZWaveMultiLevelSensorCommandClassTest extends ZWaveCommandClassTest {

    @Test
    public void Sensor_Luminance() {
        byte[] packetData = { 0x01, 0x0C, 0x00, 0x04, 0x00, 0x02, 0x06, 0x31, 0x05, 0x03, 0x0A, 0x00, 0x67,
                (byte) 0xA9 };

        List<ZWaveEvent> events = processCommandClassMessage(packetData);

        assertEquals(events.size(), 1);

        ZWaveMultiLevelSensorValueEvent event = (ZWaveMultiLevelSensorValueEvent) events.get(0);

        assertEquals(event.getCommandClass(), CommandClass.SENSOR_MULTILEVEL);
        // assertEquals(event.getNodeId(), 2);
        assertEquals(event.getEndpoint(), 0);
        assertEquals(event.getSensorType(), ZWaveMultiLevelSensorCommandClass.SensorType.LUMINANCE);
        assertEquals(event.getValue(), new BigDecimal("103"));
    }

    @Test
    public void Sensor_Temperature() {
        byte[] packetData = { 0x01, 0x0C, 0x00, 0x04, 0x00, 0x02, 0x06, 0x31, 0x05, 0x01, 0x22, 0x01, 0x12,
                (byte) 0xF7 };

        List<ZWaveEvent> events = processCommandClassMessage(packetData);

        assertEquals(events.size(), 1);

        ZWaveMultiLevelSensorValueEvent event = (ZWaveMultiLevelSensorValueEvent) events.get(0);

        assertEquals(event.getCommandClass(), CommandClass.SENSOR_MULTILEVEL);
        // assertEquals(event.getNodeId(), 2);
        assertEquals(event.getEndpoint(), 0);
        assertEquals(event.getSensorType(), ZWaveMultiLevelSensorCommandClass.SensorType.TEMPERATURE);
        assertEquals(event.getValue(), new BigDecimal("27.4"));
    }
}
