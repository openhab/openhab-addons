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
import java.util.List;

import org.junit.Test;
import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveProtectionCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveProtectionCommandClass.LocalProtectionType;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveProtectionCommandClass.RfProtectionType;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveProtectionCommandClass.Type;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveCommandClassValueEvent;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveEvent;

/**
 * Test cases for {@link ZWaveProtectionCommandClass}.
 *
 * @author Jorg de Jong
 */
public class ZWaveProtectionCommandClassTest extends ZWaveCommandClassTest {

    @Test
    public void reportProtection() {
        byte[] packetData = { 0x01, 0x0A, 0x00, 0x04, 0x00, 0x0A, 0x04, 0x75, 0x03, 0x00, 0x00, (byte) 0x89 };

        List<ZWaveEvent> events = processCommandClassMessage(packetData, 2);

        assertEquals(events.size(), 2);

        ZWaveCommandClassValueEvent localEvent = (ZWaveCommandClassValueEvent) events.get(0);

        assertEquals(localEvent.getCommandClass(), CommandClass.PROTECTION);
        assertEquals(localEvent.getEndpoint(), 0);
        assertEquals(localEvent.getType(), Type.PROTECTION_LOCAL);
        assertEquals(localEvent.getValue(), LocalProtectionType.UNPROTECTED);

        ZWaveCommandClassValueEvent rfEvent = (ZWaveCommandClassValueEvent) events.get(1);

        assertEquals(rfEvent.getCommandClass(), CommandClass.PROTECTION);
        assertEquals(rfEvent.getEndpoint(), 0);
        assertEquals(rfEvent.getType(), Type.PROTECTION_RF);
        assertEquals(rfEvent.getValue(), RfProtectionType.UNPROTECTED);
    }

    @Test
    public void setProtection() {
        ZWaveProtectionCommandClass cls = (ZWaveProtectionCommandClass) getCommandClass(CommandClass.PROTECTION);
        cls.setVersion(cls.getMaxVersion());

        byte[] expectedResponse = { 99, 4, 117, 1, 1, 2 };
        SerialMessage msg = cls.setValueMessage(LocalProtectionType.SEQUENCE, RfProtectionType.NORFRESPONSE);

        assertTrue(Arrays.equals(msg.getMessagePayload(), expectedResponse));
    }

}
