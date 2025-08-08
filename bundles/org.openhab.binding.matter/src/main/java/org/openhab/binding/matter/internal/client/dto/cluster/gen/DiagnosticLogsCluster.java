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
 * DiagnosticLogs
 *
 * @author Dan Cunningham - Initial contribution
 */
public class DiagnosticLogsCluster extends BaseCluster {

    public static final int CLUSTER_ID = 0x0032;
    public static final String CLUSTER_NAME = "DiagnosticLogs";
    public static final String CLUSTER_PREFIX = "diagnosticLogs";
    public static final String ATTRIBUTE_CLUSTER_REVISION = "clusterRevision";

    public Integer clusterRevision; // 65533 ClusterRevision

    // Enums
    public enum IntentEnum implements MatterEnum {
        END_USER_SUPPORT(0, "End User Support"),
        NETWORK_DIAG(1, "Network Diag"),
        CRASH_LOGS(2, "Crash Logs");

        public final Integer value;
        public final String label;

        private IntentEnum(Integer value, String label) {
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

    public enum StatusEnum implements MatterEnum {
        SUCCESS(0, "Success"),
        EXHAUSTED(1, "Exhausted"),
        NO_LOGS(2, "No Logs"),
        BUSY(3, "Busy"),
        DENIED(4, "Denied");

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

    public enum TransferProtocolEnum implements MatterEnum {
        RESPONSE_PAYLOAD(0, "Response Payload"),
        BDX(1, "Bdx");

        public final Integer value;
        public final String label;

        private TransferProtocolEnum(Integer value, String label) {
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

    public DiagnosticLogsCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 50, "DiagnosticLogs");
    }

    protected DiagnosticLogsCluster(BigInteger nodeId, int endpointId, int clusterId, String clusterName) {
        super(nodeId, endpointId, clusterId, clusterName);
    }

    // commands
    /**
     * Reception of this command starts the process of retrieving diagnostic logs from a Node.
     */
    public static ClusterCommand retrieveLogsRequest(IntentEnum intent, TransferProtocolEnum requestedProtocol,
            String transferFileDesignator) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (intent != null) {
            map.put("intent", intent);
        }
        if (requestedProtocol != null) {
            map.put("requestedProtocol", requestedProtocol);
        }
        if (transferFileDesignator != null) {
            map.put("transferFileDesignator", transferFileDesignator);
        }
        return new ClusterCommand("retrieveLogsRequest", map);
    }

    @Override
    public @NonNull String toString() {
        String str = "";
        str += "clusterRevision : " + clusterRevision + "\n";
        return str;
    }
}
