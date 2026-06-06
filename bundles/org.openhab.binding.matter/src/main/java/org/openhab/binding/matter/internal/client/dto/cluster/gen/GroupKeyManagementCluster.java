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

    public FeatureMap featureMap; // 65532 FeatureMap
    /**
     * This attribute is a list of GroupKeyMapStruct entries. Each entry associates a logical Group Id with a particular
     * group key set.
     */
    public List<GroupKeyMapStruct> groupKeyMap; // 0 list RW F VM
    /**
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
     * Indicates the maximum number of groups that this node supports per fabric. The value of this attribute shall be
     * set to be no less than the required minimum supported groups as specified in Section 2.11.1.2, "Group Limits".
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
         * This field, if not null, shall be the root credential used in the derivation of an operational group key for
         * epoch slot 0 of the given group key set. If EpochKey0 is not null, EpochStartTime0 shall NOT be null.
         */
        public OctetString epochKey0; // octstr
        /**
         * This field, if not null, shall define when EpochKey0 becomes valid as specified by Section 4.17.3, "Epoch
         * Keys". Units are absolute UTC time in microseconds encoded using the epoch-us representation.
         */
        public BigInteger epochStartTime0; // epoch-us
        /**
         * This field, if not null, shall be the root credential used in the derivation of an operational group key for
         * epoch slot 1 of the given group key set. If EpochKey1 is not null, EpochStartTime1 shall NOT be null.
         */
        public OctetString epochKey1; // octstr
        /**
         * This field, if not null, shall define when EpochKey1 becomes valid as specified by Section 4.17.3, "Epoch
         * Keys". Units are absolute UTC time in microseconds encoded using the epoch-us representation.
         */
        public BigInteger epochStartTime1; // epoch-us
        /**
         * This field, if not null, shall be the root credential used in the derivation of an operational group key for
         * epoch slot 2 of the given group key set. If EpochKey2 is not null, EpochStartTime2 shall NOT be null.
         */
        public OctetString epochKey2; // octstr
        /**
         * This field, if not null, shall define when EpochKey2 becomes valid as specified by Section 4.17.3, "Epoch
         * Keys". Units are absolute UTC time in microseconds encoded using the epoch-us representation.
         */
        public BigInteger epochStartTime2; // epoch-us
        /**
         * This field specifies how the IPv6 Multicast Address shall be formed for groups using this operational group
         * key set.
         * The PerGroupID method maximizes filtering of multicast messages, so that receiving nodes receive only
         * multicast messages for groups to which they are subscribed.
         * The AllNodes method minimizes the number of multicast addresses to which a receiver node needs to subscribe.
         * > [!NOTE]
         * > NOTE: Support for GroupKeyMulticastPolicy is provisional. Correct default behavior is that implied by value
         * PerGroupID.
         */
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
         * The ability to support CacheAndSync security policy and MCSP.
         */
        public boolean cacheAndSync;

        public FeatureMap(boolean cacheAndSync) {
            this.cacheAndSync = cacheAndSync;
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
        return str;
    }
}
