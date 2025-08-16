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
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;

/**
 * EnergyPreference
 *
 * @author Dan Cunningham - Initial contribution
 */
public class EnergyPreferenceCluster extends BaseCluster {

    public static final int CLUSTER_ID = 0x009B;
    public static final String CLUSTER_NAME = "EnergyPreference";
    public static final String CLUSTER_PREFIX = "energyPreference";
    public static final String ATTRIBUTE_CLUSTER_REVISION = "clusterRevision";
    public static final String ATTRIBUTE_FEATURE_MAP = "featureMap";
    public static final String ATTRIBUTE_ENERGY_BALANCES = "energyBalances";
    public static final String ATTRIBUTE_CURRENT_ENERGY_BALANCE = "currentEnergyBalance";
    public static final String ATTRIBUTE_ENERGY_PRIORITIES = "energyPriorities";
    public static final String ATTRIBUTE_LOW_POWER_MODE_SENSITIVITIES = "lowPowerModeSensitivities";
    public static final String ATTRIBUTE_CURRENT_LOW_POWER_MODE_SENSITIVITY = "currentLowPowerModeSensitivity";

    public Integer clusterRevision; // 65533 ClusterRevision
    public FeatureMap featureMap; // 65532 FeatureMap
    /**
     * Indicates a list of BalanceStructs, each representing a step along a linear scale of relative priorities. A Step
     * field with a value of zero shall indicate that the device SHOULD entirely favor the priority specified by the
     * first element in EnergyPriorities; whereas a Step field with a value of 100 shall indicate that the device SHOULD
     * entirely favor the priority specified by the second element in EnergyPriorities. The midpoint value of 50 shall
     * indicate an even split between the two priorities.
     * This shall contain at least two BalanceStructs.
     * Each BalanceStruct shall have a Step field larger than the Step field on the previous BalanceStruct in the list.
     * The first BalanceStruct shall have a Step value of zero, and the last BalanceStruct shall have a Step value of
     * 100.
     */
    public List<BalanceStruct> energyBalances; // 0 list R V
    /**
     * Indicates the current preference of the user for balancing different priorities during device use. The value of
     * this attribute is the index, 0-based, into the EnergyBalances attribute for the currently selected balance.
     * If an attempt is made to set this attribute to an index outside the maximum index for EnergyBalances, a response
     * with the status code CONSTRAINT_ERROR shall be returned.
     * If the value of EnergyBalances changes after an update, the device shall migrate the value of the
     * CurrentEnergyBalance attribute to the index which the manufacturer specifies most closely matches the previous
     * value, while preserving extreme preferences as follows:
     * 1. If the previous value of CurrentEnergyBalance was zero, indicating a total preference for the priority
     * specified by the first element in EnergyPriorities, the new value of CurrentEnergyBalance shall also be zero.
     * 2. If the previous value of CurrentEnergyBalance was the index of the last BalanceStruct in the previous value of
     * EnergyBalances, indicating a total preference for the priority specified by the last element in EnergyPriorities,
     * the new value of CurrentEnergyBalance shall be the index of the last element in the updated value of
     * EnergyBalances.
     */
    public Integer currentEnergyBalance; // 1 uint8 RW VO
    /**
     * Indicates two extremes for interpreting the values in the EnergyBalances attribute. These two priorities shall be
     * in opposition to each other; e.g. Comfort vs. Efficiency or Speed vs. WaterConsumption.
     * If the value of EnergyPriorities changes after an update to represent a new balance between priorities, the value
     * of the CurrentEnergyBalance attribute shall be set to its default.
     */
    public List<EnergyPriorityEnum> energyPriorities; // 2 list R V
    /**
     * Indicates a list of BalanceStructs, each representing a condition or set of conditions for the device to enter a
     * low power mode.
     * This shall contain at least two BalanceStructs.
     * Each BalanceStruct shall have a Step field larger than the Step field on the previous BalanceStruct in the list.
     */
    public List<BalanceStruct> lowPowerModeSensitivities; // 3 list R V
    /**
     * Indicates the current preference of the user for determining when the device should enter a low power mode. The
     * value of this attribute is the index, 0-based, into the LowPowerModeSensitivities attribute for the currently
     * selected preference.
     * If an attempt is made to set this attribute to an index outside the maximum index for LowPowerModeSensitivities,
     * a response with the status code CONSTRAINT_ERROR shall be returned.
     * If the value of LowPowerModeSensitivities changes after an update, the device shall migrate the value of the
     * LowPowerModeSensitivity attribute to the index which the manufacturer specifies most closely matches the previous
     * value.
     */
    public Integer currentLowPowerModeSensitivity; // 4 uint8 RW VO

    // Structs
    /**
     * This represents a step along a scale of preferences.
     */
    public static class BalanceStruct {
        /**
         * This field shall indicate the relative value of this step.
         */
        public Integer step; // percent
        /**
         * This field shall indicate an optional string explaining which actions a device might take at the given step
         * value.
         */
        public String label; // string

        public BalanceStruct(Integer step, String label) {
            this.step = step;
            this.label = label;
        }
    }

    // Enums
    public enum EnergyPriorityEnum implements MatterEnum {
        COMFORT(0, "Comfort"),
        SPEED(1, "Speed"),
        EFFICIENCY(2, "Efficiency"),
        WATER_CONSUMPTION(3, "Water Consumption");

        public final Integer value;
        public final String label;

        private EnergyPriorityEnum(Integer value, String label) {
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
         * This feature allows a user to select from a list of energy balances with associated descriptions of which
         * strategies a device will use to target the specified balance.
         */
        public boolean energyBalance;
        /**
         * 
         * This feature allows the user to select a condition or set of conditions which will cause the device to switch
         * to a mode using less power. For example, a device might provide a scale of durations that must elapse without
         * user interaction before it goes to sleep.
         */
        public boolean lowPowerModeSensitivity;

        public FeatureMap(boolean energyBalance, boolean lowPowerModeSensitivity) {
            this.energyBalance = energyBalance;
            this.lowPowerModeSensitivity = lowPowerModeSensitivity;
        }
    }

    public EnergyPreferenceCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 155, "EnergyPreference");
    }

    protected EnergyPreferenceCluster(BigInteger nodeId, int endpointId, int clusterId, String clusterName) {
        super(nodeId, endpointId, clusterId, clusterName);
    }

    @Override
    public @NonNull String toString() {
        String str = "";
        str += "clusterRevision : " + clusterRevision + "\n";
        str += "featureMap : " + featureMap + "\n";
        str += "energyBalances : " + energyBalances + "\n";
        str += "currentEnergyBalance : " + currentEnergyBalance + "\n";
        str += "energyPriorities : " + energyPriorities + "\n";
        str += "lowPowerModeSensitivities : " + lowPowerModeSensitivities + "\n";
        str += "currentLowPowerModeSensitivity : " + currentLowPowerModeSensitivity + "\n";
        return str;
    }
}
