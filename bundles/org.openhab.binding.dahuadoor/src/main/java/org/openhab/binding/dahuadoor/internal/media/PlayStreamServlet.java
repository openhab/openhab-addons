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
package org.openhab.binding.dahuadoor.internal.media;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.dahuadoor.internal.DahuaDoorBaseHandler;
import org.openhab.binding.dahuadoor.internal.DahuaDoorBindingConstants;
import org.openhab.binding.dahuadoor.internal.DahuaDoorHandlerFactory;
import org.openhab.binding.dahuadoor.internal.sip.SipClient;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PlayStreamServlet} is an HTTP servlet registered with the OSGi {@link HttpService}
 * at {@value DahuaDoorBindingConstants#WEBRTC_SERVLET_PATH}.
 *
 * <p>
 * It proxies WebRTC SDP offer/answer exchange between the browser and the local go2rtc instance,
 * making go2rtc's localhost-only API accessible to browser clients via openHAB's HTTP port.
 * </p>
 *
 * <h3>Usage</h3>
 * 
 * <pre>
 *   POST /dahuadoor/webrtc/{streamName}
 *   Content-Type: application/x-www-form-urlencoded
 *   Body: data=&lt;base64(SDP offer)&gt;
 *
 *   Response 200:
 *   Content-Type: text/plain
 *   Body: base64(SDP answer)
 * </pre>
 *
 * <p>
 * Based on the WebRTC servlet in the UniFi Protect binding.
 * </p>
 *
 * @author Dan Cunningham - Initial contribution
 * @author Sven Schad - Adapted for DahuaDoor binding
 */
@NonNullByDefault
public class PlayStreamServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(PlayStreamServlet.class);

    /** Timeout for the HTTP call to the go2rtc API. */
    private static final int GO2RTC_API_TIMEOUT_MS = 10_000;
    /** Maximum accepted size for incoming request body. */
    private static final int MAX_REQUEST_BODY_BYTES = 64 * 1024;
    private static final String SESSION_STREAM_PATH = "session";
    /** Correlation id generator for WebRTC proxy requests. */
    private static final AtomicLong REQUEST_SEQUENCE = new AtomicLong();
    /** How long a request may stay in CONNECTING before the lock is treated as stale. */
    private static final long CONNECTING_STALE_MS = 30_000L;
    /** Fallback timeout for ACTIVE locks if SIP state callbacks are not observed. */
    private static final long ACTIVE_STALE_MS = 30 * 60_000L;

    private final HttpService httpService;
    private final DahuaDoorHandlerFactory handlerFactory;

    /**
     * Map from go2rtc stream name (e.g. {@code dahua_vto2202_living}) to API port.
     * Modified from multiple threads → ConcurrentHashMap.
     */
    private final Map<String, Integer> streamApiPorts = new ConcurrentHashMap<>();

    /** Map from stream name to handler for SIP state lookup. */
    private final Map<String, DahuaDoorBaseHandler> streamHandlers = new ConcurrentHashMap<>();

    /**
     * Per stream+client request guard.
     *
     * Key format: {@code <streamName>|<clientKey>} where clientKey is either explicit clientId
     * query parameter or HTTP session id.
     */
    private final Map<String, OfferGateState> offerGateByStreamAndClient = new ConcurrentHashMap<>();

    /** Last logged mapping per stream+HTTP session to avoid repeated log spam. */
    private final Map<String, String> lastLoggedSessionClientMapping = new ConcurrentHashMap<>();

    /** Number of currently registered streams; used to decide when to unregister the servlet. */
    private volatile int registrationCount = 0;

    public PlayStreamServlet(HttpService httpService, DahuaDoorHandlerFactory handlerFactory) {
        this.httpService = httpService;
        this.handlerFactory = handlerFactory;
    }

    // -------------------------------------------------------------------------
    // Stream registration / de-registration
    // -------------------------------------------------------------------------

    /**
     * Registers a stream and – on the first registration – activates the servlet.
     *
     * @param streamName go2rtc stream name
     * @param apiPort go2rtc API port for that stream
     * @param handler owning thing handler used to resolve SIP session state for this stream
     */
    public synchronized void registerStream(String streamName, int apiPort, DahuaDoorBaseHandler handler) {
        Integer previous = streamApiPorts.put(streamName, apiPort);
        streamHandlers.put(streamName, handler);
        if (previous == null && registrationCount == 0) {
            if (!activate()) {
                streamApiPorts.remove(streamName);
                streamHandlers.remove(streamName);
                LOGGER.warn("Could not register stream '{}' because servlet activation failed", streamName);
                return;
            }
        }
        if (previous == null) {
            registrationCount++;
        }
        LOGGER.debug("Registered WebRTC stream '{}' on port {}", streamName, apiPort);
    }

    /**
     * De-registers a stream and – on the last de-registration – deactivates the servlet.
     *
     * @param streamName go2rtc stream name
     */
    public synchronized void unregisterStream(String streamName) {
        Integer removed = streamApiPorts.remove(streamName);
        streamHandlers.remove(streamName);
        if (removed != null) {
            registrationCount = Math.max(0, registrationCount - 1);
            if (registrationCount == 0) {
                deactivate();
            }
        }
        offerGateByStreamAndClient.keySet().removeIf(key -> key.startsWith(streamName + "|"));
        lastLoggedSessionClientMapping.keySet().removeIf(key -> key.startsWith(streamName + "|"));
        LOGGER.debug("Unregistered WebRTC stream '{}'", streamName);
    }

    public synchronized void deactivateAll() {
        streamApiPorts.clear();
        streamHandlers.clear();
        offerGateByStreamAndClient.clear();
        lastLoggedSessionClientMapping.clear();
        registrationCount = 0;
        deactivate();
    }

    // -------------------------------------------------------------------------
    // Servlet implementation
    // -------------------------------------------------------------------------

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        addCorsHeaders(resp);

        // Path info is /{streamName}
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.length() <= 1) {
            sendBase64Message(resp, HttpServletResponse.SC_BAD_REQUEST, "Stream name missing in URL path");
            LOGGER.warn("Rejected request: missing stream name in URL path");
            return;
        }
        String streamName = pathInfo.substring(1); // strip leading /
        boolean sessionEndpointRequest = SESSION_STREAM_PATH.equals(streamName);
        if (SESSION_STREAM_PATH.equals(streamName)) {
            @Nullable
            String resolvedStreamName = resolveSessionStreamName(req, resp);
            if (resolvedStreamName == null) {
                return;
            }
            streamName = resolvedStreamName;
        }
        long requestId = REQUEST_SEQUENCE.incrementAndGet();
        long startedAtNanos = System.nanoTime();

        @Nullable
        String clientIdParam = getQueryParameter(req, "clientId");
        @Nullable
        String sessionId = resolveSessionId(req);
        if (sessionId == null || sessionId.isBlank()) {
            sendBase64Message(resp, HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "No usable HTTP session id available for WebRTC request.");
            LOGGER.warn("WebRTC proxy [{}] rejected: missing session id for stream '{}'", requestId, streamName);
            return;
        }
        String clientKey = clientIdParam != null && !clientIdParam.isBlank() ? clientIdParam : sessionId;
        String streamClientKey = buildStreamClientKey(streamName, clientKey);

        logSessionToDahuaClientMapping(streamName, sessionId, clientIdParam);

        if (!tryAcquireOfferGate(streamName, streamClientKey, sessionId)) {
            if (sessionEndpointRequest) {
                // Accept /webrtc/session requests instead of returning a hard 409.
                LOGGER.trace("/webrtc/session accepted stream='{}' clientKey='{}' sessionId='{}'", streamName,
                        clientKey, sessionId);
            } else {
                sendBase64Message(resp, HttpServletResponse.SC_CONFLICT,
                        "Duplicate WebRTC request rejected for same client while setup/connection is active.");
                LOGGER.warn("Rejected duplicate WebRTC offer for stream '{}' (clientKey={})", streamName, clientKey);
                return;
            }
        }

        Integer apiPort = streamApiPorts.get(streamName);
        if (apiPort == null) {
            releaseOfferGate(streamClientKey);
            sendBase64Message(resp, HttpServletResponse.SC_NOT_FOUND,
                    "Unknown stream: " + streamName + ". Is the thing online with WebRTC enabled?");
            LOGGER.warn("WebRTC proxy [{}] rejected: unknown stream '{}'", requestId, streamName);
            return;
        }

        String requestBody;
        try (InputStream in = req.getInputStream()) {
            requestBody = new String(readLimitedBytes(in, MAX_REQUEST_BODY_BYTES), StandardCharsets.UTF_8);
        } catch (RequestTooLargeException e) {
            releaseOfferGate(streamClientKey);
            sendBase64Message(resp, HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE,
                    "Request body too large. Maximum supported size is " + MAX_REQUEST_BODY_BYTES + " bytes.");
            LOGGER.warn("Rejected request for stream '{}': request body exceeded {} bytes", streamName,
                    MAX_REQUEST_BODY_BYTES);
            return;
        }
        if (requestBody.isBlank()) {
            releaseOfferGate(streamClientKey);
            sendBase64Message(resp, HttpServletResponse.SC_BAD_REQUEST, "Empty request body");
            LOGGER.warn("Rejected request for stream '{}': empty body", streamName);
            return;
        }

        if (!requestBody.startsWith("data=")) {
            releaseOfferGate(streamClientKey);
            sendBase64Message(resp, HttpServletResponse.SC_BAD_REQUEST,
                    "Unsupported request format. Expected form body: data=<base64(sdp)>.");
            LOGGER.warn("Rejected request for stream '{}': unsupported input mode", streamName);
            return;
        }
        if (requestBody.indexOf('&') >= 0) {
            releaseOfferGate(streamClientKey);
            sendBase64Message(resp, HttpServletResponse.SC_BAD_REQUEST,
                    "Unsupported request format. Only one form field is allowed: data=<base64(sdp)>.");
            LOGGER.warn("Rejected request for stream '{}': additional form fields present", streamName);
            return;
        }

        String decodedFormValue = URLDecoder.decode(requestBody.substring(5), StandardCharsets.UTF_8);
        if (decodedFormValue.isBlank()) {
            releaseOfferGate(streamClientKey);
            sendBase64Message(resp, HttpServletResponse.SC_BAD_REQUEST, "Missing SDP payload in form field 'data'.");
            LOGGER.warn("Rejected request for stream '{}': empty data field", streamName);
            return;
        }

        String sdpOffer;
        try {
            sdpOffer = new String(Base64.getDecoder().decode(decodedFormValue), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            releaseOfferGate(streamClientKey);
            sendBase64Message(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid Base64 payload in form field 'data'.");
            LOGGER.warn("Rejected request for stream '{}': invalid base64 payload", streamName);
            return;
        }
        if (sdpOffer.isBlank()) {
            releaseOfferGate(streamClientKey);
            sendBase64Message(resp, HttpServletResponse.SC_BAD_REQUEST, "Decoded SDP offer is empty.");
            LOGGER.warn("Rejected request for stream '{}': decoded SDP is empty", streamName);
            return;
        }

        String remote = firstNonBlank(req.getHeader("X-Forwarded-For"), req.getRemoteAddr());
        String origin = firstNonBlank(req.getHeader("Origin"), "-");
        String userAgent = abbreviate(req.getHeader("User-Agent"), 160);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(
                    "WebRTC proxy [{}] incoming POST stream='{}' remote='{}' origin='{}' userAgent='{}' bodyBytes={} offerBytes={} audio={} audioLines={}",
                    requestId, streamName, remote, origin, userAgent, requestBody.length(),
                    sdpOffer.getBytes(StandardCharsets.UTF_8).length, summarizeAudioSdpDirection(sdpOffer),
                    extractAudioSdpLines(sdpOffer));
        }

        // Forward to go2rtc API
        String go2rtcUrl = "http://127.0.0.1:" + apiPort + "/api/webrtc?src="
                + URLEncoder.encode(streamName, StandardCharsets.UTF_8);

        HttpURLConnection conn = (HttpURLConnection) URI.create(go2rtcUrl).toURL().openConnection();
        try {
            conn.setConnectTimeout(GO2RTC_API_TIMEOUT_MS);
            conn.setReadTimeout(GO2RTC_API_TIMEOUT_MS);
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/sdp");
            byte[] offerBytes = sdpOffer.getBytes(StandardCharsets.UTF_8);
            conn.setRequestProperty("Content-Length", String.valueOf(offerBytes.length));
            LOGGER.debug("WebRTC proxy [{}] forwarding POST to {} (stream='{}', offerBytes={})", requestId, go2rtcUrl,
                    streamName, offerBytes.length);
            try (OutputStream out = conn.getOutputStream()) {
                out.write(offerBytes);
            }

            int status = conn.getResponseCode();
            if (status >= 300) {
                byte[] errorBodyBytes;
                InputStream errorStream = conn.getErrorStream();
                if (errorStream != null) {
                    try (InputStream in = errorStream) {
                        errorBodyBytes = in.readAllBytes();
                    }
                } else {
                    errorBodyBytes = new byte[0];
                }
                String errorBody = new String(errorBodyBytes, StandardCharsets.UTF_8).trim();
                if (errorBody.length() > 300) {
                    errorBody = errorBody.substring(0, 300);
                }
                String message = errorBody.isEmpty() ? "go2rtc API returned HTTP " + status
                        : "go2rtc API returned HTTP " + status + ": " + errorBody;
                releaseOfferGate(streamClientKey);
                sendBase64Message(resp, HttpServletResponse.SC_BAD_GATEWAY, message);
                LOGGER.warn(
                        "WebRTC proxy [{}] upstream error stream='{}' status={} bodyBytes={} durationMs={} body='{}'",
                        requestId, streamName, status, errorBodyBytes.length, elapsedMillis(startedAtNanos),
                        abbreviate(errorBody, 300));
                return;
            }

            byte[] sdpAnswer;
            try (InputStream in = conn.getInputStream()) {
                sdpAnswer = in.readAllBytes();
            }

            if (LOGGER.isDebugEnabled()) {
                String answerText = new String(sdpAnswer, StandardCharsets.UTF_8);
                LOGGER.debug(
                        "WebRTC proxy [{}] upstream answer stream='{}' status={} answerBytes={} durationMs={} audio={} audioLines={}",
                        requestId, streamName, status, sdpAnswer.length, elapsedMillis(startedAtNanos),
                        summarizeAudioSdpDirection(answerText), extractAudioSdpLines(answerText));
            }

            byte[] encoded = Base64.getEncoder().encode(sdpAnswer);
            sendEncodedAnswer(resp, encoded);
            markOfferGateActive(streamClientKey);
        } catch (IOException e) {
            releaseOfferGate(streamClientKey);
            sendBase64Message(resp, HttpServletResponse.SC_BAD_GATEWAY,
                    "Failed to reach go2rtc API: " + e.getMessage());
            LOGGER.warn("WebRTC proxy [{}] upstream communication failure stream='{}' after {} ms: {}", requestId,
                    streamName, elapsedMillis(startedAtNanos), e.getMessage());
        } finally {
            conn.disconnect();
        }
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        addCorsHeaders(resp);
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------

    private boolean activate() {
        Dictionary<String, String> params = new Hashtable<>();
        try {
            httpService.registerServlet(DahuaDoorBindingConstants.WEBRTC_SERVLET_PATH, this, params,
                    httpService.createDefaultHttpContext());
            LOGGER.debug("PlayStreamServlet registered at {}", DahuaDoorBindingConstants.WEBRTC_SERVLET_PATH);
            return true;
        } catch (ServletException | NamespaceException e) {
            LOGGER.error("Failed to register PlayStreamServlet: {}", e.getMessage(), e);
            return false;
        }
    }

    private void deactivate() {
        try {
            httpService.unregister(DahuaDoorBindingConstants.WEBRTC_SERVLET_PATH);
            LOGGER.debug("PlayStreamServlet unregistered");
        } catch (IllegalArgumentException e) {
            LOGGER.trace("PlayStreamServlet was not registered: {}", e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    private static void addCorsHeaders(HttpServletResponse resp) {
        resp.setHeader("Access-Control-Allow-Methods", "POST, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
    }

    private static void sendEncodedAnswer(HttpServletResponse resp, byte[] encodedAnswer) throws IOException {
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType("text/plain");
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.getOutputStream().write(encodedAnswer);
    }

    private @Nullable String resolveSessionStreamName(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String thingUid = getQueryParameter(req, "thing");
        if (thingUid == null || thingUid.isBlank()) {
            sendBase64Message(resp, HttpServletResponse.SC_BAD_REQUEST, "Missing thing parameter");
            return null;
        }

        DahuaDoorBaseHandler handler = handlerFactory.getDahuaHandler(thingUid);
        if (handler == null) {
            sendBase64Message(resp, HttpServletResponse.SC_NOT_FOUND,
                    "Thing not found or not initialized: " + thingUid);
            return null;
        }

        String sessionId = req.getSession().getId();
        @Nullable
        String clientId = handler.assignClientForSession(sessionId);
        if (clientId == null) {
            sendBase64Message(resp, HttpServletResponse.SC_CONFLICT, "No free outgoing SIP client available");
            return null;
        }

        @Nullable
        String streamName = handler.getOrAllocateWebRtcStreamNameForSession(sessionId, clientId);
        if (streamName == null || streamName.isBlank()) {
            sendBase64Message(resp, HttpServletResponse.SC_NOT_FOUND,
                    "No WebRTC stream available for SIP client: " + clientId);
            return null;
        }

        int relayListenPort = handler.getRelayListenPortForClientId(clientId);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Allocated WebRTC stream '{}' for session '{}' clientId='{}' relayListenPort={}", streamName,
                    sessionId, clientId, relayListenPort);
        }
        return streamName;
    }

    private static @Nullable String getQueryParameter(HttpServletRequest req, String name) {
        String query = req.getQueryString();
        if (query == null || query.isBlank()) {
            return null;
        }

        for (String segment : query.split("&")) {
            int separator = segment.indexOf('=');
            String key = separator >= 0 ? segment.substring(0, separator) : segment;
            if (!name.equals(URLDecoder.decode(key, StandardCharsets.UTF_8))) {
                continue;
            }

            String value = separator >= 0 ? segment.substring(separator + 1) : "";
            return URLDecoder.decode(value, StandardCharsets.UTF_8);
        }

        return null;
    }

    private static void sendBase64Message(HttpServletResponse resp, int statusCode, String message) throws IOException {
        byte[] encoded = Base64.getEncoder().encode(message.getBytes(StandardCharsets.UTF_8));
        resp.setStatus(statusCode);
        resp.setContentType("text/plain");
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.getOutputStream().write(encoded);
    }

    private static String buildStreamClientKey(String streamName, String clientKey) {
        return streamName + "|" + clientKey;
    }

    private static @Nullable String resolveSessionId(HttpServletRequest req) {
        try {
            HttpSession session = req.getSession();
            return session.getId();
        } catch (IllegalStateException e) {
            LOGGER.warn("WebRTC request failed to access session id: {}", e.getMessage());
            return null;
        }
    }

    private boolean tryAcquireOfferGate(String streamName, String streamClientKey, String sessionId) {
        // Primary release signal: SIP call control transitioned to a terminal state.
        if (isSipTerminalState(streamName, sessionId)) {
            releaseOfferGate(streamClientKey);
        }

        long now = System.currentTimeMillis();
        OfferGateState state = offerGateByStreamAndClient.compute(streamClientKey, (key, current) -> {
            if (current == null) {
                return new OfferGateState(OfferGatePhase.CONNECTING, now);
            }
            long ageMs = now - current.lastUpdateMs;
            if (current.phase == OfferGatePhase.CONNECTING && ageMs > CONNECTING_STALE_MS) {
                return new OfferGateState(OfferGatePhase.CONNECTING, now);
            }
            if (current.phase == OfferGatePhase.ACTIVE && ageMs > ACTIVE_STALE_MS) {
                return new OfferGateState(OfferGatePhase.CONNECTING, now);
            }
            return current;
        });
        return state != null && state.phase == OfferGatePhase.CONNECTING && state.lastUpdateMs == now;
    }

    private void markOfferGateActive(String streamClientKey) {
        long now = System.currentTimeMillis();
        offerGateByStreamAndClient.computeIfPresent(streamClientKey,
                (key, current) -> new OfferGateState(OfferGatePhase.ACTIVE, now));
    }

    private void releaseOfferGate(String streamClientKey) {
        offerGateByStreamAndClient.remove(streamClientKey);
    }

    private boolean isSipTerminalState(String streamName, String sessionId) {
        DahuaDoorBaseHandler handler = streamHandlers.get(streamName);
        if (handler == null) {
            return false;
        }

        @Nullable
        String callState = handler.getSipCallStateForSession(sessionId);
        return SipClient.SipCallState.IDLE.name().equals(callState)
                || SipClient.SipCallState.HUNGUP.name().equals(callState)
                || SipClient.SipCallState.TERMINATING.name().equals(callState);
    }

    private void logSessionToDahuaClientMapping(String streamName, String sessionId,
            @Nullable String requestedClientId) {
        DahuaDoorBaseHandler handler = streamHandlers.get(streamName);
        if (handler == null) {
            return;
        }

        @Nullable
        String dahuaClientId = handler.getAssignedClientForSession(sessionId);
        String safeClientId = dahuaClientId != null ? dahuaClientId : "unassigned";
        String safeRequestedClientId = requestedClientId != null ? requestedClientId : "";
        String mappingKey = buildStreamClientKey(streamName, sessionId);
        String mappingValue = safeClientId + "|" + safeRequestedClientId;
        String previous = lastLoggedSessionClientMapping.put(mappingKey, mappingValue);

        if (!mappingValue.equals(previous)) {
            LOGGER.info("WebRTC session mapping: stream='{}' sessionId='{}' dahuaClientId='{}' requestedClientId='{}'",
                    streamName, sessionId, safeClientId, safeRequestedClientId);
        }
    }

    private enum OfferGatePhase {
        CONNECTING,
        ACTIVE
    }

    private static final class OfferGateState {
        private final OfferGatePhase phase;
        private final long lastUpdateMs;

        private OfferGateState(OfferGatePhase phase, long lastUpdateMs) {
            this.phase = phase;
            this.lastUpdateMs = lastUpdateMs;
        }
    }

    private static String summarizeAudioSdpDirection(String sdp) {
        boolean hasAudioMedia = false;
        boolean inAudioSection = false;
        String mediaDirection = "unspecified";
        String sessionDirection = "unspecified";
        boolean audioHasMsid = false;
        boolean audioHasSsrc = false;
        String audioCodecs = "";

        String[] lines = sdp.split("\\r?\\n");
        for (String line : lines) {
            if (line.startsWith("m=")) {
                inAudioSection = line.startsWith("m=audio ");
                if (inAudioSection) {
                    hasAudioMedia = true;
                    mediaDirection = "unspecified";
                    audioCodecs = "";
                }
                continue;
            }

            if ("a=sendrecv".equals(line) || "a=sendonly".equals(line) || "a=recvonly".equals(line)
                    || "a=inactive".equals(line)) {
                String direction = line.substring(2);
                if (inAudioSection) {
                    mediaDirection = direction;
                } else {
                    sessionDirection = direction;
                }
            } else if (inAudioSection && line.startsWith("a=msid:")) {
                audioHasMsid = true;
            } else if (inAudioSection && line.startsWith("a=ssrc:")) {
                audioHasSsrc = true;
            } else if (inAudioSection && line.startsWith("a=rtpmap:")) {
                if (!audioCodecs.isEmpty()) {
                    audioCodecs += ",";
                }
                int space = line.indexOf(' ');
                audioCodecs += (space > 0) ? line.substring(space + 1) : line.substring("a=rtpmap:".length());
            }
        }

        String effective = !"unspecified".equals(mediaDirection) ? mediaDirection : sessionDirection;
        return hasAudioMedia
                ? ("media=" + mediaDirection + ", session=" + sessionDirection + ", effective=" + effective + ", msid="
                        + audioHasMsid + ", ssrc=" + audioHasSsrc + ", codecs=[" + audioCodecs + "]")
                : "no-audio-media";
    }

    private static String extractAudioSdpLines(String sdp) {
        StringBuilder linesSummary = new StringBuilder();
        boolean inAudioSection = false;
        for (String rawLine : sdp.split("\\r?\\n")) {
            String line = rawLine.trim();
            if (line.startsWith("m=")) {
                inAudioSection = line.startsWith("m=audio ");
                if (inAudioSection) {
                    appendSdpLine(linesSummary, line);
                }
                continue;
            }
            if (!inAudioSection) {
                continue;
            }
            if (line.startsWith("a=rtpmap:") || "a=sendrecv".equals(line) || "a=sendonly".equals(line)
                    || "a=recvonly".equals(line) || "a=inactive".equals(line) || line.startsWith("a=fmtp:")) {
                appendSdpLine(linesSummary, line);
            }
        }
        return linesSummary.length() == 0 ? "[]" : '[' + linesSummary.toString() + ']';
    }

    private static void appendSdpLine(StringBuilder linesSummary, String line) {
        if (linesSummary.length() > 0) {
            linesSummary.append(" | ");
        }
        linesSummary.append(line);
    }

    private static String firstNonBlank(@Nullable String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private static String abbreviate(@Nullable String value, int maxLen) {
        if (value == null || value.isBlank()) {
            return "-";
        }
        if (value.length() <= maxLen) {
            return value;
        }
        return value.substring(0, Math.max(0, maxLen - 3)) + "...";
    }

    private static long elapsedMillis(long startedAtNanos) {
        return (System.nanoTime() - startedAtNanos) / 1_000_000L;
    }

    private static byte[] readLimitedBytes(InputStream in, int maxBytes) throws IOException, RequestTooLargeException {
        byte[] buffer = new byte[4096];
        int total = 0;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int read;
        while ((read = in.read(buffer)) != -1) {
            total += read;
            if (total > maxBytes) {
                throw new RequestTooLargeException();
            }
            out.write(buffer, 0, read);
        }
        return out.toByteArray();
    }

    private static final class RequestTooLargeException extends Exception {
        private static final long serialVersionUID = 1L;
    }
}
