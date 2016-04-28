/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.test.internal.converter;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.types.State;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.openhab.binding.zwave.handler.ZWaveThingChannel;
import org.openhab.binding.zwave.handler.ZWaveThingChannel.DataType;
import org.openhab.binding.zwave.internal.converter.ZWaveThermostatFanStateConverter;
import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveThermostatFanStateCommandClass;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveCommandClassValueEvent;

public class ZWaveThermostatFanStateConverterTest {
    final ChannelUID uid = new ChannelUID("zwave:node:bridge:channel");

    private ZWaveThingChannel createChannel() {
        Map<String, String> args = new HashMap<String, String>();
        return new ZWaveThingChannel(null, uid, DataType.DecimalType, CommandClass.THERMOSTAT_FAN_STATE.toString(), 0,
                args);
    }

    @Test
    public void FanState() {
        ZWaveThermostatFanStateConverter converter = new ZWaveThermostatFanStateConverter(null);
        ZWaveThingChannel channel = createChannel();
        Integer value = new Integer(3);

        ZWaveCommandClassValueEvent event = new ZWaveCommandClassValueEvent(0, 0, CommandClass.THERMOSTAT_FAN_STATE,
                value);
        State state = converter.handleEvent(channel, event);

        assertEquals(state.getClass(), DecimalType.class);
        assertEquals(((DecimalType) state).intValue(), value.intValue());
    }

    @Test
    public void refresh() {
        // Setup mocks
        ZWaveThermostatFanStateConverter converter = new ZWaveThermostatFanStateConverter(null);
        ZWaveThingChannel channel = createChannel();
        ZWaveNode node = Mockito.mock(ZWaveNode.class);
        ZWaveThermostatFanStateCommandClass cls = mock(ZWaveThermostatFanStateCommandClass.class);
        when(node.resolveCommandClass(Matchers.eq(CommandClass.THERMOSTAT_FAN_STATE), Matchers.anyInt()))
                .thenReturn(cls);
        when(cls.getValueMessage()).thenReturn(new SerialMessage());
        when(cls.getCommandClass()).thenReturn(CommandClass.THERMOSTAT_FAN_STATE);

        // the actual call
        List<SerialMessage> result = converter.executeRefresh(channel, node);

        // verify the results
        assertNotNull(result);
        assertEquals(result.size(), 1);
        verify(node, times(1)).encapsulate(Matchers.any(SerialMessage.class),
                Matchers.any(ZWaveThermostatFanStateCommandClass.class), Matchers.anyInt());
        verify(cls, times(1)).getValueMessage();
    }
}
