/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mihome.handler;

import static org.openhab.binding.mihome.XiaomiGatewayBindingConstants.*;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.Thing;

import com.google.gson.JsonObject;

/**
 * Handles the Xiaomi temperature & humidity sensor
 *
 * @author Patrick Boos - Initial contribution
 */
public class XiaomiSensorHtHandler extends XiaomiSensorBaseHandler {

    public XiaomiSensorHtHandler(Thing thing) {
        super(thing);
    }

    @Override
    void parseReport(JsonObject data) {
        if (data.get("humidity") != null) {
            float humidity = data.get("humidity").getAsFloat() / 100;
            updateState(CHANNEL_HUMIDITY, new DecimalType(humidity));
        }
        if (data.get("temperature") != null) {
            float temperature = data.get("temperature").getAsFloat() / 100;
            updateState(CHANNEL_TEMPERATURE, new DecimalType(temperature));
        }
        if (data.get("voltage") != null) {
            Integer voltage = data.get("voltage").getAsInt();
            calculateBatteryLevelFromVoltage(voltage);
        }
    }
}
