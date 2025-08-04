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
 * ThreadNetworkDiagnostics
 *
 * @author Dan Cunningham - Initial contribution
 */
public class ThreadNetworkDiagnosticsCluster extends BaseCluster {

    public static final int CLUSTER_ID = 0x0035;
    public static final String CLUSTER_NAME = "ThreadNetworkDiagnostics";
    public static final String CLUSTER_PREFIX = "threadNetworkDiagnostics";
    public static final String ATTRIBUTE_CLUSTER_REVISION = "clusterRevision";
    public static final String ATTRIBUTE_FEATURE_MAP = "featureMap";
    public static final String ATTRIBUTE_CHANNEL = "channel";
    public static final String ATTRIBUTE_ROUTING_ROLE = "routingRole";
    public static final String ATTRIBUTE_NETWORK_NAME = "networkName";
    public static final String ATTRIBUTE_PAN_ID = "panId";
    public static final String ATTRIBUTE_EXTENDED_PAN_ID = "extendedPanId";
    public static final String ATTRIBUTE_MESH_LOCAL_PREFIX = "meshLocalPrefix";
    public static final String ATTRIBUTE_OVERRUN_COUNT = "overrunCount";
    public static final String ATTRIBUTE_NEIGHBOR_TABLE = "neighborTable";
    public static final String ATTRIBUTE_ROUTE_TABLE = "routeTable";
    public static final String ATTRIBUTE_PARTITION_ID = "partitionId";
    public static final String ATTRIBUTE_WEIGHTING = "weighting";
    public static final String ATTRIBUTE_DATA_VERSION = "dataVersion";
    public static final String ATTRIBUTE_STABLE_DATA_VERSION = "stableDataVersion";
    public static final String ATTRIBUTE_LEADER_ROUTER_ID = "leaderRouterId";
    public static final String ATTRIBUTE_DETACHED_ROLE_COUNT = "detachedRoleCount";
    public static final String ATTRIBUTE_CHILD_ROLE_COUNT = "childRoleCount";
    public static final String ATTRIBUTE_ROUTER_ROLE_COUNT = "routerRoleCount";
    public static final String ATTRIBUTE_LEADER_ROLE_COUNT = "leaderRoleCount";
    public static final String ATTRIBUTE_ATTACH_ATTEMPT_COUNT = "attachAttemptCount";
    public static final String ATTRIBUTE_PARTITION_ID_CHANGE_COUNT = "partitionIdChangeCount";
    public static final String ATTRIBUTE_BETTER_PARTITION_ATTACH_ATTEMPT_COUNT = "betterPartitionAttachAttemptCount";
    public static final String ATTRIBUTE_PARENT_CHANGE_COUNT = "parentChangeCount";
    public static final String ATTRIBUTE_TX_TOTAL_COUNT = "txTotalCount";
    public static final String ATTRIBUTE_TX_UNICAST_COUNT = "txUnicastCount";
    public static final String ATTRIBUTE_TX_BROADCAST_COUNT = "txBroadcastCount";
    public static final String ATTRIBUTE_TX_ACK_REQUESTED_COUNT = "txAckRequestedCount";
    public static final String ATTRIBUTE_TX_ACKED_COUNT = "txAckedCount";
    public static final String ATTRIBUTE_TX_NO_ACK_REQUESTED_COUNT = "txNoAckRequestedCount";
    public static final String ATTRIBUTE_TX_DATA_COUNT = "txDataCount";
    public static final String ATTRIBUTE_TX_DATA_POLL_COUNT = "txDataPollCount";
    public static final String ATTRIBUTE_TX_BEACON_COUNT = "txBeaconCount";
    public static final String ATTRIBUTE_TX_BEACON_REQUEST_COUNT = "txBeaconRequestCount";
    public static final String ATTRIBUTE_TX_OTHER_COUNT = "txOtherCount";
    public static final String ATTRIBUTE_TX_RETRY_COUNT = "txRetryCount";
    public static final String ATTRIBUTE_TX_DIRECT_MAX_RETRY_EXPIRY_COUNT = "txDirectMaxRetryExpiryCount";
    public static final String ATTRIBUTE_TX_INDIRECT_MAX_RETRY_EXPIRY_COUNT = "txIndirectMaxRetryExpiryCount";
    public static final String ATTRIBUTE_TX_ERR_CCA_COUNT = "txErrCcaCount";
    public static final String ATTRIBUTE_TX_ERR_ABORT_COUNT = "txErrAbortCount";
    public static final String ATTRIBUTE_TX_ERR_BUSY_CHANNEL_COUNT = "txErrBusyChannelCount";
    public static final String ATTRIBUTE_RX_TOTAL_COUNT = "rxTotalCount";
    public static final String ATTRIBUTE_RX_UNICAST_COUNT = "rxUnicastCount";
    public static final String ATTRIBUTE_RX_BROADCAST_COUNT = "rxBroadcastCount";
    public static final String ATTRIBUTE_RX_DATA_COUNT = "rxDataCount";
    public static final String ATTRIBUTE_RX_DATA_POLL_COUNT = "rxDataPollCount";
    public static final String ATTRIBUTE_RX_BEACON_COUNT = "rxBeaconCount";
    public static final String ATTRIBUTE_RX_BEACON_REQUEST_COUNT = "rxBeaconRequestCount";
    public static final String ATTRIBUTE_RX_OTHER_COUNT = "rxOtherCount";
    public static final String ATTRIBUTE_RX_ADDRESS_FILTERED_COUNT = "rxAddressFilteredCount";
    public static final String ATTRIBUTE_RX_DEST_ADDR_FILTERED_COUNT = "rxDestAddrFilteredCount";
    public static final String ATTRIBUTE_RX_DUPLICATED_COUNT = "rxDuplicatedCount";
    public static final String ATTRIBUTE_RX_ERR_NO_FRAME_COUNT = "rxErrNoFrameCount";
    public static final String ATTRIBUTE_RX_ERR_UNKNOWN_NEIGHBOR_COUNT = "rxErrUnknownNeighborCount";
    public static final String ATTRIBUTE_RX_ERR_INVALID_SRC_ADDR_COUNT = "rxErrInvalidSrcAddrCount";
    public static final String ATTRIBUTE_RX_ERR_SEC_COUNT = "rxErrSecCount";
    public static final String ATTRIBUTE_RX_ERR_FCS_COUNT = "rxErrFcsCount";
    public static final String ATTRIBUTE_RX_ERR_OTHER_COUNT = "rxErrOtherCount";
    public static final String ATTRIBUTE_ACTIVE_TIMESTAMP = "activeTimestamp";
    public static final String ATTRIBUTE_PENDING_TIMESTAMP = "pendingTimestamp";
    public static final String ATTRIBUTE_DELAY = "delay";
    public static final String ATTRIBUTE_SECURITY_POLICY = "securityPolicy";
    public static final String ATTRIBUTE_CHANNEL_PAGE0MASK = "channelPage0Mask";
    public static final String ATTRIBUTE_OPERATIONAL_DATASET_COMPONENTS = "operationalDatasetComponents";
    public static final String ATTRIBUTE_ACTIVE_NETWORK_FAULTS_LIST = "activeNetworkFaultsList";
    public static final String ATTRIBUTE_EXT_ADDRESS = "extAddress";
    public static final String ATTRIBUTE_RLOC16 = "rloc16";

    public Integer clusterRevision; // 65533 ClusterRevision
    public FeatureMap featureMap; // 65532 FeatureMap
    /**
     * The Channel attribute shall indicate the 802.15.4 channel number configured on the Node’s Thread interface (that
     * is, the Active Operational Dataset’s current Channel value). A value of null shall indicate that the Thread
     * interface is not currently configured or operational.
     */
    public Integer channel; // 0 uint16 R V
    /**
     * The RoutingRole attribute shall indicate the role that this Node has within the routing of messages through the
     * Thread network, as defined by RoutingRoleEnum. The potential roles are defined in the following table. A value of
     * null shall indicate that the Thread interface is not currently configured or operational.
     */
    public RoutingRoleEnum routingRole; // 1 RoutingRoleEnum R V
    /**
     * The NetworkName attribute shall indicate a human-readable (displayable) name for the Thread network that the Node
     * has been configured to join to. A value of null shall indicate that the Thread interface is not currently
     * configured or operational.
     */
    public String networkName; // 2 string R V
    /**
     * The PanId attribute shall indicate the 16-bit identifier of the Node on the Thread network. A value of null shall
     * indicate that the Thread interface is not currently configured or operational.
     */
    public Integer panId; // 3 uint16 R V
    /**
     * The ExtendedPanId attribute shall indicate the unique 64-bit identifier of the Node on the Thread network. A
     * value of null shall indicate that the Thread interface is not currently configured or operational.
     */
    public BigInteger extendedPanId; // 4 uint64 R V
    /**
     * The MeshLocalPrefix attribute shall indicate the mesh-local IPv6 prefix for the Thread network that the Node has
     * been configured to join to. A value of null shall indicate that the Thread interface is not currently configured
     * or operational.
     */
    public OctetString meshLocalPrefix; // 5 ipv6pre R V
    /**
     * The OverrunCount attribute shall indicate the number of packets dropped either at ingress or egress, due to lack
     * of buffer memory to retain all packets on the ethernet network interface. The OverrunCount attribute shall be
     * reset to 0 upon a reboot of the Node.
     */
    public BigInteger overrunCount; // 6 uint64 R V
    /**
     * The NeighborTable attribute shall indicate the current list of Nodes that comprise the neighbor table on the
     * Node.
     */
    public List<NeighborTableStruct> neighborTable; // 7 list R V
    /**
     * The RouteTable attribute shall indicate the current list of router capable Nodes for which routes have been
     * established.
     */
    public List<RouteTableStruct> routeTable; // 8 list R V
    /**
     * The PartitionId attribute shall indicate the Thread Leader Partition Id for the Thread network to which the Node
     * is joined. Null if not attached to a Thread network.
     */
    public Integer partitionId; // 9 uint32 R V
    /**
     * The Weighting attribute shall indicate the Thread Leader Weight used when operating in the Leader role. Null if
     * not attached to a Thread network.
     */
    public Integer weighting; // 10 uint16 R V
    /**
     * The DataVersion attribute shall indicate the full Network Data Version the Node currently uses. Null if not
     * attached to a Thread network.
     */
    public Integer dataVersion; // 11 uint16 R V
    /**
     * The StableDataVersion attribute shall indicate the Network Data Version for the stable subset of data the Node
     * currently uses. Null if not attached to a Thread network.
     */
    public Integer stableDataVersion; // 12 uint16 R V
    /**
     * The LeaderRouterId attribute shall indicate the 8-bit LeaderRouterId the Node shall attempt to utilize upon
     * becoming a router or leader on the Thread network. Null if not attached to a Thread network.
     */
    public Integer leaderRouterId; // 13 uint8 R V
    /**
     * The DetachedRoleCount attribute shall indicate the number of times the Node entered the OT_DEVICE_ROLE_DETACHED
     * role as specified within the Thread specification. This value shall only be reset upon a Node reboot.
     */
    public Integer detachedRoleCount; // 14 uint16 R V
    /**
     * The ChildRoleCount attribute shall indicate the number of times the Node entered the OT_DEVICE_ROLE_CHILD role as
     * specified within the Thread specification. This value shall only be reset upon a Node reboot.
     */
    public Integer childRoleCount; // 15 uint16 R V
    /**
     * The RouterRoleCount attribute shall indicate the number of times the Node entered the OT_DEVICE_ROLE_ROUTER role
     * as specified within the Thread specification. This value shall only be reset upon a Node reboot.
     */
    public Integer routerRoleCount; // 16 uint16 R V
    /**
     * The LeaderRoleCount attribute shall indicate the number of times the Node entered the OT_DEVICE_ROLE_LEADER role
     * as specified within the Thread specification. This value shall only be reset upon a Node reboot.
     */
    public Integer leaderRoleCount; // 17 uint16 R V
    /**
     * The AttachAttemptCount attribute shall indicate the number of attempts that have been made to attach to a Thread
     * network while the Node was detached from all Thread networks. This value shall only be reset upon a Node reboot.
     */
    public Integer attachAttemptCount; // 18 uint16 R V
    /**
     * The PartitionIdChangeCount attribute shall indicate the number of times that the Thread network that the Node is
     * connected to has changed its Partition ID. This value shall only be reset upon a Node reboot.
     */
    public Integer partitionIdChangeCount; // 19 uint16 R V
    /**
     * The BetterPartitionAttachAttemptCount attribute shall indicate the number of times a Node has attempted to attach
     * to a different Thread partition that it has determined is better than the partition it is currently attached to.
     * This value shall only be reset upon a Node reboot.
     */
    public Integer betterPartitionAttachAttemptCount; // 20 uint16 R V
    /**
     * The ParentChangeCount attribute shall indicate the number of times a Node has changed its parent. This value
     * shall only be reset upon a Node reboot.
     */
    public Integer parentChangeCount; // 21 uint16 R V
    /**
     * The TxTotalCount attribute shall indicate the total number of unique MAC frame transmission requests. The
     * TxTotalCount attribute shall only be incremented by 1 for each MAC transmission request regardless of the amount
     * of CCA failures, CSMA-CA attempts, or retransmissions. This value shall only be reset upon a Node reboot.
     */
    public Integer txTotalCount; // 22 uint32 R V
    /**
     * The TxUnicastCount attribute shall indicate the total number of unique unicast MAC frame transmission requests.
     * The TxUnicastCount attribute shall only be incremented by 1 for each unicast MAC transmission request regardless
     * of the amount of CCA failures, CSMA-CA attempts, or retransmissions. This value shall only be reset upon a Node
     * reboot.
     */
    public Integer txUnicastCount; // 23 uint32 R V
    /**
     * The TxBroadcastCount attribute shall indicate the total number of unique broadcast MAC frame transmission
     * requests. The TxBroadcastCount attribute shall only be incremented by 1 for each broadcast MAC transmission
     * request regardless of the amount of CCA failures, CSMA-CA attempts, or retransmissions. This value shall only be
     * reset upon a Node reboot.
     */
    public Integer txBroadcastCount; // 24 uint32 R V
    /**
     * The TxAckRequestedCount attribute shall indicate the total number of unique MAC frame transmission requests with
     * requested acknowledgment. The TxAckRequestedCount attribute shall only be incremented by 1 for each MAC
     * transmission request with requested acknowledgment regardless of the amount of CCA failures, CSMA-CA attempts, or
     * retransmissions. This value shall only be reset upon a Node reboot.
     */
    public Integer txAckRequestedCount; // 25 uint32 R V
    /**
     * The TxAckedCount attribute shall indicate the total number of unique MAC frame transmission requests that were
     * acked. The TxAckedCount attribute shall only be incremented by 1 for each MAC transmission request that is acked
     * regardless of the amount of CCA failures, CSMA-CA attempts, or retransmissions. This value shall only be reset
     * upon a Node reboot.
     */
    public Integer txAckedCount; // 26 uint32 R V
    /**
     * The TxNoAckRequestedCount attribute shall indicate the total number of unique MAC frame transmission requests
     * without requested acknowledgment. The TxNoAckRequestedCount attribute shall only be incremented by 1 for each MAC
     * transmission request that is does not request acknowledgement regardless of the amount of CCA failures, CSMA-CA
     * attempts, or retransmissions.
     */
    public Integer txNoAckRequestedCount; // 27 uint32 R V
    /**
     * The TxDataCount attribute shall indicate the total number of unique MAC Data frame transmission requests. The
     * TxDataCount attribute shall only be incremented by 1 for each MAC Data frame transmission request regardless of
     * the amount of CCA failures, CSMA-CA attempts, or retransmissions. This value shall only be reset upon a Node
     * reboot.
     */
    public Integer txDataCount; // 28 uint32 R V
    /**
     * The TxDataPollCount attribute shall indicate the total number of unique MAC Data Poll frame transmission
     * requests. The TxDataPollCount attribute shall only be incremented by 1 for each MAC Data Poll frame transmission
     * request regardless of the amount of CCA failures, CSMA-CA attempts, or retransmissions. This value shall only be
     * reset upon a Node reboot.
     */
    public Integer txDataPollCount; // 29 uint32 R V
    /**
     * The TxBeaconCount attribute shall indicate the total number of unique MAC Beacon frame transmission requests. The
     * TxBeaconCount attribute shall only be incremented by 1 for each MAC Beacon frame transmission request regardless
     * of the amount of CCA failures, CSMA-CA attempts, or retransmissions.
     */
    public Integer txBeaconCount; // 30 uint32 R V
    /**
     * The TxBeaconRequestCount attribute shall indicate the total number of unique MAC Beacon Request frame
     * transmission requests. The TxBeaconRequestCount attribute shall only be incremented by 1 for each MAC Beacon
     * Request frame transmission request regardless of the amount of CCA failures, CSMA-CA attempts, or
     * retransmissions. This value shall only be reset upon a Node reboot.
     */
    public Integer txBeaconRequestCount; // 31 uint32 R V
    /**
     * The TxOtherCount attribute shall indicate the total number of unique MAC frame transmission requests that are not
     * counted by any other attribute. The TxOtherCount attribute shall only be incremented by 1 for each MAC frame
     * transmission request regardless of the amount of CCA failures, CSMA-CA attempts, or retransmissions. This value
     * shall only be reset upon a Node reboot.
     */
    public Integer txOtherCount; // 32 uint32 R V
    /**
     * The TxRetryCount attribute shall indicate the total number of MAC retransmission attempts. The TxRetryCount
     * attribute shall only be incremented by 1 for each retransmission attempt that may be triggered by lack of
     * acknowledgement, CSMA/CA failure, or other type of transmission error. This value shall only be reset upon a Node
     * reboot.
     */
    public Integer txRetryCount; // 33 uint32 R V
    /**
     * The TxDirectMaxRetryExpiryCount attribute shall indicate the total number of unique MAC transmission packets that
     * meet maximal retry limit for direct packets. The TxDirectMaxRetryExpiryCount attribute shall only be incremented
     * by 1 for each unique MAC transmission packets that meets the maximal retry limit for direct packets. This value
     * shall only be reset upon a Node reboot.
     */
    public Integer txDirectMaxRetryExpiryCount; // 34 uint32 R V
    /**
     * The TxIndirectMaxRetryExpiryCount attribute shall indicate the total number of unique MAC transmission packets
     * that meet maximal retry limit for indirect packets. The TxIndirectMaxRetryExpiryCount attribute shall only be
     * incremented by 1 for each unique MAC transmission packets that meets the maximal retry limit for indirect
     * packets. This value shall only be reset upon a Node reboot.
     */
    public Integer txIndirectMaxRetryExpiryCount; // 35 uint32 R V
    /**
     * The TxErrCcaCount attribute shall indicate the total number of CCA failures. The TxErrCcaCount attribute shall
     * only be incremented by 1 for each instance of a CCA failure. This value shall only be reset upon a Node reboot.
     */
    public Integer txErrCcaCount; // 36 uint32 R V
    /**
     * The TxErrAbortCount attribute shall indicate the total number of unique MAC transmission request failures caused
     * by an abort error. The TxErrAbortCount attribute shall only be incremented by 1 for each unique MAC transmission
     * request failure caused by an abort error.
     */
    public Integer txErrAbortCount; // 37 uint32 R V
    /**
     * The TxErrBusyChannelCount attribute shall indicate the total number of unique MAC transmission request failures
     * caused by an error as the result of a busy channel (a CSMA/CA fail). The TxErrBusyChannelCount attribute shall
     * only be incremented by 1 for each unique MAC transmission request failure caused by a busy channel such as a
     * CSMA/CA failure.
     */
    public Integer txErrBusyChannelCount; // 38 uint32 R V
    /**
     * The RxTotalCount attribute shall indicate the total number of received unique MAC frames. This value shall only
     * be reset upon a Node reboot.
     */
    public Integer rxTotalCount; // 39 uint32 R V
    /**
     * The RxUnicastCount attribute shall indicate the total number of received unique unicast MAC frames. This value
     * shall only be reset upon a Node reboot.
     */
    public Integer rxUnicastCount; // 40 uint32 R V
    /**
     * The RxBroadcastCount attribute shall indicate the total number of received unique broadcast MAC frames. This
     * value shall only be reset upon a Node reboot.
     */
    public Integer rxBroadcastCount; // 41 uint32 R V
    /**
     * The RxDataCount attribute shall indicate the total number of received unique MAC Data frames. This value shall
     * only be reset upon a Node reboot.
     */
    public Integer rxDataCount; // 42 uint32 R V
    /**
     * The RxDataPollCount attribute shall indicate the total number of received unique MAC Data Poll frames. This value
     * shall only be reset upon a Node reboot.
     */
    public Integer rxDataPollCount; // 43 uint32 R V
    /**
     * The RxBeaconCount attribute shall indicate the total number of received unique MAC Beacon frames. This value
     * shall only be reset upon a Node reboot.
     */
    public Integer rxBeaconCount; // 44 uint32 R V
    /**
     * The RxBeaconRequestCount attribute shall indicate the total number of received unique MAC Beacon Request frames.
     * This value shall only be reset upon a Node reboot.
     */
    public Integer rxBeaconRequestCount; // 45 uint32 R V
    /**
     * The RxOtherCount attribute shall indicate the total number of received unique MAC frame requests that are not
     * counted by any other attribute. This value shall only be reset upon a Node reboot.
     */
    public Integer rxOtherCount; // 46 uint32 R V
    /**
     * The RxAddressFilteredCount attribute shall indicate the total number of received unique MAC frame requests that
     * have been dropped as a result of MAC filtering. This value shall only be reset upon a Node reboot.
     */
    public Integer rxAddressFilteredCount; // 47 uint32 R V
    /**
     * The RxDestAddrFilteredCount attribute shall indicate the total number of received unique MAC frame requests that
     * have been dropped as a result of a destination address check. This value shall only be reset upon a Node reboot.
     */
    public Integer rxDestAddrFilteredCount; // 48 uint32 R V
    /**
     * The RxDuplicatedCount attribute shall indicate the total number of received MAC frame requests that have been
     * dropped as a result of being a duplicate of a previously received MAC frame request. This value shall only be
     * reset upon a Node reboot.
     */
    public Integer rxDuplicatedCount; // 49 uint32 R V
    /**
     * The RxErrNoFrameCount attribute shall indicate the total number of received unique MAC frame requests that have
     * been dropped as a result of missing or malformed frame contents. This value shall only be reset upon a Node
     * reboot.
     */
    public Integer rxErrNoFrameCount; // 50 uint32 R V
    /**
     * The RxErrUnknownNeighborCount attribute shall indicate the total number of received unique MAC frame requests
     * that have been dropped as a result of originating from an unknown neighbor device. This value shall only be reset
     * upon a Node reboot.
     */
    public Integer rxErrUnknownNeighborCount; // 51 uint32 R V
    /**
     * The RxErrInvalidSrcAddrCount attribute shall indicate the total number of received unique MAC frame requests that
     * have been dropped as a result of containing an invalid source address. This value shall only be reset upon a Node
     * reboot.
     */
    public Integer rxErrInvalidSrcAddrCount; // 52 uint32 R V
    /**
     * The RxErrSecCount attribute shall indicate the total number of received unique MAC frame requests that have been
     * dropped as a result of an error with the security of the received frame. This value shall only be reset upon a
     * Node reboot.
     */
    public Integer rxErrSecCount; // 53 uint32 R V
    /**
     * The RxErrFcsCount attribute shall indicate the total number of received unique MAC frame requests that have been
     * dropped as a result of an error with the FCS of the received frame. This value shall only be reset upon a Node
     * reboot.
     */
    public Integer rxErrFcsCount; // 54 uint32 R V
    /**
     * The RxErrOtherCount attribute shall indicate the total number of received unique MAC frame requests that have
     * been dropped as a result of an error that is not counted by any other attribute. This value shall only be reset
     * upon a Node reboot.
     */
    public Integer rxErrOtherCount; // 55 uint32 R V
    /**
     * Null when there is no dataset configured.
     */
    public BigInteger activeTimestamp; // 56 uint64 R V
    /**
     * Null when there is no dataset configured.
     */
    public BigInteger pendingTimestamp; // 57 uint64 R V
    /**
     * Null when there is no dataset configured.
     */
    public Integer delay; // 58 uint32 R V
    /**
     * The SecurityPolicy attribute indicates the current security policies for the Thread partition to which a Node is
     * connected. Null when there is no dataset configured.
     */
    public SecurityPolicy securityPolicy; // 59 SecurityPolicy R V
    /**
     * The ChannelPage0Mask attribute indicates the channels within channel page 0, in the 2.4GHz ISM band. The channels
     * are represented in most significant bit order, with bit value 1 meaning selected, bit value 0 meaning unselected.
     * For example, the most significant bit of the left-most byte indicates channel 0. If channel 0 and channel 10 are
     * selected, the mask would be: 80 20 00 00. Null when there is no dataset configured.
     */
    public OctetString channelPage0Mask; // 60 octstr R V
    /**
     * The OperationalDatasetComponents attribute is a collection of flags to indicate the presence of various
     * operationally acquired values.
     */
    public OperationalDatasetComponents operationalDatasetComponents; // 61 OperationalDatasetComponents R V
    public List<NetworkFaultEnum> activeNetworkFaultsList; // 62 list R V
    public BigInteger extAddress; // 63 uint64 R V
    public Integer rloc16; // 64 uint16 R V

    // Structs
    /**
     * The ConnectionStatus Event shall indicate that a Node’s connection status to a Thread network has changed.
     */
    public static class ConnectionStatus {
        public ConnectionStatusEnum connectionStatus; // ConnectionStatusEnum

        public ConnectionStatus(ConnectionStatusEnum connectionStatus) {
            this.connectionStatus = connectionStatus;
        }
    }

    /**
     * The NetworkFaultChange Event shall indicate a change in the set of network faults currently detected by the Node.
     */
    public static class NetworkFaultChange {
        /**
         * This field shall represent the set of faults currently detected, as per Section 11.14.5.1, “NetworkFaultEnum
         * Type”.
         */
        public List<NetworkFaultEnum> current; // list
        /**
         * This field shall represent the set of faults detected prior to this change event, as per Section 11.14.5.1,
         * “NetworkFaultEnum Type”.
         */
        public List<NetworkFaultEnum> previous; // list

        public NetworkFaultChange(List<NetworkFaultEnum> current, List<NetworkFaultEnum> previous) {
            this.current = current;
            this.previous = previous;
        }
    }

    public static class NeighborTableStruct {
        /**
         * This field shall specify the IEEE 802.15.4 extended address for the neighboring Node.
         */
        public BigInteger extAddress; // uint64
        /**
         * This field shall specify the duration of time, in seconds, since a frame has been received from the
         * neighboring Node.
         */
        public Integer age; // uint32
        /**
         * This field shall specify the RLOC16 of the neighboring Node.
         */
        public Integer rloc16; // uint16
        /**
         * This field shall specify the number of link layer frames that have been received from the neighboring node.
         * This field shall be reset to 0 upon a reboot of the Node.
         */
        public Integer linkFrameCounter; // uint32
        /**
         * This field shall specify the number of Mesh Link Establishment frames that have been received from the
         * neighboring node. This field shall be reset to 0 upon a reboot of the Node.
         */
        public Integer mleFrameCounter; // uint32
        /**
         * This field shall specify the implementation specific mix of IEEE 802.15.4 PDU receive quality indicators,
         * scaled from 0 to 255.
         */
        public Integer lqi; // uint8
        /**
         * This field SHOULD specify the average RSSI across all received frames from the neighboring Node since the
         * receiving Node’s last reboot. If there is no known received frames this field SHOULD have the value of null.
         * This field shall have the units of dBm, having the range -128 dBm to 0 dBm.
         */
        public Integer averageRssi; // int8
        /**
         * This field shall specify the RSSI of the most recently received frame from the neighboring Node. If there is
         * no known last received frame the LastRssi field SHOULD have the value of null. This field shall have the
         * units of dBm, having the range -128 dBm to 0 dBm.
         */
        public Integer lastRssi; // int8
        /**
         * This field shall specify the percentage of received frames from the neighboring Node that have resulted in
         * errors.
         */
        public Integer frameErrorRate; // uint8
        /**
         * This field shall specify the percentage of received messages from the neighboring Node that have resulted in
         * errors.
         */
        public Integer messageErrorRate; // uint8
        /**
         * This field shall specify if the neighboring Node is capable of receiving frames while the Node is in an idle
         * state.
         */
        public Boolean rxOnWhenIdle; // bool
        /**
         * This field shall specify if the neighboring Node is a full Thread device.
         */
        public Boolean fullThreadDevice; // bool
        /**
         * This field shall specify if the neighboring Node requires the full Network Data. If set to False, the
         * neighboring Node only requires the stable Network Data.
         */
        public Boolean fullNetworkData; // bool
        /**
         * This field shall specify if the neighboring Node is a direct child of the Node reporting the NeighborTable
         * attribute.
         */
        public Boolean isChild; // bool

        public NeighborTableStruct(BigInteger extAddress, Integer age, Integer rloc16, Integer linkFrameCounter,
                Integer mleFrameCounter, Integer lqi, Integer averageRssi, Integer lastRssi, Integer frameErrorRate,
                Integer messageErrorRate, Boolean rxOnWhenIdle, Boolean fullThreadDevice, Boolean fullNetworkData,
                Boolean isChild) {
            this.extAddress = extAddress;
            this.age = age;
            this.rloc16 = rloc16;
            this.linkFrameCounter = linkFrameCounter;
            this.mleFrameCounter = mleFrameCounter;
            this.lqi = lqi;
            this.averageRssi = averageRssi;
            this.lastRssi = lastRssi;
            this.frameErrorRate = frameErrorRate;
            this.messageErrorRate = messageErrorRate;
            this.rxOnWhenIdle = rxOnWhenIdle;
            this.fullThreadDevice = fullThreadDevice;
            this.fullNetworkData = fullNetworkData;
            this.isChild = isChild;
        }
    }

    public static class RouteTableStruct {
        /**
         * This field shall specify the IEEE 802.15.4 extended address for the Node for which this route table entry
         * corresponds.
         */
        public BigInteger extAddress; // uint64
        /**
         * This field shall specify the RLOC16 for the Node for which this route table entry corresponds.
         */
        public Integer rloc16; // uint16
        /**
         * This field shall specify the Router ID for the Node for which this route table entry corresponds.
         */
        public Integer routerId; // uint8
        /**
         * This field shall specify the Router ID for the next hop in the route to the Node for which this route table
         * entry corresponds.
         */
        public Integer nextHop; // uint8
        /**
         * This Field shall specify the cost of the route to the Node for which this route table entry corresponds.
         */
        public Integer pathCost; // uint8
        /**
         * This field shall specify the implementation specific mix of IEEE 802.15.4 PDU receive quality indicators,
         * scaled from 0 to 255, from the perspective of the Node reporting the neighbor table.
         */
        public Integer lqiIn; // uint8
        /**
         * This field shall specify the implementation specific mix of IEEE 802.15.4 PDU receive quality indicators,
         * scaled from 0 to 255, from the perspective of the Node specified within the NextHop field.
         */
        public Integer lqiOut; // uint8
        /**
         * This field shall specify the duration of time, in seconds, since a frame has been received from the Node for
         * which this route table entry corresponds.
         */
        public Integer age; // uint8
        /**
         * This field shall specify if the router ID as defined within the RouterId field has been allocated.
         */
        public Boolean allocated; // bool
        /**
         * This field shall specify if a link has been established to the Node for which this route table entry
         * corresponds.
         */
        public Boolean linkEstablished; // bool

        public RouteTableStruct(BigInteger extAddress, Integer rloc16, Integer routerId, Integer nextHop,
                Integer pathCost, Integer lqiIn, Integer lqiOut, Integer age, Boolean allocated,
                Boolean linkEstablished) {
            this.extAddress = extAddress;
            this.rloc16 = rloc16;
            this.routerId = routerId;
            this.nextHop = nextHop;
            this.pathCost = pathCost;
            this.lqiIn = lqiIn;
            this.lqiOut = lqiOut;
            this.age = age;
            this.allocated = allocated;
            this.linkEstablished = linkEstablished;
        }
    }

    public static class SecurityPolicy {
        /**
         * This field shall specify the interval of time, in hours, that Thread security keys are rotated. Null when
         * there is no dataset configured.
         */
        public Integer rotationTime; // uint16
        /**
         * This field shall specify the flags as specified in Thread 1.3.0 section 8.10.1.15. Null when there is no
         * dataset configured.
         */
        public Integer flags; // uint16

        public SecurityPolicy(Integer rotationTime, Integer flags) {
            this.rotationTime = rotationTime;
            this.flags = flags;
        }
    }

    public static class OperationalDatasetComponents {
        /**
         * This field shall be True if the Node has an active timestamp present, else False.
         */
        public Boolean activeTimestampPresent; // bool
        /**
         * This field shall be True if the Node has a pending timestamp is present, else False.
         */
        public Boolean pendingTimestampPresent; // bool
        /**
         * This field shall be True if the Node has the Thread master key, else False.
         */
        public Boolean masterKeyPresent; // bool
        /**
         * This field shall be True if the Node has the Thread network’s name, else False.
         */
        public Boolean networkNamePresent; // bool
        /**
         * This field shall be True if the Node has an extended Pan ID, else False.
         */
        public Boolean extendedPanIdPresent; // bool
        /**
         * This field shall be True if the Node has the mesh local prefix, else False.
         */
        public Boolean meshLocalPrefixPresent; // bool
        /**
         * This field shall be True if the Node has the Thread network delay set, else False.
         */
        public Boolean delayPresent; // bool
        /**
         * This field shall be True if the Node has a Pan ID, else False.
         */
        public Boolean panIdPresent; // bool
        /**
         * This field shall be True if the Node has configured an operational channel for the Thread network, else
         * False.
         */
        public Boolean channelPresent; // bool
        /**
         * This field shall be True if the Node has been configured with the Thread network Pskc, else False.
         */
        public Boolean pskcPresent; // bool
        /**
         * This field shall be True if the Node has been configured with the Thread network security policies, else
         * False.
         */
        public Boolean securityPolicyPresent; // bool
        /**
         * This field shall be True if the Node has available a mask of available channels, else False.
         */
        public Boolean channelMaskPresent; // bool

        public OperationalDatasetComponents(Boolean activeTimestampPresent, Boolean pendingTimestampPresent,
                Boolean masterKeyPresent, Boolean networkNamePresent, Boolean extendedPanIdPresent,
                Boolean meshLocalPrefixPresent, Boolean delayPresent, Boolean panIdPresent, Boolean channelPresent,
                Boolean pskcPresent, Boolean securityPolicyPresent, Boolean channelMaskPresent) {
            this.activeTimestampPresent = activeTimestampPresent;
            this.pendingTimestampPresent = pendingTimestampPresent;
            this.masterKeyPresent = masterKeyPresent;
            this.networkNamePresent = networkNamePresent;
            this.extendedPanIdPresent = extendedPanIdPresent;
            this.meshLocalPrefixPresent = meshLocalPrefixPresent;
            this.delayPresent = delayPresent;
            this.panIdPresent = panIdPresent;
            this.channelPresent = channelPresent;
            this.pskcPresent = pskcPresent;
            this.securityPolicyPresent = securityPolicyPresent;
            this.channelMaskPresent = channelMaskPresent;
        }
    }

    // Enums
    public enum NetworkFaultEnum implements MatterEnum {
        UNSPECIFIED(0, "Unspecified"),
        LINK_DOWN(1, "Link Down"),
        HARDWARE_FAILURE(2, "Hardware Failure"),
        NETWORK_JAMMED(3, "Network Jammed");

        public final Integer value;
        public final String label;

        private NetworkFaultEnum(Integer value, String label) {
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

    public enum ConnectionStatusEnum implements MatterEnum {
        CONNECTED(0, "Connected"),
        NOT_CONNECTED(1, "Not Connected");

        public final Integer value;
        public final String label;

        private ConnectionStatusEnum(Integer value, String label) {
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

    public enum RoutingRoleEnum implements MatterEnum {
        UNSPECIFIED(0, "Unspecified"),
        UNASSIGNED(1, "Unassigned"),
        SLEEPY_END_DEVICE(2, "Sleepy End Device"),
        END_DEVICE(3, "End Device"),
        REED(4, "Reed"),
        ROUTER(5, "Router"),
        LEADER(6, "Leader");

        public final Integer value;
        public final String label;

        private RoutingRoleEnum(Integer value, String label) {
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
         * Server supports the counts for the number of received and transmitted packets on the Thread interface.
         */
        public boolean packetCounts;
        /**
         * 
         * Server supports the counts for the number of errors that have occurred during the reception and transmission
         * of packets on the Thread interface.
         */
        public boolean errorCounts;
        /**
         * 
         * Server supports the counts for various MLE layer happenings.
         */
        public boolean mleCounts;
        /**
         * 
         * Server supports the counts for various MAC layer happenings.
         */
        public boolean macCounts;

        public FeatureMap(boolean packetCounts, boolean errorCounts, boolean mleCounts, boolean macCounts) {
            this.packetCounts = packetCounts;
            this.errorCounts = errorCounts;
            this.mleCounts = mleCounts;
            this.macCounts = macCounts;
        }
    }

    public ThreadNetworkDiagnosticsCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 53, "ThreadNetworkDiagnostics");
    }

    protected ThreadNetworkDiagnosticsCluster(BigInteger nodeId, int endpointId, int clusterId, String clusterName) {
        super(nodeId, endpointId, clusterId, clusterName);
    }

    // commands
    /**
     * Reception of this command shall reset the following attributes to 0:
     * • OverrunCount
     * This command has no associated data. Upon completion, this command shall send a status code set to a value of
     * SUCCESS back to the initiator.
     */
    public static ClusterCommand resetCounts() {
        return new ClusterCommand("resetCounts");
    }

    @Override
    public @NonNull String toString() {
        String str = "";
        str += "clusterRevision : " + clusterRevision + "\n";
        str += "featureMap : " + featureMap + "\n";
        str += "channel : " + channel + "\n";
        str += "routingRole : " + routingRole + "\n";
        str += "networkName : " + networkName + "\n";
        str += "panId : " + panId + "\n";
        str += "extendedPanId : " + extendedPanId + "\n";
        str += "meshLocalPrefix : " + meshLocalPrefix + "\n";
        str += "overrunCount : " + overrunCount + "\n";
        str += "neighborTable : " + neighborTable + "\n";
        str += "routeTable : " + routeTable + "\n";
        str += "partitionId : " + partitionId + "\n";
        str += "weighting : " + weighting + "\n";
        str += "dataVersion : " + dataVersion + "\n";
        str += "stableDataVersion : " + stableDataVersion + "\n";
        str += "leaderRouterId : " + leaderRouterId + "\n";
        str += "detachedRoleCount : " + detachedRoleCount + "\n";
        str += "childRoleCount : " + childRoleCount + "\n";
        str += "routerRoleCount : " + routerRoleCount + "\n";
        str += "leaderRoleCount : " + leaderRoleCount + "\n";
        str += "attachAttemptCount : " + attachAttemptCount + "\n";
        str += "partitionIdChangeCount : " + partitionIdChangeCount + "\n";
        str += "betterPartitionAttachAttemptCount : " + betterPartitionAttachAttemptCount + "\n";
        str += "parentChangeCount : " + parentChangeCount + "\n";
        str += "txTotalCount : " + txTotalCount + "\n";
        str += "txUnicastCount : " + txUnicastCount + "\n";
        str += "txBroadcastCount : " + txBroadcastCount + "\n";
        str += "txAckRequestedCount : " + txAckRequestedCount + "\n";
        str += "txAckedCount : " + txAckedCount + "\n";
        str += "txNoAckRequestedCount : " + txNoAckRequestedCount + "\n";
        str += "txDataCount : " + txDataCount + "\n";
        str += "txDataPollCount : " + txDataPollCount + "\n";
        str += "txBeaconCount : " + txBeaconCount + "\n";
        str += "txBeaconRequestCount : " + txBeaconRequestCount + "\n";
        str += "txOtherCount : " + txOtherCount + "\n";
        str += "txRetryCount : " + txRetryCount + "\n";
        str += "txDirectMaxRetryExpiryCount : " + txDirectMaxRetryExpiryCount + "\n";
        str += "txIndirectMaxRetryExpiryCount : " + txIndirectMaxRetryExpiryCount + "\n";
        str += "txErrCcaCount : " + txErrCcaCount + "\n";
        str += "txErrAbortCount : " + txErrAbortCount + "\n";
        str += "txErrBusyChannelCount : " + txErrBusyChannelCount + "\n";
        str += "rxTotalCount : " + rxTotalCount + "\n";
        str += "rxUnicastCount : " + rxUnicastCount + "\n";
        str += "rxBroadcastCount : " + rxBroadcastCount + "\n";
        str += "rxDataCount : " + rxDataCount + "\n";
        str += "rxDataPollCount : " + rxDataPollCount + "\n";
        str += "rxBeaconCount : " + rxBeaconCount + "\n";
        str += "rxBeaconRequestCount : " + rxBeaconRequestCount + "\n";
        str += "rxOtherCount : " + rxOtherCount + "\n";
        str += "rxAddressFilteredCount : " + rxAddressFilteredCount + "\n";
        str += "rxDestAddrFilteredCount : " + rxDestAddrFilteredCount + "\n";
        str += "rxDuplicatedCount : " + rxDuplicatedCount + "\n";
        str += "rxErrNoFrameCount : " + rxErrNoFrameCount + "\n";
        str += "rxErrUnknownNeighborCount : " + rxErrUnknownNeighborCount + "\n";
        str += "rxErrInvalidSrcAddrCount : " + rxErrInvalidSrcAddrCount + "\n";
        str += "rxErrSecCount : " + rxErrSecCount + "\n";
        str += "rxErrFcsCount : " + rxErrFcsCount + "\n";
        str += "rxErrOtherCount : " + rxErrOtherCount + "\n";
        str += "activeTimestamp : " + activeTimestamp + "\n";
        str += "pendingTimestamp : " + pendingTimestamp + "\n";
        str += "delay : " + delay + "\n";
        str += "securityPolicy : " + securityPolicy + "\n";
        str += "channelPage0Mask : " + channelPage0Mask + "\n";
        str += "operationalDatasetComponents : " + operationalDatasetComponents + "\n";
        str += "activeNetworkFaultsList : " + activeNetworkFaultsList + "\n";
        str += "extAddress : " + extAddress + "\n";
        str += "rloc16 : " + rloc16 + "\n";
        return str;
    }
}
