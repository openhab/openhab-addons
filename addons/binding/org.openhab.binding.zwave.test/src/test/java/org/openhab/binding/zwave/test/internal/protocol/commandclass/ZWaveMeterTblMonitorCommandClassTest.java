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
import java.util.List;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageClass;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageType;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.ZWaveSerialMessageException;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveMeterTblMonitorCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveMeterTblMonitorCommandClass.ZWaveMeterTblMonitorValueEvent;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveEvent;

/**
 * Test cases for {@link ZWaveMeterTblMonitorCommandClass}.
 *
 * @author Jorg de Jong - Initial version
 */
public class ZWaveMeterTblMonitorCommandClassTest {

    @Test
    public void Gas_Cubic_Meters() {

        byte[] initData = { 0x01, 0x13, 0x00, 0x04, 0x00, 0x07, 0x0D, 0x3D, 0x06, 0x42, 0x01, 0x03, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0x99 };

        byte[] packetData = { 0x01, 0x26, 0x00, 0x04, 0x00, 0x07, 0x18, 0x3D, 0x0D, 0x00, 0x01, 0x03, 0x00, 0x00, 0x00,
                0x00, 0x01, 0x01, 0x01, 0x1B, 0x19, 0x60, 0x00, 0x00, 0x02, 0x6C, 0x68, 0x00, 0x00, 0x00, 0x00, -107 };

        List<ZWaveEvent> events = processCommandClassMessages(
                Arrays.asList(new SerialMessage(initData), new SerialMessage(packetData)));

        assertEquals(events.size(), 1);

        ZWaveMeterTblMonitorValueEvent event = (ZWaveMeterTblMonitorValueEvent) events.get(0);

        assertEquals(event.getCommandClass(), CommandClass.METER_TBL_MONITOR);
        assertEquals(event.getMeterScale(), ZWaveMeterTblMonitorCommandClass.MeterTblMonitorScale.G_Cubic_Meters);
        assertEquals(event.getMeterType(), ZWaveMeterTblMonitorCommandClass.MeterTblMonitorType.GAS);
        assertEquals(event.getValue(), new BigDecimal("0.620"));
    }

    protected List<ZWaveEvent> processCommandClassMessages(List<SerialMessage> msgs) {

        // Mock the controller so we can get any events
        ZWaveController controller = Mockito.mock(ZWaveController.class);
        ArgumentCaptor<ZWaveEvent> argument = ArgumentCaptor.forClass(ZWaveEvent.class);
        Mockito.doNothing().when(controller).notifyEventListeners(argument.capture());
        ZWaveNode node = Mockito.mock(ZWaveNode.class);
        ZWaveCommandClass cls = null;

        for (SerialMessage msg : msgs) {

            // Check the packet is not corrupted and is a command class request
            assertEquals(true, msg.isValid);
            assertEquals(SerialMessageType.Request, msg.getMessageType());
            assertEquals(SerialMessageClass.ApplicationCommandHandler, msg.getMessageClass());

            // Get the command class and process the response
            try {
                if (cls == null) {
                    cls = ZWaveCommandClass.getInstance(msg.getMessagePayloadByte(3), node, controller);
                    assertNotNull(cls);
                } else {
                    // ensure a 2nd msg uses the same command class
                    assertEquals(cls.getCommandClass().getKey(), msg.getMessagePayloadByte(3));
                }
                cls.handleApplicationCommandRequest(msg, 4, 0);
            } catch (ZWaveSerialMessageException e) {
                fail("Out of bounds exception processing data");
            }
        }
        // Check that we received a response
        assertNotNull(argument.getAllValues());
        assertNotEquals(argument.getAllValues().size(), 0);

        // Return all the notifications....
        return argument.getAllValues();
    }
}
