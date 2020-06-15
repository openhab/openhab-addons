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

    protected DecimalType temperatureCache = UNDEF;
    protected DecimalType humidityCache = UNDEF;
    protected DecimalType pressureCache = UNDEF;
    protected DecimalType pressureSeaCache = UNDEF;

    public ConditionHandler(Thing thing) {
        super(thing);
    }

    @Override
    public int updateChannels(@Nullable String json) {
        if (json != null) {
            List<SensorDataValue> valueList = HTTPHandler.getLatestValues(json);
            if (valueList != null) {
                if (HTTPHandler.isCondition(valueList)) {
                    Iterator<SensorDataValue> iter = valueList.iterator();
                    while (iter.hasNext()) {
                        SensorDataValue v = iter.next();
                        if (v.getValue_type().equals(TEMPERATURE)) {
                            temperatureCache = new DecimalType(NumberUtils.round(v.getValue(), 1));
                            updateState(TEMPERATURE_CHANNEL, temperatureCache);
                        } else if (v.getValue_type().equals(HUMIDITY)) {
                            humidityCache = new DecimalType(NumberUtils.round(v.getValue(), 1));
                            updateState(HUMIDITY_CHANNEL, humidityCache);
                        } else if (v.getValue_type().equals(PRESSURE)) {
                            double pressure = NumberUtils.convert(v.getValue()) / 100;
                            pressureCache = new DecimalType(NumberUtils.round(pressure, 1));
                            updateState(PRESSURE_CHANNEL, pressureCache);
                        } else if (v.getValue_type().equals(PRESSURE_SEALEVEL)) {
                            double pressureSea = NumberUtils.convert(v.getValue()) / 100;
                            pressureSeaCache = new DecimalType(NumberUtils.round(pressureSea, 1));
                            updateState(PRESSURE_SEA_CHANNEL, pressureSeaCache);
                        }
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
        updateState(TEMPERATURE_CHANNEL, temperatureCache);
        updateState(HUMIDITY_CHANNEL, humidityCache);
        updateState(PRESSURE_CHANNEL, pressureCache);
        updateState(PRESSURE_SEA_CHANNEL, pressureSeaCache);
    }
}
