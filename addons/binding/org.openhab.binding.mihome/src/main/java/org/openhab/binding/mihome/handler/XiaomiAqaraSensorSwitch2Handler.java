/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mihome.handler;

import static org.openhab.binding.mihome.XiaomiGatewayBindingConstants.*;

import org.eclipse.smarthome.core.thing.CommonTriggerEvents;
import org.eclipse.smarthome.core.thing.Thing;
import org.openhab.binding.mihome.internal.ChannelMapper;

import com.google.gson.JsonObject;

/**
 * Handles the Xiaomi aqara smart switch with two buttons
 *
 * @author Dieter Schmidt - Initial contribution
 */
public class XiaomiAqaraSensorSwitch2Handler extends XiaomiSensorBaseHandler {

    private static final String CHANNEL_0 = "channel_0";
    private static final String CHANNEL_1 = "channel_1";
    private static final String DUAL_CHANNEL = "dual_channel";

    public XiaomiAqaraSensorSwitch2Handler(Thing thing) {
        super(thing);
    }

    @Override
    protected void parseReport(JsonObject data) {
        if (data.has(CHANNEL_0)) {
            triggerChannel(CHANNEL_SWITCH_CH0,
                    ChannelMapper.getChannelEvent(data.get(CHANNEL_0).getAsString().toUpperCase()));
        }
        if (data.has(CHANNEL_1)) {
            triggerChannel(CHANNEL_SWITCH_CH1,
                    ChannelMapper.getChannelEvent(data.get(CHANNEL_1).getAsString().toUpperCase()));
        }
        if (data.has(DUAL_CHANNEL)) {
            triggerChannel(CHANNEL_SWITCH_DUAL_CH, CommonTriggerEvents.SHORT_PRESSED);
        }
    }
}
