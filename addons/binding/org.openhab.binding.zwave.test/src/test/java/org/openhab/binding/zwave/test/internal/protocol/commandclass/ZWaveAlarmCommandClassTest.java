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

import java.util.List;

import org.junit.Test;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveAlarmCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveAlarmCommandClass.ZWaveAlarmValueEvent;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveEvent;

/**
 * Test cases for {@link ZWaveAlarmCommandClass}.
 *
 * @author Chris Jackson - Initial version
 */
public class ZWaveAlarmCommandClassTest extends ZWaveCommandClassTest {

    @Test
    public void Alarm_Smoke() {
        byte[] packetData = { 0x01, 0x10, 0x00, 0x04, 0x10, 0x28, 0x0A, 0x71, 0x05, 0x00, 0x00, 0x00, (byte) 0xFF, 0x01,
                0x00, 0x01, 0x03, 0x51 };

        List<ZWaveEvent> events = processCommandClassMessage(packetData);

        assertEquals(events.size(), 1);

        ZWaveAlarmValueEvent event = (ZWaveAlarmValueEvent) events.get(0);

        assertEquals(event.getCommandClass(), CommandClass.ALARM);
        // assertEquals(event.getNodeId(), 40);
        assertEquals(event.getEndpoint(), 0);
        assertEquals(event.getAlarmType(), ZWaveAlarmCommandClass.AlarmType.SMOKE);
        assertEquals(event.getAlarmStatus(), 0xFF);
    }

}
