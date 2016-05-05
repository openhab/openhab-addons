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
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveTimeParametersCommandClass;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveCommandClassValueEvent;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveEvent;

/**
 * Test cases for {@link ZWaveTimeParametersCommandClass}.
 *
 * @author Jorg de Jong
 */
public class ZWaveTimeParametersCommandClassTest extends ZWaveCommandClassTest {

    @Test
    public void reportTimeOffset() {
        byte[] packetData = { 0x01, 0x0F, 0x00, 0x04, 0x00, 0x07, 0x09, (byte) 0x8B, 0x03, 0x07, (byte) 0xE0, 0x04,
                0x17, 0x0B, 0x29, 0x18, (byte) 0xBC };

        List<ZWaveEvent> events = processCommandClassMessage(packetData);

        assertEquals(events.size(), 1);

        ZWaveCommandClassValueEvent event = (ZWaveCommandClassValueEvent) events.get(0);

        assertEquals(event.getCommandClass(), CommandClass.TIME_PARAMETERS);
        assertEquals(event.getEndpoint(), 0);
        Date date = (Date) event.getValue();
        assertNotNull(date);
        // reported time is in the past.
        assertEquals(true, date.before(new Date()));
    }

    @Test
    public void setTime() {
        ZWaveTimeParametersCommandClass cls = (ZWaveTimeParametersCommandClass) getCommandClass(
                CommandClass.TIME_PARAMETERS);

        byte[] expectedResponse = { 99, 9, -117, 1, 7, -78, 1, 1, 1, 0, 0 };
        SerialMessage msg = cls.getSetMessage(new Date(0));

        assertTrue(Arrays.equals(msg.getMessagePayload(), expectedResponse));
    }

}
