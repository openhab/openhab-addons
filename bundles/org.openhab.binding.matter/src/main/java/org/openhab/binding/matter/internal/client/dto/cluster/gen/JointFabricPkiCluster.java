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
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.binding.matter.internal.client.dto.cluster.ClusterCommand;

/**
 * JointFabricPki
 *
 * @author Dan Cunningham - Initial contribution
 */
public class JointFabricPkiCluster extends BaseCluster {

    public static final int CLUSTER_ID = 0x0753;
    public static final String CLUSTER_NAME = "JointFabricPki";
    public static final String CLUSTER_PREFIX = "jointFabricPki";
    public static final String ATTRIBUTE_CLUSTER_REVISION = "clusterRevision";

    public Integer clusterRevision; // 65533 ClusterRevision

    // Enums
    /**
     * This enumeration is used by the ICACSRResponse command to convey the detailed outcome of this cluster’s
     * ICACSRRequest command.
     */
    public enum IcacsrRequestStatusEnum implements MatterEnum {
        OK(0, "Ok"),
        INVALID_ICA_CSR_FORMAT(1, "Invalid Ica Csr Format"),
        INVALID_ICA_CSR_SIGNATURE(2, "Invalid Ica Csr Signature"),
        FAILED_DCL_VENDOR_ID_VALIDATION(3, "Failed Dcl Vendor Id Validation"),
        NOT_AN_ICAC(4, "Not An Icac"),
        BUSY_ANCHOR_TRANSFER(5, "Busy Anchor Transfer"),
        ICA_CSR_SIGNING_FAILED(6, "Ica Csr Signing Failed"),
        ICA_CSR_REQUEST_NO_USER_CONSENT(7, "Ica Csr Request No User Consent");

        public final Integer value;
        public final String label;

        private IcacsrRequestStatusEnum(Integer value, String label) {
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
     * This enumeration is used by the TransferAnchorResponse command to convey the detailed outcome of this cluster’s
     * TransferAnchorRequest command.
     */
    public enum TransferAnchorResponseStatusEnum implements MatterEnum {
        OK(0, "Ok"),
        TRANSFER_ANCHOR_STATUS_DATASTORE_BUSY(1, "Transfer Anchor Status Datastore Busy"),
        TRANSFER_ANCHOR_STATUS_NO_USER_CONSENT(2, "Transfer Anchor Status No User Consent");

        public final Integer value;
        public final String label;

        private TransferAnchorResponseStatusEnum(Integer value, String label) {
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

    public JointFabricPkiCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 1875, "JointFabricPki");
    }

    protected JointFabricPkiCluster(BigInteger nodeId, int endpointId, int clusterId, String clusterName) {
        super(nodeId, endpointId, clusterId, clusterName);
    }

    // commands
    /**
     * This command shall be generated and executed during the Joint Commissioning Method steps and subsequently respond
     * in the form of an ICACSRResponse command.
     * Check ICA Cross Signing for details about the generation and contents of the ICACSR.
     */
    public static ClusterCommand icacsrRequest(OctetString icacsr) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (icacsr != null) {
            map.put("icacsr", icacsr);
        }
        return new ClusterCommand("icacsrRequest", map);
    }

    public static ClusterCommand transferAnchorRequest() {
        return new ClusterCommand("transferAnchorRequest");
    }

    public static ClusterCommand transferAnchorComplete() {
        return new ClusterCommand("transferAnchorComplete");
    }

    @Override
    public @NonNull String toString() {
        String str = "";
        str += "clusterRevision : " + clusterRevision + "\n";
        return str;
    }
}
