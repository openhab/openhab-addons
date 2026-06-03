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
// AUTO-GENERATED, DO NOT EDIT!

package org.openhab.binding.matter.internal.client.dto.cluster.gen;

import java.math.BigInteger;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;

/**
 * ElectricalGridConditions
 *
 * @author Dan Cunningham - Initial contribution
 */
public class ElectricalGridConditionsCluster extends BaseCluster {

    public static final int CLUSTER_ID = 0x00A0;
    public static final String CLUSTER_NAME = "ElectricalGridConditions";
    public static final String CLUSTER_PREFIX = "electricalGridConditions";
    public static final String ATTRIBUTE_FEATURE_MAP = "featureMap";
    public static final String ATTRIBUTE_LOCAL_GENERATION_AVAILABLE = "localGenerationAvailable";
    public static final String ATTRIBUTE_CURRENT_CONDITIONS = "currentConditions";
    public static final String ATTRIBUTE_FORECAST_CONDITIONS = "forecastConditions";

    public FeatureMap featureMap; // 65532 FeatureMap
    /**
     * This shall indicate if there is known to be local generation (for example Solar PV or Battery Storage) at the
     * premises.
     * If the presence of any local generation is unknown, or cannot be determined, the value shall be null.
     */
    public Boolean localGenerationAvailable; // 0 bool RW VO
    /**
     * This shall indicate the current electricity supply conditions. If the current conditions are unknown, or cannot
     * be determined, the value shall be null.
     */
    public ElectricalGridConditionsStruct currentConditions; // 1 ElectricalGridConditionsStruct R V
    /**
     * This shall indicate the forecast of upcoming electricity supply conditions. If the forecast is unable to be
     * determined, this list shall be empty.
     * The list entries shall be in time order:
     * - All entries except the last one shall have a non-null PeriodEnd.
     * - For all entries except the first one, PeriodStart shall be greater than the previous entry's PeriodEnd.
     */
    public List<ElectricalGridConditionsStruct> forecastConditions; // 2 list R V

    // Structs
    /**
     * This event shall be generated when the value of the CurrentConditions attribute changes.
     */
    public static class CurrentConditionsChanged {
        /**
         * This field shall be the new value of the CurrentConditions attribute.
         */
        public ElectricalGridConditionsStruct currentConditions; // ElectricalGridConditionsStruct

        public CurrentConditionsChanged(ElectricalGridConditionsStruct currentConditions) {
            this.currentConditions = currentConditions;
        }
    }

    /**
     * This represents the greenhouse gas carbon intensity over a given period.
     */
    public static class ElectricalGridConditionsStruct {
        /**
         * This field shall indicate the beginning timestamp in UTC of the period.
         */
        public Integer periodStart; // epoch-s
        /**
         * This field shall indicate the ending timestamp in UTC of the period. This shall be greater than PeriodStart.
         * If this field is null, then the period has no definite end.
         */
        public Integer periodEnd; // epoch-s
        /**
         * This field shall indicate the estimated carbon intensity in grams of CO2 equivalent per kWh of the grid. This
         * is not impacted by any local generation.
         */
        public Integer gridCarbonIntensity; // int16
        /**
         * This field shall indicate the relative level of carbon intensity of the grid. This is not impacted by any
         * local generation.
         * It is up to the cluster server to determine the thresholds of High, Medium or Low based upon typical grid
         * carbon levels for this region or market, since this can vary significantly between countries across the
         * world.
         */
        public ThreeLevelEnum gridCarbonLevel; // ThreeLevelEnum
        /**
         * This field shall indicate the estimated carbon intensity in grams of CO2 equivalent per kWh of the premises
         * mains supply. This value shall take into account the impact of any local generation.
         * For example, if an EMS can forecast that excess generation will occur in a period or the premises are
         * currently generating excess power to the grid, then this could assume a value of 0 grams CO2 equivalent per
         * kWh for this period.
         * When solar PV is not being exported to the grid then this value is typically the same as the
         * GridCarbonIntensity.
         * Clients are expected to use this value when computing or displaying the local premises carbon intensity to
         * users.
         * If there is no local generation, this value shall be the same as the GridCarbonIntensity at all times.
         */
        public Integer localCarbonIntensity; // int16
        /**
         * This field shall indicate the relative level of carbon intensity of the premises mains supply. This level
         * shall take into account impact of any local generation.
         * It is up to the cluster server to determine the thresholds of High, Medium or Low based upon typical grid
         * carbon levels for this region or market, since this can vary significantly between countries across the
         * world.
         * When local power generation (for example from solar PV) is not being exported to the grid then this level is
         * the same as the GridCarbonLevel.
         * Clients are expected to use this value when displaying the local premises carbon intensity to users.
         * If there is no local generation, this value shall be the same as the GridCarbonLevel at all times.
         */
        public ThreeLevelEnum localCarbonLevel; // ThreeLevelEnum

        public ElectricalGridConditionsStruct(Integer periodStart, Integer periodEnd, Integer gridCarbonIntensity,
                ThreeLevelEnum gridCarbonLevel, Integer localCarbonIntensity, ThreeLevelEnum localCarbonLevel) {
            this.periodStart = periodStart;
            this.periodEnd = periodEnd;
            this.gridCarbonIntensity = gridCarbonIntensity;
            this.gridCarbonLevel = gridCarbonLevel;
            this.localCarbonIntensity = localCarbonIntensity;
            this.localCarbonLevel = localCarbonLevel;
        }
    }

    // Enums
    /**
     * This data type is derived from enum8 and is used for indicating three levels: Low, Medium, High.
     */
    public enum ThreeLevelEnum implements MatterEnum {
        LOW(0, "Low"),
        MEDIUM(1, "Medium"),
        HIGH(2, "High");

        private final Integer value;
        private final String label;

        private ThreeLevelEnum(Integer value, String label) {
            this.value = value;
            this.label = label;
        }

        @Override
        public Integer getValue() {
            return value;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    // Bitmaps
    public static class FeatureMap {
        /**
         * 
         * The feature indicates the server is capable of providing a forecast of grid and local conditions for several
         * hours in the future.
         */
        public boolean forecasting;

        public FeatureMap(boolean forecasting) {
            this.forecasting = forecasting;
        }
    }

    public ElectricalGridConditionsCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 160, "ElectricalGridConditions");
    }

    protected ElectricalGridConditionsCluster(BigInteger nodeId, int endpointId, int clusterId, String clusterName) {
        super(nodeId, endpointId, clusterId, clusterName);
    }

    @Override
    public @NonNull String toString() {
        String str = "";
        str += "featureMap : " + featureMap + "\n";
        str += "localGenerationAvailable : " + localGenerationAvailable + "\n";
        str += "currentConditions : " + currentConditions + "\n";
        str += "forecastConditions : " + forecastConditions + "\n";
        return str;
    }
}
