/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.solaredge.internal.model;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * this class is used to map the live data json response
 *
 * @author Alexander Friese - initial contribution
 */
public class LiveDataResponseMeterless implements DataResponse {
    private static final String ZERO_POWER = "0.0";

    public static class Power {
        public String power;
    }

    public static class Energy {
        public String energy;
    }

    public static class Overview {
        public Power currentPower;
        public Energy lastDayData;
        public Energy lastMonthData;
        public Energy lastYearData;
    }

    private Overview overview;

    @Override
    public Map<String, String> getValues() {
        Map<String, String> valueMap = new HashMap<>();

        if (overview != null) {

            // init fields with zero
            valueMap.put(LiveDataChannels.PRODUCTION.getFQName(), ZERO_POWER);
            valueMap.put(AggregateDataChannels.DAY_PRODUCTION.getFQName(), ZERO_POWER);
            valueMap.put(AggregateDataChannels.WEEK_PRODUCTION.getFQName(), ZERO_POWER);
            valueMap.put(AggregateDataChannels.MONTH_PRODUCTION.getFQName(), ZERO_POWER);
            valueMap.put(AggregateDataChannels.YEAR_PRODUCTION.getFQName(), ZERO_POWER);

            if (overview.currentPower != null) {
                valueMap.put(LiveDataChannels.PRODUCTION.getFQName(), getValueAsKW(overview.currentPower.power));
            }

            if (overview.lastDayData != null) {
                valueMap.put(AggregateDataChannels.DAY_PRODUCTION.getFQName(),
                        getValueAsKW(overview.lastDayData.energy));
            }

            if (overview.lastMonthData != null) {
                valueMap.put(AggregateDataChannels.MONTH_PRODUCTION.getFQName(),
                        getValueAsKW(overview.lastMonthData.energy));
            }

            if (overview.lastYearData != null) {
                valueMap.put(AggregateDataChannels.YEAR_PRODUCTION.getFQName(),
                        getValueAsKW(overview.lastYearData.energy));
            }

        }
        return valueMap;
    }

    /**
     * converts the value to kW / kWh
     *
     * @param value value retrievd from Solaredge
     * @return converted value
     */
    private String getValueAsKW(String value) {
        Double convertedValue = Double.valueOf(value);
        convertedValue = convertedValue / 1000;

        DecimalFormat df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.HALF_UP);
        return df.format(convertedValue);
    }

    public final Overview getOverview() {
        return overview;
    }

    public final void setOverview(Overview overview) {
        this.overview = overview;
    }

}
