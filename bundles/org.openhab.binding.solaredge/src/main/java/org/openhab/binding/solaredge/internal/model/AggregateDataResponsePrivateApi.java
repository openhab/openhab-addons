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
package org.openhab.binding.solaredge.internal.model;

import com.google.gson.annotations.SerializedName;

/**
 * this abstract class is used as base for the specific aggregate response classes
 *
 * @author Alexander Friese - initial contribution
 */
public class AggregateDataResponsePrivateApi {

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
}
