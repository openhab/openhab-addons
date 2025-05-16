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

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.binding.matter.internal.client.dto.cluster.ClusterCommand;

/**
 * LowPower
 *
 * @author Dan Cunningham - Initial contribution
 */
public class LowPowerCluster extends BaseCluster {

    public static final int CLUSTER_ID = 0x0508;
    public static final String CLUSTER_NAME = "LowPower";
    public static final String CLUSTER_PREFIX = "lowPower";
    public static final String ATTRIBUTE_CLUSTER_REVISION = "clusterRevision";

    public Integer clusterRevision; // 65533 ClusterRevision

    public LowPowerCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 1288, "LowPower");
    }

    protected LowPowerCluster(BigInteger nodeId, int endpointId, int clusterId, String clusterName) {
        super(nodeId, endpointId, clusterId, clusterName);
    }

    // commands
    /**
     * This command shall put the device into low power mode.
     */
    public static ClusterCommand sleep() {
        return new ClusterCommand("sleep");
    }

    @Override
    public @NonNull String toString() {
        String str = "";
        str += "clusterRevision : " + clusterRevision + "\n";
        return str;
    }
}
