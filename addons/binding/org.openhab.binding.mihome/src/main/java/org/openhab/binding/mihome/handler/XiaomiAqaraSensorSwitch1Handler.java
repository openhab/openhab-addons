/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mihome.handler;

import static org.openhab.binding.mihome.XiaomiGatewayBindingConstants.CHANNEL_SWITCH_CH0;

import org.eclipse.smarthome.core.thing.Thing;
import org.openhab.binding.mihome.internal.ChannelMapper;

import com.google.gson.JsonObject;

/**
 * Handles the Xiaomi aqara smart switch with one button
 *
 * @author Dieter Schmidt - Initial contribution
 */
public class XiaomiAqaraSensorSwitch1Handler extends XiaomiSensorBaseHandler {

    private static final String CHANNEL_0 = "channel_0";

    public XiaomiAqaraSensorSwitch1Handler(Thing thing) {
        super(thing);
    }

    @Override
    protected void parseReport(JsonObject data) {
        if (data.has(CHANNEL_0)) {
            triggerChannel(CHANNEL_SWITCH_CH0,
                    ChannelMapper.getChannelEvent(data.get(CHANNEL_0).getAsString().toUpperCase()));
        }
    }
}
