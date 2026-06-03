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
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.binding.matter.internal.client.dto.cluster.ClusterCommand;

/**
 * ZoneManagement
 *
 * @author Dan Cunningham - Initial contribution
 */
public class ZoneManagementCluster extends BaseCluster {

    public static final int CLUSTER_ID = 0x0550;
    public static final String CLUSTER_NAME = "ZoneManagement";
    public static final String CLUSTER_PREFIX = "zoneManagement";
    public static final String ATTRIBUTE_FEATURE_MAP = "featureMap";
    public static final String ATTRIBUTE_MAX_USER_DEFINED_ZONES = "maxUserDefinedZones";
    public static final String ATTRIBUTE_MAX_ZONES = "maxZones";
    public static final String ATTRIBUTE_ZONES = "zones";
    public static final String ATTRIBUTE_TRIGGERS = "triggers";
    public static final String ATTRIBUTE_SENSITIVITY_MAX = "sensitivityMax";
    public static final String ATTRIBUTE_SENSITIVITY = "sensitivity";
    public static final String ATTRIBUTE_TWO_DCARTESIAN_MAX = "twoDCartesianMax";

    public FeatureMap featureMap; // 65532 FeatureMap
    /**
     * This attribute shall specify the maximum number of user-defined zones that can be supported by the Node. This
     * value is manufacturer-defined.
     */
    public Integer maxUserDefinedZones; // 0 uint8 R V
    /**
     * This attribute shall specify the maximum number of zones allowed to created. This value shall be the sum of the
     * number of predefined Mfg Zones, and MaxUserDefinedZones, if supported. This value is manufacturer-defined.
     */
    public Integer maxZones; // 1 uint8 R V
    /**
     * This attribute shall specify all currently defined zones as a list of ZoneInformationStruct. Use the commands
     * from this cluster to add, update or remove entries.
     */
    public List<ZoneInformationStruct> zones; // 2 list R V
    /**
     * This attribute shall specify all currently defined triggers controlling the generation of ZoneTriggered and
     * ZoneStopped events and shall be a list of ZoneTriggerControlStruct. To add an entry use CreateOrUpdateTrigger. To
     * remove an entry use RemoveTrigger.
     */
    public List<ZoneTriggerControlStruct> triggers; // 3 list R V
    /**
     * This attribute shall specify the hardware specific value for the number of supported sensitivity levels. This
     * value is manufacturer defined. If the PerZoneSensitivity feature is supported, the value of this attribute
     * determines valid values for the Sensitivity field in ZoneTriggerControlStruct; if the PerZoneSensitivity feature
     * is not supported, the value of this attribute determines valid values for the Sensitivity Attribute.
     * Implementations require two to ten levels of sensitivity control in order to ensure that there is some user-level
     * customization of the Trigger.
     */
    public Integer sensitivityMax; // 4 uint8 R V
    /**
     * This attribute shall specify the sensitivity of the underlying zone triggering detection mechanism if the
     * PerZoneSensitivity features is not supported. The higher the value the more sensitive the detection. The actual
     * meaning of the values is implementation specific.
     */
    public Integer sensitivity; // 5 uint8 RW VO
    /**
     * This attribute shall specify the maximum X and Y points that are allowed for TwoD Cartesian Zones. If this
     * cluster is on the same endpoint as Camera AV Stream Management Cluster, these values shall be equal to the value
     * of SensorWidth - 1 and SensorHeight - 1 from the VideoSensorParams attribute.
     */
    public TwoDCartesianVertexStruct twoDCartesianMax; // 6 TwoDCartesianVertexStruct R V

    // Structs
    /**
     * This event shall be generated when a Zone is first triggered.
     */
    public static class ZoneTriggered {
        /**
         * This field shall contain the ZoneID of the Zone that triggered.
         */
        public Integer zone; // ZoneID
        /**
         * This field shall indicate why the zone was triggered.
         */
        public ZoneEventTriggeredReasonEnum reason; // ZoneEventTriggeredReasonEnum

        public ZoneTriggered(Integer zone, ZoneEventTriggeredReasonEnum reason) {
            this.zone = zone;
            this.reason = reason;
        }
    }

    /**
     * This event shall be generated when either the TriggerDetectedDuration value is exceeded by the
     * TimeSinceInitialTrigger value or the MaxDuration value is exceeded by the TimeSinceInitialTrigger value, as
     * described in Section 2.14.5.9, "ZoneTriggerControlStruct".
     */
    public static class ZoneStopped {
        /**
         * This field shall contain the ZoneID of the Zone that stopped.
         */
        public Integer zone; // ZoneID
        /**
         * This field shall indicate why the zone stopped triggering.
         */
        public ZoneEventStoppedReasonEnum reason; // ZoneEventStoppedReasonEnum

        public ZoneStopped(Integer zone, ZoneEventStoppedReasonEnum reason) {
            this.zone = zone;
            this.reason = reason;
        }
    }

    /**
     * This struct is used to encode a point on the 2 Dimensional Cartesian Plane for the TwoDCartesianZone feature.
     */
    public static class TwoDCartesianVertexStruct {
        /**
         * This field shall represent the position of the vertex along the horizontal (x) axis.
         */
        public Integer x; // uint16
        /**
         * This field shall represent the position of the vertex along the vertical (y) axis.
         */
        public Integer y; // uint16

        public TwoDCartesianVertexStruct(Integer x, Integer y) {
            this.x = x;
            this.y = y;
        }
    }

    /**
     * This struct is used to encode all information needed to define a TwoDCartesianZone.
     */
    public static class TwoDCartesianZoneStruct {
        /**
         * The Name field shall be a string representing the name of the Zone. This is not guaranteed to be unique.
         */
        public String name; // string
        /**
         * The Use field shall be a Zone Use Enum representing the purpose of the Zone.
         */
        public ZoneUseEnum use; // ZoneUseEnum
        /**
         * The Vertices field shall be a list of vertices of type TwoDCartesianVertexStruct. These vertices define a
         * simple polygon on the TwoD Cartesian plane, which represents the bounds of the TwoD Cartesian Zone with an
         * implicit connection between the last and first list items.
         */
        public List<TwoDCartesianVertexStruct> vertices; // list
        /**
         * This field shall indicate the color, in RGB or RGBA, used for attaching a color to the Zone definition and is
         * a purely informational value to help in uniformly presenting Zones in User Interfaces and may be ignored. The
         * value shall conform to the 6-digit or 8-digit format defined for CSS sRGB hexadecimal color notation. If a
         * 6-digit format is used, then the alpha component shall assume the value of 0 meaning fully transparent
         * interior.
         * Examples:
         * - #00FFFF for R=0x00, G=0xFF, B=0xFF, A absent - For a light-blue zone with full transparency.
         * - #00FFFF80 for R=0x00, G=0xFF, B=0xFF, A=0x80 - For a light-blue zone with partial interior transparency.
         * - #000000FF for R=0x00, G=0x00, B=0x00, A=0xFF - For a Privacy type zone that is black and fully opaque
         * interior.
         */
        public String color; // string

        public TwoDCartesianZoneStruct(String name, ZoneUseEnum use, List<TwoDCartesianVertexStruct> vertices,
                String color) {
            this.name = name;
            this.use = use;
            this.vertices = vertices;
            this.color = color;
        }
    }

    /**
     * This struct is used to encode basic information about a Zone without containing the specifics of how the zone is
     * defined.
     */
    public static class ZoneInformationStruct {
        /**
         * This field shall indicate the unique ZoneID of the Zone.
         */
        public Integer zoneId; // ZoneID
        /**
         * This field shall indicate the zone type which defines the Zone.
         */
        public ZoneTypeEnum zoneType; // ZoneTypeEnum
        /**
         * This field shall indicate the source of the Zone.
         */
        public ZoneSourceEnum zoneSource; // ZoneSourceEnum
        /**
         * This field shall indicate the detailed information for the TwoDCartesianZone.
         */
        public TwoDCartesianZoneStruct twoDCartesianZone; // TwoDCartesianZoneStruct

        public ZoneInformationStruct(Integer zoneId, ZoneTypeEnum zoneType, ZoneSourceEnum zoneSource,
                TwoDCartesianZoneStruct twoDCartesianZone) {
            this.zoneId = zoneId;
            this.zoneType = zoneType;
            this.zoneSource = zoneSource;
            this.twoDCartesianZone = twoDCartesianZone;
        }
    }

    /**
     * This struct is used to encode a set of values for controlling the generation of ZoneTriggered and ZoneStopped
     * events from the Node.
     * Zone events can be triggered due to many underlying reasons, such as a motion sensor on the device, and this is
     * intended to be manufacturer-specific. When a triggering activity is initially detected, the Node shall generate a
     * ZoneTriggered event.
     * This places the Node in a triggered state, at which point the Node shall internally track two values.
     * ### TimeSinceInitialTrigger
     * : The time in seconds since the initial triggering activity.
     * ### TriggerDetectedDuration
     * : Initially set to the InitialDuration value.
     * If the TriggerDetectedDuration value is exceeded by the TimeSinceInitialTrigger, the Node shall generate a
     * ZoneStopped event with the reason parameter set to ActionStopped.
     * However, if additional triggering actions are detected during this period, the Node shall increase the
     * TriggerDetectedDuration value by the AugmentationDuration value. This process can occur repeatedly but after the
     * first increase of TriggerDetectedDuration the Node shall NOT increase the TriggerDetectedDuration value unless
     * the previous TriggerDetectedDuration has been exceeded by the TimeSinceInitialTrigger.
     * If the TimeSinceInitialTrigger value exceeds the MaxDuration value, the Node shall generate a ZoneStopped with
     * the reason parameter set to Timeout.
     * Once a ZoneStopped event has been generated, the Node shall stop detecting the triggering activity for the period
     * of the BlindDuration value.
     */
    public static class ZoneTriggerControlStruct {
        /**
         * This field shall indicate the unique ZoneID of the Zone this Trigger applies to.
         */
        public Integer zoneId; // ZoneID
        /**
         * This field shall indicate the initial duration in seconds after triggering activity is first detected before
         * the Node could generate a ZoneStopped event.
         */
        public Integer initialDuration; // elapsed-s
        /**
         * This field shall indicate the duration in seconds that the TriggerDetectedDuration value is to be extended by
         * if the triggering activity is still detected during this period.
         */
        public Integer augmentationDuration; // elapsed-s
        /**
         * This field shall indicate the maximum duration in seconds after the initial triggering activity detection
         * that additional triggering activity will be detected.
         */
        public Integer maxDuration; // elapsed-s
        /**
         * This field shall indicate the duration in seconds after a ZoneStopped event is generated that the Node shall
         * NOT generate any ZoneTriggered events.
         */
        public Integer blindDuration; // elapsed-s
        /**
         * This field shall indicate the per-zone sensitivity of the underlying zone triggering detection mechanism. The
         * higher the value, the more sensitive the detection. The actual meaning of the values is
         * implementation-specific.
         */
        public Integer sensitivity; // uint8

        public ZoneTriggerControlStruct(Integer zoneId, Integer initialDuration, Integer augmentationDuration,
                Integer maxDuration, Integer blindDuration, Integer sensitivity) {
            this.zoneId = zoneId;
            this.initialDuration = initialDuration;
            this.augmentationDuration = augmentationDuration;
            this.maxDuration = maxDuration;
            this.blindDuration = blindDuration;
            this.sensitivity = sensitivity;
        }
    }

    // Enums
    public enum ZoneTypeEnum implements MatterEnum {
        TWO_DCART_ZONE(0, "Two Dcart Zone");

        private final Integer value;
        private final String label;

        private ZoneTypeEnum(Integer value, String label) {
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

    /**
     * This data type is derived from enum8, and is used to indicate intended Zone usage.
     */
    public enum ZoneUseEnum implements MatterEnum {
        MOTION(0, "Motion"),
        PRIVACY(1, "Privacy"),
        FOCUS(2, "Focus");

        private final Integer value;
        private final String label;

        private ZoneUseEnum(Integer value, String label) {
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

    public enum ZoneSourceEnum implements MatterEnum {
        MFG(0, "Mfg"),
        USER(1, "User");

        private final Integer value;
        private final String label;

        private ZoneSourceEnum(Integer value, String label) {
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

    public enum ZoneEventTriggeredReasonEnum implements MatterEnum {
        MOTION(0, "Motion");

        private final Integer value;
        private final String label;

        private ZoneEventTriggeredReasonEnum(Integer value, String label) {
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

    public enum ZoneEventStoppedReasonEnum implements MatterEnum {
        ACTION_STOPPED(0, "Action Stopped"),
        TIMEOUT(1, "Timeout");

        private final Integer value;
        private final String label;

        private ZoneEventStoppedReasonEnum(Integer value, String label) {
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
         * When this feature is supported, Zones based on a 2 Dimensional Cartesian plane may be defined and shall be
         * represented by a TwoDCartesianZoneStruct. Within a TwoDCartesianZoneStruct the bounding of the zone shall be
         * a polygon defined by a list of vertices comprising X (horizontal) and Y (vertical) coordinates, with each
         * vertex defining the point where adjacent edges meet and an implicit connection between the last and first
         * vertices in the list.
         * The origin (0,0) shall be located at the top left of the Cartesian plane, with positive X and Y values moving
         * right and down across the Cartesian plane respectively.
         */
        public boolean twoDimensionalCartesianZone;
        /**
         * 
         * When this feature is supported, the ZoneTriggerControlStruct shall be used for specifying a zone specific
         * value for the sensitivity of that zone to trigger events. If not supported, only the Sensitivity Attribute
         * shall be used.
         */
        public boolean perZoneSensitivity;
        /**
         * 
         * When this feature is supported, the device allows for creating and managing user defined zones via commands.
         */
        public boolean userDefined;
        /**
         * 
         * When this feature is supported, the device allows for creating and managing user defined Focus Value zones
         * via commands.
         */
        public boolean focusZones;

        public FeatureMap(boolean twoDimensionalCartesianZone, boolean perZoneSensitivity, boolean userDefined,
                boolean focusZones) {
            this.twoDimensionalCartesianZone = twoDimensionalCartesianZone;
            this.perZoneSensitivity = perZoneSensitivity;
            this.userDefined = userDefined;
            this.focusZones = focusZones;
        }
    }

    public ZoneManagementCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 1360, "ZoneManagement");
    }

    protected ZoneManagementCluster(BigInteger nodeId, int endpointId, int clusterId, String clusterName) {
        super(nodeId, endpointId, clusterId, clusterName);
    }

    // commands
    /**
     * This command shall create and store a TwoD Cartesian Zone.
     */
    public static ClusterCommand createTwoDCartesianZone(TwoDCartesianZoneStruct zone) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (zone != null) {
            map.put("zone", zone);
        }
        return new ClusterCommand("createTwoDCartesianZone", map);
    }

    /**
     * The UpdateTwoDCartesianZone shall update a stored TwoD Cartesian Zone.
     */
    public static ClusterCommand updateTwoDCartesianZone(Integer zoneId, TwoDCartesianZoneStruct zone) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (zoneId != null) {
            map.put("zoneId", zoneId);
        }
        if (zone != null) {
            map.put("zone", zone);
        }
        return new ClusterCommand("updateTwoDCartesianZone", map);
    }

    /**
     * This command shall remove the user-defined Zone indicated by ZoneID.
     */
    public static ClusterCommand removeZone(Integer zoneId) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (zoneId != null) {
            map.put("zoneId", zoneId);
        }
        return new ClusterCommand("removeZone", map);
    }

    /**
     * This command is used to create or update a Trigger for the specified motion Zone.
     */
    public static ClusterCommand createOrUpdateTrigger(ZoneTriggerControlStruct trigger) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (trigger != null) {
            map.put("trigger", trigger);
        }
        return new ClusterCommand("createOrUpdateTrigger", map);
    }

    /**
     * This command shall remove the Trigger for the provided ZoneID.
     */
    public static ClusterCommand removeTrigger(Integer zoneId) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (zoneId != null) {
            map.put("zoneId", zoneId);
        }
        return new ClusterCommand("removeTrigger", map);
    }

    @Override
    public @NonNull String toString() {
        String str = "";
        str += "featureMap : " + featureMap + "\n";
        str += "maxUserDefinedZones : " + maxUserDefinedZones + "\n";
        str += "maxZones : " + maxZones + "\n";
        str += "zones : " + zones + "\n";
        str += "triggers : " + triggers + "\n";
        str += "sensitivityMax : " + sensitivityMax + "\n";
        str += "sensitivity : " + sensitivity + "\n";
        str += "twoDCartesianMax : " + twoDCartesianMax + "\n";
        return str;
    }
}
