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
 * MicrowaveOvenControl
 *
 * @author Dan Cunningham - Initial contribution
 */
public class MicrowaveOvenControlCluster extends BaseCluster {

    public static final int CLUSTER_ID = 0x005F;
    public static final String CLUSTER_NAME = "MicrowaveOvenControl";
    public static final String CLUSTER_PREFIX = "microwaveOvenControl";
    public static final String ATTRIBUTE_CLUSTER_REVISION = "clusterRevision";
    public static final String ATTRIBUTE_FEATURE_MAP = "featureMap";
    public static final String ATTRIBUTE_COOK_TIME = "cookTime";
    public static final String ATTRIBUTE_MAX_COOK_TIME = "maxCookTime";
    public static final String ATTRIBUTE_POWER_SETTING = "powerSetting";
    public static final String ATTRIBUTE_MIN_POWER = "minPower";
    public static final String ATTRIBUTE_MAX_POWER = "maxPower";
    public static final String ATTRIBUTE_POWER_STEP = "powerStep";
    public static final String ATTRIBUTE_SUPPORTED_WATTS = "supportedWatts";
    public static final String ATTRIBUTE_SELECTED_WATT_INDEX = "selectedWattIndex";
    public static final String ATTRIBUTE_WATT_RATING = "wattRating";

    public Integer clusterRevision; // 65533 ClusterRevision
    public FeatureMap featureMap; // 65532 FeatureMap
    /**
     * Indicates the total cook time associated with the operation of the device.
     * This attribute shall remain unchanged during the operation of the oven unless the value is changed via a command
     * or out-of-band action.
     */
    public Integer cookTime; // 0 elapsed-s R V
    /**
     * Indicates the maximum value to which the CookTime attribute can be set.
     */
    public Integer maxCookTime; // 1 elapsed-s R V
    /**
     * Indicates the power level associated with the operation of the device. If the MinPower, MaxPower, and PowerStep
     * attributes are not supported:
     * • The minimum value of this attribute shall be 10,
     * • The maximum value of this attribute shall be 100,
     * • The value shall be in even multiples of 10,
     * • The default value shall be 100.
     * If the MinPower, MaxPower, and PowerStep attributes are supported:
     * • The value of this attribute shall be between MinPower and MaxPower inclusive.
     * • The value of this attribute shall be such that (PowerSetting - MinPower) % PowerStep &#x3D;&#x3D; 0
     */
    public Integer powerSetting; // 2 uint8 R V
    /**
     * Indicates the minimum value to which the PowerSetting attribute that can be set on the server.
     */
    public Integer minPower; // 3 uint8 R V
    /**
     * Indicates the maximum value to which the PowerSetting attribute that can be set on the server.
     */
    public Integer maxPower; // 4 uint8 R V
    /**
     * Indicates the increment of power that can be set on the server. The value of this attribute shall be between 1
     * and MaxPower inclusive.
     * The value of this attribute shall be such that (MaxPower - MinPower) % PowerStep &#x3D;&#x3D; 0
     * For example, if MinPower is 1, MaxPower is 10, and PowerSetting can be set to any integer between MinPower and
     * MaxPower, PowerStep would be set to 1.
     */
    public Integer powerStep; // 5 uint8 R V
    /**
     * Indicates the list of power levels (in W) supported by the server.
     */
    public List<Integer> supportedWatts; // 6 list R V
    /**
     * Indicates the index into the list of SupportedWatts of the currently selected power setting.
     * The index shall be a valid index into the SupportedWatts list.
     */
    public Integer selectedWattIndex; // 7 uint8 R V
    /**
     * Indicates the rating, in Watts, of the microwave power of the oven.
     * Supporting this attribute can assist clients in suggesting cooking settings for various foods and beverages.
     */
    public Integer wattRating; // 8 uint16 R V

    // Bitmaps
    public static class FeatureMap {
        /**
         * 
         * Power is specified as a unitless number or a percentage
         */
        public boolean powerAsNumber;
        /**
         * 
         * Power is specified in Watts
         */
        public boolean powerInWatts;
        /**
         * 
         * Supports the limit attributes used with the PWRNUM feature
         */
        public boolean powerNumberLimits;

        public FeatureMap(boolean powerAsNumber, boolean powerInWatts, boolean powerNumberLimits) {
            this.powerAsNumber = powerAsNumber;
            this.powerInWatts = powerInWatts;
            this.powerNumberLimits = powerNumberLimits;
        }
    }

    public MicrowaveOvenControlCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 95, "MicrowaveOvenControl");
    }

    protected MicrowaveOvenControlCluster(BigInteger nodeId, int endpointId, int clusterId, String clusterName) {
        super(nodeId, endpointId, clusterId, clusterName);
    }

    // commands
    /**
     * This command is used to set the cooking parameters associated with the operation of the device. This command
     * supports the following fields:
     */
    public static ClusterCommand setCookingParameters(Integer cookMode, Integer cookTime, Integer powerSetting,
            Integer wattSettingIndex, Boolean startAfterSetting) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (cookMode != null) {
            map.put("cookMode", cookMode);
        }
        if (cookTime != null) {
            map.put("cookTime", cookTime);
        }
        if (powerSetting != null) {
            map.put("powerSetting", powerSetting);
        }
        if (wattSettingIndex != null) {
            map.put("wattSettingIndex", wattSettingIndex);
        }
        if (startAfterSetting != null) {
            map.put("startAfterSetting", startAfterSetting);
        }
        return new ClusterCommand("setCookingParameters", map);
    }

    /**
     * This command is used to add more time to the CookTime attribute of the server. This command supports these
     * fields:
     */
    public static ClusterCommand addMoreTime(Integer timeToAdd) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (timeToAdd != null) {
            map.put("timeToAdd", timeToAdd);
        }
        return new ClusterCommand("addMoreTime", map);
    }

    @Override
    public @NonNull String toString() {
        String str = "";
        str += "clusterRevision : " + clusterRevision + "\n";
        str += "featureMap : " + featureMap + "\n";
        str += "cookTime : " + cookTime + "\n";
        str += "maxCookTime : " + maxCookTime + "\n";
        str += "powerSetting : " + powerSetting + "\n";
        str += "minPower : " + minPower + "\n";
        str += "maxPower : " + maxPower + "\n";
        str += "powerStep : " + powerStep + "\n";
        str += "supportedWatts : " + supportedWatts + "\n";
        str += "selectedWattIndex : " + selectedWattIndex + "\n";
        str += "wattRating : " + wattRating + "\n";
        return str;
    }
}
