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

import static org.openhab.binding.mihome.internal.XiaomiGatewayBindingConstants.*;

import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
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
        int limVoltage = voltage;
        limVoltage = Math.min(VOLTAGE_MAX_MILLIVOLTS, limVoltage);
        limVoltage = Math.max(VOLTAGE_MIN_MILLIVOLTS, limVoltage);
        Integer battLevel = (int) ((float) (limVoltage - VOLTAGE_MIN_MILLIVOLTS)
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
