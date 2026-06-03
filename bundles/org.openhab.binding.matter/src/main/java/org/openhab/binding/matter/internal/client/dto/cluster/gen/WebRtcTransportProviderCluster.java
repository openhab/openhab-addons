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
 * WebRtcTransportProvider
 *
 * @author Dan Cunningham - Initial contribution
 */
public class WebRtcTransportProviderCluster extends BaseCluster {

    public static final int CLUSTER_ID = 0x0553;
    public static final String CLUSTER_NAME = "WebRtcTransportProvider";
    public static final String CLUSTER_PREFIX = "webRtcTransportProvider";
    public static final String ATTRIBUTE_FEATURE_MAP = "featureMap";
    public static final String ATTRIBUTE_CURRENT_SESSIONS = "currentSessions";

    public FeatureMap featureMap; // 65532 FeatureMap
    /**
     * This attribute shall be a list of WebRTCSessionStruct, which represents all the active WebRTC Sessions.
     */
    public List<WebRtcTransportDefinitionsCluster.WebRTCSessionStruct> currentSessions; // 0 list R S M

    // Structs
    /**
     * This type shall specify the RFC 9605 data needed to use SFrames as an end-to-end encryption mechanism with
     * WebRTC.
     */
    public static class SFrameStruct {
        /**
         * This field shall specify the SFrame cipher suite value as defined in RFC 9605 Section 8.1 Cipher Suites
         * table, and maintained in the IANA SFrame Registry.
         */
        public Integer cipherSuite; // uint16
        /**
         * This field shall specify the SFrame base_key value to use for a session. The length of this key depends on
         * the selected cipher suite's Nk value as defined in Section 4.5 Cipher Suites.
         */
        public OctetString baseKey; // octstr
        /**
         * This field shall specify the initial SFrame KID (Key Id) value to be used. The bottom 8 bits of this value
         * will be overwritten and used for ratchet step tracking.
         */
        public OctetString kid; // octstr

        public SFrameStruct(Integer cipherSuite, OctetString baseKey, OctetString kid) {
            this.cipherSuite = cipherSuite;
            this.baseKey = baseKey;
            this.kid = kid;
        }
    }

    // Bitmaps
    public static class FeatureMap {
        /**
         * 
         * When this feature is supported and a session activates it, a WebRTC DataChannel using the protocol name
         * urn:csa:matter:av-metadata shall be used for transmitting the metadata. The ability to include metadata is
         * supported on a per session basis.
         * This feature is designed to be JSEP compliant with the RTCDataChannel object interface and consists of
         * AVMetadataStruct content.
         * If SFrame End-to-End Encryption is active in a session, all metadata transmissions shall be sent using the
         * protocol name urn:csa:matter:sframe:av-metadata instead with each transmission being wrapped in SFrames.
         */
        public boolean metadata;

        public FeatureMap(boolean metadata) {
            this.metadata = metadata;
        }
    }

    public WebRtcTransportProviderCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 1363, "WebRtcTransportProvider");
    }

    protected WebRtcTransportProviderCluster(BigInteger nodeId, int endpointId, int clusterId, String clusterName) {
        super(nodeId, endpointId, clusterId, clusterName);
    }

    // commands
    /**
     * Requests that the Provider initiates a new session with the Offer / Answer flow in a way that allows for options
     * to be passed and work with devices needing the standby flow.
     */
    public static ClusterCommand solicitOffer(StreamUsageEnum streamUsage, Integer originatingEndpointId,
            Integer videoStreamId, Integer audioStreamId,
            List<WebRtcTransportDefinitionsCluster.ICEServerStruct> iceServers, String iceTransportPolicy,
            Boolean metadataEnabled, SFrameStruct sFrameConfig, List<Integer> videoStreams,
            List<Integer> audioStreams) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (streamUsage != null) {
            map.put("streamUsage", streamUsage);
        }
        if (originatingEndpointId != null) {
            map.put("originatingEndpointId", originatingEndpointId);
        }
        if (videoStreamId != null) {
            map.put("videoStreamId", videoStreamId);
        }
        if (audioStreamId != null) {
            map.put("audioStreamId", audioStreamId);
        }
        if (iceServers != null) {
            map.put("iceServers", iceServers);
        }
        if (iceTransportPolicy != null) {
            map.put("iceTransportPolicy", iceTransportPolicy);
        }
        if (metadataEnabled != null) {
            map.put("metadataEnabled", metadataEnabled);
        }
        if (sFrameConfig != null) {
            map.put("sFrameConfig", sFrameConfig);
        }
        if (videoStreams != null) {
            map.put("videoStreams", videoStreams);
        }
        if (audioStreams != null) {
            map.put("audioStreams", audioStreams);
        }
        return new ClusterCommand("solicitOffer", map);
    }

    /**
     * This command allows an SDP Offer to be set and start a new session. This command can also be used in the re-offer
     * flow of an existing session to change the details of the SDP (e.g. to enable/disable two-way talk).
     */
    public static ClusterCommand provideOffer(Integer webRtcSessionId, String sdp, StreamUsageEnum streamUsage,
            Integer originatingEndpointId, Integer videoStreamId, Integer audioStreamId,
            List<WebRtcTransportDefinitionsCluster.ICEServerStruct> iceServers, String iceTransportPolicy,
            Boolean metadataEnabled, SFrameStruct sFrameConfig, List<Integer> videoStreams,
            List<Integer> audioStreams) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (webRtcSessionId != null) {
            map.put("webRtcSessionId", webRtcSessionId);
        }
        if (sdp != null) {
            map.put("sdp", sdp);
        }
        if (streamUsage != null) {
            map.put("streamUsage", streamUsage);
        }
        if (originatingEndpointId != null) {
            map.put("originatingEndpointId", originatingEndpointId);
        }
        if (videoStreamId != null) {
            map.put("videoStreamId", videoStreamId);
        }
        if (audioStreamId != null) {
            map.put("audioStreamId", audioStreamId);
        }
        if (iceServers != null) {
            map.put("iceServers", iceServers);
        }
        if (iceTransportPolicy != null) {
            map.put("iceTransportPolicy", iceTransportPolicy);
        }
        if (metadataEnabled != null) {
            map.put("metadataEnabled", metadataEnabled);
        }
        if (sFrameConfig != null) {
            map.put("sFrameConfig", sFrameConfig);
        }
        if (videoStreams != null) {
            map.put("videoStreams", videoStreams);
        }
        if (audioStreams != null) {
            map.put("audioStreams", audioStreams);
        }
        return new ClusterCommand("provideOffer", map);
    }

    /**
     * This command shall be initiated from a Node in response to an Offer that was previously received from a remote
     * peer. It shall have the following data fields:
     * This command shall respond with a response status of NOT_FOUND if the WebRTCSessionID does not match an entry in
     * CurrentSessions, or if the matching entry's associated fabric and PeerNodeID do not match the accessing fabric
     * and the Peer Node ID entry stored in the Secure Session Context of the session this command was received on.
     */
    public static ClusterCommand provideAnswer(Integer webRtcSessionId, String sdp) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (webRtcSessionId != null) {
            map.put("webRtcSessionId", webRtcSessionId);
        }
        if (sdp != null) {
            map.put("sdp", sdp);
        }
        return new ClusterCommand("provideAnswer", map);
    }

    /**
     * This command allows for string based ICE candidates generated after the initial Offer / Answer exchange, via a
     * JSEP onicecandidate event, a DOM rtcpeerconnectioniceevent event, or other WebRTC compliant implementations, to
     * be added to a session during the gathering phase. This is typically used for STUN or TURN discovered candidates,
     * or to indicate the end of gathering state.
     * This command shall respond with a response status of NOT_FOUND if the WebRTCSessionID does not match an entry in
     * CurrentSessions, or if the matching entry's associated fabric and PeerNodeID do not match the accessing fabric
     * and the Peer Node ID entry stored in the Secure Session Context of the session this command was received on.
     */
    public static ClusterCommand provideIceCandidates(Integer webRtcSessionId,
            List<WebRtcTransportDefinitionsCluster.ICECandidateStruct> iceCandidates) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (webRtcSessionId != null) {
            map.put("webRtcSessionId", webRtcSessionId);
        }
        if (iceCandidates != null) {
            map.put("iceCandidates", iceCandidates);
        }
        return new ClusterCommand("provideIceCandidates", map);
    }

    /**
     * This command instructs the stream provider to end the WebRTC session.
     */
    public static ClusterCommand endSession(Integer webRtcSessionId,
            WebRtcTransportDefinitionsCluster.WebRTCEndReasonEnum reason) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (webRtcSessionId != null) {
            map.put("webRtcSessionId", webRtcSessionId);
        }
        if (reason != null) {
            map.put("reason", reason);
        }
        return new ClusterCommand("endSession", map);
    }

    @Override
    public @NonNull String toString() {
        String str = "";
        str += "featureMap : " + featureMap + "\n";
        str += "currentSessions : " + currentSessions + "\n";
        return str;
    }
}
