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

import org.eclipse.jdt.annotation.NonNull;

/**
 * SoilMeasurement
 *
 * @author Dan Cunningham - Initial contribution
 */
public class SoilMeasurementCluster extends BaseCluster {

    public static final int CLUSTER_ID = 0x0430;
    public static final String CLUSTER_NAME = "SoilMeasurement";
    public static final String CLUSTER_PREFIX = "soilMeasurement";
    public static final String ATTRIBUTE_SOIL_MOISTURE_MEASUREMENT_LIMITS = "soilMoistureMeasurementLimits";
    public static final String ATTRIBUTE_SOIL_MOISTURE_MEASURED_VALUE = "soilMoistureMeasuredValue";

    /**
     * Indicates the limits for the SoilMoistureMeasuredValue attribute.
     * Given the measurements are in percentage, the MinMeasuredValue field in the SoilMoistureMeasurementLimits
     * attribute shall NOT be less than 0 and shall NOT be greater than 99. The MaxMeasuredValue field in the
     * SoilMoistureMeasurementLimits attribute shall NOT be less than (SoilMoistureMinMeasurableValue + 1) and shall NOT
     * be greater than 100. The MeasurementType field value shall be set to SoilMoisture.
     * There shall only be a single entry in the AccuracyRanges list of the SoilMoistureMeasurementLimits attribute. The
     * entry shall cover the full measurement range, meaning that the value of the RangeMin field shall be equal to the
     * value of the MinMeasuredValue field and the value of the RangeMax field shall be equal to the value of the
     * MaxMeasuredValue field. The entry shall only indicate a PercentMax value and the value shall NOT be greater than
     * 10.00 percent.
     */
    public MeasurementAccuracyStruct soilMoistureMeasurementLimits; // 0 MeasurementAccuracyStruct R V
    /**
     * Indicates the water content of the soil in percentage.
     * The null value indicates that the measurement is unknown e.g. no measurement has been performed yet.
     */
    public Integer soilMoistureMeasuredValue; // 1 percent R V

    public SoilMeasurementCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 1072, "SoilMeasurement");
    }

    protected SoilMeasurementCluster(BigInteger nodeId, int endpointId, int clusterId, String clusterName) {
        super(nodeId, endpointId, clusterId, clusterName);
    }

    @Override
    public @NonNull String toString() {
        String str = "";
        str += "soilMoistureMeasurementLimits : " + soilMoistureMeasurementLimits + "\n";
        str += "soilMoistureMeasuredValue : " + soilMoistureMeasuredValue + "\n";
        return str;
    }
}
