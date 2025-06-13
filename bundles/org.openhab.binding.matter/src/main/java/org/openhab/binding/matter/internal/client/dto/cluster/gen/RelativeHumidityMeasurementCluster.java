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
 * RelativeHumidityMeasurement
 *
 * @author Dan Cunningham - Initial contribution
 */
public class RelativeHumidityMeasurementCluster extends BaseCluster {

    public static final int CLUSTER_ID = 0x0405;
    public static final String CLUSTER_NAME = "RelativeHumidityMeasurement";
    public static final String CLUSTER_PREFIX = "relativeHumidityMeasurement";
    public static final String ATTRIBUTE_CLUSTER_REVISION = "clusterRevision";
    public static final String ATTRIBUTE_MEASURED_VALUE = "measuredValue";
    public static final String ATTRIBUTE_MIN_MEASURED_VALUE = "minMeasuredValue";
    public static final String ATTRIBUTE_MAX_MEASURED_VALUE = "maxMeasuredValue";
    public static final String ATTRIBUTE_TOLERANCE = "tolerance";

    public Integer clusterRevision; // 65533 ClusterRevision
    /**
     * MeasuredValue represents the water content in % as follows:
     * MeasuredValue &#x3D; 100 x water content
     * Where 0% &lt; &#x3D; water content &lt; &#x3D; 100%, corresponding to a MeasuredValue in the range 0 to 10000.
     * The maximum resolution this format allows is 0.01%.
     * MinMeasuredValue and MaxMeasuredValue define the range of the sensor.
     * The null value indicates that the measurement is unknown, otherwise the range shall be as described in Measured
     * Value.
     * MeasuredValue is updated continuously as new measurements are made.
     */
    public Integer measuredValue; // 0 uint16 R V
    /**
     * The MinMeasuredValue attribute indicates the minimum value of MeasuredValue that can be measured. The null value
     * means this attribute is not defined. See Measured Value for more details.
     */
    public Integer minMeasuredValue; // 1 uint16 R V
    /**
     * The MaxMeasuredValue attribute indicates the maximum value of MeasuredValue that can be measured. The null value
     * means this attribute is not defined. See Measured Value for more details.
     */
    public Integer maxMeasuredValue; // 2 uint16 R V
    /**
     * See Measured Value.
     */
    public Integer tolerance; // 3 uint16 R V

    public RelativeHumidityMeasurementCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 1029, "RelativeHumidityMeasurement");
    }

    protected RelativeHumidityMeasurementCluster(BigInteger nodeId, int endpointId, int clusterId, String clusterName) {
        super(nodeId, endpointId, clusterId, clusterName);
    }

    @Override
    public @NonNull String toString() {
        String str = "";
        str += "clusterRevision : " + clusterRevision + "\n";
        str += "measuredValue : " + measuredValue + "\n";
        str += "minMeasuredValue : " + minMeasuredValue + "\n";
        str += "maxMeasuredValue : " + maxMeasuredValue + "\n";
        str += "tolerance : " + tolerance + "\n";
        return str;
    }
}
