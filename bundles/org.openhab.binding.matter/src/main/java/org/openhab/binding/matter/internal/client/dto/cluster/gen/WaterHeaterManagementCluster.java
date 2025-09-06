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
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.binding.matter.internal.client.dto.cluster.ClusterCommand;

/**
 * WaterHeaterManagement
 *
 * @author Dan Cunningham - Initial contribution
 */
public class WaterHeaterManagementCluster extends BaseCluster {

    public static final int CLUSTER_ID = 0x0094;
    public static final String CLUSTER_NAME = "WaterHeaterManagement";
    public static final String CLUSTER_PREFIX = "waterHeaterManagement";
    public static final String ATTRIBUTE_CLUSTER_REVISION = "clusterRevision";
    public static final String ATTRIBUTE_FEATURE_MAP = "featureMap";
    public static final String ATTRIBUTE_HEATER_TYPES = "heaterTypes";
    public static final String ATTRIBUTE_HEAT_DEMAND = "heatDemand";
    public static final String ATTRIBUTE_TANK_VOLUME = "tankVolume";
    public static final String ATTRIBUTE_ESTIMATED_HEAT_REQUIRED = "estimatedHeatRequired";
    public static final String ATTRIBUTE_TANK_PERCENTAGE = "tankPercentage";
    public static final String ATTRIBUTE_BOOST_STATE = "boostState";

    public Integer clusterRevision; // 65533 ClusterRevision
    public FeatureMap featureMap; // 65532 FeatureMap
    /**
     * Indicates the heat sources that the water heater can call on for heating. If a bit is set then the water heater
     * supports the corresponding heat source.
     */
    public WaterHeaterHeatSourceBitmap heaterTypes; // 0 WaterHeaterHeatSourceBitmap R V
    /**
     * Indicates if the water heater is heating water. If a bit is set then the corresponding heat source is active.
     */
    public WaterHeaterHeatSourceBitmap heatDemand; // 1 WaterHeaterHeatSourceBitmap R V
    /**
     * Indicates the volume of water that the hot water tank can hold (in units of Litres). This allows an energy
     * management system to estimate the required heating energy needed to reach the target temperature.
     */
    public Integer tankVolume; // 2 uint16 R V
    /**
     * Indicates the estimated heat energy needed to raise the water temperature to the target setpoint. This can be
     * computed by taking the specific heat capacity of water (4182 J/kg °C) and by knowing the current temperature of
     * the water, the tank volume and target temperature.
     * For example, if the target temperature was 60°C, the current temperature was 20°C and the tank volume was 100L:
     * ### Mass of water &#x3D; 1kg per Litre
     * Total Mass &#x3D; 100 x 1kg &#x3D; 100kg
     * Δ Temperature &#x3D; (target temperature - current temperature)
     * &#x3D; (60°C - 20°C) &#x3D; 40°C
     * ### Energy required to
     * heat the water to 60°C &#x3D; 4182 x 40 x 100 &#x3D; 16,728,000 J
     * Converting Joules in to Wh of heat (divide by 3600):
     * &#x3D; 16,728,000 J / 3600
     * &#x3D; 4647 Wh (4.65kWh)
     * If the TankPercent feature is supported, then this estimate shall also take into account the percentage of the
     * water in the tank which is already hot.
     * &gt; [!NOTE]
     * &gt; The electrical energy required to heat the water depends on the heating system used to heat the water. For
     * example, a direct electric immersion heating element can be close to 100% efficient, so the electrical energy
     * needed to heat the hot water is nearly the same as the EstimatedHeatEnergyRequired. However some forms of
     * heating, such as an air-source heat pump which extracts heat from ambient air, requires much less electrical
     * energy to heat hot water. Heat pumps can be produce 3kWh of heat output for 1kWh of electrical energy input. The
     * conversion between heat energy and electrical energy is outside the scope of this cluster.
     */
    public BigInteger estimatedHeatRequired; // 3 energy-mWh R V
    /**
     * Indicates an approximate level of hot water stored in the tank, which might help consumers understand the amount
     * of hot water remaining in the tank. The accuracy of this attribute is manufacturer specific.
     * In most hot water tanks, there is a stratification effect where the hot water layer rests above a cooler layer of
     * water below. For this reason cold water is fed in at the bottom of the tank and the hot water is drawn from the
     * top.
     * Some water tanks might use multiple temperature probes to estimate the level of the hot water layer. A water
     * heater with multiple temperature probes is likely to implement an algorithm to estimate the hot water tank
     * percentage by taking into account the temperature values of each probe to determine the height of the hot water.
     * However it might be possible with a single temperature probe to estimate how much hot water is left using a
     * simpler algorithm:
     * For example, if the target temperature was 60°C, the CurrentTemperature was 40°C from a single temperature probe
     * measuring the average water temperature and the temperature of incoming cold water (COLD_WATER_TEMP) was assumed
     * to be 20°C:
     * TankPercentage &#x3D; int(((current temperature - COLD_WATER_TEMP) / (target temperature - COLD_WATER_TEMP)) *
     * 100)
     * TankPercentage &#x3D; min( max(TankPercentage,0), 100)
     * ### TankPercentage &#x3D; 50%
     */
    public Integer tankPercentage; // 4 percent R V
    /**
     * Indicates whether the Boost, as triggered by a Boost command, is currently Active or Inactive.
     * See Boost and CancelBoost commands for more details.
     */
    public BoostStateEnum boostState; // 5 BoostStateEnum R V

    // Structs
    /**
     * This event shall be generated whenever a Boost command is accepted.
     * The corresponding structure fields within the WaterHeaterBoostInfoStruct are copied from the Boost command.
     */
    public static class BoostStarted {
        public WaterHeaterBoostInfoStruct boostInfo; // WaterHeaterBoostInfoStruct

        public BoostStarted(WaterHeaterBoostInfoStruct boostInfo) {
            this.boostInfo = boostInfo;
        }
    }

    /**
     * This event shall be generated whenever the BoostState transitions from Active to Inactive.
     */
    public static class BoostEnded {
        public BoostEnded() {
        }
    }

    public static class WaterHeaterBoostInfoStruct {
        /**
         * This field shall indicate the time period, in seconds, for which the boost state is activated.
         */
        public Integer duration; // elapsed-s
        /**
         * This field shall indicate whether the boost state shall be automatically canceled once the hot water has
         * reached either:
         * • the set point temperature (from the thermostat cluster)
         * • the TemporarySetpoint temperature (if specified)
         * • the TargetPercentage (if specified).
         */
        public Boolean oneShot; // bool
        /**
         * This field shall indicate that the consumer wants the water to be heated quickly. This may cause multiple
         * heat sources to be activated (e.g. a heat pump and direct electric immersion heating element).
         * The choice of which heat sources are activated is manufacturer specific.
         */
        public Boolean emergencyBoost; // bool
        /**
         * This field shall indicate the target temperature to which the water will be heated.
         * If included, it shall be used instead of the thermostat cluster set point temperature whilst the boost state
         * is activated.
         * The value of this field shall be within the constraints of the MinHeatSetpointLimit and MaxHeatSetpointLimit
         * attributes (inclusive), of the thermostat cluster.
         */
        public Integer temporarySetpoint; // temperature
        /**
         * This field shall indicate the target percentage of hot water in the tank that the TankPercentage attribute
         * must reach before the heating is switched off.
         */
        public Integer targetPercentage; // percent
        /**
         * This field shall indicate the percentage to which the hot water in the tank shall be allowed to fall before
         * again beginning to reheat it.
         * For example if the TargetPercentage was 80%, and the TargetReheat was 40%, then after initial heating to 80%
         * hot water, the tank may have hot water drawn off until only 40% hot water remains. At this point the heater
         * will begin to heat back up to 80% of hot water. If this field and the OneShot field were both omitted,
         * heating would begin again after any water draw which reduced the TankPercentage below 80%.
         * This field shall be less than or equal to the TargetPercentage field.
         */
        public Integer targetReheat; // percent

        public WaterHeaterBoostInfoStruct(Integer duration, Boolean oneShot, Boolean emergencyBoost,
                Integer temporarySetpoint, Integer targetPercentage, Integer targetReheat) {
            this.duration = duration;
            this.oneShot = oneShot;
            this.emergencyBoost = emergencyBoost;
            this.temporarySetpoint = temporarySetpoint;
            this.targetPercentage = targetPercentage;
            this.targetReheat = targetReheat;
        }
    }

    // Enums
    public enum BoostStateEnum implements MatterEnum {
        INACTIVE(0, "Inactive"),
        ACTIVE(1, "Active");

        public final Integer value;
        public final String label;

        private BoostStateEnum(Integer value, String label) {
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
    public static class WaterHeaterHeatSourceBitmap {
        public boolean immersionElement1;
        public boolean immersionElement2;
        public boolean heatPump;
        public boolean boiler;
        public boolean other;

        public WaterHeaterHeatSourceBitmap(boolean immersionElement1, boolean immersionElement2, boolean heatPump,
                boolean boiler, boolean other) {
            this.immersionElement1 = immersionElement1;
            this.immersionElement2 = immersionElement2;
            this.heatPump = heatPump;
            this.boiler = boiler;
            this.other = other;
        }
    }

    public static class FeatureMap {
        /**
         * 
         * Allows energy management control of the tank
         */
        public boolean energyManagement;
        /**
         * 
         * Supports monitoring the percentage of hot water in the tank
         */
        public boolean tankPercent;

        public FeatureMap(boolean energyManagement, boolean tankPercent) {
            this.energyManagement = energyManagement;
            this.tankPercent = tankPercent;
        }
    }

    public WaterHeaterManagementCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 148, "WaterHeaterManagement");
    }

    protected WaterHeaterManagementCluster(BigInteger nodeId, int endpointId, int clusterId, String clusterName) {
        super(nodeId, endpointId, clusterId, clusterName);
    }

    // commands
    /**
     * Allows a client to request that the water heater is put into a Boost state.
     */
    public static ClusterCommand boost(WaterHeaterBoostInfoStruct boostInfo) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (boostInfo != null) {
            map.put("boostInfo", boostInfo);
        }
        return new ClusterCommand("boost", map);
    }

    /**
     * Allows a client to cancel an ongoing Boost operation. This command has no payload.
     */
    public static ClusterCommand cancelBoost() {
        return new ClusterCommand("cancelBoost");
    }

    @Override
    public @NonNull String toString() {
        String str = "";
        str += "clusterRevision : " + clusterRevision + "\n";
        str += "featureMap : " + featureMap + "\n";
        str += "heaterTypes : " + heaterTypes + "\n";
        str += "heatDemand : " + heatDemand + "\n";
        str += "tankVolume : " + tankVolume + "\n";
        str += "estimatedHeatRequired : " + estimatedHeatRequired + "\n";
        str += "tankPercentage : " + tankPercentage + "\n";
        str += "boostState : " + boostState + "\n";
        return str;
    }
}
