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
package org.openhab.binding.unifiprotect.internal.media;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * WebRTCServlet acts as a SDP proxy for go2rtc.
 * Supports both raw SDP and base64 encoded SDP types
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class PlayStreamServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final UnifiMediaService mediaService;
    private HttpClient httpClient;
    private final Logger logger = LoggerFactory.getLogger(PlayStreamServlet.class);

    public PlayStreamServlet(UnifiMediaService mediaService, HttpClient httpClient) {
        this.httpClient = httpClient;
        this.mediaService = mediaService;
    }

    @Override
    protected void doOptions(@Nullable HttpServletRequest req, @Nullable HttpServletResponse resp) {
        if (req == null || resp == null) {
            return;
        }
        resp.setStatus(204);
    }

    @Override
    protected void doGet(@Nullable HttpServletRequest req, @Nullable HttpServletResponse resp) throws IOException {
        if (req == null || resp == null) {
            return;
        }
        resp.setStatus(200);
        resp.getOutputStream().write("Only POST is supported".getBytes());
        resp.getOutputStream().flush();
    }

    @Override
    protected void doPost(@Nullable HttpServletRequest req, @Nullable HttpServletResponse resp) throws IOException {
        if (req == null || resp == null) {
            return;
        }
        String pathInfo = req.getPathInfo();
        if (pathInfo == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        String[] parts = pathInfo.split("\\/");
        if (parts.length < 2) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        final String streamId = parts[1].trim();
        final String encodedId = URLEncoder.encode(streamId, StandardCharsets.UTF_8);
        String go2rtcBase = mediaService.getGo2RtcBaseForStream(streamId);
        if (go2rtcBase == null) {
            logger.debug("Unknown stream: {}", streamId);
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Unknown stream");
            return;
        }
        // Read body
        String base64 = req.getParameter("data");
        String sdpOffer;
        boolean isFormBase64 = base64 != null;
        if (isFormBase64) {
            String decoded = new String(Base64.getDecoder().decode(base64), StandardCharsets.UTF_8);
            sdpOffer = decoded;
        } else {
            sdpOffer = new String(req.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        }
        logger.trace("SDP Offer: {}", sdpOffer);
        // Forward to go2rtc as raw SDP
        final URI target = URI.create(go2rtcBase + "/api/webrtc?src=" + encodedId);
        logger.debug("Starting go2rtc stream: {}", target);
        Request reqJetty = httpClient.newRequest(target).header("Content-Type", "application/sdp");
        reqJetty.method("POST");
        reqJetty.content(new StringContentProvider("application/sdp", sdpOffer, StandardCharsets.UTF_8));

        try {
            ContentResponse r = reqJetty.send();
            int sc = r.getStatus();
            logger.trace("Go2rtc response: {}", sc);
            // Treat 200 and 201 as success (go2rtc may use 201)
            if (sc == 200 || sc == 201) {
                String answerSdp = r.getContentAsString();
                logger.trace("Go2rtc answer SDP: {}", answerSdp);
                resp.setCharacterEncoding("UTF-8");
                if (isFormBase64) {
                    resp.setContentType("text/plain; charset=UTF-8");
                    String b64 = Base64.getEncoder().encodeToString(answerSdp.getBytes(StandardCharsets.UTF_8));
                    resp.setStatus(200);
                    resp.getWriter().write(b64);
                } else {
                    resp.setContentType("application/sdp; charset=UTF-8");
                    resp.setStatus(200);
                    resp.getWriter().write(answerSdp);
                }
            } else {
                // bad response
                resp.setStatus(sc);
                resp.setCharacterEncoding("UTF-8");
                resp.setContentType("text/plain; charset=UTF-8");
                resp.getOutputStream().write(r.getContent());
            }
        } catch (Exception e) {
            logger.debug("HTTP proxy failed", e);
            resp.sendError(500, "HTTP proxy failed");
        }
    }
}
