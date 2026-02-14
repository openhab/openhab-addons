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
 * OperationalCredentials
 *
 * @author Dan Cunningham - Initial contribution
 */
public class OperationalCredentialsCluster extends BaseCluster {

    public static final int CLUSTER_ID = 0x003E;
    public static final String CLUSTER_NAME = "OperationalCredentials";
    public static final String CLUSTER_PREFIX = "operationalCredentials";
    public static final String ATTRIBUTE_NOCS = "nocs";
    public static final String ATTRIBUTE_FABRICS = "fabrics";
    public static final String ATTRIBUTE_SUPPORTED_FABRICS = "supportedFabrics";
    public static final String ATTRIBUTE_COMMISSIONED_FABRICS = "commissionedFabrics";
    public static final String ATTRIBUTE_TRUSTED_ROOT_CERTIFICATES = "trustedRootCertificates";
    public static final String ATTRIBUTE_CURRENT_FABRIC_INDEX = "currentFabricIndex";

    /**
     * This attribute shall contain all NOCs applicable to this Node, encoded as a read-only list of NOCStruct.
     * Operational Certificates shall be added through the AddNOC command, and shall be removed through the RemoveFabric
     * command.
     * Upon Factory Data Reset, this attribute shall be set to a default value of an empty list.
     * The number of entries in this list shall match the number of entries in the Fabrics attribute.
     */
    public List<NOCStruct> nocs; // 0 list R F A
    /**
     * Indicates all fabrics to which this Node is commissioned, encoded as a read-only list of FabricDescriptorStruct.
     * This information may be computed directly from the NOCs attribute.
     * The Fabrics attribute is also known as &quot;the fabric table&quot;.
     * Upon Factory Data Reset, this attribute shall be set to a default value of an empty list.
     * The number of entries in this list shall match the number of entries in the NOCs attribute.
     */
    public List<FabricDescriptorStruct> fabrics; // 1 list R F V
    /**
     * Indicates the number of Fabrics that are supported by the device. This value is fixed for a particular device.
     */
    public Integer supportedFabrics; // 2 uint8 R V
    /**
     * Indicates the number of Fabrics to which the device is currently commissioned. This attribute shall be equal to
     * the following:
     * • The number of entries in the NOCs attribute.
     * • The number of entries in the Fabrics attribute.
     * Upon Factory Data Reset, this attribute shall be set to a default value of 0.
     */
    public Integer commissionedFabrics; // 3 uint8 R V
    /**
     * This attribute shall contain the list of Trusted Root CA Certificates (RCAC) installed on the Node, as octet
     * strings containing their Matter Certificate Encoding representation.
     * These certificates are installed through the AddTrustedRootCertificate command.
     * Depending on the method of storage employed by the server, either shared storage for identical root certificates
     * shared by many fabrics, or individually stored root certificate per fabric, multiple identical root certificates
     * may legally appear within the list.
     * To match a root with a given fabric, the root certificate’s subject and subject public key need to be
     * cross-referenced with the NOC or ICAC certificates that appear in the NOCs attribute for a given fabric.
     * Upon Factory Data Reset, this attribute shall be set to a default value whereby the list is empty.
     */
    public List<OctetString> trustedRootCertificates; // 4 list R V
    /**
     * Indicates the accessing fabric index.
     * This attribute is useful to contextualize Fabric-Scoped entries obtained from response commands or attribute
     * reads, since a given Fabric may be referenced by a different Fabric Index locally on a remote Node.
     */
    public Integer currentFabricIndex; // 5 fabric-idx R V

    // Structs
    /**
     * This encodes a NOC chain, underpinning a commissioned Operational Identity for a given Node.
     * &gt; [!NOTE]
     * &gt; The VVSC field is mutually exclusive with the ICAC field. If the ICAC field is non-null, the VVSC field
     * shall be omitted. If the VVSC field is present in the structure, the ICAC field shall be null. The reason for
     * this is to optimize storage usage, as the VID Verification Signer Certificate (VVSC) is a field that is only
     * needed in root-per-fabric situations without ICAC present.
     * &gt; [!NOTE]
     * &gt; The Trusted Root CA Certificate (RCAC) is not included in this structure. The roots are available in the
     * TrustedRootCertificates attribute under the same associated fabric as the one for the NOCStruct entry.
     */
    public static class NOCStruct {
        /**
         * This field shall contain the NOC for the struct’s associated fabric, encoded using Matter Certificate
         * Encoding.
         */
        public OctetString noc; // octstr
        /**
         * This field shall contain the ICAC for the struct’s associated fabric, encoded using Matter Certificate
         * Encoding. If no ICAC is present in the chain, this field shall be set to null.
         */
        public OctetString icac; // octstr
        /**
         * This field shall contain the Vendor Verification Signer Certificate (VVSC) for the struct’s associated
         * fabric, encoded using Matter Certificate Encoding. If no VVSC is needed, this field shall be omitted (in that
         * there shall NOT be a value present, not even an empty octet string). If the ICAC field is non-null, this
         * field shall NOT be present.
         */
        public OctetString vvsc; // octstr
        public Integer fabricIndex; // FabricIndex

        public NOCStruct(OctetString noc, OctetString icac, OctetString vvsc, Integer fabricIndex) {
            this.noc = noc;
            this.icac = icac;
            this.vvsc = vvsc;
            this.fabricIndex = fabricIndex;
        }
    }

    /**
     * This structure encodes a Fabric Reference for a fabric within which a given Node is currently commissioned.
     */
    public static class FabricDescriptorStruct {
        /**
         * This field shall contain the public key for the trusted root that scopes the fabric referenced by FabricIndex
         * and its associated operational credential (see Section 6.4.5.3, “Trusted Root CA Certificates”). The format
         * for the key shall be the same as that used in the ec-pub-key field of the Matter Certificate Encoding for the
         * root in the operational certificate chain.
         */
        public OctetString rootPublicKey; // octstr
        /**
         * This field shall contain the value of VendorID associated with the fabric.
         * This value shall have been provided by the AdminVendorID value provided in the AddNOC command that led to the
         * creation of this FabricDescriptorStruct, or the value updated via the SetVIDVerificationStatement command,
         * whichever was last completed. The set of allowed values is defined in AdminVendorID.
         * The intent is to provide user transparency about which entities have Administer privileges on the Node.
         * Clients shall consider the VendorID field value to be untrustworthy until the Fabric Table Vendor ID
         * Verification Procedure has been executed against the fabric entry having this VendorID.
         */
        public Integer vendorId; // vendor-id
        /**
         * This field shall contain the FabricID allocated to the fabric referenced by FabricIndex. This field shall
         * match the value found in the matter-fabric-id field from the operational certificate providing the
         * operational identity under this Fabric.
         */
        public BigInteger fabricId; // fabric-id
        /**
         * This field shall contain the NodeID in use within the fabric referenced by FabricIndex. This field shall
         * match the value found in the matter-node-id field from the operational certificate providing this operational
         * identity.
         */
        public BigInteger nodeId; // node-id
        /**
         * This field shall contain a commissioner-set label for the fabric referenced by FabricIndex. This field is set
         * by the UpdateFabricLabel command.
         */
        public String label; // string
        /**
         * This field, if present, shall contain a previously-installed administrator-set vid_verification_statement
         * value (see Section 6.4.10, “Fabric Table Vendor ID Verification Procedure”) for the fabric referenced by
         * FabricIndex. This field is set by the SetVIDVerificationStatement command.
         */
        public OctetString vidVerificationStatement; // octstr
        public Integer fabricIndex; // FabricIndex

        public FabricDescriptorStruct(OctetString rootPublicKey, Integer vendorId, BigInteger fabricId,
                BigInteger nodeId, String label, OctetString vidVerificationStatement, Integer fabricIndex) {
            this.rootPublicKey = rootPublicKey;
            this.vendorId = vendorId;
            this.fabricId = fabricId;
            this.nodeId = nodeId;
            this.label = label;
            this.vidVerificationStatement = vidVerificationStatement;
            this.fabricIndex = fabricIndex;
        }
    }

    // Enums
    /**
     * This enumeration is used by the CertificateChainRequest command to convey which certificate from the device
     * attestation certificate chain to transmit back to the client.
     */
    public enum CertificateChainTypeEnum implements MatterEnum {
        DAC_CERTIFICATE(1, "Dac Certificate"),
        PAI_CERTIFICATE(2, "Pai Certificate");

        private final Integer value;
        private final String label;

        private CertificateChainTypeEnum(Integer value, String label) {
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
     * This enumeration is used by the NOCResponse common response command to convey detailed outcome of several of this
     * cluster’s operations.
     */
    public enum NodeOperationalCertStatusEnum implements MatterEnum {
        OK(0, "Ok"),
        INVALID_PUBLIC_KEY(1, "Invalid Public Key"),
        INVALID_NODE_OP_ID(2, "Invalid Node Op Id"),
        INVALID_NOC(3, "Invalid Noc"),
        MISSING_CSR(4, "Missing Csr"),
        TABLE_FULL(5, "Table Full"),
        INVALID_ADMIN_SUBJECT(6, "Invalid Admin Subject"),
        FABRIC_CONFLICT(9, "Fabric Conflict"),
        LABEL_CONFLICT(10, "Label Conflict"),
        INVALID_FABRIC_INDEX(11, "Invalid Fabric Index");

        private final Integer value;
        private final String label;

        private NodeOperationalCertStatusEnum(Integer value, String label) {
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

    public OperationalCredentialsCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 62, "OperationalCredentials");
    }

    protected OperationalCredentialsCluster(BigInteger nodeId, int endpointId, int clusterId, String clusterName) {
        super(nodeId, endpointId, clusterId, clusterName);
    }

    // commands
    /**
     * This command is used to perform an attestation request.
     * This command shall be generated to request the Attestation Information, in the form of an AttestationResponse
     * Command. If the AttestationNonce that is provided in the command is malformed, a recipient shall fail the command
     * with a Status Code of INVALID_COMMAND. The AttestationNonce field shall be used in the computation of the
     * Attestation Information.
     */
    public static ClusterCommand attestationRequest(OctetString attestationNonce) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (attestationNonce != null) {
            map.put("attestationNonce", attestationNonce);
        }
        return new ClusterCommand("attestationRequest", map);
    }

    /**
     * This command is used to request a certificate from the device attestation certificate chain.
     * If the CertificateType is not a valid value per CertificateChainTypeEnum then the command shall fail with a
     * Status Code of INVALID_COMMAND.
     */
    public static ClusterCommand certificateChainRequest(CertificateChainTypeEnum certificateType) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (certificateType != null) {
            map.put("certificateType", certificateType);
        }
        return new ClusterCommand("certificateChainRequest", map);
    }

    /**
     * This command is used to perform a CSR request.
     * This command shall be generated to execute the Node Operational CSR Procedure and subsequently return the NOCSR
     * Information, in the form of a CSRResponse Command.
     * The CSRNonce field shall be used in the computation of the NOCSR Information. If the CSRNonce is malformed, then
     * this command shall fail with an INVALID_COMMAND status code.
     * If the IsForUpdateNOC field is present and set to true, but the command was received over a PASE session, the
     * command shall fail with an INVALID_COMMAND status code, as it would never be possible to use a resulting
     * subsequent certificate issued from the CSR with the UpdateNOC command, which is forbidden over PASE sessions.
     * If the IsForUpdateNOC field is present and set to true, the internal state of the CSR associated key pair shall
     * be tagged as being for a subsequent UpdateNOC, otherwise the internal state of the CSR shall be tagged as being
     * for a subsequent AddNOC. See Section 11.18.6.8, “AddNOC Command” and Section 11.18.6.9, “UpdateNOC Command” for
     * details about the processing.
     * If this command is received without an armed fail-safe context (see Section 11.10.7.2, “ArmFailSafe Command”),
     * then this command shall fail with a FAILSAFE_REQUIRED status code sent back to the initiator.
     * If a prior UpdateNOC or AddNOC command was successfully executed within the fail-safe timer period, then this
     * command shall fail with a CONSTRAINT_ERROR status code sent back to the initiator.
     * If the Node Operational Key Pair generated during processing of the Node Operational CSR Procedure is found to
     * collide with an existing key pair already previously generated and installed, and that check had been executed,
     * then this command shall fail with a FAILURE status code sent back to the initiator.
     */
    public static ClusterCommand csrRequest(OctetString csrNonce, Boolean isForUpdateNoc) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (csrNonce != null) {
            map.put("csrNonce", csrNonce);
        }
        if (isForUpdateNoc != null) {
            map.put("isForUpdateNoc", isForUpdateNoc);
        }
        return new ClusterCommand("csrRequest", map);
    }

    /**
     * This command is used to add a new NOC to the device.
     * This command shall add a new NOC chain to the device and commission a new Fabric association upon successful
     * validation of all arguments and preconditions.
     * The new value shall immediately be reflected in the NOCs list attribute.
     * A Commissioner or Administrator shall issue this command after issuing the CSRRequest command and receiving its
     * response.
     * A Commissioner or Administrator SHOULD issue this command after performing the Attestation Procedure.
     */
    public static ClusterCommand addNoc(OctetString nocValue, OctetString icacValue, OctetString ipkValue,
            BigInteger caseAdminSubject, Integer adminVendorId) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (nocValue != null) {
            map.put("nocValue", nocValue);
        }
        if (icacValue != null) {
            map.put("icacValue", icacValue);
        }
        if (ipkValue != null) {
            map.put("ipkValue", ipkValue);
        }
        if (caseAdminSubject != null) {
            map.put("caseAdminSubject", caseAdminSubject);
        }
        if (adminVendorId != null) {
            map.put("adminVendorId", adminVendorId);
        }
        return new ClusterCommand("addNoc", map);
    }

    /**
     * This command is used to update an existing NOC on the device.
     * This command shall replace the NOC and optional associated ICAC (if present) scoped under the accessing fabric
     * upon successful validation of all arguments and preconditions. The new value shall immediately be reflected in
     * the NOCs list attribute.
     * A Commissioner or Administrator shall issue this command after issuing the CSRRequest Command and receiving its
     * response.
     * A Commissioner or Administrator SHOULD issue this command after performing the Attestation Procedure.
     * ### Effect on Receipt
     * If this command is received without an armed fail-safe context (see Section 11.10.7.2, “ArmFailSafe Command”),
     * then this command shall fail with a FAILSAFE_REQUIRED status code sent back to the initiator.
     * If a prior UpdateNOC or AddNOC command was successfully executed within the fail-safe timer period, then this
     * command shall fail with a CONSTRAINT_ERROR status code sent back to the initiator.
     * If a prior AddTrustedRootCertificate command was successfully invoked within the fail-safe timer period, then
     * this command shall fail with a CONSTRAINT_ERROR status code sent back to the initiator, since the only valid
     * following logical operation is invoking the AddNOC command.
     * If the prior CSRRequest state that preceded UpdateNOC had the IsForUpdateNOC field indicated as false, then this
     * command shall fail with a CONSTRAINT_ERROR status code sent back to the initiator.
     * If any of the following conditions arise, the Node shall process an error by responding with an NOCResponse with
     * a StatusCode of InvalidNOC as described in Section 11.18.6.7.2, “Handling Errors”:
     * • The NOC provided in the NOCValue does not refer in its subject to the FabricID associated with the accessing
     * fabric.
     * • The ICAC provided in the ICACValue (if present) has a FabricID in its subject that does not match the FabricID
     * associated with the accessing fabric.
     * Otherwise, the command is considered an update of existing credentials for a given Fabric, and the following
     * shall apply:
     * 1. The Operational Certificate under the accessing fabric index in the NOCs list shall be updated to match the
     * incoming NOCValue and ICACValue (if present), such that the Node’s Operational Identifier within the Fabric
     * immediately changes.
     * a. The operational key pair associated with the incoming NOC from the NOCValue, and generated by the prior
     * CSRRequest command, shall be committed to permanent storage, for subsequent use during CASE.
     * b. The operational discovery service record shall immediately reflect the new Operational Identifier.
     * c. All internal data reflecting the prior operational identifier of the Node within the Fabric shall be revoked
     * and removed, to an outcome equivalent to the disappearance of the prior Node, except for the ongoing CASE session
     * context, which shall temporarily remain valid until the NOCResponse has been successfully delivered or until the
     * next transport-layer error, so that the response can be received by the Administrator invoking the command.
     * Thereafter, the Node shall respond with an NOCResponse with a StatusCode of OK and a FabricIndex field matching
     * the FabricIndex under which the updated NOC is scoped.
     */
    public static ClusterCommand updateNoc(OctetString nocValue, OctetString icacValue, Integer fabricIndex) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (nocValue != null) {
            map.put("nocValue", nocValue);
        }
        if (icacValue != null) {
            map.put("icacValue", icacValue);
        }
        if (fabricIndex != null) {
            map.put("fabricIndex", fabricIndex);
        }
        return new ClusterCommand("updateNoc", map);
    }

    /**
     * This command is used to set the user-visible fabric label for a given Fabric.
     * This command shall be used by an Administrator to set the user-visible Label field for a given Fabric, as
     * reflected by entries in the Fabrics attribute. An Administrator shall use this command to set the Label to a
     * string (possibly selected by the user themselves) that the user can recognize and relate to this Administrator
     * • during the commissioning process, and
     * • whenever the user chooses to update this string.
     * The Label field, along with the VendorID field in the same entry of the Fabrics attribute, SHOULD be used by
     * Administrators to provide additional per-fabric context when operations such as RemoveFabric are considered or
     * used.
     */
    public static ClusterCommand updateFabricLabel(String label, Integer fabricIndex) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (label != null) {
            map.put("label", label);
        }
        if (fabricIndex != null) {
            map.put("fabricIndex", fabricIndex);
        }
        return new ClusterCommand("updateFabricLabel", map);
    }

    /**
     * This command is used to remove a Fabric from the device.
     * This command is used by Administrators to remove a given Fabric and delete all associated fabric-scoped data.
     * If the given Fabric being removed is the last one to reference a given Trusted Root CA Certificate stored in the
     * Trusted Root Certificates list, then that Trusted Root Certificate shall be removed.
     * ### WARNING
     * This command, if referring to an already existing Fabric not under the control of the invoking Administrator,
     * shall ONLY be invoked after obtaining some form of explicit user consent through some method executed by the
     * Administrator or Commissioner. This method of obtaining consent SHOULD employ as much data as possible about the
     * existing Fabric associations within the Fabrics list, so that likelihood is as small as possible of a user
     * removing a Fabric unwittingly. If a method exists for an Administrator or Commissioner to convey Fabric Removal
     * to an entity related to that Fabric, whether in-band or out-of-band, then this method SHOULD be used to notify
     * the other Administrative Domain’s party of the removal. Otherwise, users may only observe the removal of a Fabric
     * association as persistently failing attempts to reach a Node operationally.
     */
    public static ClusterCommand removeFabric(Integer fabricIndex) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (fabricIndex != null) {
            map.put("fabricIndex", fabricIndex);
        }
        return new ClusterCommand("removeFabric", map);
    }

    /**
     * This command is used to add a trusted root certificate to the device.
     * This command shall add a Trusted Root CA Certificate, provided as its Matter Certificate Encoding representation,
     * to the TrustedRootCertificates Attribute list and shall ensure the next AddNOC command executed uses the provided
     * certificate as its root of trust.
     * If the certificate from the RootCACertificate field is already installed, based on exact byte-for-byte equality,
     * then this command shall succeed with no change to the list.
     * If this command is received without an armed fail-safe context (see Section 11.10.7.2, “ArmFailSafe Command”),
     * then this command shall fail with a FAILSAFE_REQUIRED status code sent back to the initiator.
     * If a prior AddTrustedRootCertificate command was successfully invoked within the fail-safe timer period, which
     * would cause the new invocation to add a second root certificate within a given fail-safe timer period, then this
     * command shall fail with a CONSTRAINT_ERROR status code sent back to the initiator.
     * If a prior UpdateNOC or AddNOC command was successfully executed within the fail-safe timer period, then this
     * command shall fail with a CONSTRAINT_ERROR status code sent back to the initiator.
     * If the certificate from the RootCACertificate field fails any validity checks, not fulfilling all the
     * requirements for a valid Matter Certificate Encoding representation, including a truncated or oversize value,
     * then this command shall fail with an INVALID_COMMAND status code sent back to the initiator.
     * Note that the only method of removing a trusted root is by removing the Fabric that uses it as its root of trust
     * using the RemoveFabric command.
     */
    public static ClusterCommand addTrustedRootCertificate(OctetString rootCaCertificate) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (rootCaCertificate != null) {
            map.put("rootCaCertificate", rootCaCertificate);
        }
        return new ClusterCommand("addTrustedRootCertificate", map);
    }

    /**
     * This command is used to manage the VendorID and VIDVerificationStatement fields of the Fabrics attribute, and the
     * VVSC field of an entry in the NOCs attribute.
     * This command shall be used to one or more of the following:
     * • Update the VendorID associated with an entry in the Fabrics attribute.
     * • Associate or remove a VIDVerificationStatement associated with an entry in the Fabrics attribute.
     * • Associate or remove a VendorVerificationSigningCertificate (VVSC) associated with an entry in the NOCs
     * attribute.
     * This command shall only operate against the Fabrics and NOCs attribute entries associated with the accessing
     * fabric index.
     * ### Effect on Receipt
     * If the VendorID field is present, the value of the VendorID in the Fabrics attribute entry associated with the
     * accessing fabric index shall have its value replaced with the value from the command field.
     * If the VVSC field is present, but the ICAC field is already present in the NOCs attribute entry associated with
     * the accessing fabric index, then the command shall fail with a status code of INVALID_COMMAND.
     * If the VIDVerificationStatement field is present:
     * • If the length of the field’s value is neither exactly 0 nor exactly 85, then the command shall fail with a
     * status code of CONSTRAINT_ERROR.
     * • If the length of the field’s value is exactly 0, then the VIDVerificationStatement field in the Fabrics
     * attribute entry associated with the accessing fabric index shall be erased and the field shall disappear from the
     * Fabrics entry.
     * • If the length of the field’s value is exactly 85, then the VIDVerificationStatement field in the Fabrics
     * attribute entry associated with the accessing fabric index shall have its value replaced with the value from the
     * command field.
     * If the VVSC field is present:
     * • If the length of the field’s value is exactly 0, then the VVSC field in the NOCs attribute entry associated
     * with the accessing fabric index shall be erased and the field shall disappear from the NOCs entry.
     * • If the length of the field’s value is not 0, then the VVSC field in the NOCs attribute entry associated with
     * the accessing fabric index shall have its value replaced with the value from the command field. The contents of
     * the certificate need not be validated by the server. Clients shall validate the contents at time of use.
     * If the command was invoked within a fail-safe context after a successful AddNOC or UpdateNOC command, then the
     * field updates shall apply to the pending update state that will be reverted if fail-safe expires prior to a
     * CommissioningComplete command. In other words, field updates apply to the state of the Fabrics Attribute as
     * currently visible, even for an existing fabric currently in process of being updated.
     */
    public static ClusterCommand setVidVerificationStatement(Integer vendorId, OctetString vidVerificationStatement,
            OctetString vvsc) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (vendorId != null) {
            map.put("vendorId", vendorId);
        }
        if (vidVerificationStatement != null) {
            map.put("vidVerificationStatement", vidVerificationStatement);
        }
        if (vvsc != null) {
            map.put("vvsc", vvsc);
        }
        return new ClusterCommand("setVidVerificationStatement", map);
    }

    /**
     * This command is used to authenticate the fabric associated with the FabricIndex.
     * This command shall be used to request that the server authenticate the fabric associated with the FabricIndex
     * given by generating the response described in Section 6.4.10, “Fabric Table Vendor ID Verification Procedure”.
     * The FabricIndex field shall contain the fabric index being targeted by the request.
     * The ClientChallenge field shall contain a client-provided random challenge to be used during the signature
     * procedure.
     * ### Effect on Receipt
     * If the FabricIndex field contains a fabric index which does not have an associated entry in the Fabrics
     * attribute, then the command shall fail with a status code of CONSTRAINT_ERROR.
     * Otherwise, if no other errors have occurred, the command shall generate a SignVIDVerificationResponse.
     */
    public static ClusterCommand signVidVerificationRequest(Integer fabricIndex, OctetString clientChallenge) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (fabricIndex != null) {
            map.put("fabricIndex", fabricIndex);
        }
        if (clientChallenge != null) {
            map.put("clientChallenge", clientChallenge);
        }
        return new ClusterCommand("signVidVerificationRequest", map);
    }

    @Override
    public @NonNull String toString() {
        String str = "";
        str += "nocs : " + nocs + "\n";
        str += "fabrics : " + fabrics + "\n";
        str += "supportedFabrics : " + supportedFabrics + "\n";
        str += "commissionedFabrics : " + commissionedFabrics + "\n";
        str += "trustedRootCertificates : " + trustedRootCertificates + "\n";
        str += "currentFabricIndex : " + currentFabricIndex + "\n";
        return str;
    }
}
