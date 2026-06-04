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
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;

/**
 * WebRtcTransportDefinitions
 *
 * @author Dan Cunningham - Initial contribution
 */
public abstract class WebRtcTransportDefinitionsCluster extends BaseCluster {

    public static final String CLUSTER_NAME = "WebRtcTransportDefinitions";
    public static final String CLUSTER_PREFIX = "webRtcTransportDefinitions";

    // Structs
    /**
     * This type shall specify the RFC 8825 compliant ICE servers used to facilitate the negotiation of peer-to-peer
     * connections through NATs (Network Address Translators) and firewalls. It mimics the model used in the W3C WebRTC
     * API RTCIceServer dictionary with the addition of a Matter specific field for specifying the Root Certificate of
     * any ICE servers that require TLS.
     * There are two types of ICE Servers which help to discover the public IP address of a device and relay media
     * traffic when direct peer-to-peer communication is not possible:
     * - STUN Servers, which help to discover the public IP address and NAT/Firewall type if any, of a device. When a
     * WebRTC session starts, it contacts the STUN server, which returns the device's public IP and port number. This
     * information is used to generate ICE candidates for the peer-to-peer connection setup.
     * - TURN Servers, which are used when STUN is not sufficient to establish a peer-to-peer connection—typically such
     * as when devices are behind symmetric NATs, which STUN cannot traverse. TURN servers act as a relay between the
     * peers, routing the media traffic between them.
     */
    public static class ICEServerStruct {
        /**
         * This field shall specify a list of URLs pointing to the STUN and/or TURN servers. The URL scheme
         * distinguishes whether it is a STUN or TURN server (stun:, stuns:, turn:, or turns: respectively). This field
         * maps to the RTCIceServer urls field.
         */
        public List<String> urLs; // list
        /**
         * (Optional for STUN, usually required for TURN) The RFC 8489 compliant UTF-8 encoded username required for
         * authentication with the STUN or TURN servers found in the URLs field. This field maps to the RTCIceServer
         * username field.
         */
        public String username; // string
        /**
         * (Optional for STUN, usually required for TURN) The RFC 8489 compliant UTF-8 encoded short-term credential
         * (password) used for authentication with the STUN or TURN servers found in the URLs field. This field maps to
         * the RTCIceServer credential field.
         */
        public String credential; // string
        /**
         * This field represents the TLSRCAC via its assigned TLSCAID (see Chapter 14, Certificate Authority ID (CAID)
         * Mapping and TLS Certificate Management Commands sections in [[MatterCore]](#ref_MatterCore)) that will
         * validate the certificate chain presented by the entries in the urls field. It shall be set to a valid value
         * if a turns: or stuns: url is present in the urls field and shall be used to validate those servers' presented
         * TLS root certificates.
         */
        public Integer caid; // TlsCertificateManagement.TLSCAID

        public ICEServerStruct(List<String> urLs, String username, String credential, Integer caid) {
            this.urLs = urLs;
            this.username = username;
            this.credential = credential;
            this.caid = caid;
        }
    }

    /**
     * This type shall specify the RFC 8825 compliant ICE Candidate used to facilitate the negotiation of peer-to-peer
     * connections through NATs (Network Address Translators) and firewalls. It mimics the model used in the W3C WebRTC
     * API RTCIceCandidate dictionary.
     */
    public static class ICECandidateStruct {
        /**
         * This field shall specify the RFC 8825 compliant RFC 8839 candidate-attribute field in string form. This is
         * the same value as the W3C WebRTC API RTCIceCandidate candidate value. The RFCs define no min or max length on
         * this value.
         * Note: This string is not the same string as doing a candidate.toString() on a RTCIceCandidate ECMAScript
         * object itself. Some browsers and ECMAScript libraries use non-standard ways to serialize sdpMid and
         * sdpMLineIndex into the resulting string, but this is not defined in the W3 specification. This specification
         * requires those fields to be passed directly using the named struct fields SDPMid and SDPMLineIndex.
         */
        public String candidate; // string
        /**
         * This field shall specify the Candidate's media stream identification tag which uniquely identifies the media
         * stream within the component with which the candidate is associated, or null if no such association exists.
         * This is the same value as the W3C WebRTC API RTCIceCandidate sdpMid value. The RFCs define no max length on
         * this value.
         */
        public String sdpMid; // string
        /**
         * This field shall specify the zero-based index number of the media description (as defined in RFC 8866) in the
         * SDP with which the Candidate is associated or null if no such association exists. This is the same value as
         * the W3C WebRTC API RTCIceCandidate sdpMLineIndex value.
         */
        public Integer sdpmLineIndex; // uint16

        public ICECandidateStruct(String candidate, String sdpMid, Integer sdpmLineIndex) {
            this.candidate = candidate;
            this.sdpMid = sdpMid;
            this.sdpmLineIndex = sdpmLineIndex;
        }
    }

    /**
     * This type stores all the relevant values associated with an active WebRTC session.
     * This values of PeerNodeID and FabricIndex are used to validate the source of, or select the correct remote
     * target, for WebRTC session related commands. The implicit field FabricIndex exists since this structure is
     * defined as Fabric Scoped.
     */
    public static class WebRTCSessionStruct {
        /**
         * This field contains the WebRTC Session ID for this session.
         */
        public Integer id; // WebRTCSessionID
        /**
         * This field contains the NodeId for the peer entity involved in this session.
         */
        public BigInteger peerNodeId; // node-id
        /**
         * This field contains the EndpointId for the peer entity involved in this session.
         */
        public Integer peerEndpointId; // endpoint-no
        /**
         * This field contains the StreamUsageEnum of this session.
         */
        public StreamUsageEnum streamUsage; // StreamUsageEnum
        /**
         * This field is deprecated and the VideoStreams field used instead.
         * For compatibility with clients implementing cluster revision 1, the first video stream found in the
         * VideoStreams field shall be populated here, or null if no video stream is currently associated with this
         * session.
         */
        public Integer videoStreamId; // CameraAvStreamManagement.VideoStreamID
        /**
         * This field is deprecated and the AudioStreams field used instead.
         * For compatibility with clients implementing cluster revision 1, the first audio stream found in the
         * AudioStreams field shall be populated here, or null if no audio stream is currently associated with this
         * session.
         */
        public Integer audioStreamId; // CameraAvStreamManagement.AudioStreamID
        /**
         * This field indicates if metadata is active in this session.
         */
        public Boolean metadataEnabled; // bool
        /**
         * This field shall be a list of all video streams used by this session. Each VideoStreamID entry corresponds to
         * an entry in the AllocatedVideoStreams attribute.
         * - If present, the specified video streams from the AllocatedVideoStreams attribute shall be used.
         * - If not present, this session has no video.
         */
        public List<Integer> videoStreams; // list
        /**
         * This field shall be a list of all audio streams used by this session. Each VideoStreamID entry corresponds to
         * an entry in the AllocatedVideoStreams attribute.
         * - If present, the specified audio streams from the AllocatedAudioStreams attribute shall be used.
         * - If not present, this session has no audio.
         */
        public List<Integer> audioStreams; // list
        public Integer fabricIndex; // FabricIndex

        public WebRTCSessionStruct(Integer id, BigInteger peerNodeId, Integer peerEndpointId,
                StreamUsageEnum streamUsage, Integer videoStreamId, Integer audioStreamId, Boolean metadataEnabled,
                List<Integer> videoStreams, List<Integer> audioStreams, Integer fabricIndex) {
            this.id = id;
            this.peerNodeId = peerNodeId;
            this.peerEndpointId = peerEndpointId;
            this.streamUsage = streamUsage;
            this.videoStreamId = videoStreamId;
            this.audioStreamId = audioStreamId;
            this.metadataEnabled = metadataEnabled;
            this.videoStreams = videoStreams;
            this.audioStreams = audioStreams;
            this.fabricIndex = fabricIndex;
        }
    }

    // Enums
    public enum WebRTCEndReasonEnum implements MatterEnum {
        ICE_FAILED(0, "Ice Failed"),
        ICE_TIMEOUT(1, "Ice Timeout"),
        USER_HANGUP(2, "User Hangup"),
        USER_BUSY(3, "User Busy"),
        REPLACED(4, "Replaced"),
        NO_USER_MEDIA(5, "No User Media"),
        INVITE_TIMEOUT(6, "Invite Timeout"),
        ANSWERED_ELSEWHERE(7, "Answered Elsewhere"),
        OUT_OF_RESOURCES(8, "Out Of Resources"),
        MEDIA_TIMEOUT(9, "Media Timeout"),
        LOW_POWER(10, "Low Power"),
        PRIVACY_MODE(11, "Privacy Mode"),
        UNKNOWN_REASON(12, "Unknown Reason");

        private final Integer value;
        private final String label;

        private WebRTCEndReasonEnum(Integer value, String label) {
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

    protected WebRtcTransportDefinitionsCluster(BigInteger nodeId, int endpointId, int clusterId, String clusterName) {
        super(nodeId, endpointId, clusterId, clusterName);
    }

    @Override
    public @NonNull String toString() {
        String str = "";
        return str;
    }
}
