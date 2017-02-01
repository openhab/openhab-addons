/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.test.internal.protocol.commandclass.multiinstance;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.List;

import org.junit.Test;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveMeterCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveMeterCommandClass.ZWaveMeterValueEvent;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveEvent;
import org.openhab.binding.zwave.test.internal.protocol.commandclass.ZWaveMultiInstanceCommandClassTest;

/**
 * Test cases for {@link ZWaveMeterCommandClass}.
 *
 * @author Chris Jackson - Initial version
 */
public class ZWaveMeterCommandClassTest extends ZWaveMultiInstanceCommandClassTest {

    @Test
    public void Meter_Electric_Watts() {
        byte[] packetData = { 0x01, 0x12, 0x00, 0x04, 0x00, 0x3B, 0x0C, 0x60, 0x0D, 0x03, 0x01, 0x32, 0x02, 0x01,
                (byte) 0x84, 0x00, 0x00, 0x00, 0x3C, 0x38 };

        List<ZWaveEvent> events = processMessage(packetData);

        assertEquals(events.size(), 1);

        ZWaveMeterValueEvent event = (ZWaveMeterValueEvent) events.get(0);

        assertEquals(event.getCommandClass(), CommandClass.METER);
        // assertEquals(event.getNodeId(), 59);
        assertEquals(event.getEndpoint(), 3);
        assertEquals(event.getMeterScale(), ZWaveMeterCommandClass.MeterScale.E_KWh);
        assertEquals(event.getMeterType(), ZWaveMeterCommandClass.MeterType.ELECTRIC);
        assertEquals(event.getValue(), new BigDecimal("0.006"));
    }

}
