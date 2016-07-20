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
import org.openhab.binding.zwave.internal.converter.ZWaveMultiLevelSensorConverter;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveEndpoint;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveMultiLevelSensorCommandClass;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveCommandClassValueEvent;

public class ZWaveMultiLevelSensorConverterTest {
    final ChannelUID uid = new ChannelUID("zwave:node:bridge:channel");

    private ZWaveThingChannel createChannel(String type) {
        Map<String, String> args = new HashMap<String, String>();
        args.put("type", type);
        return new ZWaveThingChannel(null, uid, DataType.DecimalType, CommandClass.SENSOR_MULTILEVEL.toString(), 0,
                args);
    }

    private ZWaveCommandClassValueEvent createEvent(ZWaveMultiLevelSensorCommandClass.SensorType type, int scale,
            BigDecimal value) {
        ZWaveController controller = Mockito.mock(ZWaveController.class);
        ZWaveNode node = Mockito.mock(ZWaveNode.class);
        ZWaveEndpoint endpoint = Mockito.mock(ZWaveEndpoint.class);
        ZWaveMultiLevelSensorCommandClass cls = new ZWaveMultiLevelSensorCommandClass(node, controller, endpoint);

        return cls.new ZWaveMultiLevelSensorValueEvent(1, 0, type, 0, value);
    }

    @Test
    public void Event_Luminance() {
        ZWaveMultiLevelSensorConverter converter = new ZWaveMultiLevelSensorConverter(null);
        ZWaveThingChannel channel = createChannel(ZWaveMultiLevelSensorCommandClass.SensorType.LUMINANCE.toString());
        BigDecimal value = new BigDecimal("103");

        ZWaveCommandClassValueEvent event = createEvent(ZWaveMultiLevelSensorCommandClass.SensorType.LUMINANCE, 0,
                value);

        State state = converter.handleEvent(channel, event);

        assertEquals(state.getClass(), DecimalType.class);
        assertEquals(((DecimalType) state).toBigDecimal(), value);
    }
}
