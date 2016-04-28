/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.test.internal.converter;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.types.State;
import org.junit.Test;
import org.mockito.Mockito;
import org.openhab.binding.zwave.handler.ZWaveThingChannel;
import org.openhab.binding.zwave.handler.ZWaveThingChannel.DataType;
import org.openhab.binding.zwave.internal.converter.ZWaveMeterConverter;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveEndpoint;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveMeterCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveMeterCommandClass.MeterScale;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveMeterCommandClass.MeterType;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveCommandClassValueEvent;

public class ZWaveMeterConverterTest {
    final ChannelUID uid = new ChannelUID("zwave:node:bridge:channel");

    private ZWaveThingChannel createChannel(String type) {
        Map<String, String> args = new HashMap<String, String>();
        args.put("type", type);
        return new ZWaveThingChannel(null, uid, DataType.DecimalType, CommandClass.METER.toString(), 0, args);
    }

    private ZWaveCommandClassValueEvent createEvent(MeterType type, MeterScale scale, BigDecimal value) {
        ZWaveController controller = Mockito.mock(ZWaveController.class);
        ZWaveNode node = Mockito.mock(ZWaveNode.class);
        ZWaveEndpoint endpoint = Mockito.mock(ZWaveEndpoint.class);
        ZWaveMeterCommandClass cls = new ZWaveMeterCommandClass(node, controller, endpoint);

        return cls.new ZWaveMeterValueEvent(1, 0, type, scale, value);
    }

    @Test
    public void Event_Electric() {
        ZWaveMeterConverter converter = new ZWaveMeterConverter(null);
        ZWaveThingChannel channel = createChannel(MeterScale.E_KWh.toString());
        BigDecimal value = new BigDecimal("3.3");

        ZWaveCommandClassValueEvent event = createEvent(ZWaveMeterCommandClass.MeterType.ELECTRIC,
                ZWaveMeterCommandClass.MeterScale.E_KWh, value);

        State state = converter.handleEvent(channel, event);

        assertEquals(state.getClass(), DecimalType.class);
        assertEquals(((DecimalType) state).toBigDecimal(), value);
    }
}
