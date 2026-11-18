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
import org.openhab.core.auth.Role;
import org.openhab.core.auth.UserApiTokenCredentials;
import org.openhab.core.auth.UserRegistry;
import org.openhab.io.mcp.internal.tools.McpToolUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Validates bearer tokens presented to the MCP server and checks whether the user is an admin.
 * Supports two token families:
 * <ul>
 * <li><b>{@code oh.*} API tokens</b> — validated directly via {@link UserRegistry}
 * (fast path, no HTTP round-trip).</li>
 * <li><b>openHAB-issued JWTs</b> — checked with an admin-only REST request to openHAB core.
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
    /** Admin-only. Returns 200 for an admin, 403 for other users, 401 for a bad token. */
    private static final String ADMIN_PROBE_PATH = "/rest/logging/";
    private static final long PROBE_TIMEOUT_SECONDS = 5;
    private static final int PROBE_FAILED = -1;

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
     * @return the user, or {@code null} if the token is missing or rejected
     */
    public @Nullable McpUser authenticate(HttpServletRequest request) {
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
                McpUser user = new McpUser(auth.getUsername(), auth.getRoles().contains(Role.ADMIN));
                logger.debug("Auth accept: {} {} — oh.* token {} (user={}, admin={})", request.getMethod(), path,
                        tokenShape, user.name(), user.admin());
                return user;
            } catch (AuthenticationException e) {
                logger.debug("Auth reject: {} {} — oh.* token {} rejected by UserRegistry: {}", request.getMethod(),
                        path, tokenShape, e.getMessage());
                return null;
            }
        }
        McpUser user = verifyViaRest(token);
        logger.debug("Auth {}: {} {} — JWT {} via REST probe (admin={})", user != null ? "accept" : "reject",
                request.getMethod(), path, tokenShape, user != null && user.admin());
        return user;
    }

    /** The username isn't known on this path, so it's left empty. */
    private @Nullable McpUser verifyViaRest(String token) {
        return switch (probe(token)) {
            case HttpStatus.OK_200 -> new McpUser("", true);
            case HttpStatus.FORBIDDEN_403 -> new McpUser("", false);
            default -> null;
        };
    }

    /**
     * @return the HTTP status, or {@link #PROBE_FAILED} if the request failed
     */
    private int probe(String token) {
        try {
            ContentResponse resp = httpClient.newRequest(coreBaseUrl + ADMIN_PROBE_PATH).method(HttpMethod.GET)
                    .header(AUTH_HEADER, BEARER_PREFIX + token).timeout(PROBE_TIMEOUT_SECONDS, TimeUnit.SECONDS).send();
            int status = resp.getStatus();
            if (status != HttpStatus.OK_200) {
                logger.debug("REST probe to {}{} returned {} {} (body prefix: {})", coreBaseUrl, ADMIN_PROBE_PATH,
                        status, resp.getReason(), truncate(resp.getContentAsString(), 200));
            }
            return status;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.debug("REST probe interrupted for JWT validation");
            return PROBE_FAILED;
        } catch (TimeoutException | ExecutionException | RuntimeException e) {
            logger.debug("REST probe failed for JWT validation: {}", e.getMessage(), e);
            return PROBE_FAILED;
        }
    }

    private static String truncate(String s, int max) {
        return McpToolUtils.truncate(s, max);
    }

    public static @Nullable String extractToken(HttpServletRequest request) {
        String auth = request.getHeader(AUTH_HEADER);
        String token = null;
        if (auth != null && auth.startsWith(BEARER_PREFIX)) {
            token = auth.substring(BEARER_PREFIX.length()).trim();
        } else {
            String alt = request.getHeader(AUTH_ALT_HEADER);
            if (alt != null) {
                token = alt.trim();
            }
        }
        // Core treats a token with spaces as no token at all, so the request would pass as anonymous.
        return token != null && !token.isEmpty() && token.chars().noneMatch(Character::isWhitespace) ? token : null;
    }
}
