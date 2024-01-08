/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import static org.openhab.binding.mihome.internal.XiaomiGatewayBindingConstants.CHANNEL_SWITCH_CH0;

import org.openhab.binding.mihome.internal.ChannelMapper;
import org.openhab.core.thing.Thing;

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
