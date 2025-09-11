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
 * BooleanState
 *
 * @author Dan Cunningham - Initial contribution
 */
public class BooleanStateCluster extends BaseCluster {

    public static final int CLUSTER_ID = 0x0045;
    public static final String CLUSTER_NAME = "BooleanState";
    public static final String CLUSTER_PREFIX = "booleanState";
    public static final String ATTRIBUTE_CLUSTER_REVISION = "clusterRevision";
    public static final String ATTRIBUTE_STATE_VALUE = "stateValue";

    public Integer clusterRevision; // 65533 ClusterRevision
    /**
     * This represents a boolean state.
     * The semantics of this boolean state are defined by the device type using this cluster.
     * For example, in a Contact Sensor device type, FALSE&#x3D;open or no contact, TRUE&#x3D;closed or contact.
     */
    public Boolean stateValue; // 0 bool R V

    // Structs
    /**
     * If this event is supported, it shall be generated when the StateValue attribute changes.
     */
    public static class StateChange {
        /**
         * This field shall indicate the new value of the StateValue attribute.
         */
        public Boolean stateValue; // bool

        public StateChange(Boolean stateValue) {
            this.stateValue = stateValue;
        }
    }

    public BooleanStateCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 69, "BooleanState");
    }

    protected BooleanStateCluster(BigInteger nodeId, int endpointId, int clusterId, String clusterName) {
        super(nodeId, endpointId, clusterId, clusterName);
    }

    @Override
    public @NonNull String toString() {
        String str = "";
        str += "clusterRevision : " + clusterRevision + "\n";
        str += "stateValue : " + stateValue + "\n";
        return str;
    }
}
