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

    public FeatureMap featureMap; // 65532 FeatureMap
    /**
     * Indicates the current speed mode of the fan.
     * This attribute shall be set to one of the values in FanModeEnum supported by the server as indicated in the
     * FanModeSequence attribute. The Low value shall be supported if and only if the FanModeSequence attribute value is
     * less than 4. The Medium value shall be supported if and only if the FanModeSequence attribute value is 0 or 2.
     * This attribute may be written by a client to request a different fan mode. The server shall return
     * INVALID_IN_STATE to indicate that the fan is not in a state where this attribute can be changed to the requested
     * value.
     * The server may have values that this attribute can never be set to or that will be ignored by the server. For
     * example, where this cluster appears on the same or another endpoint as other clusters with a system dependency,
     * for example the Thermostat cluster, attempting to set this attribute to Off may not be allowed by the system.
     * If an attempt is made to set this attribute to a value not supported by the server as indicated in the
     * FanModeSequence attribute, the server shall respond with CONSTRAINT_ERROR.
     * When this attribute is successfully written to, the PercentSetting and SpeedSetting (if present) attributes shall
     * be set to appropriate values, as defined by Section 4.4.6.3.1, “Percent Rules” and Section 4.4.6.6.1, “Speed
     * Rules” respectively, unless otherwise specified below.
     * When this attribute is set to any valid value, the PercentCurrent and SpeedCurrent (if present) attributes shall
     * indicate the actual currently operating fan speed, unless otherwise specified below.
     */
    public FanModeEnum fanMode; // 0 FanModeEnum RW VO
    /**
     * This attribute indicates the fan speed ranges that shall be supported by the server.
     */
    public FanModeSequenceEnum fanModeSequence; // 1 FanModeSequenceEnum R V
    /**
     * Indicates the speed setting for the fan with a value of 0 indicating that the fan is off and a value of 100
     * indicating that the fan is set to run at its maximum speed. If the FanMode attribute is set to Auto, the value of
     * this attribute shall be set to null.
     * This attribute may be written to by a client to indicate a new fan speed. If a client writes null to this
     * attribute, the attribute value shall NOT change. If the fan is in a state where this attribute cannot be changed
     * to the requested value, the server shall return INVALID_IN_STATE.
     * When this attribute is successfully written, the server shall set the value of the FanMode and SpeedSetting (if
     * present) attributes to values that abide by the mapping requirements listed below.
     */
    public Integer percentSetting; // 2 percent RW VO
    /**
     * Indicates the actual currently operating fan speed, or zero to indicate that the fan is off. There may be a
     * temporary mismatch between the value of this attribute and the value of the PercentSetting attribute due to other
     * system requirements or constraints that would not allow the fan to operate at the requested setting.
     * For example, if the value of this attribute is currently 50%, and the PercentSetting attribute is newly set to
     * 25%, the value of this attribute may stay above 25% for a period necessary to dissipate internal heat, maintain
     * product operational safety, etc.
     * When the value of the FanMode attribute is AUTO, the value of this attribute may vary across the range over time.
     * See Section 4.4.6.3.1, “Percent Rules” for more details.
     */
    public Integer percentCurrent; // 3 percent R V
    /**
     * Indicates the maximum value to which the SpeedSetting attribute can be set.
     */
    public Integer speedMax; // 4 uint8 R V
    /**
     * Indicates the speed setting for the fan. This attribute may be written by a client to indicate a new fan speed.
     * If the FanMode attribute is set to Auto, the value of this attribute shall be set to null.
     * The server shall support all values between 0 and SpeedMax.
     * If a client writes null to this attribute, the attribute value shall NOT change. If the fan is in a state where
     * this attribute cannot be changed to the requested value, the server shall return INVALID_IN_STATE.
     * When this attribute is successfully written to, the server shall set the value of the FanMode and PercentSetting
     * attributes to values that abide by the mapping requirements listed below.
     */
    public Integer speedSetting; // 5 uint8 RW VO
    /**
     * Indicates the actual currently operating fan speed, or zero to indicate that the fan is off. There may be a
     * temporary mismatch between the value of this attribute and the value of the SpeedSetting attribute due to other
     * system requirements or constraints that would not allow the fan to operate at the requested setting.
     * For example, if the value of this attribute is currently 5, and the SpeedSetting attribute is newly set to 2, the
     * value of this attribute may stay above 2 for a period necessary to dissipate internal heat, maintain product
     * operational safety, etc.
     * When the value of the FanMode attribute is AUTO, the value of this attribute may vary across the range over time.
     * See Section 4.4.6.6.1, “Speed Rules” for more details.
     */
    public Integer speedCurrent; // 6 uint8 R V
    /**
     * This attribute is a bitmap that indicates the rocking motions that are supported by the server. If this attribute
     * is supported by the server, at least one bit shall be set in this attribute.
     */
    public RockBitmap rockSupport; // 7 RockBitmap R V
    /**
     * This attribute is a bitmap that indicates the currently active fan rocking motion setting. Each bit shall only be
     * set to 1, if the corresponding bit in the RockSupport attribute is set to 1, otherwise a status code of
     * CONSTRAINT_ERROR shall be returned.
     * If a combination of supported bits is set by a client, and the server does not support the combination, the
     * lowest supported single bit in the combination shall be set and active, and all other bits shall indicate zero.
     * For example: If RockUpDown and RockRound are both set, but this combination is not possible, then only RockUpDown
     * becomes active.
     */
    public RockBitmap rockSetting; // 8 RockBitmap RW VO
    /**
     * This attribute is a bitmap that indicates what wind modes are supported by the server. If this attribute is
     * supported by the server, at least one bit shall be set in this attribute.
     */
    public WindBitmap windSupport; // 9 WindBitmap R V
    /**
     * This attribute is a bitmap that indicates the current active fan wind feature settings. Each bit shall only be
     * set to 1, if the corresponding bit in the WindSupport attribute is set to 1, otherwise a status code of
     * CONSTRAINT_ERROR shall be returned.
     * If a combination of supported bits is set by a client, and the server does not support the combination, the
     * lowest supported single bit in the combination shall be set and active, and all other bits shall indicate zero.
     * For example: If Sleep Wind and Natural Wind are set, but this combination is not possible, then only Sleep Wind
     * becomes active.
     */
    public WindBitmap windSetting; // 10 WindBitmap RW VO
    /**
     * Indicates the current airflow direction of the fan. This attribute may be written by a client to indicate a new
     * airflow direction for the fan. This attribute shall be set to one of the values in the AirflowDirectionEnum
     * table.
     */
    public AirflowDirectionEnum airflowDirection; // 11 AirflowDirectionEnum RW VO

    // Enums
    public enum StepDirectionEnum implements MatterEnum {
        INCREASE(0, "Increase"),
        DECREASE(1, "Decrease");

        private final Integer value;
        private final String label;

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

        private final Integer value;
        private final String label;

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

        private final Integer value;
        private final String label;

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

        private final Integer value;
        private final String label;

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
        /**
         * Indicate sleep wind
         * The fan speed, based on current settings, shall gradually slow down to a final minimum speed. For this
         * process, the sequence, speeds and duration are MS.
         */
        public boolean sleepWind;
        /**
         * Indicate natural wind
         * The fan speed shall vary to emulate natural wind. For this setting, the sequence, speeds and duration are MS.
         */
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
         * The MultiSpeed feature includes attributes that support a running fan speed value from 0 to SpeedMax.
         * See Section 4.4.6.6.1, “Speed Rules” for more details.
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
     * This command indirectly changes the speed-oriented attributes of the fan in steps rather than using the
     * speed-oriented attributes, FanMode, PercentSetting, or SpeedSetting, directly. This command supports, for
     * example, a user-operated and wall-mounted toggle switch that can be used to increase or decrease the speed of the
     * fan by pressing the toggle switch up or down until the desired fan speed is reached. How this command is
     * interpreted by the server and how it affects the values of the speed-oriented attributes is implementation
     * specific.
     * For example, a fan supports this command, and the value of the FanModeSequence attribute is 0. The current value
     * of the FanMode attribute is 2, or Medium. This command is received with the Direction field set to Increase. As
     * per it’s specific implementation, the server reacts to the command by setting the value of the FanMode attribute
     * to 3, or High, which in turn sets the PercentSetting and SpeedSetting (if present) attributes to appropriate
     * values, as defined by Section 4.4.6.3.1, “Percent Rules” and Section 4.4.6.6.1, “Speed Rules” respectively.
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
