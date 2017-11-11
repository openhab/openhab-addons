/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.solaredge.internal.model;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * this class is used to map the aggregate data json response
 *
 * @author Alexander Friese - initial contribution
 *
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class AggregateDataResponse implements DataResponse {

    private static final String UNIT_WH = "Wh";
    private static final String UNIT_KWH = "KWh";

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Value {
        public Double value;
        public String unit;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ValueAndPercent extends Value {
        public Double percentage;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UtilizationMeasures {
        public Value production;
        public Value consumption;
        public ValueAndPercent selfConsumptionForConsumption;
        public ValueAndPercent batterySelfConsumption;
        @JsonProperty("import")
        public ValueAndPercent imported;
        public ValueAndPercent export;
    }

    private UtilizationMeasures utilizationMeasures;

    @Override
    public Map<String, String> getValues() {
        Map<String, String> valueMap = new HashMap<>();

        valueMap.put(AggregateDataChannels.PRODUCTION.getFQName(), getValueAsKWh(utilizationMeasures.production));
        valueMap.put(AggregateDataChannels.CONSUMPTION.getFQName(), getValueAsKWh(utilizationMeasures.consumption));
        valueMap.put(AggregateDataChannels.SELFCONSUMPTIONFORCONSUMPTION.getFQName(),
                getValueAsKWh(utilizationMeasures.selfConsumptionForConsumption));
        valueMap.put(AggregateDataChannels.SELFCONSUMPTIONCOVERAGE.getFQName(),
                getValueAsPercent(utilizationMeasures.selfConsumptionForConsumption));

        valueMap.put(AggregateDataChannels.BATTERYSELFCONSUMPTION.getFQName(),
                getValueAsKWh(utilizationMeasures.batterySelfConsumption));
        valueMap.put(AggregateDataChannels.IMPORT.getFQName(), getValueAsKWh(utilizationMeasures.imported));
        valueMap.put(AggregateDataChannels.EXPORT.getFQName(), getValueAsKWh(utilizationMeasures.export));

        return valueMap;
    }

    private String getValueAsKWh(Value value) {
        if (value.unit != null && value.unit.equals(UNIT_WH)) {
            Double convertedValue = value.value / 1000;
            return convertedValue.toString();
        } else {
            return value.value.toString();
        }
    }

    private String getValueAsPercent(ValueAndPercent value) {
        if (value.percentage != null) {
            Double convertedValue = value.percentage * 100;
            return convertedValue.toString();
        }
        return null;
    }

    public final UtilizationMeasures getUtilizationMeasures() {
        return utilizationMeasures;
    }

    public final void setUtilizationMeasures(UtilizationMeasures utilizationMeasures) {
        this.utilizationMeasures = utilizationMeasures;
    }

}
