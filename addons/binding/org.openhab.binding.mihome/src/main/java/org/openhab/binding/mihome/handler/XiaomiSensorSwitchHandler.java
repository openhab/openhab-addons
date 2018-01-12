/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mihome.handler;

import static org.openhab.binding.mihome.XiaomiGatewayBindingConstants.CHANNEL_BUTTON;

import org.eclipse.smarthome.core.thing.Thing;
import org.openhab.binding.mihome.internal.ChannelMapper;

import com.google.gson.JsonObject;

/**
 * Handles the Xiaomi smart switch device
 *
 * @author Patrick Boos - Initial contribution
 */
public class XiaomiSensorSwitchHandler extends XiaomiSensorBaseHandler {

    private static final String STATUS = "status";

    public XiaomiSensorSwitchHandler(Thing thing) {
        super(thing);
    }

    @Override
    void parseReport(JsonObject data) {
        if (data.has(STATUS)) {
            triggerChannel(CHANNEL_BUTTON, ChannelMapper.getChannelEvent(data.get(STATUS).getAsString().toUpperCase()));
        }
    }
}
