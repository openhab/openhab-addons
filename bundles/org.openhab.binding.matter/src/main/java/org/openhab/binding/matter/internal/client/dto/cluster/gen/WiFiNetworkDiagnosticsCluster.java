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
 * WiFiNetworkDiagnostics
 *
 * @author Dan Cunningham - Initial contribution
 */
public class WiFiNetworkDiagnosticsCluster extends BaseCluster {

    public static final int CLUSTER_ID = 0x0036;
    public static final String CLUSTER_NAME = "WiFiNetworkDiagnostics";
    public static final String CLUSTER_PREFIX = "wiFiNetworkDiagnostics";
    public static final String ATTRIBUTE_CLUSTER_REVISION = "clusterRevision";
    public static final String ATTRIBUTE_FEATURE_MAP = "featureMap";
    public static final String ATTRIBUTE_BSSID = "bssid";
    public static final String ATTRIBUTE_SECURITY_TYPE = "securityType";
    public static final String ATTRIBUTE_WI_FI_VERSION = "wiFiVersion";
    public static final String ATTRIBUTE_CHANNEL_NUMBER = "channelNumber";
    public static final String ATTRIBUTE_RSSI = "rssi";
    public static final String ATTRIBUTE_BEACON_LOST_COUNT = "beaconLostCount";
    public static final String ATTRIBUTE_BEACON_RX_COUNT = "beaconRxCount";
    public static final String ATTRIBUTE_PACKET_MULTICAST_RX_COUNT = "packetMulticastRxCount";
    public static final String ATTRIBUTE_PACKET_MULTICAST_TX_COUNT = "packetMulticastTxCount";
    public static final String ATTRIBUTE_PACKET_UNICAST_RX_COUNT = "packetUnicastRxCount";
    public static final String ATTRIBUTE_PACKET_UNICAST_TX_COUNT = "packetUnicastTxCount";
    public static final String ATTRIBUTE_CURRENT_MAX_RATE = "currentMaxRate";
    public static final String ATTRIBUTE_OVERRUN_COUNT = "overrunCount";

    public Integer clusterRevision; // 65533 ClusterRevision
    public FeatureMap featureMap; // 65532 FeatureMap
    /**
     * The BSSID attribute shall indicate the BSSID for which the Wi-Fi network the Node is currently connected.
     */
    public OctetString bssid; // 0 octstr R V
    /**
     * The SecurityType attribute shall indicate the current type of Wi-Fi security used.
     */
    public SecurityTypeEnum securityType; // 1 SecurityTypeEnum R V
    /**
     * The WiFiVersion attribute shall indicate the current 802.11 standard version in use by the Node, per the table
     * below.
     */
    public WiFiVersionEnum wiFiVersion; // 2 WiFiVersionEnum R V
    /**
     * The ChannelNumber attribute shall indicate the channel that Wi-Fi communication is currently operating on.
     */
    public Integer channelNumber; // 3 uint16 R V
    /**
     * The RSSI attribute shall indicate the current RSSI of the Node’s Wi-Fi radio in dBm.
     */
    public Integer rssi; // 4 int8 R V
    /**
     * The BeaconLostCount attribute shall indicate the count of the number of missed beacons the Node has detected. If
     * the Node does not have an ability to count beacons expected and not received, this value may remain set to zero.
     */
    public Integer beaconLostCount; // 5 uint32 R V
    /**
     * The BeaconRxCount attribute shall indicate the count of the number of received beacons. The total number of
     * expected beacons that could have been received during the interval since association SHOULD match the sum of
     * BeaconRxCount and BeaconLostCount. If the Node does not have an ability to report count of beacons received, this
     * value may remain set to zero.
     */
    public Integer beaconRxCount; // 6 uint32 R V
    /**
     * The PacketMulticastRxCount attribute shall indicate the number of multicast packets received by the Node.
     */
    public Integer packetMulticastRxCount; // 7 uint32 R V
    /**
     * The PacketMulticastTxCount attribute shall indicate the number of multicast packets transmitted by the Node.
     */
    public Integer packetMulticastTxCount; // 8 uint32 R V
    /**
     * The PacketUnicastRxCount attribute shall indicate the number of unicast packets received by the Node.
     */
    public Integer packetUnicastRxCount; // 9 uint32 R V
    /**
     * The PacketUnicastTxCount attribute shall indicate the number of unicast packets transmitted by the Node.
     */
    public Integer packetUnicastTxCount; // 10 uint32 R V
    /**
     * The CurrentMaxRate attribute shall indicate the current maximum PHY rate of transfer of data in bits-per-second.
     */
    public BigInteger currentMaxRate; // 11 uint64 R V
    /**
     * The OverrunCount attribute shall indicate the number of packets dropped either at ingress or egress, due to lack
     * of buffer memory to retain all packets on the network interface. The OverrunCount attribute shall be reset to 0
     * upon a reboot of the Node.
     */
    public BigInteger overrunCount; // 12 uint64 R V

    // Structs
    /**
     * The Disconnection Event shall indicate that a Node’s Wi-Fi connection has been disconnected as a result of
     * de-authenticated or dis-association and indicates the reason.
     */
    public static class Disconnection {
        /**
         * This field shall contain the Reason Code field value for the Disassociation or Deauthentication event that
         * caused the disconnection and the value shall align with Table 9-49 &quot;Reason codes&quot; of IEEE
         * 802.11-2020.
         */
        public Integer reasonCode; // uint16

        public Disconnection(Integer reasonCode) {
            this.reasonCode = reasonCode;
        }
    }

    /**
     * The AssociationFailure event shall indicate that a Node has attempted to connect, or reconnect, to a Wi-Fi access
     * point, but is unable to successfully associate or authenticate, after exhausting all internal retries of its
     * supplicant.
     */
    public static class AssociationFailure {
        /**
         * The Status field shall be set to a value from the AssociationFailureCauseEnum.
         */
        public AssociationFailureCauseEnum associationFailureCause; // AssociationFailureCauseEnum
        /**
         * The Status field shall be set to the Status Code value that was present in the last frame related to
         * association where Status Code was not equal to zero and which caused the failure of a last trial attempt, if
         * this last failure was due to one of the following Management frames:
         * • Association Response (Type 0, Subtype 1)
         * • Reassociation Response (Type 0, Subtype 3)
         * • Authentication (Type 0, Subtype 11)
         * Table 9-50 &quot;Status codes&quot; of IEEE 802.11-2020 contains a description of all values possible.
         */
        public Integer status; // uint16

        public AssociationFailure(AssociationFailureCauseEnum associationFailureCause, Integer status) {
            this.associationFailureCause = associationFailureCause;
            this.status = status;
        }
    }

    /**
     * The ConnectionStatus Event shall indicate that a Node’s connection status to a Wi-Fi network has changed.
     * Connected, in this context, shall mean that a Node acting as a Wi-Fi station is successfully associated to a
     * Wi-Fi Access Point.
     */
    public static class ConnectionStatus {
        public ConnectionStatusEnum connectionStatus; // ConnectionStatusEnum

        public ConnectionStatus(ConnectionStatusEnum connectionStatus) {
            this.connectionStatus = connectionStatus;
        }
    }

    // Enums
    public enum SecurityTypeEnum implements MatterEnum {
        UNSPECIFIED(0, "Unspecified"),
        NONE(1, "None"),
        WEP(2, "Wep"),
        WPA(3, "Wpa"),
        WPA2(4, "Wpa 2"),
        WPA3(5, "Wpa 3");

        public final Integer value;
        public final String label;

        private SecurityTypeEnum(Integer value, String label) {
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

    public enum WiFiVersionEnum implements MatterEnum {
        A(0, "A"),
        B(1, "B"),
        G(2, "G"),
        N(3, "N"),
        AC(4, "Ac"),
        AX(5, "Ax"),
        AH(6, "Ah");

        public final Integer value;
        public final String label;

        private WiFiVersionEnum(Integer value, String label) {
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

    public enum AssociationFailureCauseEnum implements MatterEnum {
        UNKNOWN(0, "Unknown"),
        ASSOCIATION_FAILED(1, "Association Failed"),
        AUTHENTICATION_FAILED(2, "Authentication Failed"),
        SSID_NOT_FOUND(3, "Ssid Not Found");

        public final Integer value;
        public final String label;

        private AssociationFailureCauseEnum(Integer value, String label) {
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

    public enum ConnectionStatusEnum implements MatterEnum {
        CONNECTED(0, "Connected"),
        NOT_CONNECTED(1, "Not Connected");

        public final Integer value;
        public final String label;

        private ConnectionStatusEnum(Integer value, String label) {
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
         * Node makes available the counts for the number of received and transmitted packets on the Wi-Fi interface.
         */
        public boolean packetCounts;
        /**
         * 
         * Node makes available the counts for the number of errors that have occurred during the reception and
         * transmission of packets on the Wi-Fi interface.
         */
        public boolean errorCounts;

        public FeatureMap(boolean packetCounts, boolean errorCounts) {
            this.packetCounts = packetCounts;
            this.errorCounts = errorCounts;
        }
    }

    public WiFiNetworkDiagnosticsCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 54, "WiFiNetworkDiagnostics");
    }

    protected WiFiNetworkDiagnosticsCluster(BigInteger nodeId, int endpointId, int clusterId, String clusterName) {
        super(nodeId, endpointId, clusterId, clusterName);
    }

    // commands
    /**
     * Reception of this command shall reset the following attributes to 0:
     * • BeaconLostCount
     * • BeaconRxCount
     * • PacketMulticastRxCount
     * • PacketMulticastTxCount
     * • PacketUnicastRxCount
     * • PacketUnicastTxCount
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
        str += "bssid : " + bssid + "\n";
        str += "securityType : " + securityType + "\n";
        str += "wiFiVersion : " + wiFiVersion + "\n";
        str += "channelNumber : " + channelNumber + "\n";
        str += "rssi : " + rssi + "\n";
        str += "beaconLostCount : " + beaconLostCount + "\n";
        str += "beaconRxCount : " + beaconRxCount + "\n";
        str += "packetMulticastRxCount : " + packetMulticastRxCount + "\n";
        str += "packetMulticastTxCount : " + packetMulticastTxCount + "\n";
        str += "packetUnicastRxCount : " + packetUnicastRxCount + "\n";
        str += "packetUnicastTxCount : " + packetUnicastTxCount + "\n";
        str += "currentMaxRate : " + currentMaxRate + "\n";
        str += "overrunCount : " + overrunCount + "\n";
        return str;
    }
}
