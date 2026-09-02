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

import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.api.Request;

/**
 * Default {@link UniFiSession} implementation used by {@code UniFiControllerBridgeHandler}. Wraps a
 * {@link UniFiAuthenticator} and a {@link UniFiRequestThrottler}: the throttler is applied inside
 * {@link #addAuthHeaders(Request)} so that every caller (Network, Protect, Access) obeys the same rate limit
 * against a shared console, and the authenticator supplies the Cookie + CSRF headers.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class UniFiSessionImpl implements UniFiSession {

    private final String baseUrl;
    private final UniFiAuthenticator authenticator;
    private final UniFiRequestThrottler throttler;
    private final Object reauthLock = new Object();
    private @Nullable CompletableFuture<Void> reauthInFlight;

    public UniFiSessionImpl(String baseUrl, UniFiAuthenticator authenticator, UniFiRequestThrottler throttler) {
        this.baseUrl = baseUrl;
        this.authenticator = authenticator;
        this.throttler = throttler;
    }

    @Override
    public String getBaseUrl() {
        return baseUrl;
    }

    @Override
    public void addAuthHeaders(Request request) {
        try {
            throttler.acquire();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        authenticator.addAuthHeaders(request);
    }

    @Override
    public @Nullable String getAuthCookie() {
        return authenticator.getAuthCookie();
    }

    @Override
    public @Nullable String getCsrfToken() {
        return authenticator.getCsrfToken();
    }

    @Override
    public CompletableFuture<Void> reauthenticate() {
        // Don't clear credentials first: a failed/throttled login must leave the still-valid cookie in place.
        // Coalesce concurrent callers (Network/Protect/Access share this session).
        synchronized (reauthLock) {
            CompletableFuture<Void> existing = reauthInFlight;
            if (existing != null && !existing.isDone()) {
                return existing;
            }
            CompletableFuture<Void> future = authenticator.authenticate();
            reauthInFlight = future;
            return future;
        }
    }

    @Override
    public void updateCsrfToken(String newToken) {
        authenticator.updateCsrfToken(newToken);
    }
}
