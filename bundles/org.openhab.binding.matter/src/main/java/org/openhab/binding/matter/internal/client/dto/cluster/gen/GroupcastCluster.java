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
 * Groupcast
 *
 * @author Dan Cunningham - Initial contribution
 */
public class GroupcastCluster extends BaseCluster {

    public static final int CLUSTER_ID = 0x0065;
    public static final String CLUSTER_NAME = "Groupcast";
    public static final String CLUSTER_PREFIX = "groupcast";
    public static final String ATTRIBUTE_FEATURE_MAP = "featureMap";
    public static final String ATTRIBUTE_MEMBERSHIP = "membership";
    public static final String ATTRIBUTE_MAX_MEMBERSHIP_COUNT = "maxMembershipCount";
    public static final String ATTRIBUTE_MAX_MCAST_ADDR_COUNT = "maxMcastAddrCount";
    public static final String ATTRIBUTE_USED_MCAST_ADDR_COUNT = "usedMcastAddrCount";
    public static final String ATTRIBUTE_FABRIC_UNDER_TEST = "fabricUnderTest";

    public FeatureMap featureMap; // 65532 FeatureMap
    /**
     * Indicates the list of groups memberships currently active on the node.
     * Each entry specifies the group ID and the list of the endpoints that participate in that group, as well as policy
     * for that group.
     * For any single fabric, the server shall limit the total number of GroupIDs used across all entries in the
     * Membership attribute to no more than half (rounded down) of the MaxMembershipCount value.
     * If processing a command would cause this per-fabric limit to be exceeded, then the resource exhaustion behavior
     * of the associated command shall apply.
     * Since every MembershipStruct entry only supports up to 255 endpoints in the case where the Listener feature is
     * enabled in the FeatureMap, there may be situations where the server has to employ more than one entry to
     * represent the full list of joined endpoints in the list. In those cases:
     * - The entries associated with such a group shall be consecutive.
     * - These entries shall be identical in all fields except the Endpoints field.
     * - These entries shall NOT have any intersection of EndpointID values between their Endpoints field.
     * - The maximum number of entries over which a group membership is split shall NOT be more than the minimum number
     * of entries needed to fully encode the Endpoints list while respecting that no entry has more than 255 Endpoints
     * listed.
     * - Therefore, the maximum number of entries for that group shall be ceil(length(Endpoints) / 255).
     * The actual number of entries in the list across all fabrics shall be at most ceil(num_endpoints_in_node / 255) *
     * MaxMembershipCount.
     * For example, if a Group 123 is joined on endpoints 1 through 300 (i.e. a large group on a large bridge), the
     * following Membership list contents would both be valid examples, and there may be more valid entries matching the
     * rules.
     * The following examples would be ILLEGAL:
     */
    public List<MembershipStruct> membership; // 0 list R F V
    /**
     * Indicates the maximum number of Groups which can be joined and appear in entries of the Membership attribute.
     * This attribute does not specify the maximum number of list entries in the Membership attribute list, but rather
     * the maximum number of different GroupID values which can appear across all entries of the list.
     */
    public Integer maxMembershipCount; // 1 uint16 R V
    /**
     * Indicates the maximum number of unique multicast addresses the node can support. The value of this attribute
     * shall be at least 1, to support the IANA Assigned IPv6 Multicast Address.
     * When the PerGroup feature is supported, this attribute SHOULD be equal to MaxMembershipCount. However, the value
     * may be less than MaxMembershipCount. When the PerGroup feature is supported, the value of this attribute shall be
     * at least 4.
     */
    public Integer maxMcastAddrCount; // 2 uint16 R V
    /**
     * Indicates the number of unique multicast addresses currently in use by the Groupcast cluster. This count shall
     * include the IANA Assigned IPv6 Multicast Address if at least one group is configured to use the IanaAddr policy.
     * This count shall include one unique multicast address for each group configured to use the PerGroup policy. The
     * value of this attribute shall NOT exceed MaxMcastAddrCount.
     */
    public Integer usedMcastAddrCount; // 3 uint16 R V
    /**
     * Indicates the FabricIndex of the fabric currently testing the Groupcast feature with the GroupcastTesting
     * command.
     * The last accessing fabric to have invoked a successful TestGroupcast command that caused testing mode to be
     * engaged shall be the value indicated.
     * Otherwise, if Groupcast testing is currently disabled, this attribute shall have a value of zero, which is the
     * invalid FabricIndex value.
     * This attribute shall be set to zero when the server initializes.
     */
    public Integer fabricUnderTest; // 4 fabric-idx R V

    // Structs
    /**
     * This event shall be generated during Groupcast testing processing after invocation of the GroupcastTesting
     * command, under the conditions stated in that command's Effect on Receipt section. Some of the fields may be
     * present or absent depending of the test mode involved.
     * This event shall contain the following fields:
     */
    public static class GroupcastTesting {
        /**
         * This field, if present, shall be set to the source IPv6 address obtained from the UDP datagram of the
         * groupcast message.
         */
        public OctetString sourceIpAddress; // ipv6adr
        /**
         * This field, if present, shall be set to the destination IPv6 group address obtained from the UDP datagram of
         * a groupcast message.
         */
        public OctetString destinationIpAddress; // ipv6adr
        /**
         * This field, if present, shall be set to the GroupID associated with the groupcast message. This represents
         * the initial Group ID that led to InvokeRequest processing, the Group ID from the request, or the Group ID
         * used for message transmission.
         */
        public Integer groupId; // group-id
        /**
         * This field, if present, shall be set to the concrete path's endpoint ID derived from the processed request.
         */
        public Integer endpointId; // endpoint-no
        /**
         * This field, if present, shall be set to the concrete path's cluster ID derived from the processed request.
         */
        public Integer clusterId; // cluster-id
        /**
         * This field, if present, shall be set to the concrete path's element ID (Command ID or Attribute ID) derived
         * from the processed request.
         */
        public Integer elementId; // uint32
        /**
         * This field, if present, shall indicate whether the Groupcast sender was allowed to invoke the command by
         * Access Control, in this Node. The value shall be set to true if the request was allowed, and false otherwise.
         */
        public Boolean accessAllowed; // bool
        /**
         * This field shall indicate the outcome of the groupcast operation or the specific error encountered. The value
         * shall be one of the values defined in GroupcastTestResultEnum.
         */
        public GroupcastTestResultEnum groupcastTestResult; // GroupcastTestResultEnum
        public Integer fabricIndex; // FabricIndex

        public GroupcastTesting(OctetString sourceIpAddress, OctetString destinationIpAddress, Integer groupId,
                Integer endpointId, Integer clusterId, Integer elementId, Boolean accessAllowed,
                GroupcastTestResultEnum groupcastTestResult, Integer fabricIndex) {
            this.sourceIpAddress = sourceIpAddress;
            this.destinationIpAddress = destinationIpAddress;
            this.groupId = groupId;
            this.endpointId = endpointId;
            this.clusterId = clusterId;
            this.elementId = elementId;
            this.accessAllowed = accessAllowed;
            this.groupcastTestResult = groupcastTestResult;
            this.fabricIndex = fabricIndex;
        }
    }

    public static class MembershipStruct {
        /**
         * This field shall indicate an identifier for the multicast group within the fabric. Group Identifier is a
         * 16-bit value that shall be assigned by the administrator when the group is created.
         */
        public Integer groupId; // group-id
        /**
         * This field shall indicate a list of endpoint numbers that are members of the multicast group. This field is
         * only relevant for Listeners. The content of this field shall be considered in Request Path Expansion when
         * processing the relevant Interaction for the received Groupcast message for the associated GroupID. When the
         * device is acting as a Listener as member of the group, the Endpoints field shall list at least one endpoint
         * and shall NOT contain more than 255 endpoints.
         * If both the Listener and Sender bits are set in the FeatureMap attribute, then the list may be either empty
         * or missing, until such time that the JoinGroup command is done with a non-empty Endpoints field. With these
         * FeatureMap bits both set, the empty or missing list indicates that the group is joined for sending, but since
         * there are not endpoints available in the group membership, no group messages will be possible to receive.
         */
        public List<Integer> endpoints; // list
        /**
         * This field shall indicate the value used to identify the group operational key used for this group.
         * This field maps directly to the GroupKeySetID as defined in the Group Key Management cluster. This field is
         * provided by the administrator with the provided Key within the JoinGroup or UpdateGroupKey commands. This
         * field shall be used by administrators of the fabric to determine if the server has the correct group
         * operational key for their group. If not, the administrator may update the GroupKey using the UpdateGroupKey
         * command to recover communication.
         */
        public Integer keySetId; // uint16
        /**
         * This field shall indicate if AuxiliaryACL entries are to be generated. The field shall be set to true if the
         * group membership was configured to cause AuxiliaryACL entries to be generated, or false otherwise. See
         * Groupcast Auxiliary ACL Handling for the handling of this field.
         * See also the ConfigureAuxiliaryACL command.
         */
        public Boolean hasAuxiliaryAcl; // bool
        /**
         * This field shall indicate how the IPv6 Multicast Address shall be constructed for this group.
         */
        public MulticastAddrPolicyEnum mcastAddrPolicy; // MulticastAddrPolicyEnum
        public Integer fabricIndex; // FabricIndex

        public MembershipStruct(Integer groupId, List<Integer> endpoints, Integer keySetId, Boolean hasAuxiliaryAcl,
                MulticastAddrPolicyEnum mcastAddrPolicy, Integer fabricIndex) {
            this.groupId = groupId;
            this.endpoints = endpoints;
            this.keySetId = keySetId;
            this.hasAuxiliaryAcl = hasAuxiliaryAcl;
            this.mcastAddrPolicy = mcastAddrPolicy;
            this.fabricIndex = fabricIndex;
        }
    }

    // Enums
    public enum MulticastAddrPolicyEnum implements MatterEnum {
        IANA_ADDR(0, "Iana Addr"),
        PER_GROUP(1, "Per Group");

        private final Integer value;
        private final String label;

        private MulticastAddrPolicyEnum(Integer value, String label) {
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
     * See the GroupcastTesting command for a description of these operations.
     */
    public enum GroupcastTestingEnum implements MatterEnum {
        DISABLE_TESTING(0, "Disable Testing"),
        ENABLE_LISTENER_TESTING(1, "Enable Listener Testing"),
        ENABLE_SENDER_TESTING(2, "Enable Sender Testing");

        private final Integer value;
        private final String label;

        private GroupcastTestingEnum(Integer value, String label) {
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

    public enum GroupcastTestResultEnum implements MatterEnum {
        SUCCESS(0, "Success"),
        GENERAL_ERROR(1, "General Error"),
        MESSAGE_REPLAY(2, "Message Replay"),
        FAILED_AUTH(3, "Failed Auth"),
        NO_AVAILABLE_KEY(4, "No Available Key"),
        SEND_FAILURE(5, "Send Failure");

        private final Integer value;
        private final String label;

        private GroupcastTestResultEnum(Integer value, String label) {
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
         * This feature indicates that the device can join one or more Groupcast groups and receive multicast messages
         * targeted to those groups.
         */
        public boolean listener;
        /**
         * 
         * This feature indicates the ability to send multicast messages to one or more targeted groups of nodes to
         * which it belongs. Being a sender does not imply the ability to listen to messages sent to those multicast
         * addresses.
         */
        public boolean sender;
        /**
         * 
         * Supports PerGroup multicast addresses.
         */
        public boolean perGroup;

        public FeatureMap(boolean listener, boolean sender, boolean perGroup) {
            this.listener = listener;
            this.sender = sender;
            this.perGroup = perGroup;
        }
    }

    public GroupcastCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 101, "Groupcast");
    }

    protected GroupcastCluster(BigInteger nodeId, int endpointId, int clusterId, String clusterName) {
        super(nodeId, endpointId, clusterId, clusterName);
    }

    // commands
    /**
     * This command shall be used to instruct the server to join a multicast group. It provides a comprehensive way to
     * add all information required to create a new group in one command. This command shall be used to create a new
     * GroupID or to add specified endpoints to an existing GroupID. This command may also be used to change the
     * OperationalGroupKey associated with an existing GroupID, but if this is the only operation desired, the
     * UpdateGroupKey command SHOULD be used instead. This command shall be used following the rules defined in
     * Groupcast Key Management section.
     * This command shall have the following data fields subject to the listed conformance.
     */
    public static ClusterCommand joinGroup(Integer groupId, List<Integer> endpoints, Integer keySetId, OctetString key,
            Boolean useAuxiliaryAcl, Boolean replaceEndpoints, MulticastAddrPolicyEnum mcastAddrPolicy) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (groupId != null) {
            map.put("groupId", groupId);
        }
        if (endpoints != null) {
            map.put("endpoints", endpoints);
        }
        if (keySetId != null) {
            map.put("keySetId", keySetId);
        }
        if (key != null) {
            map.put("key", key);
        }
        if (useAuxiliaryAcl != null) {
            map.put("useAuxiliaryAcl", useAuxiliaryAcl);
        }
        if (replaceEndpoints != null) {
            map.put("replaceEndpoints", replaceEndpoints);
        }
        if (mcastAddrPolicy != null) {
            map.put("mcastAddrPolicy", mcastAddrPolicy);
        }
        return new ClusterCommand("joinGroup", map);
    }

    /**
     * This command shall allow a maintainer to request that the server withdraws itself or specific endpoints from a
     * specific group or from all groups of this client's fabric.
     * This command shall have the following data fields subject to the listed conformance.
     */
    public static ClusterCommand leaveGroup(Integer groupId, List<Integer> endpoints) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (groupId != null) {
            map.put("groupId", groupId);
        }
        if (endpoints != null) {
            map.put("endpoints", endpoints);
        }
        return new ClusterCommand("leaveGroup", map);
    }

    /**
     * This command shall allow a fabric administrator to update the OperationalGroupKey associated with the existing
     * group identified by GroupID, which is already joined. This command shall be used following the rules defined in
     * Groupcast Key Management section.
     * This command shall have the following data fields subject to the listed conformance.
     */
    public static ClusterCommand updateGroupKey(Integer groupId, Integer keySetId, OctetString key) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (groupId != null) {
            map.put("groupId", groupId);
        }
        if (keySetId != null) {
            map.put("keySetId", keySetId);
        }
        if (key != null) {
            map.put("key", key);
        }
        return new ClusterCommand("updateGroupKey", map);
    }

    /**
     * This command shall allow an Administrator to enable or disable the generation of AuxiliaryACL entries in the
     * Access Control Cluster based on the groups joined (see Groupcast Auxiliary ACL Handling).
     * This command shall have the following data fields subject to the listed conformance.
     */
    public static ClusterCommand configureAuxiliaryAcl(Integer groupId, Boolean useAuxiliaryAcl) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (groupId != null) {
            map.put("groupId", groupId);
        }
        if (useAuxiliaryAcl != null) {
            map.put("useAuxiliaryAcl", useAuxiliaryAcl);
        }
        return new ClusterCommand("configureAuxiliaryAcl", map);
    }

    /**
     * This command shall allow an Administrator to configure test modes that allow validation of Groupcast
     * communication.
     * This command shall have the following data fields subject to the listed conformance.
     */
    public static ClusterCommand groupcastTesting(GroupcastTestingEnum testOperation, Integer durationSeconds) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (testOperation != null) {
            map.put("testOperation", testOperation);
        }
        if (durationSeconds != null) {
            map.put("durationSeconds", durationSeconds);
        }
        return new ClusterCommand("groupcastTesting", map);
    }

    @Override
    public @NonNull String toString() {
        String str = "";
        str += "featureMap : " + featureMap + "\n";
        str += "membership : " + membership + "\n";
        str += "maxMembershipCount : " + maxMembershipCount + "\n";
        str += "maxMcastAddrCount : " + maxMcastAddrCount + "\n";
        str += "usedMcastAddrCount : " + usedMcastAddrCount + "\n";
        str += "fabricUnderTest : " + fabricUnderTest + "\n";
        return str;
    }
}
