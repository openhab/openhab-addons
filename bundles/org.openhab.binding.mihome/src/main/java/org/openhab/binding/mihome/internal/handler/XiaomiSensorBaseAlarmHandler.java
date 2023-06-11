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

import static org.openhab.binding.mihome.internal.XiaomiGatewayBindingConstants.*;

import java.util.HashMap;
import java.util.Map;

import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Thing;

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
                updateState(CHANNEL_STATUS, StringType.valueOf(status));
            } else {
                updateState(CHANNEL_STATUS, StringType.valueOf(UNKNOWN));
            }
        }
    }
}
