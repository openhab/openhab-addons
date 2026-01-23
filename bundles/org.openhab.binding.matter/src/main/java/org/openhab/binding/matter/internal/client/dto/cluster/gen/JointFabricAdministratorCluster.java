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
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.binding.matter.internal.client.dto.cluster.ClusterCommand;

/**
 * JointFabricAdministrator
 *
 * @author Dan Cunningham - Initial contribution
 */
public class JointFabricAdministratorCluster extends BaseCluster {

    public static final int CLUSTER_ID = 0x0753;
    public static final String CLUSTER_NAME = "JointFabricAdministrator";
    public static final String CLUSTER_PREFIX = "jointFabricAdministrator";
    public static final String ATTRIBUTE_ADMINISTRATOR_FABRIC_INDEX = "administratorFabricIndex";

    /**
     * The AdministratorFabricIndex attribute shall indicate the FabricIndex from the Endpoint 0’s Operational Cluster
     * Fabrics attribute (i.e. the Fabric Table) which is associated with the JointFabric. This field shall have the
     * value of null if there is no fabric associated with the JointFabric.
     */
    public Integer administratorFabricIndex; // 0 fabric-idx A

    // Enums
    /**
     * This enumeration is used by the ICACResponse command to convey the outcome of this cluster’s operations.
     */
    public enum ICACResponseStatusEnum implements MatterEnum {
        OK(0, "Ok"),
        INVALID_PUBLIC_KEY(1, "Invalid Public Key"),
        INVALID_ICAC(2, "Invalid Icac");

        private final Integer value;
        private final String label;

        private ICACResponseStatusEnum(Integer value, String label) {
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

        private final Integer value;
        private final String label;

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

    public enum StatusCodeEnum implements MatterEnum {
        BUSY(2, "Busy"),
        PAKE_PARAMETER_ERROR(3, "Pake Parameter Error"),
        WINDOW_NOT_OPEN(4, "Window Not Open"),
        VID_NOT_VERIFIED(5, "Vid Not Verified"),
        INVALID_ADMINISTRATOR_FABRIC_INDEX(6, "Invalid Administrator Fabric Index");

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

    public JointFabricAdministratorCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 1875, "JointFabricAdministrator");
    }

    protected JointFabricAdministratorCluster(BigInteger nodeId, int endpointId, int clusterId, String clusterName) {
        super(nodeId, endpointId, clusterId, clusterName);
    }

    // commands
    /**
     * This command shall be generated during Joint Commissioning Method and subsequently be responded in the form of an
     * ICACCSRResponse command.
     * If this command is received without an armed fail-safe context (see Section 11.10.7.2, “ArmFailSafe Command”),
     * then this command shall fail with a FAILSAFE_REQUIRED status code sent back to the initiator.
     * If this command is received from a peer against FabricFabric Table Vendor ID Verification Procedure hasn’t been
     * executed then it shall fail with a JfVidNotVerified status code sent back to the initiator.
     * If a prior AddICAC command was successfully executed within the fail-safe timer period, then this command shall
     * fail with a CONSTRAINT_ERROR status code sent back to the initiator.
     */
    public static ClusterCommand icaccsrRequest() {
        return new ClusterCommand("icaccsrRequest");
    }

    /**
     * This command shall be generated and executed during Joint Commissioning Method and subsequently be responded in
     * the form of an ICACResponse command.
     * A Commissioner or Administrator shall issue this command after issuing the ICACCSRRequest command and receiving
     * its response.
     * A Commissioner or Administrator shall issue this command after performing the Attestation Procedure, Fabric Table
     * VID Verification and after validating that the peer is authorized to act as an Administrator in its own Fabric.
     * Check ICA Cross Signing for details about the generation of ICACValue.
     */
    public static ClusterCommand addIcac(OctetString icacValue) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (icacValue != null) {
            map.put("icacValue", icacValue);
        }
        return new ClusterCommand("addIcac", map);
    }

    /**
     * &gt; [!NOTE]
     * &gt; This is an alias onto the OpenCommissioningWindow command within the Joint Fabric Administrator Cluster.
     * Refer to the OpenCommissioningWindow command for a description of the command behavior and parameters.
     * This command shall fail with a InvalidAdministratorFabricIndex status code sent back to the initiator if the
     * AdministratorFabricIndex field has the value of null.
     * The parameters for OpenJointCommissioningWindow command are as follows:
     */
    public static ClusterCommand openJointCommissioningWindow(Integer commissioningTimeout,
            OctetString pakePasscodeVerifier, Integer discriminator, Integer iterations, OctetString salt) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (commissioningTimeout != null) {
            map.put("commissioningTimeout", commissioningTimeout);
        }
        if (pakePasscodeVerifier != null) {
            map.put("pakePasscodeVerifier", pakePasscodeVerifier);
        }
        if (discriminator != null) {
            map.put("discriminator", discriminator);
        }
        if (iterations != null) {
            map.put("iterations", iterations);
        }
        if (salt != null) {
            map.put("salt", salt);
        }
        return new ClusterCommand("openJointCommissioningWindow", map);
    }

    /**
     * This command shall be sent by a candidate Joint Fabric Anchor Administrator to the current Joint Fabric Anchor
     * Administrator to request transfer of the Anchor Fabric.
     */
    public static ClusterCommand transferAnchorRequest() {
        return new ClusterCommand("transferAnchorRequest");
    }

    /**
     * This command shall indicate the completion of the transfer of the Anchor Fabric to another Joint Fabric Ecosystem
     * Administrator.
     */
    public static ClusterCommand transferAnchorComplete() {
        return new ClusterCommand("transferAnchorComplete");
    }

    /**
     * This command shall be used for communicating to client the endpoint that holds the Joint Fabric Administrator
     * Cluster.
     * ### This field shall contain the unique identifier for the endpoint that holds the Joint Fabric Administrator
     * Cluster.
     */
    public static ClusterCommand announceJointFabricAdministrator(Integer endpointId) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (endpointId != null) {
            map.put("endpointId", endpointId);
        }
        return new ClusterCommand("announceJointFabricAdministrator", map);
    }

    @Override
    public @NonNull String toString() {
        String str = "";
        str += "administratorFabricIndex : " + administratorFabricIndex + "\n";
        return str;
    }
}
