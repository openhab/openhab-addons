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
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.binding.matter.internal.client.dto.cluster.ClusterCommand;

/**
 * ThreadBorderRouterManagement
 *
 * @author Dan Cunningham - Initial contribution
 */
public class ThreadBorderRouterManagementCluster extends BaseCluster {

    public static final int CLUSTER_ID = 0x0452;
    public static final String CLUSTER_NAME = "ThreadBorderRouterManagement";
    public static final String CLUSTER_PREFIX = "threadBorderRouterManagement";
    public static final String ATTRIBUTE_CLUSTER_REVISION = "clusterRevision";
    public static final String ATTRIBUTE_FEATURE_MAP = "featureMap";
    public static final String ATTRIBUTE_BORDER_ROUTER_NAME = "borderRouterName";
    public static final String ATTRIBUTE_BORDER_AGENT_ID = "borderAgentId";
    public static final String ATTRIBUTE_THREAD_VERSION = "threadVersion";
    public static final String ATTRIBUTE_INTERFACE_ENABLED = "interfaceEnabled";
    public static final String ATTRIBUTE_ACTIVE_DATASET_TIMESTAMP = "activeDatasetTimestamp";
    public static final String ATTRIBUTE_PENDING_DATASET_TIMESTAMP = "pendingDatasetTimestamp";

    public Integer clusterRevision; // 65533 ClusterRevision
    public FeatureMap featureMap; // 65532 FeatureMap
    /**
     * Indicates a user-friendly name identifying the device model or product of the Border Router in MeshCOP (DNS-SD
     * service name) as defined in the Thread specification, and has the following recommended format:
     * &lt;VendorName&gt; &lt;ProductName&gt;._meshcop._udp. An example name would be ACME Border Router
     * (74be)._meshcop._udp.
     */
    public String borderRouterName; // 0 string R V
    /**
     * Indicates a 16-byte globally unique ID for a Thread Border Router device. This ID is manufacturer-specific, and
     * it is created and managed by the border router’s implementation.
     */
    public OctetString borderAgentId; // 1 octstr R V
    /**
     * Indicates the Thread version supported by the Thread interface configured by the cluster instance.
     * The format shall match the value mapping defined in the &quot;Version TLV&quot; section of the Thread
     * specification. For example, Thread 1.3.0 would have ThreadVersion set to 4.
     */
    public Integer threadVersion; // 2 uint16 R V
    /**
     * Indicates whether the associated IEEE 802.15.4 Thread interface is enabled or disabled.
     */
    public Boolean interfaceEnabled; // 3 bool R V
    /**
     * Null if the Thread Border Router has no dataset configured, otherwise it shall be the timestamp value extracted
     * from the Active Dataset value configured by the Thread Node to which the border router is connected. This
     * attribute shall be updated when a new Active dataset is configured on the Thread network to which the border
     * router is connected.
     */
    public BigInteger activeDatasetTimestamp; // 4 uint64 R V
    /**
     * Null if the Thread Border Router has no Pending dataset configured, otherwise it shall be the timestamp value
     * extracted from the Pending Dataset value configured by the Thread Node to which the border router is connected.
     * This attribute shall be updated when a new Pending dataset is configured on the Thread network to which the
     * border router is connected.
     */
    public BigInteger pendingDatasetTimestamp; // 5 uint64 R V

    // Bitmaps
    public static class FeatureMap {
        /**
         * 
         * This feature shall indicate the ability of the Border Router to change its already configured PAN to another,
         * by setting a pending dataset.
         * &gt; [!NOTE]
         * &gt; This feature flag can be used to protect an already-configured network from accidental configuration
         * change, e.g. when the Thread Border Router serves non- Matter devices that do not support PAN change for an
         * implementation-specific reason.
         */
        public boolean panChange;

        public FeatureMap(boolean panChange) {
            this.panChange = panChange;
        }
    }

    public ThreadBorderRouterManagementCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 1106, "ThreadBorderRouterManagement");
    }

    protected ThreadBorderRouterManagementCluster(BigInteger nodeId, int endpointId, int clusterId,
            String clusterName) {
        super(nodeId, endpointId, clusterId, clusterName);
    }

    // commands
    /**
     * This command shall be used to request the active operational dataset of the Thread network to which the border
     * router is connected.
     * If the command is not executed via a CASE session, the command shall fail with a status code of
     * UNSUPPORTED_ACCESS.
     * If an internal error occurs, then this command shall fail with a FAILURE status code sent back to the initiator.
     * Otherwise, this shall generate a DatasetResponse command.
     */
    public static ClusterCommand getActiveDatasetRequest() {
        return new ClusterCommand("getActiveDatasetRequest");
    }

    /**
     * This command shall be used to request the pending dataset of the Thread network to which the border router is
     * connected.
     * If the command is not executed via a CASE session, the command shall fail with a status code of
     * UNSUPPORTED_ACCESS.
     * If an internal error occurs, then this command shall fail with a FAILURE status code sent back to the initiator.
     * Otherwise, this shall generate a DatasetResponse command.
     */
    public static ClusterCommand getPendingDatasetRequest() {
        return new ClusterCommand("getPendingDatasetRequest");
    }

    /**
     * This command shall be used to set the active Dataset of the Thread network to which the Border Router is
     * connected, when there is no active dataset already.
     */
    public static ClusterCommand setActiveDatasetRequest(OctetString activeDataset, BigInteger breadcrumb) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (activeDataset != null) {
            map.put("activeDataset", activeDataset);
        }
        if (breadcrumb != null) {
            map.put("breadcrumb", breadcrumb);
        }
        return new ClusterCommand("setActiveDatasetRequest", map);
    }

    /**
     * This command shall be used to set or update the pending Dataset of the Thread network to which the Border Router
     * is connected, if the Border Router supports PAN Change.
     * If the command is not executed via a CASE session, the command shall fail with a status code of
     * UNSUPPORTED_ACCESS.
     * This PendingDataset field shall contain the pending dataset to which the Thread network should be updated. The
     * format of the data shall be an octet string containing the raw Thread TLV value of the pending dataset, as
     * defined in the Thread specification.
     * If any of the parameters in the PendingDataset is invalid, the command shall fail with a status of
     * INVALID_COMMAND.
     * Otherwise, this command shall configure the pending dataset of the Thread network to which the Border Router is
     * connected, with the value given in the PendingDataset parameter. The Border Router will manage activation of the
     * pending dataset as defined in the Thread specification.
     */
    public static ClusterCommand setPendingDatasetRequest(OctetString pendingDataset) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (pendingDataset != null) {
            map.put("pendingDataset", pendingDataset);
        }
        return new ClusterCommand("setPendingDatasetRequest", map);
    }

    @Override
    public @NonNull String toString() {
        String str = "";
        str += "clusterRevision : " + clusterRevision + "\n";
        str += "featureMap : " + featureMap + "\n";
        str += "borderRouterName : " + borderRouterName + "\n";
        str += "borderAgentId : " + borderAgentId + "\n";
        str += "threadVersion : " + threadVersion + "\n";
        str += "interfaceEnabled : " + interfaceEnabled + "\n";
        str += "activeDatasetTimestamp : " + activeDatasetTimestamp + "\n";
        str += "pendingDatasetTimestamp : " + pendingDatasetTimestamp + "\n";
        return str;
    }
}
