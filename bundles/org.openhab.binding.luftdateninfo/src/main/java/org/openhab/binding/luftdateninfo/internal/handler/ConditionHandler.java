/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.luftdateninfo.internal.handler;

import static org.openhab.binding.luftdateninfo.internal.LuftdatenInfoBindingConstants.*;
import static org.openhab.binding.luftdateninfo.internal.handler.HTTPHandler.*;

import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.Thing;
import org.openhab.binding.luftdateninfo.internal.dto.SensorDataValue;
import org.openhab.binding.luftdateninfo.internal.utils.NumberUtils;

/**
 * The {@link ConditionHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class ConditionHandler extends BaseSensorHandler {

    protected @Nullable DecimalType temperatureCache;
    protected @Nullable DecimalType humidityCache;
    protected @Nullable DecimalType pressureCache;
    protected @Nullable DecimalType pressureSeaCache;
    protected DecimalType UNDEF = new DecimalType(-1);

    public ConditionHandler(Thing thing) {
        super(thing);
    }

    @Override
    public int updateChannels(@Nullable String json) {
        if (json != null) {
            List<SensorDataValue> valueList = HTTPHandler.getValues(json);
            if (valueList != null) {
                if (HTTPHandler.isCondition(valueList)) {
                    Iterator<SensorDataValue> iter = valueList.iterator();
                    while (iter.hasNext()) {
                        SensorDataValue v = iter.next();
                        if (v.getValue_type().equals(TEMPERATURE)) {
                            temperatureCache = new DecimalType(v.getValue());
                            updateState(TEMPERATURE_CHANNEL, temperatureCache);
                        } else if (v.getValue_type().equals(HUMIDITY)) {
                            humidityCache = new DecimalType(v.getValue());
                            updateState(HUMIDITY_CHANNEL, humidityCache);
                        } else if (v.getValue_type().equals(PRESSURE)) {
                            double pressure = Double.parseDouble(v.getValue()) / 100;
                            pressureCache = new DecimalType(NumberUtils.round(pressure, 2));
                            updateState(PRESSURE_CHANNEL, pressureCache);
                        } else if (v.getValue_type().equals(PRESSURE_SEALEVEL)) {
                            double pressureSea = Double.parseDouble(v.getValue()) / 100;
                            pressureSeaCache = new DecimalType(NumberUtils.round(pressureSea, 2));
                            updateState(PRESSURE_SEA_CHANNEL, pressureSeaCache);
                        }
                    }
                    // if optional pressure values are not deliverd put them to undefined
                    if (pressureCache == null) {
                        logger.info("Pressure Info not delivered by this sensor");
                        pressureCache = UNDEF;
                        updateState(PRESSURE_CHANNEL, pressureCache);
                    }
                    if (pressureSeaCache == null) {
                        logger.info("Pressure Info not delivered by this sensor");
                        pressureSeaCache = UNDEF;
                        updateState(PRESSURE_SEA_CHANNEL, pressureSeaCache);
                    }
                    return UPDATE_OK;
                } else {
                    return UPDATE_VALUE_ERROR;
                }
            } else {
                return UPDATE_VALUE_EMPTY;
            }
        } else {
            return UPDATE_CONNECTION_ERROR;
        }
    }

    @Override
    protected void updateFromCache() {
        if (temperatureCache != null) {
            updateState(TEMPERATURE_CHANNEL, temperatureCache);
        } else {
            logger.debug("No cached values for Temperature available");
        }
        if (humidityCache != null) {
            updateState(HUMIDITY_CHANNEL, humidityCache);
        } else {
            logger.debug("No cached values for Humidity available");
        }
        if (pressureCache != null) {
            updateState(PRESSURE_CHANNEL, pressureCache);
        } else {
            logger.debug("No cached values for Pressure available");
        }
        if (pressureSeaCache != null) {
            updateState(PRESSURE_SEA_CHANNEL, pressureSeaCache);
        } else {
            logger.debug("No cached values for Pressure Sealevel available");
        }
    }
}
