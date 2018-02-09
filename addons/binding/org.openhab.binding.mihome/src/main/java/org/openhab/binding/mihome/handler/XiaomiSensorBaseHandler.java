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

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

/**
 * Abstract base class for battery powered Xiaomi sensor devices
 *
 * @author Dieter Schmidt - Initial contribution
 */
public abstract class XiaomiSensorBaseHandler extends XiaomiDeviceBaseHandler {

    private static final int VOLTAGE_MAX_MILLIVOLTS = 3100;
    private static final int VOLTAGE_MIN_MILLIVOLTS = 2700;
    private static final int BATT_LEVEL_LOW = 20;
    private static final int BATT_LEVEL_LOW_HYS = 5;

    private static final String STATUS = "status";
    private static final String VOLTAGE = "voltage";

    private boolean isBatteryLow = false;

    private final Logger logger = LoggerFactory.getLogger(XiaomiSensorBaseHandler.class);

    public XiaomiSensorBaseHandler(Thing thing) {
        super(thing);
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
        if (data.get(VOLTAGE) != null) {
            Integer voltage = data.get(VOLTAGE).getAsInt();
            calculateBatteryLevelFromVoltage(voltage);
        }
        if (data.get(STATUS) != null) {
            logger.trace(
                    "Got status {} - Apart from \"report\" all other status updates for sensors seem not right (Firmware 1.4.1.145)",
                    data.get(STATUS));
        }
    }

    void calculateBatteryLevelFromVoltage(Integer voltage) {
        voltage = Math.min(VOLTAGE_MAX_MILLIVOLTS, voltage);
        voltage = Math.max(VOLTAGE_MIN_MILLIVOLTS, voltage);
        Integer battLevel = (int) ((float) (voltage - VOLTAGE_MIN_MILLIVOLTS)
                / (float) (VOLTAGE_MAX_MILLIVOLTS - VOLTAGE_MIN_MILLIVOLTS) * 100);
        updateState(CHANNEL_BATTERY_LEVEL, new DecimalType(battLevel));
        int lowThreshold = isBatteryLow ? BATT_LEVEL_LOW + BATT_LEVEL_LOW_HYS : BATT_LEVEL_LOW;
        if (battLevel <= lowThreshold) {
            updateState(CHANNEL_LOW_BATTERY, OnOffType.ON);
            isBatteryLow = true;
        } else {
            updateState(CHANNEL_LOW_BATTERY, OnOffType.OFF);
            isBatteryLow = false;
        }
    }

    @Override
    void execute(ChannelUID channelUID, Command command) {
        logger.warn("Cannot execute command - Sensors by definition only have read only channels");
    }
}
