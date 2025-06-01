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
 * PowerSourceConfiguration
 *
 * @author Dan Cunningham - Initial contribution
 */
public class PowerSourceConfigurationCluster extends BaseCluster {

    public static final int CLUSTER_ID = 0x002E;
    public static final String CLUSTER_NAME = "PowerSourceConfiguration";
    public static final String CLUSTER_PREFIX = "powerSourceConfiguration";
    public static final String ATTRIBUTE_CLUSTER_REVISION = "clusterRevision";
    public static final String ATTRIBUTE_SOURCES = "sources";

    public Integer clusterRevision; // 65533 ClusterRevision
    /**
     * This list shall contain the set of all power sources capable of participating in the power system of this Node.
     * Each entry in the list shall be the endpoint number of an endpoint having a Power Source cluster, which
     * corresponds to a physical power source. The endpoint number shall be unique within the list.
     * The order of power sources on a Node is defined by the Order attribute of its associated Power Source cluster
     * provided on the endpoint. List entries shall be sorted in increasing order, that is, an entry with a lower order
     * shall have a lower index than any entry with a higher order. Multiple entries may have the same order, there are
     * no restrictions on their relative sorting.
     */
    public List<Integer> sources; // 0 list R V

    public PowerSourceConfigurationCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 46, "PowerSourceConfiguration");
    }

    protected PowerSourceConfigurationCluster(BigInteger nodeId, int endpointId, int clusterId, String clusterName) {
        super(nodeId, endpointId, clusterId, clusterName);
    }

    @Override
    public @NonNull String toString() {
        String str = "";
        str += "clusterRevision : " + clusterRevision + "\n";
        str += "sources : " + sources + "\n";
        return str;
    }
}
