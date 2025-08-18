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
 * OtaSoftwareUpdateRequestor
 *
 * @author Dan Cunningham - Initial contribution
 */
public class OtaSoftwareUpdateRequestorCluster extends BaseCluster {

    public static final int CLUSTER_ID = 0x002A;
    public static final String CLUSTER_NAME = "OtaSoftwareUpdateRequestor";
    public static final String CLUSTER_PREFIX = "otaSoftwareUpdateRequestor";
    public static final String ATTRIBUTE_CLUSTER_REVISION = "clusterRevision";
    public static final String ATTRIBUTE_DEFAULT_OTA_PROVIDERS = "defaultOtaProviders";
    public static final String ATTRIBUTE_UPDATE_POSSIBLE = "updatePossible";
    public static final String ATTRIBUTE_UPDATE_STATE = "updateState";
    public static final String ATTRIBUTE_UPDATE_STATE_PROGRESS = "updateStateProgress";

    public Integer clusterRevision; // 65533 ClusterRevision
    /**
     * This field is a list of ProviderLocation whose entries shall be set by Administrators, either during
     * Commissioning or at a later time, to set the ProviderLocation for the default OTA Provider Node to use for
     * software updates on a given Fabric.
     * There shall NOT be more than one entry per Fabric. On a list update that would introduce more than one entry per
     * fabric, the write shall fail with CONSTRAINT_ERROR status code.
     * Provider Locations obtained using the AnnounceOTAProvider command shall NOT overwrite values set in the
     * DefaultOTAProviders attribute.
     */
    public List<ProviderLocation> defaultOtaProviders; // 0 list RW F VA
    /**
     * This field shall be set to True if the OTA Requestor is currently able to be updated. Otherwise, it shall be set
     * to False in case of any condition preventing update being possible, such as insufficient capacity of an internal
     * battery. This field is merely informational for diagnostics purposes and shall NOT affect the responses provided
     * by an OTA Provider to an OTA Requestor.
     */
    public Boolean updatePossible; // 1 bool R V
    /**
     * This field shall reflect the current state of the OTA Requestor with regards to obtaining software updates. See
     * Section 11.20.7.4.2, “UpdateStateEnum Type” for possible values.
     * This field SHOULD be updated in a timely manner whenever OTA Requestor internal state updates.
     */
    public UpdateStateEnum updateState; // 2 UpdateStateEnum R V
    /**
     * This field shall reflect the percentage value of progress, relative to the current UpdateState, if applicable to
     * the state.
     * The value of this field shall be null if a progress indication does not apply to the current state.
     * A value of 0 shall indicate that the beginning has occurred. A value of 100 shall indicate completion.
     * This field may be updated infrequently. Some care SHOULD be taken by Nodes to avoid over-reporting progress when
     * this attribute is part of a subscription.
     */
    public Integer updateStateProgress; // 3 uint8 R V

    // Structs
    /**
     * This event shall be generated when a change of the UpdateState attribute occurs due to an OTA Requestor moving
     * through the states necessary to query for updates.
     */
    public static class StateTransition {
        /**
         * This field shall be set to the state that preceded the transition causing this event to be generated, if such
         * a state existed. If no previous state exists, the value shall be Unknown.
         */
        public UpdateStateEnum previousState; // UpdateStateEnum
        /**
         * This field shall be set to the state now in effect through the transition causing this event to be generated.
         */
        public UpdateStateEnum newState; // UpdateStateEnum
        /**
         * This field shall be set to the reason why this event was generated.
         */
        public ChangeReasonEnum reason; // ChangeReasonEnum
        /**
         * This field shall be set to the target SoftwareVersion which is the subject of the operation, whenever the
         * NewState is Downloading, Applying or RollingBack. Otherwise TargetSoftwareVersion shall be null.
         */
        public Integer targetSoftwareVersion; // uint32

        public StateTransition(UpdateStateEnum previousState, UpdateStateEnum newState, ChangeReasonEnum reason,
                Integer targetSoftwareVersion) {
            this.previousState = previousState;
            this.newState = newState;
            this.reason = reason;
            this.targetSoftwareVersion = targetSoftwareVersion;
        }
    }

    /**
     * This event shall be generated whenever a new version starts executing after being applied due to a software
     * update. This event SHOULD be generated even if a software update was done using means outside of this cluster.
     */
    public static class VersionApplied {
        /**
         * This field shall be set to the same value as the one available in the Software Version attribute of the Basic
         * Information Cluster for the newly executing version.
         */
        public Integer softwareVersion; // uint32
        /**
         * This field shall be set to the ProductID applying to the executing version, as reflected by the Basic
         * Information Cluster. This can be used to detect a product updating its definition due to a large-scale
         * functional update that may impact aspects of the product reflected in the DeviceModel schema of the
         * Distributed Compliance Ledger.
         */
        public Integer productId; // uint16

        public VersionApplied(Integer softwareVersion, Integer productId) {
            this.softwareVersion = softwareVersion;
            this.productId = productId;
        }
    }

    /**
     * This event shall be generated whenever an error occurs during OTA Requestor download operation.
     */
    public static class DownloadError {
        /**
         * This field shall be set to the value of the SoftwareVersion being downloaded, matching the SoftwareVersion
         * field of the QueryImageResponse that caused the failing download to take place.
         */
        public Integer softwareVersion; // uint32
        /**
         * This field shall be set to the number of bytes that have been downloaded during the failing transfer that
         * caused this event to be generated.
         */
        public BigInteger bytesDownloaded; // uint64
        /**
         * This field shall be set to the nearest integer percent value reflecting how far within the transfer the
         * failure occurred during the failing transfer that caused this event to be generated, unless the total length
         * of the transfer is unknown, in which case it shall be null.
         */
        public Integer progressPercent; // uint8
        /**
         * This field SHOULD be set to some internal product-specific error code, closest in temporal/functional
         * proximity to the failure that caused this event to be generated. Otherwise, it shall be null. This event
         * field may be used for debugging purposes and no uniform definition exists related to its meaning.
         */
        public BigInteger platformCode; // int64

        public DownloadError(Integer softwareVersion, BigInteger bytesDownloaded, Integer progressPercent,
                BigInteger platformCode) {
            this.softwareVersion = softwareVersion;
            this.bytesDownloaded = bytesDownloaded;
            this.progressPercent = progressPercent;
            this.platformCode = platformCode;
        }
    }

    /**
     * This structure encodes a fabric-scoped location of an OTA provider on a given fabric.
     */
    public static class ProviderLocation {
        /**
         * This field shall contain the Node ID of the OTA Provider to contact within the Fabric identified by the
         * FabricIndex.
         */
        public BigInteger providerNodeId; // node-id
        /**
         * This field shall contain the endpoint number which has the OTA Provider device type and OTA Software Update
         * Provider cluster server on the ProviderNodeID. This is provided to avoid having to do discovery of the
         * location of that endpoint by walking over all endpoints and checking their Descriptor Cluster.
         */
        public Integer endpoint; // endpoint-no
        public Integer fabricIndex; // FabricIndex

        public ProviderLocation(BigInteger providerNodeId, Integer endpoint, Integer fabricIndex) {
            this.providerNodeId = providerNodeId;
            this.endpoint = endpoint;
            this.fabricIndex = fabricIndex;
        }
    }

    // Enums
    public enum AnnouncementReasonEnum implements MatterEnum {
        SIMPLE_ANNOUNCEMENT(0, "Simple Announcement"),
        UPDATE_AVAILABLE(1, "Update Available"),
        URGENT_UPDATE_AVAILABLE(2, "Urgent Update Available");

        public final Integer value;
        public final String label;

        private AnnouncementReasonEnum(Integer value, String label) {
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

    public enum UpdateStateEnum implements MatterEnum {
        UNKNOWN(0, "Unknown"),
        IDLE(1, "Idle"),
        QUERYING(2, "Querying"),
        DELAYED_ON_QUERY(3, "Delayed On Query"),
        DOWNLOADING(4, "Downloading"),
        APPLYING(5, "Applying"),
        DELAYED_ON_APPLY(6, "Delayed On Apply"),
        ROLLING_BACK(7, "Rolling Back"),
        DELAYED_ON_USER_CONSENT(8, "Delayed On User Consent");

        public final Integer value;
        public final String label;

        private UpdateStateEnum(Integer value, String label) {
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

    public enum ChangeReasonEnum implements MatterEnum {
        UNKNOWN(0, "Unknown"),
        SUCCESS(1, "Success"),
        FAILURE(2, "Failure"),
        TIME_OUT(3, "Time Out"),
        DELAY_BY_PROVIDER(4, "Delay By Provider");

        public final Integer value;
        public final String label;

        private ChangeReasonEnum(Integer value, String label) {
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

    public OtaSoftwareUpdateRequestorCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 42, "OtaSoftwareUpdateRequestor");
    }

    protected OtaSoftwareUpdateRequestorCluster(BigInteger nodeId, int endpointId, int clusterId, String clusterName) {
        super(nodeId, endpointId, clusterId, clusterName);
    }

    // commands
    /**
     * This command may be invoked by Administrators to announce the presence of a particular OTA Provider.
     * This command shall be scoped to the accessing fabric.
     * If the accessing fabric index is 0, this command shall fail with an UNSUPPORTED_ACCESS status code.
     */
    public static ClusterCommand announceOtaProvider(BigInteger providerNodeId, Integer vendorId,
            AnnouncementReasonEnum announcementReason, OctetString metadataForNode, Integer endpoint,
            Integer fabricIndex) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (providerNodeId != null) {
            map.put("providerNodeId", providerNodeId);
        }
        if (vendorId != null) {
            map.put("vendorId", vendorId);
        }
        if (announcementReason != null) {
            map.put("announcementReason", announcementReason);
        }
        if (metadataForNode != null) {
            map.put("metadataForNode", metadataForNode);
        }
        if (endpoint != null) {
            map.put("endpoint", endpoint);
        }
        if (fabricIndex != null) {
            map.put("fabricIndex", fabricIndex);
        }
        return new ClusterCommand("announceOtaProvider", map);
    }

    @Override
    public @NonNull String toString() {
        String str = "";
        str += "clusterRevision : " + clusterRevision + "\n";
        str += "defaultOtaProviders : " + defaultOtaProviders + "\n";
        str += "updatePossible : " + updatePossible + "\n";
        str += "updateState : " + updateState + "\n";
        str += "updateStateProgress : " + updateStateProgress + "\n";
        return str;
    }
}
