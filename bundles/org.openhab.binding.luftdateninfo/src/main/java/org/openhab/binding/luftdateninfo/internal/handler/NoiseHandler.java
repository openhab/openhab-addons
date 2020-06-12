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

/**
 * The {@link NoiseHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class NoiseHandler extends BaseSensorHandler {
    protected @Nullable DecimalType noiseEQCache;
    protected @Nullable DecimalType noiseMinCache;
    protected @Nullable DecimalType noiseMaxCache;

    public NoiseHandler(Thing thing) {
        super(thing);
    }

    @Override
    public int updateChannels(@Nullable String json) {
        if (json != null) {
            List<SensorDataValue> valueList = HTTPHandler.getLatestValues(json);
            if (valueList != null) {
                if (HTTPHandler.isNoise(valueList)) {

                    Iterator<SensorDataValue> iter = valueList.iterator();
                    while (iter.hasNext()) {
                        SensorDataValue v = iter.next();
                        if (v.getValue_type().equals(NOISE_EQ)) {
                            noiseEQCache = new DecimalType(v.getValue());
                            updateState(NOISE_EQ_CHANNEL, noiseEQCache);
                        } else if (v.getValue_type().equals(NOISE_MIN)) {
                            noiseMinCache = new DecimalType(v.getValue());
                            updateState(NOISE_MIN_CHANNEL, noiseMinCache);
                        } else if (v.getValue_type().equals(NOISE_MAX)) {
                            noiseMaxCache = new DecimalType(v.getValue());
                            updateState(NOISE_MAX_CHANNEL, noiseMaxCache);
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
        if (noiseEQCache != null) {
            updateState(NOISE_EQ_CHANNEL, noiseEQCache);
        } else {
            logger.debug("No cached values for Noise EQ available");
        }
        if (noiseMinCache != null) {
            updateState(NOISE_MIN_CHANNEL, noiseMinCache);
        } else {
            logger.debug("No cached values for Noise Min available");
        }
        if (noiseMaxCache != null) {
            updateState(NOISE_MAX_CHANNEL, noiseMaxCache);
        } else {
            logger.debug("No cached values for Noise Max available");
        }
    }
}
