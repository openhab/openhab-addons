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

import com.google.gson.annotations.SerializedName;

/**
 * this abstract class is used as base for the specific aggregate response classes
 *
 * @author Alexander Friese - initial contribution
 *
 */
public abstract class AbstractAggregateDataResponse implements DataResponse {

    private static final String UNIT_WH = "Wh";
    private static final String UNIT_KWH = "KWh";
    private static final String UNIT_MWH = "MWh";

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

    protected final String getValueAsKWh(Value value) {
        Double convertedValue = value.value;

        if (value.unit != null && value.unit.equals(UNIT_WH)) {
            convertedValue = convertedValue / 1000;
        } else if (value.unit != null && value.unit.equals(UNIT_MWH)) {
            convertedValue = convertedValue * 1000;
        }

        DecimalFormat df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.HALF_UP);
        return df.format(convertedValue);
    }

    protected final String getValueAsPercent(ValueAndPercent value) {
        if (value.percentage != null) {
            Double convertedValue = value.percentage * 100;

            DecimalFormat df = new DecimalFormat("#.##");
            df.setRoundingMode(RoundingMode.HALF_UP);
            return df.format(convertedValue);
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
