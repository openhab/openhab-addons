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
 * ScenesManagement
 *
 * @author Dan Cunningham - Initial contribution
 */
public class ScenesManagementCluster extends BaseCluster {

    public static final int CLUSTER_ID = 0x0062;
    public static final String CLUSTER_NAME = "ScenesManagement";
    public static final String CLUSTER_PREFIX = "scenesManagement";
    public static final String ATTRIBUTE_CLUSTER_REVISION = "clusterRevision";
    public static final String ATTRIBUTE_FEATURE_MAP = "featureMap";
    public static final String ATTRIBUTE_LAST_CONFIGURED_BY = "lastConfiguredBy";
    public static final String ATTRIBUTE_SCENE_TABLE_SIZE = "sceneTableSize";
    public static final String ATTRIBUTE_FABRIC_SCENE_INFO = "fabricSceneInfo";

    public Integer clusterRevision; // 65533 ClusterRevision
    public FeatureMap featureMap; // 65532 FeatureMap
    /**
     * Indicates the Node ID of the node that last configured the Scene Table.
     * The null value indicates that the server has not been configured, or that the identifier of the node that last
     * configured the Scenes Management cluster is not known.
     * The Node ID is scoped to the accessing fabric.
     */
    public BigInteger lastConfiguredBy; // 0 node-id R V
    /**
     * Indicates the number of entries in the Scene Table on this endpoint. This is the total across all fabrics; note
     * that a single fabric cannot use all those entries (see Handling of fabric-scoping). The minimum size of this
     * table, (i.e., the minimum number of scenes to support across all fabrics per endpoint) shall be 16, unless a
     * device type in which this cluster is used, defines a larger value in the device type definition.
     */
    public Integer sceneTableSize; // 1 uint16 R V
    /**
     * Indicates a list of fabric scoped information about scenes on this endpoint.
     * The number of list entries for this attribute shall NOT exceed the number of supported fabrics by the device.
     */
    public List<SceneInfoStruct> fabricSceneInfo; // 2 list R F V

    // Structs
    public static class SceneInfoStruct {
        /**
         * This field shall indicate the number of scenes currently used in the server’s Scene Table on the endpoint
         * where the Scenes Management cluster appears.
         * This only includes the count for the associated fabric.
         */
        public Integer sceneCount; // uint8
        /**
         * This field shall indicate the scene identifier of the scene last invoked on the associated fabric. If no
         * scene has been invoked, the value of this field shall be 0xFF, the undefined scene identifier.
         */
        public Integer currentScene; // uint8
        /**
         * This field shall indicate the group identifier of the scene last invoked on the associated fabric, or 0 if
         * the scene last invoked is not associated with a group.
         */
        public Integer currentGroup; // group-id
        /**
         * This field shall indicate whether the state of the server corresponds to that associated with the
         * CurrentScene and CurrentGroup fields of the SceneInfoStruct they belong to. TRUE indicates that these fields
         * are valid, FALSE indicates that they are not valid.
         * This field shall be set to False for all other fabrics when an attribute with the Scenes (&quot;S&quot;)
         * designation in the Quality column of another cluster present on the same endpoint is modified or when the
         * current scene is modified by a fabric through the RecallScene or StoreScene commands, regardless of the
         * fabric-scoped access quality of the command.
         * In the event where the SceneValid field is set to False for a fabric, the CurrentScene and CurrentGroup
         * fields shall be the last invoked scene and group for that fabric. In the event where no scene was previously
         * invoked for that fabric, the CurrentScene and CurrentGroup fields shall be their default values.
         */
        public Boolean sceneValid; // bool
        /**
         * This field shall indicate the remaining capacity of the Scene Table on this endpoint for the accessing
         * fabric. Note that this value may change between reads, even if no entries are added or deleted on the
         * accessing fabric, due to other clients associated with other fabrics adding or deleting entries that impact
         * the resource usage on the device.
         */
        public Integer remainingCapacity; // uint8
        public Integer fabricIndex; // FabricIndex

        public SceneInfoStruct(Integer sceneCount, Integer currentScene, Integer currentGroup, Boolean sceneValid,
                Integer remainingCapacity, Integer fabricIndex) {
            this.sceneCount = sceneCount;
            this.currentScene = currentScene;
            this.currentGroup = currentGroup;
            this.sceneValid = sceneValid;
            this.remainingCapacity = remainingCapacity;
            this.fabricIndex = fabricIndex;
        }
    }

    /**
     * This data type indicates a combination of an identifier and the value of an attribute.
     */
    public static class AttributeValuePairStruct {
        /**
         * This field shall be present for all instances in a given ExtensionFieldSetStruct.
         * Which Value* field is used shall be determined based on the data type of the attribute indicated by
         * AttributeID, as described in the Value* Fields subsection.
         * The AttributeID field shall NOT refer to an attribute without the Scenes (&quot;S&quot;) designation in the
         * Quality column of the cluster specification.
         * ### 1.4.7.3.2. ValueUnsigned8, ValueSigned8, ValueUnsigned16, ValueSigned16, ValueUnsigned32, ValueSigned32,
         * ValueUnsigned64, ValueSigned64 Fields
         * These fields shall indicate the attribute value as part of an extension field set, associated with a given
         * AttributeID under an ExtensionFieldSetStruct’s ClusterID. Which of the fields is used shall be determined by
         * the type of the attribute indicated by AttributeID as follows:
         * • Data types bool, map8, and uint8 shall map to ValueUnsigned8.
         * • Data types int8 shall map to ValueSigned8.
         * • Data types map16 and uint16 shall map to ValueUnsigned16.
         * • Data types int16 shall map to ValueSigned16.
         * • Data types map32, uint24, and uint32 shall map to ValueUnsigned32.
         * • Data types int24 and int32 shall map to ValueSigned32.
         * • Data types map64, uint40, uint48, uint56 and uint64 shall map to ValueUnsigned64.
         * • Data types int40, int48, int56 and int64 shall map to ValueSigned64.
         * • For derived types, the mapping shall be based on the base type. For example, an attribute of type percent
         * shall be treated as if it were of type uint8, whereas an attribute of type percent100ths shall be treated as
         * if it were of type uint16.
         * • For boolean nullable attributes, any value that is not 0 or 1 shall be considered to have the null value.
         * • For boolean non-nullable attributes, any value that is not 0 or 1 shall be considered to have the value
         * FALSE.
         * • For non-boolean nullable attributes, any value that is not a valid numeric value for the attribute’s type
         * after accounting for range reductions due to being nullable and constraints shall be considered to have the
         * null value for the type.
         * • For non-boolean non-nullable attributes, any value that is not a valid numeric value for the attribute’s
         * type after accounting for constraints shall be considered to be the valid attribute value that is closest to
         * the provided value.
         * ◦ In the event that an invalid provided value is of equal numerical distance to the two closest valid values,
         * the lowest of those values shall be considered the closest valid attribute value.
         * If the used field does not match the data type of the attribute indicated by AttributeID, the
         * AttributeValuePairStruct shall be considered invalid.
         * Examples of processing are:
         * • ColorControl cluster CurrentX (AttributeID 0x0003) has a type of uint16 and is not nullable.
         * ◦ ValueUnsigned16 of 0xAB12 would be used as-is, as it is in range.
         * ◦ ValueUnsigned16 of 0xFF80 is outside of the range allowed for attribute CurrentX, and would be saturated to
         * the closest valid value, which is the maximum of the attribute’s constraint range: 0xFEFF.
         * • LevelControl cluster CurrentLevel (AttributeID 0x0000) has a type of uint8 and is nullable.
         * ◦ ValueUnsigned8 of 0xA1 would be used as-is, as it is in range.
         * ◦ ValueUnsigned8 of 0xFF is outside the range allowed for nullable attribute CurrentLevel, and would be
         * considered as the null value.
         */
        public Integer attributeId; // attrib-id
        public Integer valueUnsigned8; // uint8
        public Integer valueSigned8; // int8
        public Integer valueUnsigned16; // uint16
        public Integer valueSigned16; // int16
        public Integer valueUnsigned32; // uint32
        public Integer valueSigned32; // int32
        public BigInteger valueUnsigned64; // uint64
        public BigInteger valueSigned64; // int64

        public AttributeValuePairStruct(Integer attributeId, Integer valueUnsigned8, Integer valueSigned8,
                Integer valueUnsigned16, Integer valueSigned16, Integer valueUnsigned32, Integer valueSigned32,
                BigInteger valueUnsigned64, BigInteger valueSigned64) {
            this.attributeId = attributeId;
            this.valueUnsigned8 = valueUnsigned8;
            this.valueSigned8 = valueSigned8;
            this.valueUnsigned16 = valueUnsigned16;
            this.valueSigned16 = valueSigned16;
            this.valueUnsigned32 = valueUnsigned32;
            this.valueSigned32 = valueSigned32;
            this.valueUnsigned64 = valueUnsigned64;
            this.valueSigned64 = valueSigned64;
        }
    }

    /**
     * This data type indicates for a given cluster a set of attributes and their values.
     */
    public static class ExtensionFieldSetStruct {
        /**
         * This field shall indicate the cluster-id of the cluster whose attributes are in the AttributeValueList field.
         */
        public Integer clusterId; // cluster-id
        /**
         * This field shall indicate a set of attributes and their values which are stored as part of a scene.
         * Attributes which do not have the Scenes (&quot;S&quot;) designation in the Quality column of their cluster
         * specification shall NOT be used in the AttributeValueList field.
         */
        public List<AttributeValuePairStruct> attributeValueList; // list

        public ExtensionFieldSetStruct(Integer clusterId, List<AttributeValuePairStruct> attributeValueList) {
            this.clusterId = clusterId;
            this.attributeValueList = attributeValueList;
        }
    }

    /**
     * The Scene Table is used to store information for each scene capable of being invoked on the server. Each scene is
     * defined for a particular group. The Scene Table is defined here as a conceptual illustration to assist in
     * understanding the underlying data to be stored when scenes are defined. Though the Scene Table is defined here
     * using the data model architecture rules and format, the design is not normative.
     * The Scene table is logically a list of fabric-scoped structs. The logical fields of each Scene Table entry struct
     * are illustrated below. An ExtensionFieldSetStruct may be present for each Scenes-supporting cluster implemented
     * on the same endpoint.
     */
    public static class LogicalSceneTable {
        /**
         * This field is the group identifier for which this scene applies, or 0 if the scene is not associated with a
         * group.
         */
        public Integer sceneGroupId; // group-id
        /**
         * This field is unique within this group, which is used to identify this scene.
         */
        public Integer sceneId; // uint8
        /**
         * The field is the name of the scene.
         * If scene names are not supported, any commands that write a scene name shall simply discard the name, and any
         * command that returns a scene name shall return an empty string.
         */
        public String sceneName; // string
        /**
         * This field is the amount of time, in milliseconds, it will take for a cluster to change from its current
         * state to the requested state.
         */
        public Integer sceneTransitionTime; // uint32
        /**
         * See the Scene Table Extensions subsections of individual clusters. A Scene Table Extension shall only use
         * attributes with the Scene quality. Each ExtensionFieldSetStruct holds a set of values of these attributes for
         * a cluster implemented on the same endpoint where the Scene (&quot;S&quot;) designation appears in the quality
         * column. A scene is the aggregate of all such fields across all clusters on the endpoint.
         */
        public List<ExtensionFieldSetStruct> extensionFields; // list

        public LogicalSceneTable(Integer sceneGroupId, Integer sceneId, String sceneName, Integer sceneTransitionTime,
                List<ExtensionFieldSetStruct> extensionFields) {
            this.sceneGroupId = sceneGroupId;
            this.sceneId = sceneId;
            this.sceneName = sceneName;
            this.sceneTransitionTime = sceneTransitionTime;
            this.extensionFields = extensionFields;
        }
    }

    // Bitmaps
    public static class CopyModeBitmap {
        public boolean copyAllScenes;

        public CopyModeBitmap(boolean copyAllScenes) {
            this.copyAllScenes = copyAllScenes;
        }
    }

    public static class FeatureMap {
        /**
         * 
         * This feature indicates the ability to store a name for a scene when a scene is added.
         */
        public boolean sceneNames;

        public FeatureMap(boolean sceneNames) {
            this.sceneNames = sceneNames;
        }
    }

    public ScenesManagementCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 98, "ScenesManagement");
    }

    protected ScenesManagementCluster(BigInteger nodeId, int endpointId, int clusterId, String clusterName) {
        super(nodeId, endpointId, clusterId, clusterName);
    }

    // commands
    /**
     * It is not mandatory for an extension field set to be included in the command for every cluster on that endpoint
     * that has a defined extension field set. Extension field sets may be omitted, including the case of no extension
     * field sets at all.
     */
    public static ClusterCommand addScene(Integer groupId, Integer sceneId, Integer transitionTime, String sceneName,
            List<ExtensionFieldSetStruct> extensionFieldSetStructs) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (groupId != null) {
            map.put("groupId", groupId);
        }
        if (sceneId != null) {
            map.put("sceneId", sceneId);
        }
        if (transitionTime != null) {
            map.put("transitionTime", transitionTime);
        }
        if (sceneName != null) {
            map.put("sceneName", sceneName);
        }
        if (extensionFieldSetStructs != null) {
            map.put("extensionFieldSetStructs", extensionFieldSetStructs);
        }
        return new ClusterCommand("addScene", map);
    }

    public static ClusterCommand viewScene(Integer groupId, Integer sceneId) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (groupId != null) {
            map.put("groupId", groupId);
        }
        if (sceneId != null) {
            map.put("sceneId", sceneId);
        }
        return new ClusterCommand("viewScene", map);
    }

    public static ClusterCommand removeScene(Integer groupId, Integer sceneId) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (groupId != null) {
            map.put("groupId", groupId);
        }
        if (sceneId != null) {
            map.put("sceneId", sceneId);
        }
        return new ClusterCommand("removeScene", map);
    }

    public static ClusterCommand removeAllScenes(Integer groupId) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (groupId != null) {
            map.put("groupId", groupId);
        }
        return new ClusterCommand("removeAllScenes", map);
    }

    public static ClusterCommand storeScene(Integer groupId, Integer sceneId) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (groupId != null) {
            map.put("groupId", groupId);
        }
        if (sceneId != null) {
            map.put("sceneId", sceneId);
        }
        return new ClusterCommand("storeScene", map);
    }

    public static ClusterCommand recallScene(Integer groupId, Integer sceneId, Integer transitionTime) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (groupId != null) {
            map.put("groupId", groupId);
        }
        if (sceneId != null) {
            map.put("sceneId", sceneId);
        }
        if (transitionTime != null) {
            map.put("transitionTime", transitionTime);
        }
        return new ClusterCommand("recallScene", map);
    }

    /**
     * This command can be used to get the used scene identifiers within a certain group, for the endpoint that
     * implements this cluster.
     */
    public static ClusterCommand getSceneMembership(Integer groupId) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (groupId != null) {
            map.put("groupId", groupId);
        }
        return new ClusterCommand("getSceneMembership", map);
    }

    /**
     * This command allows a client to efficiently copy scenes from one group/scene identifier pair to another
     * group/scene identifier pair.
     */
    public static ClusterCommand copyScene(CopyModeBitmap mode, Integer groupIdentifierFrom,
            Integer sceneIdentifierFrom, Integer groupIdentifierTo, Integer sceneIdentifierTo) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (mode != null) {
            map.put("mode", mode);
        }
        if (groupIdentifierFrom != null) {
            map.put("groupIdentifierFrom", groupIdentifierFrom);
        }
        if (sceneIdentifierFrom != null) {
            map.put("sceneIdentifierFrom", sceneIdentifierFrom);
        }
        if (groupIdentifierTo != null) {
            map.put("groupIdentifierTo", groupIdentifierTo);
        }
        if (sceneIdentifierTo != null) {
            map.put("sceneIdentifierTo", sceneIdentifierTo);
        }
        return new ClusterCommand("copyScene", map);
    }

    @Override
    public @NonNull String toString() {
        String str = "";
        str += "clusterRevision : " + clusterRevision + "\n";
        str += "featureMap : " + featureMap + "\n";
        str += "lastConfiguredBy : " + lastConfiguredBy + "\n";
        str += "sceneTableSize : " + sceneTableSize + "\n";
        str += "fabricSceneInfo : " + fabricSceneInfo + "\n";
        return str;
    }
}
