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

import java.util.List;

import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageClass;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageType;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveEndpoint;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.ZWaveSerialMessageException;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveEvent;

/**
 * This class provides methods to allow test for standard command classes to be run simply.
 * It provides the mocked framework to allow command classes to be processed and receives the notification events.
 * Command classes pass in the packet data, and receive the command classes notification event(s) in return which should
 * then be checked for the correct output.
 *
 * @author Chris Jackson
 *
 */
public class ZWaveCommandClassTest {
    public ArgumentCaptor<ZWaveEvent> argument;

    /**
     * Helper class to create everything we need to test a command class message.
     *
     * We pass in the data, and the expected command class. This method creates the class, checks it's the right one,
     * processes the message and gets the response events.
     *
     * It expects at least one response event.
     *
     * @param packetData byte array containing the full Z-Wave frame
     * @param version commandclass version
     * @return List of ZWaveEvent(s)
     */
    protected List<ZWaveEvent> processCommandClassMessage(byte[] packetData, int version) {
        SerialMessage msg = new SerialMessage(packetData);

        // Check the packet is not corrupted and is a command class request
        assertEquals(true, msg.isValid);
        assertEquals(SerialMessageType.Request, msg.getMessageType());
        assertEquals(SerialMessageClass.ApplicationCommandHandler, msg.getMessageClass());

        // Mock the controller so we can get any events
        ZWaveController controller = Mockito.mock(ZWaveController.class);
        argument = ArgumentCaptor.forClass(ZWaveEvent.class);
        Mockito.doNothing().when(controller).notifyEventListeners(argument.capture());
        ZWaveNode node = Mockito.mock(ZWaveNode.class);
        ZWaveEndpoint endpoint = Mockito.mock(ZWaveEndpoint.class);

        // Get the command class and process the response
        try {
            ZWaveCommandClass cls = ZWaveCommandClass.getInstance(msg.getMessagePayloadByte(3), node, controller);
            assertNotNull(cls);
            cls.setVersion(version);
            cls.handleApplicationCommandRequest(msg, 4, 0);
        } catch (ZWaveSerialMessageException e) {
            fail("Out of bounds exception processing data");
        }

        // Check that we received a response
        assertNotNull(argument.getAllValues());
        assertNotEquals(argument.getAllValues().size(), 0);

        // Return all the notifications....
        return argument.getAllValues();
    }

    /**
     * Helper class to create everything we need to test a command class message.
     *
     * We pass in the data, and the expected command class. This method creates the class, checks it's the right one,
     * processes the message and gets the response events.
     *
     * It expects at least one response event.
     *
     * @param packetData byte array containing the full Z-Wave frame
     * @return List of ZWaveEvent(s)
     */
    protected List<ZWaveEvent> processCommandClassMessage(byte[] packetData) {
        return processCommandClassMessage(packetData, 0);
    }

    ZWaveCommandClass getCommandClass(CommandClass cls) {
        // Mock the controller so we can get any events
        ZWaveController mockedController = Mockito.mock(ZWaveController.class);
        ZWaveNode node = Mockito.mock(ZWaveNode.class);
        ZWaveEndpoint endpoint = Mockito.mock(ZWaveEndpoint.class);
        Mockito.when(node.getNodeId()).thenReturn(99);

        // Get the command class and process the response
        ZWaveCommandClass cmdCls = ZWaveCommandClass.getInstance(cls.getKey(), node, mockedController);
        assertNotNull(cls);

        return cmdCls;
    }
}
