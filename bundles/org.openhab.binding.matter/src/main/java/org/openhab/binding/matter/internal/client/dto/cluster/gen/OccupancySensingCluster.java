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
 * OccupancySensing
 *
 * @author Dan Cunningham - Initial contribution
 */
public class OccupancySensingCluster extends BaseCluster {

    public static final int CLUSTER_ID = 0x0406;
    public static final String CLUSTER_NAME = "OccupancySensing";
    public static final String CLUSTER_PREFIX = "occupancySensing";
    public static final String ATTRIBUTE_CLUSTER_REVISION = "clusterRevision";
    public static final String ATTRIBUTE_FEATURE_MAP = "featureMap";
    public static final String ATTRIBUTE_OCCUPANCY = "occupancy";
    public static final String ATTRIBUTE_OCCUPANCY_SENSOR_TYPE = "occupancySensorType";
    public static final String ATTRIBUTE_OCCUPANCY_SENSOR_TYPE_BITMAP = "occupancySensorTypeBitmap";
    public static final String ATTRIBUTE_HOLD_TIME = "holdTime";
    public static final String ATTRIBUTE_HOLD_TIME_LIMITS = "holdTimeLimits";
    public static final String ATTRIBUTE_PIR_OCCUPIED_TO_UNOCCUPIED_DELAY = "pirOccupiedToUnoccupiedDelay";
    public static final String ATTRIBUTE_PIR_UNOCCUPIED_TO_OCCUPIED_DELAY = "pirUnoccupiedToOccupiedDelay";
    public static final String ATTRIBUTE_PIR_UNOCCUPIED_TO_OCCUPIED_THRESHOLD = "pirUnoccupiedToOccupiedThreshold";
    public static final String ATTRIBUTE_ULTRASONIC_OCCUPIED_TO_UNOCCUPIED_DELAY = "ultrasonicOccupiedToUnoccupiedDelay";
    public static final String ATTRIBUTE_ULTRASONIC_UNOCCUPIED_TO_OCCUPIED_DELAY = "ultrasonicUnoccupiedToOccupiedDelay";
    public static final String ATTRIBUTE_ULTRASONIC_UNOCCUPIED_TO_OCCUPIED_THRESHOLD = "ultrasonicUnoccupiedToOccupiedThreshold";
    public static final String ATTRIBUTE_PHYSICAL_CONTACT_OCCUPIED_TO_UNOCCUPIED_DELAY = "physicalContactOccupiedToUnoccupiedDelay";
    public static final String ATTRIBUTE_PHYSICAL_CONTACT_UNOCCUPIED_TO_OCCUPIED_DELAY = "physicalContactUnoccupiedToOccupiedDelay";
    public static final String ATTRIBUTE_PHYSICAL_CONTACT_UNOCCUPIED_TO_OCCUPIED_THRESHOLD = "physicalContactUnoccupiedToOccupiedThreshold";

    public Integer clusterRevision; // 65533 ClusterRevision
    public FeatureMap featureMap; // 65532 FeatureMap
    /**
     * Indicates the sensed (processed) status of occupancy. For compatibility reasons this is expressed as a bitmap
     * where the status is indicated in bit 0: a value of 1 means occupied, and 0 means unoccupied, with the other bits
     * set to 0; this can be considered equivalent to a boolean.
     */
    public OccupancyBitmap occupancy; // 0 OccupancyBitmap R V
    public OccupancySensorTypeEnum occupancySensorType; // 1 OccupancySensorTypeEnum R V
    public OccupancySensorTypeBitmap occupancySensorTypeBitmap; // 2 OccupancySensorTypeBitmap R V
    /**
     * This attribute shall specify the time delay, in seconds, before the sensor changes to its unoccupied state after
     * the last detection of occupancy in the sensed area. This is equivalent to the legacy
     * OccupiedToUnoccupiedDelay attributes.
     * The value of HoldTime shall be within the limits provided in the HoldTimeLimits attribute, i.e. HoldTimeMin
     * &lt;&#x3D; HoldTime &lt;&#x3D; HoldTimeMax Low values of HoldTime SHOULD be avoided since they could lead to many
     * reporting messages. A value 0 for HoldTime shall NOT be used.
     * The figure below illustrates this with an example of how this attribute is used for a PIR sensor. It uses
     * threshold detection to generate an &quot;internal detection&quot; signal, which needs post-processing to become
     * usable for transmission (traffic shaping). The bit in the Occupancy attribute will be set to 1 when the internal
     * detection signal goes high, and will stay at 1 for HoldTime after the (last) instance where the internal
     * detection signal goes low.
     * The top half of the figure shows the case of a single trigger: the bit in the Occupancy attribute will be 1 for
     * the duration of the PIR signal exceeding the threshold plus HoldTime. The bottom half of the figure shows the
     * case of multiple triggers: the second trigger starts before the HoldTime of the first trigger has expired; this
     * results in a single period of the bit in the Occupancy attribute being 1. The bit in the Occupancy attribute will
     * be set to 1 from the start of the first period where the PIR signal exceeds the threshold until HoldTime after
     * the last moment where the PIR exceeded the threshold.
     */
    public Integer holdTime; // 3 uint16 RW VM
    /**
     * Indicates the server’s limits, and default value, for the HoldTime attribute.
     */
    public HoldTimeLimitsStruct holdTimeLimits; // 4 HoldTimeLimitsStruct R V
    /**
     * This attribute shall specify the time delay, in seconds, before the PIR sensor changes to its unoccupied state
     * after the last detection of occupancy in the sensed area.
     */
    public Integer pirOccupiedToUnoccupiedDelay; // 16 uint16 RW VM
    /**
     * This attribute shall specify the time delay, in seconds, before the PIR sensor changes to its occupied state
     * after the first detection of occupancy in the sensed area.
     */
    public Integer pirUnoccupiedToOccupiedDelay; // 17 uint16 RW VM
    /**
     * This attribute shall specify the number of occupancy detection events that must occur in the period
     * PIRUnoccupiedToOccupiedDelay, before the PIR sensor changes to its occupied state.
     */
    public Integer pirUnoccupiedToOccupiedThreshold; // 18 uint8 RW VM
    /**
     * This attribute shall specify the time delay, in seconds, before the Ultrasonic sensor changes to its unoccupied
     * state after the last detection of occupancy in the sensed area.
     */
    public Integer ultrasonicOccupiedToUnoccupiedDelay; // 32 uint16 RW VM
    /**
     * This attribute shall specify the time delay, in seconds, before the Ultrasonic sensor changes to its occupied
     * state after the first detection of occupancy in the sensed area.
     */
    public Integer ultrasonicUnoccupiedToOccupiedDelay; // 33 uint16 RW VM
    /**
     * This attribute shall specify the number of occupancy detection events that must occur in the period
     * UltrasonicUnoccupiedToOccupiedDelay, before the Ultrasonic sensor changes to its occupied state.
     */
    public Integer ultrasonicUnoccupiedToOccupiedThreshold; // 34 uint8 RW VM
    /**
     * This attribute shall specify the time delay, in seconds, before the physical contact occupancy sensor changes to
     * its unoccupied state after detecting the unoccupied event.
     */
    public Integer physicalContactOccupiedToUnoccupiedDelay; // 48 uint16 RW VM
    /**
     * This attribute shall specify the time delay, in seconds, before the physical contact sensor changes to its
     * occupied state after the first detection of the occupied event.
     */
    public Integer physicalContactUnoccupiedToOccupiedDelay; // 49 uint16 RW VM
    /**
     * This attribute shall specify the number of occupancy detection events that must occur in the period
     * PhysicalContactUnoccupiedToOccupiedDelay, before the PhysicalContact sensor changes to its occupied state.
     */
    public Integer physicalContactUnoccupiedToOccupiedThreshold; // 50 uint8 RW VM

    // Structs
    /**
     * If this event is supported, it shall be generated when the Occupancy attribute changes.
     */
    public static class OccupancyChanged {
        /**
         * This field shall indicate the new value of the Occupancy attribute.
         */
        public OccupancyBitmap occupancy; // OccupancyBitmap

        public OccupancyChanged(OccupancyBitmap occupancy) {
            this.occupancy = occupancy;
        }
    }

    /**
     * This structure provides information on the server’s supported values for the HoldTime attribute.
     */
    public static class HoldTimeLimitsStruct {
        /**
         * This field shall specify the minimum value of the server’s supported value for the HoldTime attribute, in
         * seconds.
         */
        public Integer holdTimeMin; // uint16
        /**
         * This field shall specify the maximum value of the server’s supported value for the HoldTime attribute, in
         * seconds.
         */
        public Integer holdTimeMax; // uint16
        /**
         * This field shall specify the (manufacturer-determined) default value of the server’s HoldTime attribute, in
         * seconds. This is the value that a client who wants to reset the settings to a valid default SHOULD use.
         */
        public Integer holdTimeDefault; // uint16

        public HoldTimeLimitsStruct(Integer holdTimeMin, Integer holdTimeMax, Integer holdTimeDefault) {
            this.holdTimeMin = holdTimeMin;
            this.holdTimeMax = holdTimeMax;
            this.holdTimeDefault = holdTimeDefault;
        }
    }

    // Enums
    /**
     * &gt; [!NOTE]
     * &gt; This enum is as defined in ClusterRevision 4 and its definition shall NOT be extended; the feature flags
     * provide the sensor modality (or modalities) for later cluster revisions. See Backward Compatibility section.
     */
    public enum OccupancySensorTypeEnum implements MatterEnum {
        PIR(0, "Pir"),
        ULTRASONIC(1, "Ultrasonic"),
        PIR_AND_ULTRASONIC(2, "Pir And Ultrasonic"),
        PHYSICAL_CONTACT(3, "Physical Contact");

        public final Integer value;
        public final String label;

        private OccupancySensorTypeEnum(Integer value, String label) {
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
    public static class OccupancyBitmap {
        /**
         * Indicates the sensed occupancy state
         * If this bit is set, it shall indicate the occupied state else if the bit if not set, it shall indicate the
         * unoccupied state.
         */
        public boolean occupied;

        public OccupancyBitmap(boolean occupied) {
            this.occupied = occupied;
        }
    }

    /**
     * &gt; [!NOTE]
     * &gt; This enum is as defined in ClusterRevision 4 and its definition shall NOT be extended; the feature flags
     * provide the sensor modality (or modalities) for later cluster revisions. See Backward Compatibility section.
     */
    public static class OccupancySensorTypeBitmap {
        public boolean pir;
        public boolean ultrasonic;
        public boolean physicalContact;

        public OccupancySensorTypeBitmap(boolean pir, boolean ultrasonic, boolean physicalContact) {
            this.pir = pir;
            this.ultrasonic = ultrasonic;
            this.physicalContact = physicalContact;
        }
    }

    public static class FeatureMap {
        /**
         * 
         * Supports sensing using a modality not listed in the other bits
         */
        public boolean other;
        /**
         * 
         * Supports sensing using PIR (Passive InfraRed)
         */
        public boolean passiveInfrared;
        /**
         * 
         * Supports sensing using UltraSound
         */
        public boolean ultrasonic;
        /**
         * 
         * Supports sensing using a physical contact
         */
        public boolean physicalContact;
        /**
         * 
         * Supports sensing using Active InfraRed measurement (e.g. time-of-flight or transflective/reflec tive IR
         * sensing)
         */
        public boolean activeInfrared;
        /**
         * 
         * Supports sensing using radar waves (microwave)
         */
        public boolean radar;
        /**
         * 
         * Supports sensing based on RF signal analysis
         */
        public boolean rfSensing;
        /**
         * 
         * Supports sensing based on analyzing images
         */
        public boolean vision;

        public FeatureMap(boolean other, boolean passiveInfrared, boolean ultrasonic, boolean physicalContact,
                boolean activeInfrared, boolean radar, boolean rfSensing, boolean vision) {
            this.other = other;
            this.passiveInfrared = passiveInfrared;
            this.ultrasonic = ultrasonic;
            this.physicalContact = physicalContact;
            this.activeInfrared = activeInfrared;
            this.radar = radar;
            this.rfSensing = rfSensing;
            this.vision = vision;
        }
    }

    public OccupancySensingCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 1030, "OccupancySensing");
    }

    protected OccupancySensingCluster(BigInteger nodeId, int endpointId, int clusterId, String clusterName) {
        super(nodeId, endpointId, clusterId, clusterName);
    }

    @Override
    public @NonNull String toString() {
        String str = "";
        str += "clusterRevision : " + clusterRevision + "\n";
        str += "featureMap : " + featureMap + "\n";
        str += "occupancy : " + occupancy + "\n";
        str += "occupancySensorType : " + occupancySensorType + "\n";
        str += "occupancySensorTypeBitmap : " + occupancySensorTypeBitmap + "\n";
        str += "holdTime : " + holdTime + "\n";
        str += "holdTimeLimits : " + holdTimeLimits + "\n";
        str += "pirOccupiedToUnoccupiedDelay : " + pirOccupiedToUnoccupiedDelay + "\n";
        str += "pirUnoccupiedToOccupiedDelay : " + pirUnoccupiedToOccupiedDelay + "\n";
        str += "pirUnoccupiedToOccupiedThreshold : " + pirUnoccupiedToOccupiedThreshold + "\n";
        str += "ultrasonicOccupiedToUnoccupiedDelay : " + ultrasonicOccupiedToUnoccupiedDelay + "\n";
        str += "ultrasonicUnoccupiedToOccupiedDelay : " + ultrasonicUnoccupiedToOccupiedDelay + "\n";
        str += "ultrasonicUnoccupiedToOccupiedThreshold : " + ultrasonicUnoccupiedToOccupiedThreshold + "\n";
        str += "physicalContactOccupiedToUnoccupiedDelay : " + physicalContactOccupiedToUnoccupiedDelay + "\n";
        str += "physicalContactUnoccupiedToOccupiedDelay : " + physicalContactUnoccupiedToOccupiedDelay + "\n";
        str += "physicalContactUnoccupiedToOccupiedThreshold : " + physicalContactUnoccupiedToOccupiedThreshold + "\n";
        return str;
    }
}
