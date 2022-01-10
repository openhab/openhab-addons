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

import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.Thing;

import com.google.gson.JsonObject;

/**
 * Handles the Xiaomi temperature & humidity sensor
 *
 * @author Patrick Boos - Initial contribution
 * @author Daniel Walters - Add pressure support
 */
public class XiaomiSensorHtHandler extends XiaomiSensorBaseHandler {

    private static final String HUMIDITY = "humidity";
    private static final String TEMPERATURE = "temperature";
    private static final String VOLTAGE = "voltage";
    private static final String PRESSURE = "pressure";

    public XiaomiSensorHtHandler(Thing thing) {
        super(thing);
    }

    @Override
    void parseReport(JsonObject data) {
        parseDefault(data);
    }

    @Override
    void parseHeartbeat(JsonObject data) {
        parseDefault(data);
    }

    @Override
    void parseReadAck(JsonObject data) {
        parseDefault(data);
    }

    @Override
    void parseDefault(JsonObject data) {
        if (data.has(HUMIDITY)) {
            float humidity = data.get(HUMIDITY).getAsFloat() / 100;
            updateState(CHANNEL_HUMIDITY, new QuantityType<>(humidity, PERCENT_UNIT));
        }
        if (data.has(TEMPERATURE)) {
            float temperature = data.get(TEMPERATURE).getAsFloat() / 100;
            updateState(CHANNEL_TEMPERATURE, new QuantityType<>(temperature, TEMPERATURE_UNIT));
        }
        if (data.has(VOLTAGE)) {
            Integer voltage = data.get(VOLTAGE).getAsInt();
            calculateBatteryLevelFromVoltage(voltage);
        }
        if (data.has(PRESSURE)) {
            float pressure = (float) (data.get(PRESSURE).getAsFloat() / 1000.0);
            updateState(CHANNEL_PRESSURE, new QuantityType<>(pressure, PRESSURE_UNIT));
        }
    }
}
