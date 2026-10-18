/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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

import com.google.gson.annotations.SerializedName;

/**
 * this class represents the energy response of the private SolarEdge API
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
public class AggregateDataResponsePrivateApi {

    public static class ProductionDistribution {
        public @Nullable Double productionToHome;
        public @Nullable Double productionToHomePercentage;
        public @Nullable Double productionUnknown;
        public @Nullable Double productionUnknownPercentage;
        public @Nullable Double productionToBattery;
        public @Nullable Double productionToBatteryPercentage;
        public @Nullable Double productionToGrid;
        public @Nullable Double productionToGridPercentage;
    }

    public static class ConsumptionDistribution {
        public @Nullable Double consumptionFromBattery;
        public @Nullable Double consumptionFromBatteryPercentage;
        public @Nullable Double consumptionFromSolar;
        public @Nullable Double consumptionFromSolarPercentage;
        public @Nullable Double consumptionFromGrid;
        public @Nullable Double consumptionFromGridPercentage;
    }

    public static class Summary {
        public @Nullable Double production;
        public @Nullable ProductionDistribution productionDistribution;
        public @Nullable Double consumption;
        public @Nullable ConsumptionDistribution consumptionDistribution;
        public @Nullable Double performanceRatio;
        public @Nullable Double averagePowerFactor;
        public @Nullable Double selfConsumptionRatio;
        public @Nullable Double selfSufficiencyRatio;
        public @Nullable Integer siteAvailability;
        public @Nullable Double export;
        public @Nullable String lateProductionStartDate;
        public @Nullable String lateProductionDistributionStartDate;
        public @Nullable String lateConsumptionStartDate;
        public @Nullable String lateConsumptionDistributionStartDate;
        public @Nullable String latePerformanceRatioStartDate;
        public @Nullable String lateYieldStartDate;
        public @Nullable Double yield;

        @SerializedName("import")
        public @Nullable Double imported;
    }

    public static class MeasurementProductionDistribution {
        public @Nullable Double productionToHome;
        public @Nullable Double productionUnknown;
        public @Nullable Double productionToBattery;
        public @Nullable Double productionToGrid;
        public @Nullable Boolean estimated;
    }

    public static class MeasurementConsumptionDistribution {
        public @Nullable Double consumptionFromBattery;
        public @Nullable Double consumptionFromSolar;
        public @Nullable Double consumptionFromGrid;
        public @Nullable Boolean estimated;
    }

    public static class Measurement {
        public @Nullable String measurementTime;
        public @Nullable Double production;
        public @Nullable MeasurementProductionDistribution productionDistribution;
        public @Nullable Double consumption;
        public @Nullable MeasurementConsumptionDistribution consumptionDistribution;
        public @Nullable String weatherDescription;
        public @Nullable Double export;

        @SerializedName("import")
        public @Nullable Double imported;
    }

    public static class Chart {
        public @Nullable List<Measurement> measurements;
    }

    private @Nullable Summary summary;
    private @Nullable Chart chart;

    public final @Nullable Summary getSummary() {
        return summary;
    }

    public final void setSummary(Summary summary) {
        this.summary = summary;
    }

    public final @Nullable Chart getChart() {
        return chart;
    }

    public final void setChart(Chart chart) {
        this.chart = chart;
    }
}
