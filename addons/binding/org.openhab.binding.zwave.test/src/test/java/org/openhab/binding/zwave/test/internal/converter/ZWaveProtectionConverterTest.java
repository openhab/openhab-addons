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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.types.State;
import org.junit.Test;
import org.openhab.binding.zwave.handler.ZWaveThingChannel;
import org.openhab.binding.zwave.handler.ZWaveThingChannel.DataType;
import org.openhab.binding.zwave.internal.converter.ZWaveProtectionConverter;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveProtectionCommandClass.LocalProtectionType;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveProtectionCommandClass.RfProtectionType;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveProtectionCommandClass.Type;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveCommandClassValueEvent;

public class ZWaveProtectionConverterTest {
    final ChannelUID uid = new ChannelUID("zwave:node:bridge:channel");

    private ZWaveThingChannel createChannel(String type) {
        Map<String, String> args = new HashMap<String, String>();
        args.put("type", type);
        return new ZWaveThingChannel(null, uid, DataType.DecimalType, CommandClass.PROTECTION.toString(), 0, args);
    }

    @Test
    public void LocalProtectionEvent() {
        ZWaveProtectionConverter converter = new ZWaveProtectionConverter(null);
        ZWaveThingChannel channel = createChannel(Type.PROTECTION_LOCAL.name());

        ZWaveCommandClassValueEvent event = new ZWaveCommandClassValueEvent(0, 0, CommandClass.PROTECTION,
                LocalProtectionType.SEQUENCE, Type.PROTECTION_LOCAL);

        State state = converter.handleEvent(channel, event);

        assertEquals(state.getClass(), DecimalType.class);
        assertEquals(((DecimalType) state).intValue(), LocalProtectionType.SEQUENCE.ordinal());
    }

    @Test
    public void RFProtectionEvent() {
        ZWaveProtectionConverter converter = new ZWaveProtectionConverter(null);
        ZWaveThingChannel channel = createChannel(Type.PROTECTION_RF.name());

        ZWaveCommandClassValueEvent event = new ZWaveCommandClassValueEvent(0, 0, CommandClass.PROTECTION,
                RfProtectionType.NORFRESPONSE, Type.PROTECTION_RF);

        State state = converter.handleEvent(channel, event);

        assertEquals(state.getClass(), DecimalType.class);
        assertEquals(((DecimalType) state).intValue(), RfProtectionType.NORFRESPONSE.ordinal());
    }

}
