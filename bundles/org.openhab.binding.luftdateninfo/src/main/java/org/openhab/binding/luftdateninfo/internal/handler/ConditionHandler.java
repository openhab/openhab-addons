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
package org.openhab.binding.luftdateninfo.internal.handler;

import static org.openhab.binding.luftdateninfo.internal.LuftdatenInfoBindingConstants.*;
import static org.openhab.binding.luftdateninfo.internal.utils.Constants.*;
import static org.openhab.core.library.unit.MetricPrefix.HECTO;

import java.util.List;

import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Pressure;
import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.luftdateninfo.internal.dto.SensorDataValue;
import org.openhab.binding.luftdateninfo.internal.utils.NumberUtils;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Thing;

/**
 * The {@link ConditionHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class ConditionHandler extends BaseSensorHandler {
    protected QuantityType<Temperature> temperatureCache = QuantityType.valueOf(-1, SIUnits.CELSIUS);
    protected QuantityType<Dimensionless> humidityCache = QuantityType.valueOf(-1, Units.PERCENT);
    protected QuantityType<Pressure> pressureCache = QuantityType.valueOf(-1, HECTO(SIUnits.PASCAL));
    protected QuantityType<Pressure> pressureSeaCache = QuantityType.valueOf(-1, HECTO(SIUnits.PASCAL));

    public ConditionHandler(Thing thing) {
        super(thing);
    }

    @Override
    public UpdateStatus updateChannels(@Nullable String json) {
        if (json != null) {
            List<SensorDataValue> valueList = HTTPHandler.getHandler().getLatestValues(json);
            if (valueList != null) {
                if (HTTPHandler.getHandler().isCondition(valueList)) {
                    valueList.forEach(v -> {
                        if (v.getValueType().endsWith(TEMPERATURE)) {
                            temperatureCache = QuantityType.valueOf(NumberUtils.round(v.getValue(), 1),
                                    SIUnits.CELSIUS);
                            updateState(TEMPERATURE_CHANNEL, temperatureCache);
                        } else if (v.getValueType().endsWith(HUMIDITY)) {
                            humidityCache = QuantityType.valueOf(NumberUtils.round(v.getValue(), 1), Units.PERCENT);
                            updateState(HUMIDITY_CHANNEL, humidityCache);
                        } else if (v.getValueType().endsWith(PRESSURE)) {
                            pressureCache = QuantityType.valueOf(
                                    NumberUtils.round(NumberUtils.convert(v.getValue()) / 100, 1),
                                    HECTO(SIUnits.PASCAL));
                            updateState(PRESSURE_CHANNEL, pressureCache);
                        } else if (v.getValueType().endsWith(PRESSURE_SEALEVEL)) {
                            pressureSeaCache = QuantityType.valueOf(
                                    NumberUtils.round(NumberUtils.convert(v.getValue()) / 100, 1),
                                    HECTO(SIUnits.PASCAL));
                            updateState(PRESSURE_SEA_CHANNEL, pressureSeaCache);
                        }
                    });
                    return UpdateStatus.OK;
                } else {
                    return UpdateStatus.VALUE_ERROR;
                }
            } else {
                return UpdateStatus.VALUE_EMPTY;
            }
        } else {
            return UpdateStatus.CONNECTION_ERROR;
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
