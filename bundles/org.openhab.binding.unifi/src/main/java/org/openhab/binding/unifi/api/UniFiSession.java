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
package org.openhab.binding.unifi.api;

import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.api.Request;

/**
 * An authenticated session against a UniFi console, shared across the UniFi Network, Protect, and Access child
 * bindings so all three families reuse a single {@code /api/auth/login} on a given console.
 * <p>
 * Obtained from a {@link org.openhab.binding.unifi.handler.UniFiControllerBridgeHandler} via
 * {@code getSessionAsync()}. Implementations are responsible for CSRF-token rotation, 401 re-authentication, and
 * session persistence.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public interface UniFiSession {

    /**
     * @return the base URL of the UniFi console, e.g. {@code https://unifi.example.com:443}
     */
    String getBaseUrl();

    /**
     * Applies the session's authentication headers (Cookie, x-csrf-token) to the given Jetty HTTP request.
     *
     * @param request the request about to be sent
     */
    void addAuthHeaders(Request request);

    /**
     * @return the raw authentication cookie value (e.g. {@code TOKEN=...}) for use by WebSocket upgrade requests,
     *         or {@code null} if the session is not currently authenticated
     */
    @Nullable
    String getAuthCookie();

    /**
     * @return the current CSRF token, or {@code null} if the session is not currently authenticated
     */
    @Nullable
    String getCsrfToken();

    /**
     * Trigger an explicit re-authentication. Useful when a child binding receives a 401 outside the normal HTTP
     * client path (for example, on a WebSocket upgrade failure).
     *
     * @return a future that completes when the session has been re-established
     */
    CompletableFuture<Void> reauthenticate();

    /**
     * Notify the session that the console rotated the CSRF token (typically found in the
     * {@code X-CSRF-Token} response header). Child bindings should call this from their request wrappers so
     * subsequent calls on any family binding see the new token.
     *
     * @param newToken the replacement CSRF token
     */
    void updateCsrfToken(String newToken);
}
