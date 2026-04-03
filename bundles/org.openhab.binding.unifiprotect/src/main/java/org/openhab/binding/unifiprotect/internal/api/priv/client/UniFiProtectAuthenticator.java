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
package org.openhab.binding.unifiprotect.internal.api.priv.client;

import java.net.HttpCookie;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.unifiprotect.internal.api.priv.dto.gson.JsonUtil;
import org.openhab.binding.unifiprotect.internal.api.priv.dto.system.LoginResponse;
import org.openhab.binding.unifiprotect.internal.api.priv.exception.AuthenticationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

/**
 * Handles authentication with UniFi Protect
 * Manages login, session cookies, CSRF tokens, and automatic re-authentication
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class UniFiProtectAuthenticator {

    private final Logger logger = LoggerFactory.getLogger(UniFiProtectAuthenticator.class);
    private static final String AUTH_PATH = "/api/auth/login";
    private static final Duration SESSION_EXPIRY = Duration.ofHours(24);
    private static final long REQUEST_TIMEOUT_SECONDS = 30;
    private static final String COOKIE_TOKEN = "TOKEN";
    private static final String COOKIE_UOS_TOKEN = "UOS_TOKEN";

    private final HttpClient httpClient;
    private final Executor executor;
    private final String baseUrl;
    private final String username;
    private final String password;
    private @Nullable SessionPersistence sessionPersistence;

    private volatile @Nullable String authCookie;
    private volatile @Nullable String csrfToken;
    private volatile @Nullable String userId;

    public UniFiProtectAuthenticator(HttpClient httpClient, Executor executor, String baseUrl, String username,
            String password, boolean enableSessionPersistence) {
        this.httpClient = httpClient;
        this.executor = executor;
        this.baseUrl = baseUrl;
        this.username = username;
        this.password = password;

        SessionPersistence tempPersistence = null;
        if (enableSessionPersistence) {
            try {
                tempPersistence = new SessionPersistence(baseUrl, username);
                SessionPersistence.SessionData sessionData = tempPersistence.load();
                if (sessionData != null) {
                    this.authCookie = sessionData.cookie;
                    this.csrfToken = sessionData.csrfToken;
                    logger.debug("Loaded saved session for {}", username);
                    // Fetch user ID since we didn't login
                    fetchUserIdFromSelf();
                }
            } catch (Exception e) {
                logger.debug("Failed to initialize or load session persistence: {}", e.getMessage(), e);
                tempPersistence = null;
            }
        }
        this.sessionPersistence = tempPersistence;
    }

    /**
     * Authenticate with UniFi Protect
     * Returns CompletableFuture that completes when authenticated
     */
    public CompletableFuture<Void> authenticate() {
        return CompletableFuture.runAsync(() -> {
            try {
                logger.debug("Authenticating with UniFi Protect as {}", username);

                // Create login request body
                JsonObject loginData = new JsonObject();
                loginData.addProperty("username", username);
                loginData.addProperty("password", password);
                loginData.addProperty("rememberMe", sessionPersistence != null);

                String url = baseUrl + AUTH_PATH;
                Request request = httpClient.newRequest(url).method(HttpMethod.POST)
                        .header("Content-Type", "application/json")
                        .content(new StringContentProvider(loginData.toString()))
                        .timeout(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);

                ContentResponse response = request.send();

                if (response.getStatus() != HttpStatus.OK_200) {
                    throw new AuthenticationException(
                            "Authentication failed: " + response.getStatus() + " " + response.getReason());
                }

                String responseBody = response.getContentAsString();
                logger.debug("Login response body: {}", responseBody);
                try {
                    LoginResponse loginResponse = JsonUtil.getGson().fromJson(responseBody, LoginResponse.class);
                    if (loginResponse != null && loginResponse.id != null) {
                        this.userId = loginResponse.id;
                        logger.debug("Got user ID: {}", userId);
                    } else {
                        logger.debug("Login response did not contain user ID");
                    }
                } catch (Exception e) {
                    logger.debug("Failed to parse login response for user ID", e);
                }

                String csrfHeader = response.getHeaders().get("x-csrf-token");
                if (csrfHeader != null) {
                    this.csrfToken = csrfHeader;
                    logger.debug("Got CSRF token: {}", csrfToken);
                }

                List<String> setCookieHeaders = response.getHeaders().getValuesList("Set-Cookie");
                if (setCookieHeaders != null && !setCookieHeaders.isEmpty()) {
                    for (String setCookieHeader : setCookieHeaders) {
                        parseCookie(setCookieHeader);
                    }
                    logger.debug("Got auth cookie(s) from {} Set-Cookie header(s)", setCookieHeaders.size());
                }

                if (authCookie == null) {
                    throw new AuthenticationException("No authentication cookie received");
                }

                logger.debug("Successfully authenticated as {} (user ID: {})", username, userId);

                if (sessionPersistence != null) {
                    try {
                        SessionPersistence.SessionData sessionData = new SessionPersistence.SessionData(authCookie,
                                csrfToken, Instant.now().plus(SESSION_EXPIRY));
                        sessionPersistence.save(sessionData);
                    } catch (Exception e) {
                        logger.debug("Failed to save session to disk: {}", e.getMessage(), e);
                    }
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new AuthenticationException("Authentication interrupted", e);
            } catch (TimeoutException | ExecutionException e) {
                throw new AuthenticationException("Authentication failed", e);
            } catch (AuthenticationException e) {
                throw e;
            }
        }, executor);
    }

    /**
     * Parse cookie from Set-Cookie header
     */
    private void parseCookie(String setCookieHeader) {
        try {
            List<HttpCookie> cookies = HttpCookie.parse(setCookieHeader);
            for (HttpCookie cookie : cookies) {
                if (COOKIE_TOKEN.equals(cookie.getName()) || COOKIE_UOS_TOKEN.equals(cookie.getName())) {
                    this.authCookie = cookie.getName() + "=" + cookie.getValue();
                    logger.debug("Parsed cookie: {}", cookie.getName());
                    break;
                }
            }
        } catch (Exception e) {
            logger.debug("Failed to parse cookie: {}", setCookieHeader, e);
        }
    }

    /**
     * Fetch user ID from /api/users/self
     * Used when loading a saved session (no login response to parse)
     */
    private void fetchUserIdFromSelf() {
        try {
            String url = baseUrl + "/api/users/self";
            Request request = httpClient.newRequest(url).method(HttpMethod.GET).timeout(REQUEST_TIMEOUT_SECONDS,
                    TimeUnit.SECONDS);

            addAuthHeaders(request);

            ContentResponse response = request.send();

            if (response.getStatus() == HttpStatus.OK_200) {
                String responseBody = response.getContentAsString();
                logger.debug("/users/self response: {}", responseBody);

                LoginResponse userInfo = JsonUtil.getGson().fromJson(responseBody, LoginResponse.class);
                if (userInfo != null && userInfo.id != null) {
                    this.userId = userInfo.id;
                    logger.debug("Fetched user ID from /users/self: {}", userId);
                } else {
                    logger.debug("/users/self response did not contain user ID");
                }
            } else {
                logger.debug("Failed to fetch user info from /users/self: {} {}", response.getStatus(),
                        response.getReason());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.debug("Interrupted while fetching user ID from /users/self", e);
        } catch (TimeoutException | ExecutionException e) {
            logger.debug("Error fetching user ID from /users/self", e);
        }
    }

    /**
     * Add authentication headers to a request
     */
    public void addAuthHeaders(Request request) {
        if (authCookie != null) {
            request.header("Cookie", authCookie);
        }
        if (csrfToken != null) {
            request.header("x-csrf-token", csrfToken);
        }
    }

    /**
     * Check if we're authenticated
     */
    public boolean isAuthenticated() {
        return authCookie != null && csrfToken != null;
    }

    /**
     * Get auth cookie for WebSocket
     */
    public @Nullable String getAuthCookie() {
        return authCookie;
    }

    /**
     * Get CSRF token
     */
    public @Nullable String getCsrfToken() {
        return csrfToken;
    }

    /**
     * Get authenticated user ID
     * Fetches from /users/self if not available
     */
    public @Nullable String getUserId() {
        if (userId == null && isAuthenticated()) {
            // Lazy fetch if we don't have it yet
            logger.debug("User ID not available, fetching from /users/self");
            fetchUserIdFromSelf();
        }
        return userId;
    }

    /**
     * Clear authentication (logout)
     */
    public void clearAuth() {
        this.authCookie = null;
        this.csrfToken = null;
        this.userId = null;

        if (sessionPersistence != null) {
            try {
                sessionPersistence.delete();
            } catch (Exception e) {
                logger.debug("Failed to delete saved session: {}", e.getMessage(), e);
            }
        }

        logger.debug("Cleared authentication");
    }
}
