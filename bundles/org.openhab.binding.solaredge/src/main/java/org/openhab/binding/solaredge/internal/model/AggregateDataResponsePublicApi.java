/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * this class is used to map the aggregate data response of the public API
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
public class AggregateDataResponsePublicApi {

    public static class MeterTelemetry {
        public @Nullable String date;
        public @Nullable Double value;
    }

    public static class MeterTelemetries {
        public @Nullable String type;
        public @Nullable List<MeterTelemetry> values;
    }

    public static class EnergyDetails {
        public @Nullable AggregatePeriod timeUnit;
        public @Nullable String unit;
        public @Nullable List<MeterTelemetries> meters;
    }

    private @Nullable EnergyDetails energyDetails;

    public @Nullable EnergyDetails getEnergyDetails() {
        return energyDetails;
    }

    public void setEnergyDetails(EnergyDetails energyDetails) {
        this.energyDetails = energyDetails;
    }
}
