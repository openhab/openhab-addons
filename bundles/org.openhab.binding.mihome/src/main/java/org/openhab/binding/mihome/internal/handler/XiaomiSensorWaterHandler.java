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

import static org.openhab.binding.mihome.internal.XiaomiGatewayBindingConstants.CHANNEL_LEAK;

import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Thing;

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
