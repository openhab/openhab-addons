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

import java.util.List;

import javax.measure.quantity.Dimensionless;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.luftdateninfo.internal.dto.SensorDataValue;
import org.openhab.binding.luftdateninfo.internal.utils.NumberUtils;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Thing;

/**
 * The {@link NoiseHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class NoiseHandler extends BaseSensorHandler {
    protected QuantityType<Dimensionless> noiseEQCache = QuantityType.valueOf(-1, Units.DECIBEL);
    protected QuantityType<Dimensionless> noiseMinCache = QuantityType.valueOf(-1, Units.DECIBEL);
    protected QuantityType<Dimensionless> noiseMaxCache = QuantityType.valueOf(-1, Units.DECIBEL);

    public NoiseHandler(Thing thing) {
        super(thing);
    }

    @Override
    public UpdateStatus updateChannels(@Nullable String json) {
        if (json != null) {
            List<SensorDataValue> valueList = HTTPHandler.getHandler().getLatestValues(json);
            if (valueList != null) {
                if (HTTPHandler.getHandler().isNoise(valueList)) {
                    valueList.forEach(v -> {
                        if (v.getValueType().endsWith(NOISE_EQ)) {
                            noiseEQCache = QuantityType.valueOf(NumberUtils.round(v.getValue(), 1), Units.DECIBEL);
                            updateState(NOISE_EQ_CHANNEL, noiseEQCache);
                        } else if (v.getValueType().endsWith(NOISE_MIN)) {
                            noiseMinCache = QuantityType.valueOf(NumberUtils.round(v.getValue(), 1), Units.DECIBEL);
                            updateState(NOISE_MIN_CHANNEL, noiseMinCache);
                        } else if (v.getValueType().endsWith(NOISE_MAX)) {
                            noiseMaxCache = QuantityType.valueOf(NumberUtils.round(v.getValue(), 1), Units.DECIBEL);
                            updateState(NOISE_MAX_CHANNEL, noiseMaxCache);
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
        updateState(NOISE_EQ_CHANNEL, noiseEQCache);
        updateState(NOISE_MIN_CHANNEL, noiseMinCache);
        updateState(NOISE_MAX_CHANNEL, noiseMaxCache);
    }
}
