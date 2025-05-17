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
 * ProxyDiscovery
 *
 * @author Dan Cunningham - Initial contribution
 */
public class ProxyDiscoveryCluster extends BaseCluster {

    public static final int CLUSTER_ID = 0x0043;
    public static final String CLUSTER_NAME = "ProxyDiscovery";
    public static final String CLUSTER_PREFIX = "proxyDiscovery";
    public static final String ATTRIBUTE_CLUSTER_REVISION = "clusterRevision";

    public Integer clusterRevision; // 65533 ClusterRevision

    public ProxyDiscoveryCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 67, "ProxyDiscovery");
    }

    protected ProxyDiscoveryCluster(BigInteger nodeId, int endpointId, int clusterId, String clusterName) {
        super(nodeId, endpointId, clusterId, clusterName);
    }

    // commands
    /**
     * This command is used during proxy discovery, as specified in Section 9.15.7, “Proxy Discovery &amp; Assignment
     * Flow”.
     */
    public static ClusterCommand proxyDiscoverRequest(BigInteger sourceNodeId, Integer numAttributePaths,
            Integer numEventPaths) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (sourceNodeId != null) {
            map.put("sourceNodeId", sourceNodeId);
        }
        if (numAttributePaths != null) {
            map.put("numAttributePaths", numAttributePaths);
        }
        if (numEventPaths != null) {
            map.put("numEventPaths", numEventPaths);
        }
        return new ClusterCommand("proxyDiscoverRequest", map);
    }

    @Override
    public @NonNull String toString() {
        String str = "";
        str += "clusterRevision : " + clusterRevision + "\n";
        return str;
    }
}
