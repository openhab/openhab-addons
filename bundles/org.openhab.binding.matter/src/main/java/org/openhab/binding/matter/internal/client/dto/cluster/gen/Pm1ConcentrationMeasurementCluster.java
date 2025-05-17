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

/**
 * Pm1ConcentrationMeasurement
 *
 * @author Dan Cunningham - Initial contribution
 */
public class Pm1ConcentrationMeasurementCluster extends ConcentrationMeasurementCluster {

    public static final int CLUSTER_ID = 0x042C;
    public static final String CLUSTER_NAME = "Pm1ConcentrationMeasurement";
    public static final String CLUSTER_PREFIX = "pm1ConcentrationMeasurement";

    public Pm1ConcentrationMeasurementCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 1068, "Pm1ConcentrationMeasurement");
    }

    protected Pm1ConcentrationMeasurementCluster(BigInteger nodeId, int endpointId, int clusterId, String clusterName) {
        super(nodeId, endpointId, clusterId, clusterName);
    }

    @Override
    public @NonNull String toString() {
        String str = "";
        return str;
    }
}
