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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLDecoder;
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
 * @author Sven Schad - Initial contribution
 */
@NonNullByDefault
public class PlayStreamServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(PlayStreamServlet.class);

    /** Timeout for the HTTP call to the go2rtc API. */
    private static final int GO2RTC_API_TIMEOUT_MS = 10_000;

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
        streamApiPorts.put(streamName, apiPort);
        if (registrationCount == 0) {
            activate();
        }
        registrationCount++;
        LOGGER.debug("Registered WebRTC stream '{}' on port {}", streamName, apiPort);
    }

    /**
     * De-registers a stream and – on the last de-registration – deactivates the servlet.
     *
     * @param streamName go2rtc stream name
     */
    public synchronized void unregisterStream(String streamName) {
        streamApiPorts.remove(streamName);
        registrationCount = Math.max(0, registrationCount - 1);
        if (registrationCount == 0) {
            deactivate();
        }
        LOGGER.debug("Unregistered WebRTC stream '{}'", streamName);
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
            requestBody = new String(in.readAllBytes(), StandardCharsets.UTF_8);
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

        // Forward to go2rtc API
        String go2rtcUrl = "http://127.0.0.1:" + apiPort + "/api/webrtc?src=" + streamName;

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

            byte[] sdpAnswer = conn.getInputStream().readAllBytes();

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

    private void activate() {
        Dictionary<String, String> params = new Hashtable<>();
        try {
            httpService.registerServlet(DahuaDoorBindingConstants.WEBRTC_SERVLET_PATH, this, params,
                    httpService.createDefaultHttpContext());
            LOGGER.debug("PlayStreamServlet registered at {}", DahuaDoorBindingConstants.WEBRTC_SERVLET_PATH);
        } catch (ServletException | NamespaceException e) {
            LOGGER.error("Failed to register PlayStreamServlet: {}", e.getMessage(), e);
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
        resp.setHeader("Access-Control-Allow-Origin", "*");
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
}
