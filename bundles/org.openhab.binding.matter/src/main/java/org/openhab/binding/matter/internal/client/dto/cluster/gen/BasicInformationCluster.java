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
 * BasicInformation
 *
 * @author Dan Cunningham - Initial contribution
 */
public class BasicInformationCluster extends BaseCluster {

    public static final int CLUSTER_ID = 0x0028;
    public static final String CLUSTER_NAME = "BasicInformation";
    public static final String CLUSTER_PREFIX = "basicInformation";
    public static final String ATTRIBUTE_CLUSTER_REVISION = "clusterRevision";
    public static final String ATTRIBUTE_DATA_MODEL_REVISION = "dataModelRevision";
    public static final String ATTRIBUTE_VENDOR_NAME = "vendorName";
    public static final String ATTRIBUTE_VENDOR_ID = "vendorId";
    public static final String ATTRIBUTE_PRODUCT_NAME = "productName";
    public static final String ATTRIBUTE_PRODUCT_ID = "productId";
    public static final String ATTRIBUTE_NODE_LABEL = "nodeLabel";
    public static final String ATTRIBUTE_LOCATION = "location";
    public static final String ATTRIBUTE_HARDWARE_VERSION = "hardwareVersion";
    public static final String ATTRIBUTE_HARDWARE_VERSION_STRING = "hardwareVersionString";
    public static final String ATTRIBUTE_SOFTWARE_VERSION = "softwareVersion";
    public static final String ATTRIBUTE_SOFTWARE_VERSION_STRING = "softwareVersionString";
    public static final String ATTRIBUTE_MANUFACTURING_DATE = "manufacturingDate";
    public static final String ATTRIBUTE_PART_NUMBER = "partNumber";
    public static final String ATTRIBUTE_PRODUCT_URL = "productUrl";
    public static final String ATTRIBUTE_PRODUCT_LABEL = "productLabel";
    public static final String ATTRIBUTE_SERIAL_NUMBER = "serialNumber";
    public static final String ATTRIBUTE_LOCAL_CONFIG_DISABLED = "localConfigDisabled";
    public static final String ATTRIBUTE_REACHABLE = "reachable";
    public static final String ATTRIBUTE_UNIQUE_ID = "uniqueId";
    public static final String ATTRIBUTE_CAPABILITY_MINIMA = "capabilityMinima";
    public static final String ATTRIBUTE_PRODUCT_APPEARANCE = "productAppearance";
    public static final String ATTRIBUTE_SPECIFICATION_VERSION = "specificationVersion";
    public static final String ATTRIBUTE_MAX_PATHS_PER_INVOKE = "maxPathsPerInvoke";

    public Integer clusterRevision; // 65533 ClusterRevision
    /**
     * This attribute shall be set to the revision number of the Data Model against which the Node is certified. The
     * value of this attribute shall be one of the valid values listed in Section 7.1.1, “Revision History”.
     */
    public Integer dataModelRevision; // 0 uint16 R V
    /**
     * This attribute shall specify a human readable (displayable) name of the vendor for the Node.
     */
    public String vendorName; // 1 string R V
    /**
     * This attribute shall specify the Vendor ID.
     */
    public Integer vendorId; // 2 vendor-id R V
    /**
     * This attribute shall specify a human readable (displayable) name of the model for the Node such as the model
     * number (or other identifier) assigned by the vendor.
     */
    public String productName; // 3 string R V
    /**
     * This attribute shall specify the Product ID assigned by the vendor that is unique to the specific product of the
     * Node.
     */
    public Integer productId; // 4 uint16 R V
    /**
     * Indicates a user defined name for the Node. This attribute SHOULD be set during initial commissioning and may be
     * updated by further reconfigurations.
     */
    public String nodeLabel; // 5 string RW VM
    /**
     * This attribute shall be an ISO 3166-1 alpha-2 code to represent the country, dependent territory, or special area
     * of geographic interest in which the Node is located at the time of the attribute being set. This attribute shall
     * be set during initial commissioning (unless already set) and may be updated by further reconfigurations. This
     * attribute may affect some regulatory aspects of the Node’s operation, such as radio transmission power levels in
     * given spectrum allocation bands if technologies where this is applicable are used. The Location’s region code
     * shall be interpreted in a case-insensitive manner. If the Node cannot understand the location code with which it
     * was configured, or the location code has not yet been configured, it shall configure itself in a region-agnostic
     * manner as determined by the vendor, avoiding region-specific assumptions as much as is practical. The special
     * value XX shall indicate that region-agnostic mode is used.
     */
    public String location; // 6 string RW VA
    /**
     * This attribute shall specify the version number of the hardware of the Node. The meaning of its value, and the
     * versioning scheme, are vendor defined.
     */
    public Integer hardwareVersion; // 7 uint16 R V
    /**
     * This attribute shall specify the version number of the hardware of the Node. The meaning of its value, and the
     * versioning scheme, are vendor defined. The HardwareVersionString attribute shall be used to provide a more
     * user-friendly value than that represented by the HardwareVersion attribute.
     */
    public String hardwareVersionString; // 8 string R V
    /**
     * This attribute shall contain the current version number for the software running on this Node. The version number
     * can be compared using a total ordering to determine if a version is logically newer than another one. A larger
     * value of SoftwareVersion is newer than a lower value, from the perspective of software updates (see Section
     * 11.20.3.3, “Availability of Software Images”). Nodes may query this field to determine the currently running
     * version of software on another given Node.
     */
    public Integer softwareVersion; // 9 uint32 R V
    /**
     * This attribute shall contain a current human-readable representation for the software running on the Node. This
     * version information may be conveyed to users. The maximum length of the SoftwareVersionString attribute is 64
     * bytes of UTF-8 characters. The contents SHOULD only use simple 7-bit ASCII alphanumeric and punctuation
     * characters, so as to simplify the conveyance of the value to a variety of cultures.
     * Examples of version strings include &quot;1.0&quot;, &quot;1.2.3456&quot;, &quot;1.2-2&quot;,
     * &quot;1.0b123&quot;, &quot;1.2_3&quot;.
     */
    public String softwareVersionString; // 10 string R V
    /**
     * This attribute shall specify the date that the Node was manufactured. The first 8 characters shall specify the
     * date of manufacture of the Node in international date notation according to ISO 8601, i.e., YYYYMMDD, e.g.,
     * 20060814. The final 8 characters may include country, factory, line, shift or other related information at the
     * option of the vendor. The format of this information is vendor defined.
     */
    public String manufacturingDate; // 11 string R V
    /**
     * This attribute shall specify a human-readable (displayable) vendor assigned part number for the Node whose
     * meaning and numbering scheme is vendor defined.
     * Multiple products (and hence PartNumbers) can share a ProductID. For instance, there may be different packaging
     * (with different PartNumbers) for different regions; also different colors of a product might share the ProductID
     * but may have a different PartNumber.
     */
    public String partNumber; // 12 string R V
    /**
     * This attribute shall specify a link to a product specific web page. The specified URL SHOULD resolve to a
     * maintained web page available for the lifetime of the product. The syntax of this attribute shall follow the
     * syntax as specified in RFC 1738 and shall use the https scheme. The maximum length of this attribute is 256 ASCII
     * characters.
     */
    public String productUrl; // 13 string R V
    /**
     * This attribute shall specify a vendor specific human readable (displayable) product label. The ProductLabel
     * attribute may be used to provide a more user-friendly value than that represented by the ProductName attribute.
     * The ProductLabel attribute SHOULD NOT include the name of the vendor as defined within the VendorName attribute.
     */
    public String productLabel; // 14 string R V
    /**
     * This attribute shall specify a human readable (displayable) serial number.
     */
    public String serialNumber; // 15 string R V
    /**
     * This attribute shall allow a local Node configuration to be disabled. When this attribute is set to True the Node
     * shall disable the ability to configure the Node through an on-Node user interface. The value of the
     * LocalConfigDisabled attribute shall NOT in any way modify, disable, or otherwise affect the user’s ability to
     * trigger a factory reset on the Node.
     */
    public Boolean localConfigDisabled; // 16 bool RW VM
    /**
     * This attribute (when used) shall indicate whether the Node can be reached. For a native Node this is implicitly
     * True (and its use is optional).
     * Its main use case is in the derived Bridged Device Basic Information cluster where it is used to indicate whether
     * the bridged device is reachable by the bridge over the non-native network.
     */
    public Boolean reachable; // 17 bool R V
    /**
     * Indicates a unique identifier for the device, which is constructed in a manufacturer specific manner.
     * It may be constructed using a permanent device identifier (such as device MAC address) as basis. In order to
     * prevent tracking,
     * • it SHOULD NOT be identical to (or easily derived from) such permanent device identifier
     * • it shall be updated when the device is factory reset
     * • it shall NOT be identical to the SerialNumber attribute
     * • it shall NOT be printed on the product or delivered with the product
     * The value does not need to be human readable, since it is intended for machine to machine (M2M) communication.
     * &gt; [!NOTE]
     * &gt; The conformance of the UniqueID attribute was optional in cluster revisions prior to revision 4.
     * This UniqueID attribute shall NOT be the same as the Persistent Unique ID which is used in the Rotating Device
     * Identifier mechanism.
     */
    public String uniqueId; // 18 string R V
    /**
     * This attribute shall provide the minimum guaranteed value for some system-wide resource capabilities that are not
     * otherwise cluster-specific and do not appear elsewhere. This attribute may be used by clients to optimize
     * communication with Nodes by allowing them to use more than the strict minimum values required by this
     * specification, wherever available.
     * The values supported by the server in reality may be larger than the values provided in this attribute, such as
     * if a server is not resource-constrained at all. However, clients SHOULD only rely on the amounts provided in this
     * attribute.
     * Note that since the fixed values within this attribute may change over time, both increasing and decreasing, as
     * software versions change for a given Node, clients SHOULD take care not to assume forever unchanging values and
     * SHOULD NOT cache this value permanently at Commissioning time.
     */
    public CapabilityMinimaStruct capabilityMinima; // 19 CapabilityMinimaStruct R V
    /**
     * This attribute shall provide information about the appearance of the product, which could be useful to a user
     * trying to locate or identify the node.
     */
    public ProductAppearanceStruct productAppearance; // 20 ProductAppearanceStruct R V
    /**
     * This attribute shall contain the current version number for the specification version this Node was certified
     * against. The version number can be compared using a total ordering to determine if a version is logically newer
     * than another one. A larger value of SpecificationVersion is newer than a lower value.
     * Nodes may query this field to determine the currently supported version of the specification on another given
     * Node.
     * The format of this number is segmented as its four component bytes. Bit positions for the fields are as follows:
     * For example, a SpecificationVersion value of 0x0102AA00 is composed of 4 version components, representing a
     * version 1.2.170.0.
     * In the example above:
     * • Major version is the uppermost byte (0x01).
     * • Minor version is the following byte (0x02).
     * • Patch version is 170/0xAA.
     * • Reserved1 value is 0.
     * The initial revision (1.0) of this specification (1.0) was 0x01000000. Matter Spring 2024 release (1.3) was
     * 0x01030000.
     * If the SpecificationVersion is absent or zero, such as in Basic Information cluster revisions prior to Revision
     * 3, the specification version cannot be properly inferred unless other heuristics are employed.
     * Comparison of SpecificationVersion shall always include the total value over 32 bits, without masking reserved
     * parts.
     */
    public Integer specificationVersion; // 21 uint32 R V
    /**
     * Indicates the maximum number of elements in a single InvokeRequests list (see Section 8.8.2, “Invoke Request
     * Action”) that the Node is able to process. Note that since this attribute may change over time, both increasing
     * and decreasing, as software versions change for a given Node, clients SHOULD take care not to assume forever
     * unchanging values and SHOULD NOT cache this value permanently at Commissioning time.
     * If the MaxPathsPerInvoke attribute is absent or zero, such as in Basic Information cluster revisions prior to
     * Revision 3, clients shall assume a value of 1.
     */
    public Integer maxPathsPerInvoke; // 22 uint16 R V

    // Structs
    /**
     * The StartUp event shall be generated by a Node as soon as reasonable after completing a boot or reboot process.
     * The StartUp event SHOULD be the first Data Model event recorded by the Node after it completes a boot or reboot
     * process.
     */
    public static class StartUp {
        /**
         * This field shall be set to the same value as the one available in the SoftwareVersion attribute.
         */
        public Integer softwareVersion; // uint32

        public StartUp(Integer softwareVersion) {
            this.softwareVersion = softwareVersion;
        }
    }

    /**
     * The ShutDown event SHOULD be generated by a Node prior to any orderly shutdown sequence on a best-effort basis.
     * When a ShutDown event is generated, it SHOULD be the last Data Model event recorded by the Node. This event
     * SHOULD be delivered urgently to current subscribers on a best-effort basis. Any subsequent incoming interactions
     * to the Node may be dropped until the completion of a future boot or reboot process.
     */
    public static class ShutDown {
        public ShutDown() {
        }
    }

    /**
     * The Leave event SHOULD be generated by a Node prior to permanently leaving a given Fabric, such as when the
     * RemoveFabric command is invoked for a given fabric, or triggered by factory reset or some other manufacturer
     * specific action to disable or reset the operational data in the Node. When a Leave event is generated, it SHOULD
     * be assumed that the fabric recorded in the event is no longer usable, and subsequent interactions targeting that
     * fabric will most likely fail.
     * Upon receipt of Leave Event on a subscription, the receiving Node may update other nodes in the fabric by
     * removing related bindings, access control list entries and other data referencing the leaving Node.
     */
    public static class Leave {
        /**
         * This field shall contain the local Fabric Index of the fabric which the node is about to leave.
         */
        public Integer fabricIndex; // fabric-idx

        public Leave(Integer fabricIndex) {
            this.fabricIndex = fabricIndex;
        }
    }

    /**
     * This event shall be supported if and only if the Reachable attribute is supported.
     * This event (when supported) shall be generated when there is a change in the Reachable attribute.
     * Its main use case is in the derived Bridged Device Basic Information cluster.
     */
    public static class ReachableChanged {
        /**
         * This field shall indicate the value of the Reachable attribute after it was changed.
         */
        public Boolean reachableNewValue; // bool

        public ReachableChanged(Boolean reachableNewValue) {
            this.reachableNewValue = reachableNewValue;
        }
    }

    /**
     * This structure provides a description of the product’s appearance.
     */
    public static class ProductAppearanceStruct {
        /**
         * This field shall indicate the visible finish of the product.
         */
        public ProductFinishEnum finish; // ProductFinishEnum
        /**
         * This field indicates the representative color of the visible parts of the product. If the product has no
         * representative color, the field shall be null.
         */
        public ColorEnum primaryColor; // ColorEnum

        public ProductAppearanceStruct(ProductFinishEnum finish, ColorEnum primaryColor) {
            this.finish = finish;
            this.primaryColor = primaryColor;
        }
    }

    /**
     * This structure provides constant values related to overall global capabilities of this Node, that are not
     * cluster-specific.
     */
    public static class CapabilityMinimaStruct {
        /**
         * This field shall indicate the actual minimum number of concurrent CASE sessions that are supported per
         * fabric.
         * This value shall NOT be smaller than the required minimum indicated in Section 4.14.2.8, “Minimal Number of
         * CASE Sessions”.
         */
        public Integer caseSessionsPerFabric; // uint16
        /**
         * This field shall indicate the actual minimum number of concurrent subscriptions supported per fabric.
         * This value shall NOT be smaller than the required minimum indicated in Section 8.5.1, “Subscribe
         * Transaction”.
         */
        public Integer subscriptionsPerFabric; // uint16

        public CapabilityMinimaStruct(Integer caseSessionsPerFabric, Integer subscriptionsPerFabric) {
            this.caseSessionsPerFabric = caseSessionsPerFabric;
            this.subscriptionsPerFabric = subscriptionsPerFabric;
        }
    }

    // Enums
    /**
     * The data type of ProductFinishEnum is derived from enum8.
     */
    public enum ProductFinishEnum implements MatterEnum {
        OTHER(0, "Other"),
        MATTE(1, "Matte"),
        SATIN(2, "Satin"),
        POLISHED(3, "Polished"),
        RUGGED(4, "Rugged"),
        FABRIC(5, "Fabric");

        public final Integer value;
        public final String label;

        private ProductFinishEnum(Integer value, String label) {
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

    /**
     * The data type of ColorEnum is derived from enum8.
     */
    public enum ColorEnum implements MatterEnum {
        BLACK(0, "Black"),
        NAVY(1, "Navy"),
        GREEN(2, "Green"),
        TEAL(3, "Teal"),
        MAROON(4, "Maroon"),
        PURPLE(5, "Purple"),
        OLIVE(6, "Olive"),
        GRAY(7, "Gray"),
        BLUE(8, "Blue"),
        LIME(9, "Lime"),
        AQUA(10, "Aqua"),
        RED(11, "Red"),
        FUCHSIA(12, "Fuchsia"),
        YELLOW(13, "Yellow"),
        WHITE(14, "White"),
        NICKEL(15, "Nickel"),
        CHROME(16, "Chrome"),
        BRASS(17, "Brass"),
        COPPER(18, "Copper"),
        SILVER(19, "Silver"),
        GOLD(20, "Gold");

        public final Integer value;
        public final String label;

        private ColorEnum(Integer value, String label) {
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

    public BasicInformationCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 40, "BasicInformation");
    }

    protected BasicInformationCluster(BigInteger nodeId, int endpointId, int clusterId, String clusterName) {
        super(nodeId, endpointId, clusterId, clusterName);
    }

    @Override
    public @NonNull String toString() {
        String str = "";
        str += "clusterRevision : " + clusterRevision + "\n";
        str += "dataModelRevision : " + dataModelRevision + "\n";
        str += "vendorName : " + vendorName + "\n";
        str += "vendorId : " + vendorId + "\n";
        str += "productName : " + productName + "\n";
        str += "productId : " + productId + "\n";
        str += "nodeLabel : " + nodeLabel + "\n";
        str += "location : " + location + "\n";
        str += "hardwareVersion : " + hardwareVersion + "\n";
        str += "hardwareVersionString : " + hardwareVersionString + "\n";
        str += "softwareVersion : " + softwareVersion + "\n";
        str += "softwareVersionString : " + softwareVersionString + "\n";
        str += "manufacturingDate : " + manufacturingDate + "\n";
        str += "partNumber : " + partNumber + "\n";
        str += "productUrl : " + productUrl + "\n";
        str += "productLabel : " + productLabel + "\n";
        str += "serialNumber : " + serialNumber + "\n";
        str += "localConfigDisabled : " + localConfigDisabled + "\n";
        str += "reachable : " + reachable + "\n";
        str += "uniqueId : " + uniqueId + "\n";
        str += "capabilityMinima : " + capabilityMinima + "\n";
        str += "productAppearance : " + productAppearance + "\n";
        str += "specificationVersion : " + specificationVersion + "\n";
        str += "maxPathsPerInvoke : " + maxPathsPerInvoke + "\n";
        return str;
    }
}
