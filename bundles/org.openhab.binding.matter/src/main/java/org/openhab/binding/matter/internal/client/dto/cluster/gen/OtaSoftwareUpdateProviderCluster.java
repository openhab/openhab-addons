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
 * OtaSoftwareUpdateProvider
 *
 * @author Dan Cunningham - Initial contribution
 */
public class OtaSoftwareUpdateProviderCluster extends BaseCluster {

    public static final int CLUSTER_ID = 0x0029;
    public static final String CLUSTER_NAME = "OtaSoftwareUpdateProvider";
    public static final String CLUSTER_PREFIX = "otaSoftwareUpdateProvider";
    public static final String ATTRIBUTE_CLUSTER_REVISION = "clusterRevision";

    public Integer clusterRevision; // 65533 ClusterRevision

    // Enums
    /**
     * See Section 11.20.3.2, “Querying the OTA Provider” for the semantics of these values.
     */
    public enum StatusEnum implements MatterEnum {
        UPDATE_AVAILABLE(0, "Update Available"),
        BUSY(1, "Busy"),
        NOT_AVAILABLE(2, "Not Available"),
        DOWNLOAD_PROTOCOL_NOT_SUPPORTED(3, "Download Protocol Not Supported");

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

    /**
     * See Section 11.20.3.6, “Applying a software update” for the semantics of the values. This enumeration is used in
     * the Action field of the ApplyUpdateResponse command. See (Action).
     */
    public enum ApplyUpdateActionEnum implements MatterEnum {
        PROCEED(0, "Proceed"),
        AWAIT_NEXT_ACTION(1, "Await Next Action"),
        DISCONTINUE(2, "Discontinue");

        public final Integer value;
        public final String label;

        private ApplyUpdateActionEnum(Integer value, String label) {
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
     * Note that only HTTP over TLS (HTTPS) is supported (see RFC 7230). Using HTTP without TLS shall NOT be supported,
     * as there is no way to authenticate the involved participants.
     */
    public enum DownloadProtocolEnum implements MatterEnum {
        BDX_SYNCHRONOUS(0, "Bdx Synchronous"),
        BDX_ASYNCHRONOUS(1, "Bdx Asynchronous"),
        HTTPS(2, "Https"),
        VENDOR_SPECIFIC(3, "Vendor Specific");

        public final Integer value;
        public final String label;

        private DownloadProtocolEnum(Integer value, String label) {
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

    public OtaSoftwareUpdateProviderCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 41, "OtaSoftwareUpdateProvider");
    }

    protected OtaSoftwareUpdateProviderCluster(BigInteger nodeId, int endpointId, int clusterId, String clusterName) {
        super(nodeId, endpointId, clusterId, clusterName);
    }

    // commands
    /**
     * Upon receipt, this command shall trigger an attempt to find an updated Software Image by the OTA Provider to
     * match the OTA Requestor’s constraints provided in the payload fields.
     */
    public static ClusterCommand queryImage(Integer vendorId, Integer productId, Integer softwareVersion,
            List<DownloadProtocolEnum> protocolsSupported, Integer hardwareVersion, String location,
            Boolean requestorCanConsent, OctetString metadataForProvider) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (vendorId != null) {
            map.put("vendorId", vendorId);
        }
        if (productId != null) {
            map.put("productId", productId);
        }
        if (softwareVersion != null) {
            map.put("softwareVersion", softwareVersion);
        }
        if (protocolsSupported != null) {
            map.put("protocolsSupported", protocolsSupported);
        }
        if (hardwareVersion != null) {
            map.put("hardwareVersion", hardwareVersion);
        }
        if (location != null) {
            map.put("location", location);
        }
        if (requestorCanConsent != null) {
            map.put("requestorCanConsent", requestorCanConsent);
        }
        if (metadataForProvider != null) {
            map.put("metadataForProvider", metadataForProvider);
        }
        return new ClusterCommand("queryImage", map);
    }

    public static ClusterCommand applyUpdateRequest(OctetString updateToken, Integer newVersion) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (updateToken != null) {
            map.put("updateToken", updateToken);
        }
        if (newVersion != null) {
            map.put("newVersion", newVersion);
        }
        return new ClusterCommand("applyUpdateRequest", map);
    }

    public static ClusterCommand notifyUpdateApplied(OctetString updateToken, Integer softwareVersion) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (updateToken != null) {
            map.put("updateToken", updateToken);
        }
        if (softwareVersion != null) {
            map.put("softwareVersion", softwareVersion);
        }
        return new ClusterCommand("notifyUpdateApplied", map);
    }

    @Override
    public @NonNull String toString() {
        String str = "";
        str += "clusterRevision : " + clusterRevision + "\n";
        return str;
    }
}
