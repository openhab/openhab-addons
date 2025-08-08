/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNull;

/**
 * Pm25ConcentrationMeasurement
 *
 * @author Dan Cunningham - Initial contribution
 */
public class Pm25ConcentrationMeasurementCluster extends BaseCluster {

    public static final int CLUSTER_ID = 0x042A;
    public static final String CLUSTER_NAME = "Pm25ConcentrationMeasurement";
    public static final String CLUSTER_PREFIX = "pm25ConcentrationMeasurement";
    public static final String ATTRIBUTE_CLUSTER_REVISION = "clusterRevision";
    public static final String ATTRIBUTE_FEATURE_MAP = "featureMap";
    public static final String ATTRIBUTE_MEASURED_VALUE = "measuredValue";
    public static final String ATTRIBUTE_MIN_MEASURED_VALUE = "minMeasuredValue";
    public static final String ATTRIBUTE_MAX_MEASURED_VALUE = "maxMeasuredValue";
    public static final String ATTRIBUTE_PEAK_MEASURED_VALUE = "peakMeasuredValue";
    public static final String ATTRIBUTE_PEAK_MEASURED_VALUE_WINDOW = "peakMeasuredValueWindow";
    public static final String ATTRIBUTE_AVERAGE_MEASURED_VALUE = "averageMeasuredValue";
    public static final String ATTRIBUTE_AVERAGE_MEASURED_VALUE_WINDOW = "averageMeasuredValueWindow";
    public static final String ATTRIBUTE_UNCERTAINTY = "uncertainty";
    public static final String ATTRIBUTE_MEASUREMENT_UNIT = "measurementUnit";
    public static final String ATTRIBUTE_MEASUREMENT_MEDIUM = "measurementMedium";
    public static final String ATTRIBUTE_LEVEL_VALUE = "levelValue";

    public Integer clusterRevision; // 65533 ClusterRevision
    public FeatureMap featureMap; // 65532 FeatureMap
    /**
     * Indicates the most recent measurement as a single-precision floating-point number. MeasuredValue’s unit is
     * represented by MeasurementUnit.
     * A value of null indicates that the measurement is unknown or outside the valid range. MinMeasuredValue and
     * MaxMeasuredValue define the valid range for MeasuredValue.
     */
    public Float measuredValue; // 0 single R V
    /**
     * Indicates the minimum value of MeasuredValue that is capable of being measured. A MinMeasuredValue of null
     * indicates that the MinMeasuredValue is not defined.
     */
    public Float minMeasuredValue; // 1 single R V
    /**
     * Indicates the maximum value of MeasuredValue that is capable of being measured. A MaxMeasuredValue of null
     * indicates that the MaxMeasuredValue is not defined.
     */
    public Float maxMeasuredValue; // 2 single R V
    /**
     * Indicates the maximum value of MeasuredValue that has been measured during the PeakMeasuredValueWindow. If this
     * attribute is provided, the PeakMeasuredValueWindow attribute shall also be provided.
     */
    public Float peakMeasuredValue; // 3 single R V
    /**
     * Indicates the window of time used for determining the PeakMeasuredValue. The value is in seconds.
     */
    public Integer peakMeasuredValueWindow; // 4 elapsed-s R V
    /**
     * Indicates the average value of MeasuredValue that has been measured during the AverageMeasuredValueWindow. If
     * this attribute is provided, the AverageMeasuredValueWindow attribute shall also be provided.
     */
    public Float averageMeasuredValue; // 5 single R V
    /**
     * This attribute shall represent the window of time used for determining the AverageMeasuredValue. The value is in
     * seconds.
     */
    public Integer averageMeasuredValueWindow; // 6 elapsed-s R V
    /**
     * Indicates the range of error or deviation that can be found in MeasuredValue and PeakMeasuredValue. This is
     * considered a +/- value and should be considered to be in MeasurementUnit.
     */
    public Float uncertainty; // 7 single R V
    /**
     * Indicates the unit of MeasuredValue. See MeasurementUnitEnum.
     */
    public MeasurementUnitEnum measurementUnit; // 8 MeasurementUnitEnum R V
    /**
     * Indicates the medium in which MeasuredValue is being measured. See MeasurementMediumEnum.
     */
    public MeasurementMediumEnum measurementMedium; // 9 MeasurementMediumEnum R V
    /**
     * Indicates the level of the substance detected. See LevelValueEnum.
     */
    public LevelValueEnum levelValue; // 10 LevelValueEnum R V

    // Enums
    /**
     * Where mentioned, Billion refers to 10, Trillion refers to 1012 (short scale).
     */
    public enum MeasurementUnitEnum implements MatterEnum {
        PPM(0, "Ppm"),
        PPB(1, "Ppb"),
        PPT(2, "Ppt"),
        MGM3(3, "Mgm 3"),
        UGM3(4, "Ugm 3"),
        NGM3(5, "Ngm 3"),
        PM3(6, "Pm 3"),
        BQM3(7, "Bqm 3");

        public final Integer value;
        public final String label;

        private MeasurementUnitEnum(Integer value, String label) {
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

    public enum MeasurementMediumEnum implements MatterEnum {
        AIR(0, "Air"),
        WATER(1, "Water"),
        SOIL(2, "Soil");

        public final Integer value;
        public final String label;

        private MeasurementMediumEnum(Integer value, String label) {
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

    public enum LevelValueEnum implements MatterEnum {
        UNKNOWN(0, "Unknown"),
        LOW(1, "Low"),
        MEDIUM(2, "Medium"),
        HIGH(3, "High"),
        CRITICAL(4, "Critical");

        public final Integer value;
        public final String label;

        private LevelValueEnum(Integer value, String label) {
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
         * Cluster supports numeric measurement of substance
         */
        public boolean numericMeasurement;
        /**
         * 
         * Cluster supports basic level indication for substance using the ConcentrationLev el enum
         */
        public boolean levelIndication;
        /**
         * 
         * Cluster supports the Medium Concentration Level
         */
        public boolean mediumLevel;
        /**
         * 
         * Cluster supports the Critical Concentration Level
         */
        public boolean criticalLevel;
        /**
         * 
         * Cluster supports peak numeric measurement of substance
         */
        public boolean peakMeasurement;
        /**
         * 
         * Cluster supports average numeric measurement of substance
         */
        public boolean averageMeasurement;

        public FeatureMap(boolean numericMeasurement, boolean levelIndication, boolean mediumLevel,
                boolean criticalLevel, boolean peakMeasurement, boolean averageMeasurement) {
            this.numericMeasurement = numericMeasurement;
            this.levelIndication = levelIndication;
            this.mediumLevel = mediumLevel;
            this.criticalLevel = criticalLevel;
            this.peakMeasurement = peakMeasurement;
            this.averageMeasurement = averageMeasurement;
        }
    }

    public Pm25ConcentrationMeasurementCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 1066, "Pm25ConcentrationMeasurement");
    }

    protected Pm25ConcentrationMeasurementCluster(BigInteger nodeId, int endpointId, int clusterId,
            String clusterName) {
        super(nodeId, endpointId, clusterId, clusterName);
    }

    @Override
    public @NonNull String toString() {
        String str = "";
        str += "clusterRevision : " + clusterRevision + "\n";
        str += "featureMap : " + featureMap + "\n";
        str += "measuredValue : " + measuredValue + "\n";
        str += "minMeasuredValue : " + minMeasuredValue + "\n";
        str += "maxMeasuredValue : " + maxMeasuredValue + "\n";
        str += "peakMeasuredValue : " + peakMeasuredValue + "\n";
        str += "peakMeasuredValueWindow : " + peakMeasuredValueWindow + "\n";
        str += "averageMeasuredValue : " + averageMeasuredValue + "\n";
        str += "averageMeasuredValueWindow : " + averageMeasuredValueWindow + "\n";
        str += "uncertainty : " + uncertainty + "\n";
        str += "measurementUnit : " + measurementUnit + "\n";
        str += "measurementMedium : " + measurementMedium + "\n";
        str += "levelValue : " + levelValue + "\n";
        return str;
    }
}
