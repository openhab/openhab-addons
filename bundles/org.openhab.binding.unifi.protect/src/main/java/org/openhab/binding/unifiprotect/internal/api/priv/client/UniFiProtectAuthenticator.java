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
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.unifi.api.UniFiSession;
import org.openhab.binding.unifiprotect.internal.api.priv.dto.gson.JsonUtil;
import org.openhab.binding.unifiprotect.internal.api.priv.dto.system.LoginResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Thin adapter that exposes the subset of session operations needed by {@link UniFiProtectPrivateClient} and the
 * Protect private-API WebSocket over an already-authenticated {@link UniFiSession}.
 * <p>
 * Since Phase F the Protect NVR bridge runs as a child of the parent {@code unifi:controller} bridge, so the
 * session is always supplied at construction time by the NVR handler (which pulls it from the parent bridge
 * handler). This class no longer creates its own session or logs in — it just delegates
 * {@code addAuthHeaders(Request)}, {@code getAuthCookie()}, and {@code getCsrfToken()} to the shared session,
 * and still owns the "fetch user id from {@code /api/users/self}" helper used by the auto-token-provisioning
 * flow.
 *
 * @author Dan Cunningham - Initial contribution (original implementation)
 * @author Dan Cunningham - Refactored onto parent-owned UniFiSession
 */
@NonNullByDefault
public class UniFiProtectAuthenticator {

    private static final long REQUEST_TIMEOUT_SECONDS = 30;

    private final Logger logger = LoggerFactory.getLogger(UniFiProtectAuthenticator.class);

    private final HttpClient httpClient;
    private final Executor executor;
    private final String baseUrl;
    private final UniFiSession session;

    private volatile @Nullable String userId;

    public UniFiProtectAuthenticator(HttpClient httpClient, Executor executor, String baseUrl, UniFiSession session) {
        this.httpClient = httpClient;
        this.executor = executor;
        this.baseUrl = baseUrl;
        this.session = session;
    }

    /**
     * Pre-warms the authenticated user id from {@code /api/users/self}. The NVR handler calls this to make
     * {@link #getUserId()} available when auto-provisioning a Protect public-API token via
     * {@link UniFiProtectPrivateClient#getOrCreateApiKey(String, String)}.
     *
     * @return a future that completes once the user id has been fetched (or failed to fetch — the future still
     *         completes normally; call {@link #getUserId()} afterwards to retrieve the cached value).
     */
    public CompletableFuture<Void> authenticate() {
        return CompletableFuture.runAsync(this::fetchUserIdFromSelf, executor);
    }

    private void fetchUserIdFromSelf() {
        try {
            String url = baseUrl + "/api/users/self";
            Request request = httpClient.newRequest(url).method(HttpMethod.GET).timeout(REQUEST_TIMEOUT_SECONDS,
                    TimeUnit.SECONDS);
            session.addAuthHeaders(request);

            ContentResponse response = request.send();

            if (response.getStatus() == HttpStatus.OK_200) {
                String responseBody = response.getContentAsString();
                logger.trace("/users/self response: {}", responseBody);

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
     * Add authentication headers to a request by delegating to the shared {@link UniFiSession}.
     */
    public void addAuthHeaders(Request request) {
        session.addAuthHeaders(request);
    }

    public boolean isAuthenticated() {
        return session.getAuthCookie() != null;
    }

    public @Nullable String getAuthCookie() {
        return session.getAuthCookie();
    }

    public @Nullable String getCsrfToken() {
        return session.getCsrfToken();
    }

    /**
     * Get authenticated user ID. Fetches from {@code /api/users/self} if not already cached.
     */
    public @Nullable String getUserId() {
        if (userId == null) {
            logger.debug("User ID not available, fetching from /users/self");
            fetchUserIdFromSelf();
        }
        return userId;
    }

    /**
     * Clear the cached user id. The parent bridge handler owns the session's lifecycle, so this does NOT
     * invalidate the session itself.
     */
    public void clearAuth() {
        this.userId = null;
        logger.debug("Cleared cached user id");
    }
}
