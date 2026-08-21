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
package org.openhab.binding.solaredge.internal.oauth;

import static org.openhab.binding.solaredge.internal.SolarEdgeBindingConstants.PUBLIC_DATA_API_V2_AUTHORIZE_URL;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.solaredge.internal.handler.SolarEdgeGenericHandler;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.http.whiteboard.HttpWhiteboardConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Receives SolarEdge OAuth callbacks and dispatches them to the corresponding Thing handler.
 *
 * @author Ronny Grun - Initial contribution
 */
@Component(service = { Servlet.class,
        SolarEdgeOAuthServlet.class }, property = HttpWhiteboardConstants.HTTP_WHITEBOARD_SERVLET_PATTERN + "="
                + SolarEdgeOAuthServlet.SERVLET_ALIAS)
@NonNullByDefault
public class SolarEdgeOAuthServlet extends HttpServlet {
    public static final String SERVLET_ALIAS = "/solaredge/oauth/callback";
    private static final long serialVersionUID = 1L;

    private final Logger logger = LoggerFactory.getLogger(SolarEdgeOAuthServlet.class);
    private final Map<String, SolarEdgeGenericHandler> pendingAuthorizations = new ConcurrentHashMap<>();

    @Deactivate
    protected void deactivate() {
        pendingAuthorizations.clear();
    }

    public String register(SolarEdgeGenericHandler handler, String clientId) {
        pendingAuthorizations.values().removeIf(candidate -> candidate.equals(handler));
        String externalId = UUID.randomUUID().toString();
        pendingAuthorizations.put(externalId, handler);
        return PUBLIC_DATA_API_V2_AUTHORIZE_URL + "?client_id=" + encode(clientId) + "&external_id="
                + encode(externalId);
    }

    public void unregister(SolarEdgeGenericHandler handler) {
        pendingAuthorizations.values().removeIf(candidate -> candidate.equals(handler));
    }

    @Override
    protected void doGet(@Nullable HttpServletRequest request, @Nullable HttpServletResponse response)
            throws IOException {
        if (request == null || response == null) {
            return;
        }
        String error = request.getParameter("error");
        String code = request.getParameter("code");
        String siteId = request.getParameter("site_id");
        String externalId = request.getParameter("external_id");
        if (error != null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "SolarEdge authorization failed: " + error);
            return;
        }
        if (code == null || siteId == null || externalId == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Incomplete SolarEdge OAuth callback");
            return;
        }
        SolarEdgeGenericHandler handler = pendingAuthorizations.get(externalId);
        if (handler == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown or expired SolarEdge authorization");
            return;
        }
        try {
            handler.onOAuthAuthorized(code, siteId);
            pendingAuthorizations.remove(externalId, handler);
            response.setContentType("text/html; charset=UTF-8");
            try (PrintWriter writer = response.getWriter()) {
                writer.println("<h1>SolarEdge authorization successful</h1>");
                writer.println("<p>You can close this window and return to openHAB.</p>");
            }
        } catch (SolarEdgeOAuthException e) {
            logger.warn("SolarEdge OAuth callback failed for {}: {}", handler.getThing().getUID(), e.getMessage());
            response.sendError(HttpServletResponse.SC_BAD_GATEWAY, e.getMessage());
        }
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
