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

/**
 * ApplicationBasic
 *
 * @author Dan Cunningham - Initial contribution
 */
public class ApplicationBasicCluster extends BaseCluster {

    public static final int CLUSTER_ID = 0x050D;
    public static final String CLUSTER_NAME = "ApplicationBasic";
    public static final String CLUSTER_PREFIX = "applicationBasic";
    public static final String ATTRIBUTE_CLUSTER_REVISION = "clusterRevision";
    public static final String ATTRIBUTE_VENDOR_NAME = "vendorName";
    public static final String ATTRIBUTE_VENDOR_ID = "vendorId";
    public static final String ATTRIBUTE_APPLICATION_NAME = "applicationName";
    public static final String ATTRIBUTE_PRODUCT_ID = "productId";
    public static final String ATTRIBUTE_APPLICATION = "application";
    public static final String ATTRIBUTE_STATUS = "status";
    public static final String ATTRIBUTE_APPLICATION_VERSION = "applicationVersion";
    public static final String ATTRIBUTE_ALLOWED_VENDOR_LIST = "allowedVendorList";

    public Integer clusterRevision; // 65533 ClusterRevision
    /**
     * This attribute shall specify a human readable (displayable) name of the vendor for the Content App.
     */
    public String vendorName; // 0 string R V
    /**
     * This attribute, if present, shall specify the Connectivity Standards Alliance assigned Vendor ID for the Content
     * App.
     */
    public Integer vendorId; // 1 vendor-id R V
    /**
     * This attribute shall specify a human readable (displayable) name of the Content App assigned by the vendor. For
     * example, &quot;NPR On Demand&quot;. The maximum length of the ApplicationName attribute is 256 bytes of UTF-8
     * characters.
     */
    public String applicationName; // 2 string R V
    /**
     * This attribute, if present, shall specify a numeric ID assigned by the vendor to identify a specific Content App
     * made by them. If the Content App is certified by the Connectivity Standards Alliance, then this would be the
     * Product ID as specified by the vendor for the certification.
     */
    public Integer productId; // 3 uint16 R V
    /**
     * This attribute shall specify a Content App which consists of an Application ID using a specified catalog.
     */
    public ApplicationStruct application; // 4 ApplicationStruct R V
    /**
     * This attribute shall specify the current running status of the application.
     */
    public ApplicationStatusEnum status; // 5 ApplicationStatusEnum R V
    /**
     * This attribute shall specify a human readable (displayable) version of the Content App assigned by the vendor.
     * The maximum length of the ApplicationVersion attribute is 32 bytes of UTF-8 characters.
     */
    public String applicationVersion; // 6 string R V
    /**
     * This attribute is a list of vendor IDs. Each entry is a vendor-id.
     */
    public List<Integer> allowedVendorList; // 7 list R A

    // Structs
    /**
     * This indicates a global identifier for an Application given a catalog.
     */
    public static class ApplicationStruct {
        /**
         * This field shall indicate the Connectivity Standards Alliance issued vendor ID for the catalog. The DIAL
         * registry shall use value 0x0000.
         * It is assumed that Content App Platform providers (see Video Player Architecture section in [MatterDevLib])
         * will have their own catalog vendor ID (set to their own Vendor ID) and will assign an ApplicationID to each
         * Content App.
         */
        public Integer catalogVendorId; // uint16
        /**
         * This field shall indicate the application identifier, expressed as a string, such as &quot;123456-5433&quot;,
         * &quot;PruneVideo&quot; or &quot;Company X&quot;. This field shall be unique within a catalog.
         * For the DIAL registry catalog, this value shall be the DIAL prefix.
         */
        public String applicationId; // string

        public ApplicationStruct(Integer catalogVendorId, String applicationId) {
            this.catalogVendorId = catalogVendorId;
            this.applicationId = applicationId;
        }
    }

    // Enums
    public enum ApplicationStatusEnum implements MatterEnum {
        STOPPED(0, "Stopped"),
        ACTIVE_VISIBLE_FOCUS(1, "Active Visible Focus"),
        ACTIVE_HIDDEN(2, "Active Hidden"),
        ACTIVE_VISIBLE_NOT_FOCUS(3, "Active Visible Not Focus");

        public final Integer value;
        public final String label;

        private ApplicationStatusEnum(Integer value, String label) {
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

    public ApplicationBasicCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 1293, "ApplicationBasic");
    }

    protected ApplicationBasicCluster(BigInteger nodeId, int endpointId, int clusterId, String clusterName) {
        super(nodeId, endpointId, clusterId, clusterName);
    }

    @Override
    public @NonNull String toString() {
        String str = "";
        str += "clusterRevision : " + clusterRevision + "\n";
        str += "vendorName : " + vendorName + "\n";
        str += "vendorId : " + vendorId + "\n";
        str += "applicationName : " + applicationName + "\n";
        str += "productId : " + productId + "\n";
        str += "application : " + application + "\n";
        str += "status : " + status + "\n";
        str += "applicationVersion : " + applicationVersion + "\n";
        str += "allowedVendorList : " + allowedVendorList + "\n";
        return str;
    }
}
