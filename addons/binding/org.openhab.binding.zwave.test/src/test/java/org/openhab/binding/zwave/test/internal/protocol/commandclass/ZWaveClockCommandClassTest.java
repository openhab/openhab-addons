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
import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveClockCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveClockCommandClass.ZWaveClockValueEvent;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveEvent;

/**
 * Test cases for {@link ZWaveClockCommandClass}.
 *
 * @author Jorg de Jong
 */
public class ZWaveClockCommandClassTest extends ZWaveCommandClassTest {

    @Test
    public void reportTimeOffset() {
        byte[] packetData = { 0x01, 0x0F, 0x00, 0x04, 0x00, 0x07, 0x07, (byte) 0x81, 0x06, -127, 4, 127, 0, -119 };

        List<ZWaveEvent> events = processCommandClassMessage(packetData);

        assertEquals(events.size(), 1);

        ZWaveClockValueEvent event = (ZWaveClockValueEvent) events.get(0);

        assertEquals(event.getCommandClass(), CommandClass.CLOCK);
        assertEquals(event.getEndpoint(), 0);
        Date date = (Date) event.getValue();
        assertNotNull(date);
        // reported time is in the past.
        assertEquals(true, date.before(new Date()));
    }

    @Test
    public void setTime() {
        ZWaveClockCommandClass cls = (ZWaveClockCommandClass) getCommandClass(CommandClass.CLOCK);

        byte[] expectedResponse = { 99, 4, -127, 4, -127, 0 };
        SerialMessage msg = cls.getSetMessage(new Date(0));

        assertTrue(Arrays.equals(msg.getMessagePayload(), expectedResponse));
    }

}
