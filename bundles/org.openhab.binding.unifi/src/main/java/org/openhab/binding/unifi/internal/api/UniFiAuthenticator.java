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
package org.openhab.binding.unifi.internal.api;

import java.net.HttpCookie;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
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
import org.openhab.binding.unifi.api.UniFiException;
import org.openhab.binding.unifi.api.UniFiException.AuthState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

/**
 * Handles authentication against a UniFi console. Supports both the modern UniFiOS endpoint
 * ({@code /api/auth/login}) and the legacy controller endpoint ({@code /api/login}) via the {@code unifios} flag.
 * Captures the session cookie (TOKEN/UOS_TOKEN) and CSRF token returned by login, persists them via
 * {@link SessionPersistence} when enabled, and applies them as headers on outbound Jetty {@link Request}s via
 * {@link #addAuthHeaders(Request)}.
 * <p>
 * Forked from the UniFi Protect binding's {@code UniFiProtectAuthenticator}; adapted to handle the unifios flag
 * and to throw the public {@link UniFiException} so child bindings only see one exception type.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class UniFiAuthenticator {

    private static final String UNIFIOS_AUTH_PATH = "/api/auth/login";
    private static final String LEGACY_AUTH_PATH = "/api/login";
    private static final Duration SESSION_EXPIRY = Duration.ofHours(24);
    private static final long REQUEST_TIMEOUT_SECONDS = 30;
    private static final String COOKIE_TOKEN = "TOKEN";
    private static final String COOKIE_UOS_TOKEN = "UOS_TOKEN";

    private final Logger logger = LoggerFactory.getLogger(UniFiAuthenticator.class);

    private final HttpClient httpClient;
    private final Executor executor;
    private final String baseUrl;
    private final String username;
    private final String password;
    private final boolean unifios;
    private final @Nullable SessionPersistence sessionPersistence;

    private volatile @Nullable String authCookie;
    private volatile @Nullable String csrfToken;

    public UniFiAuthenticator(HttpClient httpClient, Executor executor, String baseUrl, String username,
            String password, boolean unifios, boolean enableSessionPersistence) {
        this.httpClient = httpClient;
        this.executor = executor;
        this.baseUrl = baseUrl;
        this.username = username;
        this.password = password;
        this.unifios = unifios;

        SessionPersistence tempPersistence = null;
        if (enableSessionPersistence) {
            try {
                tempPersistence = new SessionPersistence(baseUrl, username);
                SessionPersistence.SessionData sessionData = tempPersistence.load();
                if (sessionData != null) {
                    this.authCookie = sessionData.cookie;
                    this.csrfToken = sessionData.csrfToken;
                    logger.debug("Loaded saved session for {}", username);
                }
            } catch (Exception e) {
                logger.debug("Failed to initialize or load session persistence: {}", e.getMessage(), e);
                tempPersistence = null;
            }
        }
        this.sessionPersistence = tempPersistence;
    }

    /**
     * Perform a fresh login against the console. Completes when the session cookie and CSRF token have been
     * captured from the response.
     */
    public CompletableFuture<Void> authenticate() {
        return CompletableFuture.runAsync(() -> {
            try {
                doAuthenticate();
            } catch (UniFiException e) {
                throw new CompletionException(e);
            }
        }, executor);
    }

    private void doAuthenticate() throws UniFiException {
        try {
            logger.debug("Authenticating with UniFi console as {}", username);

            JsonObject loginBody = new JsonObject();
            loginBody.addProperty("username", username);
            loginBody.addProperty("password", password);
            loginBody.addProperty("rememberMe", sessionPersistence != null);

            String url = baseUrl + (unifios ? UNIFIOS_AUTH_PATH : LEGACY_AUTH_PATH);
            Request request = httpClient.newRequest(url).method(HttpMethod.POST)
                    .header("Content-Type", "application/json").content(new StringContentProvider(loginBody.toString()))
                    .timeout(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            ContentResponse response = request.send();

            int status = response.getStatus();
            if (status == HttpStatus.UNAUTHORIZED_401) {
                throw new UniFiException("Authentication rejected by console (HTTP " + status + ")",
                        AuthState.REJECTED);
            }
            if (status == HttpStatus.FORBIDDEN_403 || status == HttpStatus.TOO_MANY_REQUESTS_429) {
                throw new UniFiException("Authentication throttled by console (HTTP " + status + ")",
                        AuthState.THROTTLED);
            }
            if (status != HttpStatus.OK_200) {
                throw new UniFiException("Authentication failed: " + status + " " + response.getReason());
            }

            String csrfHeader = response.getHeaders().get("x-csrf-token");
            if (csrfHeader != null) {
                this.csrfToken = csrfHeader;
                logger.debug("Got CSRF token from login response");
            }

            List<String> setCookieHeaders = response.getHeaders().getValuesList("Set-Cookie");
            if (setCookieHeaders != null && !setCookieHeaders.isEmpty()) {
                for (String setCookieHeader : setCookieHeaders) {
                    parseCookie(setCookieHeader);
                }
                logger.debug("Got auth cookie(s) from {} Set-Cookie header(s)", setCookieHeaders.size());
            }

            if (authCookie == null) {
                throw new UniFiException("No authentication cookie received from console");
            }

            logger.debug("Successfully authenticated as {}", username);

            SessionPersistence persistence = sessionPersistence;
            if (persistence != null) {
                try {
                    SessionPersistence.SessionData sessionData = new SessionPersistence.SessionData(authCookie,
                            csrfToken, Instant.now().plus(SESSION_EXPIRY));
                    persistence.save(sessionData);
                } catch (Exception e) {
                    logger.debug("Failed to save session to disk: {}", e.getMessage(), e);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new UniFiException("Authentication interrupted", e);
        } catch (TimeoutException | ExecutionException e) {
            throw new UniFiException("Authentication failed", e);
        }
    }

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
     * Applies the session cookie and CSRF token as headers on the given Jetty request.
     */
    public void addAuthHeaders(Request request) {
        String cookie = authCookie;
        if (cookie != null) {
            request.header("Cookie", cookie);
        }
        String csrf = csrfToken;
        if (csrf != null) {
            request.header("x-csrf-token", csrf);
        }
    }

    public boolean isAuthenticated() {
        return authCookie != null;
    }

    public @Nullable String getAuthCookie() {
        return authCookie;
    }

    public @Nullable String getCsrfToken() {
        return csrfToken;
    }

    /**
     * Update the CSRF token. Called by the session/request wrapper whenever the console rotates the token via the
     * {@code X-CSRF-Token} response header.
     */
    public void updateCsrfToken(String newToken) {
        this.csrfToken = newToken;
    }

    public void clearAuth() {
        this.authCookie = null;
        this.csrfToken = null;

        SessionPersistence persistence = sessionPersistence;
        if (persistence != null) {
            try {
                persistence.delete();
            } catch (Exception e) {
                logger.debug("Failed to delete saved session: {}", e.getMessage(), e);
            }
        }

        logger.debug("Cleared authentication");
    }
}
