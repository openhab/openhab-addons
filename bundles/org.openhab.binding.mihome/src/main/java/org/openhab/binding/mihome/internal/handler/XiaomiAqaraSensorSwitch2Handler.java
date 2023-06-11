/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.mihome.internal.handler;

import static org.openhab.binding.mihome.internal.XiaomiGatewayBindingConstants.*;

import org.openhab.binding.mihome.internal.ChannelMapper;
import org.openhab.core.thing.Thing;

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
            triggerChannel(CHANNEL_SWITCH_DUAL_CH,
                    ChannelMapper.getChannelEvent(data.get(DUAL_CHANNEL).getAsString().toUpperCase()));
        }
    }
}
