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
 * WebRtcTransportRequestor
 *
 * @author Dan Cunningham - Initial contribution
 */
public class WebRtcTransportRequestorCluster extends BaseCluster {

    public static final int CLUSTER_ID = 0x0554;
    public static final String CLUSTER_NAME = "WebRtcTransportRequestor";
    public static final String CLUSTER_PREFIX = "webRtcTransportRequestor";
    public static final String ATTRIBUTE_CURRENT_SESSIONS = "currentSessions";

    /**
     * This attribute shall be a list of WebRTCSessionStruct, which represents all the active WebRTC Sessions on this
     * Node.
     */
    public List<WebRtcTransportDefinitionsCluster.WebRTCSessionStruct> currentSessions; // 0 list R S A

    public WebRtcTransportRequestorCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 1364, "WebRtcTransportRequestor");
    }

    protected WebRtcTransportRequestorCluster(BigInteger nodeId, int endpointId, int clusterId, String clusterName) {
        super(nodeId, endpointId, clusterId, clusterName);
    }

    // commands
    /**
     * This command provides the stream requestor with WebRTC session details. It is sent following the receipt of a
     * SolicitOffer command or a re-offer initiated by the Provider.
     * This command shall respond with a response status of NOT_FOUND if the WebRTCSessionID does not match an entry in
     * CurrentSessions, or if the matching entry's associated fabric and PeerNodeID do not match the accessing fabric
     * and the Peer Node ID entry stored in the Secure Session Context (see Chapter 4 Secure Channel, Secure Session
     * Context section, in [[MatterCore]](#ref_MatterCore)) of the session this command was received on.
     */
    public static ClusterCommand offer(Integer webRtcSessionId, String sdp,
            List<WebRtcTransportDefinitionsCluster.ICEServerStruct> iceServers, String iceTransportPolicy) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (webRtcSessionId != null) {
            map.put("webRtcSessionId", webRtcSessionId);
        }
        if (sdp != null) {
            map.put("sdp", sdp);
        }
        if (iceServers != null) {
            map.put("iceServers", iceServers);
        }
        if (iceTransportPolicy != null) {
            map.put("iceTransportPolicy", iceTransportPolicy);
        }
        return new ClusterCommand("offer", map);
    }

    /**
     * This command provides the stream requestor with the WebRTC session details (i.e. Session ID and SDP answer), It
     * is the next command in the Offer/Answer flow to the ProvideOffer command.
     * This command shall respond with a response status of NOT_FOUND if the WebRTCSessionID does not match an entry in
     * CurrentSessions, or if the matching entry's associated fabric and PeerNodeID do not match the accessing fabric
     * and the Peer Node ID entry stored in the Secure Session Context of the session this command was received on.
     */
    public static ClusterCommand answer(Integer webRtcSessionId, String sdp) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (webRtcSessionId != null) {
            map.put("webRtcSessionId", webRtcSessionId);
        }
        if (sdp != null) {
            map.put("sdp", sdp);
        }
        return new ClusterCommand("answer", map);
    }

    /**
     * This command allows for the object based ICE candidates generated after the initial Offer / Answer exchange, via
     * a JSEP onicecandidate event, a DOM rtcpeerconnectioniceevent event, or other WebRTC compliant implementations, to
     * be added to a session during the gathering phase. This is typically used for STUN or TURN discovered candidates,
     * or to indicate the end of gathering state.
     * This command shall respond with a response status of NOT_FOUND if the WebRTCSessionID does not match an entry in
     * CurrentSessions, or if the matching entry's associated fabric and PeerNodeID do not match the accessing fabric
     * and the Peer Node ID entry stored in the Secure Session Context of the session this command was received on.
     */
    public static ClusterCommand iceCandidates(Integer webRtcSessionId,
            List<WebRtcTransportDefinitionsCluster.ICECandidateStruct> iceCandidates) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (webRtcSessionId != null) {
            map.put("webRtcSessionId", webRtcSessionId);
        }
        if (iceCandidates != null) {
            map.put("iceCandidates", iceCandidates);
        }
        return new ClusterCommand("iceCandidates", map);
    }

    /**
     * This command notifies the stream requestor that the provider has ended the WebRTC session.
     */
    public static ClusterCommand end(Integer webRtcSessionId,
            WebRtcTransportDefinitionsCluster.WebRTCEndReasonEnum reason) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (webRtcSessionId != null) {
            map.put("webRtcSessionId", webRtcSessionId);
        }
        if (reason != null) {
            map.put("reason", reason);
        }
        return new ClusterCommand("end", map);
    }

    @Override
    public @NonNull String toString() {
        String str = "";
        str += "currentSessions : " + currentSessions + "\n";
        return str;
    }
}
