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
 * BooleanStateConfiguration
 *
 * @author Dan Cunningham - Initial contribution
 */
public class BooleanStateConfigurationCluster extends BaseCluster {

    public static final int CLUSTER_ID = 0x0080;
    public static final String CLUSTER_NAME = "BooleanStateConfiguration";
    public static final String CLUSTER_PREFIX = "booleanStateConfiguration";
    public static final String ATTRIBUTE_CLUSTER_REVISION = "clusterRevision";
    public static final String ATTRIBUTE_FEATURE_MAP = "featureMap";
    public static final String ATTRIBUTE_CURRENT_SENSITIVITY_LEVEL = "currentSensitivityLevel";
    public static final String ATTRIBUTE_SUPPORTED_SENSITIVITY_LEVELS = "supportedSensitivityLevels";
    public static final String ATTRIBUTE_DEFAULT_SENSITIVITY_LEVEL = "defaultSensitivityLevel";
    public static final String ATTRIBUTE_ALARMS_ACTIVE = "alarmsActive";
    public static final String ATTRIBUTE_ALARMS_SUPPRESSED = "alarmsSuppressed";
    public static final String ATTRIBUTE_ALARMS_ENABLED = "alarmsEnabled";
    public static final String ATTRIBUTE_ALARMS_SUPPORTED = "alarmsSupported";
    public static final String ATTRIBUTE_SENSOR_FAULT = "sensorFault";

    public Integer clusterRevision; // 65533 ClusterRevision
    public FeatureMap featureMap; // 65532 FeatureMap
    /**
     * Indicates the currently selected sensitivity level.
     * If a write interaction to this attribute contains an unsupported sensitivity value, a CONSTRAINT_ERROR status
     * shall be returned.
     */
    public Integer currentSensitivityLevel; // 0 uint8 RW VO
    /**
     * Indicates the number of supported sensitivity levels by the device.
     * These supported sensitivity levels shall be ordered by sensitivity, where a value of 0 shall be considered the
     * lowest sensitivity level (least sensitive) and the highest supported value shall be considered the highest
     * sensitivity level.
     * The number of supported sensitivity levels SHOULD represent unique sensitivity levels supported by the device.
     */
    public Integer supportedSensitivityLevels; // 1 uint8 R V
    /**
     * Indicates the default sensitivity level selected by the manufacturer.
     */
    public Integer defaultSensitivityLevel; // 2 uint8 R V
    /**
     * Indicates which specific alarm modes on the server are currently active. When the sensor is no longer triggered,
     * this attribute shall be set to the inactive state, by setting the bit to 0, for all supported alarm modes.
     * If an alarm mode is not supported, the bit indicating this alarm mode shall always be 0. A bit shall indicate
     * whether the alarm mode inactive or not:
     * • 0 &#x3D; Inactive
     * • 1 &#x3D; Active
     */
    public AlarmModeBitmap alarmsActive; // 3 AlarmModeBitmap R V
    /**
     * Indicates which specific alarm modes on the server are currently suppressed. When the sensor is no longer
     * triggered, this attribute shall be set to the unsuppressed state, by setting the bit to 0, for all supported
     * alarm modes.
     * If an alarm mode is not supported, the bit indicating this alarm mode shall always be 0. A bit shall indicate
     * whether the alarm mode is suppressed or not:
     * • 0 &#x3D; Not suppressed
     * • 1 &#x3D; Suppressed
     */
    public AlarmModeBitmap alarmsSuppressed; // 4 AlarmModeBitmap R V
    /**
     * Indicates the alarm modes that will be emitted if the sensor is triggered.
     * If an alarm mode is not supported, the bit indicating this alarm mode shall always be 0. A bit shall indicate
     * whether the alarm mode is enabled or disabled:
     * • 0 &#x3D; Disabled
     * • 1 &#x3D; Enabled
     */
    public AlarmModeBitmap alarmsEnabled; // 5 AlarmModeBitmap R V
    /**
     * Indicates the alarms supported by the sensor. A bit shall indicate whether the alarm mode is supported:
     * • 0 &#x3D; Not supported
     * • 1 &#x3D; Supported
     */
    public AlarmModeBitmap alarmsSupported; // 6 AlarmModeBitmap R V
    /**
     * Indicates any faults registered by the device.
     */
    public SensorFaultBitmap sensorFault; // 7 SensorFaultBitmap R V

    // Structs
    /**
     * This event shall be generated after any bits in the AlarmsActive and/or AlarmsSuppressed attributes change. This
     * may occur in situations such as when internal processing by the server determines that an alarm mode becomes
     * active or inactive, or when the SuppressAlarm or EnableDisableAlarm commands are processed in a way that some
     * alarm modes becomes suppressed, active or inactive.
     * If several alarm modes change state at the same time, a single event combining multiple changes may be emitted
     * instead of multiple events each representing a single change.
     */
    public static class AlarmsStateChanged {
        /**
         * This field shall indicate the state of active alarm modes, as indicated by the AlarmsActive attribute, at the
         * time the event was generated.
         */
        public AlarmModeBitmap alarmsActive; // AlarmModeBitmap
        /**
         * This field shall indicate the state of suppressed alarm modes, as indicated by the AlarmsSuppressed
         * attribute, at the time the event was generated.
         */
        public AlarmModeBitmap alarmsSuppressed; // AlarmModeBitmap

        public AlarmsStateChanged(AlarmModeBitmap alarmsActive, AlarmModeBitmap alarmsSuppressed) {
            this.alarmsActive = alarmsActive;
            this.alarmsSuppressed = alarmsSuppressed;
        }
    }

    /**
     * This event shall be generated when the device registers or clears a fault.
     */
    public static class SensorFault {
        /**
         * This field shall indicate the value of the SensorFault attribute, at the time this event is generated.
         */
        public SensorFaultBitmap sensorFault; // SensorFaultBitmap

        public SensorFault(SensorFaultBitmap sensorFault) {
            this.sensorFault = sensorFault;
        }
    }

    // Bitmaps
    public static class AlarmModeBitmap {
        public boolean visual;
        public boolean audible;

        public AlarmModeBitmap(boolean visual, boolean audible) {
            this.visual = visual;
            this.audible = audible;
        }
    }

    public static class SensorFaultBitmap {
        public boolean generalFault;

        public SensorFaultBitmap(boolean generalFault) {
            this.generalFault = generalFault;
        }
    }

    public static class FeatureMap {
        /**
         * 
         * Supports visual alarms
         */
        public boolean visual;
        /**
         * 
         * Supports audible alarms
         */
        public boolean audible;
        /**
         * 
         * This feature shall indicate that the device is able to suppress the supported alarm modes, when the user
         * acknowledges the alarm. This is intended to stop visual and/or audible alarms, when the user has become aware
         * that the sensor is triggered, but it is no longer desired to have the alarm modes active on the device, e.g.:
         * • The triggering cause have been resolved by the user, but the sensor has not yet stopped detecting the
         * triggering cause.
         * • The user is not able to address the triggering cause, but is aware of the alarm and suppress/acknowledge it
         * be addressed at a later point.
         * Acknowledge of alarms will for the remainder of this cluster be referred to as suppress.
         * A suppressed alarm is still considered active and will remain so unless it is actively disabled or the
         * triggering condition is not longer present. The action of suppressing an alarm mode is only applicable to and
         * is intended to stop the physical alarming, e.g. emitting a sound or blinking a light; it does not impact
         * alarm reporting in AlarmsActive.
         */
        public boolean alarmSuppress;
        /**
         * 
         * Supports ability to set sensor sensitivity
         */
        public boolean sensitivityLevel;

        public FeatureMap(boolean visual, boolean audible, boolean alarmSuppress, boolean sensitivityLevel) {
            this.visual = visual;
            this.audible = audible;
            this.alarmSuppress = alarmSuppress;
            this.sensitivityLevel = sensitivityLevel;
        }
    }

    public BooleanStateConfigurationCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 128, "BooleanStateConfiguration");
    }

    protected BooleanStateConfigurationCluster(BigInteger nodeId, int endpointId, int clusterId, String clusterName) {
        super(nodeId, endpointId, clusterId, clusterName);
    }

    // commands
    public static ClusterCommand suppressAlarm(AlarmModeBitmap alarmsToSuppress) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (alarmsToSuppress != null) {
            map.put("alarmsToSuppress", alarmsToSuppress);
        }
        return new ClusterCommand("suppressAlarm", map);
    }

    public static ClusterCommand enableDisableAlarm(AlarmModeBitmap alarmsToEnableDisable) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (alarmsToEnableDisable != null) {
            map.put("alarmsToEnableDisable", alarmsToEnableDisable);
        }
        return new ClusterCommand("enableDisableAlarm", map);
    }

    @Override
    public @NonNull String toString() {
        String str = "";
        str += "clusterRevision : " + clusterRevision + "\n";
        str += "featureMap : " + featureMap + "\n";
        str += "currentSensitivityLevel : " + currentSensitivityLevel + "\n";
        str += "supportedSensitivityLevels : " + supportedSensitivityLevels + "\n";
        str += "defaultSensitivityLevel : " + defaultSensitivityLevel + "\n";
        str += "alarmsActive : " + alarmsActive + "\n";
        str += "alarmsSuppressed : " + alarmsSuppressed + "\n";
        str += "alarmsEnabled : " + alarmsEnabled + "\n";
        str += "alarmsSupported : " + alarmsSupported + "\n";
        str += "sensorFault : " + sensorFault + "\n";
        return str;
    }
}
