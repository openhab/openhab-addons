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
 * The {@link PMHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class PMHandler extends BaseSensorHandler {

    protected @Nullable DecimalType pm25Cache;
    protected @Nullable DecimalType pm100Cache;

    public PMHandler(Thing thing) {
        super(thing);
    }

    @Override
    public int updateChannels(@Nullable String json) {
        if (json != null) {
            List<SensorDataValue> valueList = HTTPHandler.getLatestValues(json);
            if (valueList != null) {
                if (HTTPHandler.isParticulate(valueList)) {

                    Iterator<SensorDataValue> iter = valueList.iterator();
                    while (iter.hasNext()) {
                        SensorDataValue v = iter.next();
                        if (v.getValue_type().equals(P1)) {
                            pm100Cache = new DecimalType(v.getValue());
                            updateState(PM100_CHANNEL, pm100Cache);
                        } else if (v.getValue_type().equals(P2)) {
                            pm25Cache = new DecimalType(v.getValue());
                            updateState(PM25_CHANNEL, pm25Cache);
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
        if (pm25Cache != null) {
            updateState(PM25_CHANNEL, pm25Cache);
        } else {
            logger.debug("No cached values for PM25 available");
        }
        if (pm100Cache != null) {
            updateState(PM100_CHANNEL, pm100Cache);
        } else {
            logger.debug("No cached values for PM100 available");
        }
    }
}
