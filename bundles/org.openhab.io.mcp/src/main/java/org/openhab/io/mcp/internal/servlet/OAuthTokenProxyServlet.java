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
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.InputStreamContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.io.mcp.internal.tools.McpToolUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Proxies POSTs to {@code /mcp/oauth/token} onto openHAB core's local
 * {@code /auth/token} endpoint. This keeps the MCP server's Cloud-webhook surface
 * to a single {@code /mcp} registration: remote MCP clients POST the OAuth
 * authorization code exchange against a path under {@code /mcp/*} instead of a
 * second hook on {@code /auth/token}.
 * <p>
 * Forwards the request body (typically form-encoded with {@code grant_type},
 * {@code code}, {@code code_verifier}, etc.) unchanged, preserves the Content-Type,
 * and streams the response body back to the caller.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class OAuthTokenProxyServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    public static final String PATH = "/mcp/oauth/token";
    private static final long REQUEST_TIMEOUT_SECONDS = 30;
    private static final long MAX_BODY_SIZE = 8192;

    private final Logger logger = LoggerFactory.getLogger(OAuthTokenProxyServlet.class);

    private final HttpClient httpClient;
    private final String localTokenEndpoint;

    public OAuthTokenProxyServlet(HttpClient httpClient, String coreBaseUrl) {
        this.httpClient = httpClient;
        String base = coreBaseUrl.endsWith("/") ? coreBaseUrl.substring(0, coreBaseUrl.length() - 1) : coreBaseUrl;
        // openHAB core's OAuth token endpoint is JAX-RS-mounted at /rest/auth/token.
        // Note: /auth/token itself is caught by AuthorizePageServlet (registered on
        // the /auth/* pattern) and would treat the request as a login form POST.
        this.localTokenEndpoint = base + "/rest/auth/token";
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String contentType = request.getContentType();
        if (contentType == null || !contentType.startsWith("application/x-www-form-urlencoded")) {
            response.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE,
                    "Token endpoint requires application/x-www-form-urlencoded");
            return;
        }
        if (request.getContentLengthLong() > MAX_BODY_SIZE) {
            response.sendError(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE, "Request body too large");
            return;
        }
        logger.debug("OAuth token proxy: forwarding POST {} Content-Type={} → {}", request.getRequestURI(), contentType,
                localTokenEndpoint);
        try (InputStream in = request.getInputStream()) {
            Request proxy = httpClient.newRequest(URI.create(localTokenEndpoint)).method(HttpMethod.POST)
                    .timeout(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (contentType != null) {
                proxy.content(new InputStreamContentProvider(in), contentType);
            } else {
                proxy.content(new InputStreamContentProvider(in));
            }
            ContentResponse resp = proxy.send();
            int status = resp.getStatus();
            String bodyStr = resp.getContentAsString();
            if (status >= 200 && status < 300) {
                logger.debug("OAuth token proxy: upstream {} (body length {})", status,
                        bodyStr != null ? bodyStr.length() : 0);
            } else {
                logger.debug("OAuth token proxy: upstream {} {} body={}", status, resp.getReason(),
                        truncate(bodyStr, 300));
            }
            response.setStatus(status);
            String respContentType = resp.getMediaType();
            if (respContentType != null) {
                response.setContentType(respContentType);
            }
            String encoding = resp.getEncoding();
            response.setCharacterEncoding(encoding != null ? encoding : StandardCharsets.UTF_8.name());
            byte[] body = resp.getContent();
            if (body != null) {
                response.getOutputStream().write(body);
            }
        } catch (Exception e) {
            logger.debug("OAuth token proxy failed: {}", e.getMessage(), e);
            response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Token endpoint proxy failed");
        }
    }

    private static String truncate(String s, int max) {
        return McpToolUtils.truncate(s, max);
    }
}
