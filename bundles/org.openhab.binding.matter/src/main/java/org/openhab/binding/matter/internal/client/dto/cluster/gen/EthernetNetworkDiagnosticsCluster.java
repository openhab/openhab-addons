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
 * EthernetNetworkDiagnostics
 *
 * @author Dan Cunningham - Initial contribution
 */
public class EthernetNetworkDiagnosticsCluster extends BaseCluster {

    public static final int CLUSTER_ID = 0x0037;
    public static final String CLUSTER_NAME = "EthernetNetworkDiagnostics";
    public static final String CLUSTER_PREFIX = "ethernetNetworkDiagnostics";
    public static final String ATTRIBUTE_CLUSTER_REVISION = "clusterRevision";
    public static final String ATTRIBUTE_FEATURE_MAP = "featureMap";
    public static final String ATTRIBUTE_PHY_RATE = "phyRate";
    public static final String ATTRIBUTE_FULL_DUPLEX = "fullDuplex";
    public static final String ATTRIBUTE_PACKET_RX_COUNT = "packetRxCount";
    public static final String ATTRIBUTE_PACKET_TX_COUNT = "packetTxCount";
    public static final String ATTRIBUTE_TX_ERR_COUNT = "txErrCount";
    public static final String ATTRIBUTE_COLLISION_COUNT = "collisionCount";
    public static final String ATTRIBUTE_OVERRUN_COUNT = "overrunCount";
    public static final String ATTRIBUTE_CARRIER_DETECT = "carrierDetect";
    public static final String ATTRIBUTE_TIME_SINCE_RESET = "timeSinceReset";

    public Integer clusterRevision; // 65533 ClusterRevision
    public FeatureMap featureMap; // 65532 FeatureMap
    /**
     * The PHYRate attribute shall indicate the current nominal, usable speed at the top of the physical layer of the
     * Node. A value of null shall indicate that the interface is not currently configured or operational.
     */
    public PHYRateEnum phyRate; // 0 PHYRateEnum R V
    /**
     * The FullDuplex attribute shall indicate if the Node is currently utilizing the full-duplex operating mode. A
     * value of null shall indicate that the interface is not currently configured or operational.
     */
    public Boolean fullDuplex; // 1 bool R V
    /**
     * The PacketRxCount attribute shall indicate the number of packets that have been received on the ethernet network
     * interface. The PacketRxCount attribute shall be reset to 0 upon a reboot of the Node.
     */
    public BigInteger packetRxCount; // 2 uint64 R V
    /**
     * The PacketTxCount attribute shall indicate the number of packets that have been successfully transferred on the
     * ethernet network interface. The PacketTxCount attribute shall be reset to 0 upon a reboot of the Node.
     */
    public BigInteger packetTxCount; // 3 uint64 R V
    /**
     * The TxErrCount attribute shall indicate the number of failed packet transmissions that have occurred on the
     * ethernet network interface. The TxErrCount attribute shall be reset to 0 upon a reboot of the Node.
     */
    public BigInteger txErrCount; // 4 uint64 R V
    /**
     * The CollisionCount attribute shall indicate the number of collisions that have occurred while attempting to
     * transmit a packet on the ethernet network interface. The CollisionCount attribute shall be reset to 0 upon a
     * reboot of the Node.
     */
    public BigInteger collisionCount; // 5 uint64 R V
    /**
     * The OverrunCount attribute shall indicate the number of packets dropped either at ingress or egress, due to lack
     * of buffer memory to retain all packets on the ethernet network interface. The OverrunCount attribute shall be
     * reset to 0 upon a reboot of the Node.
     */
    public BigInteger overrunCount; // 6 uint64 R V
    /**
     * The CarrierDetect attribute shall indicate the value of the Carrier Detect control signal present on the ethernet
     * network interface. A value of null shall indicate that the interface is not currently configured or operational.
     */
    public Boolean carrierDetect; // 7 bool R V
    /**
     * The TimeSinceReset attribute shall indicate the duration of time, in minutes, that it has been since the ethernet
     * network interface has reset for any reason.
     */
    public BigInteger timeSinceReset; // 8 uint64 R V

    // Enums
    public enum PHYRateEnum implements MatterEnum {
        RATE10M(0, "Rate 10 M"),
        RATE100M(1, "Rate 100 M"),
        RATE1G(2, "Rate 1 G"),
        RATE25G(3, "Rate 25 G"),
        RATE5G(4, "Rate 5 G"),
        RATE10G(5, "Rate 10 G"),
        RATE40G(6, "Rate 40 G"),
        RATE100G(7, "Rate 100 G"),
        RATE200G(8, "Rate 200 G"),
        RATE400G(9, "Rate 400 G");

        public final Integer value;
        public final String label;

        private PHYRateEnum(Integer value, String label) {
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

    // Bitmaps
    public static class FeatureMap {
        /**
         * 
         * Node makes available the counts for the number of received and transmitted packets on the ethernet interface.
         */
        public boolean packetCounts;
        /**
         * 
         * Node makes available the counts for the number of errors that have occurred during the reception and
         * transmission of packets on the ethernet interface.
         */
        public boolean errorCounts;

        public FeatureMap(boolean packetCounts, boolean errorCounts) {
            this.packetCounts = packetCounts;
            this.errorCounts = errorCounts;
        }
    }

    public EthernetNetworkDiagnosticsCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 55, "EthernetNetworkDiagnostics");
    }

    protected EthernetNetworkDiagnosticsCluster(BigInteger nodeId, int endpointId, int clusterId, String clusterName) {
        super(nodeId, endpointId, clusterId, clusterName);
    }

    // commands
    /**
     * Reception of this command shall reset the following attributes to 0:
     * • PacketRxCount
     * • PacketTxCount
     * • TxErrCount
     * • CollisionCount
     * • OverrunCount
     * This command has no associated data.
     */
    public static ClusterCommand resetCounts() {
        return new ClusterCommand("resetCounts");
    }

    @Override
    public @NonNull String toString() {
        String str = "";
        str += "clusterRevision : " + clusterRevision + "\n";
        str += "featureMap : " + featureMap + "\n";
        str += "phyRate : " + phyRate + "\n";
        str += "fullDuplex : " + fullDuplex + "\n";
        str += "packetRxCount : " + packetRxCount + "\n";
        str += "packetTxCount : " + packetTxCount + "\n";
        str += "txErrCount : " + txErrCount + "\n";
        str += "collisionCount : " + collisionCount + "\n";
        str += "overrunCount : " + overrunCount + "\n";
        str += "carrierDetect : " + carrierDetect + "\n";
        str += "timeSinceReset : " + timeSinceReset + "\n";
        return str;
    }
}
