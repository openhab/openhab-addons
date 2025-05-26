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

/**
 * PowerTopology
 *
 * @author Dan Cunningham - Initial contribution
 */
public class PowerTopologyCluster extends BaseCluster {

    public static final int CLUSTER_ID = 0x009C;
    public static final String CLUSTER_NAME = "PowerTopology";
    public static final String CLUSTER_PREFIX = "powerTopology";
    public static final String ATTRIBUTE_CLUSTER_REVISION = "clusterRevision";
    public static final String ATTRIBUTE_FEATURE_MAP = "featureMap";
    public static final String ATTRIBUTE_AVAILABLE_ENDPOINTS = "availableEndpoints";
    public static final String ATTRIBUTE_ACTIVE_ENDPOINTS = "activeEndpoints";

    public Integer clusterRevision; // 65533 ClusterRevision
    public FeatureMap featureMap; // 65532 FeatureMap
    /**
     * Indicates the list of endpoints capable of providing power to and/or consuming power from the endpoint hosting
     * this server.
     */
    public List<Integer> availableEndpoints; // 0 list R V
    /**
     * Indicates the current list of endpoints currently providing or consuming power to or from the endpoint hosting
     * this server. This list shall be a subset of the value of the AvailableEndpoints attribute.
     */
    public List<Integer> activeEndpoints; // 1 list R V

    // Bitmaps
    public static class FeatureMap {
        /**
         * 
         * This endpoint provides or consumes power to/from the entire node
         */
        public boolean nodeTopology;
        /**
         * 
         * This endpoint provides or consumes power to/from itself and its child endpoints
         */
        public boolean treeTopology;
        /**
         * 
         * This endpoint provides or consumes power to/from a specified set of endpoints
         */
        public boolean setTopology;
        /**
         * 
         * The specified set of endpoints may change
         */
        public boolean dynamicPowerFlow;

        public FeatureMap(boolean nodeTopology, boolean treeTopology, boolean setTopology, boolean dynamicPowerFlow) {
            this.nodeTopology = nodeTopology;
            this.treeTopology = treeTopology;
            this.setTopology = setTopology;
            this.dynamicPowerFlow = dynamicPowerFlow;
        }
    }

    public PowerTopologyCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 156, "PowerTopology");
    }

    protected PowerTopologyCluster(BigInteger nodeId, int endpointId, int clusterId, String clusterName) {
        super(nodeId, endpointId, clusterId, clusterName);
    }

    @Override
    public @NonNull String toString() {
        String str = "";
        str += "clusterRevision : " + clusterRevision + "\n";
        str += "featureMap : " + featureMap + "\n";
        str += "availableEndpoints : " + availableEndpoints + "\n";
        str += "activeEndpoints : " + activeEndpoints + "\n";
        return str;
    }
}
