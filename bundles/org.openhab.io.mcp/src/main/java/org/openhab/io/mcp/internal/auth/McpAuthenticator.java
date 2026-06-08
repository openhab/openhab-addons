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
package org.openhab.io.mcp.internal.auth;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.core.auth.Authentication;
import org.openhab.core.auth.AuthenticationException;
import org.openhab.core.auth.UserApiTokenCredentials;
import org.openhab.core.auth.UserRegistry;
import org.openhab.io.mcp.internal.tools.McpToolUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Validates bearer tokens presented to the MCP server. Supports two token families:
 * <ul>
 * <li><b>{@code oh.*} API tokens</b> — validated directly via {@link UserRegistry}
 * (fast path, no HTTP round-trip).</li>
 * <li><b>openHAB-issued JWTs</b> — validated by proxying a cheap authenticated REST
 * request to openHAB core. A 200 means core's own auth stack accepted the token.
 * This keeps us a pure resource server and avoids linking against core's internal
 * {@code JwtHelper}.</li>
 * </ul>
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class McpAuthenticator {

    private static final String AUTH_HEADER = "Authorization";
    private static final String AUTH_ALT_HEADER = "X-OPENHAB-TOKEN";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String OH_TOKEN_PREFIX = "oh.";
    /**
     * Tiny auth-gated endpoint used to delegate bearer-token validation to openHAB
     * core. {@code /rest/} returns a small JSON root response ({@code locale},
     * {@code version}, {@code links}) — auth-protected, typically a few hundred
     * bytes, so it fits in Jetty's default 2 MB response buffer even on instances
     * with very large item registries.
     */
    private static final String PROBE_PATH = "/rest/";
    private static final long PROBE_TIMEOUT_SECONDS = 5;

    private final Logger logger = LoggerFactory.getLogger(McpAuthenticator.class);

    private final UserRegistry userRegistry;
    private final HttpClient httpClient;
    private final String coreBaseUrl;

    public McpAuthenticator(UserRegistry userRegistry, HttpClient httpClient, String coreBaseUrl) {
        this.userRegistry = userRegistry;
        this.httpClient = httpClient;
        this.coreBaseUrl = coreBaseUrl.endsWith("/") ? coreBaseUrl.substring(0, coreBaseUrl.length() - 1) : coreBaseUrl;
    }

    /**
     * Validates the bearer token on the incoming request and resolves the authenticated user when possible.
     *
     * @return the authenticated user's name on success (empty string when the token was accepted but no
     *         username could be resolved — currently the JWT path), or {@code null} on failure.
     */
    public @Nullable String authenticate(HttpServletRequest request) {
        String path = request.getRequestURI();
        String token = extractToken(request);
        if (token == null) {
            logger.debug("Auth reject: {} {} — no Authorization/X-OPENHAB-TOKEN header", request.getMethod(), path);
            return null;
        }
        String tokenShape = token.length() > 8 ? token.substring(0, 8) + "…(" + token.length() + ")" : "…";
        if (token.startsWith(OH_TOKEN_PREFIX)) {
            try {
                Authentication auth = userRegistry.authenticate(new UserApiTokenCredentials(token));
                String username = auth.getUsername();
                logger.debug("Auth accept: {} {} — oh.* token {} (user={})", request.getMethod(), path, tokenShape,
                        username);
                return username;
            } catch (AuthenticationException e) {
                logger.debug("Auth reject: {} {} — oh.* token {} rejected by UserRegistry: {}", request.getMethod(),
                        path, tokenShape, e.getMessage());
                return null;
            }
        }
        boolean ok = verifyViaRest(token);
        logger.debug("Auth {}: {} {} — JWT {} via REST probe", ok ? "accept" : "reject", request.getMethod(), path,
                tokenShape);
        // JWT path: core accepted the token but we don't have the username locally. Return empty string to
        // signal "authenticated, user unknown" so callers can distinguish from "rejected" (null).
        return ok ? "" : null;
    }

    /**
     * Proxies the bearer token to openHAB core's REST auth filter. A 200 response
     * means core accepted the token (whether it's a JWT or any other format core
     * supports now or in the future).
     */
    private boolean verifyViaRest(String token) {
        try {
            ContentResponse resp = httpClient.newRequest(coreBaseUrl + PROBE_PATH).method(HttpMethod.GET)
                    .header(AUTH_HEADER, BEARER_PREFIX + token).timeout(PROBE_TIMEOUT_SECONDS, TimeUnit.SECONDS).send();
            boolean ok = resp.getStatus() == HttpStatus.OK_200;
            if (!ok) {
                logger.debug("REST probe to {}{} returned {} {} (body prefix: {})", coreBaseUrl, PROBE_PATH,
                        resp.getStatus(), resp.getReason(), truncate(resp.getContentAsString(), 200));
            }
            return ok;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.debug("REST probe interrupted for JWT validation");
            return false;
        } catch (TimeoutException | ExecutionException | RuntimeException e) {
            logger.debug("REST probe failed for JWT validation: {}", e.getMessage(), e);
            return false;
        }
    }

    private static String truncate(String s, int max) {
        return McpToolUtils.truncate(s, max);
    }

    public static @Nullable String extractToken(HttpServletRequest request) {
        String auth = request.getHeader(AUTH_HEADER);
        if (auth != null && auth.startsWith(BEARER_PREFIX)) {
            return auth.substring(BEARER_PREFIX.length()).trim();
        }
        String alt = request.getHeader(AUTH_ALT_HEADER);
        if (alt != null && !alt.isBlank()) {
            return alt.trim();
        }
        return null;
    }
}
