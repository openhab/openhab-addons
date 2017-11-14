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

/**
 * this class is used to map the yearly aggregate data json response
 *
 * @author Alexander Friese - initial contribution
 *
 */
public class AggregateYearDataResponse extends AbstractAggregateDataResponse {

    @Override
    public Map<String, String> getValues() {
        Map<String, String> valueMap = new HashMap<>();

        if (getUtilizationMeasures() != null) {
            if (getUtilizationMeasures().production != null) {
                valueMap.put(AggregateDataChannels.YEAR_PRODUCTION.getFQName(),
                        getValueAsKWh(getUtilizationMeasures().production));
            }

            if (getUtilizationMeasures().consumption != null) {
                valueMap.put(AggregateDataChannels.YEAR_CONSUMPTION.getFQName(),
                        getValueAsKWh(getUtilizationMeasures().consumption));
            }

            if (getUtilizationMeasures().selfConsumptionForConsumption != null) {
                valueMap.put(AggregateDataChannels.YEAR_SELFCONSUMPTIONFORCONSUMPTION.getFQName(),
                        getValueAsKWh(getUtilizationMeasures().selfConsumptionForConsumption));
                valueMap.put(AggregateDataChannels.YEAR_SELFCONSUMPTIONCOVERAGE.getFQName(),
                        getValueAsPercent(getUtilizationMeasures().selfConsumptionForConsumption));
            }

            if (getUtilizationMeasures().batterySelfConsumption != null) {
                valueMap.put(AggregateDataChannels.YEAR_BATTERYSELFCONSUMPTION.getFQName(),
                        getValueAsKWh(getUtilizationMeasures().batterySelfConsumption));
            }

            if (getUtilizationMeasures().imported != null) {
                valueMap.put(AggregateDataChannels.YEAR_IMPORT.getFQName(),
                        getValueAsKWh(getUtilizationMeasures().imported));
            }

            if (getUtilizationMeasures().export != null) {
                valueMap.put(AggregateDataChannels.YEAR_EXPORT.getFQName(),
                        getValueAsKWh(getUtilizationMeasures().export));
            }
        }

        return valueMap;
    }

}
