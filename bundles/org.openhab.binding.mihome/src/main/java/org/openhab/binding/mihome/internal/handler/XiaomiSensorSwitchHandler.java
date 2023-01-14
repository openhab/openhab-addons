/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import static org.openhab.binding.mihome.internal.XiaomiGatewayBindingConstants.CHANNEL_BUTTON;

import org.openhab.binding.mihome.internal.ChannelMapper;
import org.openhab.core.thing.Thing;

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
