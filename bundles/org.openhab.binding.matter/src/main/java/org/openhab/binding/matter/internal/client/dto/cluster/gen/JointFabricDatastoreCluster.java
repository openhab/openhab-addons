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
import java.util.List;

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
    public static final String ATTRIBUTE_CLUSTER_REVISION = "clusterRevision";
    public static final String ATTRIBUTE_ANCHOR_ROOT_CA = "anchorRootCa";
    public static final String ATTRIBUTE_ANCHOR_NODE_ID = "anchorNodeId";
    public static final String ATTRIBUTE_ANCHOR_VENDOR_ID = "anchorVendorId";
    public static final String ATTRIBUTE_FRIENDLY_NAME = "friendlyName";
    public static final String ATTRIBUTE_GROUP_KEY_SET_LIST = "groupKeySetList";
    public static final String ATTRIBUTE_GROUP_LIST = "groupList";
    public static final String ATTRIBUTE_NODE_LIST = "nodeList";
    public static final String ATTRIBUTE_ADMIN_LIST = "adminList";
    public static final String ATTRIBUTE_STATUS_ENTRY = "statusEntry";

    public Integer clusterRevision; // 65533 ClusterRevision
    /**
     * This shall indicate the Anchor Root CA used to sign all NOC Issuers in the Joint Fabric. A null value indicates
     * that the Joint Fabric is not yet formed.
     */
    public OctetString anchorRootCa; // 0 octstr R S A
    /**
     * This shall indicate the Node identifier of the Joint Fabric Anchor Root CA.
     */
    public BigInteger anchorNodeId; // 1 node-id R S A
    /**
     * This shall indicate the Vendor identifier of the Joint Fabric Anchor Root CA.
     */
    public Integer anchorVendorId; // 2 vendor-id R S A
    /**
     * Friendly name for this fabric which can be propagated to nodes.
     */
    public String friendlyName; // 3 string R S A
    /**
     * This shall indicate the list of GroupKeySetStruct used in the Joint Fabric.
     * This attribute shall contain at least one entry, the IPK, which has GroupKeySetID of 0.
     */
    public List<GroupKeyManagementCluster.GroupKeySetStruct> groupKeySetList; // 4 list R S A
    /**
     * This shall indicate the list of groups in the Joint Fabric.
     */
    public List<DatastoreGroupInformationEntry> groupList; // 5 list R S A
    /**
     * This shall indicate the list of nodes in the Joint Fabric.
     */
    public List<DatastoreNodeInformationEntry> nodeList; // 6 list R S A
    /**
     * This shall indicate the list of administrators in the Joint Fabric.
     * Only one Administrator may serve as the Anchor Root CA and Anchor Fabric Administrator and shall have index value
     * 0. All other Joint Fabric Administrators shall be referenced at index 1 or greater.
     * A null value or empty list indicates that the Joint Fabric is not yet formed.
     */
    public List<DatastoreAdministratorInformationEntry> adminList; // 7 list R S A
    /**
     * This shall indicate the current state of the Joint Fabric Datastore Cluster.
     * The Committed status indicates the DataStore is ready for use. The Pending status indicates that the DataStore is
     * not yet ready for use. The DeletePending status indicates that the DataStore is in the process of being
     * transferred to another Joint Fabric Anchor Administrator.
     */
    public DatastoreAdministratorInformationEntry statusEntry; // 8 DatastoreAdministratorInformationEntry R S A

    // Structs
    public static class DatastoreStatusEntry {
        /**
         * This field shall contain the current state of the target device operation.
         */
        public DatastoreStateEnum state; // DatastoreStateEnum
        /**
         * This field shall contain the timestamp of the last update.
         */
        public Integer updateTimestamp; // epoch-s
        public Integer fabricIndex; // FabricIndex

        public DatastoreStatusEntry(DatastoreStateEnum state, Integer updateTimestamp, Integer fabricIndex) {
            this.state = state;
            this.updateTimestamp = updateTimestamp;
            this.fabricIndex = fabricIndex;
        }
    }

    public static class DatastoreNodeKeyEntry {
        public Integer groupKeySetId; // uint16
        /**
         * Indicates whether entry in this list is pending, committed, or delete-pending.
         */
        public DatastoreStatusEntry statusEntry; // DatastoreStatusEntry
        public Integer fabricIndex; // FabricIndex

        public DatastoreNodeKeyEntry(Integer groupKeySetId, DatastoreStatusEntry statusEntry, Integer fabricIndex) {
            this.groupKeySetId = groupKeySetId;
            this.statusEntry = statusEntry;
            this.fabricIndex = fabricIndex;
        }
    }

    public static class DatastoreGroupInformationEntry {
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
         */
        public Integer groupKeySetId; // uint16
        /**
         * CAT value for this group. This is used for control of individual members of a group (non-broadcast commands).
         */
        public Integer groupCat; // uint16
        /**
         * Current version number for this CAT.
         */
        public Integer groupCatVersion; // uint16
        /**
         * The permission level associated with ACL entries for this group. There should be only one Administrator group
         * per fabric, and at most one Manage group per Ecosystem (Vendor Entry).
         */
        public AccessControlCluster.AccessControlEntryPrivilegeEnum groupPermission; // AccessControl.AccessControlEntryPrivilegeEnum
        public Integer fabricIndex; // FabricIndex

        public DatastoreGroupInformationEntry(BigInteger groupId, String friendlyName, Integer groupKeySetId,
                Integer groupCat, Integer groupCatVersion,
                AccessControlCluster.AccessControlEntryPrivilegeEnum groupPermission, Integer fabricIndex) {
            this.groupId = groupId;
            this.friendlyName = friendlyName;
            this.groupKeySetId = groupKeySetId;
            this.groupCat = groupCat;
            this.groupCatVersion = groupCatVersion;
            this.groupPermission = groupPermission;
            this.fabricIndex = fabricIndex;
        }
    }

    public static class DatastoreBindingEntry {
        /**
         * The unique identifier for the Binding entry in the Datastore’s list of DatastoreBindingEntry.
         */
        public Integer listId; // uint16
        /**
         * The binding target structure.
         */
        public BindingCluster.TargetStruct binding; // Binding.TargetStruct
        /**
         * Indicates whether entry in this list is pending, committed, or delete-pending.
         */
        public DatastoreStatusEntry statusEntry; // DatastoreStatusEntry
        public Integer fabricIndex; // FabricIndex

        public DatastoreBindingEntry(Integer listId, BindingCluster.TargetStruct binding,
                DatastoreStatusEntry statusEntry, Integer fabricIndex) {
            this.listId = listId;
            this.binding = binding;
            this.statusEntry = statusEntry;
            this.fabricIndex = fabricIndex;
        }
    }

    public static class DatastoreGroupIDEntry {
        /**
         * The unique identifier for the group.
         */
        public Integer groupId; // group-id
        /**
         * Indicates whether entry in this list is pending, committed, or delete-pending.
         */
        public DatastoreStatusEntry statusEntry; // DatastoreStatusEntry
        public Integer fabricIndex; // FabricIndex

        public DatastoreGroupIDEntry(Integer groupId, DatastoreStatusEntry statusEntry, Integer fabricIndex) {
            this.groupId = groupId;
            this.statusEntry = statusEntry;
            this.fabricIndex = fabricIndex;
        }
    }

    public static class DatastoreEndpointEntry {
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
         * Indicates whether changes to Friendly Name are pending or committed.
         */
        public DatastoreStatusEntry statusEntry; // DatastoreStatusEntry
        /**
         * List of Group IDs that this endpoint is a member of. Any changes to Group Id List (add/remove entry) must
         * follow the pending→committed workflow with current state reflected in the Status Entry.
         */
        public List<DatastoreGroupIDEntry> groupIdList; // list
        /**
         * List of Binding Targets for this endpoint. Any changes to Binding List (add/remove entry) must follow the
         * pending→committed workflow with current state reflected in the Status Entry.
         */
        public List<DatastoreBindingEntry> bindingList; // list
        public Integer fabricIndex; // FabricIndex

        public DatastoreEndpointEntry(Integer endpointId, BigInteger nodeId, String friendlyName,
                DatastoreStatusEntry statusEntry, List<DatastoreGroupIDEntry> groupIdList,
                List<DatastoreBindingEntry> bindingList, Integer fabricIndex) {
            this.endpointId = endpointId;
            this.nodeId = nodeId;
            this.friendlyName = friendlyName;
            this.statusEntry = statusEntry;
            this.groupIdList = groupIdList;
            this.bindingList = bindingList;
            this.fabricIndex = fabricIndex;
        }
    }

    public static class DatastoreACLEntry {
        /**
         * The unique identifier for the ACL entry in the Datastore’s list of DatastoreACLEntry.
         */
        public Integer listId; // uint16
        /**
         * The Access Control Entry structure.
         */
        public AccessControlCluster.AccessControlEntryStruct aclEntry; // AccessControl.AccessControlEntryStruct
        /**
         * Indicates whether entry in this list is pending, committed, or delete-pending.
         */
        public DatastoreStatusEntry statusEntry; // DatastoreStatusEntry
        public Integer fabricIndex; // FabricIndex

        public DatastoreACLEntry(Integer listId, AccessControlCluster.AccessControlEntryStruct aclEntry,
                DatastoreStatusEntry statusEntry, Integer fabricIndex) {
            this.listId = listId;
            this.aclEntry = aclEntry;
            this.statusEntry = statusEntry;
            this.fabricIndex = fabricIndex;
        }
    }

    public static class DatastoreNodeInformationEntry {
        /**
         * The unique identifier for the node.
         */
        public BigInteger nodeId; // node-id
        /**
         * Friendly name for this node which is not propagated to nodes.
         */
        public String friendlyName; // string
        /**
         * Set to pending prior to completing commissioning, and set to completed after commissioning complete is
         * successful.
         */
        public DatastoreStatusEntry commissioningStatusEntry; // DatastoreStatusEntry
        /**
         * List of Key Set information for the given Node. Updates to the Group Key List must follow the
         * pending→committed workflow with current state reflected in the Status Entry for the corresponding entry in
         * the list.
         */
        public List<DatastoreNodeKeyEntry> nodeKeySetList; // list
        /**
         * List of ACL entries. Group membership for this node is inferred from the ACLs. Client access to a Node
         * Information Entry will be determined from the ACL List. Any changes to ACL List (add/remove entry) must
         * follow the pending→committed workflow with current state reflected in the Status Entry for the corresponding
         * entry in the list.
         */
        public List<DatastoreACLEntry> aclList; // list
        /**
         * The list of endpoints for this node. Any changes to Endpoint List (add/remove entry) must follow the
         * pending→committed workflow with current state reflected in the Status Entry for the corresponding entry in
         * the list.
         */
        public List<DatastoreEndpointEntry> endpointList; // list
        public Integer fabricIndex; // FabricIndex

        public DatastoreNodeInformationEntry(BigInteger nodeId, String friendlyName,
                DatastoreStatusEntry commissioningStatusEntry, List<DatastoreNodeKeyEntry> nodeKeySetList,
                List<DatastoreACLEntry> aclList, List<DatastoreEndpointEntry> endpointList, Integer fabricIndex) {
            this.nodeId = nodeId;
            this.friendlyName = friendlyName;
            this.commissioningStatusEntry = commissioningStatusEntry;
            this.nodeKeySetList = nodeKeySetList;
            this.aclList = aclList;
            this.endpointList = endpointList;
            this.fabricIndex = fabricIndex;
        }
    }

    public static class DatastoreAdministratorInformationEntry {
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
        public Integer fabricIndex; // FabricIndex

        public DatastoreAdministratorInformationEntry(BigInteger nodeId, String friendlyName, Integer vendorId,
                OctetString icac, Integer fabricIndex) {
            this.nodeId = nodeId;
            this.friendlyName = friendlyName;
            this.vendorId = vendorId;
            this.icac = icac;
            this.fabricIndex = fabricIndex;
        }
    }

    // Enums
    public enum DatastoreStateEnum implements MatterEnum {
        PENDING(0, "Pending"),
        COMMITTED(1, "Committed"),
        DELETE_PENDING(2, "Delete Pending");

        public final Integer value;
        public final String label;

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

    public JointFabricDatastoreCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 1874, "JointFabricDatastore");
    }

    protected JointFabricDatastoreCluster(BigInteger nodeId, int endpointId, int clusterId, String clusterName) {
        super(nodeId, endpointId, clusterId, clusterName);
    }

    // commands
    public static ClusterCommand section112471() {
        return new ClusterCommand("section112471");
    }

    public static ClusterCommand section112472() {
        return new ClusterCommand("section112472");
    }

    public static ClusterCommand section112473() {
        return new ClusterCommand("section112473");
    }

    public static ClusterCommand section112474() {
        return new ClusterCommand("section112474");
    }

    public static ClusterCommand section112475() {
        return new ClusterCommand("section112475");
    }

    public static ClusterCommand section112476() {
        return new ClusterCommand("section112476");
    }

    public static ClusterCommand section112477() {
        return new ClusterCommand("section112477");
    }

    public static ClusterCommand section112478() {
        return new ClusterCommand("section112478");
    }

    public static ClusterCommand section112479() {
        return new ClusterCommand("section112479");
    }

    public static ClusterCommand section1124710() {
        return new ClusterCommand("section1124710");
    }

    public static ClusterCommand section1124711() {
        return new ClusterCommand("section1124711");
    }

    public static ClusterCommand section1124712() {
        return new ClusterCommand("section1124712");
    }

    public static ClusterCommand section1124713() {
        return new ClusterCommand("section1124713");
    }

    public static ClusterCommand section1124714() {
        return new ClusterCommand("section1124714");
    }

    public static ClusterCommand section1124715() {
        return new ClusterCommand("section1124715");
    }

    public static ClusterCommand section1124716() {
        return new ClusterCommand("section1124716");
    }

    public static ClusterCommand section1124717() {
        return new ClusterCommand("section1124717");
    }

    public static ClusterCommand section1124718() {
        return new ClusterCommand("section1124718");
    }

    public static ClusterCommand section1124719() {
        return new ClusterCommand("section1124719");
    }

    public static ClusterCommand section1124720() {
        return new ClusterCommand("section1124720");
    }

    @Override
    public @NonNull String toString() {
        String str = "";
        str += "clusterRevision : " + clusterRevision + "\n";
        str += "anchorRootCa : " + anchorRootCa + "\n";
        str += "anchorNodeId : " + anchorNodeId + "\n";
        str += "anchorVendorId : " + anchorVendorId + "\n";
        str += "friendlyName : " + friendlyName + "\n";
        str += "groupKeySetList : " + groupKeySetList + "\n";
        str += "groupList : " + groupList + "\n";
        str += "nodeList : " + nodeList + "\n";
        str += "adminList : " + adminList + "\n";
        str += "statusEntry : " + statusEntry + "\n";
        return str;
    }
}
