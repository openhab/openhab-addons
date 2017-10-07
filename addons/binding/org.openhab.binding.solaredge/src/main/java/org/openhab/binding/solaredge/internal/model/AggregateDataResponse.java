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
 * @author afriese
 *
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class AggregateDataResponse implements DataResponse {

    private static final String UNIT_WH = "Wh";
    private static final String UNIT_KWH = "KWh";

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Value {
        public String value;
        public String unit;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ValueAndPercent extends Value {
        public String percentage;
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

    // TODO: use units!
    // utilizationMeasures":{
    // "production":{"value":867.0,"unit":"Wh"},"consumption":{"value":8.856,"unit":"KWh"},"selfConsumptionForProduction":{"value":861.0,"percentage":0.9930796,"unit":"Wh"},"selfConsumptionForConsumption":{"value":0.861,"percentage":0.097222224,"unit":"KWh"},"batterySelfConsumption":{"value":0.001,"percentage":0.0011614403,"unit":"KWh"},"import":{"value":7.995,"percentage":0.9027778,"unit":"KWh"},"export":{"value":6.0,"percentage":0.0069204154,"unit":"Wh"}},

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
            try {
                Double val = Double.valueOf(value.value);
                val = val / 1000;
                return val.toString();
            } catch (NumberFormatException ex) {
                return "0";
            }
        } else {
            return value.value;
        }
    }

    private String getValueAsPercent(ValueAndPercent value) {
        if (value.percentage != null) {
            try {
                Double val = Double.valueOf(value.percentage);
                val = val * 100;
                return val.toString();
            } catch (NumberFormatException ex) {
                return "0";
            }
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
