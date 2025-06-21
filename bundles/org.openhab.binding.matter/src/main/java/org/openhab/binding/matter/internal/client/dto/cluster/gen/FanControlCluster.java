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
 * FanControl
 *
 * @author Dan Cunningham - Initial contribution
 */
public class FanControlCluster extends BaseCluster {

    public static final int CLUSTER_ID = 0x0202;
    public static final String CLUSTER_NAME = "FanControl";
    public static final String CLUSTER_PREFIX = "fanControl";
    public static final String ATTRIBUTE_CLUSTER_REVISION = "clusterRevision";
    public static final String ATTRIBUTE_FEATURE_MAP = "featureMap";
    public static final String ATTRIBUTE_FAN_MODE = "fanMode";
    public static final String ATTRIBUTE_FAN_MODE_SEQUENCE = "fanModeSequence";
    public static final String ATTRIBUTE_PERCENT_SETTING = "percentSetting";
    public static final String ATTRIBUTE_PERCENT_CURRENT = "percentCurrent";
    public static final String ATTRIBUTE_SPEED_MAX = "speedMax";
    public static final String ATTRIBUTE_SPEED_SETTING = "speedSetting";
    public static final String ATTRIBUTE_SPEED_CURRENT = "speedCurrent";
    public static final String ATTRIBUTE_ROCK_SUPPORT = "rockSupport";
    public static final String ATTRIBUTE_ROCK_SETTING = "rockSetting";
    public static final String ATTRIBUTE_WIND_SUPPORT = "windSupport";
    public static final String ATTRIBUTE_WIND_SETTING = "windSetting";
    public static final String ATTRIBUTE_AIRFLOW_DIRECTION = "airflowDirection";

    public Integer clusterRevision; // 65533 ClusterRevision
    public FeatureMap featureMap; // 65532 FeatureMap
    /**
     * Indicates the current speed mode of the fan. This attribute may be written by the client to request a different
     * fan mode. A server shall return INVALID_IN_STATE to indicate that the fan is not in a state where the FanMode can
     * be changed to the requested value. A server may have FanMode values that it can never be set to. For example,
     * where this cluster appears on the same or another endpoint as other clusters with a system dependency, for
     * example the Thermostat cluster, attempting to set the FanMode attribute of this cluster to Off may not be allowed
     * by the system.
     * This attribute shall be set to one of the values in FanModeEnum.
     * When the FanMode attribute is successfully written to, the PercentSetting and SpeedSetting (if present)
     * attributes shall be set to appropriate values, as defined by the Section 4.4.6.3.1 and Section 4.4.6.6.1
     * respectively, unless otherwise specified below.
     * When the FanMode attribute is set to any given mode, the PercentCurrent and SpeedCurrent (if present) shall
     * indicate the actual currently operating fan speed, unless otherwise specified below.
     */
    public FanModeEnum fanMode; // 0 FanModeEnum RW VO
    /**
     * This attribute indicates the fan speed ranges that shall be supported.
     */
    public FanModeSequenceEnum fanModeSequence; // 1 FanModeSequenceEnum R V
    /**
     * Indicates the speed setting for the fan. This attribute may be written by the client to indicate a new fan speed.
     * If the client writes null to this attribute, the attribute value shall NOT change. A server shall return
     * INVALID_IN_STATE to indicate that the fan is not in a state where the PercentSetting can be changed to the
     * requested value.
     * If this is successfully written to 0, the server shall set the FanMode attribute value to Off.
     */
    public Integer percentSetting; // 2 percent RW VO
    /**
     * Indicates the actual currently operating fan speed, or zero to indicate that the fan is off. There may be a
     * temporary mismatch between the value of this attribute and the value of the PercentSetting attribute due to other
     * system requirements that would not allow the fan to operate at the requested setting. See Section 4.4.6.3.1 for
     * more details.
     */
    public Integer percentCurrent; // 3 percent R V
    /**
     * Indicates that the fan has one speed (value of 1) or the maximum speed, if the fan is capable of multiple speeds.
     */
    public Integer speedMax; // 4 uint8 R V
    /**
     * Indicates the speed setting for the fan. This attribute may be written by the client to indicate a new fan speed.
     * If the client writes null to this attribute, the attribute value shall NOT change. A server shall return
     * INVALID_IN_STATE to indicate that the fan is not in a state where the SpeedSetting can be changed to the
     * requested value.
     * If this is successfully written to 0, the server shall set the FanMode attribute value to Off. Please see the
     * Section 4.4.6.6.1 for details on other values.
     */
    public Integer speedSetting; // 5 uint8 RW VO
    /**
     * Indicates the actual currently operating fan speed, or zero to indicate that the fan is off. There may be a
     * temporary mismatch between the value of this attribute and the value of the SpeedSetting attribute due to other
     * system requirements that would not allow the fan to operate at the requested setting.
     */
    public Integer speedCurrent; // 6 uint8 R V
    /**
     * This attribute is a bitmap that indicates what rocking motions the server supports.
     */
    public RockBitmap rockSupport; // 7 RockBitmap R V
    /**
     * This attribute is a bitmap that indicates the current active fan rocking motion settings. Each bit shall only be
     * set to 1, if the corresponding bit in the RockSupport attribute is set to 1, otherwise a status code of
     * CONSTRAINT_ERROR shall be returned.
     * If a combination of supported bits is set by the client, and the server does not support the combination, the
     * lowest supported single bit in the combination shall be set and active, and all other bits shall indicate zero.
     * For example: If RockUpDown and RockRound are both set, but this combination is not possible, then only RockUpDown
     * becomes active.
     */
    public RockBitmap rockSetting; // 8 RockBitmap RW VO
    /**
     * This attribute is a bitmap that indicates what wind modes the server supports. At least one wind mode bit shall
     * be set.
     */
    public WindBitmap windSupport; // 9 WindBitmap R V
    /**
     * This attribute is a bitmap that indicates the current active fan wind feature settings. Each bit shall only be
     * set to 1, if the corresponding bit in the WindSupport attribute is set to 1, otherwise a status code of
     * CONSTRAINT_ERROR shall be returned.
     * If a combination of supported bits is set by the client, and the server does not support the combination, the
     * lowest supported single bit in the combination shall be set and active, and all other bits shall indicate zero.
     * For example: If Sleep Wind and Natural Wind are set, but this combination is not possible, then only Sleep Wind
     * becomes active.
     */
    public WindBitmap windSetting; // 10 WindBitmap RW VO
    /**
     * Indicates the current airflow direction of the fan. This attribute may be written by the client to indicate a new
     * airflow direction for the fan. This attribute shall be set to one of the values in the AirflowDirectionEnum
     * table.
     */
    public AirflowDirectionEnum airflowDirection; // 11 AirflowDirectionEnum RW VO

    // Enums
    public enum StepDirectionEnum implements MatterEnum {
        INCREASE(0, "Increase"),
        DECREASE(1, "Decrease");

        public final Integer value;
        public final String label;

        private StepDirectionEnum(Integer value, String label) {
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

    public enum AirflowDirectionEnum implements MatterEnum {
        FORWARD(0, "Forward"),
        REVERSE(1, "Reverse");

        public final Integer value;
        public final String label;

        private AirflowDirectionEnum(Integer value, String label) {
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

    public enum FanModeEnum implements MatterEnum {
        OFF(0, "Off"),
        LOW(1, "Low"),
        MEDIUM(2, "Medium"),
        HIGH(3, "High"),
        ON(4, "On"),
        AUTO(5, "Auto"),
        SMART(6, "Smart");

        public final Integer value;
        public final String label;

        private FanModeEnum(Integer value, String label) {
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

    public enum FanModeSequenceEnum implements MatterEnum {
        OFF_LOW_MED_HIGH(0, "Off Low Med High"),
        OFF_LOW_HIGH(1, "Off Low High"),
        OFF_LOW_MED_HIGH_AUTO(2, "Off Low Med High Auto"),
        OFF_LOW_HIGH_AUTO(3, "Off Low High Auto"),
        OFF_HIGH_AUTO(4, "Off High Auto"),
        OFF_HIGH(5, "Off High");

        public final Integer value;
        public final String label;

        private FanModeSequenceEnum(Integer value, String label) {
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
    public static class RockBitmap {
        public boolean rockLeftRight;
        public boolean rockUpDown;
        public boolean rockRound;

        public RockBitmap(boolean rockLeftRight, boolean rockUpDown, boolean rockRound) {
            this.rockLeftRight = rockLeftRight;
            this.rockUpDown = rockUpDown;
            this.rockRound = rockRound;
        }
    }

    public static class WindBitmap {
        public boolean sleepWind;
        public boolean naturalWind;

        public WindBitmap(boolean sleepWind, boolean naturalWind) {
            this.sleepWind = sleepWind;
            this.naturalWind = naturalWind;
        }
    }

    public static class FeatureMap {
        /**
         * 
         * Legacy Fan Control cluster revision 0-1 defined 3 speeds (low, medium and high) plus automatic speed control
         * but left it up to the implementer to decide what was supported. Therefore, it is assumed that legacy client
         * implementations are capable of determining, from the server, the number of speeds supported between 1, 2, or
         * 3, and whether automatic speed control is supported.
         * The MultiSpeed feature includes new attributes that support a running fan speed value from 0 to SpeedMax,
         * which has a maximum of 100.
         * See Section 4.4.6.6.1 for more details.
         */
        public boolean multiSpeed;
        /**
         * 
         * Automatic mode supported for fan speed
         */
        public boolean auto;
        /**
         * 
         * Rocking movement supported
         */
        public boolean rocking;
        /**
         * 
         * Wind emulation supported
         */
        public boolean wind;
        /**
         * 
         * Step command supported
         */
        public boolean step;
        /**
         * 
         * Airflow Direction attribute is supported
         */
        public boolean airflowDirection;

        public FeatureMap(boolean multiSpeed, boolean auto, boolean rocking, boolean wind, boolean step,
                boolean airflowDirection) {
            this.multiSpeed = multiSpeed;
            this.auto = auto;
            this.rocking = rocking;
            this.wind = wind;
            this.step = step;
            this.airflowDirection = airflowDirection;
        }
    }

    public FanControlCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 514, "FanControl");
    }

    protected FanControlCluster(BigInteger nodeId, int endpointId, int clusterId, String clusterName) {
        super(nodeId, endpointId, clusterId, clusterName);
    }

    // commands
    /**
     * This command speeds up or slows down the fan, in steps, without the client having to know the fan speed. This
     * command supports, for example, a user operated wall switch, where the user provides the feedback or control to
     * stop sending this command when the proper speed is reached. The step speed values are implementation specific.
     * How many step speeds are implemented is implementation specific.
     * This command supports these fields:
     */
    public static ClusterCommand step(StepDirectionEnum direction, Boolean wrap, Boolean lowestOff) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (direction != null) {
            map.put("direction", direction);
        }
        if (wrap != null) {
            map.put("wrap", wrap);
        }
        if (lowestOff != null) {
            map.put("lowestOff", lowestOff);
        }
        return new ClusterCommand("step", map);
    }

    @Override
    public @NonNull String toString() {
        String str = "";
        str += "clusterRevision : " + clusterRevision + "\n";
        str += "featureMap : " + featureMap + "\n";
        str += "fanMode : " + fanMode + "\n";
        str += "fanModeSequence : " + fanModeSequence + "\n";
        str += "percentSetting : " + percentSetting + "\n";
        str += "percentCurrent : " + percentCurrent + "\n";
        str += "speedMax : " + speedMax + "\n";
        str += "speedSetting : " + speedSetting + "\n";
        str += "speedCurrent : " + speedCurrent + "\n";
        str += "rockSupport : " + rockSupport + "\n";
        str += "rockSetting : " + rockSetting + "\n";
        str += "windSupport : " + windSupport + "\n";
        str += "windSetting : " + windSetting + "\n";
        str += "airflowDirection : " + airflowDirection + "\n";
        return str;
    }
}
