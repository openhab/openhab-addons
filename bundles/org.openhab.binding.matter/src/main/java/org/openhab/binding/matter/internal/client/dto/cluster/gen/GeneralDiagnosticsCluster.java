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
 * GeneralDiagnostics
 *
 * @author Dan Cunningham - Initial contribution
 */
public class GeneralDiagnosticsCluster extends BaseCluster {

    public static final int CLUSTER_ID = 0x0033;
    public static final String CLUSTER_NAME = "GeneralDiagnostics";
    public static final String CLUSTER_PREFIX = "generalDiagnostics";
    public static final String ATTRIBUTE_CLUSTER_REVISION = "clusterRevision";
    public static final String ATTRIBUTE_FEATURE_MAP = "featureMap";
    public static final String ATTRIBUTE_NETWORK_INTERFACES = "networkInterfaces";
    public static final String ATTRIBUTE_REBOOT_COUNT = "rebootCount";
    public static final String ATTRIBUTE_UP_TIME = "upTime";
    public static final String ATTRIBUTE_TOTAL_OPERATIONAL_HOURS = "totalOperationalHours";
    public static final String ATTRIBUTE_BOOT_REASON = "bootReason";
    public static final String ATTRIBUTE_ACTIVE_HARDWARE_FAULTS = "activeHardwareFaults";
    public static final String ATTRIBUTE_ACTIVE_RADIO_FAULTS = "activeRadioFaults";
    public static final String ATTRIBUTE_ACTIVE_NETWORK_FAULTS = "activeNetworkFaults";
    public static final String ATTRIBUTE_TEST_EVENT_TRIGGERS_ENABLED = "testEventTriggersEnabled";

    public Integer clusterRevision; // 65533 ClusterRevision
    public FeatureMap featureMap; // 65532 FeatureMap
    /**
     * The NetworkInterfaces attribute shall be a list of NetworkInterface structs. Each logical network interface on
     * the Node shall be represented by a single entry within the NetworkInterfaces attribute.
     */
    public List<NetworkInterface> networkInterfaces; // 0 list R V
    /**
     * The RebootCount attribute shall indicate a best-effort count of the number of times the Node has rebooted. The
     * RebootCount attribute SHOULD be incremented each time the Node reboots. The RebootCount attribute shall NOT be
     * incremented when a Node wakes from a low-power or sleep state. The RebootCount attribute shall only be reset to 0
     * upon a factory reset of the Node.
     */
    public Integer rebootCount; // 1 uint16 R V
    /**
     * The UpTime attribute shall indicate a best-effort assessment of the length of time, in seconds, since the Node’s
     * last reboot. This attribute SHOULD be incremented to account for the periods of time that a Node is in a
     * low-power or sleep state. This attribute shall only be reset upon a device reboot. This attribute shall be based
     * on the same System Time source as those used to fulfill any usage of the system-us and system-ms data types
     * within the server.
     */
    public BigInteger upTime; // 2 uint64 R V
    /**
     * The TotalOperationalHours attribute shall indicate a best-effort attempt at tracking the length of time, in
     * hours, that the Node has been operational. The TotalOperationalHours attribute SHOULD be incremented to account
     * for the periods of time that a Node is in a low-power or sleep state. The TotalOperationalHours attribute shall
     * only be reset upon a factory reset of the Node.
     */
    public Integer totalOperationalHours; // 3 uint32 R V
    /**
     * The BootReason attribute shall indicate the reason for the Node’s most recent boot.
     */
    public BootReasonEnum bootReason; // 4 BootReasonEnum R V
    /**
     * The ActiveHardwareFaults attribute shall indicate the set of faults currently detected by the Node. When the Node
     * detects a fault has been raised, the appropriate HardwareFaultEnum value shall be added to this list. This list
     * shall NOT contain more than one instance of a specific HardwareFaultEnum value. When the Node detects that all
     * conditions contributing to a fault has been cleared, the corresponding HardwareFaultEnum value shall be removed
     * from this list. An empty list shall indicate there are currently no active faults. The order of this list SHOULD
     * have no significance. Clients interested in monitoring changes in active faults may subscribe to this attribute,
     * or they may subscribe to HardwareFaultChange.
     */
    public List<HardwareFaultEnum> activeHardwareFaults; // 5 list R V
    /**
     * The ActiveRadioFaults attribute shall indicate the set of faults currently detected by the Node. When the Node
     * detects a fault has been raised, the appropriate RadioFaultEnum value shall be added to this list. This list
     * shall NOT contain more than one instance of a specific RadioFaultEnum value. When the Node detects that all
     * conditions contributing to a fault has been cleared, the corresponding RadioFaultEnum value shall be removed from
     * this list. An empty list shall indicate there are currently no active faults. The order of this list SHOULD have
     * no significance. Clients interested in monitoring changes in active faults may subscribe to this attribute, or
     * they may subscribe to RadioFaultChange.
     */
    public List<RadioFaultEnum> activeRadioFaults; // 6 list R V
    /**
     * The ActiveNetworkFaults attribute shall indicate the set of faults currently detected by the Node. When the Node
     * detects a fault has been raised, the appropriate NetworkFaultEnum value shall be added to this list. This list
     * shall NOT contain more than one instance of a specific NetworkFaultEnum value. When the Node detects that all
     * conditions contributing to a fault has been cleared, the corresponding NetworkFaultEnum value shall be removed
     * from this list. An empty list shall indicate there are currently no active faults. The order of this list SHOULD
     * have no significance. Clients interested in monitoring changes in active faults may subscribe to this attribute,
     * or they may subscribe to NetworkFaultChange.
     */
    public List<NetworkFaultEnum> activeNetworkFaults; // 7 list R V
    /**
     * The TestEventTriggersEnabled attribute shall indicate whether the Node has any TestEventTrigger configured. When
     * this attribute is true, the Node has been configured with one or more test event triggers by virtue of the
     * internally programmed EnableKey value (see TestEventTrigger) being set to a non-zero value. This attribute can be
     * used by Administrators to detect if a device was inadvertently commissioned with test event trigger mode enabled,
     * and take appropriate action (e.g. warn the user and/or offer to remove all fabrics on the Node).
     */
    public Boolean testEventTriggersEnabled; // 8 bool R V

    // Structs
    /**
     * The HardwareFaultChange Event shall indicate a change in the set of hardware faults currently detected by the
     * Node.
     */
    public static class HardwareFaultChange {
        /**
         * This field shall represent the set of faults currently detected, as per HardwareFaultEnum.
         */
        public List<HardwareFaultEnum> current; // list
        /**
         * This field shall represent the set of faults detected prior to this change event, as per HardwareFaultEnum.
         */
        public List<HardwareFaultEnum> previous; // list

        public HardwareFaultChange(List<HardwareFaultEnum> current, List<HardwareFaultEnum> previous) {
            this.current = current;
            this.previous = previous;
        }
    }

    /**
     * The RadioFaultChange Event shall indicate a change in the set of radio faults currently detected by the Node.
     */
    public static class RadioFaultChange {
        /**
         * This field shall represent the set of faults currently detected, as per RadioFaultEnum.
         */
        public List<RadioFaultEnum> current; // list
        /**
         * This field shall represent the set of faults detected prior to this change event, as per RadioFaultEnum.
         */
        public List<RadioFaultEnum> previous; // list

        public RadioFaultChange(List<RadioFaultEnum> current, List<RadioFaultEnum> previous) {
            this.current = current;
            this.previous = previous;
        }
    }

    /**
     * The NetworkFaultChange Event shall indicate a change in the set of network faults currently detected by the Node.
     */
    public static class NetworkFaultChange {
        /**
         * This field shall represent the set of faults currently detected, as per NetworkFaultEnum.
         */
        public List<NetworkFaultEnum> current; // list
        /**
         * This field shall represent the set of faults detected prior to this change event, as per NetworkFaultEnum.
         */
        public List<NetworkFaultEnum> previous; // list

        public NetworkFaultChange(List<NetworkFaultEnum> current, List<NetworkFaultEnum> previous) {
            this.current = current;
            this.previous = previous;
        }
    }

    /**
     * The BootReason Event shall indicate the reason that caused the device to start-up.
     */
    public static class BootReason {
        /**
         * This field shall contain the reason for this BootReason event.
         */
        public BootReasonEnum bootReason; // BootReasonEnum

        public BootReason(BootReasonEnum bootReason) {
            this.bootReason = bootReason;
        }
    }

    /**
     * This structure describes a network interface supported by the Node, as provided in the NetworkInterfaces
     * attribute.
     */
    public static class NetworkInterface {
        /**
         * This field shall indicate a human-readable (displayable) name for the network interface, that is different
         * from all other interfaces.
         */
        public String name; // string
        /**
         * This field shall indicate if the Node is currently advertising itself operationally on this network interface
         * and is capable of successfully receiving incoming traffic from other Nodes.
         */
        public Boolean isOperational; // bool
        /**
         * This field shall indicate whether the Node is currently able to reach off-premise services it uses by
         * utilizing IPv4. The value shall be null if the Node does not use such services or does not know whether it
         * can reach them.
         */
        public Boolean offPremiseServicesReachableIPv4; // bool
        /**
         * This field shall indicate whether the Node is currently able to reach off-premise services it uses by
         * utilizing IPv6. The value shall be null if the Node does not use such services or does not know whether it
         * can reach them.
         */
        public Boolean offPremiseServicesReachableIPv6; // bool
        /**
         * This field shall contain the current link-layer address for a 802.3 or IEEE 802.11-2020 network interface and
         * contain the current extended MAC address for a 802.15.4 interface. The byte order of the octstr shall be in
         * wire byte order. For addresses values less than 64 bits, the first two bytes shall be zero.
         */
        public OctetString hardwareAddress; // hwadr
        /**
         * This field shall provide a list of the IPv4 addresses that are currently assigned to the network interface.
         */
        public List<OctetString> iPv4Addresses; // list
        /**
         * This field shall provide a list of the unicast IPv6 addresses that are currently assigned to the network
         * interface. This list shall include the Node’s link-local address and SHOULD include any assigned GUA and ULA
         * addresses. This list shall NOT include any multicast group addresses to which the Node is subscribed.
         */
        public List<OctetString> iPv6Addresses; // list
        /**
         * This field shall indicate the type of the interface using the InterfaceTypeEnum.
         */
        public InterfaceTypeEnum type; // InterfaceTypeEnum

        public NetworkInterface(String name, Boolean isOperational, Boolean offPremiseServicesReachableIPv4,
                Boolean offPremiseServicesReachableIPv6, OctetString hardwareAddress, List<OctetString> iPv4Addresses,
                List<OctetString> iPv6Addresses, InterfaceTypeEnum type) {
            this.name = name;
            this.isOperational = isOperational;
            this.offPremiseServicesReachableIPv4 = offPremiseServicesReachableIPv4;
            this.offPremiseServicesReachableIPv6 = offPremiseServicesReachableIPv6;
            this.hardwareAddress = hardwareAddress;
            this.iPv4Addresses = iPv4Addresses;
            this.iPv6Addresses = iPv6Addresses;
            this.type = type;
        }
    }

    // Enums
    public enum HardwareFaultEnum implements MatterEnum {
        UNSPECIFIED(0, "Unspecified"),
        RADIO(1, "Radio"),
        SENSOR(2, "Sensor"),
        RESETTABLE_OVER_TEMP(3, "Resettable Over Temp"),
        NON_RESETTABLE_OVER_TEMP(4, "Non Resettable Over Temp"),
        POWER_SOURCE(5, "Power Source"),
        VISUAL_DISPLAY_FAULT(6, "Visual Display Fault"),
        AUDIO_OUTPUT_FAULT(7, "Audio Output Fault"),
        USER_INTERFACE_FAULT(8, "User Interface Fault"),
        NON_VOLATILE_MEMORY_ERROR(9, "Non Volatile Memory Error"),
        TAMPER_DETECTED(10, "Tamper Detected");

        public final Integer value;
        public final String label;

        private HardwareFaultEnum(Integer value, String label) {
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

    public enum RadioFaultEnum implements MatterEnum {
        UNSPECIFIED(0, "Unspecified"),
        WI_FI_FAULT(1, "Wi Fi Fault"),
        CELLULAR_FAULT(2, "Cellular Fault"),
        THREAD_FAULT(3, "Thread Fault"),
        NFC_FAULT(4, "Nfc Fault"),
        BLE_FAULT(5, "Ble Fault"),
        ETHERNET_FAULT(6, "Ethernet Fault");

        public final Integer value;
        public final String label;

        private RadioFaultEnum(Integer value, String label) {
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

    public enum NetworkFaultEnum implements MatterEnum {
        UNSPECIFIED(0, "Unspecified"),
        HARDWARE_FAILURE(1, "Hardware Failure"),
        NETWORK_JAMMED(2, "Network Jammed"),
        CONNECTION_FAILED(3, "Connection Failed");

        public final Integer value;
        public final String label;

        private NetworkFaultEnum(Integer value, String label) {
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

    public enum InterfaceTypeEnum implements MatterEnum {
        UNSPECIFIED(0, "Unspecified"),
        WI_FI(1, "Wi Fi"),
        ETHERNET(2, "Ethernet"),
        CELLULAR(3, "Cellular"),
        THREAD(4, "Thread");

        public final Integer value;
        public final String label;

        private InterfaceTypeEnum(Integer value, String label) {
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

    public enum BootReasonEnum implements MatterEnum {
        UNSPECIFIED(0, "Unspecified"),
        POWER_ON_REBOOT(1, "Power On Reboot"),
        BROWN_OUT_RESET(2, "Brown Out Reset"),
        SOFTWARE_WATCHDOG_RESET(3, "Software Watchdog Reset"),
        HARDWARE_WATCHDOG_RESET(4, "Hardware Watchdog Reset"),
        SOFTWARE_UPDATE_COMPLETED(5, "Software Update Completed"),
        SOFTWARE_RESET(6, "Software Reset");

        public final Integer value;
        public final String label;

        private BootReasonEnum(Integer value, String label) {
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
         * This feature indicates support for extended Data Model testing commands, which are required in some
         * situations.
         * This feature shall be supported if the MaxPathsPerInvoke attribute of the Basic Information Cluster has a
         * value &gt; 1.
         */
        public boolean dataModelTest;

        public FeatureMap(boolean dataModelTest) {
            this.dataModelTest = dataModelTest;
        }
    }

    public GeneralDiagnosticsCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 51, "GeneralDiagnostics");
    }

    protected GeneralDiagnosticsCluster(BigInteger nodeId, int endpointId, int clusterId, String clusterName) {
        super(nodeId, endpointId, clusterId, clusterName);
    }

    // commands
    /**
     * This command shall be supported to provide a means for certification tests to trigger some test-plan-specific
     * events, necessary to assist in automation of device interactions for some certification test cases. This command
     * shall NOT cause any changes to the state of the device that persist after the last fabric is removed.
     * The fields for the TestEventTrigger command are as follows:
     */
    public static ClusterCommand testEventTrigger(OctetString enableKey, BigInteger eventTrigger) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (enableKey != null) {
            map.put("enableKey", enableKey);
        }
        if (eventTrigger != null) {
            map.put("eventTrigger", eventTrigger);
        }
        return new ClusterCommand("testEventTrigger", map);
    }

    /**
     * This command may be used by a client to obtain a correlated view of both System Time, and, if currently
     * synchronized and supported, &quot;wall clock time&quot; of the server. This can help clients establish time
     * correlation between their concept of time and the server’s concept of time. This is especially useful when
     * processing event histories where some events only contain System Time.
     * Upon command invocation, the server shall respond with a TimeSnapshotResponse.
     */
    public static ClusterCommand timeSnapshot() {
        return new ClusterCommand("timeSnapshot");
    }

    /**
     * This command provides a means for certification tests or manufacturer’s internal tests to validate particular
     * command handling and encoding constraints by generating a response of a given size.
     * This command shall use the same EnableKey behavior as the TestEventTrigger command, whereby processing of the
     * command is only enabled when the TestEventTriggersEnabled field is true, which shall NOT be true outside of
     * certification testing or manufacturer’s internal tests.
     * The fields for the PayloadTestRequest command are as follows:
     */
    public static ClusterCommand payloadTestRequest(OctetString enableKey, Integer value, Integer count) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (enableKey != null) {
            map.put("enableKey", enableKey);
        }
        if (value != null) {
            map.put("value", value);
        }
        if (count != null) {
            map.put("count", count);
        }
        return new ClusterCommand("payloadTestRequest", map);
    }

    @Override
    public @NonNull String toString() {
        String str = "";
        str += "clusterRevision : " + clusterRevision + "\n";
        str += "featureMap : " + featureMap + "\n";
        str += "networkInterfaces : " + networkInterfaces + "\n";
        str += "rebootCount : " + rebootCount + "\n";
        str += "upTime : " + upTime + "\n";
        str += "totalOperationalHours : " + totalOperationalHours + "\n";
        str += "bootReason : " + bootReason + "\n";
        str += "activeHardwareFaults : " + activeHardwareFaults + "\n";
        str += "activeRadioFaults : " + activeRadioFaults + "\n";
        str += "activeNetworkFaults : " + activeNetworkFaults + "\n";
        str += "testEventTriggersEnabled : " + testEventTriggersEnabled + "\n";
        return str;
    }
}
