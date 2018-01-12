/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mihome.handler;

import static org.openhab.binding.mihome.XiaomiGatewayBindingConstants.CHANNEL_LEAK;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Thing;

import com.google.gson.JsonObject;

/**
 * Handles the Xiaomi water leak sensor
 *
 * @author Kuba Wolanin - Initial contribution
 */
public class XiaomiSensorWaterHandler extends XiaomiSensorBaseHandler {

    private static final String STATUS = "status";
    private static final String LEAK = "leak";

    @Override
    void parseReport(JsonObject data) {
        boolean leak = data.has(STATUS) && LEAK.equals(data.get(STATUS).getAsString());

        if (leak) {
            updateState(CHANNEL_LEAK, OnOffType.ON);
        } else {
            updateState(CHANNEL_LEAK, OnOffType.OFF);
        }
    }

    public XiaomiSensorWaterHandler(Thing thing) {
        super(thing);
    }
}
