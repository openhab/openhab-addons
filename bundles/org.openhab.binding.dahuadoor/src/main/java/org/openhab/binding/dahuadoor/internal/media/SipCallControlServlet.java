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

import static org.openhab.binding.dahuadoor.internal.DahuaDoorBindingConstants.SIP_CONTROL_SERVLET_PATH;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Dictionary;
import java.util.Hashtable;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.dahuadoor.internal.DahuaDoorBaseHandler;
import org.openhab.binding.dahuadoor.internal.DahuaDoorHandlerFactory;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servlet for SIP call control endpoints.
 *
 * Endpoints:
 * - GET /dahuadoor/sip/assign-client?thing={thingUid}
 * - GET /dahuadoor/sip/state?thing={thingUid}
 * - POST /dahuadoor/sip/answer?thing={thingUid}
 * - POST /dahuadoor/sip/hangup?thing={thingUid}
 */
@NonNullByDefault
public class SipCallControlServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(SipCallControlServlet.class);

    private final HttpService httpService;
    private final DahuaDoorHandlerFactory handlerFactory;

    public SipCallControlServlet(HttpService httpService, DahuaDoorHandlerFactory handlerFactory) {
        this.httpService = httpService;
        this.handlerFactory = handlerFactory;
    }

    public void activate() {
        Dictionary<String, String> params = new Hashtable<>();
        try {
            httpService.registerServlet(SIP_CONTROL_SERVLET_PATH, this, params, httpService.createDefaultHttpContext());
            LOGGER.debug("SipCallControlServlet registered at {}", SIP_CONTROL_SERVLET_PATH);
        } catch (ServletException | NamespaceException e) {
            LOGGER.error("Failed to register SipCallControlServlet: {}", e.getMessage(), e);
        }
    }

    public void deactivate() {
        try {
            httpService.unregister(SIP_CONTROL_SERVLET_PATH);
            LOGGER.debug("SipCallControlServlet unregistered");
        } catch (IllegalArgumentException e) {
            LOGGER.trace("SipCallControlServlet was not registered: {}", e.getMessage());
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        addCorsHeaders(resp);

        String action = getAction(req);
        DahuaDoorBaseHandler handler = resolveHandler(req, resp);
        if (handler == null) {
            return;
        }

        String sessionId = req.getSession().getId();

        if ("assign-client".equals(action)) {
            String clientId = handler.assignClientForSession(sessionId);
            String callState = handler.getSipCallStateForSession(sessionId);
            writeJson(resp, HttpServletResponse.SC_OK,
                    "{\"clientId\":\"" + escapeJson(clientId) + "\",\"callState\":\"" + escapeJson(callState) + "\"}");
            return;
        }

        if ("state".equals(action)) {
            String clientId = handler.assignClientForSession(sessionId);
            String callState = handler.getSipCallStateForSession(sessionId);
            @Nullable
            String caller = handler.getSipCallerForSession(sessionId);
            String callerValue = caller != null ? caller : "";
            @Nullable
            SipBackchannelSession session = handler.getSipBackchannelSessionForSession(sessionId);

            StringBuilder json = new StringBuilder(384);
            json.append("{\"clientId\":\"").append(escapeJson(clientId)).append("\",\"callState\":\"")
                    .append(escapeJson(callState)).append("\",\"caller\":\"").append(escapeJson(callerValue))
                    .append("\"");
            if (session != null) {
                json.append(",\"session\":{\"sessionId\":\"").append(escapeJson(session.getSessionId()))
                        .append("\",\"clientId\":\"").append(escapeJson(session.getClientId()))
                        .append("\",\"thingUid\":\"").append(escapeJson(session.getThingUid()))
                        .append("\",\"callerId\":\"").append(escapeJson(session.getCallerId()))
                        .append("\",\"callState\":\"").append(escapeJson(session.getCallState())).append("\"");
                @Nullable
                String inviteSdp = session.getInviteSdp();
                if (inviteSdp != null) {
                    json.append(",\"inviteSdp\":\"").append(escapeJson(inviteSdp)).append("\"");
                }
                json.append(",\"createdAtMs\":").append(session.getCreatedAtMs()).append(",\"updatedAtMs\":")
                        .append(session.getUpdatedAtMs()).append("}");
            }
            json.append('}');

            writeJson(resp, HttpServletResponse.SC_OK, json.toString());
            return;
        }

        resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Unknown SIP control endpoint");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        addCorsHeaders(resp);

        String action = getAction(req);
        DahuaDoorBaseHandler handler = resolveHandler(req, resp);
        if (handler == null) {
            return;
        }

        String sessionId = req.getSession().getId();
        String clientId = handler.assignClientForSession(sessionId);

        if ("answer".equals(action)) {
            boolean success = handler.answerSipCallForSession(sessionId);
            String callState = handler.getSipCallStateForSession(sessionId);
            writeJson(resp, success ? HttpServletResponse.SC_OK : HttpServletResponse.SC_CONFLICT,
                    "{\"success\":" + success + ",\"clientId\":\"" + escapeJson(clientId) + "\",\"callState\":\""
                            + escapeJson(callState) + "\"}");
            return;
        }

        if ("hangup".equals(action)) {
            boolean success = handler.hangupSipCallForSession(sessionId);
            String callState = handler.getSipCallStateForSession(sessionId);
            writeJson(resp, success ? HttpServletResponse.SC_OK : HttpServletResponse.SC_CONFLICT,
                    "{\"success\":" + success + ",\"clientId\":\"" + escapeJson(clientId) + "\",\"callState\":\""
                            + escapeJson(callState) + "\"}");
            return;
        }

        resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Unknown SIP control endpoint");
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        addCorsHeaders(resp);
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    private static String getAction(HttpServletRequest req) {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.isBlank() || "/".equals(pathInfo)) {
            return "";
        }
        return pathInfo.startsWith("/") ? pathInfo.substring(1) : pathInfo;
    }

    private @Nullable DahuaDoorBaseHandler resolveHandler(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String thingUid = req.getParameter("thing");
        if (thingUid == null || thingUid.isBlank()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing thing parameter");
            return null;
        }

        try {
            new org.openhab.core.thing.ThingUID(thingUid);
        } catch (IllegalArgumentException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid thing UID format: " + thingUid);
            return null;
        }
        DahuaDoorBaseHandler handler = handlerFactory.getDahuaHandler(thingUid);
        if (handler == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Thing not found or not initialized: " + thingUid);
            return null;
        }
        return handler;
    }

    private static void writeJson(HttpServletResponse resp, int status, String body) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json");
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.getWriter().write(body);
    }

    private static String escapeJson(String text) {
        StringBuilder escaped = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            switch (ch) {
                case '\\':
                    escaped.append("\\\\");
                    break;
                case '"':
                    escaped.append("\\\"");
                    break;
                case '\b':
                    escaped.append("\\b");
                    break;
                case '\f':
                    escaped.append("\\f");
                    break;
                case '\n':
                    escaped.append("\\n");
                    break;
                case '\r':
                    escaped.append("\\r");
                    break;
                case '\t':
                    escaped.append("\\t");
                    break;
                default:
                    if (ch < 0x20) {
                        escaped.append(String.format("\\u%04x", (int) ch));
                    } else {
                        escaped.append(ch);
                    }
                    break;
            }
        }
        return escaped.toString();
    }

    private static void addCorsHeaders(HttpServletResponse resp) {
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
    }
}
