/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * this abstract class is used as base for the specific aggregate response classes
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
public class AggregateDataResponsePrivateApi {

    public static class Value {
        public @Nullable Double value;
        public @Nullable String unit;
    }

    public static class ValueAndPercent extends Value {
        public @Nullable Double percentage;
    }

    public static class UtilizationMeasures {
        public @Nullable Value production;
        public @Nullable Value consumption;
        public @Nullable ValueAndPercent selfConsumptionForConsumption;
        public @Nullable ValueAndPercent batterySelfConsumption;
        @SerializedName("import")
        public @Nullable ValueAndPercent imported;
        public @Nullable ValueAndPercent export;
    }

    private @Nullable UtilizationMeasures utilizationMeasures;

    public final @Nullable UtilizationMeasures getUtilizationMeasures() {
        return utilizationMeasures;
    }

    public final void setUtilizationMeasures(UtilizationMeasures utilizationMeasures) {
        this.utilizationMeasures = utilizationMeasures;
    }
}
