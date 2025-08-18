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
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.binding.matter.internal.client.dto.cluster.ClusterCommand;

/**
 * NetworkCommissioning
 *
 * @author Dan Cunningham - Initial contribution
 */
public class NetworkCommissioningCluster extends BaseCluster {

    public static final int CLUSTER_ID = 0x0031;
    public static final String CLUSTER_NAME = "NetworkCommissioning";
    public static final String CLUSTER_PREFIX = "networkCommissioning";
    public static final String ATTRIBUTE_CLUSTER_REVISION = "clusterRevision";
    public static final String ATTRIBUTE_FEATURE_MAP = "featureMap";
    public static final String ATTRIBUTE_MAX_NETWORKS = "maxNetworks";
    public static final String ATTRIBUTE_NETWORKS = "networks";
    public static final String ATTRIBUTE_SCAN_MAX_TIME_SECONDS = "scanMaxTimeSeconds";
    public static final String ATTRIBUTE_CONNECT_MAX_TIME_SECONDS = "connectMaxTimeSeconds";
    public static final String ATTRIBUTE_INTERFACE_ENABLED = "interfaceEnabled";
    public static final String ATTRIBUTE_LAST_NETWORKING_STATUS = "lastNetworkingStatus";
    public static final String ATTRIBUTE_LAST_NETWORK_ID = "lastNetworkId";
    public static final String ATTRIBUTE_LAST_CONNECT_ERROR_VALUE = "lastConnectErrorValue";
    public static final String ATTRIBUTE_SUPPORTED_WI_FI_BANDS = "supportedWiFiBands";
    public static final String ATTRIBUTE_SUPPORTED_THREAD_FEATURES = "supportedThreadFeatures";
    public static final String ATTRIBUTE_THREAD_VERSION = "threadVersion";

    public Integer clusterRevision; // 65533 ClusterRevision
    public FeatureMap featureMap; // 65532 FeatureMap
    /**
     * This shall indicate the maximum number of network configuration entries that can be added, based on available
     * device resources. The length of the Networks attribute shall be less than or equal to this value.
     */
    public Integer maxNetworks; // 0 uint8 R A
    /**
     * Indicates the network configurations that are usable on the network interface represented by this cluster server
     * instance.
     * The order of configurations in the list reflects precedence. That is, any time the Node attempts to connect to
     * the network it shall attempt to do so using the configurations in Networks Attribute in the order as they appear
     * in the list.
     * The order of list items shall only be modified by the AddOrUpdateThreadNetwork, AddOrUpdateWiFiNetwork and
     * ReorderNetwork commands. In other words, the list shall be stable over time, unless mutated externally.
     * Ethernet networks shall be automatically populated by the cluster server. Ethernet Network Commissioning Cluster
     * instances shall always have exactly one NetworkInfoStruct instance in their Networks attribute. There shall be no
     * way to add, update or remove Ethernet network configurations to those Cluster instances.
     */
    public List<NetworkInfoStruct> networks; // 1 list R A
    /**
     * Indicates the maximum duration taken, in seconds, by the network interface on this cluster server instance to
     * provide scan results.
     * See ScanNetworks for usage.
     */
    public Integer scanMaxTimeSeconds; // 2 uint8 R V
    /**
     * Indicates the maximum duration taken, in seconds, by the network interface on this cluster server instance to
     * report a successful or failed network connection indication. This maximum time shall account for all operations
     * needed until a successful network connection is deemed to have occurred, including, for example, obtaining IP
     * addresses, or the execution of necessary internal retries.
     */
    public Integer connectMaxTimeSeconds; // 3 uint8 R V
    /**
     * Indicates whether the associated network interface is enabled or not. By default all network interfaces SHOULD be
     * enabled during initial commissioning (InterfaceEnabled set to true).
     * It is undefined what happens if InterfaceEnabled is written to false on the same interface as that which is used
     * to write the value. In that case, it is possible that the Administrator would have to await expiry of the
     * fail-safe, and associated recovery of network configuration to prior safe values, before being able to
     * communicate with the node again (see ArmFailSafe).
     * It may be possible to disable Ethernet interfaces but it is implementation-defined. If not supported, a write to
     * this attribute with a value of false shall fail with a status of INVALID_ACTION. When disabled, an Ethernet
     * interface would longer employ media detection. That is, a simple unplug and replug of the cable shall NOT
     * re-enable the interface.
     * On Ethernet-only Nodes, there shall always be at least one of the Network Commissioning server cluster instances
     * with InterfaceEnabled set to true.
     */
    public Boolean interfaceEnabled; // 4 bool RW VA
    /**
     * Indicates the status of the last attempt either scan or connect to an operational network, using this interface,
     * whether by invocation of the ConnectNetwork command or by autonomous connection after loss of connectivity or
     * during initial establishment. If no such attempt was made, or no network configurations exist in the Networks
     * attribute, then this attribute shall be set to null.
     * This attribute is present to assist with error recovery during Network commissioning and to assist in
     * non-concurrent networking commissioning flows.
     */
    public NetworkCommissioningStatusEnum lastNetworkingStatus; // 5 NetworkCommissioningStatusEnum R A
    /**
     * Indicates the NetworkID used in the last attempt to connect to an operational network, using this interface,
     * whether by invocation of the ConnectNetwork command or by autonomous connection after loss of connectivity or
     * during initial establishment. If no such attempt was made, or no network configurations exist in the Networks
     * attribute, then this attribute shall be set to null.
     * If a network configuration is removed from the Networks attribute using the RemoveNetwork command after a
     * connection attempt, this field may indicate a NetworkID that is no longer configured on the Node.
     * This attribute is present to assist with error recovery during Network commissioning and to assist in
     * non-concurrent networking commissioning flows.
     */
    public OctetString lastNetworkId; // 6 octstr R A
    /**
     * Indicates the ErrorValue used in the last failed attempt to connect to an operational network, using this
     * interface, whether by invocation of the ConnectNetwork command or by autonomous connection after loss of
     * connectivity or during initial establishment. If no such attempt was made, or no network configurations exist in
     * the Networks attribute, then this attribute shall be set to null.
     * If the last connection succeeded, as indicated by a value of Success in the LastNetworkingStatus attribute, then
     * this field shall be set to null.
     * This attribute is present to assist with error recovery during Network commissioning and to assist in
     * non-concurrent networking commissioning flows.
     */
    public Integer lastConnectErrorValue; // 7 int32 R A
    /**
     * Indicates all the frequency bands supported by the Wi-Fi interface configured by the cluster instance.
     */
    public List<WiFiBandEnum> supportedWiFiBands; // 8 list R V
    /**
     * Indicates all of the Thread features supported by the Thread interface configured by the cluster instance.
     * This attribute is primarily used to determine the most important general capabilities of the Thread interface
     * associated with the cluster instance, as opposed to the current runtime dynamic configuration. Note that most
     * run-time details of the actual Thread interface are found in the Thread Network Diagnostics cluster, if
     * supported.
     */
    public ThreadCapabilitiesBitmap supportedThreadFeatures; // 9 ThreadCapabilitiesBitmap R V
    /**
     * Indicates the Thread version supported by the Thread interface configured by the cluster instance.
     * The format shall match the value mapping found in the &quot;Version TLV&quot; section of Thread specification.
     * For example, Thread 1.3.0 would have ThreadVersion set to 4.
     */
    public Integer threadVersion; // 10 uint16 R V

    // Structs
    /**
     * NetworkInfoStruct struct describes an existing network configuration, as provided in the Networks attribute.
     */
    public static class NetworkInfoStruct {
        /**
         * Every network is uniquely identified (for purposes of commissioning) by a NetworkID mapping to the following
         * technology-specific properties:
         * • SSID for Wi-Fi
         * • Extended PAN ID for Thread
         * • Network interface instance name at operating system (or equivalent unique name) for Ethernet.
         * The semantics of the NetworkID field therefore varies between network types accordingly. It contains SSID for
         * Wi-Fi networks, Extended PAN ID (XPAN ID) for Thread networks and netif name for Ethernet networks.
         * &gt; [!NOTE]
         * &gt; SSID in Wi-Fi is a collection of 1-32 bytes, the text encoding of which is not specified.
         * Implementations must be careful to support reporting byte strings without requiring a particular encoding for
         * transfer. Only the commissioner should try to potentially decode the bytes. The most common encoding is
         * UTF-8, however this is just a convention. Some configurations may use Latin-1 or other character sets. A
         * commissioner may decode using UTF-8, replacing encoding errors with &quot;?&quot; at the application level
         * while retaining the underlying representation.
         * XPAN ID is a big-endian 64-bit unsigned number, represented on the first 8 octets of the octet string.
         */
        public OctetString networkId; // octstr
        /**
         * This field shall indicate the connected status of the associated network, where &quot;connected&quot; means
         * currently linked to the network technology (e.g. Associated for a Wi-Fi network, media connected for an
         * Ethernet network).
         */
        public Boolean connected; // bool

        public NetworkInfoStruct(OctetString networkId, Boolean connected) {
            this.networkId = networkId;
            this.connected = connected;
        }
    }

    /**
     * WiFiInterfaceScanResultStruct represents a single Wi-Fi network scan result.
     */
    public static class WiFiInterfaceScanResultStruct {
        public WiFiSecurityBitmap security; // WiFiSecurityBitmap
        public OctetString ssid; // octstr
        public OctetString bssid; // octstr
        public Integer channel; // uint16
        /**
         * This field, if present, may be used to differentiate overlapping channel number values across different Wi-Fi
         * frequency bands.
         */
        public WiFiBandEnum wiFiBand; // WiFiBandEnum
        /**
         * This field, if present, shall denote the signal strength in dBm of the associated scan result.
         */
        public Integer rssi; // int8

        public WiFiInterfaceScanResultStruct(WiFiSecurityBitmap security, OctetString ssid, OctetString bssid,
                Integer channel, WiFiBandEnum wiFiBand, Integer rssi) {
            this.security = security;
            this.ssid = ssid;
            this.bssid = bssid;
            this.channel = channel;
            this.wiFiBand = wiFiBand;
            this.rssi = rssi;
        }
    }

    /**
     * ThreadInterfaceScanResultStruct represents a single Thread network scan result.
     */
    public static class ThreadInterfaceScanResultStruct {
        public Integer panId; // uint16
        public BigInteger extendedPanId; // uint64
        public String networkName; // string
        public Integer channel; // uint16
        public Integer version; // uint8
        /**
         * ExtendedAddress stands for an IEEE 802.15.4 Extended Address.
         */
        public OctetString extendedAddress; // hwadr
        public Integer rssi; // int8
        public Integer lqi; // uint8

        public ThreadInterfaceScanResultStruct(Integer panId, BigInteger extendedPanId, String networkName,
                Integer channel, Integer version, OctetString extendedAddress, Integer rssi, Integer lqi) {
            this.panId = panId;
            this.extendedPanId = extendedPanId;
            this.networkName = networkName;
            this.channel = channel;
            this.version = version;
            this.extendedAddress = extendedAddress;
            this.rssi = rssi;
            this.lqi = lqi;
        }
    }

    // Enums
    /**
     * WiFiBandEnum encodes a supported Wi-Fi frequency band present in the WiFiBand field of the
     * WiFiInterfaceScanResultStruct.
     */
    public enum WiFiBandEnum implements MatterEnum {
        V2G4(0, "2 G 4"),
        V3G65(1, "3 G 65"),
        V5G(2, "5 G"),
        V6G(3, "6 G"),
        V60G(4, "60 G"),
        V1G(5, "1 G");

        public final Integer value;
        public final String label;

        private WiFiBandEnum(Integer value, String label) {
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

    public enum NetworkCommissioningStatusEnum implements MatterEnum {
        SUCCESS(0, "Success"),
        OUT_OF_RANGE(1, "Out Of Range"),
        BOUNDS_EXCEEDED(2, "Bounds Exceeded"),
        NETWORK_ID_NOT_FOUND(3, "Network Id Not Found"),
        DUPLICATE_NETWORK_ID(4, "Duplicate Network Id"),
        NETWORK_NOT_FOUND(5, "Network Not Found"),
        REGULATORY_ERROR(6, "Regulatory Error"),
        AUTH_FAILURE(7, "Auth Failure"),
        UNSUPPORTED_SECURITY(8, "Unsupported Security"),
        OTHER_CONNECTION_FAILURE(9, "Other Connection Failure"),
        IPV6FAILED(10, "Ipv 6 Failed"),
        IP_BIND_FAILED(11, "Ip Bind Failed"),
        UNKNOWN_ERROR(12, "Unknown Error");

        public final Integer value;
        public final String label;

        private NetworkCommissioningStatusEnum(Integer value, String label) {
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
    /**
     * WiFiSecurityBitmap encodes the supported Wi-Fi security types present in the Security field of the
     * WiFiInterfaceScanResultStruct.
     */
    public static class WiFiSecurityBitmap {
        public boolean unencrypted;
        public boolean wep;
        public boolean wpaPersonal;
        public boolean wpa2Personal;
        public boolean wpa3Personal;

        public WiFiSecurityBitmap(boolean unencrypted, boolean wep, boolean wpaPersonal, boolean wpa2Personal,
                boolean wpa3Personal) {
            this.unencrypted = unencrypted;
            this.wep = wep;
            this.wpaPersonal = wpaPersonal;
            this.wpa2Personal = wpa2Personal;
            this.wpa3Personal = wpa3Personal;
        }
    }

    /**
     * The ThreadCapabilitiesBitmap encodes the supported Thread features and capabilities of a Thread-enabled network
     * interface.
     * &gt; [!NOTE]
     * &gt; The valid combinations of capabilities are restricted and dependent on Thread version.
     */
    public static class ThreadCapabilitiesBitmap {
        public boolean isBorderRouterCapable;
        public boolean isRouterCapable;
        public boolean isSleepyEndDeviceCapable;
        public boolean isFullThreadDevice;
        public boolean isSynchronizedSleepyEndDeviceCapable;

        public ThreadCapabilitiesBitmap(boolean isBorderRouterCapable, boolean isRouterCapable,
                boolean isSleepyEndDeviceCapable, boolean isFullThreadDevice,
                boolean isSynchronizedSleepyEndDeviceCapable) {
            this.isBorderRouterCapable = isBorderRouterCapable;
            this.isRouterCapable = isRouterCapable;
            this.isSleepyEndDeviceCapable = isSleepyEndDeviceCapable;
            this.isFullThreadDevice = isFullThreadDevice;
            this.isSynchronizedSleepyEndDeviceCapable = isSynchronizedSleepyEndDeviceCapable;
        }
    }

    public static class FeatureMap {
        /**
         * 
         * Wi-Fi related features
         */
        public boolean wiFiNetworkInterface;
        /**
         * 
         * Thread related features
         */
        public boolean threadNetworkInterface;
        /**
         * 
         * Ethernet related features
         */
        public boolean ethernetNetworkInterface;

        public FeatureMap(boolean wiFiNetworkInterface, boolean threadNetworkInterface,
                boolean ethernetNetworkInterface) {
            this.wiFiNetworkInterface = wiFiNetworkInterface;
            this.threadNetworkInterface = threadNetworkInterface;
            this.ethernetNetworkInterface = ethernetNetworkInterface;
        }
    }

    public NetworkCommissioningCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 49, "NetworkCommissioning");
    }

    protected NetworkCommissioningCluster(BigInteger nodeId, int endpointId, int clusterId, String clusterName) {
        super(nodeId, endpointId, clusterId, clusterName);
    }

    // commands
    /**
     * This command shall scan on the Cluster instance’s associated network interface for either of:
     * • All available networks (non-directed scanning)
     * • Specific networks (directed scanning)
     * Scanning for available networks detects all networks of the type corresponding to the cluster server instance’s
     * associated network interface that are possible to join, such as all visible Wi-Fi access points for Wi-Fi cluster
     * server instances, all Thread PANs for Thread cluster server instances, within bounds of the maximum response
     * size.
     * Scanning for a specific network (i.e. directed scanning) takes place if a network identifier (e.g. Wi-Fi SSID) is
     * provided in the command arguments. Directed scanning shall restrict the result set to the specified network only.
     * If this command is received without an armed fail-safe context (see ArmFailSafe), then this command shall fail
     * with a FAILSAFE_REQUIRED status code sent back to the initiator.
     * The client shall NOT expect the server to be done scanning and have responded with ScanNetworksResponse before
     * ScanMaxTimeSeconds seconds have elapsed. Enough transport time affordances for retries SHOULD be expected before
     * a client determines the operation to have timed-out.
     * This command shall fail with a status code of BUSY if the server determines that it will fail to reliably send a
     * response due to changes of networking interface configuration at runtime for the interface over which the command
     * was invoked, or if it is currently unable to proceed with such an operation.
     * For Wi-Fi-supporting servers (WI feature) the server shall always honor directed scans, and attempt to provide
     * all matching BSSID which are reachable on the bands which would otherwise be attempted if a ConnectNetwork having
     * the specified SSID were to take place. This command is useful for clients to determine reachability capabilities
     * as seen by the server’s own radios.
     * For Wi-Fi-supporting servers the server shall always scan on all bands supported by the interface associated with
     * the cluster instance on which the command was invoked.
     * If the command was invoked over the same link whose configuration is managed by a given server cluster instance,
     * there may be an impact on other communication from the invoking client, as well as other clients, while the
     * network interface is processing the scan. Clients SHOULD NOT use this command unless actively in the process of
     * re-configuring network connectivity.
     */
    public static ClusterCommand scanNetworks(OctetString ssid, BigInteger breadcrumb) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (ssid != null) {
            map.put("ssid", ssid);
        }
        if (breadcrumb != null) {
            map.put("breadcrumb", breadcrumb);
        }
        return new ClusterCommand("scanNetworks", map);
    }

    /**
     * This command shall be used to add or modify Wi-Fi network configurations.
     * If this command is received without an armed fail-safe context (see ArmFailSafe), then this command shall fail
     * with a FAILSAFE_REQUIRED status code sent back to the initiator.
     * The Credentials associated with the network are not readable after execution of this command, as they do not
     * appear in the Networks attribute, for security reasons.
     * If this command contains a ClientIdentifier, and the Networks list does not contain an entry with a matching
     * ClientIdentifier, then this command shall fail with a status of NOT_FOUND.
     * See Section 11.9.7.5, “Common processing of AddOrUpdateWiFiNetwork and AddOrUpdateThreadNetwork” for behavior of
     * addition/update.
     */
    public static ClusterCommand addOrUpdateWiFiNetwork(OctetString ssid, OctetString credentials,
            BigInteger breadcrumb) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (ssid != null) {
            map.put("ssid", ssid);
        }
        if (credentials != null) {
            map.put("credentials", credentials);
        }
        if (breadcrumb != null) {
            map.put("breadcrumb", breadcrumb);
        }
        return new ClusterCommand("addOrUpdateWiFiNetwork", map);
    }

    /**
     * This command shall be used to add or modify Thread network configurations.
     * If this command is received without an armed fail-safe context (see ArmFailSafe), then this command shall fail
     * with a FAILSAFE_REQUIRED status code sent back to the initiator.
     * See Section 11.9.7.5, “Common processing of AddOrUpdateWiFiNetwork and AddOrUpdateThreadNetwork” for behavior of
     * addition/update.
     * The XPAN ID in the OperationalDataset serves as the NetworkID for the network configuration to be added or
     * updated.
     * If the Networks attribute does not contain an entry with the same NetworkID as the one provided in the
     * OperationalDataset, the operation shall be considered an addition, otherwise, it shall be considered an update.
     */
    public static ClusterCommand addOrUpdateThreadNetwork(OctetString operationalDataset, BigInteger breadcrumb) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (operationalDataset != null) {
            map.put("operationalDataset", operationalDataset);
        }
        if (breadcrumb != null) {
            map.put("breadcrumb", breadcrumb);
        }
        return new ClusterCommand("addOrUpdateThreadNetwork", map);
    }

    /**
     * This command shall remove the network configuration from the Cluster if there was already a network configuration
     * with the same NetworkID. The relative order of the entries in the Networks attribute shall remain unchanged,
     * except for the removal of the requested network configuration.
     * If this command is received without an armed fail-safe context (see ArmFailSafe), then this command shall fail
     * with a FAILSAFE_REQUIRED status code sent back to the initiator.
     * If the Networks attribute does not contain a matching entry, the command shall immediately respond with
     * NetworkConfigResponse having NetworkingStatus status field set to NetworkIdNotFound.
     * On success, the NetworkConfigResponse command shall have its NetworkIndex field set to the 0- based index of the
     * entry in the Networks attribute that was just removed, and a NetworkingStatus status field set to Success.
     */
    public static ClusterCommand removeNetwork(OctetString networkId, BigInteger breadcrumb) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (networkId != null) {
            map.put("networkId", networkId);
        }
        if (breadcrumb != null) {
            map.put("breadcrumb", breadcrumb);
        }
        return new ClusterCommand("removeNetwork", map);
    }

    /**
     * This command shall attempt to connect to a network whose configuration was previously added by either the
     * AddOrUpdateWiFiNetwork or AddOrUpdateThreadNetwork commands. Network is identified by its NetworkID.
     * This command shall fail with a BUSY status code returned to the initiator if the server is currently unable to
     * proceed with such an operation, such as if it is currently attempting to connect in the background, or is already
     * proceeding with a prior ConnectNetwork.
     * If this command is received without an armed fail-safe context (see ArmFailSafe), then this command shall fail
     * with a FAILSAFE_REQUIRED status code sent back to the initiator.
     * Before connecting to the new network, the Node shall disconnect the operational network connections managed by
     * any other Network Commissioning cluster instances (whether under the Root Node or a Secondary Network Interface),
     * where those connections are not represented by an entry in the Networks attribute of the corresponding cluster
     * instance. This ensures that an Administrator or Commissioner can reliably reconfigure the operational network
     * connection of a device that has one or more Secondary Network interfaces, for example by removing the active
     * network configuration from one cluster instance, followed by adding a new configuration and calling
     * ConnectNetwork on a different cluster instance.
     * Success or failure of this command shall be communicated by the ConnectNetworkResponse command, unless some data
     * model validations caused a FAILURE status to be sent prior to finishing execution of the command. The
     * ConnectNetworkResponse shall indicate the value Success in the NetworkingStatus field on successful connection.
     * On failure to connect, the ConnectNetworkResponse shall contain an appropriate NetworkingStatus, DebugText and
     * ErrorValue indicating the reason for failure.
     * The amount of time needed to determine successful or failing connectivity on the cluster server’s associated
     * interface is provided by the ConnectMaxTimeSeconds attribute. Clients shall NOT consider the connection to have
     * timed-out until at least that duration has taken place. For non-concurrent commissioning situations, the client
     * SHOULD allow additional margin of time to account for its delay in executing operational discovery of the Node
     * once it is connected to the new network.
     * On successful connection, the entry associated with the given Network configuration in the Networks attribute
     * shall indicate its Connected field set to true, and all other entries, if any exist, shall indicate their
     * Connected field set to false.
     * On failure to connect, the entry associated with the given Network configuration in the Networks attribute shall
     * indicate its Connected field set to false.
     * The precedence order of any entry subject to ConnectNetwork shall NOT change within the Networks attribute.
     * Even after successfully connecting to a network, the configuration shall revert to the prior state of
     * configuration if the CommissioningComplete command (see CommissioningComplete) is not successfully invoked before
     * expiry of the Fail-Safe timer.
     * When non-concurrent commissioning is being used by a Commissioner or Administrator, the ConnectNetworkResponse
     * shall be sent with the NetworkingStatus field set to Success prior to closing the commissioning channel, even if
     * not yet connected to the operational network, unless the device would be incapable of joining that network, in
     * which case the usual failure path described in the prior paragraphs shall be followed. Once the commissioning
     * channel is closed, the operational channel will be started. It is possible that the only method to determine
     * success of the operation is operational discovery of the Node on the new operational network. Therefore, before
     * invoking the ConnectNetwork command, the client SHOULD re-invoke the Arm Fail-Safe command with a duration that
     * meets the following:
     * 1. Sufficient time to meet the minimum required time (see ConnectMaxTimeSeconds) that may be taken by the server
     * to connect to the desired network.
     * 2. Sufficient time to account for possible message-layer retries when a response is requested.
     * 3. Sufficient time to allow operational discovery on the new network by a Commissioner or Administrator.
     * 4. Sufficient time to establish a CASE session after operational discovery
     * 5. Not so long that, in error situations, the delay to reverting back to being discoverable for commissioning
     * with a previous configuration would cause significant user-perceived delay.
     * Note as well that the CommissioningTimeout duration provided in a prior OpenCommissioningWindow or
     * OpenBasicCommissioningWindow command may impact the total time available to proceed with error recovery after a
     * connection failure.
     * The LastNetworkingStatus, LastNetworkID and LastConnectErrorValue attributes may assist the client in determining
     * the reason for a failure after reconnecting over a Commissioning channel, especially in non-concurrent
     * commissioning situations.
     */
    public static ClusterCommand connectNetwork(OctetString networkId, BigInteger breadcrumb) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (networkId != null) {
            map.put("networkId", networkId);
        }
        if (breadcrumb != null) {
            map.put("breadcrumb", breadcrumb);
        }
        return new ClusterCommand("connectNetwork", map);
    }

    /**
     * This command shall set the specific order of the network configuration selected by its NetworkID in the Networks
     * attribute to match the position given by NetworkIndex.
     */
    public static ClusterCommand reorderNetwork(OctetString networkId, Integer networkIndex, BigInteger breadcrumb) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (networkId != null) {
            map.put("networkId", networkId);
        }
        if (networkIndex != null) {
            map.put("networkIndex", networkIndex);
        }
        if (breadcrumb != null) {
            map.put("breadcrumb", breadcrumb);
        }
        return new ClusterCommand("reorderNetwork", map);
    }

    @Override
    public @NonNull String toString() {
        String str = "";
        str += "clusterRevision : " + clusterRevision + "\n";
        str += "featureMap : " + featureMap + "\n";
        str += "maxNetworks : " + maxNetworks + "\n";
        str += "networks : " + networks + "\n";
        str += "scanMaxTimeSeconds : " + scanMaxTimeSeconds + "\n";
        str += "connectMaxTimeSeconds : " + connectMaxTimeSeconds + "\n";
        str += "interfaceEnabled : " + interfaceEnabled + "\n";
        str += "lastNetworkingStatus : " + lastNetworkingStatus + "\n";
        str += "lastNetworkId : " + lastNetworkId + "\n";
        str += "lastConnectErrorValue : " + lastConnectErrorValue + "\n";
        str += "supportedWiFiBands : " + supportedWiFiBands + "\n";
        str += "supportedThreadFeatures : " + supportedThreadFeatures + "\n";
        str += "threadVersion : " + threadVersion + "\n";
        return str;
    }
}
