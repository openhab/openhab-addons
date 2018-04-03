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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Thing;

import com.google.gson.JsonObject;

/**
 * Abstract base class for Xiaomi sensor devices, which provide an alarm status message
 *
 * @author Dieter Schmidt - Initial contribution
 */
public abstract class XiaomiSensorBaseAlarmHandler extends XiaomiSensorBaseHandler {

    private static final Map<Integer, String> ALARM_STATUS_MAP = new HashMap<>();
    private static final String ALARM = "alarm";
    private static final String UNKNOWN = "unknown";

    public XiaomiSensorBaseAlarmHandler(Thing thing) {
        super(thing);
    }

    @Override
    void parseReport(JsonObject data) {
        if (data.has(ALARM)) {
            int alarm = data.get(ALARM).getAsInt();
            // alarm channel only receives the "real alarm"
            if (alarm == 1) {
                updateState(CHANNEL_ALARM, OnOffType.ON);
            } else {
                updateState(CHANNEL_ALARM, OnOffType.OFF);
            }
            // status shows faults
            String status = ALARM_STATUS_MAP.get(alarm);
            if (status != null) {
                updateState(CHANNEL_ALARM_STATUS, StringType.valueOf(status));
            } else {
                updateState(CHANNEL_ALARM_STATUS, StringType.valueOf(UNKNOWN));
            }
        }
    }
}
