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
import org.openhab.binding.matter.internal.client.dto.cluster.ClusterCommand;

/**
 * SoftwareDiagnostics
 *
 * @author Dan Cunningham - Initial contribution
 */
public class SoftwareDiagnosticsCluster extends BaseCluster {

    public static final int CLUSTER_ID = 0x0034;
    public static final String CLUSTER_NAME = "SoftwareDiagnostics";
    public static final String CLUSTER_PREFIX = "softwareDiagnostics";
    public static final String ATTRIBUTE_CLUSTER_REVISION = "clusterRevision";
    public static final String ATTRIBUTE_FEATURE_MAP = "featureMap";
    public static final String ATTRIBUTE_THREAD_METRICS = "threadMetrics";
    public static final String ATTRIBUTE_CURRENT_HEAP_FREE = "currentHeapFree";
    public static final String ATTRIBUTE_CURRENT_HEAP_USED = "currentHeapUsed";
    public static final String ATTRIBUTE_CURRENT_HEAP_HIGH_WATERMARK = "currentHeapHighWatermark";

    public Integer clusterRevision; // 65533 ClusterRevision
    public FeatureMap featureMap; // 65532 FeatureMap
    /**
     * The ThreadMetrics attribute shall be a list of ThreadMetricsStruct structs. Each active thread on the Node shall
     * be represented by a single entry within the ThreadMetrics attribute.
     */
    public List<ThreadMetricsStruct> threadMetrics; // 0 list R V
    /**
     * The CurrentHeapFree attribute shall indicate the current amount of heap memory, in bytes, that are free for
     * allocation. The effective amount may be smaller due to heap fragmentation or other reasons.
     */
    public BigInteger currentHeapFree; // 1 uint64 R V
    /**
     * The CurrentHeapUsed attribute shall indicate the current amount of heap memory, in bytes, that is being used.
     */
    public BigInteger currentHeapUsed; // 2 uint64 R V
    /**
     * The CurrentHeapHighWatermark attribute shall indicate the maximum amount of heap memory, in bytes, that has been
     * used by the Node. This value shall only be reset upon a Node reboot or upon receiving of the ResetWatermarks
     * command.
     */
    public BigInteger currentHeapHighWatermark; // 3 uint64 R V

    // Structs
    /**
     * The SoftwareFault Event shall be generated when a software fault takes place on the Node.
     */
    public static class SoftwareFault {
        /**
         * The ID field shall be set to the ID of the software thread in which the last software fault occurred.
         */
        public BigInteger id; // uint64
        /**
         * The Name field shall be set to a manufacturer-specified name or prefix of the software thread in which the
         * last software fault occurred.
         */
        public String name; // string
        /**
         * The FaultRecording field shall be a manufacturer-specified payload intended to convey information to assist
         * in further diagnosing or debugging a software fault. The FaultRecording field may be used to convey
         * information such as, but not limited to, thread backtraces or register contents.
         */
        public OctetString faultRecording; // octstr

        public SoftwareFault(BigInteger id, String name, OctetString faultRecording) {
            this.id = id;
            this.name = name;
            this.faultRecording = faultRecording;
        }
    }

    public static class ThreadMetricsStruct {
        /**
         * The Id field shall be a server-assigned per-thread unique ID that is constant for the duration of the thread.
         * Efforts SHOULD be made to avoid reusing ID values when possible.
         */
        public BigInteger id; // uint64
        /**
         * The Name field shall be set to a vendor defined name or prefix of the software thread that is static for the
         * duration of the thread.
         */
        public String name; // string
        /**
         * The StackFreeCurrent field shall indicate the current amount of stack memory, in bytes, that are not being
         * utilized on the respective thread.
         */
        public Integer stackFreeCurrent; // uint32
        /**
         * The StackFreeMinimum field shall indicate the minimum amount of stack memory, in bytes, that has been
         * available at any point between the current time and this attribute being reset or initialized on the
         * respective thread. This value shall only be reset upon a Node reboot or upon receiving of the ResetWatermarks
         * command.
         */
        public Integer stackFreeMinimum; // uint32
        /**
         * The StackSize field shall indicate the amount of stack memory, in bytes, that has been allocated for use by
         * the respective thread.
         */
        public Integer stackSize; // uint32

        public ThreadMetricsStruct(BigInteger id, String name, Integer stackFreeCurrent, Integer stackFreeMinimum,
                Integer stackSize) {
            this.id = id;
            this.name = name;
            this.stackFreeCurrent = stackFreeCurrent;
            this.stackFreeMinimum = stackFreeMinimum;
            this.stackSize = stackSize;
        }
    }

    // Bitmaps
    public static class FeatureMap {
        /**
         * 
         * Node makes available the metrics for high watermark related to memory consumption.
         */
        public boolean watermarks;

        public FeatureMap(boolean watermarks) {
            this.watermarks = watermarks;
        }
    }

    public SoftwareDiagnosticsCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 52, "SoftwareDiagnostics");
    }

    protected SoftwareDiagnosticsCluster(BigInteger nodeId, int endpointId, int clusterId, String clusterName) {
        super(nodeId, endpointId, clusterId, clusterName);
    }

    // commands
    /**
     * Receipt of this command shall reset the following values which track high and lower watermarks:
     * • The StackFreeMinimum field of the ThreadMetrics attribute
     * • The CurrentHeapHighWatermark attribute This command has no payload.
     * ### Effect on Receipt
     * On receipt of this command, the Node shall make the following modifications to attributes it supports:
     * If implemented, the server shall set the value of the CurrentHeapHighWatermark attribute to the value of the
     * CurrentHeapUsed attribute.
     * If implemented, the server shall set the value of the StackFreeMinimum field for every thread to the value of the
     * corresponding thread’s StackFreeCurrent field.
     */
    public static ClusterCommand resetWatermarks() {
        return new ClusterCommand("resetWatermarks");
    }

    @Override
    public @NonNull String toString() {
        String str = "";
        str += "clusterRevision : " + clusterRevision + "\n";
        str += "featureMap : " + featureMap + "\n";
        str += "threadMetrics : " + threadMetrics + "\n";
        str += "currentHeapFree : " + currentHeapFree + "\n";
        str += "currentHeapUsed : " + currentHeapUsed + "\n";
        str += "currentHeapHighWatermark : " + currentHeapHighWatermark + "\n";
        return str;
    }
}
