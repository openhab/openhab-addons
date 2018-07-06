/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.solaredge.internal.model;

import java.util.HashMap;
import java.util.Map;

import javax.measure.Quantity;
import javax.measure.Unit;

import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * this class is used to map the live data json response
 *
 * @author Alexander Friese - initial contribution
 */
public class LiveDataResponseMeterless implements DataResponse {
    private static final Logger logger = LoggerFactory.getLogger(LiveDataResponseMeterless.class);
    private static final Double ZERO_POWER = 0.0;

    public static class Power {
        public Double power;
    }

    public static class Energy {
        public Double energy;
    }

    public static class Overview {
        public Power currentPower;
        public Energy lastDayData;
        public Energy lastMonthData;
        public Energy lastYearData;
    }

    private Overview overview;

    @Override
    public Map<Channel, State> getValues() {
        Map<Channel, State> valueMap = new HashMap<>();

        if (overview != null) {

            if (overview.currentPower != null) {
                assignValue(valueMap, LiveDataChannels.PRODUCTION, overview.currentPower.power, SmartHomeUnits.WATT);
            } else {
                assignValue(valueMap, LiveDataChannels.PRODUCTION, null, null);
            }

            if (overview.lastDayData != null) {
                assignValue(valueMap, AggregateDataChannels.DAY_PRODUCTION, overview.lastDayData.energy,
                        SmartHomeUnits.WATT_HOUR);
            } else {
                assignValue(valueMap, AggregateDataChannels.DAY_PRODUCTION, null, null);
            }

            if (overview.lastMonthData != null) {
                assignValue(valueMap, AggregateDataChannels.MONTH_PRODUCTION, overview.lastMonthData.energy,
                        SmartHomeUnits.WATT_HOUR);
            } else {
                assignValue(valueMap, AggregateDataChannels.MONTH_PRODUCTION, null, null);
            }

            if (overview.lastYearData != null) {
                assignValue(valueMap, AggregateDataChannels.YEAR_PRODUCTION, overview.lastYearData.energy,
                        SmartHomeUnits.WATT_HOUR);
            } else {
                assignValue(valueMap, AggregateDataChannels.YEAR_PRODUCTION, null, null);
            }

            // week production is not available
            assignValue(valueMap, AggregateDataChannels.WEEK_PRODUCTION, null, null);

        }
        return valueMap;
    }

    /**
     * converts the value to QuantityType. If no value provided UnDefType.UNDEF will be used
     *
     * @param targetMap result will be put into this map
     * @param channel   channel to assign the value
     * @param value     the value to convert
     */
    protected final <T extends Quantity<T>> void assignValue(Map<Channel, State> targetMap, Channel channel,
            Double value, Unit<T> unit) {
        State result = UnDefType.UNDEF;

        if (value != null && unit != null) {
            result = new QuantityType<T>(value, unit);
        } else {
            logger.debug("Channel {}: no value/unit provided", channel);
        }
        targetMap.put(channel, result);
    }

    public final Overview getOverview() {
        return overview;
    }

    public final void setOverview(Overview overview) {
        this.overview = overview;
    }

}
