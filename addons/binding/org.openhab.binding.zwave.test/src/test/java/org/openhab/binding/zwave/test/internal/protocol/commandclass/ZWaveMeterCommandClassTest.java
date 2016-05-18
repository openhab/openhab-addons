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

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveMeterCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveMeterCommandClass.MeterScale;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveMeterCommandClass.ZWaveMeterValueEvent;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveEvent;

/**
 * Test cases for {@link ZWaveMeterCommandClass}.
 *
 * @author Chris Jackson - Initial version
 */
public class ZWaveMeterCommandClassTest extends ZWaveCommandClassTest {

    @Test
    public void Meter_Electric_Watts() {
        byte[] packetData = { 0x01, 0x14, 0x00, 0x04, 0x00, 0x2C, 0x0E, 0x32, 0x02, 0x21, 0x34, 0x00, 0x00, 0x01,
                (byte) 0xB7, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x5E };

        List<ZWaveEvent> events = processCommandClassMessage(packetData);

        assertEquals(events.size(), 1);

        ZWaveMeterValueEvent event = (ZWaveMeterValueEvent) events.get(0);

        assertEquals(event.getCommandClass(), CommandClass.METER);
        // assertEquals(event.getNodeId(), 44);
        assertEquals(event.getEndpoint(), 0);
        assertEquals(event.getMeterScale(), ZWaveMeterCommandClass.MeterScale.E_W);
        assertEquals(event.getMeterType(), ZWaveMeterCommandClass.MeterType.ELECTRIC);
        assertEquals(event.getValue(), new BigDecimal("43.9"));
    }

    @Test
    public void Meter_Electric_Watts_2() {
        byte[] packetData = { 0x01, 0x0E, 0x00, 0x04, 0x00, 0x1B, 0x08, 0x32, 0x02, 0x21, 0x74, 0x00, 0x02, 0x5C, 0x1D,
                (byte) 0xC0 };

        List<ZWaveEvent> events = processCommandClassMessage(packetData);

        assertEquals(events.size(), 1);

        ZWaveMeterValueEvent event = (ZWaveMeterValueEvent) events.get(0);

        assertEquals(event.getCommandClass(), CommandClass.METER);
        // assertEquals(event.getNodeId(), 27);
        assertEquals(event.getEndpoint(), 0);
        assertEquals(event.getMeterScale(), ZWaveMeterCommandClass.MeterScale.E_W);
        assertEquals(event.getMeterType(), ZWaveMeterCommandClass.MeterType.ELECTRIC);
        assertEquals(event.getValue(), new BigDecimal("154.653"));
    }

    @Test
    public void setValueMessage() {
        ZWaveMeterCommandClass cls = (ZWaveMeterCommandClass) getCommandClass(CommandClass.METER);
        SerialMessage msg;

        byte[] expectedResponseV1 = { 1, 9, 0, 19, 99, 2, 50, 1, 0, 0, -73 };
        cls.setVersion(1);
        msg = cls.getValueMessage();
        assertTrue(Arrays.equals(msg.getMessageBuffer(), expectedResponseV1));
    }

    @Test
    public void getResetMessage() {
        ZWaveMeterCommandClass cls = (ZWaveMeterCommandClass) getCommandClass(CommandClass.METER);
        SerialMessage msg;

        byte[] expectedResponseV1 = { 1, 9, 0, 19, 99, 2, 50, 5, 0, 0, -77 };
        cls.setVersion(2);
        Map<String, String> options = new HashMap<String, String>();
        options.put("meterCanReset", "true");
        cls.setOptions(options);
        msg = cls.getResetMessage();
        byte[] x = msg.getMessageBuffer();
        assertTrue(Arrays.equals(msg.getMessageBuffer(), expectedResponseV1));
    }

    @Test
    public void getSupportedMessage() {
        ZWaveMeterCommandClass cls = (ZWaveMeterCommandClass) getCommandClass(CommandClass.METER);
        SerialMessage msg;

        byte[] expectedResponseV1 = { 1, 9, 0, 19, 99, 2, 50, 3, 0, 0, -75 };
        cls.setVersion(1);
        msg = cls.getSupportedMessage();
        assertTrue(Arrays.equals(msg.getMessageBuffer(), expectedResponseV1));
    }

    @Test
    public void getMessage() {
        ZWaveMeterCommandClass cls = (ZWaveMeterCommandClass) getCommandClass(CommandClass.METER);
        SerialMessage msg;

        byte[] expectedResponseV1 = { 1, 10, 0, 19, 99, 3, 50, 1, 0, 0, 0, -75 };
        cls.setVersion(1);
        msg = cls.getMessage(MeterScale.E_KWh);
        assertTrue(Arrays.equals(msg.getMessageBuffer(), expectedResponseV1));
    }
}
