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
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveNodeNamingCommandClass;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveCommandClassValueEvent;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveEvent;

/**
 * Test cases for {@link ZWaveNodeNamingCommandClassTest}.
 *
 * @author Chris Jackson - Initial version
 */
public class ZWaveNodeNamingCommandClassTest extends ZWaveCommandClassTest {

    @Test
    public void reportNodeName_Len0_UTF16() {
        byte[] packetData = { 0x01, 0x09, 0x00, 0x04, 0x00, 0x12, 0x03, 0x77, 0x03, 0x02, (byte) 0x95 };

        List<ZWaveEvent> events = processCommandClassMessage(packetData);
        assertEquals(events.size(), 1);

        ZWaveCommandClassValueEvent event = (ZWaveCommandClassValueEvent) events.get(0);

        assertEquals(event.getCommandClass(), CommandClass.NODE_NAMING);
        // assertEquals(event.getNodeId(), 44);
        assertEquals(event.getEndpoint(), 0);
        assertEquals(event.getType(), ZWaveNodeNamingCommandClass.Type.NODENAME_NAME);
        assertEquals(event.getValue(), new String(""));
    }

    @Test
    public void reportNodeName_Len4_UTF16() {
        // This reports UTF16 but is really ASCII
        byte[] packetData = { 0x01, 0x10, 0x00, 0x04, 0x00, 0x09, 0x0A, 0x77, 0x03, 0x02, 0x54, 0x65, 0x73, 0x74, 0x69,
                0x6E, 0x67, (byte) 0xC8 };

        List<ZWaveEvent> events = processCommandClassMessage(packetData);
        assertEquals(events.size(), 1);

        ZWaveCommandClassValueEvent event = (ZWaveCommandClassValueEvent) events.get(0);

        assertEquals(event.getCommandClass(), CommandClass.NODE_NAMING);
        // assertEquals(event.getNodeId(), 44);
        assertEquals(event.getEndpoint(), 0);
        assertEquals(event.getType(), ZWaveNodeNamingCommandClass.Type.NODENAME_NAME);
        assertEquals(event.getValue(), new String("Testing"));
    }

    @Test
    public void reportLocation_Len0_ASCII() {
        byte[] packetData = { 0x01, 0x09, 0x00, 0x04, 0x00, 0x12, 0x03, 0x77, 0x06, 0x00, (byte) 0x92 };

        List<ZWaveEvent> events = processCommandClassMessage(packetData);
        assertEquals(events.size(), 1);

        ZWaveCommandClassValueEvent event = (ZWaveCommandClassValueEvent) events.get(0);

        assertEquals(event.getCommandClass(), CommandClass.NODE_NAMING);
        // assertEquals(event.getNodeId(), 44);
        assertEquals(event.getEndpoint(), 0);
        assertEquals(event.getType(), ZWaveNodeNamingCommandClass.Type.NODENAME_LOCATION);
        assertEquals(event.getValue(), new String(""));
    }

    @Test
    public void setName() {
        ZWaveNodeNamingCommandClass cls = (ZWaveNodeNamingCommandClass) getCommandClass(CommandClass.NODE_NAMING);

        SerialMessage msg;

        byte[] expectedResponse1 = { 99, 10, 119, 1, 0, 84, 101, 115, 116, 105, 110, 103 };
        msg = cls.setNameMessage("Testing");
        assertTrue(Arrays.equals(msg.getMessagePayload(), expectedResponse1));

        byte[] expectedResponse2 = { 99, 19, 119, 1, 0, 84, 101, 115, 116, 105, 110, 103, 32, 77, 111, 114, 101, 32, 84,
                104, 97 };
        msg = cls.setNameMessage("Testing More Than 16 Bytes");
        assertTrue(Arrays.equals(msg.getMessagePayload(), expectedResponse2));

        byte[] expectedResponse3 = { 99, 15, 119, 1, 2, -2, -1, 0, 65, 0, -22, 0, -15, 0, -4, 0, 67 };
        msg = cls.setNameMessage(new String("A" + "\u00ea" + "\u00f1" + "\u00fc" + "C"));
        assertTrue(Arrays.equals(msg.getMessagePayload(), expectedResponse3));
    }

    @Test
    public void setLocation() {
        // Note that most of the functionality is common between NAME and LOCATION SET.
        // We so just do a quick test here to make sure the command format is correct
        ZWaveNodeNamingCommandClass cls = (ZWaveNodeNamingCommandClass) getCommandClass(CommandClass.NODE_NAMING);

        SerialMessage msg;

        byte[] expectedResponse1 = { 99, 10, 119, 4, 0, 84, 101, 115, 116, 105, 110, 103 };
        msg = cls.setLocationMessage("Testing");
        assertTrue(Arrays.equals(msg.getMessagePayload(), expectedResponse1));
    }
}
