/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.sensorcommunity.internal.handler;

import static org.openhab.binding.sensorcommunity.internal.SensorCommunityBindingConstants.*;
import static org.openhab.binding.sensorcommunity.internal.utils.Constants.*;
import static org.openhab.core.library.unit.MetricPrefix.HECTO;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sensorcommunity.internal.dto.SensorDataValue;
import org.openhab.binding.sensorcommunity.internal.utils.NumberUtils;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * The {@link ConditionHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Bernd Weymann - Initial contribution
 * @author Bernd Weymann - Cache initialization with UNDEF
 */
@NonNullByDefault
public class ConditionHandler extends BaseSensorHandler {
    protected State temperatureCache = UnDefType.UNDEF;
    protected State humidityCache = UnDefType.UNDEF;
    protected State pressureCache = UnDefType.UNDEF;
    protected State pressureSeaCache = UnDefType.UNDEF;

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
