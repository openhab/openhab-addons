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
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.io.mcp.internal.McpCloudWebhookService;
import org.openhab.io.mcp.internal.tools.McpToolUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Serves the MCP server's OAuth 2.1 discovery documents, unauthenticated so clients can
 * read them before they have a token. Each document is published twice: under
 * {@code /mcp} so it routes through the webhook, and at the RFC 8414 / RFC 9728 location
 * off the server root.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class OAuthMetadataServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final String TOKEN_PROXY_PATH = OAuthTokenProxyServlet.PATH;
    private static final String MCP_LOCAL_PATH = "/mcp";
    /** Bundle id the webhook connector stamps on requests it forwards. */
    private static final String WEBHOOK_SOURCE = "org.openhab.io.openhabcloud";
    public static final String PATH_PROTECTED_RESOURCE = MCP_LOCAL_PATH + "/.well-known/oauth-protected-resource";
    public static final String PATH_AUTH_SERVER = MCP_LOCAL_PATH + "/.well-known/oauth-authorization-server";
    /** OIDC-flavoured alias of the AS metadata — same doc, different URL. */
    public static final String PATH_AUTH_SERVER_OIDC = MCP_LOCAL_PATH + "/.well-known/openid-configuration";
    /** RFC 9728 location — the well-known segment sits between host and path. */
    public static final String PATH_ROOT_PROTECTED_RESOURCE = "/.well-known/oauth-protected-resource" + MCP_LOCAL_PATH;
    /** RFC 8414 location. */
    public static final String PATH_ROOT_AUTH_SERVER = "/.well-known/oauth-authorization-server" + MCP_LOCAL_PATH;
    /** OIDC-flavoured alias of {@link #PATH_ROOT_AUTH_SERVER}. */
    public static final String PATH_ROOT_AUTH_SERVER_OIDC = "/.well-known/openid-configuration" + MCP_LOCAL_PATH;

    private final Logger logger = LoggerFactory.getLogger(OAuthMetadataServlet.class);
    private final ObjectMapper jackson = McpToolUtils.jackson();
    private final @Nullable McpCloudWebhookService cloudWebhook;

    public OAuthMetadataServlet(@Nullable McpCloudWebhookService cloudWebhook) {
        this.cloudWebhook = cloudWebhook;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String path = request.getRequestURI();
        UrlContext ctx = resolveUrlContext(request);

        Map<String, Object> body;
        if (PATH_PROTECTED_RESOURCE.equals(path) || PATH_ROOT_PROTECTED_RESOURCE.equals(path)) {
            body = protectedResourceMetadata(ctx);
        } else if (PATH_AUTH_SERVER.equals(path) || PATH_AUTH_SERVER_OIDC.equals(path)
                || PATH_ROOT_AUTH_SERVER.equals(path) || PATH_ROOT_AUTH_SERVER_OIDC.equals(path)) {
            body = authorizationServerMetadata(ctx);
            if (ctx.authorizationEndpoint == null) {
                logger.debug("OAuth AS metadata served with null authorization_endpoint — "
                        + "Cloud /api/v1/proxyurl never returned a browserUrl. "
                        + "Clients requiring OAuth will reject this document.");
            }
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        logger.debug("OAuth metadata GET {} -> {}", path, body);

        response.setContentType("application/json");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader("Cache-Control", "public, max-age=3600");
        try (PrintWriter writer = response.getWriter()) {
            jackson.writeValue(writer, body);
        }
    }

    private Map<String, Object> protectedResourceMetadata(UrlContext ctx) {
        Map<String, Object> doc = new LinkedHashMap<>();
        doc.put("resource", ctx.resource);
        doc.put("authorization_servers", List.of(ctx.issuer));
        doc.put("bearer_methods_supported", List.of("header"));
        doc.put("scopes_supported", List.of("mcp"));
        doc.put("resource_documentation", "https://www.openhab.org/addons/integrations/mcp");
        return doc;
    }

    private Map<String, Object> authorizationServerMetadata(UrlContext ctx) {
        Map<String, Object> doc = new LinkedHashMap<>();
        doc.put("issuer", ctx.issuer);
        String authEndpoint = ctx.authorizationEndpoint;
        if (authEndpoint != null) {
            doc.put("authorization_endpoint", authEndpoint);
        }
        String tokenEndpoint = ctx.tokenEndpoint;
        if (tokenEndpoint != null) {
            doc.put("token_endpoint", tokenEndpoint);
        }
        String regEndpoint = ctx.registrationEndpoint;
        if (regEndpoint != null) {
            doc.put("registration_endpoint", regEndpoint);
        }
        doc.put("response_types_supported", List.of("code"));
        doc.put("grant_types_supported", List.of("authorization_code", "refresh_token"));
        doc.put("code_challenge_methods_supported", List.of("S256"));
        doc.put("token_endpoint_auth_methods_supported", List.of("none"));
        doc.put("scopes_supported", List.of("mcp"));
        return doc;
    }

    UrlContext resolveUrlContext(HttpServletRequest request) {
        return resolveUrlContext(request, cloudWebhook);
    }

    /**
     * Per-request URL context. Clients reject metadata whose {@code resource} doesn't
     * match the URL they requested, so we advertise the route the request came in on:
     * webhook URLs for forwarded requests, request-derived URLs for everything else.
     */
    static UrlContext resolveUrlContext(HttpServletRequest request, @Nullable McpCloudWebhookService hook) {
        if (hook != null) {
            String mcpHookUrl = hook.getPublicUrl();
            if (mcpHookUrl != null && isWebhook(request)) {
                String browserBase = hook.deriveBrowserBaseUrl();
                // The webhook prepends /mcp when forwarding, so advertise sub-paths
                // without that prefix.
                return new UrlContext(mcpHookUrl, mcpHookUrl,
                        browserBase != null ? browserBase + "/auth/authorize" : null,
                        mcpHookUrl + stripMcpPrefix(TOKEN_PROXY_PATH),
                        mcpHookUrl + stripMcpPrefix(OAuthRegisterServlet.PATH));
            }
        }
        String base = resolveExternalBase(request);
        return new UrlContext(base + MCP_LOCAL_PATH, base + MCP_LOCAL_PATH, base + "/auth/authorize",
                base + TOKEN_PROXY_PATH, base + OAuthRegisterServlet.PATH);
    }

    /**
     * Whether this request was forwarded by the webhook rather than sent to us directly.
     * The connector stamps its bundle id on requests it forwards, but a client could send
     * that header too, so we only trust it from loopback — the connector always forwards
     * to localhost. Host can't be used to tell the two apart because a self-hosted cloud
     * may share a hostname with the direct route.
     */
    static boolean isWebhook(HttpServletRequest request) {
        if (!isLoopback(request.getRemoteAddr())) {
            return false;
        }
        String source = headerOrNull(request, "x-openhab-source");
        return source != null && source.contains(WEBHOOK_SOURCE);
    }

    private static boolean isLoopback(@Nullable String remoteAddr) {
        if (remoteAddr == null || remoteAddr.isBlank()) {
            return false;
        }
        try {
            return InetAddress.getByName(remoteAddr).isLoopbackAddress();
        } catch (UnknownHostException e) {
            return false;
        }
    }

    /**
     * Absolute URL of this request's protected-resource metadata, for the RFC 9728
     * {@code WWW-Authenticate} challenge. Must be absolute — strict clients discard a
     * challenge carrying a bare path.
     */
    public static String protectedResourceMetadataUrl(HttpServletRequest request,
            @Nullable McpCloudWebhookService hook) {
        if (hook != null) {
            String mcpHookUrl = hook.getPublicUrl();
            if (mcpHookUrl != null && isWebhook(request)) {
                return mcpHookUrl + stripMcpPrefix(PATH_PROTECTED_RESOURCE);
            }
        }
        return resolveExternalBase(request) + PATH_PROTECTED_RESOURCE;
    }

    private static String stripMcpPrefix(String localPath) {
        return localPath.startsWith(MCP_LOCAL_PATH) ? localPath.substring(MCP_LOCAL_PATH.length()) : localPath;
    }

    /**
     * Reconstructs the externally-visible base URL for this request. Trusts
     * {@code X-Forwarded-*} headers only when the forwarded host matches the host the
     * servlet container actually bound ({@link HttpServletRequest#getServerName()}).
     * This blocks Host-header injection attacks that would otherwise let an attacker
     * poison the metadata response with URLs pointing at an attacker-controlled host.
     */
    static String resolveExternalBase(HttpServletRequest request) {
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();

        String proto = headerOrNull(request, "X-Forwarded-Proto");
        if (proto == null || (!"http".equalsIgnoreCase(proto) && !"https".equalsIgnoreCase(proto))) {
            proto = request.getScheme();
        }

        String host = headerOrNull(request, "X-Forwarded-Host");
        if (host == null) {
            host = headerOrNull(request, "Host");
        }
        if (host != null && !hostMatches(host, serverName)) {
            host = null;
        }
        if (host == null) {
            host = (serverPort == 80 || serverPort == 443) ? serverName : serverName + ":" + serverPort;
        }
        return proto + "://" + host;
    }

    private static @Nullable String headerOrNull(HttpServletRequest request, String name) {
        String v = request.getHeader(name);
        return v == null || v.isBlank() ? null : v;
    }

    private static boolean hostMatches(String headerHost, String serverName) {
        int colon = headerHost.indexOf(':');
        String hostOnly = colon >= 0 ? headerHost.substring(0, colon) : headerHost;
        return hostOnly.equalsIgnoreCase(serverName);
    }

    record UrlContext(String resource, String issuer, @Nullable String authorizationEndpoint,
            @Nullable String tokenEndpoint, @Nullable String registrationEndpoint) {
    }
}
