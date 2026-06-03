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
 * TlsCertificateManagement
 *
 * @author Dan Cunningham - Initial contribution
 */
public class TlsCertificateManagementCluster extends BaseCluster {

    public static final int CLUSTER_ID = 0x0801;
    public static final String CLUSTER_NAME = "TlsCertificateManagement";
    public static final String CLUSTER_PREFIX = "tlsCertificateManagement";
    public static final String ATTRIBUTE_MAX_ROOT_CERTIFICATES = "maxRootCertificates";
    public static final String ATTRIBUTE_PROVISIONED_ROOT_CERTIFICATES = "provisionedRootCertificates";
    public static final String ATTRIBUTE_MAX_CLIENT_CERTIFICATES = "maxClientCertificates";
    public static final String ATTRIBUTE_PROVISIONED_CLIENT_CERTIFICATES = "provisionedClientCertificates";

    /**
     * This attribute shall contain the maximum number of per fabric TLSRCACs that can be installed on this Node.
     */
    public Integer maxRootCertificates; // 0 uint8 R V
    /**
     * This attribute shall be a list of all provisioned TLSCertStruct that are currently installed on this Node. When
     * this attribute is read over a non Large Message capable transport, the Certificate field shall NOT be included.
     * To get the full details of a certificate use the FindRootCertificate command.
     */
    public List<TLSCertStruct> provisionedRootCertificates; // 1 list R S V
    /**
     * This attribute shall contain the maximum number of per fabric Client Certificates that can be installed on this
     * Node.
     */
    public Integer maxClientCertificates; // 2 uint8 R V
    /**
     * This attribute shall be a list of all provisioned TLSCCDID that are currently installed on this Node. When this
     * attribute is read over a non Large Message capable transport, the ClientCertificate and IntermediateCertificates
     * fields shall NOT be included. To get the full details of a client certificate use the FindClientCertificate
     * command.
     */
    public List<TLSClientCertificateDetailStruct> provisionedClientCertificates; // 3 list R S V

    // Structs
    /**
     * This encodes the mapping between a TLSCAID and the associated root certificate.
     */
    public static class TLSCertStruct {
        /**
         * This field shall be a TLSCAID representing the unique Certificate Authority ID.
         */
        public Integer caid; // TLSCAID
        /**
         * This field shall be an octet string that represents a certificate encoded using DER encoding.
         * When this field exists and is read over a Large Message capable transport, it shall be included. When this
         * field exists and is read over a non Large Message capable transport, it shall NOT be included. To get the
         * full details of a certificate use the FindRootCertificate command.
         */
        public OctetString certificate; // octstr
        public Integer fabricIndex; // FabricIndex

        public TLSCertStruct(Integer caid, OctetString certificate, Integer fabricIndex) {
            this.caid = caid;
            this.certificate = certificate;
            this.fabricIndex = fabricIndex;
        }
    }

    /**
     * This encodes a TLS Client Certificate and corresponding ICAC chain.
     */
    public static class TLSClientCertificateDetailStruct {
        /**
         * This field shall be a TLSCCDID representing the unique Client Certificate ID.
         */
        public Integer ccdid; // TLSCCDID
        /**
         * This field shall be an octet string that represents a TLS Client Certificate encoded using DER encoding.
         * When this field exists and is read over a Large Message capable transport, it shall be included. When this
         * field exists, is non-NULL, and is read over a non Large Message capable transport, it shall NOT be included.
         * To get the full details of a certificate use the FindClientCertificate command.
         * A NULL value indicates that the TLS Client Certificate Signing Request (CSR) Procedure has not yet completed.
         */
        public OctetString clientCertificate; // octstr
        /**
         * This field shall be a list of octet strings representing one or more ICACs (also encoded using DER) that form
         * a Certificate Chain up to, but not including, the TLSRCAC.
         * When this field exists and is read over a Large Message capable transport, it shall be included. When this
         * field exists, is non-empty, and is read over a non Large Message capable transport, it shall NOT be included.
         * To get the full details of a certificate use the FindClientCertificate command.
         * An empty value means that no intermediate certificates are needed for the TLS Server to validate the
         * ClientCertificate.
         */
        public List<OctetString> intermediateCertificates; // list
        public Integer fabricIndex; // FabricIndex

        public TLSClientCertificateDetailStruct(Integer ccdid, OctetString clientCertificate,
                List<OctetString> intermediateCertificates, Integer fabricIndex) {
            this.ccdid = ccdid;
            this.clientCertificate = clientCertificate;
            this.intermediateCertificates = intermediateCertificates;
            this.fabricIndex = fabricIndex;
        }
    }

    public TlsCertificateManagementCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 2049, "TlsCertificateManagement");
    }

    protected TlsCertificateManagementCluster(BigInteger nodeId, int endpointId, int clusterId, String clusterName) {
        super(nodeId, endpointId, clusterId, clusterName);
    }

    // commands
    /**
     * This command shall provision a newly provided certificate, or rotate an existing one, based on the contents of
     * the CAID field.
     */
    public static ClusterCommand provisionRootCertificate(OctetString certificate, Integer caid) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (certificate != null) {
            map.put("certificate", certificate);
        }
        if (caid != null) {
            map.put("caid", caid);
        }
        return new ClusterCommand("provisionRootCertificate", map);
    }

    /**
     * This command shall return the specified TLS root certificate, or all provisioned TLS root certificates for the
     * accessing fabric, based on the contents of the CAID field.
     */
    public static ClusterCommand findRootCertificate(Integer caid) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (caid != null) {
            map.put("caid", caid);
        }
        return new ClusterCommand("findRootCertificate", map);
    }

    /**
     * This command shall return the CAID for the passed in fingerprint.
     */
    public static ClusterCommand lookupRootCertificate(OctetString fingerprint) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (fingerprint != null) {
            map.put("fingerprint", fingerprint);
        }
        return new ClusterCommand("lookupRootCertificate", map);
    }

    /**
     * This command shall be generated to request the server removes the certificate provisioned to the provided
     * Certificate Authority ID.
     */
    public static ClusterCommand removeRootCertificate(Integer caid) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (caid != null) {
            map.put("caid", caid);
        }
        return new ClusterCommand("removeRootCertificate", map);
    }

    /**
     * This command shall be generated to request the Node generates a certificate signing request for a new TLS key
     * pair or use an existing CCDID for certificate rotation.
     */
    public static ClusterCommand clientCsr(OctetString nonce, Integer ccdid) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (nonce != null) {
            map.put("nonce", nonce);
        }
        if (ccdid != null) {
            map.put("ccdid", ccdid);
        }
        return new ClusterCommand("clientCsr", map);
    }

    /**
     * This command shall be generated to request the Node provisions newly provided Client Certificate Details, or
     * rotate an existing client certificate.
     * This command is typically invoked after having created a new client certificate using the CSR requested in
     * ClientCSR, with the TLSCCDID returned by ClientCSRResponse.
     */
    public static ClusterCommand provisionClientCertificate(Integer ccdid, OctetString clientCertificate,
            List<OctetString> intermediateCertificates) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (ccdid != null) {
            map.put("ccdid", ccdid);
        }
        if (clientCertificate != null) {
            map.put("clientCertificate", clientCertificate);
        }
        if (intermediateCertificates != null) {
            map.put("intermediateCertificates", intermediateCertificates);
        }
        return new ClusterCommand("provisionClientCertificate", map);
    }

    /**
     * This command shall return the TLSClientCertificateDetailStruct for the passed in CCDID, or all TLS client
     * certificates for the accessing fabric, based on the contents of the CCDID field.
     */
    public static ClusterCommand findClientCertificate(Integer ccdid) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (ccdid != null) {
            map.put("ccdid", ccdid);
        }
        return new ClusterCommand("findClientCertificate", map);
    }

    /**
     * This command shall return the CCDID for the passed in Fingerprint.
     */
    public static ClusterCommand lookupClientCertificate(OctetString fingerprint) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (fingerprint != null) {
            map.put("fingerprint", fingerprint);
        }
        return new ClusterCommand("lookupClientCertificate", map);
    }

    /**
     * This command shall be used to request the Node removes all stored information for the provided CCDID.
     */
    public static ClusterCommand removeClientCertificate(Integer ccdid) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (ccdid != null) {
            map.put("ccdid", ccdid);
        }
        return new ClusterCommand("removeClientCertificate", map);
    }

    @Override
    public @NonNull String toString() {
        String str = "";
        str += "maxRootCertificates : " + maxRootCertificates + "\n";
        str += "provisionedRootCertificates : " + provisionedRootCertificates + "\n";
        str += "maxClientCertificates : " + maxClientCertificates + "\n";
        str += "provisionedClientCertificates : " + provisionedClientCertificates + "\n";
        return str;
    }
}
