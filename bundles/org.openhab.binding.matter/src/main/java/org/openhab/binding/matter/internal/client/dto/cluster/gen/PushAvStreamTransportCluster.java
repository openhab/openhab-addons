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
 * PushAvStreamTransport
 *
 * @author Dan Cunningham - Initial contribution
 */
public class PushAvStreamTransportCluster extends BaseCluster {

    public static final int CLUSTER_ID = 0x0555;
    public static final String CLUSTER_NAME = "PushAvStreamTransport";
    public static final String CLUSTER_PREFIX = "pushAvStreamTransport";
    public static final String ATTRIBUTE_FEATURE_MAP = "featureMap";
    public static final String ATTRIBUTE_SUPPORTED_FORMATS = "supportedFormats";
    public static final String ATTRIBUTE_CURRENT_CONNECTIONS = "currentConnections";

    public FeatureMap featureMap; // 65532 FeatureMap
    /**
     * This attribute shall contain a list of Supported Container Format structs, which represents the combinations of
     * Ingestion Method and Container Format that the Node supports. Nodes shall support at least the combination
     * CMAFIngest,CMAF.
     */
    public List<SupportedFormatStruct> supportedFormats; // 0 list R V
    /**
     * This attribute shall be a list of TransportConfigurationStruct which represents all the allocated connections
     * added via AllocatePushTransport. When this attribute is read over a non Large Message (See Large Message Quality
     * in the Data Model section of [[MatterCore]](#ref_MatterCore)) capable transport, the TransportOptions field shall
     * NOT be included. To get the full details of the connections use the FindTransport command. The maximum size of
     * this list is run-time dependent upon the resource constraints of the system as described in Resource Management
     * and Stream Priorities and the currently used bandwidth of the total available specified by MaxNetworkBandwidth.
     */
    public List<TransportConfigurationStruct> currentConnections; // 1 list R S V

    // Structs
    /**
     * This event shall indicate a push transport transmission has begun.
     * With Motion and Continuous based triggers, this event shall be generated when each new recording starts
     * uploading.
     * With Command based triggers, this event shall be generated when uploading starts via the ManuallyTriggerTransport
     * command.
     * The data on this event shall contain the following information.
     */
    public static class PushTransportBegin {
        /**
         * This field shall be a PushTransportConnectionID representing the push transport which started transmitting.
         */
        public Integer connectionId; // PushTransportConnectionID
        /**
         * This field shall represent the type of trigger which caused this event to be generated.
         */
        public TransportTriggerTypeEnum triggerType; // TransportTriggerTypeEnum
        /**
         * This field shall only be present when the TriggerType is Command and provides the reason for the event.
         */
        public TriggerActivationReasonEnum activationReason; // TriggerActivationReasonEnum
        /**
         * This field shall indicate the container type chosen for this transport.
         */
        public ContainerFormatEnum containerType; // ContainerFormatEnum
        /**
         * This field shall represent the CMAF Session number of the recording session that was triggered.
         */
        public BigInteger cmafSessionNumber; // uint64

        public PushTransportBegin(Integer connectionId, TransportTriggerTypeEnum triggerType,
                TriggerActivationReasonEnum activationReason, ContainerFormatEnum containerType,
                BigInteger cmafSessionNumber) {
            this.connectionId = connectionId;
            this.triggerType = triggerType;
            this.activationReason = activationReason;
            this.containerType = containerType;
            this.cmafSessionNumber = cmafSessionNumber;
        }
    }

    /**
     * This event shall indicate a push transport upload of the indicated recording has completed.
     * With Motion, Continuous, and Command based triggers, this event shall be generated when each new recording
     * finishes uploading.
     * The data on this event shall contain the following information.
     */
    public static class PushTransportEnd {
        /**
         * This field shall be a PushTransportConnectionID representing the push transport which stopped transmitting.
         */
        public Integer connectionId; // PushTransportConnectionID
        /**
         * This field shall indicate the container type chosen for this transport.
         */
        public ContainerFormatEnum containerType; // ContainerFormatEnum
        /**
         * This field shall represent the CMAF Session number of the recording session that ended.
         */
        public BigInteger cmafSessionNumber; // uint64

        public PushTransportEnd(Integer connectionId, ContainerFormatEnum containerType, BigInteger cmafSessionNumber) {
            this.connectionId = connectionId;
            this.containerType = containerType;
            this.cmafSessionNumber = cmafSessionNumber;
        }
    }

    /**
     * This struct holds the combination of container format and ingest method which represents a valid combination for
     * a transport.
     */
    public static class SupportedFormatStruct {
        /**
         * This field shall indicate a supported container format that when combined with IngestMethod, can be used in a
         * transport.
         */
        public ContainerFormatEnum containerFormat; // ContainerFormatEnum
        /**
         * This field shall indicate a supported ingest method that when combined with ContainerFormat, can be used in a
         * transport.
         */
        public IngestMethodsEnum ingestMethod; // IngestMethodsEnum

        public SupportedFormatStruct(ContainerFormatEnum containerFormat, IngestMethodsEnum ingestMethod) {
            this.containerFormat = containerFormat;
            this.ingestMethod = ingestMethod;
        }
    }

    /**
     * This struct holds a video stream id and the symbolic stream name associated with it. For CMAF based transports,
     * this becomes a CMAF Track.
     */
    public static class VideoStreamStruct {
        /**
         * This field shall identify the unique name assigned to this stream. In CMAF, this value becomes the CMAF track
         * name.
         */
        public String videoStreamName; // string
        /**
         * This field shall indicate the video stream identified by the VideoStreamID entry in the AllocatedVideoStreams
         * list to use.
         */
        public Integer videoStreamId; // CameraAvStreamManagement.VideoStreamID

        public VideoStreamStruct(String videoStreamName, Integer videoStreamId) {
            this.videoStreamName = videoStreamName;
            this.videoStreamId = videoStreamId;
        }
    }

    /**
     * This struct holds a video stream id and the symbolic stream name associated with it. For CMAF based transports,
     * this becomes a CMAF Track.
     */
    public static class AudioStreamStruct {
        /**
         * This field shall identify the unique name assigned to this stream. In CMAF, this value becomes the CMAF track
         * name.
         */
        public String audioStreamName; // string
        /**
         * This field shall indicate the audio stream identified by the AudioStreamID entry in the AllocatedAudioStreams
         * list to use.
         */
        public Integer audioStreamId; // CameraAvStreamManagement.AudioStreamID

        public AudioStreamStruct(String audioStreamName, Integer audioStreamId) {
            this.audioStreamName = audioStreamName;
            this.audioStreamId = audioStreamId;
        }
    }

    /**
     * This struct encodes options for configuration of the CMAF container format.
     */
    public static class CMAFContainerOptionsStruct {
        /**
         * This field shall indicate the selected Interface of the CMAF container. The Interface chosen determines the
         * number and type of operations that occur within each CMAF session. See CMAFInterfaceEnum for details on CMAF
         * Interfaces.
         */
        public CMAFInterfaceEnum cmafInterface; // CMAFInterfaceEnum
        /**
         * This field shall indicate the segment duration (in milliseconds) of the CMAF container. This value shall be a
         * multiple of the KeyFrameInterval for the associated video stream. It is recommended to use a value of 4000 (4
         * seconds).
         */
        public Integer segmentDuration; // uint16
        /**
         * This field shall indicate the chunk duration (in milliseconds) of the CMAF container. A value of 0 shall
         * indicate that chunks are not used.
         * When chunking is used, an even divisor of SegmentDuration SHOULD be used that aligns with the video frame
         * rate and audio frame duration. recommended values are 1/2, 1/4, or 1/8 of the SegmentDuration depending on
         * the end to end latency requirements needed. Each chunk results in an additional 144 bytes of overhead in the
         * resulting file.
         */
        public Integer chunkDuration; // uint16
        /**
         * This field is deprecated and has been replaced with the VideoStreams and AudioStreams fields.
         */
        public Integer sessionGroup; // uint8
        /**
         * This field is deprecated and has been replaced with the VideoStreamName or AudioStreamName fields of the
         * associated stream entry in the VideoStreams or AudioStreams lists
         */
        public String trackName; // string
        /**
         * This field, if present, shall indicate the CENC key to be used to encrypt the CMAF data. When absent, the
         * CMAF data shall be sent without CENC encryption added. See CMAF Background for further details on CMAF CENC
         * encryption.
         */
        public OctetString cencKey; // octstr
        /**
         * This field, if present, shall indicate the opaque CENC Key ID (KID) that represents the key in the
         * Controllers ecosystem. This fields maps to the KID value as specified in ISO 23001-7:2023 or later. See CMAF
         * Background for further details on CMAF CENC encryption.
         */
        public OctetString cencKeyId; // octstr
        /**
         * This field, if present and true, indicates that AVMetadataStruct based Metadata tracks and boxes may be
         * included in the CMAF segments. If this field is not present or is false, metadata tracks and boxes shall NOT
         * be included.
         * Metadata within CMAF segments when used, shall be encoded as follows:
         * - Use urim as the codec-type.
         * - Use meta as the content-type.
         * - Use urn:csa:matter:av-metadata as the uri.
         * Placement of the metadata shall be as follows:
         * - Use a single trac for time-synced data points as described in CMAF-Ingest Interface-1 Section 6.6
         * Requirements for Timed Metadata Tracks.
         * - Use a single Box for non-time synced data points.
         */
        public Boolean metadataEnabled; // bool

        public CMAFContainerOptionsStruct(CMAFInterfaceEnum cmafInterface, Integer segmentDuration,
                Integer chunkDuration, Integer sessionGroup, String trackName, OctetString cencKey,
                OctetString cencKeyId, Boolean metadataEnabled) {
            this.cmafInterface = cmafInterface;
            this.segmentDuration = segmentDuration;
            this.chunkDuration = chunkDuration;
            this.sessionGroup = sessionGroup;
            this.trackName = trackName;
            this.cencKey = cencKey;
            this.cencKeyId = cencKeyId;
            this.metadataEnabled = metadataEnabled;
        }
    }

    /**
     * This struct encodes the specific container type options struct
     */
    public static class ContainerOptionsStruct {
        /**
         * This field shall indicate the container type chosen for this transport.
         */
        public ContainerFormatEnum containerType; // ContainerFormatEnum
        /**
         * This field shall contain a CMAF Container Options if the ContainerType is set to CMAF, otherwise this field
         * shall be omitted.
         */
        public CMAFContainerOptionsStruct cmafContainerOptions; // CMAFContainerOptionsStruct

        public ContainerOptionsStruct(ContainerFormatEnum containerType,
                CMAFContainerOptionsStruct cmafContainerOptions) {
            this.containerType = containerType;
            this.cmafContainerOptions = cmafContainerOptions;
        }
    }

    /**
     * This struct encodes the options that configure the per Zone portion of a Trigger configuration.
     */
    public static class TransportZoneOptionsStruct {
        /**
         * This field shall be a Motion ZoneID found in the Zone Management Cluster which shall cause the trigger to
         * activate. If not null, motion in this zone will activate the trigger. If null, motion anywhere in the
         * complement of the union of all the Zones defined in the Zone Management Cluster on this endpoint will
         * activate the trigger.
         */
        public Integer zone; // ZoneManagement.ZoneID
        /**
         * This field shall indicate how sensitive the trigger for the specified Zone is, and shall match the same
         * implementation specifics as the Sensitivity attribute in the Zone Management Cluster. This field shall only
         * be included when PerZoneSensitivity is supported, otherwise the value from MotionSensitivity is used.
         */
        public Integer sensitivity; // uint8

        public TransportZoneOptionsStruct(Integer zone, Integer sensitivity) {
            this.zone = zone;
            this.sensitivity = sensitivity;
        }
    }

    /**
     * This struct encodes the conditions and options that configures the trigger for the push transport. The transport
     * shall only start transmitting AV Streams when it's associated trigger is activated.
     */
    public static class TransportTriggerOptionsStruct {
        /**
         * This field shall indicate the type of the transport trigger.
         */
        public TransportTriggerTypeEnum triggerType; // TransportTriggerTypeEnum
        /**
         * This field shall be a list of TransportZoneOptionsStruct containing the Motion Zones to trigger on. If this
         * list is null, empty, or the Zone Management Cluster is not supported on this endpoint, then motion anywhere
         * shall cause the trigger to activate. The maximum size of this list is MaxZones.
         */
        public List<TransportZoneOptionsStruct> motionZones; // list
        /**
         * This field shall indicate how sensitive the trigger is to motion and shall match the same implementation
         * specifics as Sensitivity. This field shall NOT be used if the PerZoneSensitivity feature is supported, as a
         * Zone specific value is available in the TransportZoneOptionsStruct Sensitivity field. If this is null and the
         * Zone Management Cluster is supported on this endpoint, then the value found in the Sensitivity attribute in
         * the Zone Management Cluster shall be used. If this is null and the Zone Management Cluster is not supported
         * on this endpoint, a value of 10 shall be used.
         */
        public Integer motionSensitivity; // uint8
        /**
         * This field shall control timing around repeated activation of the trigger (see
         * TransportMotionTriggerTimeControlStruct). If TriggerType is Motion Value, this field shall be required.
         */
        public TransportMotionTriggerTimeControlStruct motionTimeControl; // TransportMotionTriggerTimeControlStruct
        /**
         * This field shall indicate the maximum duration in milliseconds of pre-roll content that can be included, if
         * the TriggerType is Motion Value or Command Value, when the trigger activates.
         * A value of 0 shall indicate that no extra segments beyond the one containing the trigger point will be sent.
         * When using a non 0 value, the value shall be greater than or equal to the value of the stream's
         * KeyFrameInterval and it SHOULD be a multiple of that value if larger.
         * The actual amount transmitted will always be less than or equal to the per stream storage amount found in the
         * MaxContentBufferSize.
         * Since a transmission caused by a trigger activation always begins on the Container Format's segment (or
         * key-frame) boundary, if the trigger occurs mid segment, the entire segment still needs to be sent. This time
         * delta between the actual trigger point and the start of the segment is counted as part of the pre-roll
         * length. Thus, for more than the current segment to be sent as pre-roll, the full size of a segment must fit
         * within the remainder of this length. For this reason, it is recommended that a value of at least two times
         * SegmentDuration be used so that a full segment is always included if available.
         */
        public Integer maxPreRollLen; // uint16

        public TransportTriggerOptionsStruct(TransportTriggerTypeEnum triggerType,
                List<TransportZoneOptionsStruct> motionZones, Integer motionSensitivity,
                TransportMotionTriggerTimeControlStruct motionTimeControl, Integer maxPreRollLen) {
            this.triggerType = triggerType;
            this.motionZones = motionZones;
            this.motionSensitivity = motionSensitivity;
            this.motionTimeControl = motionTimeControl;
            this.maxPreRollLen = maxPreRollLen;
        }
    }

    /**
     * This struct is used to encode a set of values for controlling the lifecycle of a motion triggered transport.
     * When a Motion Trigger is activated, either by receiving a ManuallyTriggerTransport command, or when motion is
     * initially detected which matches a configured motion trigger, the Node shall start the push transport configured
     * with this trigger see (TransportOptionsStruct).
     * This places the Node in a Motion Detected state, at which point the Node shall internally track two values.
     * ### TimeSinceActivation
     * : The time in seconds since the trigger was activated.
     * ### MotionDetectedDuration
     * : Initially set to the InitialDuration value.
     * The transport shall remain active minimally for InitialDuration period before a PushTransportEnd event can occur.
     * However, if additional motion is detected during this MotionDetectedDuration period, the Node shall increase its
     * value by the AugmentationDuration value. This process may occur repeatedly, but after the first increase of
     * MotionDetectedDuration, the Node shall NOT increase the MotionDetectedDuration value unless the previous
     * MotionDetectedDuration has been exceeded by the TimeSinceActivation.
     * If the TimeSinceActivation value exceeds the MaxDuration or MotionDetectedDuration value, the Node shall generate
     * a PushTransportEnd event and stop detecting motion for this trigger for the period of the BlindDuration value.
     * If SetTransportStatus is called anytime during this state and sets the TransportStatus to Inactive, the Motion
     * Detected state is superseded.
     * Since multiple triggers (and corresponding push transports) may be activated by the same motion, the Node shall
     * perform this process independently for each motion trigger activated.
     */
    public static class TransportMotionTriggerTimeControlStruct {
        /**
         * This field shall indicate the initial duration (in seconds) of the recording, following the initial trigger.
         */
        public Integer initialDuration; // uint16
        /**
         * This field shall indicate the duration (in seconds) that the MotionDetectedDuration value is to be extended
         * by if motion is still detected during this period.
         */
        public Integer augmentationDuration; // uint16
        /**
         * This field shall indicate the maximum duration (in seconds) after initial motion detection that additional
         * motion will be detected.
         */
        public Integer maxDuration; // elapsed-s
        /**
         * This field shall indicate the duration (in seconds) after a transport finishes transmitting that the Node
         * shall NOT activate the trigger again.
         */
        public Integer blindDuration; // uint16

        public TransportMotionTriggerTimeControlStruct(Integer initialDuration, Integer augmentationDuration,
                Integer maxDuration, Integer blindDuration) {
            this.initialDuration = initialDuration;
            this.augmentationDuration = augmentationDuration;
            this.maxDuration = maxDuration;
            this.blindDuration = blindDuration;
        }
    }

    /**
     * This encodes the options and configuration of a transport.
     */
    public static class TransportOptionsStruct {
        /**
         * This field contains the StreamUsageEnum of this transport.
         */
        public StreamUsageEnum streamUsage; // StreamUsageEnum
        /**
         * This field is deprecated and the VideoStreams field used instead.
         * If this field is encountered from clients implementing cluster revision 1, then the following shall be done:
         * - If not present, video isn't requested.
         * - If present and null, automatic video stream assignment is requested.
         * - If present and non-null, the specific video stream identified by the VideoStreamID shall be added as an
         * entry to the VideoStreams field using the VideoStreamName of video.
         */
        public Integer videoStreamId; // CameraAvStreamManagement.VideoStreamID
        /**
         * This field is deprecated and the AudioStreams field used instead.
         * If this field is encountered from clients implementing cluster revision 1, then the following shall be done:
         * - If not present, audio isn't requested.
         * - If present and null, automatic audio stream assignment is requested.
         * - If present and non-null, the specific audio stream identified by the AudioStreamID shall be added as an
         * entry to the AudioStreams field using the AudioStreamName of audio.
         */
        public Integer audioStreamId; // CameraAvStreamManagement.AudioStreamID
        /**
         * This field shall be a TLSEndpointID representing a provisioned TLS Endpoint, which shall have valid TLSCAID
         * and TLSCCDID values (see Chapter 14, Certificate Authority ID (CAID) Mapping and the ProvisionEndpoint
         * command in the TLS Client Management Cluster sections in [[MatterCore]](#ref_MatterCore)).
         */
        public Integer tlsEndpointId; // TlsClientManagement.TLSEndpointID
        /**
         * This field shall be a valid string in RFC 3986 format representing the upload location. The field shall use
         * the https scheme which will be validated by the underlying TLSEndpointID.
         * When the IngestMethod is CMAFIngest, this shall be the CMAF publishing_point_URL to transport the AV Stream
         * to. The URL length does not need to include space for the full CMAF POST_URL fields which specify the
         * session, track, and segment names as these will be internally appended. See Section 11.7.1.2, "Operation" for
         * further restrictions on the characters allowed in the URL.
         */
        public String url; // string
        /**
         * This field shall be of type TransportTriggerOptionsStruct and represents the Trigger Type and its sub
         * options.
         */
        public TransportTriggerOptionsStruct triggerOptions; // TransportTriggerOptionsStruct
        /**
         * This field shall be of type IngestMethodsEnum and represents the Ingest Method to be used.
         */
        public IngestMethodsEnum ingestMethod; // IngestMethodsEnum
        /**
         * This field shall be of type ContainerOptionsStruct and represents the type of Push AV Stream Container to be
         * uploaded and any additional options relating to the Container Format used.
         */
        public ContainerOptionsStruct containerOptions; // ContainerOptionsStruct
        /**
         * This field shall be an unsigned 32 bit integer representing the TTL in seconds of a transport allocation. If
         * not present, the transport shall never expire.
         */
        public Integer expiryTime; // epoch-s
        /**
         * This field shall be a list of VideoStreamStruct which indicates the requested video streams and the stream
         * names for this transport.
         */
        public List<VideoStreamStruct> videoStreams; // list
        /**
         * This field shall be a list of AudioStreamStruct which indicates the requested audio streams and the stream
         * names for this transport.
         */
        public List<AudioStreamStruct> audioStreams; // list

        public TransportOptionsStruct(StreamUsageEnum streamUsage, Integer videoStreamId, Integer audioStreamId,
                Integer tlsEndpointId, String url, TransportTriggerOptionsStruct triggerOptions,
                IngestMethodsEnum ingestMethod, ContainerOptionsStruct containerOptions, Integer expiryTime,
                List<VideoStreamStruct> videoStreams, List<AudioStreamStruct> audioStreams) {
            this.streamUsage = streamUsage;
            this.videoStreamId = videoStreamId;
            this.audioStreamId = audioStreamId;
            this.tlsEndpointId = tlsEndpointId;
            this.url = url;
            this.triggerOptions = triggerOptions;
            this.ingestMethod = ingestMethod;
            this.containerOptions = containerOptions;
            this.expiryTime = expiryTime;
            this.videoStreams = videoStreams;
            this.audioStreams = audioStreams;
        }
    }

    /**
     * This encodes the current configuration of an allocated transport.
     */
    public static class TransportConfigurationStruct {
        /**
         * This field shall be a PushTransportConnectionID representing a unique transport.
         */
        public Integer connectionId; // PushTransportConnectionID
        /**
         * This field shall represent the Stream Transport Status of the transport.
         */
        public TransportStatusEnum transportStatus; // TransportStatusEnum
        /**
         * This field shall represent the Stream Transport Options of the transport.
         */
        public TransportOptionsStruct transportOptions; // TransportOptionsStruct
        public Integer fabricIndex; // FabricIndex

        public TransportConfigurationStruct(Integer connectionId, TransportStatusEnum transportStatus,
                TransportOptionsStruct transportOptions, Integer fabricIndex) {
            this.connectionId = connectionId;
            this.transportStatus = transportStatus;
            this.transportOptions = transportOptions;
            this.fabricIndex = fabricIndex;
        }
    }

    // Enums
    /**
     * The Trigger Type determines the basic operation of the Push Transport and when it will actually transmit content.
     */
    public enum TransportTriggerTypeEnum implements MatterEnum {
        COMMAND(0, "Command"),
        MOTION(1, "Motion"),
        CONTINUOUS(2, "Continuous");

        private final Integer value;
        private final String label;

        private TransportTriggerTypeEnum(Integer value, String label) {
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

    public enum TransportStatusEnum implements MatterEnum {
        ACTIVE(0, "Active"),
        INACTIVE(1, "Inactive");

        private final Integer value;
        private final String label;

        private TransportStatusEnum(Integer value, String label) {
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

    public enum ContainerFormatEnum implements MatterEnum {
        CMAF(0, "Cmaf");

        private final Integer value;
        private final String label;

        private ContainerFormatEnum(Integer value, String label) {
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

    public enum IngestMethodsEnum implements MatterEnum {
        CMAF_INGEST(0, "Cmaf Ingest");

        private final Integer value;
        private final String label;

        private IngestMethodsEnum(Integer value, String label) {
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

    public enum TriggerActivationReasonEnum implements MatterEnum {
        USER_INITIATED(0, "User Initiated"),
        AUTOMATION(1, "Automation"),
        EMERGENCY(2, "Emergency"),
        DOORBELL_PRESSED(3, "Doorbell Pressed");

        private final Integer value;
        private final String label;

        private TriggerActivationReasonEnum(Integer value, String label) {
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
     * This type indicates the exact mode of CMAF that is in use.
     */
    public enum CMAFInterfaceEnum implements MatterEnum {
        INTERFACE1(0, "Interface 1"),
        INTERFACE2DASH(1, "Interface 2 Dash"),
        INTERFACE2HLS(2, "Interface 2 Hls");

        private final Integer value;
        private final String label;

        private CMAFInterfaceEnum(Integer value, String label) {
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
        INVALID_TLS_ENDPOINT(2, "Invalid Tls Endpoint"),
        INVALID_STREAM(3, "Invalid Stream"),
        INVALID_URL(4, "Invalid Url"),
        INVALID_ZONE(5, "Invalid Zone"),
        INVALID_COMBINATION(6, "Invalid Combination"),
        INVALID_TRIGGER_TYPE(7, "Invalid Trigger Type"),
        INVALID_TRANSPORT_STATUS(8, "Invalid Transport Status"),
        INVALID_OPTIONS(9, "Invalid Options"),
        INVALID_STREAM_USAGE(10, "Invalid Stream Usage"),
        INVALID_TIME(11, "Invalid Time"),
        INVALID_PRE_ROLL_LENGTH(12, "Invalid Pre Roll Length"),
        DUPLICATE_STREAM_VALUES(13, "Duplicate Stream Values");

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

    // Bitmaps
    public static class FeatureMap {
        /**
         * 
         * When this feature is supported, the Sensitivity for a Motion Trigger can be set per zone. When not supported,
         * only a single sensitivity can be used for all Motion Triggers.
         */
        public boolean perZoneSensitivity;
        /**
         * 
         * When this feature is supported and a transport activates it, metadata shall be included within the uploaded
         * data.
         */
        public boolean metadata;

        public FeatureMap(boolean perZoneSensitivity, boolean metadata) {
            this.perZoneSensitivity = perZoneSensitivity;
            this.metadata = metadata;
        }
    }

    public PushAvStreamTransportCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 1365, "PushAvStreamTransport");
    }

    protected PushAvStreamTransportCluster(BigInteger nodeId, int endpointId, int clusterId, String clusterName) {
        super(nodeId, endpointId, clusterId, clusterName);
    }

    // commands
    /**
     * This command shall allocate a transport and return a PushTransportConnectionID.
     */
    public static ClusterCommand allocatePushTransport(TransportOptionsStruct transportOptions) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (transportOptions != null) {
            map.put("transportOptions", transportOptions);
        }
        return new ClusterCommand("allocatePushTransport", map);
    }

    /**
     * This command shall be generated to request the Node deallocates the specified transport.
     */
    public static ClusterCommand deallocatePushTransport(Integer connectionId) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (connectionId != null) {
            map.put("connectionId", connectionId);
        }
        return new ClusterCommand("deallocatePushTransport", map);
    }

    /**
     * This command is used to request the Node modifies the configuration of the specified push transport.
     */
    public static ClusterCommand modifyPushTransport(Integer connectionId, TransportOptionsStruct transportOptions) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (connectionId != null) {
            map.put("connectionId", connectionId);
        }
        if (transportOptions != null) {
            map.put("transportOptions", transportOptions);
        }
        return new ClusterCommand("modifyPushTransport", map);
    }

    /**
     * This command shall be generated to request the Node modifies the Transport Status of a specified transport or all
     * transports.
     */
    public static ClusterCommand setTransportStatus(Integer connectionId, TransportStatusEnum transportStatus) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (connectionId != null) {
            map.put("connectionId", connectionId);
        }
        if (transportStatus != null) {
            map.put("transportStatus", transportStatus);
        }
        return new ClusterCommand("setTransportStatus", map);
    }

    /**
     * This command shall be generated to request the Node to manually start the specified push transport.
     */
    public static ClusterCommand manuallyTriggerTransport(Integer connectionId,
            TriggerActivationReasonEnum activationReason, TransportMotionTriggerTimeControlStruct timeControl,
            OctetString userDefined) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (connectionId != null) {
            map.put("connectionId", connectionId);
        }
        if (activationReason != null) {
            map.put("activationReason", activationReason);
        }
        if (timeControl != null) {
            map.put("timeControl", timeControl);
        }
        if (userDefined != null) {
            map.put("userDefined", userDefined);
        }
        return new ClusterCommand("manuallyTriggerTransport", map);
    }

    /**
     * This command shall return the Transport Configuration for the specified push transport or all allocated
     * transports for the fabric if null.
     */
    public static ClusterCommand findTransport(Integer connectionId) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (connectionId != null) {
            map.put("connectionId", connectionId);
        }
        return new ClusterCommand("findTransport", map);
    }

    @Override
    public @NonNull String toString() {
        String str = "";
        str += "featureMap : " + featureMap + "\n";
        str += "supportedFormats : " + supportedFormats + "\n";
        str += "currentConnections : " + currentConnections + "\n";
        return str;
    }
}
