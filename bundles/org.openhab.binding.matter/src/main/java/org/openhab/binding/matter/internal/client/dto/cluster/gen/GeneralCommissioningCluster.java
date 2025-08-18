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
 * GeneralCommissioning
 *
 * @author Dan Cunningham - Initial contribution
 */
public class GeneralCommissioningCluster extends BaseCluster {

    public static final int CLUSTER_ID = 0x0030;
    public static final String CLUSTER_NAME = "GeneralCommissioning";
    public static final String CLUSTER_PREFIX = "generalCommissioning";
    public static final String ATTRIBUTE_CLUSTER_REVISION = "clusterRevision";
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

    public Integer clusterRevision; // 65533 ClusterRevision
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
     * This attribute shall describe critical parameters needed at the beginning of commissioning flow. See
     * BasicCommissioningInfo for more information.
     */
    public BasicCommissioningInfo basicCommissioningInfo; // 1 BasicCommissioningInfo R V
    /**
     * Indicates the regulatory configuration for the product.
     * Note that the country code is part of Basic Information Cluster and therefore NOT listed on the RegulatoryConfig
     * attribute.
     */
    public RegulatoryLocationTypeEnum regulatoryConfig; // 2 RegulatoryLocationTypeEnum R V
    /**
     * LocationCapability is statically set by the manufacturer and indicates if this Node needs to be told an exact
     * RegulatoryLocation. For example a Node which is &quot;Indoor Only&quot; would not be certified for outdoor use at
     * all, and thus there is no need for a commissioner to set or ask the user about whether the device will be used
     * inside or outside. However a device which states its capability is &quot;Indoor/Outdoor&quot; means it would like
     * clarification if possible.
     * For Nodes without radio network interfaces (e.g. Ethernet-only devices), the value IndoorOutdoor shall always be
     * used.
     * The default value of the RegulatoryConfig attribute is the value of LocationCapability attribute. This means
     * devices always have a safe default value, and Commissioners which choose to implement smarter handling can.
     */
    public RegulatoryLocationTypeEnum locationCapability; // 3 RegulatoryLocationTypeEnum R V
    /**
     * This attribute shall indicate whether this device supports &quot;concurrent connection flow&quot; commissioning
     * mode (see Section 5.5, “Commissioning Flows”). If false, the device only supports &quot;non-concurrent connection
     * flow&quot; mode.
     */
    public Boolean supportsConcurrentConnection; // 4 bool R V
    /**
     * Indicates the last version of the T&amp;Cs for which the device received user acknowledgements. On factory reset
     * this field shall be reset to 0.
     * When Custom Commissioning Flow is used to obtain user consent (e. g. because the Commissioner does not support
     * the TC feature), the manufacturer-provided means for obtaining user consent shall ensure that this attribute is
     * set to a value which is greater than or equal to TCMinRequiredVersion before returning the user back to the
     * originating Commissioner (see Enhanced Setup Flow).
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
     * accept for CommissioningComplete to succeed.
     * This attribute may appear, or become True after commissioning (e.g. due to a firmware update) to indicate that
     * new Terms &amp; Conditions are available that the user must accept.
     * Upon Factory Data Reset, this attribute shall be set to a value of True.
     * When Custom Commissioning Flow is used to obtain user consent (e.g. because the Commissioner does not support the
     * TC feature), the manufacturer-provided means for obtaining user consent shall ensure that this attribute is set
     * to False before returning the user back to the original Commissioner (see Enhanced Setup Flow).
     */
    public Boolean tcAcknowledgementsRequired; // 8 bool R A
    /**
     * Indicates the System Time in seconds when any functionality limitations will begin due to a lack of acceptance of
     * updated Terms and Conditions, as described in Section 5.7.4.5, “Presenting Updated Terms and Conditions”.
     * A null value indicates that there is no pending deadline for updated TC acceptance.
     */
    public Integer tcUpdateDeadline; // 9 uint32 R A

    // Structs
    /**
     * This structure provides some constant values that may be of use to all commissioners.
     */
    public static class BasicCommissioningInfo {
        /**
         * This field shall contain a conservative initial duration (in seconds) to set in the FailSafe for the
         * commissioning flow to complete successfully. This may vary depending on the speed or sleepiness of the
         * Commissionee. This value, if used in the ArmFailSafe command’s ExpiryLengthSeconds field SHOULD allow a
         * Commissioner to proceed with a nominal commissioning without having to-rearm the fail-safe, with some margin.
         */
        public Integer failSafeExpiryLengthSeconds; // uint16
        /**
         * This field shall contain a conservative value in seconds denoting the maximum total duration for which a fail
         * safe timer can be re-armed. See Section 11.10.7.2.1, “Fail Safe Context”.
         * The value of this field shall be greater than or equal to the FailSafeExpiryLengthSeconds. Absent additional
         * guidelines, it is recommended that the value of this field be aligned with the initial Announcement Duration
         * and default to 900 seconds.
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

        public final Integer value;
        public final String label;

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

        public final Integer value;
        public final String label;

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
         * Supports Terms &amp; Conditions acknowledgement
         */
        public boolean termsAndConditions;

        public FeatureMap(boolean termsAndConditions) {
            this.termsAndConditions = termsAndConditions;
        }
    }

    /**
     * This field shall contain the user responses to the Enhanced Setup Flow Terms &amp; Conditions as a map where each
     * bit set in the bitmap corresponds to an accepted term in the file located at EnhancedSetupFlowTCUrl.
     * ### Effect on Receipt
     * This command shall copy the user responses and accepted version to the presented Enhanced Setup Flow Terms &amp;
     * Conditions from the values provided in the TCUserResponse and TCVersion fields to the TCAcknowledgements
     * Attribute and the TCAcceptedVersion Attribute fields respectively.
     * This command shall result in success with an ErrorCode value of OK in the SetTCAcknowledgementsResponse if all
     * required terms were accepted by the user. Specifically, all bits have a value of 1 in TCAcknowledgements whose
     * ordinal is marked as required in the file located at EnhancedSetupFlowTCUrl.
     * If the TCVersion field is less than the TCMinRequiredVersion, then the ErrorCode of TCMinVersionNotMet shall be
     * returned and TCAcknowledgements shall remain unchanged.
     * If TCVersion is greater than or equal to TCMinRequiredVersion, but the TCUserResponse value indicates that not
     * all required terms were accepted by the user, then the ErrorCode of RequiredTCNotAccepted shall be returned and
     * TCAcknowledgements shall remain unchanged.
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
     * Success or failure of this command shall be communicated by the ArmFailSafeResponse command, unless some data
     * model validations caused a failure status code to be issued during the processing of the command.
     * If the fail-safe timer is not currently armed, the commissioning window is open, and the command was received
     * over a CASE session, the command shall leave the current fail-safe state unchanged and immediately respond with
     * an ArmFailSafeResponse containing an ErrorCode value of BusyWithOtherAdmin. This is done to allow commissioners,
     * which use PASE connections, the opportunity to use the failsafe during the relatively short commissioning window.
     * Otherwise, the command shall arm or re-arm the &quot;fail-safe timer&quot; with an expiry time set for a duration
     * of ExpiryLengthSeconds, or disarm it, depending on the situation:
     * • If ExpiryLengthSeconds is 0 and the fail-safe timer was already armed and the accessing fabric matches the
     * Fabric currently associated with the fail-safe context, then the fail-safe timer shall be immediately expired
     * (see further below for side-effects of expiration).
     * • If ExpiryLengthSeconds is 0 and the fail-safe timer was not armed, then this command invocation shall lead to a
     * success response with no side-effects against the fail-safe context.
     * • If ExpiryLengthSeconds is non-zero and the fail-safe timer was not currently armed, then the fail-safe timer
     * shall be armed for that duration.
     * • If ExpiryLengthSeconds is non-zero and the fail-safe timer was currently armed, and the accessing Fabric
     * matches the fail-safe context’s associated Fabric, then the fail-safe timer shall be re-armed to expire in
     * ExpiryLengthSeconds.
     * • Otherwise, the command shall leave the current fail-safe state unchanged and immediately respond with
     * ArmFailSafeResponse containing an ErrorCode value of BusyWithOtherAdmin, indicating a likely conflict between
     * commissioners.
     * The value of the Breadcrumb field shall be written to the Breadcrumb on successful execution of the command.
     * If the receiver restarts unexpectedly (e.g., power interruption, software crash, or other reset) the receiver
     * shall behave as if the fail-safe timer expired and perform the sequence of clean-up steps listed below.
     * On successful execution of the command, the ErrorCode field of the ArmFailSafeResponse shall be set to OK.
     * ### Fail Safe Context
     * When first arming the fail-safe timer, a &#x27;Fail Safe Context&#x27; shall be created on the receiver, to track
     * the following state information while the fail-safe is armed:
     * • The fail-safe timer duration.
     * • The state of all Network Commissioning Networks attribute configurations, to allow recovery of connectivity
     * after Fail-Safe expiry.
     * • Whether an AddNOC command or UpdateNOC command has taken place.
     * • A Fabric Index for the fabric-scoping of the context, starting at the accessing fabric index for the
     * ArmFailSafe command, and updated with the Fabric Index associated with an AddNOC command or an UpdateNOC command
     * being invoked successfully during the ongoing Fail-Safe timer period.
     * • The operational credentials associated with any Fabric whose configuration is affected by the UpdateNOC
     * command.
     * • Optionally: the previous state of non-fabric-scoped data that is mutated during the fail-safe period.
     * Note the following to assist in understanding the above state-keeping, which summarizes other normative
     * requirements in the respective sections:
     * • The AddNOC command can only be invoked once per contiguous non-expiring fail-safe timer period, and only if no
     * UpdateNOC command was previously processed within the same fail-safe timer period.
     * • The UpdateNOC command can only be invoked once per contiguous non-expiring fail-safe timer period, can only be
     * invoked over a CASE session, and only if no AddNOC command was previously processed in the same fail-safe timer
     * period.
     * On creation of the Fail Safe Context a second timer shall be created to expire at MaxCumulativeFailsafeSeconds as
     * specified in BasicCommissioningInfo. This Cumulative Fail Safe Context timer (CFSC timer) serves to limit the
     * lifetime of any particular Fail Safe Context; it shall NOT be extended or modified on subsequent invocations of
     * ArmFailSafe associated with this Fail Safe Context. Upon expiry of the CFSC timer, the receiver shall execute
     * cleanup behavior equivalent to that of fail-safe timer expiration as detailed in Section 11.10.7.2.2, “Behavior
     * on expiry of Fail-Safe timer”. Termination of the session prior to the expiration of that timer for any reason
     * (including a successful end of commissioning or an expiry of a fail-safe timer) shall also delete the CFSC timer.
     * ### Behavior on expiry of Fail-Safe timer
     * If the fail-safe timer expires before the CommissioningComplete command is successfully invoked, the following
     * sequence of clean-up steps shall be executed, in order, by the receiver:
     * 1. Terminate any open PASE secure session by clearing any associated Secure Session Context at the Server.
     * 2. Revoke the temporary administrative privileges granted to any open PASE session (see Section 6.6.2.9,
     * “Bootstrapping of the Access Control Cluster”) at the Server.
     * 3. If an AddNOC or UpdateNOC command has been successfully invoked, terminate all CASE sessions associated with
     * the Fabric whose Fabric Index is recorded in the Fail-Safe context (see ArmFailSafe) by clearing any associated
     * Secure Session Context at the Server.
     * 4. Reset the configuration of all Network Commissioning Networks attribute to their state prior to the Fail-Safe
     * being armed.
     * 5. If an UpdateNOC command had been successfully invoked, revert the state of operational key pair, NOC and ICAC
     * for that Fabric to the state prior to the Fail-Safe timer being armed, for the Fabric Index that was the subject
     * of the UpdateNOC command.
     * 6. If an AddNOC command had been successfully invoked, achieve the equivalent effect of invoking the RemoveFabric
     * command against the Fabric Index stored in the Fail-Safe Context for the Fabric Index that was the subject of the
     * AddNOC command. This shall remove all associations to that Fabric including all fabric-scoped data, and may
     * possibly factory-reset the device depending on current device state. This shall only apply to Fabrics added
     * during the fail-safe period as the result of the AddNOC command.
     * 7. If the CSRRequest command had been successfully invoked, but no AddNOC or UpdateNOC command had been
     * successfully invoked, then the new operational key pair temporarily generated for the purposes of NOC addition or
     * update (see Node Operational CSR Procedure) shall be removed as it is no longer needed.
     * 8. Remove any RCACs added by the AddTrustedRootCertificate command that are not currently referenced by any entry
     * in the Fabrics attribute.
     * 9. Reset the Breadcrumb attribute to zero.
     * 10. Optionally: if no factory-reset resulted from the previous steps, it is recommended that the Node rollback
     * the state of all non fabric-scoped data present in the Fail-Safe context.
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
     * This command has no data.
     * Success or failure of this command shall be communicated by the CommissioningCompleteResponse command, unless
     * some data model validations caused a failure status code to be issued during the processing of the command.
     * This command signals the Server that the Commissioner or Administrator has successfully completed all steps
     * needed during the Fail-Safe period, such as commissioning (see Section 5.5, “Commissioning Flows”) or other
     * Administrator operations requiring usage of the Fail Safe timer. It ensures that the Server is configured in a
     * state such that it still has all necessary elements to be fully operable within a Fabric, such as ACL entries
     * (see Section 9.10, “Access Control Cluster”) and operational credentials (see Section 6.4, “Node Operational
     * Credentials Specification”), and that the Node is reachable using CASE (see Section 4.14.2, “Certificate
     * Authenticated Session Establishment (CASE)”) over an operational network.
     * An ErrorCode of NoFailSafe shall be responded to the invoker if the CommissioningComplete command was received
     * when no Fail-Safe context exists.
     * If Terms and Conditions are required, then an ErrorCode of TCAcknowledgementsNotReceived shall be responded to
     * the invoker if the user acknowledgements to the required Terms and Conditions have not been provided. If the
     * TCAcceptedVersion for the provided acknowledgements is less than TCMinRequiredVersion, then an ErrorCode of
     * TCMinVersionNotMet shall be responded to the invoker.
     * This command is fabric-scoped, so cannot be issued over a session that does not have an associated fabric, i.e.
     * over PASE session prior to an AddNOC command. In addition, this command is only permitted over CASE and must be
     * issued by a node associated with the ongoing Fail-Safe context. An ErrorCode of InvalidAuthentication shall be
     * responded to the invoker if the CommissioningComplete command was received outside a CASE session (e.g., over
     * Group messaging, or PASE session after AddNOC), or if the accessing fabric is not the one associated with the
     * ongoing Fail-Safe context.
     * This command shall only result in success with an ErrorCode value of OK in the CommissioningCompleteResponse if
     * received over a CASE session and the accessing fabric index matches the Fabric Index associated with the current
     * Fail-Safe context. In other words:
     * • If no AddNOC command had been successfully invoked, the CommissioningComplete command must originate from the
     * Fabric that initiated the Fail-Safe context.
     * • After an AddNOC command has been successfully invoked, the CommissioningComplete command must originate from
     * the Fabric which was joined through the execution of that command, which updated the Fail-Safe context’s Fabric
     * Index.
     * On successful execution of the CommissioningComplete command, where the CommissioningCompleteResponse has an
     * ErrorCode of OK, the following actions shall be undertaken on the Server:
     * 1. The Fail-Safe timer associated with the current Fail-Safe context shall be disarmed.
     * 2. The commissioning window at the Server shall be closed.
     * 3. Any temporary administrative privileges automatically granted to any open PASE session shall be revoked (see
     * Section 6.6.2.9, “Bootstrapping of the Access Control Cluster”).
     * 4. The Secure Session Context of any PASE session still established at the Server shall be cleared.
     * 5. The Breadcrumb attribute shall be reset to zero.
     * After receipt of a CommissioningCompleteResponse with an ErrorCode value of OK, a client cannot expect any
     * previously established PASE session to still be usable, due to the server having cleared such sessions.
     */
    public static ClusterCommand commissioningComplete() {
        return new ClusterCommand("commissioningComplete");
    }

    /**
     * This command sets the user acknowledgements received in the Enhanced Setup Flow Terms &amp; Conditions into the
     * node.
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
        str += "clusterRevision : " + clusterRevision + "\n";
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
        return str;
    }
}
