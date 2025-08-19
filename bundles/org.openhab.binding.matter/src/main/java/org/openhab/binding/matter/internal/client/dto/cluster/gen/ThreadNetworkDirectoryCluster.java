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
 * ThreadNetworkDirectory
 *
 * @author Dan Cunningham - Initial contribution
 */
public class ThreadNetworkDirectoryCluster extends BaseCluster {

    public static final int CLUSTER_ID = 0x0453;
    public static final String CLUSTER_NAME = "ThreadNetworkDirectory";
    public static final String CLUSTER_PREFIX = "threadNetworkDirectory";
    public static final String ATTRIBUTE_CLUSTER_REVISION = "clusterRevision";
    public static final String ATTRIBUTE_PREFERRED_EXTENDED_PAN_ID = "preferredExtendedPanId";
    public static final String ATTRIBUTE_THREAD_NETWORKS = "threadNetworks";
    public static final String ATTRIBUTE_THREAD_NETWORK_TABLE_SIZE = "threadNetworkTableSize";

    public Integer clusterRevision; // 65533 ClusterRevision
    /**
     * Indicates the Thread Extended PAN ID value for the Thread network designated by the user to be their preferred
     * network for commissioning of Thread devices. If not null, the value of this attribute shall match the
     * ExtendedPanID of a network in the ThreadNetworks attribute. A write operation with a non-null value that does not
     * match any network in the ThreadNetworks list shall be rejected with a status of CONSTRAINT_ERROR.
     * The purpose of designating one Thread network as preferred is to help a commissioner to select a Thread network
     * when a Thread device is within suitable range of more than one Thread network which appears in the ThreadNetworks
     * list. A value of null indicates that there is no current preferred network: All networks may be treated as
     * equally preferred by a commissioner with access to this cluster.
     * This attribute may be automatically set to the ExtendedPanID of the first Thread network added to the
     * ThreadNetworks list.
     * A client shall obtain user consent before changing the value of this attribute from a non-null value.
     * On a factory reset this attribute shall be reset to null.
     */
    public OctetString preferredExtendedPanId; // 0 octstr RW VM
    /**
     * Indicates the list of Thread Networks known about by this cluster. If the node hosting this cluster includes a
     * Thread Border Router, then an entry for its Thread Network shall be included in this list.
     * The list can be modified via the AddNetwork and RemoveNetwork commands.
     * For each entry in the list, the cluster server also stores a Thread Operational Dataset. Clients use the
     * GetOperationalDataset command to obtain the Operational Dataset for an entry in this list.
     * On a factory reset this list shall be cleared, and any Thread Operational datasets previously stored shall be
     * removed from the Node.
     */
    public List<ThreadNetworkStruct> threadNetworks; // 1 list R V
    /**
     * This attribute shall indicate the maximum number of entries that can be held in the ThreadNetworks list; it shall
     * be at least 2 times the number of SupportedFabrics advertised in the Operational Credentials Cluster on the root
     * endpoint of this node.
     */
    public Integer threadNetworkTableSize; // 2 uint8 R V

    // Structs
    /**
     * Represents the data associated with a Thread Network.
     */
    public static class ThreadNetworkStruct {
        /**
         * This field shall indicate the Extended PAN ID from the OperationalDataset for the given Thread network.
         */
        public OctetString extendedPanId; // octstr
        /**
         * This field shall indicate the Network Name from the OperationalDataset for the given Thread network.
         */
        public String networkName; // string
        /**
         * This field shall indicate the Channel from the OperationalDataset for the given Thread network.
         */
        public Integer channel; // uint16
        /**
         * This field shall indicate the Active Timestamp from the OperationalDataset for the given Thread network.
         */
        public BigInteger activeTimestamp; // uint64

        public ThreadNetworkStruct(OctetString extendedPanId, String networkName, Integer channel,
                BigInteger activeTimestamp) {
            this.extendedPanId = extendedPanId;
            this.networkName = networkName;
            this.channel = channel;
            this.activeTimestamp = activeTimestamp;
        }
    }

    public ThreadNetworkDirectoryCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 1107, "ThreadNetworkDirectory");
    }

    protected ThreadNetworkDirectoryCluster(BigInteger nodeId, int endpointId, int clusterId, String clusterName) {
        super(nodeId, endpointId, clusterId, clusterName);
    }

    // commands
    /**
     * Adds an entry to the ThreadNetworks attribute with the specified Thread Operational Dataset.
     * If there is an existing entry with the Extended PAN ID then the Thread Operational Dataset for that entry is
     * replaced. As a result, changes to the network parameters (e.g. Channel, Network Name, PSKc, …) of an existing
     * entry with a given Extended PAN ID can be made using this command.
     */
    public static ClusterCommand addNetwork(OctetString operationalDataset) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (operationalDataset != null) {
            map.put("operationalDataset", operationalDataset);
        }
        return new ClusterCommand("addNetwork", map);
    }

    /**
     * Removes the network with the given Extended PAN ID from the ThreadNetworks attribute.
     */
    public static ClusterCommand removeNetwork(OctetString extendedPanId) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (extendedPanId != null) {
            map.put("extendedPanId", extendedPanId);
        }
        return new ClusterCommand("removeNetwork", map);
    }

    /**
     * Retrieves the Thread Operational Dataset with the given Extended PAN ID.
     */
    public static ClusterCommand getOperationalDataset(OctetString extendedPanId) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (extendedPanId != null) {
            map.put("extendedPanId", extendedPanId);
        }
        return new ClusterCommand("getOperationalDataset", map);
    }

    @Override
    public @NonNull String toString() {
        String str = "";
        str += "clusterRevision : " + clusterRevision + "\n";
        str += "preferredExtendedPanId : " + preferredExtendedPanId + "\n";
        str += "threadNetworks : " + threadNetworks + "\n";
        str += "threadNetworkTableSize : " + threadNetworkTableSize + "\n";
        return str;
    }
}
