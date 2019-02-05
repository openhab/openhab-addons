/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.solaredge.internal.model;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.core.types.State;

/**
 * this class is used to map the daily aggregate data json response
 *
 * @author Alexander Friese - initial contribution
 */
public class AggregateDayDataResponsePrivateApi extends AbstractAggregateDataResponsePrivateApi {

    @Override
    public Map<Channel, State> getValues() {
        Map<Channel, State> valueMap = new HashMap<>();

        if (getUtilizationMeasures() != null) {
            if (getUtilizationMeasures().production != null) {
                assignValue(valueMap, AggregateDataChannels.DAY_PRODUCTION, getUtilizationMeasures().production);
            }

            if (getUtilizationMeasures().consumption != null) {
                assignValue(valueMap, AggregateDataChannels.DAY_CONSUMPTION, getUtilizationMeasures().consumption);
            }

            if (getUtilizationMeasures().selfConsumptionForConsumption != null) {
                assignValue(valueMap, AggregateDataChannels.DAY_SELFCONSUMPTIONFORCONSUMPTION,
                        getUtilizationMeasures().selfConsumptionForConsumption);
                assignPercentage(valueMap, AggregateDataChannels.DAY_SELFCONSUMPTIONCOVERAGE,
                        getUtilizationMeasures().selfConsumptionForConsumption);
            }

            if (getUtilizationMeasures().batterySelfConsumption != null) {
                assignValue(valueMap, AggregateDataChannels.DAY_BATTERYSELFCONSUMPTION,
                        getUtilizationMeasures().batterySelfConsumption);
            }

            if (getUtilizationMeasures().imported != null) {
                assignValue(valueMap, AggregateDataChannels.DAY_IMPORT, getUtilizationMeasures().imported);
            }

            if (getUtilizationMeasures().export != null) {
                assignValue(valueMap, AggregateDataChannels.DAY_EXPORT, getUtilizationMeasures().export);
            }
        }

        return valueMap;
    }
}
