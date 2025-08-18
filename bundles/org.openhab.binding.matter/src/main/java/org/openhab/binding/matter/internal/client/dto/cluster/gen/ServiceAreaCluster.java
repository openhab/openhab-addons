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
 * ServiceArea
 *
 * @author Dan Cunningham - Initial contribution
 */
public class ServiceAreaCluster extends BaseCluster {

    public static final int CLUSTER_ID = 0x0150;
    public static final String CLUSTER_NAME = "ServiceArea";
    public static final String CLUSTER_PREFIX = "serviceArea";
    public static final String ATTRIBUTE_CLUSTER_REVISION = "clusterRevision";
    public static final String ATTRIBUTE_FEATURE_MAP = "featureMap";
    public static final String ATTRIBUTE_SUPPORTED_AREAS = "supportedAreas";
    public static final String ATTRIBUTE_SUPPORTED_MAPS = "supportedMaps";
    public static final String ATTRIBUTE_SELECTED_AREAS = "selectedAreas";
    public static final String ATTRIBUTE_CURRENT_AREA = "currentArea";
    public static final String ATTRIBUTE_ESTIMATED_END_TIME = "estimatedEndTime";
    public static final String ATTRIBUTE_PROGRESS = "progress";

    public Integer clusterRevision; // 65533 ClusterRevision
    public FeatureMap featureMap; // 65532 FeatureMap
    /**
     * This attribute shall contain the list of areas that can be included in the SelectedAreas attribute’s list. Each
     * item in this list represents a unique area, as indicated by the AreaID field of AreaStruct.
     * Each entry in this list shall have a unique value for the AreaID field.
     * If the SupportedMaps attribute is not empty, each entry in this list shall have a unique value for the
     * combination of the MapID and AreaInfo fields.
     * If the SupportedMaps attribute is empty, each entry in this list shall have a unique value for the AreaInfo field
     * and shall have the MapID field set to null.
     * An empty value indicates that the device is currently unable to provide the list of supported areas.
     * &gt; [!NOTE]
     * &gt; due to the maximum size of this list and to the fact that the entries may include strings (see
     * LocationName), care must be taken by implementers to avoid creating a data structure that is overly large, which
     * can result in significant latency in accessing this attribute.
     * The value of this attribute may change at any time via an out-of-band interaction outside of the server, such as
     * interactions with a user interface, or due to internal device changes.
     * When removing entries in the SupportedAreas attribute list the server shall adjust the values of the
     * SelectedAreas, CurrentArea, and Progress attributes such that they only reference valid entries in the updated
     * SupportedAreas attribute list. These changes to the SelectedAreas, CurrentArea, and Progress attributes may
     * result in the server setting some or all of them to empty (for SelectedAreas and Progress) or null (for
     * CurrentArea), or updating them with data that matches the constraints from the description of the respective
     * attributes. These actions are required to ensure having a consistent representation of the maps and locations
     * available to the clients.
     * The SupportedAreas attribute list changes mentioned above SHOULD NOT be allowed while the device is operating, to
     * reduce the impact on the clients, and the potential confusion for the users.
     * A few examples are provided below. Valid list of areas:
     * • AreaID&#x3D;0, LocationName&#x3D;&quot;yellow bedroom&quot;, MapID&#x3D;null
     * • AreaID&#x3D;1, LocationName&#x3D;&quot;orange bedroom&quot;, MapID&#x3D;null Valid list of areas:
     * • AreaID&#x3D;5, LocationName&#x3D;&quot;hallway&quot;, MapID&#x3D;1
     * • AreaID&#x3D;3, LocationName&#x3D;&quot;hallway&quot;, MapID&#x3D;2
     */
    public List<AreaStruct> supportedAreas; // 0 list R V
    /**
     * This attribute shall contain the list of supported maps.
     * A map is a full or a partial representation of a home, known to the device. For example:
     * • a single level home may be represented using a single map
     * • a two level home may be represented using two maps, one for each level
     * • a single level home may be represented using two maps, each including a different set of rooms, such as
     * &quot;map of living room and kitchen&quot; and &quot;map of bedrooms and hallway&quot;
     * • a single level home may be represented using one map for the indoor areas (living room, bedrooms etc.) and one
     * for the outdoor areas (garden, swimming pool etc.)
     * Each map includes one or more areas - see the SupportedAreas attribute. In the context of this cluster
     * specification, a map is effectively a group label for a set of areas, rather than a graphical representation that
     * the clients can display to the users. The clients that present the list of available areas for user selection
     * (see the SelectAreas command) may choose to filter the SupportedAreas list based on the associated map. For
     * example, the clients may allow the user to indicate that the device is to operate on the first floor, and allow
     * the user to choose only from the areas situated on that level.
     * If empty, that indicates that the device is currently unable to provide this information. Each entry in this list
     * shall have a unique value for the MapID field.
     * Each entry in this list shall have a unique value for the Name field.
     * &gt; [!NOTE]
     * &gt; due to the maximum size of this list and to the fact that the entries may include strings (see the Name
     * field of the MapStruct data type), care must be taken by implementers to avoid creating a data structure that is
     * overly large, which can result in significant latency in accessing this attribute.
     * The value of this attribute may change at any time via an out-of-band interaction outside of the server, such as
     * interactions with a user interface.
     * When updating the SupportedMaps attribute list by deleting entries, or by setting the attribute to an empty list,
     * the SupportedLocations attribute shall be updated such that all entries in that list meet the constraints
     * indicated in the description of the SupportedLocations attribute. This may result in the server removing entries
     * from the SupportedAreas attribute list. See the SupportedAreas attribute description for the implications of
     * changing that attribute.
     * The SupportedMaps attribute list changes mentioned above SHOULD NOT be allowed while the device is operating, to
     * reduce the impact on the clients, and the potential confusion for the users.
     */
    public List<MapStruct> supportedMaps; // 1 list R V
    /**
     * Indicates the set of areas where the device SHOULD attempt to operate.
     * The mobile devices may travel without operating across any areas while attempting to reach the areas indicated by
     * the SelectedAreas attribute. For example, a robotic vacuum cleaner may drive without cleaning when traveling
     * without operating.
     * If this attribute is empty, the device is not constrained to operate in any specific areas. If this attribute is
     * not empty:
     * • each item in this list shall match the AreaID field of an entry in the SupportedAreas attribute’s list
     * • each entry in this list shall have a unique value
     */
    public List<Integer> selectedAreas; // 2 list R V
    /**
     * If the device is mobile, this attribute shall indicate the area where the device is currently located, regardless
     * of whether it is operating or not, such as while traveling between areas.
     * If the device is not mobile and can operate at multiple areas sequentially, this attribute shall indicate the
     * area which is currently being serviced, or the area which is currently traversed by the device. For example, a
     * camera device may use this attribute to indicate which area it currently takes video of (serviced area) or which
     * area it currently has in view but not taking video of (e.g. an area which is traversed while panning).
     * &gt; [!NOTE]
     * &gt; A device may traverse an area regardless of the status of the area (pending, skipped, or completed).
     * If a device can simultaneously operate at multiple areas, such as in the case of a sensor that can monitor
     * multiple areas at the same time, the CurrentArea attribute shall NOT be implemented, since it doesn’t apply. Else
     * this attribute shall be optionally implemented.
     * A null value indicates that the device is currently unable to provide this information. For example, the device
     * is traversing an unknown area, or the SupportedAreas attribute was updated and the area where the device is
     * located was removed from that list.
     * If not null, the value of this attribute shall match the AreaID field of an entry on the SupportedAreas
     * attribute’s list.
     */
    public Integer currentArea; // 3 uint32 R V
    /**
     * Indicates the estimated Epoch time for completing operating at the area indicated by the CurrentArea attribute,
     * in seconds.
     * A value of 0 means that the operation has completed.
     * When this attribute is null, that represents that there is no time currently defined until operation completion.
     * This may happen, for example, because no operation is in progress or because the completion time is unknown.
     * Null if the CurrentArea attribute is null.
     * If the Progress attribute is available, and it contains an entry matching CurrentArea, the server may use the
     * time estimate provided in the InitialTimeEstimate field of that entry to compute the EstimatedEndTime attribute.
     * The value of this attribute shall only be reported in the following cases:
     * • when it changes to or from 0
     * • when it decreases
     * • when it changes to or from null
     * &gt; [!NOTE]
     * &gt; If the device is capable of pausing its operation, this attribute may be set to null, to indicate that
     * completion time is unknown, or increment the value while being in the paused state.
     */
    public Integer estimatedEndTime; // 4 epoch-s R V
    /**
     * Indicates the operating status at one or more areas. Each entry in this list shall have a unique value for the
     * AreaID field.
     * For each entry in this list, the AreaID field shall match an entry on the SupportedAreas attribute’s list.
     * When this attribute is empty, that represents that no progress information is currently available.
     * If the SelectedAreas attribute is empty, indicating the device is not constrained to operate in any specific
     * areas, the Progress attribute list may change while the device operates, due to the device adding new entries
     * dynamically, when it determines which ones it can attempt to operate at.
     * If the SelectedAreas attribute is not empty, and the device starts operating:
     * • the Progress attribute list shall be updated so each entry of SelectedAreas has a matching Progress list entry,
     * based on the AreaID field
     * • the length of the Progress and SelectedAreas list shall be the same
     * • the entries in the Progress list shall be initialized by the server, by having their status set to Pending or
     * Operating, and the TotalOperationalTime field set to null
     * When the device ends operation unexpectedly, such as due to an error, the server shall update all Progress list
     * entries with the Status field set to Operating or Pending to Skipped.
     * When the device finishes operating, successfully or not, it shall NOT change the Progress attribute, except in
     * the case of an unexpected end of operation as described above, or due to changes to the SupportedMaps or
     * SupportedAreas attributes, so the clients can retrieve the progress information at that time.
     * &gt; [!NOTE]
     * &gt; if the device implements the Operational Status cluster, or a derivation of it, in case the device fails to
     * service any locations in the SelectedAreas list before ending the operation, it SHOULD use the Operational Status
     * cluster to indicate that the device was unable to complete the operation (see the UnableToCompleteOperation error
     * from that cluster specification). The clients SHOULD then read the Progress attribute, and indicate which areas
     * have been successfully serviced (marked as completed).
     */
    public List<ProgressStruct> progress; // 5 list R V

    // Structs
    /**
     * The data from this structure indicates a landmark and position relative to the landmark.
     */
    public static class LandmarkInfoStruct {
        /**
         * This field shall indicate that the area is associated with a landmark.
         * This field shall be the ID of a landmark semantic tag, located within the Common Landmark Namespace. For
         * example, this tag may indicate that the area refers to an area next to a table.
         */
        public Integer landmarkTag; // tag
        /**
         * This field shall identify the position of the area relative to a landmark. This is a static description of a
         * zone known to the server, and this field never reflects the device’s own proximity or position relative to
         * the landmark, but that of the zone.
         * This field shall be the ID of a relative position semantic tag, located within the Common Relative Position
         * Namespace.
         * If the RelativePositionTag field is null, this field indicates proximity to the landmark. Otherwise, the
         * RelativePositionTag field indicates the position of the area relative to the landmark indicated by the
         * LandmarkTag field. For example, this tag, in conjunction with the LandmarkTag field, may indicate that the
         * area refers to a zone under a table.
         */
        public Integer relativePositionTag; // tag

        public LandmarkInfoStruct(Integer landmarkTag, Integer relativePositionTag) {
            this.landmarkTag = landmarkTag;
            this.relativePositionTag = relativePositionTag;
        }
    }

    /**
     * The data from this structure indicates the name and/or semantic data describing an area, as detailed below.
     * This data type includes the LocationInfo field, with the following fields: LocationName, FloorNumber, AreaType.
     * Additional semantic data may be available in the LandmarkInfo field.
     * For an area description to be meaningful, it shall have at least one of the following:
     * • a non-empty name (LocationInfo’s LocationName field) OR
     * • some semantic data (one or more of these: FloorNumber, AreaType or LandmarkTag) The normative text from the
     * remainder of this section describes these constraints.
     * If the LocationInfo field is null, the LandmarkInfo field shall NOT be null. If the LandmarkInfo field is null,
     * the LocationInfo field shall NOT be null.
     * If LocationInfo is not null, and its LocationName field is an empty string, at least one of the following shall
     * NOT be null:
     * • LocationInfo’s FloorNumber field
     * • LocationInfo’s AreaType field
     * • LandmarkInfo field
     * If all three of the following are null, LocationInfo’s LocationName field shall NOT be an empty string:
     * • LocationInfo’s FloorNumber field
     * • LocationInfo’s AreaType field
     * • LandmarkInfo field
     */
    public static class AreaInfoStruct {
        /**
         * This field shall indicate the name of the area, floor number and/or area type. A few examples are provided
         * below.
         * • An area can have LocationInfo’s LocationName field set to &quot;blue room&quot;, and the AreaType field set
         * to the ID of a &quot;Living Room&quot; semantic tag. Clients wishing to direct the device to operate in (or
         * service) the living room can use this area.
         * • An area can have LocationInfo set to null, the LandmarkInfo’s LandmarkTag field set to the ID of the
         * &quot;Table&quot; landmark semantic tag, and the RelativePositionTag field set to the ID of the
         * &quot;Under&quot; position semantic tag. With such an area indication, the client can request the device to
         * operate in (or service) the area located under the table.
         */
        public Locationdesc locationInfo; // locationdesc
        /**
         * This field shall indicate an association with a landmark. A value of null indicates that the information is
         * not available or known. For example, this may indicate that the area refers to a zone next to a table.
         * If this field is not null, that indicates that the area is restricted to the zone where the landmark is
         * located, as indicated by the LandmarkTag and, if not null, by the RelativePositionTag fields, rather than to
         * the entire room or floor where the landmark is located, if those are indicated by the LocationInfo field.
         */
        public LandmarkInfoStruct landmarkInfo; // LandmarkInfoStruct

        public AreaInfoStruct(Locationdesc locationInfo, LandmarkInfoStruct landmarkInfo) {
            this.locationInfo = locationInfo;
            this.landmarkInfo = landmarkInfo;
        }
    }

    /**
     * This is a struct representing a map.
     */
    public static class MapStruct {
        /**
         * This field shall represent the map’s identifier.
         */
        public Integer mapId; // uint32
        /**
         * This field shall represent a human understandable map description. For example: &quot;Main Floor&quot;, or
         * &quot;Second Level&quot;.
         */
        public String name; // string

        public MapStruct(Integer mapId, String name) {
            this.mapId = mapId;
            this.name = name;
        }
    }

    /**
     * This is a struct representing an area known to the server.
     */
    public static class AreaStruct {
        /**
         * This field shall represent the identifier of the area.
         */
        public Integer areaId; // uint32
        /**
         * This field shall indicate the map identifier which the area is associated with. A value of null indicates
         * that the area is not associated with a map.
         * If the SupportedMaps attribute is not empty, this field shall match the MapID field of an entry from the
         * SupportedMaps attribute’s list. If the SupportedMaps attribute is empty, this field shall be null.
         */
        public Integer mapId; // uint32
        /**
         * This field shall contain data describing the area.
         * This SHOULD be used by clients to determine the name and/or the full, or the partial, semantics of a certain
         * area.
         * &gt; [!NOTE]
         * &gt; If any entries on the SupportedAreas attribute’s list have the AreaInfo field missing the semantic data,
         * the client may remind the user to assign the respective data.
         */
        public AreaInfoStruct areaInfo; // AreaInfoStruct

        public AreaStruct(Integer areaId, Integer mapId, AreaInfoStruct areaInfo) {
            this.areaId = areaId;
            this.mapId = mapId;
            this.areaInfo = areaInfo;
        }
    }

    /**
     * This is a struct indicating the progress.
     */
    public static class ProgressStruct {
        /**
         * This field shall indicate the identifier of the area, and the identifier shall be an entry in the
         * SupportedAreas attribute’s list.
         */
        public Integer areaId; // uint32
        /**
         * This field shall indicate the operational status of the device regarding the area indicated by the AreaID
         * field.
         */
        public OperationalStatusEnum status; // OperationalStatusEnum
        /**
         * This field shall indicate the total operational time, in seconds, from when the device started to operate at
         * the area indicated by the AreaID field, until the operation finished, due to completion or due to skipping,
         * including any time spent while paused.
         * A value of null indicates that the total operational time is unknown.
         * There may be cases where the total operational time exceeds the maximum value that can be conveyed by this
         * attribute, and in such instances this attribute shall be populated with null. Null if the Status field is not
         * set to Completed or Skipped.
         */
        public Integer totalOperationalTime; // elapsed-s
        /**
         * This field shall indicate the estimated time for the operation, in seconds, from when the device will start
         * operating at the area indicated by the AreaID field, until the operation completes, excluding any time spent
         * while not operating in the area.
         * A value of null indicates that the estimated time is unknown. If the estimated time is unknown, or if it
         * exceeds the maximum value that can be conveyed by this attribute, this attribute shall be null.
         * After initializing the ProgressStruct instance, the server SHOULD NOT change the value of this field, except
         * when repopulating the entire instance, to avoid excessive reporting of the Progress attribute changes.
         */
        public Integer estimatedTime; // elapsed-s

        public ProgressStruct(Integer areaId, OperationalStatusEnum status, Integer totalOperationalTime,
                Integer estimatedTime) {
            this.areaId = areaId;
            this.status = status;
            this.totalOperationalTime = totalOperationalTime;
            this.estimatedTime = estimatedTime;
        }
    }

    // Enums
    /**
     * The following table defines the status values.
     */
    public enum OperationalStatusEnum implements MatterEnum {
        PENDING(0, "Pending"),
        OPERATING(1, "Operating"),
        SKIPPED(2, "Skipped"),
        COMPLETED(3, "Completed");

        public final Integer value;
        public final String label;

        private OperationalStatusEnum(Integer value, String label) {
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

    public enum SelectAreasStatus implements MatterEnum {
        SUCCESS(0, "Success"),
        UNSUPPORTED_AREA(1, "Unsupported Area"),
        INVALID_IN_MODE(2, "Invalid In Mode"),
        INVALID_SET(3, "Invalid Set");

        public final Integer value;
        public final String label;

        private SelectAreasStatus(Integer value, String label) {
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

    public enum SkipAreaStatus implements MatterEnum {
        SUCCESS(0, "Success"),
        INVALID_AREA_LIST(1, "Invalid Area List"),
        INVALID_IN_MODE(2, "Invalid In Mode"),
        INVALID_SKIPPED_AREA(3, "Invalid Skipped Area");

        public final Integer value;
        public final String label;

        private SkipAreaStatus(Integer value, String label) {
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
         * This feature indicates whether this device allows changing the selected areas, by using the SelectAreas
         * command, while operating.
         */
        public boolean selectWhileRunning;
        /**
         * 
         * The device implements the progress reporting feature
         */
        public boolean progressReporting;
        /**
         * 
         * The device has map support
         */
        public boolean maps;

        public FeatureMap(boolean selectWhileRunning, boolean progressReporting, boolean maps) {
            this.selectWhileRunning = selectWhileRunning;
            this.progressReporting = progressReporting;
            this.maps = maps;
        }
    }

    public ServiceAreaCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 336, "ServiceArea");
    }

    protected ServiceAreaCluster(BigInteger nodeId, int endpointId, int clusterId, String clusterName) {
        super(nodeId, endpointId, clusterId, clusterName);
    }

    // commands
    /**
     * This command is used to select a set of device areas, where the device is to operate.
     * On receipt of this command the device shall respond with a SelectAreasResponse command.
     */
    public static ClusterCommand selectAreas(List<Integer> newAreas) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (newAreas != null) {
            map.put("newAreas", newAreas);
        }
        return new ClusterCommand("selectAreas", map);
    }

    /**
     * This command is used to skip the given area, and to attempt operating at other areas on the SupportedAreas
     * attribute list.
     * This command shall NOT be implemented if the CurrentArea attribute and the Progress attribute are both not
     * implemented. Else, this command shall be optionally implemented.
     * On receipt of this command the device shall respond with a SkipAreaResponse command.
     */
    public static ClusterCommand skipArea(Integer skippedArea) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (skippedArea != null) {
            map.put("skippedArea", skippedArea);
        }
        return new ClusterCommand("skipArea", map);
    }

    @Override
    public @NonNull String toString() {
        String str = "";
        str += "clusterRevision : " + clusterRevision + "\n";
        str += "featureMap : " + featureMap + "\n";
        str += "supportedAreas : " + supportedAreas + "\n";
        str += "supportedMaps : " + supportedMaps + "\n";
        str += "selectedAreas : " + selectedAreas + "\n";
        str += "currentArea : " + currentArea + "\n";
        str += "estimatedEndTime : " + estimatedEndTime + "\n";
        str += "progress : " + progress + "\n";
        return str;
    }
}
