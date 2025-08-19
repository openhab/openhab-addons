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
 * ProxyConfiguration
 *
 * @author Dan Cunningham - Initial contribution
 */
public class ProxyConfigurationCluster extends BaseCluster {

    public static final int CLUSTER_ID = 0x0042;
    public static final String CLUSTER_NAME = "ProxyConfiguration";
    public static final String CLUSTER_PREFIX = "proxyConfiguration";
    public static final String ATTRIBUTE_CLUSTER_REVISION = "clusterRevision";
    public static final String ATTRIBUTE_CONFIGURATION_LIST = "configurationList";

    public Integer clusterRevision; // 65533 ClusterRevision
    /**
     * List of proxy configurations. There shall NOT be multiple entries in this list for the same fabric.
     */
    public List<ConfigurationStruct> configurationList; // 0 list RW

    // Structs
    public static class ConfigurationStruct {
        /**
         * This field shall be set to true to indicate to the proxy that it shall proxy all nodes. When true, the
         * SourceList attribute is ignored.
         */
        public Boolean proxyAllNodes; // bool
        /**
         * When ProxyAllNodes is false, this list contains the set of Node IDs of sources that this proxy shall
         * specifically proxy.
         */
        public List<BigInteger> sourceList; // list

        public ConfigurationStruct(Boolean proxyAllNodes, List<BigInteger> sourceList) {
            this.proxyAllNodes = proxyAllNodes;
            this.sourceList = sourceList;
        }
    }

    public ProxyConfigurationCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 66, "ProxyConfiguration");
    }

    protected ProxyConfigurationCluster(BigInteger nodeId, int endpointId, int clusterId, String clusterName) {
        super(nodeId, endpointId, clusterId, clusterName);
    }

    @Override
    public @NonNull String toString() {
        String str = "";
        str += "clusterRevision : " + clusterRevision + "\n";
        str += "configurationList : " + configurationList + "\n";
        return str;
    }
}
