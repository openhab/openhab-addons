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
package org.openhab.io.mcp.internal.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.io.mcp.internal.tools.McpToolUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Minimal OAuth 2.0 Dynamic Client Registration (RFC 7591) endpoint. openHAB core's
 * authorization flow treats {@code client_id == redirect_uri} as the implicit
 * registration model, so there's no real per-client state to store — we just echo
 * the caller's redirect URI back as the assigned {@code client_id}. That's enough
 * to satisfy MCP clients (Claude Desktop, ChatGPT's Connectors, mcp-remote, …) that
 * require a DCR endpoint in the AS metadata before they'll attempt the
 * authorization-code flow.
 * <p>
 * Lives under {@code /mcp/*} so it proxies cleanly via the openHAB Cloud webhook.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class OAuthRegisterServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    public static final String PATH = "/mcp/oauth/register";

    private final Logger logger = LoggerFactory.getLogger(OAuthRegisterServlet.class);

    private final ObjectMapper jackson = McpToolUtils.jackson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String contentType = request.getContentType();
        logger.debug("OAuth DCR: POST {} Content-Type={}", request.getRequestURI(), contentType);
        if (contentType == null || !contentType.contains("application/json")) {
            writeError(response, HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE, "invalid_client_metadata",
                    "Content-Type must be application/json");
            return;
        }
        JsonNode body;
        try {
            body = jackson.readTree(request.getInputStream());
        } catch (Exception e) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, "invalid_client_metadata",
                    "Request body is not valid JSON: " + e.getMessage());
            return;
        }

        JsonNode redirectUrisNode = body.get("redirect_uris");
        if (redirectUrisNode == null || !redirectUrisNode.isArray() || redirectUrisNode.isEmpty()) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, "invalid_redirect_uri",
                    "'redirect_uris' array is required with at least one entry");
            return;
        }

        List<String> redirectUris = new ArrayList<>();
        for (JsonNode node : redirectUrisNode) {
            if (node.isTextual()) {
                String uriStr = node.asText();
                try {
                    URI uri = URI.create(uriStr);
                    String scheme = uri.getScheme();
                    if (scheme == null || (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme))) {
                        writeError(response, HttpServletResponse.SC_BAD_REQUEST, "invalid_redirect_uri",
                                "redirect_uri must use http or https scheme: " + uriStr);
                        return;
                    }
                    redirectUris.add(uriStr);
                } catch (IllegalArgumentException e) {
                    writeError(response, HttpServletResponse.SC_BAD_REQUEST, "invalid_redirect_uri",
                            "Malformed redirect_uri: " + uriStr);
                    return;
                }
            }
        }
        if (redirectUris.isEmpty()) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, "invalid_redirect_uri",
                    "'redirect_uris' contains no valid URIs");
            return;
        }

        // openHAB core's AuthorizePageServlet requires client_id == redirect_uri, so
        // we assign client_id = first redirect_uri. All registered URIs are echoed
        // back so the client can pick whichever one fits the request.
        String clientId = redirectUris.get(0);

        Map<String, Object> doc = new LinkedHashMap<>();
        doc.put("client_id", clientId);
        doc.put("redirect_uris", redirectUris);
        doc.put("token_endpoint_auth_method", "none");
        doc.put("grant_types", List.of("authorization_code", "refresh_token"));
        doc.put("response_types", List.of("code"));
        if (body.hasNonNull("client_name")) {
            String name = body.get("client_name").asText();
            if (name.length() > 256) {
                name = name.substring(0, 256);
            }
            doc.put("client_name", name);
        }
        if (body.hasNonNull("scope")) {
            doc.put("scope", body.get("scope").asText());
        } else {
            doc.put("scope", "mcp");
        }

        logger.debug("OAuth DCR: issued client_id={} for redirect_uris={}", clientId, redirectUris);

        response.setStatus(HttpServletResponse.SC_CREATED);
        response.setContentType("application/json");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        try (PrintWriter writer = response.getWriter()) {
            jackson.writeValue(writer, doc);
        }
    }

    private void writeError(HttpServletResponse response, int status, String errorCode, String description)
            throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        Map<String, Object> err = new LinkedHashMap<>();
        err.put("error", errorCode);
        err.put("error_description", description);
        try (PrintWriter writer = response.getWriter()) {
            jackson.writeValue(writer, err);
        }
    }
}
