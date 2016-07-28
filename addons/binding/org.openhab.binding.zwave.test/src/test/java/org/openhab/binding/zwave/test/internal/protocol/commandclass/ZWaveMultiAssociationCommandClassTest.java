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
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveMultiAssociationCommandClass;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveAssociationEvent;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveEvent;

/**
 * Test cases for {@link ZWaveMultiAssociationCommandClass}.
 *
 * @author Chris Jackson - Initial version
 */
public class ZWaveMultiAssociationCommandClassTest extends ZWaveCommandClassTest {

    @Test
    public void getAssociationMessage() {
        ZWaveMultiAssociationCommandClass cls = (ZWaveMultiAssociationCommandClass) getCommandClass(
                CommandClass.MULTI_INSTANCE_ASSOCIATION);
        SerialMessage msg;

        byte[] expectedResponseV1 = { 1, 10, 0, 19, 99, 3, -114, 2, 1, 0, 4, 15 };
        cls.setVersion(1);
        msg = cls.getAssociationMessage(1);
        msg.setCallbackId(4);
        assertTrue(Arrays.equals(msg.getMessageBuffer(), expectedResponseV1));
    }

    @Test
    public void getGroupingsMessage() {
        ZWaveMultiAssociationCommandClass cls = (ZWaveMultiAssociationCommandClass) getCommandClass(
                CommandClass.MULTI_INSTANCE_ASSOCIATION);
        SerialMessage msg;

        byte[] expectedResponseV1 = { 1, 9, 0, 19, 99, 2, -114, 5, 0, 4, 11 };
        cls.setVersion(1);
        msg = cls.getGroupingsMessage();
        msg.setCallbackId(4);
        assertTrue(Arrays.equals(msg.getMessageBuffer(), expectedResponseV1));
    }

    @Test
    public void removeAssociationMessage() {
        ZWaveMultiAssociationCommandClass cls = (ZWaveMultiAssociationCommandClass) getCommandClass(
                CommandClass.MULTI_INSTANCE_ASSOCIATION);
        SerialMessage msg;

        byte[] expectedResponse1 = { 1, 11, 0, 19, 99, 4, -114, 4, 1, 2, 0, 4, 13 };
        byte[] expectedResponse2 = { 1, 13, 0, 19, 99, 6, -114, 4, 1, 0, 2, 3, 0, 4, 10 };

        cls.setVersion(1);
        msg = cls.removeAssociationMessage(1, 2, 0);
        msg.setCallbackId(4);
        assertTrue(Arrays.equals(msg.getMessageBuffer(), expectedResponse1));

        msg = cls.removeAssociationMessage(1, 2, 3);
        msg.setCallbackId(4);
        assertTrue(Arrays.equals(msg.getMessageBuffer(), expectedResponse2));
    }

    @Test
    public void setAssociationMessage() {
        ZWaveMultiAssociationCommandClass cls = (ZWaveMultiAssociationCommandClass) getCommandClass(
                CommandClass.MULTI_INSTANCE_ASSOCIATION);
        SerialMessage msg;

        byte[] expectedResponse1 = { 1, 11, 0, 19, 99, 4, -114, 1, 1, 2, 0, 4, 8 };
        byte[] expectedResponse2 = { 1, 13, 0, 19, 99, 6, -114, 1, 1, 0, 2, 3, 0, 4, 15 };

        cls.setVersion(1);
        msg = cls.setAssociationMessage(1, 2, 0);
        msg.setCallbackId(4);
        assertTrue(Arrays.equals(msg.getMessageBuffer(), expectedResponse1));

        msg = cls.setAssociationMessage(1, 2, 3);
        msg.setCallbackId(4);
        assertTrue(Arrays.equals(msg.getMessageBuffer(), expectedResponse2));
    }

    @Test
    public void AssociationReport() {
        byte[] packetData = { 0x01, 0x0E, 0x00, 0x04, 0x00, 0x03, 0x08, (byte) 0x8E, 0x03, 0x02, 0x10, 0x00, 0x00, 0x01,
                0x01, 0x61 };

        List<ZWaveEvent> events = processCommandClassMessage(packetData);

        assertEquals(events.size(), 1);

        ZWaveAssociationEvent event = (ZWaveAssociationEvent) events.get(0);

        assertEquals(event.getCommandClass(), CommandClass.ASSOCIATION);
        // assertEquals(event.getNodeId(), 3);
        assertEquals(event.getEndpoint(), 0);
        assertEquals(event.getGroupId(), 2);
        assertEquals(event.getGroupMembers().size(), 1);
        assertEquals(event.getGroupMembers().get(0).getNode(), 1);
        assertEquals(event.getGroupMembers().get(0).getEndpoint(), 1);
    }
}
