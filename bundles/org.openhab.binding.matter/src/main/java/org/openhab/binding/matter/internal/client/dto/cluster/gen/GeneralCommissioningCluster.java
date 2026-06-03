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
 * GeneralCommissioning
 *
 * @author Dan Cunningham - Initial contribution
 */
public class GeneralCommissioningCluster extends BaseCluster {

    public static final int CLUSTER_ID = 0x0030;
    public static final String CLUSTER_NAME = "GeneralCommissioning";
    public static final String CLUSTER_PREFIX = "generalCommissioning";
    public static final String ATTRIBUTE_FEATURE_MAP = "featureMap";
    public static final String ATTRIBUTE_BREADCRUMB = "breadcrumb";
    public static final String ATTRIBUTE_BASIC_COMMISSIONING_INFO = "basicCommissioningInfo";
    public static final String ATTRIBUTE_REGULATORY_CONFIG = "regulatoryConfig";
    public static final String ATTRIBUTE_LOCATION_CAPABILITY = "locationCapability";
    public static final String ATTRIBUTE_SUPPORTS_CONCURRENT_CONNECTION = "supportsConcurrentConnection";
    public static final String ATTRIBUTE_TC_ACCEPTED_VERSION = "tcAcceptedVersion";
    public static final String ATTRIBUTE_TC_MIN_REQUIRED_VERSION = "tcMinRequiredVersion";
    public static final String ATTRIBUTE_TC_ACKNOWLEDGEMENTS = "tcAcknowledgements";
    public static final String ATTRIBUTE_TC_ACKNOWLEDGEMENTS_REQUIRED = "tcAcknowledgementsRequired";
    public static final String ATTRIBUTE_TC_UPDATE_DEADLINE = "tcUpdateDeadline";
    public static final String ATTRIBUTE_RECOVERY_IDENTIFIER = "recoveryIdentifier";
    public static final String ATTRIBUTE_NETWORK_RECOVERY_REASON = "networkRecoveryReason";
    public static final String ATTRIBUTE_IS_COMMISSIONING_WITHOUT_POWER = "isCommissioningWithoutPower";

    public FeatureMap featureMap; // 65532 FeatureMap
    /**
     * This attribute allows for the storage of a client-provided small payload which Administrators and Commissioners
     * may write and then subsequently read, to keep track of their own progress. This may be used by the Commissioner
     * to avoid repeating already-executed actions upon re-establishing a commissioning link after an error.
     * On start/restart of the server, such as when a device is power-cycled, this attribute shall be reset to zero.
     * Some commands related to commissioning also have a side-effect of updating or resetting this attribute and this
     * is specified in their respective functional descriptions.
     * The format of the value within this attribute is unspecified and its value is not otherwise used by the
     * functioning of any cluster, other than being set as a side-effect of commands where this behavior is described.
     */
    public BigInteger breadcrumb; // 0 uint64 RW VA
    /**
     * This attribute shall describe critical parameters needed at the beginning of commissioning flow. See Section
     * 11.10.5.4, "BasicCommissioningInfo" for more information.
     */
    public BasicCommissioningInfo basicCommissioningInfo; // 1 BasicCommissioningInfo R V
    /**
     * Indicates the regulatory configuration for the product.
     * Note that the country code is part of Section 11.1, "Basic Information Cluster" and therefore NOT listed on the
     * RegulatoryConfig attribute.
     */
    public RegulatoryLocationTypeEnum regulatoryConfig; // 2 RegulatoryLocationTypeEnum R V
    /**
     * LocationCapability is statically set by the manufacturer and indicates if this Node needs to be told an exact
     * RegulatoryLocation. For example a Node which is "Indoor Only" would not be certified for outdoor use at all, and
     * thus there is no need for a commissioner to set or ask the user about whether the device will be used inside or
     * outside. However a device which states its capability is "Indoor/Outdoor" means it would like clarification if
     * possible.
     * For Nodes without radio network interfaces (e.g. Ethernet-only devices), the value IndoorOutdoor shall always be
     * used.
     * The default value of the RegulatoryConfig attribute is the value of LocationCapability attribute. This means
     * devices always have a safe default value, and Commissioners which choose to implement smarter handling can.
     */
    public RegulatoryLocationTypeEnum locationCapability; // 3 RegulatoryLocationTypeEnum R V
    /**
     * Indicates whether this device supports "concurrent connection flow" commissioning mode (see Section 5.5,
     * "Commissioning Flows"). If false, the device only supports "non-concurrent connection flow" mode.
     */
    public Boolean supportsConcurrentConnection; // 4 bool R V
    /**
     * Indicates the last version of the T&Cs for which the device received user acknowledgements. On factory reset this
     * field shall be reset to 0.
     * When Custom Commissioning Flow is used to obtain user consent (e. g. because the Commissioner does not support
     * the TC feature), the manufacturer-provided means for obtaining user consent shall ensure that this attribute is
     * set to a value which is greater than or equal to TCMinRequiredVersion before returning the user back to the
     * originating Commissioner (see Section 5.7.4, "Enhanced Setup Flow (ESF)").
     */
    public Integer tcAcceptedVersion; // 5 uint16 R A
    /**
     * Indicates the minimum version of the texts presented by the Enhanced Setup Flow that need to be accepted by the
     * user for this device. This attribute may change as the result of an OTA update.
     * If an event such as a software update causes TCAcceptedVersion to become less than TCMinRequiredVersion, then the
     * device shall update TCAcknowledgementsRequired to True so that an administrator can detect that a newer version
     * of the texts needs to be presented to the user.
     */
    public Integer tcMinRequiredVersion; // 6 uint16 R A
    /**
     * Indicates the user’s response to the presented terms. Each bit position corresponds to a user response for the
     * associated index of matching text, such that bit 0 (bit value 1) is for text index 0. Bit 15 (bit value 0x8000)
     * is for text index 15. A bit value of 1 indicates acceptance and a value of 0 indicates non-acceptance. For
     * example, if there are two texts that were presented where the first (bit 0, value 1) was declined and the second
     * accepted (bit 1, value 2), we would expect the resulting value of the map to be 2.
     * Whenever a user provides responses to newly presented terms and conditions, this attribute shall be updated with
     * the latest responses. This may happen in response to updated terms that were presented to the user. On a factory
     * reset this field shall be reset with all bits set to 0.
     */
    public TcAcknowledgements tcAcknowledgements; // 7 map16 R A
    /**
     * Indicates whether SetTCAcknowledgements is currently required to be called with the inclusion of mandatory terms
     * accepted.
     * This attribute may be present and False in the case where no terms and conditions are currently mandatory to
     * accept for CommissioningComplete command to succeed.
     * This attribute may appear, or become True after commissioning (e.g. due to a firmware update) to indicate that
     * new Terms & Conditions are available that the user must accept.
     * Upon Factory Data Reset, this attribute shall be set to a value of True.
     * When Section 5.7.3, "Custom Commissioning Flow" is used to obtain user consent (e.g. because the Commissioner
     * does not support the TC feature), the manufacturer-provided means for obtaining user consent shall ensure that
     * this attribute is set to False before returning the user back to the original Commissioner (see Section 5.7.4,
     * "Enhanced Setup Flow (ESF)").
     */
    public Boolean tcAcknowledgementsRequired; // 8 bool R A
    /**
     * Indicates the System Time in seconds when any functionality limitations will begin due to a lack of acceptance of
     * updated Terms and Conditions, as described in Section 5.7.4.6, "Presenting Updated Terms and Conditions".
     * A null value indicates that there is no pending deadline for updated TC acceptance.
     */
    public Integer tcUpdateDeadline; // 9 uint32 R A
    /**
     * This attribute shall contain the identifier to be included in the advertisements used during the Network Recovery
     * Flow. This identifier is intended to be advertised over the air and used by an Administrator to establish a
     * Node's identity without revealing its Node ID.
     * The attribute shall contain a random 64-bit value, that value shall be reset on factory reset and shall remain
     * unchanged until a next factory reset. It is important that this value be selected at random from a 64-bit number
     * space to ensure a high likelihood of uniqueness from values selected by other Nodes. This value SHOULD be
     * obtained through Crypto_DRBG(len = 64).
     */
    public OctetString recoveryIdentifier; // 10 octstr R M
    /**
     * This attribute shall contain the primary reason that triggered the Network Recovery flow and its associated
     * advertisements. Null when the Node is not undergoing a Network Recovery flow.
     */
    public NetworkRecoveryReasonEnum networkRecoveryReason; // 11 NetworkRecoveryReasonEnum R M
    /**
     * The server shall set this attribute to true if and only if is currently operating on the commissioning channel
     * but cannot operate on the operational channel because it is not powered.
     * This may happen during NFC-based commissioning, when the commissioning channel is NFC Transport Layer (NTL),
     * because it can harvest energy from NFC to operate. However, such a Commissionee must be powered on to switch to
     * the operational channel.
     * This attribute is used by the Commissioner as described in step 18 of Section 5.5, "Commissioning Flows". This
     * attribute is linked to the Commissionee behavior after reception of the ConnectNetwork Command.
     */
    public Boolean isCommissioningWithoutPower; // 12 bool R V

    // Structs
    /**
     * This structure provides some constant values that may be of use to all commissioners.
     */
    public static class BasicCommissioningInfo {
        /**
         * This field shall contain a conservative initial duration (in seconds) to set in the FailSafe for the
         * commissioning flow to complete successfully. This may vary depending on the speed or sleepiness of the
         * Commissionee. This value, if used in the Section 11.10.7.2, "ArmFailSafe" command's ExpiryLengthSeconds field
         * SHOULD allow a Commissioner to proceed with a nominal commissioning without having to-rearm the fail-safe,
         * with some margin.
         */
        public Integer failSafeExpiryLengthSeconds; // uint16
        /**
         * This field shall contain a conservative value in seconds denoting the maximum total duration for which a fail
         * safe timer can be re-armed. See Section 11.10.7.2.1, "Fail Safe Context".
         * The value of this field shall be greater than or equal to the FailSafeExpiryLengthSeconds. Absent additional
         * guidelines, it is recommended that the value of this field be aligned with the initial Section 5.4.2.3,
         * "Announcement Duration" and default to 900 seconds.
         */
        public Integer maxCumulativeFailsafeSeconds; // uint16

        public BasicCommissioningInfo(Integer failSafeExpiryLengthSeconds, Integer maxCumulativeFailsafeSeconds) {
            this.failSafeExpiryLengthSeconds = failSafeExpiryLengthSeconds;
            this.maxCumulativeFailsafeSeconds = maxCumulativeFailsafeSeconds;
        }
    }

    // Enums
    /**
     * This enumeration is used by several response commands in this cluster to indicate particular errors.
     */
    public enum CommissioningErrorEnum implements MatterEnum {
        OK(0, "Ok"),
        VALUE_OUTSIDE_RANGE(1, "Value Outside Range"),
        INVALID_AUTHENTICATION(2, "Invalid Authentication"),
        NO_FAIL_SAFE(3, "No Fail Safe"),
        BUSY_WITH_OTHER_ADMIN(4, "Busy With Other Admin"),
        REQUIRED_TC_NOT_ACCEPTED(5, "Required Tc Not Accepted"),
        TC_ACKNOWLEDGEMENTS_NOT_RECEIVED(6, "Tc Acknowledgements Not Received"),
        TC_MIN_VERSION_NOT_MET(7, "Tc Min Version Not Met");

        private final Integer value;
        private final String label;

        private CommissioningErrorEnum(Integer value, String label) {
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
     * This enumeration is used by the RegulatoryConfig and LocationCapability attributes to indicate possible radio
     * usage.
     */
    public enum RegulatoryLocationTypeEnum implements MatterEnum {
        INDOOR(0, "Indoor"),
        OUTDOOR(1, "Outdoor"),
        INDOOR_OUTDOOR(2, "Indoor Outdoor");

        private final Integer value;
        private final String label;

        private RegulatoryLocationTypeEnum(Integer value, String label) {
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
     * This data type is derived from enum8, however, the maximum value of the enumeration shall be less than 15.
     * This enumeration is used by the NetworkRecoveryReason attribute and may be embedded in the beacons advertising
     * the need for Network Recovery.
     */
    public enum NetworkRecoveryReasonEnum implements MatterEnum {
        UNSPECIFIED(0, "Unspecified"),
        AUTH(1, "Auth"),
        VISIBILITY(2, "Visibility");

        private final Integer value;
        private final String label;

        private NetworkRecoveryReasonEnum(Integer value, String label) {
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
     * Indicates the user’s response to the presented terms. Each bit position corresponds to a user response for the
     * associated index of matching text, such that bit 0 (bit value 1) is for text index 0. Bit 15 (bit value 0x8000)
     * is for text index 15. A bit value of 1 indicates acceptance and a value of 0 indicates non-acceptance. For
     * example, if there are two texts that were presented where the first (bit 0, value 1) was declined and the second
     * accepted (bit 1, value 2), we would expect the resulting value of the map to be 2.
     * Whenever a user provides responses to newly presented terms and conditions, this attribute shall be updated with
     * the latest responses. This may happen in response to updated terms that were presented to the user. On a factory
     * reset this field shall be reset with all bits set to 0.
     */
    public static class TcAcknowledgements {
        public TcAcknowledgements() {
        }
    }

    public static class FeatureMap {
        /**
         * 
         * Supports Terms & Conditions acknowledgement
         */
        public boolean termsAndConditions;
        /**
         * 
         * Supports Network Recovery
         */
        public boolean networkRecovery;

        public FeatureMap(boolean termsAndConditions, boolean networkRecovery) {
            this.termsAndConditions = termsAndConditions;
            this.networkRecovery = networkRecovery;
        }
    }

    /**
     * This field shall contain the user responses to the Enhanced Setup Flow Terms & Conditions as a map where each bit
     * set in the bitmap corresponds to an accepted term in the file located at Section 11.23.6.22,
     * "EnhancedSetupFlowTCUrl".
     */
    public static class TcUserResponse {
        public TcUserResponse() {
        }
    }

    public GeneralCommissioningCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 48, "GeneralCommissioning");
    }

    protected GeneralCommissioningCluster(BigInteger nodeId, int endpointId, int clusterId, String clusterName) {
        super(nodeId, endpointId, clusterId, clusterName);
    }

    // commands
    /**
     * This command is used to arm or disarm the fail-safe timer.
     * Success or failure of this command shall be communicated by the ArmFailSafeResponse command, unless some data
     * model validations caused a failure status code to be issued during the processing of the command.
     * If the fail-safe timer is not currently armed, the commissioning window is open, and the command was received
     * over a CASE session, the command shall leave the current fail-safe state unchanged and immediately respond with
     * an ArmFailSafeResponse containing an ErrorCode value of BusyWithOtherAdmin. This is done to allow commissioners,
     * which use PASE connections, the opportunity to use the failsafe during the relatively short commissioning window.
     * Otherwise, the command shall arm or re-arm the "fail-safe timer" with an expiry time set for a duration of
     * ExpiryLengthSeconds, or disarm it, depending on the situation:
     * - If ExpiryLengthSeconds is 0 and the fail-safe timer was already armed and the accessing fabric matches the
     * Fabric currently associated with the fail-safe context, then the fail-safe timer shall be immediately expired
     * (see further below for side-effects of expiration).
     * - If ExpiryLengthSeconds is 0 and the fail-safe timer was not armed, then this command invocation shall lead to a
     * success response with no side-effects against the fail-safe context.
     * - If ExpiryLengthSeconds is non-zero and the fail-safe timer was not currently armed, then the fail-safe timer
     * shall be armed for that duration.
     * - If ExpiryLengthSeconds is non-zero and the fail-safe timer was currently armed, and the accessing Fabric
     * matches the fail-safe context's associated Fabric, then the fail-safe timer shall be re-armed to expire in
     * ExpiryLengthSeconds.
     * - Otherwise, the command shall leave the current fail-safe state unchanged and immediately respond with
     * ArmFailSafeResponse containing an ErrorCode value of BusyWithOtherAdmin, indicating a likely conflict between
     * commissioners.
     * The value of the Breadcrumb field shall be written to the Breadcrumb on successful execution of the command.
     * If the receiver restarts unexpectedly (e.g., power interruption, software crash, or other reset) the receiver
     * shall behave as if the fail-safe timer expired and perform the sequence of clean-up steps listed below.
     * On successful execution of the command, the ErrorCode field of the ArmFailSafeResponse shall be set to OK.
     */
    public static ClusterCommand armFailSafe(Integer expiryLengthSeconds, BigInteger breadcrumb) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (expiryLengthSeconds != null) {
            map.put("expiryLengthSeconds", expiryLengthSeconds);
        }
        if (breadcrumb != null) {
            map.put("breadcrumb", breadcrumb);
        }
        return new ClusterCommand("armFailSafe", map);
    }

    /**
     * This command is used to set the regulatory configuration for the device.
     * This shall add or update the regulatory configuration in the RegulatoryConfig Attribute to the value provided in
     * the NewRegulatoryConfig field.
     * Success or failure of this command shall be communicated by the SetRegulatoryConfigResponse command, unless some
     * data model validations caused a failure status code to be issued during the processing of the command.
     * The CountryCode field shall conforms to ISO 3166-1 alpha-2 and shall be used to set the Location attribute
     * reflected by the Basic Information Cluster.
     * If the server limits some of the values (e.g. locked to a particular country, with no regulatory data for
     * others), then setting regulatory information outside a valid country or location shall still set the Location
     * attribute reflected by the Basic Information Cluster configuration, but the SetRegulatoryConfigResponse replied
     * shall have the ErrorCode field set to ValueOutsideRange error.
     * If the LocationCapability attribute is not Indoor/Outdoor and the NewRegulatoryConfig value received does not
     * match either the Indoor or Outdoor fixed value in LocationCapability, then the SetRegulatoryConfigResponse
     * replied shall have the ErrorCode field set to ValueOutsideRange error and the RegulatoryConfig attribute and
     * associated internal radio configuration shall remain unchanged.
     * If the LocationCapability attribute is set to Indoor/Outdoor, then the RegulatoryConfig attribute shall be set to
     * match the NewRegulatoryConfig field.
     * On successful execution of the command, the ErrorCode field of the SetRegulatoryConfigResponse shall be set to
     * OK.
     * The Breadcrumb field shall be used to atomically set the Breadcrumb attribute on success of this command, when
     * SetRegulatoryConfigResponse has the ErrorCode field set to OK. If the command fails, the Breadcrumb attribute
     * shall be left unchanged.
     */
    public static ClusterCommand setRegulatoryConfig(RegulatoryLocationTypeEnum newRegulatoryConfig, String countryCode,
            BigInteger breadcrumb) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (newRegulatoryConfig != null) {
            map.put("newRegulatoryConfig", newRegulatoryConfig);
        }
        if (countryCode != null) {
            map.put("countryCode", countryCode);
        }
        if (breadcrumb != null) {
            map.put("breadcrumb", breadcrumb);
        }
        return new ClusterCommand("setRegulatoryConfig", map);
    }

    /**
     * This command is used to indicate that the commissioning process is complete.
     * Success or failure of this command shall be communicated by the CommissioningCompleteResponse command, unless
     * some data model validations caused a failure status code to be issued during the processing of the command.
     * This command signals the Server that the Commissioner or Administrator has successfully completed all steps
     * needed during the Fail-Safe period, such as commissioning (see Section 5.5, "Commissioning Flows") or other
     * Administrator operations requiring usage of the Fail Safe timer. It ensures that the Server is configured in a
     * state such that it still has all necessary elements to be fully operable within a Fabric, such as ACL entries
     * (see Section 9.10, "Access Control Cluster") and operational credentials (see Section 6.4, "Node Operational
     * Credentials Specification"), and that the Node is reachable using CASE (see Section 4.14.2, "Certificate
     * Authenticated Session Establishment (CASE)") over an operational network.
     * An ErrorCode of NoFailSafe shall be responded to the invoker if the CommissioningComplete command was received
     * when no Fail-Safe context exists.
     * If Terms and Conditions are required, then an ErrorCode of TCAcknowledgementsNotReceived shall be responded to
     * the invoker if the user acknowledgements to the required Terms and Conditions have not been provided.
     * This command is fabric-scoped, so cannot be issued over a session that does not have an associated fabric, i.e.
     * over PASE session prior to an AddNOC command. In addition, this command is only permitted over CASE and must be
     * issued by a node associated with the ongoing Fail-Safe context. An ErrorCode of InvalidAuthentication shall be
     * responded to the invoker if the CommissioningComplete command was received outside a CASE session (e.g., over
     * Group messaging, or PASE session after AddNOC), or if the accessing fabric is not the one associated with the
     * ongoing Fail-Safe context.
     * This command shall only result in success with an ErrorCode value of OK in the CommissioningCompleteResponse if
     * received over a CASE session and the accessing fabric index matches the Fabric Index associated with the current
     * Fail-Safe context. In other words:
     * - If no AddNOC command had been successfully invoked, the CommissioningComplete command must originate from the
     * Fabric that initiated the Fail-Safe context.
     * - After an AddNOC command has been successfully invoked, the CommissioningComplete command must originate from
     * the Fabric which was joined through the execution of that command, which updated the Fail-Safe context's Fabric
     * Index.
     * On successful execution of the CommissioningComplete command, where the CommissioningCompleteResponse has an
     * ErrorCode of OK, the following actions shall be undertaken on the Server:
     * 1. The Fail-Safe timer associated with the current Fail-Safe context shall be disarmed.
     * 2. The commissioning window at the Server shall be closed.
     * 3. Any temporary administrative privileges automatically granted to any open PASE session shall be revoked (see
     * Section 6.6.2.9, "Bootstrapping of the Access Control Cluster").
     * 4. The Secure Session Context of any PASE session still established at the Server shall be cleared.
     * 5. The Breadcrumb attribute shall be reset to zero.
     * After receipt of a CommissioningCompleteResponse with an ErrorCode value of OK, a client cannot expect any
     * previously established PASE session to still be usable, due to the server having cleared such sessions.
     */
    public static ClusterCommand commissioningComplete() {
        return new ClusterCommand("commissioningComplete");
    }

    /**
     * This command is used to set the user acknowledgements received in the Enhanced Setup Flow Terms & Conditions into
     * the node.
     */
    public static ClusterCommand setTcAcknowledgements(Integer tcVersion, TcUserResponse tcUserResponse) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (tcVersion != null) {
            map.put("tcVersion", tcVersion);
        }
        if (tcUserResponse != null) {
            map.put("tcUserResponse", tcUserResponse);
        }
        return new ClusterCommand("setTcAcknowledgements", map);
    }

    @Override
    public @NonNull String toString() {
        String str = "";
        str += "featureMap : " + featureMap + "\n";
        str += "breadcrumb : " + breadcrumb + "\n";
        str += "basicCommissioningInfo : " + basicCommissioningInfo + "\n";
        str += "regulatoryConfig : " + regulatoryConfig + "\n";
        str += "locationCapability : " + locationCapability + "\n";
        str += "supportsConcurrentConnection : " + supportsConcurrentConnection + "\n";
        str += "tcAcceptedVersion : " + tcAcceptedVersion + "\n";
        str += "tcMinRequiredVersion : " + tcMinRequiredVersion + "\n";
        str += "tcAcknowledgements : " + tcAcknowledgements + "\n";
        str += "tcAcknowledgementsRequired : " + tcAcknowledgementsRequired + "\n";
        str += "tcUpdateDeadline : " + tcUpdateDeadline + "\n";
        str += "recoveryIdentifier : " + recoveryIdentifier + "\n";
        str += "networkRecoveryReason : " + networkRecoveryReason + "\n";
        str += "isCommissioningWithoutPower : " + isCommissioningWithoutPower + "\n";
        return str;
    }
}
