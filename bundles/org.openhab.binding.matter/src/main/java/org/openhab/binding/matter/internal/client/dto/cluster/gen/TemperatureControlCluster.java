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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.binding.matter.internal.client.dto.cluster.ClusterCommand;

/**
 * TemperatureControl
 *
 * @author Dan Cunningham - Initial contribution
 */
public class TemperatureControlCluster extends BaseCluster {

    public static final int CLUSTER_ID = 0x0056;
    public static final String CLUSTER_NAME = "TemperatureControl";
    public static final String CLUSTER_PREFIX = "temperatureControl";
    public static final String ATTRIBUTE_CLUSTER_REVISION = "clusterRevision";
    public static final String ATTRIBUTE_FEATURE_MAP = "featureMap";
    public static final String ATTRIBUTE_TEMPERATURE_SETPOINT = "temperatureSetpoint";
    public static final String ATTRIBUTE_MIN_TEMPERATURE = "minTemperature";
    public static final String ATTRIBUTE_MAX_TEMPERATURE = "maxTemperature";
    public static final String ATTRIBUTE_STEP = "step";
    public static final String ATTRIBUTE_SELECTED_TEMPERATURE_LEVEL = "selectedTemperatureLevel";
    public static final String ATTRIBUTE_SUPPORTED_TEMPERATURE_LEVELS = "supportedTemperatureLevels";

    public Integer clusterRevision; // 65533 ClusterRevision
    public FeatureMap featureMap; // 65532 FeatureMap
    /**
     * Indicates the desired Temperature Setpoint on the device.
     */
    public Integer temperatureSetpoint; // 0 temperature R V
    /**
     * Indicates the minimum temperature to which the TemperatureSetpoint attribute may be set.
     */
    public Integer minTemperature; // 1 temperature R V
    /**
     * Indicates the maximum temperature to which the TemperatureSetpoint attribute may be set.
     * If the Step attribute is supported, this attribute shall be such that MaxTemperature &#x3D; MinTemperature + Step
     * * n, where n is an integer and n &gt; 0. If the Step attribute is not supported, this attribute shall be such
     * that MaxTemperature &gt; MinTemperature.
     */
    public Integer maxTemperature; // 2 temperature R V
    /**
     * Indicates the discrete value by which the TemperatureSetpoint attribute can be changed via the SetTemperature
     * command.
     * For example, if the value of MinTemperature is 25.00C (2500) and the Step value is 0.50C (50), valid values of
     * the TargetTemperature field of the SetTemperature command would be 25.50C (2550), 26.00C (2600), 26.50C (2650),
     * etc.
     */
    public Integer step; // 3 temperature R V
    /**
     * Indicates the currently selected temperature level setting of the server. This attribute shall be the positional
     * index of the list item in the SupportedTemperatureLevels list that represents the currently selected temperature
     * level setting of the server.
     */
    public Integer selectedTemperatureLevel; // 4 uint8 R V
    /**
     * Indicates the list of supported temperature level settings that may be selected via the TargetTemperatureLevel
     * field in the SetTemperature command. Each string is readable text that describes each temperature level setting
     * in a way that can be easily understood by humans. For example, a washing machine can have temperature levels like
     * &quot;Cold&quot;, &quot;Warm&quot;, and &quot;Hot&quot;. Each string is specified by the manufacturer.
     * Each item in this list shall represent a unique temperature level. Each entry in this list shall have a unique
     * value. The entries in this list shall appear in order of increasing temperature level with list item 0 being the
     * setting with the lowest temperature level.
     */
    public List<String> supportedTemperatureLevels; // 5 list R V

    // Bitmaps
    public static class FeatureMap {
        /**
         * 
         * For devices that use an actual temperature value for the temperature setpoint, such as some water heaters,
         * the feature TN shall be used. Note that this cluster provides and supports temperatures in degrees Celsius
         * via the temperature data type.
         */
        public boolean temperatureNumber;
        /**
         * 
         * For devices that use vendor-specific temperature levels for the temperature setpoint, such as some washers,
         * the feature TL shall be used.
         */
        public boolean temperatureLevel;
        /**
         * 
         * For devices that support discrete temperature setpoints that are larger than the temperature resolution
         * imposed via the temperature data type, the Step feature may be used.
         */
        public boolean temperatureStep;

        public FeatureMap(boolean temperatureNumber, boolean temperatureLevel, boolean temperatureStep) {
            this.temperatureNumber = temperatureNumber;
            this.temperatureLevel = temperatureLevel;
            this.temperatureStep = temperatureStep;
        }
    }

    public TemperatureControlCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 86, "TemperatureControl");
    }

    protected TemperatureControlCluster(BigInteger nodeId, int endpointId, int clusterId, String clusterName) {
        super(nodeId, endpointId, clusterId, clusterName);
    }

    // commands
    public static ClusterCommand setTemperature(Integer targetTemperature, Integer targetTemperatureLevel) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (targetTemperature != null) {
            map.put("targetTemperature", targetTemperature);
        }
        if (targetTemperatureLevel != null) {
            map.put("targetTemperatureLevel", targetTemperatureLevel);
        }
        return new ClusterCommand("setTemperature", map);
    }

    @Override
    public @NonNull String toString() {
        String str = "";
        str += "clusterRevision : " + clusterRevision + "\n";
        str += "featureMap : " + featureMap + "\n";
        str += "temperatureSetpoint : " + temperatureSetpoint + "\n";
        str += "minTemperature : " + minTemperature + "\n";
        str += "maxTemperature : " + maxTemperature + "\n";
        str += "step : " + step + "\n";
        str += "selectedTemperatureLevel : " + selectedTemperatureLevel + "\n";
        str += "supportedTemperatureLevels : " + supportedTemperatureLevels + "\n";
        return str;
    }
}
