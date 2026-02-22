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
     * Friendly name for the accessing fabric which can be propagated to nodes.
     */
    public String friendlyName; // 3 string R A
    /**
     * This shall indicate the list of DatastoreGroupKeySetStruct used in the Joint Fabric for the accessing fabric.
     * This attribute shall contain at least one entry, the IPK, which has GroupKeySetID of 0.
     */
    public List<DatastoreGroupKeySetStruct> groupKeySetList; // 4 list R A
    /**
     * This shall indicate the list of groups in the Joint Fabric for the accessing fabric.
     * This list must include, at a minimum, one group with GroupCAT value set to Administrator CAT and one group with
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
     * A null value or empty list indicates that the Joint Fabric is not yet formed.
     */
    public List<DatastoreAdministratorInformationEntryStruct> adminList; // 7 list R A
    /**
     * This shall indicate the current state of the Joint Fabric Datastore Cluster for the accessing fabric.
     * The Committed status indicates the DataStore is ready for use. The Pending status indicates that the DataStore is
     * not yet ready for use. The DeletePending status indicates that the DataStore is in the process of being
     * transferred to another Joint Fabric Anchor Administrator.
     */
    public DatastoreStatusEntryStruct status; // 8 DatastoreStatusEntryStruct R A
    /**
     * This shall indicate the group membership of endpoints in the accessing fabric.
     * Any changes to this List (add/remove entry) must follow the pending→committed workflow with current state
     * reflected in the Status Entry.
     */
    public List<DatastoreEndpointGroupIDEntryStruct> endpointGroupIdList; // 9 list R A
    /**
     * This shall indicate the binding list for endpoints in the accessing fabric.
     * Any changes to this List (add/remove entry) must follow the pending→committed workflow with current state
     * reflected in the Status Entry.
     */
    public List<DatastoreEndpointBindingEntryStruct> endpointBindingList; // 10 list R A
    /**
     * This shall indicate the KeySet entries for nodes in the accessing fabric.
     * Any changes to this List (add/remove entry) must follow the pending→committed workflow with current state
     * reflected in the Status Entry.
     */
    public List<DatastoreNodeKeySetEntryStruct> nodeKeySetList; // 11 list R A
    /**
     * This shall indicate the ACL entries for nodes in the accessing fabric.
     * Any changes to this List (add/remove entry) must follow the pending→committed workflow with current state
     * reflected in the Status Entry.
     */
    public List<DatastoreACLEntryStruct> nodeAclList; // 12 list R A
    /**
     * This shall indicate the Endpoint entries for nodes in the accessing fabric.
     * Any changes to this List (add/remove entry) must follow the pending→committed workflow with current state
     * reflected in the Status Entry.
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
         * This field shall contain the StatusCode of the last failed operation where the State field is set to
         * CommitFailure.
         */
        public Integer failureCode; // status

        public DatastoreStatusEntryStruct(DatastoreStateEnum state, Integer updateTimestamp, Integer failureCode) {
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
         * This field is the binding’s remote target node ID. If the Endpoint field is present, this field shall be
         * present.
         */
        public BigInteger node; // node-id
        /**
         * This field is the binding’s target group ID that represents remote endpoints. If the Endpoint field is
         * present, this field shall NOT be present.
         */
        public Integer group; // group-id
        /**
         * This field is the binding’s remote endpoint that the local endpoint is bound to. If the Group field is
         * present, this field shall NOT be present.
         */
        public Integer endpoint; // endpoint-no
        /**
         * This field is the binding’s cluster ID (client &amp; server) on the local and target endpoint(s). If this
         * field is present, the client cluster shall also exist on this endpoint (with this Binding cluster). If this
         * field is present, the target shall be this cluster on the target endpoint(s).
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
         * The unique identifier for the entry in the Datastore’s EndpointBindingList attribute, which is a list of
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
         * Friendly name for this endpoint which is propagated to nodes. Any changes to Friendly Name or Group Id List
         * (add/remove entry) must follow the pending→committed workflow with current state reflected in the Status
         * Entry.
         */
        public String friendlyName; // string
        /**
         * Indicates whether changes to Friendly Name are pending, committed, or commit-failed.
         */
        public DatastoreStatusEntryStruct statusEntry; // DatastoreStatusEntryStruct

        public DatastoreEndpointEntryStruct(Integer endpointId, BigInteger nodeId, String friendlyName,
                DatastoreStatusEntryStruct statusEntry) {
            this.endpointId = endpointId;
            this.nodeId = nodeId;
            this.friendlyName = friendlyName;
            this.statusEntry = statusEntry;
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
         * The unique identifier for the ACL entry in the Datastore’s list of DatastoreACLEntry.
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
         * Friendly name for this node which is not propagated to nodes.
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
        PROXY_VIEW(2, "Proxy View"),
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
     * This command shall be used to add a KeySet to the Joint Fabric Datastore Cluster of the accessing fabric.
     * GroupKeySet represents the KeySet to be added to the Joint Fabric Datastore Cluster. Upon receipt of this
     * command, the Datastore shall:
     * 1. Ensure there are no KeySets in the KeySetList attribute with the given GroupKeySetID.
     * 2. If a match is found, return CONSTRAINT_ERROR.
     * 3. Add the Epoch Key Entry for the KeySet to the KeySetList attribute.
     */
    public static ClusterCommand addKeySet(DatastoreGroupKeySetStruct groupKeySet) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (groupKeySet != null) {
            map.put("groupKeySet", groupKeySet);
        }
        return new ClusterCommand("addKeySet", map);
    }

    /**
     * This command shall be used to update a KeySet in the Joint Fabric Datastore Cluster of the accessing fabric.
     * GroupKeySet represents the KeySet to be updated in the Joint Fabric Datastore Cluster. Upon receipt of this
     * command, the Datastore shall:
     * 1. Find the Epoch Key Entry for the KeySet in the KeySetList attribute with the given GroupKeySetID, and update
     * any changed fields.
     * 2. If entry is not found, return NOT_FOUND.
     * 3. If any fields are changed as a result of this command:
     * a. Iterate through each Node Information Entry:
     * i. If the NodeKeySetList contains an entry with the given GroupKeySetID:
     * A. Update the Status on the given DatastoreNodeKeySetEntryStruct tp Pending.
     * B. Update the GroupKeySet on the given Node with the new values.
     * I. If successful, update the Status on this DatastoreNodeKeySetEntryStruct to Committed.
     * II. If not successful, update the State field of the StatusEntry on this DatastoreNodeKeySetEntryStruct to
     * CommitFailed and FailureCode code to the returned error. The pending change shall be applied in a subsequent Node
     * Refresh.
     */
    public static ClusterCommand updateKeySet(DatastoreGroupKeySetStruct groupKeySet) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (groupKeySet != null) {
            map.put("groupKeySet", groupKeySet);
        }
        return new ClusterCommand("updateKeySet", map);
    }

    /**
     * This command shall be used to remove a KeySet from the Joint Fabric Datastore Cluster of the accessing fabric.
     * GroupKeySetID represents the unique identifier for the KeySet to be removed from the Joint Fabric Datastore
     * Cluster.
     * Attempt to remove the IPK, which has GroupKeySetID of 0, shall fail with response CONSTRAINT_ERROR.
     * Upon receipt of this command, the Datastore shall:
     * 1. If entry is not found, return NOT_FOUND.
     * 2. Ensure there are no Nodes using this KeySet. To do this:
     * a. Iterate through each Node Information Entry:
     * i. If the NodeKeySetList list contains an entry with the given GroupKeySetID, and the entry does NOT have Status
     * DeletePending, then return CONSTRAINT_ERROR.
     * 3. Remove the DatastoreGroupKeySetStruct for the given GroupKeySetID from the GroupKeySetList attribute.
     */
    public static ClusterCommand removeKeySet(Integer groupKeySetId) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (groupKeySetId != null) {
            map.put("groupKeySetId", groupKeySetId);
        }
        return new ClusterCommand("removeKeySet", map);
    }

    /**
     * This command shall be used to add a group to the Joint Fabric Datastore Cluster of the accessing fabric.
     * GroupInformationEntry represents the group to be added to the Joint Fabric Datastore Cluster.
     * GroupCAT values shall fall within the range 1 to 65534. Attempts to add a group with a GroupCAT value of
     * Administrator CAT or Anchor CAT shall fail with CONSTRAINT_ERROR.
     * Upon receipt of this command, the Datastore shall:
     * 1. Ensure there are no Groups in the GroupList attribute with the given GroupID. If a match is found, return
     * CONSTRAINT_ERROR.
     * 2. Add the DatastoreGroupInformationEntryStruct for the Group with the given GroupID to the GroupList attribute.
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
     * This command shall be used to update a group in the Joint Fabric Datastore Cluster of the accessing fabric.
     * GroupID represents the group to be updated in the Joint Fabric Datastore Cluster. NULL values for the additional
     * parameters will be ignored (not updated).
     * GroupCAT values shall fall within the range 1 to 65534. Attempts to update the GroupCAT on an existing group
     * which has a GroupCAT value of Administrator CAT or Anchor CAT shall fail with CONSTRAINT_ERROR.
     * Attempts to set the GroupCAT to Administrator CAT or Anchor CAT shall fail with CONSTRAINT_ERROR.
     * Upon receipt of this command, the Datastore shall:
     * 1. If entry is not found, return NOT_FOUND.
     * 2. Update the DatastoreGroupInformationEntryStruct for the Group with the given GroupID to match the non-NULL
     * fields passed in.
     * 3. If any fields are changed as a result of this command:
     * a. Iterate through each Node Information Entry:
     * i. If the GroupKeySetID changed:
     * I. Add a DatastoreNodeKeySetEntryStruct with the new GroupKeySetID, and Status set to Pending.
     * II. Add this KeySet to the Node.
     * 1. If successful, Set the Status to Committed for this entry in the NodeKeySetList.
     * 2. If not successful, Set the Status to CommitFailed and the FailureCode to the returned error. The pending
     * change shall be applied in a subsequent Node Refresh.
     * A. If the NodeKeySetList list contains an entry with the previous GroupKeySetID:
     * III. Set the Status set to DeletePending.
     * IV. Remove this KeySet from the Node.
     * 1. If successful, Remove this entry from the NodeKeySetList.
     * 2. If not successful, the pending change shall be applied in a subsequent Node Refresh.
     * ii. If the GroupCAT, GroupCATVersion or GroupPermission changed:
     * A. If the ACLList contains an entry for this Group, update the ACL List Entry in the Datastore with the new
     * values and Status Pending, update the ACL attribute on the given Node with the new values. If the update
     * succeeds, set the Status to Committed on the ACLList Entry in the Datastore.
     * iii. If the FriendlyName changed:
     * A. Iterate through each Endpoint Information Entry:
     * I. If the GroupIDList contains an entry with the given GroupID:
     * 1. Update the GroupIDList Entry in the Datastore with the new values and Status
     * ### Pending
     * 2. Update the Groups on the given Node with the new values.
     * 1. If the update succeeds, set the Status to Committed on the GroupIDList Entry in the Datastore.
     * 2. If not successful, the pending change shall be applied in a subsequent Node Refresh.
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
     * This command shall be used to remove a group from the Joint Fabric Datastore Cluster of the accessing fabric.
     * GroupID represents the unique identifier for the group to be removed from the Joint Fabric Datastore Cluster.
     * Attempts to remove a group with GroupCAT value set to Administrator CAT or Anchor CAT shall fail with
     * CONSTRAINT_ERROR.
     * Upon receipt of this command, the Datastore shall:
     * 1. If entry is not found, return NOT_FOUND.
     * 2. Ensure there are no Nodes in this group. To do this:
     * a. Iterate through each Node Information Entry:
     * i. If the GroupIDList contains an entry with the given GroupID, and the entry does NOT have Status DeletePending,
     * then return CONSTRAINT_ERROR.
     * 3. Remove the DatastoreGroupInformationEntryStruct for the Group with the given GroupID from the GroupList
     * attribute.
     */
    public static ClusterCommand removeGroup(Integer groupId) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (groupId != null) {
            map.put("groupId", groupId);
        }
        return new ClusterCommand("removeGroup", map);
    }

    /**
     * This command shall be used to add an admin to the Joint Fabric Datastore Cluster of the accessing fabric.
     * NodeID, FriendlyName, VendorID and ICAC represent the admin to be added to the Joint Fabric Datastore Cluster.
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
     * This command shall be used to update an admin in the Joint Fabric Datastore Cluster of the accessing fabric.
     * NodeID represents the admin to be updated in the Joint Fabric Datastore Cluster. NULL values for the additional
     * parameters will be ignored (not updated).
     * If entry is not found, return NOT_FOUND.
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
     * This command shall be used to remove an admin from the Joint Fabric Datastore Cluster of the accessing fabric.
     * NodeID represents the unique identifier for the admin to be removed from the Joint Fabric Datastore Cluster.
     * If entry is not found, return NOT_FOUND.
     */
    public static ClusterCommand removeAdmin(BigInteger nodeId) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (nodeId != null) {
            map.put("nodeId", nodeId);
        }
        return new ClusterCommand("removeAdmin", map);
    }

    /**
     * The command shall be used to add a node to the Joint Fabric Datastore Cluster of the accessing fabric.
     * NodeID represents the node to be added to the Joint Fabric Datastore Cluster. Upon receipt of this command, the
     * Datastore shall:
     * 1. Update CommissioningStatusEntry of the Node Information Entry with the given NodeID to Pending.
     * If a Node Information Entry exists for the given NodeID, this command shall return INVALID_CONSTRAINT.
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
     * The command shall be used to request that Datastore information relating to a Node of the accessing fabric is
     * refreshed.
     * Upon receipt of this command, the Datastore shall:
     * 1. Confirm that a Node Information Entry exists for the given NodeID, and if not, return NOT_FOUND.
     * 2. Update the CommissioningStatusEntry for the Node Information Entry to Pending.
     * 3. Ensure the Endpoint List for the Node Information Entry with the given NodeID matches Endpoint list on the
     * given Node. This involves the following steps:
     * a. Read the PartsList of the Descriptor cluster from the Node.
     * b. For each Endpoint Information Entry in the Endpoint List of the Node Information Entry that does not match an
     * Endpoint ID in the PartsList, remove the Endpoint Information Entry.
     * c. For each Endpoint Information Entry in the Endpoint List of the Node Information Entry that matches an
     * Endpoint ID in the PartsList:
     * i. Check that each entry in Node’s Group List occurs in the GroupIDList of the Endpoint Information Entry.
     * A. Add any missing entries to the GroupIDList of the Endpoint Information Entry.
     * B. For any entries in the GroupIDList with Status of Pending:
     * I. Add the corresponding change to the Node’s Group List.
     * 1. If successful, mark the Status to Committed.
     * 2. If not successful, update the Status to CommitFailed and the FailureCode to the returned error. The error
     * shall be handled in a subsequent Node Refresh.
     * C. For any entries in the GroupIDList with Status of DeletePending:
     * 1. If successful, remove the corresponding entry from the Node’s Group List.
     * 2. If not successful, update the Status to CommitFailed and the FailureCode to the returned error. The error
     * shall be handled in a subsequent Node Refresh.
     * D. For any entries in the GroupIDList with Status of CommitFailure:
     * I. A CommitFailure with an unrecoverable FailureCode shall be handled by removing the entry from the GroupIDList.
     * II. A CommitFailure with a recoverable FailureCode (i.e. TIMEOUT, BUSY) shall be handle in a subsequent Node
     * Refresh.
     * ii. Check that each entry in Node’s Binding List occurs in the BindingList of the Endpoint Information Entry.
     * A. Add any missing entries to the BindingList of the Endpoint Information Entry.
     * B. For any entries in the BindingList with Status of Pending:
     * I. Add the corresponding change to the Node’s Binding List.
     * 1. If successful, mark the Status to Committed.
     * 2. If not successful, update the Status to CommitFailed and the FailureCode to the returned error. The error
     * shall be handled in a subsequent Node Refresh.
     * C. For any entries in the BindingList with Status of DeletePending:
     * 1. If successful, remove the corresponding entry from the Node’s BindingList.
     * 2. If not successful, update the Status to CommitFailed and the FailureCode to the returned error. The error
     * shall be handled in a subsequent Node Refresh.
     * D. For any entries in the BindingList with Status of CommitFailure:
     * I. A CommitFailure with an unrecoverable FailureCode shall be handled by removing the entry from the BindingList.
     * II. A CommitFailure with a recoverable FailureCode (i.e. TIMEOUT, BUSY) shall be handle in a subsequent Node
     * Refresh.
     * 4. Ensure the GroupKeySetList for the Node Information Entry with the given NodeID matches the Group Keys on the
     * given Node. This involves the following steps:
     * a. Read the Group Keys from the Node.
     * b. For each GroupKeySetEntry in the GroupKeySetList of the Node Information Entry with a Pending Status:
     * i. Add the corresponding DatastoreGroupKeySetStruct to the Node’s Group Key list.
     * A. If successful, mark the Status to Committed.
     * B. If not successful, update the Status to CommitFailed and the FailureCode to the returned error. The error
     * shall be handled in a subsequent Node Refresh.
     * c. For each GroupKeySetEntry in the GroupKeySetList of the Node Information Entry with a CommitFailure Status:
     * i. A CommitFailure with an unrecoverable FailureCode shall be handled by removing the entry from the
     * GroupKeySetList.
     * ii. A CommitFailure with a recoverable FailureCode (i.e. TIMEOUT, BUSY) shall be handle in a subsequent Node
     * Refresh.
     * d. All remaining entries in the GroupKeySetList should be replaced by the remaining entries on the Node.
     * 5. Ensure the ACLList for the Node Information Entry with the given NodeID matches the ACL attribute on the given
     * Node. This involves the following steps:
     * a. Read the ACL attribute on the Node.
     * b. For each DatastoreACLEntryStruct in the ACLList of the Node Information Entry with a Pending Status:
     * i. Add the corresponding DatastoreACLEntryStruct to the Node’s ACL attribute.
     * A. If successful, mark the Status to Committed.
     * B. If not successful, update the Status to CommitFailed and the FailureCode to the returned error. The error
     * shall be handled in a subsequent Node Refresh.
     * c. For each DatastoreACLEntryStruct in the ACLList of the Node Information Entry with a CommitFailure Status:
     * i. A CommitFailure with an unrecoverable FailureCode (i.e. RESOURCE_EXHAUSTED, CONSTRAINT_ERROR) shall be handled
     * by removing the entry from the ACLList.
     * ii. A CommitFailure with a recoverable FailureCode (i.e. TIMEOUT, BUSY) shall be handle in a subsequent Node
     * Refresh.
     * d. All remaining entries in the ACLList should be replaced by the remaining entries on the Node.
     * 6. Update the CommissioningStatusEntry for the Node Information Entry to Committed.
     */
    public static ClusterCommand refreshNode(BigInteger nodeId) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (nodeId != null) {
            map.put("nodeId", nodeId);
        }
        return new ClusterCommand("refreshNode", map);
    }

    /**
     * The command shall be used to update the friendly name for a node in the Joint Fabric Datastore Cluster of the
     * accessing fabric.
     * NodeID represents the node to be updated in the Joint Fabric Datastore Cluster.
     * If a Node Information Entry does not exist for the given NodeID, this command shall return NOT_FOUND.
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
     * This command shall be used to remove a node from the Joint Fabric Datastore Cluster of the accessing fabric.
     * NodeID represents the unique identifier for the node to be removed from the Joint Fabric Datastore Cluster.
     * If a Node Information Entry does not exist for the given NodeID, this command shall return NOT_FOUND.
     */
    public static ClusterCommand removeNode(BigInteger nodeId) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (nodeId != null) {
            map.put("nodeId", nodeId);
        }
        return new ClusterCommand("removeNode", map);
    }

    /**
     * This command shall be used to update the state of an endpoint for a node in the Joint Fabric Datastore Cluster of
     * the accessing fabric.
     * EndpointID represents the unique identifier for the endpoint to be updated in the Joint Fabric Datastore Cluster.
     * NodeID represents the unique identifier for the node to which the endpoint belongs.
     * If an Endpoint Information Entry does not exist for the given NodeID and EndpointID, this command shall return
     * NOT_FOUND.
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
     * This command shall be used to add a Group ID to an endpoint for a node in the Joint Fabric Datastore Cluster of
     * the accessing fabric.
     * GroupID represents the unique identifier for the group to be added to the endpoint.
     * EndpointID represents the unique identifier for the endpoint to be updated in the Joint Fabric Datastore Cluster.
     * NodeID represents the unique identifier for the node to which the endpoint belongs. Upon receipt of this command,
     * the Datastore shall:
     * 1. Confirm that an Endpoint Information Entry exists for the given NodeID and EndpointID, and if not, return
     * NOT_FOUND.
     * 2. Ensure the Group Key List for the Node Information Entry with the given NodeID includes the KeySet for the
     * given Group ID. If it does not:
     * a. Add an entry for the KeySet of the given Group ID to the Group Key List for the Node. The new entry’s status
     * shall be set to Pending.
     * b. Add a Group Key Entry for this KeySet to the given Node ID.
     * i. If this succeeds, update the new KeySet entry in the Datastore to Committed.
     * ii. If not successful, the pending change shall be applied in a subsequent Node Refresh.
     * 3. Ensure the Group List for the Endpoint Information Entry with the given NodeID and EndpointID includes an
     * entry for the given Group. If it does not:
     * a. Add a Group entry for the given Group ID to the Group List for the Endpoint and Node. The new entry’s status
     * shall be set to Pending.
     * b. Add this Group entry to the given Endpoint ID on the given Node ID.
     * i. If this succeeds, update the new Group entry in the Datastore to Committed.
     * ii. If not successful, update the Status to CommitFailed and the FailureCode to the returned error. The error
     * shall be handled in a subsequent Node Refresh.
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
     * This command shall be used to remove a Group ID from an endpoint for a node in the Joint Fabric Datastore Cluster
     * of the accessing fabric.
     * GroupID represents the unique identifier for the group to be removed from the endpoint.
     * EndpointID represents the unique identifier for the endpoint to be updated in the Joint Fabric Datastore Cluster.
     * NodeID represents the unique identifier for the node to which the endpoint belongs. Upon receipt of this command,
     * the Datastore shall:
     * 1. Confirm that an Endpoint Information Entry exists for the given NodeID and EndpointID, and if not, return
     * NOT_FOUND.
     * 2. Ensure the Group List for the Endpoint Information Entry with the given NodeID and EndpointID does not include
     * an entry for the given Group. If it does:
     * a. Update the status to DeletePending of the Group entry for the given Group ID in the Group List.
     * b. Remove this Group entry for the given Endpoint ID on the given Node ID.
     * i. If this succeeds, remove the Group entry for the given Group ID in the Group List for this NodeID and
     * EndpointID in the Datastore.
     * ii. If not successful, the pending change shall be applied in a subsequent Node Refresh.
     * 3. Ensure the Group Key List for the Node Information Entry with the given NodeID does not include the KeySet for
     * the given Group ID. If it does:
     * a. Update the status to DeletePending for the entry for the KeySet of the given Group ID in the Node Group Key
     * List.
     * b. Remove the Group Key Entry for this KeySet from the given Node ID.
     * i. If this succeeds, remove the KeySet entry for the given Node ID.
     * ii. If not successful, update the Status to CommitFailed and the FailureCode to the returned error. The error
     * shall be handled in a subsequent Node Refresh.
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
     * This command shall be used to add a binding to an endpoint for a node in the Joint Fabric Datastore Cluster of
     * the accessing fabric.
     * Binding represents the binding to be added to the endpoint.
     * EndpointID represents the unique identifier for the endpoint to be updated in the Joint Fabric Datastore Cluster.
     * NodeID represents the unique identifier for the node to which the endpoint belongs. Upon receipt of this command,
     * the Datastore shall:
     * 1. Confirm that an Endpoint Information Entry exists for the given NodeID and EndpointID, and if not, return
     * NOT_FOUND.
     * 2. Ensure the Binding List for the Node Information Entry with the given NodeID includes the given Binding. If it
     * does not:
     * a. Add the Binding to the Binding List for the Node Information Entry for the given NodeID. The new entry’s
     * status shall be set to Pending.
     * b. Add this Binding to the given Node ID.
     * i. If this succeeds, update the new Binding in the Datastore to Committed.
     * ii. If not successful, update the Status to CommitFailed and the FailureCode to the returned error. The error
     * shall be handled in a subsequent Node Refresh.
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
     * This command shall be used to remove a binding from an endpoint for a node in the Joint Fabric Datastore Cluster
     * of the accessing fabric.
     * ListID represents the unique identifier for the binding entry in the Datastore’s EndpointBindingList attribute to
     * be removed from the endpoint.
     * EndpointID represents the unique identifier for the endpoint to be updated in the Joint Fabric Datastore Cluster.
     * NodeID represents the unique identifier for the node to which the endpoint belongs. Upon receipt of this command,
     * the Datastore shall:
     * 1. Confirm that an Endpoint Information Entry exists for the given NodeID and EndpointID, and if not, return
     * NOT_FOUND.
     * 2. Ensure the Binding List for the Node Information Entry with the given NodeID does not include an entry with
     * the given ListID. If it does:
     * a. Update the status to DeletePending for the given Binding in the Binding List.
     * b. Remove this Binding from the given Node ID.
     * i. If this succeeds, remove the given Binding from the Binding List.
     * ii. If not successful, update the Status to CommitFailed and the FailureCode to the returned error. The error
     * shall be handled in a subsequent Node Refresh.
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
     * This command shall be used to add an ACL to a node in the Joint Fabric Datastore Cluster of the accessing fabric.
     * NodeID represents the unique identifier for the node to which the ACL is to be added. ACLEntry represents the ACL
     * to be added to the Joint Fabric Datastore Cluster.
     * Upon receipt of this command, the Datastore shall:
     * 1. Confirm that a Node Information Entry exists for the given NodeID, and if not, return NOT_FOUND.
     * 2. Ensure the ACL List for the given NodeID includes the given ACLEntry. If it does not:
     * a. Add the ACLEntry to the ACL List for the given NodeID. The new entry’s status shall be set to Pending.
     * b. Add this ACLEntry to the given Node ID.
     * i. If this succeeds, update the new ACLEntry in the Datastore to Committed.
     * ii. If not successful, update the Status to CommitFailed and the FailureCode to the returned error. The error
     * shall be handled in a subsequent Node Refresh.
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
     * This command shall be used to remove an ACL from a node in the Joint Fabric Datastore Cluster of the accessing
     * fabric.
     * ListID represents the unique identifier for the DatastoreACLEntryStruct to be removed from the Datastore’s list
     * of DatastoreACLEntry.
     * NodeID represents the unique identifier for the node from which the ACL is to be removed. Upon receipt of this
     * command, the Datastore shall:
     * 1. Confirm that a Node Information Entry exists for the given NodeID, and if not, return NOT_FOUND.
     * 2. Ensure the ACL List for the given NodeID does not include the given ACLEntry. If it does:
     * a. Update the status to DeletePending for the given ACLEntry in the ACL List.
     * b. Remove this ACLEntry from the given Node ID.
     * i. If this succeeds, remove the given ACLEntry from the Node ACL List.
     * ii. If not successful, update the Status to CommitFailed and the FailureCode to the returned error. The error
     * shall be handled in a subsequent Node Refresh.
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
