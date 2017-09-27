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
public class AggregateDataResponse {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Value {
        public String value;
        public String unit;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UtilizationMeasures {
        public Value production;
        public Value consumption;
        public Value selfConsumptionForConsumption;
        public Value batterySelfConsumption;
        @JsonProperty("import")
        public Value imported;
        public Value export;
    }

    private UtilizationMeasures utilizationMeasures;

    public Map<String, String> getValues() {
        Map<String, String> valueMap = new HashMap<>();

        valueMap.put(AggregateDataChannels.PRODUCTION.getFQName(), utilizationMeasures.production.value);
        valueMap.put(AggregateDataChannels.CONSUMPTION.getFQName(), utilizationMeasures.consumption.value);
        valueMap.put(AggregateDataChannels.SELFCONSUMPTIONFORCONSUMPTION.getFQName(),
                utilizationMeasures.selfConsumptionForConsumption.value);
        valueMap.put(AggregateDataChannels.BATTERYSELFCONSUMPTION.getFQName(),
                utilizationMeasures.batterySelfConsumption.value);
        valueMap.put(AggregateDataChannels.IMPORT.getFQName(), utilizationMeasures.imported.value);
        valueMap.put(AggregateDataChannels.EXPORT.getFQName(), utilizationMeasures.export.value);

        return valueMap;
    }

    public final UtilizationMeasures getUtilizationMeasures() {
        return utilizationMeasures;
    }

    public final void setUtilizationMeasures(UtilizationMeasures utilizationMeasures) {
        this.utilizationMeasures = utilizationMeasures;
    }

}
