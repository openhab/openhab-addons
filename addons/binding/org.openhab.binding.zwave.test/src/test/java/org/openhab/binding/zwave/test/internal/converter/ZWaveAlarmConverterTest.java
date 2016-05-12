/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.test.internal.converter;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.types.State;
import org.junit.Test;
import org.mockito.Mockito;
import org.openhab.binding.zwave.handler.ZWaveThingChannel;
import org.openhab.binding.zwave.handler.ZWaveThingChannel.DataType;
import org.openhab.binding.zwave.internal.converter.ZWaveAlarmConverter;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveEndpoint;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveAlarmCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveAlarmCommandClass.AlarmType;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveCommandClassValueEvent;

public class ZWaveAlarmConverterTest {
    final ChannelUID uid = new ChannelUID("zwave:node:bridge:channel");

    private ZWaveThingChannel createChannel(String type, String event) {
        Map<String, String> args = new HashMap<String, String>();
        if (type != null) {
            args.put("type", type);
        }
        if (event != null) {
            args.put("event", event);
        }
        return new ZWaveThingChannel(null, uid, DataType.OnOffType, CommandClass.ALARM.toString(), 0, args);
    }

    private ZWaveCommandClassValueEvent createEvent(AlarmType type, Integer event, Integer status, Integer value) {
        ZWaveController controller = Mockito.mock(ZWaveController.class);
        ZWaveNode node = Mockito.mock(ZWaveNode.class);
        ZWaveEndpoint endpoint = Mockito.mock(ZWaveEndpoint.class);
        ZWaveAlarmCommandClass cls = new ZWaveAlarmCommandClass(node, controller, endpoint);

        return cls.new ZWaveAlarmValueEvent(1, 0, type, event, status, value);
    }

    @Test
    public void EventSmoke() {
        ZWaveAlarmConverter converter = new ZWaveAlarmConverter(null);
        ZWaveThingChannel channel = createChannel(AlarmType.SMOKE.toString(), "0");

        ZWaveCommandClassValueEvent event = createEvent(ZWaveAlarmCommandClass.AlarmType.SMOKE, 0xff, 0, 0);

        State state = converter.handleEvent(channel, event);

        // assertEquals(state.getClass(), OnOffType.class);
        // assertEquals(state, OnOffType.ON);
    }
}
