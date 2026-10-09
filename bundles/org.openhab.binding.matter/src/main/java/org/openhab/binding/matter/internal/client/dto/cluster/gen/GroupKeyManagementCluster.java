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
 * GroupKeyManagement
 *
 * @author Dan Cunningham - Initial contribution
 */
public class GroupKeyManagementCluster extends BaseCluster {

    public static final int CLUSTER_ID = 0x003F;
    public static final String CLUSTER_NAME = "GroupKeyManagement";
    public static final String CLUSTER_PREFIX = "groupKeyManagement";
    public static final String ATTRIBUTE_FEATURE_MAP = "featureMap";
    public static final String ATTRIBUTE_GROUP_KEY_MAP = "groupKeyMap";
    public static final String ATTRIBUTE_GROUP_TABLE = "groupTable";
    public static final String ATTRIBUTE_MAX_GROUPS_PER_FABRIC = "maxGroupsPerFabric";
    public static final String ATTRIBUTE_MAX_GROUP_KEYS_PER_FABRIC = "maxGroupKeysPerFabric";
    public static final String ATTRIBUTE_GROUPCAST_ADOPTION = "groupcastAdoption";

    public FeatureMap featureMap; // 65532 FeatureMap
    /**
     * If the GCAST feature bit is set in the FeatureMap attribute, the following rules apply to the accessing Fabric:
     * - When Groupcast is adopted (the GroupcastAdoption entry has GroupcastAdopted set to true):
     * - This attribute shall be empty.
     * - Any attempt to write to this attribute shall fail with an INVALID_IN_STATE status code.
     * - Otherwise (Groupcast is not adopted or the entry is missing):
     * - This attribute shall contain the Group Key Set mappings derived from the Groupcast cluster's Membership
     * attribute (one mapping per group per fabric).
     * - GroupKeyMapStruct entry updates shall cause the associated Groupcast cluster's Membership attribute (by
     * GroupID) to be updated with the provided GroupKeySetID. If an entry is missing for a given GroupID in the
     * GroupKeyMap, which exists in the Groupcast cluster's Membership attribute for a given fabric, then the Groupcast
     * cluster's membership attribute shall use placeholder value 65535 for the KeySetID. While this KeySetID is
     * technically valid, administrators SHOULD avoid allocating it for actual usage to avoid value aliasing for this
     * field.
     * This attribute is a list of GroupKeyMapStruct entries. Each entry associates a logical Group Id with a particular
     * group key set.
     */
    public List<GroupKeyMapStruct> groupKeyMap; // 0 list RW F VM
    /**
     * If the GCAST feature is set in the FeatureMap:
     * - If the GroupcastAdoption attribute has an entry for the accessing Fabric and that entry has the
     * GroupcastAdopted field set to true, then this field shall be empty.
     * - Else this attribute shall contain the Group mappings computed in equivalence to the Groupcast cluster's
     * Membership attribute (one mapping per group per fabric).
     * This attribute is a list of GroupInfoMapStruct entries. Each entry provides read-only information about how a
     * given logical Group ID maps to a particular set of endpoints, and a name for the group. The content of this
     * attribute reflects data managed via the Groups cluster (see [[AppClusters]](#ref_AppClusters)), and is in general
     * terms referred to as the 'node-wide Group Table'.
     * The GroupTable shall NOT contain any entry whose GroupInfoMapStruct has an empty Endpoints list. If a RemoveGroup
     * or RemoveAllGroups command causes the removal of a group mapping from its last mapped endpoint, the entire
     * GroupTable entry for that given GroupId shall be removed.
     */
    public List<GroupInfoMapStruct> groupTable; // 1 list R F V
    /**
     * If the Groupcast support is enabled (GCAST feature is set), this shall be set to 0 indicating group management is
     * done using the Groupcast cluster and not the legacy Groups cluster.
     * Indicates the maximum number of legacy groups that this node supports per fabric. For legacy usage, the value of
     * this attribute shall be set to be no less than the required minimum supported groups as specified in Section
     * 2.11.1.2, "Group Limits".
     * The length of the GroupKeyMap and GroupTable list attributes shall NOT exceed the value of the MaxGroupsPerFabric
     * attribute multiplied by the number of supported fabrics.
     */
    public Integer maxGroupsPerFabric; // 2 uint16 R V
    /**
     * Indicates the maximum number of group key sets this node supports per fabric. The value of this attribute shall
     * be set according to the minimum number of group key sets to support as specified in Section 2.11.1.2, "Group
     * Limits".
     */
    public Integer maxGroupKeysPerFabric; // 3 uint16 R V
    /**
     * Indicates whether the accessing fabric claims to have migrated to Groupcast.
     * When a Fabric's entry has the GroupcastAdopted field set to true, the behavior of the GroupKeyMap and GroupTable
     * attributes will change (see description of respective attributes).
     * There shall NOT be more than 1 entry per fabric in this attribute.
     * If a Fabric has not yet written an entry for themselves, the server shall act as if that Fabric had written an
     * entry with GroupcastAdopted set to false, even if not present in the list.
     */
    public List<GroupcastAdoptionStruct> groupcastAdoption; // 4 list RW F A

    // Structs
    public static class GroupKeyMapStruct {
        /**
         * This field uniquely identifies the group within the scope of the given Fabric.
         */
        public Integer groupId; // group-id
        /**
         * This field references the set of group keys that generate operational group keys for use with this group, as
         * specified in Section 4.17.3.5.1, "Group Key Set ID".
         * A GroupKeyMapStruct shall NOT accept GroupKeySetID of 0, which is reserved for the IPK.
         */
        public Integer groupKeySetId; // uint16
        public Integer fabricIndex; // FabricIndex

        public GroupKeyMapStruct(Integer groupId, Integer groupKeySetId, Integer fabricIndex) {
            this.groupId = groupId;
            this.groupKeySetId = groupKeySetId;
            this.fabricIndex = fabricIndex;
        }
    }

    public static class GroupKeySetStruct {
        /**
         * This field shall provide the fabric-unique index for the associated group key set, as specified in Section
         * 4.17.3.5.1, "Group Key Set ID".
         */
        public Integer groupKeySetId; // uint16
        /**
         * This field shall provide the security policy for an operational group key set.
         * When CacheAndSync is not supported in the FeatureMap of this cluster, any action attempting to set
         * CacheAndSync in the GroupKeySecurityPolicy field shall fail with an INVALID_COMMAND error.
         */
        public GroupKeySecurityPolicyEnum groupKeySecurityPolicy; // GroupKeySecurityPolicyEnum
        /**
         * This field, if not null, shall be the InputKey used in the derivation of an OperationalGroupKey for epoch
         * slot 0 of the given group key set. The derived OperationalGroupKey shall be persistently stored for the
         * lifetime of the derived key; however, the InputKey itself shall NOT be stored. If EpochKey0 is not null,
         * EpochStartTime0 shall NOT be null.
         */
        public OctetString epochKey0; // octstr
        /**
         * This field, if not null, shall define when EpochKey0 becomes valid as specified by Section 4.17.3, "Epoch
         * Keys". Units are absolute UTC time in microseconds encoded using the epoch-us representation.
         */
        public BigInteger epochStartTime0; // epoch-us
        /**
         * This field, if not null, shall be the InputKey used in the derivation of an OperationalGroupKey for epoch
         * slot 1 of the given group key set. The derived OperationalGroupKey shall be persistently stored for the
         * lifetime of the derived key; however, the InputKey itself shall NOT be stored. If EpochKey1 is not null,
         * EpochStartTime1 shall NOT be null.
         */
        public OctetString epochKey1; // octstr
        /**
         * This field, if not null, shall define when EpochKey1 becomes valid as specified by Section 4.17.3, "Epoch
         * Keys". Units are absolute UTC time in microseconds encoded using the epoch-us representation.
         */
        public BigInteger epochStartTime1; // epoch-us
        /**
         * If the GCAST feature bit is set in the FeatureMap, this field shall be null, unless the GroupKeySetId is 0
         * (the Identity Protection Key).
         * This field, if not null, shall be the InputKey used in the derivation of an OperationalGroupKey for epoch
         * slot 2 of the given group key set. The derived OperationalGroupKey shall be persistently stored for the
         * lifetime of the derived key; however, the InputKey itself shall NOT be stored. If EpochKey2 is not null,
         * EpochStartTime2 shall NOT be null.
         */
        public OctetString epochKey2; // octstr
        /**
         * If the GCAST feature bit is set in the FeatureMap, this field shall be null, unless the GroupKeySetId is 0
         * (the Identity Protection Key).
         * This field, if not null, shall define when EpochKey2 becomes valid as specified by Section 4.17.3, "Epoch
         * Keys". Units are absolute UTC time in microseconds encoded using the epoch-us representation.
         */
        public BigInteger epochStartTime2; // epoch-us
        public GroupKeyMulticastPolicyEnum groupKeyMulticastPolicy; // GroupKeyMulticastPolicyEnum
        public Integer fabricIndex; // FabricIndex

        public GroupKeySetStruct(Integer groupKeySetId, GroupKeySecurityPolicyEnum groupKeySecurityPolicy,
                OctetString epochKey0, BigInteger epochStartTime0, OctetString epochKey1, BigInteger epochStartTime1,
                OctetString epochKey2, BigInteger epochStartTime2, GroupKeyMulticastPolicyEnum groupKeyMulticastPolicy,
                Integer fabricIndex) {
            this.groupKeySetId = groupKeySetId;
            this.groupKeySecurityPolicy = groupKeySecurityPolicy;
            this.epochKey0 = epochKey0;
            this.epochStartTime0 = epochStartTime0;
            this.epochKey1 = epochKey1;
            this.epochStartTime1 = epochStartTime1;
            this.epochKey2 = epochKey2;
            this.epochStartTime2 = epochStartTime2;
            this.groupKeyMulticastPolicy = groupKeyMulticastPolicy;
            this.fabricIndex = fabricIndex;
        }
    }

    public static class GroupInfoMapStruct {
        /**
         * This field uniquely identifies the group within the scope of the given Fabric.
         */
        public Integer groupId; // group-id
        /**
         * This field provides the list of Endpoint IDs on the Node to which messages to this group shall be forwarded.
         */
        public List<Integer> endpoints; // list
        /**
         * This field provides a name for the group. This field shall contain the last GroupName written for a given
         * GroupId on any Endpoint via the Groups cluster.
         */
        public String groupName; // string
        public Integer fabricIndex; // FabricIndex

        public GroupInfoMapStruct(Integer groupId, List<Integer> endpoints, String groupName, Integer fabricIndex) {
            this.groupId = groupId;
            this.endpoints = endpoints;
            this.groupName = groupName;
            this.fabricIndex = fabricIndex;
        }
    }

    public static class GroupcastAdoptionStruct {
        /**
         * This field shall indicate whether Groupcast was adopted by the associated Fabric's administrators.
         */
        public Boolean groupcastAdopted; // bool
        public Integer fabricIndex; // FabricIndex

        public GroupcastAdoptionStruct(Boolean groupcastAdopted, Integer fabricIndex) {
            this.groupcastAdopted = groupcastAdopted;
            this.fabricIndex = fabricIndex;
        }
    }

    // Enums
    public enum GroupKeySecurityPolicyEnum implements MatterEnum {
        TRUST_FIRST(0, "Trust First"),
        CACHE_AND_SYNC(1, "Cache And Sync");

        private final Integer value;
        private final String label;

        private GroupKeySecurityPolicyEnum(Integer value, String label) {
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

    public enum GroupKeyMulticastPolicyEnum implements MatterEnum {
        PER_GROUP_ID(0, "Per Group Id"),
        ALL_NODES(1, "All Nodes");

        private final Integer value;
        private final String label;

        private GroupKeyMulticastPolicyEnum(Integer value, String label) {
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
         * The CacheAndSync security policy has been provisional since Matter v1.0.
         */
        public boolean cacheAndSync;
        /**
         * 
         * When set, group management and group key mapping is done using the Section 11.27, "Groupcast Cluster".
         * If the Groupcast cluster is present on the Root Node endpoint, then this feature bit shall be set.
         * When this feature map bit is set, this cluster SHOULD be used solely for key management as the Groupcast
         * cluster offers more direct and long-term supported methods of managing group key mapping.
         */
        public boolean groupcast;

        public FeatureMap(boolean cacheAndSync, boolean groupcast) {
            this.cacheAndSync = cacheAndSync;
            this.groupcast = groupcast;
        }
    }

    public GroupKeyManagementCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 63, "GroupKeyManagement");
    }

    protected GroupKeyManagementCluster(BigInteger nodeId, int endpointId, int clusterId, String clusterName) {
        super(nodeId, endpointId, clusterId, clusterName);
    }

    // commands
    /**
     * This command is used by Administrators to set the state of a given Group Key Set, including atomically updating
     * the state of all epoch keys.
     */
    public static ClusterCommand keySetWrite(GroupKeySetStruct groupKeySet) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (groupKeySet != null) {
            map.put("groupKeySet", groupKeySet);
        }
        return new ClusterCommand("keySetWrite", map);
    }

    /**
     * This command is used by Administrators to read the state of a given Group Key Set.
     */
    public static ClusterCommand keySetRead(Integer groupKeySetId) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (groupKeySetId != null) {
            map.put("groupKeySetId", groupKeySetId);
        }
        return new ClusterCommand("keySetRead", map);
    }

    /**
     * This command is used by Administrators to remove all state of a given Group Key Set.
     */
    public static ClusterCommand keySetRemove(Integer groupKeySetId) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (groupKeySetId != null) {
            map.put("groupKeySetId", groupKeySetId);
        }
        return new ClusterCommand("keySetRemove", map);
    }

    /**
     * This command is used by Administrators to query a list of all Group Key Sets associated with the accessing
     * fabric.
     */
    public static ClusterCommand keySetReadAllIndices() {
        return new ClusterCommand("keySetReadAllIndices");
    }

    @Override
    public @NonNull String toString() {
        String str = "";
        str += "featureMap : " + featureMap + "\n";
        str += "groupKeyMap : " + groupKeyMap + "\n";
        str += "groupTable : " + groupTable + "\n";
        str += "maxGroupsPerFabric : " + maxGroupsPerFabric + "\n";
        str += "maxGroupKeysPerFabric : " + maxGroupKeysPerFabric + "\n";
        str += "groupcastAdoption : " + groupcastAdoption + "\n";
        return str;
    }
}
