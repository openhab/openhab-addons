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
 * Binding
 *
 * @author Dan Cunningham - Initial contribution
 */
public class BindingCluster extends BaseCluster {

    public static final int CLUSTER_ID = 0x001E;
    public static final String CLUSTER_NAME = "Binding";
    public static final String CLUSTER_PREFIX = "binding";
    public static final String ATTRIBUTE_CLUSTER_REVISION = "clusterRevision";
    public static final String ATTRIBUTE_BINDING = "binding";

    public Integer clusterRevision; // 65533 ClusterRevision
    /**
     * Each entry shall represent a binding.
     */
    public List<TargetStruct> binding; // 0 list RW F VM

    // Structs
    public static class TargetStruct {
        /**
         * This field is the remote target node ID. If the Endpoint field is present, this field shall be present.
         */
        public BigInteger node; // node-id
        /**
         * This field is the target group ID that represents remote endpoints. If the Endpoint field is present, this
         * field shall NOT be present.
         */
        public Integer group; // group-id
        /**
         * This field is the remote endpoint that the local endpoint is bound to. If the Group field is present, this
         * field shall NOT be present.
         */
        public Integer endpoint; // endpoint-no
        /**
         * This field is the cluster ID (client &amp; server) on the local and target endpoint(s). If this field is
         * present, the client cluster shall also exist on this endpoint (with this Binding cluster). If this field is
         * present, the target shall be this cluster on the target endpoint(s).
         */
        public Integer cluster; // cluster-id
        public Integer fabricIndex; // FabricIndex

        public TargetStruct(BigInteger node, Integer group, Integer endpoint, Integer cluster, Integer fabricIndex) {
            this.node = node;
            this.group = group;
            this.endpoint = endpoint;
            this.cluster = cluster;
            this.fabricIndex = fabricIndex;
        }
    }

    public BindingCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 30, "Binding");
    }

    protected BindingCluster(BigInteger nodeId, int endpointId, int clusterId, String clusterName) {
        super(nodeId, endpointId, clusterId, clusterName);
    }

    @Override
    public @NonNull String toString() {
        String str = "";
        str += "clusterRevision : " + clusterRevision + "\n";
        str += "binding : " + binding + "\n";
        return str;
    }
}
