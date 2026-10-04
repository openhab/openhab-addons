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
 * JointFabricDatastore
 *
 * @author Dan Cunningham - Initial contribution
 */
public class JointFabricDatastoreCluster extends BaseCluster {

    public static final int CLUSTER_ID = 0x0752;
    public static final String CLUSTER_NAME = "JointFabricDatastore";
    public static final String CLUSTER_PREFIX = "jointFabricDatastore";
    public static final String ATTRIBUTE_ANCHOR_ROOT_CA = "anchorRootCa";
    public static final String ATTRIBUTE_ANCHOR_NODE_ID = "anchorNodeId";
    public static final String ATTRIBUTE_ANCHOR_VENDOR_ID = "anchorVendorId";
    public static final String ATTRIBUTE_FRIENDLY_NAME = "friendlyName";
    public static final String ATTRIBUTE_GROUP_KEY_SET_LIST = "groupKeySetList";
    public static final String ATTRIBUTE_GROUP_LIST = "groupList";
    public static final String ATTRIBUTE_NODE_LIST = "nodeList";
    public static final String ATTRIBUTE_ADMIN_LIST = "adminList";
    public static final String ATTRIBUTE_STATUS = "status";
    public static final String ATTRIBUTE_ENDPOINT_GROUP_ID_LIST = "endpointGroupIdList";
    public static final String ATTRIBUTE_ENDPOINT_BINDING_LIST = "endpointBindingList";
    public static final String ATTRIBUTE_NODE_KEY_SET_LIST = "nodeKeySetList";
    public static final String ATTRIBUTE_NODE_ACL_LIST = "nodeAclList";
    public static final String ATTRIBUTE_NODE_ENDPOINT_LIST = "nodeEndpointList";

    /**
     * This shall indicate the Anchor Root CA used to sign all NOC Issuers in the Joint Fabric for the accessing fabric.
     * A null value indicates that the Joint Fabric is not yet formed.
     */
    public OctetString anchorRootCa; // 0 octstr R A
    /**
     * This shall indicate the Node identifier of the Joint Fabric Anchor Root CA for the accessing fabric.
     */
    public BigInteger anchorNodeId; // 1 node-id R A
    /**
     * This shall indicate the Vendor identifier of the Joint Fabric Anchor Root CA for the accessing fabric.
     */
    public Integer anchorVendorId; // 2 vendor-id R A
    /**
     * Friendly name for the accessing fabric.
     */
    public String friendlyName; // 3 string R A
    /**
     * This shall indicate the list of DatastoreGroupKeySetStruct used in the Joint Fabric for the accessing fabric.
     * This attribute shall contain at least one entry, the IPK, which has GroupKeySetID of 0.
     */
    public List<DatastoreGroupKeySetStruct> groupKeySetList; // 4 list R A
    /**
     * This shall indicate the list of groups in the Joint Fabric for the accessing fabric.
     * This list shall include, at a minimum, one group with GroupCAT value set to Administrator CAT and one group with
     * GroupCAT value set to Anchor CAT.
     */
    public List<DatastoreGroupInformationEntryStruct> groupList; // 5 list R A
    /**
     * This shall indicate the list of nodes in the Joint Fabric for the accessing fabric.
     */
    public List<DatastoreNodeInformationEntryStruct> nodeList; // 6 list R A
    /**
     * This shall indicate the list of administrators in the Joint Fabric for the accessing fabric.
     * Only one Administrator may serve as the Anchor Root CA and Anchor Fabric Administrator and shall have index value
     * 0. All other Joint Fabric Administrators shall be referenced at index 1 or greater.
     * An empty list indicates that the Joint Fabric is not yet formed.
     */
    public List<DatastoreAdministratorInformationEntryStruct> adminList; // 7 list R A
    /**
     * This shall indicate the current state of the Joint Fabric Datastore Cluster for the accessing fabric.
     * The value shall be one of the following states:
     * - Committed - indicates the DataStore is ready for use.
     * - Pending - indicates that the DataStore is not yet ready for use.
     * - DeletePending - indicates that the DataStore is in the process of being transferred to another Joint Fabric
     * Anchor Administrator.
     */
    public DatastoreStatusEntryStruct status; // 8 DatastoreStatusEntryStruct R A
    /**
     * This shall indicate the group membership of endpoints in the accessing fabric.
     */
    public List<DatastoreEndpointGroupIDEntryStruct> endpointGroupIdList; // 9 list R A
    /**
     * This shall indicate the binding list for endpoints in the accessing fabric.
     */
    public List<DatastoreEndpointBindingEntryStruct> endpointBindingList; // 10 list R A
    /**
     * This shall indicate the KeySet entries for nodes in the accessing fabric.
     */
    public List<DatastoreNodeKeySetEntryStruct> nodeKeySetList; // 11 list R A
    /**
     * This shall indicate the ACL entries for nodes in the accessing fabric.
     */
    public List<DatastoreACLEntryStruct> nodeAclList; // 12 list R A
    /**
     * This shall indicate the Endpoint entries for nodes in the accessing fabric.
     */
    public List<DatastoreEndpointEntryStruct> nodeEndpointList; // 13 list R A

    // Structs
    public static class DatastoreStatusEntryStruct {
        /**
         * This field shall contain the current state of the target device operation.
         */
        public DatastoreStateEnum state; // DatastoreStateEnum
        /**
         * This field shall contain the timestamp of the last update.
         */
        public Integer updateTimestamp; // epoch-s
        /**
         * This field shall contain the Status Code of the last failed operation where the State field is set to
         * CommitFailure.
         */
        public Status failureCode; // status

        public DatastoreStatusEntryStruct(DatastoreStateEnum state, Integer updateTimestamp, Status failureCode) {
            this.state = state;
            this.updateTimestamp = updateTimestamp;
            this.failureCode = failureCode;
        }
    }

    public static class DatastoreNodeKeySetEntryStruct {
        /**
         * The unique identifier for the node.
         */
        public BigInteger nodeId; // node-id
        public Integer groupKeySetId; // uint16
        /**
         * Indicates whether entry in this list is pending, committed, delete-pending, or commit-failed.
         */
        public DatastoreStatusEntryStruct statusEntry; // DatastoreStatusEntryStruct

        public DatastoreNodeKeySetEntryStruct(BigInteger nodeId, Integer groupKeySetId,
                DatastoreStatusEntryStruct statusEntry) {
            this.nodeId = nodeId;
            this.groupKeySetId = groupKeySetId;
            this.statusEntry = statusEntry;
        }
    }

    public static class DatastoreGroupInformationEntryStruct {
        /**
         * The unique identifier for the group.
         */
        public BigInteger groupId; // uint64
        /**
         * The friendly name for the group.
         */
        public String friendlyName; // string
        /**
         * The unique identifier for the group key set.
         * This value may be null when multicast communication is not used for the group. When GroupPermission is Admin
         * or Manage, this value shall be null.
         * A value of 0 is not allowed since this value is reserved for IPK and the group entry for this value is not
         * managed by the Datastore.
         */
        public Integer groupKeySetId; // uint16
        /**
         * CAT value for this group. This is used for control of individual members of a group (non-broadcast commands).
         * Allowable values include the range 0x0000 to 0xEFFF, and the Administrator CAT and Anchor CAT values.
         * This value may be null when unicast communication is not used for the group.
         */
        public Integer groupCat; // uint16
        /**
         * Current version number for this CAT.
         * This value shall be null when GroupCAT value is null.
         */
        public Integer groupCatVersion; // uint16
        /**
         * The permission level associated with ACL entries for this group. There should be only one Administrator group
         * per fabric, and at most one Manage group per Ecosystem (Vendor Entry).
         */
        public DatastoreAccessControlEntryPrivilegeEnum groupPermission; // DatastoreAccessControlEntryPrivilegeEnum

        public DatastoreGroupInformationEntryStruct(BigInteger groupId, String friendlyName, Integer groupKeySetId,
                Integer groupCat, Integer groupCatVersion, DatastoreAccessControlEntryPrivilegeEnum groupPermission) {
            this.groupId = groupId;
            this.friendlyName = friendlyName;
            this.groupKeySetId = groupKeySetId;
            this.groupCat = groupCat;
            this.groupCatVersion = groupCatVersion;
            this.groupPermission = groupPermission;
        }
    }

    /**
     * The DatastoreBindingTargetStruct represents a Binding on a specific Node (identified by the
     * DatastoreEndpointBindingEntryStruct) which is managed by the Datastore. Only bindings on a specific Node that are
     * fabric-scoped to the Joint Fabric are managed by the Datastore. As a result, references to nodes and groups are
     * specific to the Joint Fabric.
     */
    public static class DatastoreBindingTargetStruct {
        /**
         * This field is the binding's remote target node ID. If the Endpoint field is present, this field shall be
         * present.
         */
        public BigInteger node; // node-id
        /**
         * This field is the binding's target group ID that represents remote endpoints. If the Endpoint field is
         * present, this field shall NOT be present.
         */
        public Integer group; // group-id
        /**
         * This field is the binding's remote endpoint that the local endpoint is bound to. If the Group field is
         * present, this field shall NOT be present.
         */
        public Integer endpoint; // endpoint-no
        /**
         * This field is the binding's cluster ID (client & server) on the local and target endpoint(s). If this field
         * is present, the client cluster shall also exist on this endpoint (with this Binding cluster). If this field
         * is present, the target shall be this cluster on the target endpoint(s).
         */
        public Integer cluster; // cluster-id

        public DatastoreBindingTargetStruct(BigInteger node, Integer group, Integer endpoint, Integer cluster) {
            this.node = node;
            this.group = group;
            this.endpoint = endpoint;
            this.cluster = cluster;
        }
    }

    public static class DatastoreEndpointBindingEntryStruct {
        /**
         * The unique identifier for the node.
         */
        public BigInteger nodeId; // node-id
        /**
         * The unique identifier for the endpoint.
         */
        public Integer endpointId; // endpoint-no
        /**
         * The unique identifier for the entry in the Datastore's EndpointBindingList attribute, which is a list of
         * DatastoreEndpointBindingEntryStruct.
         * This field is used to uniquely identify an entry in the EndpointBindingList attribute for the purpose of
         * deletion (RemoveBindingFromEndpointForNode Command).
         */
        public Integer listId; // uint16
        /**
         * The binding target structure.
         */
        public DatastoreBindingTargetStruct binding; // DatastoreBindingTargetStruct
        /**
         * Indicates whether entry in this list is pending, committed, delete-pending, or commit-failed.
         */
        public DatastoreStatusEntryStruct statusEntry; // DatastoreStatusEntryStruct

        public DatastoreEndpointBindingEntryStruct(BigInteger nodeId, Integer endpointId, Integer listId,
                DatastoreBindingTargetStruct binding, DatastoreStatusEntryStruct statusEntry) {
            this.nodeId = nodeId;
            this.endpointId = endpointId;
            this.listId = listId;
            this.binding = binding;
            this.statusEntry = statusEntry;
        }
    }

    public static class DatastoreEndpointGroupIDEntryStruct {
        /**
         * The unique identifier for the node.
         */
        public BigInteger nodeId; // node-id
        /**
         * The unique identifier for the endpoint.
         */
        public Integer endpointId; // endpoint-no
        /**
         * The unique identifier for the group.
         */
        public Integer groupId; // group-id
        /**
         * Indicates whether entry in this list is pending, committed, delete-pending, or commit-failed.
         */
        public DatastoreStatusEntryStruct statusEntry; // DatastoreStatusEntryStruct

        public DatastoreEndpointGroupIDEntryStruct(BigInteger nodeId, Integer endpointId, Integer groupId,
                DatastoreStatusEntryStruct statusEntry) {
            this.nodeId = nodeId;
            this.endpointId = endpointId;
            this.groupId = groupId;
            this.statusEntry = statusEntry;
        }
    }

    /**
     * The DatastoreEndpointEntryStruct represents an Endpoint on a specific Node which is managed by the Datastore.
     * Only Nodes on the Joint Fabric are managed by the Datastore. As a result, references to NodeID are specific to
     * the Joint Fabric.
     */
    public static class DatastoreEndpointEntryStruct {
        /**
         * The unique identifier for the endpoint.
         */
        public Integer endpointId; // endpoint-no
        /**
         * The unique identifier for the node.
         */
        public BigInteger nodeId; // node-id
        /**
         * This field shall indicate a user-assigned label for this endpoint, as captured by a Joint Fabric
         * Administrator's user interface. By maintaining this value in the Joint Fabric Datastore, all Joint Fabric
         * Administrators can keep these values synchronized. This is particularly useful for complex multi-application
         * endpoint devices (such as appliances) and for endpoints exposed via a Bridge, where individual endpoints
         * might have custom names assigned by the user. For basic devices, only the node-level FriendlyName might be
         * used.
         * Administrators may keep this field in sync with the NodeLabel field from the Basic Information or Bridged
         * Basic Information clusters.
         */
        public String friendlyName; // string

        public DatastoreEndpointEntryStruct(Integer endpointId, BigInteger nodeId, String friendlyName) {
            this.endpointId = endpointId;
            this.nodeId = nodeId;
            this.friendlyName = friendlyName;
        }
    }

    public static class DatastoreAccessControlTargetStruct {
        public Integer cluster; // cluster-id
        public Integer endpoint; // endpoint-no
        public Integer deviceType; // devtype-id

        public DatastoreAccessControlTargetStruct(Integer cluster, Integer endpoint, Integer deviceType) {
            this.cluster = cluster;
            this.endpoint = endpoint;
            this.deviceType = deviceType;
        }
    }

    /**
     * The DatastoreAccessControlEntryStruct represents an ACL on a specific Node (identified by the
     * DatastoreACLEntryStruct) which is managed by the Datastore. Only ACLs on a specific Node that are fabric-scoped
     * to the Joint Fabric are managed by the Datastore. As a result, references to nodes and groups are specific to the
     * Joint Fabric.
     */
    public static class DatastoreAccessControlEntryStruct {
        public DatastoreAccessControlEntryPrivilegeEnum privilege; // DatastoreAccessControlEntryPrivilegeEnum
        public DatastoreAccessControlEntryAuthModeEnum authMode; // DatastoreAccessControlEntryAuthModeEnum
        public List<BigInteger> subjects; // list
        public List<DatastoreAccessControlTargetStruct> targets; // list

        public DatastoreAccessControlEntryStruct(DatastoreAccessControlEntryPrivilegeEnum privilege,
                DatastoreAccessControlEntryAuthModeEnum authMode, List<BigInteger> subjects,
                List<DatastoreAccessControlTargetStruct> targets) {
            this.privilege = privilege;
            this.authMode = authMode;
            this.subjects = subjects;
            this.targets = targets;
        }
    }

    /**
     * The DatastoreACLEntryStruct is a holder for an ACL (DatastoreAccessControlEntryStruct) on a specific Node which
     * is managed by the Datastore. Only ACLs on a specific Node that are fabric-scoped to the Joint Fabric are managed
     * by the Datastore. As a result, references to nodes and groups are specific to the Joint Fabric.
     */
    public static class DatastoreACLEntryStruct {
        /**
         * The unique identifier for the node.
         */
        public BigInteger nodeId; // node-id
        /**
         * The unique identifier for the ACL entry in the Datastore's list of DatastoreACLEntry.
         */
        public Integer listId; // uint16
        /**
         * The Access Control Entry structure.
         */
        public DatastoreAccessControlEntryStruct aclEntry; // DatastoreAccessControlEntryStruct
        /**
         * Indicates whether entry in this list is pending, committed, delete-pending, or commit-failed.
         */
        public DatastoreStatusEntryStruct statusEntry; // DatastoreStatusEntryStruct

        public DatastoreACLEntryStruct(BigInteger nodeId, Integer listId, DatastoreAccessControlEntryStruct aclEntry,
                DatastoreStatusEntryStruct statusEntry) {
            this.nodeId = nodeId;
            this.listId = listId;
            this.aclEntry = aclEntry;
            this.statusEntry = statusEntry;
        }
    }

    public static class DatastoreNodeInformationEntryStruct {
        /**
         * The unique identifier for the node.
         */
        public BigInteger nodeId; // node-id
        /**
         * This field shall contain a user-assigned label for this node, as captured by a Joint Fabric Administrator's
         * user interface. By maintaining this value in the Joint Fabric Datastore, all Joint Fabric Administrators can
         * keep these values synchronized. This value is not propagated to the node itself.
         * Administrators may keep this field in sync with the NodeLabel field from the Basic Information or Bridged
         * Basic Information clusters.
         */
        public String friendlyName; // string
        /**
         * Set to Pending prior to completing commissioning, set to Committed after commissioning complete is
         * successful, or set to CommitFailed if commissioning failed with the FailureCode Field set to the error.
         */
        public DatastoreStatusEntryStruct commissioningStatusEntry; // DatastoreStatusEntryStruct

        public DatastoreNodeInformationEntryStruct(BigInteger nodeId, String friendlyName,
                DatastoreStatusEntryStruct commissioningStatusEntry) {
            this.nodeId = nodeId;
            this.friendlyName = friendlyName;
            this.commissioningStatusEntry = commissioningStatusEntry;
        }
    }

    public static class DatastoreAdministratorInformationEntryStruct {
        /**
         * The unique identifier for the node.
         */
        public BigInteger nodeId; // node-id
        /**
         * Friendly name for this node which is not propagated to nodes.
         */
        public String friendlyName; // string
        /**
         * The Vendor ID for the node.
         */
        public Integer vendorId; // vendor-id
        /**
         * The ICAC used to issue the NOC.
         */
        public OctetString icac; // octstr

        public DatastoreAdministratorInformationEntryStruct(BigInteger nodeId, String friendlyName, Integer vendorId,
                OctetString icac) {
            this.nodeId = nodeId;
            this.friendlyName = friendlyName;
            this.vendorId = vendorId;
            this.icac = icac;
        }
    }

    public static class DatastoreGroupKeySetStruct {
        public Integer groupKeySetId; // uint16
        public DatastoreGroupKeySecurityPolicyEnum groupKeySecurityPolicy; // DatastoreGroupKeySecurityPolicyEnum
        public OctetString epochKey0; // octstr
        public BigInteger epochStartTime0; // epoch-us
        public OctetString epochKey1; // octstr
        public BigInteger epochStartTime1; // epoch-us
        public OctetString epochKey2; // octstr
        public BigInteger epochStartTime2; // epoch-us
        public DatastoreGroupKeyMulticastPolicyEnum groupKeyMulticastPolicy; // DatastoreGroupKeyMulticastPolicyEnum

        public DatastoreGroupKeySetStruct(Integer groupKeySetId,
                DatastoreGroupKeySecurityPolicyEnum groupKeySecurityPolicy, OctetString epochKey0,
                BigInteger epochStartTime0, OctetString epochKey1, BigInteger epochStartTime1, OctetString epochKey2,
                BigInteger epochStartTime2, DatastoreGroupKeyMulticastPolicyEnum groupKeyMulticastPolicy) {
            this.groupKeySetId = groupKeySetId;
            this.groupKeySecurityPolicy = groupKeySecurityPolicy;
            this.epochKey0 = epochKey0;
            this.epochStartTime0 = epochStartTime0;
            this.epochKey1 = epochKey1;
            this.epochStartTime1 = epochStartTime1;
            this.epochKey2 = epochKey2;
            this.epochStartTime2 = epochStartTime2;
            this.groupKeyMulticastPolicy = groupKeyMulticastPolicy;
        }
    }

    // Enums
    public enum DatastoreStateEnum implements MatterEnum {
        PENDING(0, "Pending"),
        COMMITTED(1, "Committed"),
        DELETE_PENDING(2, "Delete Pending"),
        COMMIT_FAILED(3, "Commit Failed");

        private final Integer value;
        private final String label;

        private DatastoreStateEnum(Integer value, String label) {
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

    public enum DatastoreAccessControlEntryPrivilegeEnum implements MatterEnum {
        VIEW(1, "View"),
        OPERATE(3, "Operate"),
        MANAGE(4, "Manage"),
        ADMINISTER(5, "Administer");

        private final Integer value;
        private final String label;

        private DatastoreAccessControlEntryPrivilegeEnum(Integer value, String label) {
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

    public enum DatastoreAccessControlEntryAuthModeEnum implements MatterEnum {
        PASE(1, "Pase"),
        CASE(2, "Case"),
        GROUP(3, "Group");

        private final Integer value;
        private final String label;

        private DatastoreAccessControlEntryAuthModeEnum(Integer value, String label) {
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

    public enum DatastoreGroupKeySecurityPolicyEnum implements MatterEnum {
        TRUST_FIRST(0, "Trust First");

        private final Integer value;
        private final String label;

        private DatastoreGroupKeySecurityPolicyEnum(Integer value, String label) {
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

    public enum DatastoreGroupKeyMulticastPolicyEnum implements MatterEnum {
        PER_GROUP_ID(0, "Per Group Id"),
        ALL_NODES(1, "All Nodes");

        private final Integer value;
        private final String label;

        private DatastoreGroupKeyMulticastPolicyEnum(Integer value, String label) {
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

    public JointFabricDatastoreCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 1874, "JointFabricDatastore");
    }

    protected JointFabricDatastoreCluster(BigInteger nodeId, int endpointId, int clusterId, String clusterName) {
        super(nodeId, endpointId, clusterId, clusterName);
    }

    // commands
    /**
     * Upon receipt, this shall add a KeySet to the Joint Fabric Datastore Cluster of the accessing fabric.
     */
    public static ClusterCommand addKeySet(DatastoreGroupKeySetStruct groupKeySet) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (groupKeySet != null) {
            map.put("groupKeySet", groupKeySet);
        }
        return new ClusterCommand("addKeySet", map);
    }

    /**
     * Upon receipt, this shall update a KeySet in the Joint Fabric Datastore Cluster of the accessing fabric.
     */
    public static ClusterCommand updateKeySet(DatastoreGroupKeySetStruct groupKeySet) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (groupKeySet != null) {
            map.put("groupKeySet", groupKeySet);
        }
        return new ClusterCommand("updateKeySet", map);
    }

    /**
     * Upon receipt, this shall remove a KeySet from the Joint Fabric Datastore Cluster of the accessing fabric.
     */
    public static ClusterCommand removeKeySet(Integer groupKeySetId) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (groupKeySetId != null) {
            map.put("groupKeySetId", groupKeySetId);
        }
        return new ClusterCommand("removeKeySet", map);
    }

    /**
     * Upon receipt, this shall add a group to the Joint Fabric Datastore Cluster of the accessing fabric.
     */
    public static ClusterCommand addGroup(Integer groupId, String friendlyName, Integer groupKeySetId, Integer groupCat,
            Integer groupCatVersion, DatastoreAccessControlEntryPrivilegeEnum groupPermission) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (groupId != null) {
            map.put("groupId", groupId);
        }
        if (friendlyName != null) {
            map.put("friendlyName", friendlyName);
        }
        if (groupKeySetId != null) {
            map.put("groupKeySetId", groupKeySetId);
        }
        if (groupCat != null) {
            map.put("groupCat", groupCat);
        }
        if (groupCatVersion != null) {
            map.put("groupCatVersion", groupCatVersion);
        }
        if (groupPermission != null) {
            map.put("groupPermission", groupPermission);
        }
        return new ClusterCommand("addGroup", map);
    }

    /**
     * Upon receipt, this shall update a group in the Joint Fabric Datastore Cluster of the accessing fabric.
     */
    public static ClusterCommand updateGroup(Integer groupId, String friendlyName, Integer groupKeySetId,
            Integer groupCat, Integer groupCatVersion, DatastoreAccessControlEntryPrivilegeEnum groupPermission) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (groupId != null) {
            map.put("groupId", groupId);
        }
        if (friendlyName != null) {
            map.put("friendlyName", friendlyName);
        }
        if (groupKeySetId != null) {
            map.put("groupKeySetId", groupKeySetId);
        }
        if (groupCat != null) {
            map.put("groupCat", groupCat);
        }
        if (groupCatVersion != null) {
            map.put("groupCatVersion", groupCatVersion);
        }
        if (groupPermission != null) {
            map.put("groupPermission", groupPermission);
        }
        return new ClusterCommand("updateGroup", map);
    }

    /**
     * Upon receipt, this shall remove a group from the Joint Fabric Datastore Cluster of the accessing fabric.
     */
    public static ClusterCommand removeGroup(Integer groupId) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (groupId != null) {
            map.put("groupId", groupId);
        }
        return new ClusterCommand("removeGroup", map);
    }

    /**
     * Upon receipt, this shall add an admin to the Joint Fabric Datastore Cluster of the accessing fabric.
     */
    public static ClusterCommand addAdmin(BigInteger nodeId, String friendlyName, Integer vendorId, OctetString icac) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (nodeId != null) {
            map.put("nodeId", nodeId);
        }
        if (friendlyName != null) {
            map.put("friendlyName", friendlyName);
        }
        if (vendorId != null) {
            map.put("vendorId", vendorId);
        }
        if (icac != null) {
            map.put("icac", icac);
        }
        return new ClusterCommand("addAdmin", map);
    }

    /**
     * Upon receipt, this shall update an admin entry in the AdminList attribute.
     */
    public static ClusterCommand updateAdmin(BigInteger nodeId, String friendlyName, OctetString icac) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (nodeId != null) {
            map.put("nodeId", nodeId);
        }
        if (friendlyName != null) {
            map.put("friendlyName", friendlyName);
        }
        if (icac != null) {
            map.put("icac", icac);
        }
        return new ClusterCommand("updateAdmin", map);
    }

    /**
     * Upon receipt, this shall remove an admin from the Joint Fabric Datastore Cluster of the accessing fabric.
     */
    public static ClusterCommand removeAdmin(BigInteger nodeId) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (nodeId != null) {
            map.put("nodeId", nodeId);
        }
        return new ClusterCommand("removeAdmin", map);
    }

    /**
     * Upon receipt, this shall add a node to the Joint Fabric Datastore Cluster of the accessing fabric.
     */
    public static ClusterCommand addPendingNode(BigInteger nodeId, String friendlyName) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (nodeId != null) {
            map.put("nodeId", nodeId);
        }
        if (friendlyName != null) {
            map.put("friendlyName", friendlyName);
        }
        return new ClusterCommand("addPendingNode", map);
    }

    /**
     * Upon receipt, this shall request that Datastore information relating to a Node of the accessing fabric is
     * refreshed.
     */
    public static ClusterCommand refreshNode(BigInteger nodeId) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (nodeId != null) {
            map.put("nodeId", nodeId);
        }
        return new ClusterCommand("refreshNode", map);
    }

    /**
     * Upon receipt, this shall update the friendly name for a node in the Joint Fabric Datastore Cluster of the
     * accessing fabric.
     */
    public static ClusterCommand updateNode(BigInteger nodeId, String friendlyName) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (nodeId != null) {
            map.put("nodeId", nodeId);
        }
        if (friendlyName != null) {
            map.put("friendlyName", friendlyName);
        }
        return new ClusterCommand("updateNode", map);
    }

    /**
     * Upon receipt, this shall remove a node from the Joint Fabric Datastore Cluster of the accessing fabric.
     */
    public static ClusterCommand removeNode(BigInteger nodeId) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (nodeId != null) {
            map.put("nodeId", nodeId);
        }
        return new ClusterCommand("removeNode", map);
    }

    /**
     * Upon receipt, this shall update the state of an endpoint for a node in the Joint Fabric Datastore Cluster of the
     * accessing fabric.
     */
    public static ClusterCommand updateEndpointForNode(Integer endpointId, BigInteger nodeId, String friendlyName) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (endpointId != null) {
            map.put("endpointId", endpointId);
        }
        if (nodeId != null) {
            map.put("nodeId", nodeId);
        }
        if (friendlyName != null) {
            map.put("friendlyName", friendlyName);
        }
        return new ClusterCommand("updateEndpointForNode", map);
    }

    /**
     * Upon receipt, this shall add a Group ID to an endpoint for a node in the Joint Fabric Datastore Cluster of the
     * accessing fabric.
     */
    public static ClusterCommand addGroupIdToEndpointForNode(BigInteger nodeId, Integer endpointId, Integer groupId) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (nodeId != null) {
            map.put("nodeId", nodeId);
        }
        if (endpointId != null) {
            map.put("endpointId", endpointId);
        }
        if (groupId != null) {
            map.put("groupId", groupId);
        }
        return new ClusterCommand("addGroupIdToEndpointForNode", map);
    }

    /**
     * Upon receipt, this shall remove a Group ID from an endpoint for a node in the Joint Fabric Datastore Cluster of
     * the accessing fabric.
     */
    public static ClusterCommand removeGroupIdFromEndpointForNode(BigInteger nodeId, Integer endpointId,
            Integer groupId) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (nodeId != null) {
            map.put("nodeId", nodeId);
        }
        if (endpointId != null) {
            map.put("endpointId", endpointId);
        }
        if (groupId != null) {
            map.put("groupId", groupId);
        }
        return new ClusterCommand("removeGroupIdFromEndpointForNode", map);
    }

    /**
     * Upon receipt, this shall add a binding to an endpoint for a node in the Joint Fabric Datastore Cluster of the
     * accessing fabric.
     */
    public static ClusterCommand addBindingToEndpointForNode(BigInteger nodeId, Integer endpointId,
            DatastoreBindingTargetStruct binding) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (nodeId != null) {
            map.put("nodeId", nodeId);
        }
        if (endpointId != null) {
            map.put("endpointId", endpointId);
        }
        if (binding != null) {
            map.put("binding", binding);
        }
        return new ClusterCommand("addBindingToEndpointForNode", map);
    }

    /**
     * Upon receipt, this shall remove a binding from an endpoint for a node in the Joint Fabric Datastore Cluster of
     * the accessing fabric.
     */
    public static ClusterCommand removeBindingFromEndpointForNode(Integer listId, Integer endpointId,
            BigInteger nodeId) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (listId != null) {
            map.put("listId", listId);
        }
        if (endpointId != null) {
            map.put("endpointId", endpointId);
        }
        if (nodeId != null) {
            map.put("nodeId", nodeId);
        }
        return new ClusterCommand("removeBindingFromEndpointForNode", map);
    }

    /**
     * Upon receipt, this shall add an ACL to a node in the Joint Fabric Datastore Cluster of the accessing fabric.
     */
    public static ClusterCommand addAclToNode(BigInteger nodeId, DatastoreAccessControlEntryStruct aclEntry) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (nodeId != null) {
            map.put("nodeId", nodeId);
        }
        if (aclEntry != null) {
            map.put("aclEntry", aclEntry);
        }
        return new ClusterCommand("addAclToNode", map);
    }

    /**
     * Upon receipt, this shall remove an ACL from a node in the Joint Fabric Datastore Cluster of the accessing fabric.
     */
    public static ClusterCommand removeAclFromNode(Integer listId, BigInteger nodeId) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (listId != null) {
            map.put("listId", listId);
        }
        if (nodeId != null) {
            map.put("nodeId", nodeId);
        }
        return new ClusterCommand("removeAclFromNode", map);
    }

    @Override
    public @NonNull String toString() {
        String str = "";
        str += "anchorRootCa : " + anchorRootCa + "\n";
        str += "anchorNodeId : " + anchorNodeId + "\n";
        str += "anchorVendorId : " + anchorVendorId + "\n";
        str += "friendlyName : " + friendlyName + "\n";
        str += "groupKeySetList : " + groupKeySetList + "\n";
        str += "groupList : " + groupList + "\n";
        str += "nodeList : " + nodeList + "\n";
        str += "adminList : " + adminList + "\n";
        str += "status : " + status + "\n";
        str += "endpointGroupIdList : " + endpointGroupIdList + "\n";
        str += "endpointBindingList : " + endpointBindingList + "\n";
        str += "nodeKeySetList : " + nodeKeySetList + "\n";
        str += "nodeAclList : " + nodeAclList + "\n";
        str += "nodeEndpointList : " + nodeEndpointList + "\n";
        return str;
    }
}
