/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.solaredge.internal.model;

import java.util.Map;

import javax.measure.Unit;
import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Energy;

import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.annotations.SerializedName;

/**
 * this abstract class is used as base for the specific aggregate response classes
 *
 * @author Alexander Friese - initial contribution
 */
public abstract class AbstractAggregateDataResponsePrivateApi implements DataResponse {

    private final Logger logger = LoggerFactory.getLogger(AbstractAggregateDataResponsePrivateApi.class);

    public static class Value {
        public Double value;
        public String unit;
    }

    public static class ValueAndPercent extends Value {
        public Double percentage;
    }

    public static class UtilizationMeasures {
        public Value production;
        public Value consumption;
        public ValueAndPercent selfConsumptionForConsumption;
        public ValueAndPercent batterySelfConsumption;
        @SerializedName("import")
        public ValueAndPercent imported;
        public ValueAndPercent export;
    }

    private UtilizationMeasures utilizationMeasures;

    public final UtilizationMeasures getUtilizationMeasures() {
        return utilizationMeasures;
    }

    public final void setUtilizationMeasures(UtilizationMeasures utilizationMeasures) {
        this.utilizationMeasures = utilizationMeasures;
    }

    /**
     * converts the value to QuantityType. If no unit can be determined UnDefType.UNDEF will be used
     *
     * @param targetMap result will be put into this map
     * @param channel   channel to assign the value
     * @param value     the value to convert
     */
    protected final void assignValue(Map<Channel, State> targetMap, Channel channel, Value value) {
        State result = UnDefType.UNDEF;

        if (value != null && value.value != null && value.unit != null) {
            Unit<Energy> unit = determineEnergyUnit(value.unit);
            if (unit != null) {
                result = new QuantityType<Energy>(value.value, unit);
            } else {
                logger.debug("Channel {}: Could not determine unit: '{}'", channel.getFQName(), value.unit);
            }
        } else {
            logger.debug("Channel {}: no value provided or value has no unit.", channel.getFQName());
        }
        targetMap.put(channel, result);
    }

    /**
     * converts the value to QuantityType
     *
     * @param targetMap result will be put into this map
     * @param channel   channel to assign the value
     * @param value     the value to convert
     */
    protected final void assignPercentage(Map<Channel, State> targetMap, Channel channel, ValueAndPercent value) {
        State result = UnDefType.UNDEF;

        if (value != null && value.percentage != null) {
            result = new QuantityType<Dimensionless>(value.percentage * 100, SmartHomeUnits.PERCENT);
        } else {
            logger.debug("Channel {}: no value provided.", channel.getFQName());
        }
        targetMap.put(channel, result);
    }

}
