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
 * ContentAppObserver
 *
 * @author Dan Cunningham - Initial contribution
 */
public class ContentAppObserverCluster extends BaseCluster {

    public static final int CLUSTER_ID = 0x0510;
    public static final String CLUSTER_NAME = "ContentAppObserver";
    public static final String CLUSTER_PREFIX = "contentAppObserver";
    public static final String ATTRIBUTE_CLUSTER_REVISION = "clusterRevision";

    public Integer clusterRevision; // 65533 ClusterRevision

    // Enums
    public enum StatusEnum implements MatterEnum {
        SUCCESS(0, "Success"),
        UNEXPECTED_DATA(1, "Unexpected Data");

        public final Integer value;
        public final String label;

        private StatusEnum(Integer value, String label) {
            this.value = value;
            this.label = label;
        }

        @Override
        public Integer getValue() {
            return value;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    public ContentAppObserverCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 1296, "ContentAppObserver");
    }

    protected ContentAppObserverCluster(BigInteger nodeId, int endpointId, int clusterId, String clusterName) {
        super(nodeId, endpointId, clusterId, clusterName);
    }

    // commands
    /**
     * Upon receipt, the data field may be parsed and interpreted. Message encoding is specific to the Content App. A
     * Content App may when possible read attributes from the Basic Information Cluster on the Observer and use this to
     * determine the Message encoding.
     * This command returns a ContentAppMessage Response.
     */
    public static ClusterCommand contentAppMessage(String data, String encodingHint) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (data != null) {
            map.put("data", data);
        }
        if (encodingHint != null) {
            map.put("encodingHint", encodingHint);
        }
        return new ClusterCommand("contentAppMessage", map);
    }

    @Override
    public @NonNull String toString() {
        String str = "";
        str += "clusterRevision : " + clusterRevision + "\n";
        return str;
    }
}
