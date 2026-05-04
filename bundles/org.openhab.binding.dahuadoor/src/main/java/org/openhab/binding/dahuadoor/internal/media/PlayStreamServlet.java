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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.dahuadoor.internal.DahuaDoorBindingConstants;
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

    private final HttpService httpService;

    /**
     * Map from go2rtc stream name (e.g. {@code dahua_vto2202_living}) to API port.
     * Modified from multiple threads → ConcurrentHashMap.
     */
    private final Map<String, Integer> streamApiPorts = new ConcurrentHashMap<>();

    /** Number of currently registered streams; used to decide when to unregister the servlet. */
    private volatile int registrationCount = 0;

    public PlayStreamServlet(HttpService httpService) {
        this.httpService = httpService;
    }

    // -------------------------------------------------------------------------
    // Stream registration / de-registration
    // -------------------------------------------------------------------------

    /**
     * Registers a stream and – on the first registration – activates the servlet.
     *
     * @param streamName go2rtc stream name
     * @param apiPort go2rtc API port for that stream
     */
    public synchronized void registerStream(String streamName, int apiPort) {
        Integer previous = streamApiPorts.put(streamName, apiPort);
        if (previous == null && registrationCount == 0) {
            if (!activate()) {
                streamApiPorts.remove(streamName);
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
        if (removed != null) {
            registrationCount = Math.max(0, registrationCount - 1);
            if (registrationCount == 0) {
                deactivate();
            }
        }
        LOGGER.debug("Unregistered WebRTC stream '{}'", streamName);
    }

    public synchronized void deactivateAll() {
        streamApiPorts.clear();
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

        Integer apiPort = streamApiPorts.get(streamName);
        if (apiPort == null) {
            sendBase64Message(resp, HttpServletResponse.SC_NOT_FOUND,
                    "Unknown stream: " + streamName + ". Is the thing online with WebRTC enabled?");
            LOGGER.warn("Rejected request: unknown stream '{}'", streamName);
            return;
        }

        String requestBody;
        try (InputStream in = req.getInputStream()) {
            requestBody = new String(readLimitedBytes(in, MAX_REQUEST_BODY_BYTES), StandardCharsets.UTF_8);
        } catch (RequestTooLargeException e) {
            sendBase64Message(resp, HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE,
                    "Request body too large. Maximum supported size is " + MAX_REQUEST_BODY_BYTES + " bytes.");
            LOGGER.warn("Rejected request for stream '{}': request body exceeded {} bytes", streamName,
                    MAX_REQUEST_BODY_BYTES);
            return;
        }
        if (requestBody.isBlank()) {
            sendBase64Message(resp, HttpServletResponse.SC_BAD_REQUEST, "Empty request body");
            LOGGER.warn("Rejected request for stream '{}': empty body", streamName);
            return;
        }

        if (!requestBody.startsWith("data=")) {
            sendBase64Message(resp, HttpServletResponse.SC_BAD_REQUEST,
                    "Unsupported request format. Expected form body: data=<base64(sdp)>.");
            LOGGER.warn("Rejected request for stream '{}': unsupported input mode", streamName);
            return;
        }
        if (requestBody.indexOf('&') >= 0) {
            sendBase64Message(resp, HttpServletResponse.SC_BAD_REQUEST,
                    "Unsupported request format. Only one form field is allowed: data=<base64(sdp)>.");
            LOGGER.warn("Rejected request for stream '{}': additional form fields present", streamName);
            return;
        }

        String decodedFormValue = URLDecoder.decode(requestBody.substring(5), StandardCharsets.UTF_8);
        if (decodedFormValue.isBlank()) {
            sendBase64Message(resp, HttpServletResponse.SC_BAD_REQUEST, "Missing SDP payload in form field 'data'.");
            LOGGER.warn("Rejected request for stream '{}': empty data field", streamName);
            return;
        }

        String sdpOffer;
        try {
            sdpOffer = new String(Base64.getDecoder().decode(decodedFormValue), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            sendBase64Message(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid Base64 payload in form field 'data'.");
            LOGGER.warn("Rejected request for stream '{}': invalid base64 payload", streamName);
            return;
        }
        if (sdpOffer.isBlank()) {
            sendBase64Message(resp, HttpServletResponse.SC_BAD_REQUEST, "Decoded SDP offer is empty.");
            LOGGER.warn("Rejected request for stream '{}': decoded SDP is empty", streamName);
            return;
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("WebRTC offer for stream '{}' audio={}", streamName, summarizeAudioSdpDirection(sdpOffer));
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
                sendBase64Message(resp, HttpServletResponse.SC_BAD_GATEWAY, message);
                LOGGER.warn("Upstream error for stream '{}': mapped to 502 (status={}, bodyBytes={})", streamName,
                        status, errorBodyBytes.length);
                return;
            }

            byte[] sdpAnswer;
            try (InputStream in = conn.getInputStream()) {
                sdpAnswer = in.readAllBytes();
            }

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("WebRTC answer for stream '{}' audio={}", streamName,
                        summarizeAudioSdpDirection(new String(sdpAnswer, StandardCharsets.UTF_8)));
            }

            byte[] encoded = Base64.getEncoder().encode(sdpAnswer);

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentType("text/plain");
            resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
            resp.getOutputStream().write(encoded);
        } catch (IOException e) {
            sendBase64Message(resp, HttpServletResponse.SC_BAD_GATEWAY,
                    "Failed to reach go2rtc API: " + e.getMessage());
            LOGGER.warn("Upstream communication failure for stream '{}': {}", streamName, e.getMessage());
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

    private static void sendBase64Message(HttpServletResponse resp, int statusCode, String message) throws IOException {
        byte[] encoded = Base64.getEncoder().encode(message.getBytes(StandardCharsets.UTF_8));
        resp.setStatus(statusCode);
        resp.setContentType("text/plain");
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.getOutputStream().write(encoded);
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
