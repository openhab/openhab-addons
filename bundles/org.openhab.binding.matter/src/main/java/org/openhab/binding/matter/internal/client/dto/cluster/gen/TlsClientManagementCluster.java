/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
 * TlsClientManagement
 *
 * @author Dan Cunningham - Initial contribution
 */
public class TlsClientManagementCluster extends BaseCluster {

    public static final int CLUSTER_ID = 0x0802;
    public static final String CLUSTER_NAME = "TlsClientManagement";
    public static final String CLUSTER_PREFIX = "tlsClientManagement";
    public static final String ATTRIBUTE_MAX_PROVISIONED = "maxProvisioned";
    public static final String ATTRIBUTE_PROVISIONED_ENDPOINTS = "provisionedEndpoints";

    /**
     * Indicates the maximum number of per fabric TLSEndpoints that can be installed on this Node.
     */
    public Integer maxProvisioned; // 0 uint8 R V
    /**
     * Indicates a list of currently provisioned TLS Endpoints on this Node. The maximum length of this list when read
     * will be the value of MaxProvisioned.
     */
    public List<TLSEndpointStruct> provisionedEndpoints; // 1 list R S V

    // Structs
    /**
     * This struct encodes details about a TLS Endpoint.
     */
    public static class TLSEndpointStruct {
        /**
         * This field shall represent the unique TLS Endpoint ID.
         */
        public Integer endpointId; // TLSEndpointID
        /**
         * This field shall represent a TLS Hostname.
         */
        public OctetString hostname; // octstr
        /**
         * This field shall represent a TLS Port Number.
         */
        public Integer port; // uint16
        /**
         * This field shall be a TLSCAID representing the associated Certificate Authority ID.
         */
        public Integer caid; // TlsCertificateManagement.TLSCAID
        /**
         * This field shall be a TLSCCDID representing the associated Client Certificate Details ID. A NULL value means
         * no client certificate is used with this endpoint.
         */
        public Integer ccdid; // TlsCertificateManagement.TLSCCDID
        /**
         * This field shall indicate a reference count of the number of entities currently using this TLS Endpoint. The
         * node shall recompute this field to reflect the correct value at runtime (e.g., when restored from a persisted
         * value after a reboot).
         */
        public Integer referenceCount; // uint8
        public Integer fabricIndex; // FabricIndex

        public TLSEndpointStruct(Integer endpointId, OctetString hostname, Integer port, Integer caid, Integer ccdid,
                Integer referenceCount, Integer fabricIndex) {
            this.endpointId = endpointId;
            this.hostname = hostname;
            this.port = port;
            this.caid = caid;
            this.ccdid = ccdid;
            this.referenceCount = referenceCount;
            this.fabricIndex = fabricIndex;
        }
    }

    // Enums
    public enum StatusCodeEnum implements MatterEnum {
        ENDPOINT_ALREADY_INSTALLED(2, "Endpoint Already Installed"),
        ROOT_CERTIFICATE_NOT_FOUND(3, "Root Certificate Not Found"),
        CLIENT_CERTIFICATE_NOT_FOUND(4, "Client Certificate Not Found"),
        ENDPOINT_IN_USE(5, "Endpoint In Use"),
        INVALID_TIME(6, "Invalid Time");

        private final Integer value;
        private final String label;

        private StatusCodeEnum(Integer value, String label) {
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

    public TlsClientManagementCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 2050, "TlsClientManagement");
    }

    protected TlsClientManagementCluster(BigInteger nodeId, int endpointId, int clusterId, String clusterName) {
        super(nodeId, endpointId, clusterId, clusterName);
    }

    // commands
    /**
     * This command is used to provision a TLS Endpoint for the provided Hostname / Port combination.
     */
    public static ClusterCommand provisionEndpoint(OctetString hostname, Integer port, Integer caid, Integer ccdid,
            Integer endpointId) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (hostname != null) {
            map.put("hostname", hostname);
        }
        if (port != null) {
            map.put("port", port);
        }
        if (caid != null) {
            map.put("caid", caid);
        }
        if (ccdid != null) {
            map.put("ccdid", ccdid);
        }
        if (endpointId != null) {
            map.put("endpointId", endpointId);
        }
        return new ClusterCommand("provisionEndpoint", map);
    }

    /**
     * This command is used to find a TLS Endpoint by its ID.
     * This command shall return the TLSEndpointStruct for the passed in EndpointID.
     */
    public static ClusterCommand findEndpoint(Integer endpointId) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (endpointId != null) {
            map.put("endpointId", endpointId);
        }
        return new ClusterCommand("findEndpoint", map);
    }

    /**
     * This command is used to remove a TLS Endpoint by its ID.
     * This command shall be generated to request the Node remove any TLS Endpoint.
     */
    public static ClusterCommand removeEndpoint(Integer endpointId) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (endpointId != null) {
            map.put("endpointId", endpointId);
        }
        return new ClusterCommand("removeEndpoint", map);
    }

    @Override
    public @NonNull String toString() {
        String str = "";
        str += "maxProvisioned : " + maxProvisioned + "\n";
        str += "provisionedEndpoints : " + provisionedEndpoints + "\n";
        return str;
    }
}
