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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageClass;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageType;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveEndpoint;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.ZWaveSerialMessageException;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveMultiInstanceCommandClass;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveCommandClassValueEvent;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveEvent;

/**
 * Test cases for {@link ZWaveMultiInstanceCommandClassTest}.
 *
 * Since this is an encapsulation class, we try not to test all the 'end-to-end' cases, but more the process of
 * encapsulation and decapsulation.
 *
 * However, this class does provide the mock framework to process a multi-instance message, and handle the inner
 * classes, returning the command class notification events.
 *
 * Some end-to-end cases are run though.
 *
 * @author Chris Jackson - Initial version
 */
public class ZWaveMultiInstanceCommandClassTest extends ZWaveCommandClassTest {

    protected List<ZWaveEvent> processMessage(byte[] packetData) {
        SerialMessage msg = new SerialMessage(packetData);

        // Check the packet is not corrupted and is a command class request
        assertEquals(true, msg.isValid);
        assertEquals(SerialMessageType.Request, msg.getMessageType());
        assertEquals(SerialMessageClass.ApplicationCommandHandler, msg.getMessageClass());

        try {
            // This method only handles MULTI_INSTANCE
            assertEquals(msg.getMessagePayloadByte(3), CommandClass.MULTI_INSTANCE.getKey());

            // Mock the controller so we can get any events
            final ZWaveController mockedController = Mockito.mock(ZWaveController.class);
            argument = ArgumentCaptor.forClass(ZWaveEvent.class);
            Mockito.doNothing().when(mockedController).notifyEventListeners(argument.capture());
            final ZWaveNode node = Mockito.mock(ZWaveNode.class);
            // ZWaveEndpoint endpoint = Mockito.mock(ZWaveEndpoint.class);

            // Get the command class and process the response
            ZWaveMultiInstanceCommandClass cls = (ZWaveMultiInstanceCommandClass) ZWaveCommandClass
                    .getInstance(CommandClass.MULTI_INSTANCE.getKey(), node, mockedController);
            assertNotNull(cls);

            // Create an endpoint to capture the requests so we can return the command classes
            ZWaveEndpoint endpoint = Mockito.mock(ZWaveEndpoint.class);

            Mockito.when(endpoint.getCommandClass(Matchers.any(CommandClass.class)))
                    .thenAnswer(new Answer<ZWaveCommandClass>() {
                        @Override
                        public ZWaveCommandClass answer(InvocationOnMock invocation) {
                            return ZWaveCommandClass.getInstance(((CommandClass) invocation.getArguments()[0]).getKey(),
                                    node, mockedController);
                        }
                    });

            // Create a map of our mocked endpoints
            Map<Integer, ZWaveEndpoint> endpoints = new HashMap<Integer, ZWaveEndpoint>();
            endpoints.put(1, endpoint);
            endpoints.put(2, endpoint);
            endpoints.put(3, endpoint);
            endpoints.put(4, endpoint);
            endpoints.put(5, endpoint);
            endpoints.put(6, endpoint);

            // Now use reflection to set out fake endpoint list
            try {
                Class<?> clazz = cls.getClass();
                java.lang.reflect.Field field;
                field = clazz.getDeclaredField("endpoints");
                field.setAccessible(true);
                field.set(cls, endpoints);
            } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

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

    @Test
    public void MultiInstance_() {
        byte[] packetData = { 0x01, 0x0D, 0x00, 0x04, 0x00, 0x07, 0x07, 0x60, 0x0D, 0x02, 0x02, 0x25, 0x03, (byte) 0xFF,
                0x42 };

        List<ZWaveEvent> events = processMessage(packetData);
        assertEquals(events.size(), 1);
        ZWaveCommandClassValueEvent event = (ZWaveCommandClassValueEvent) events.get(0);

        assertEquals(event.getCommandClass(), CommandClass.SWITCH_BINARY);
        // assertEquals(event.getNodeId(), 44);
        assertEquals(event.getEndpoint(), 2);
        assertEquals(event.getValue(), new Integer("255"));
    }
}
