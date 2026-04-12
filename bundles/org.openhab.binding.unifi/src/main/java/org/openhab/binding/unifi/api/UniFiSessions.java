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
import java.util.concurrent.Executor;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.unifi.internal.api.UniFiAuthenticator;
import org.openhab.binding.unifi.internal.api.UniFiRequestThrottler;
import org.openhab.binding.unifi.internal.api.UniFiSessionImpl;

/**
 * Factory for creating {@link UniFiSession} instances that share the same auth / CSRF / throttle / persistence
 * machinery as the parent {@code unifi:controller} bridge.
 * <p>
 * Child bindings that need to talk directly to a UniFi console (for example the UniFi Protect legacy
 * {@code unifiprotect:nvr} bridge when not yet migrated to {@code unifi:controller}) obtain a session via this
 * factory instead of rolling their own login. The returned session uses the same 7-req/s throttle as the parent,
 * persists its auth cookie + CSRF token to {@code $OH_USERDATA/cache/unifi/}, and transparently re-authenticates
 * on 401.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class UniFiSessions {

    private UniFiSessions() {
    }

    /**
     * Create a new {@link UniFiSession} and perform the initial login against the console.
     *
     * @param httpClient the Jetty HTTP client to use (must already be started)
     * @param executor executor used for async auth work
     * @param baseUrl the console base URL, for example {@code https://192.168.1.1:443}
     * @param username local console user
     * @param password local console password
     * @param unifios {@code true} for modern UniFiOS consoles ({@code /api/auth/login}), {@code false} for legacy
     *            controllers ({@code /api/login})
     * @param enableSessionPersistence whether to persist the session cookie to disk so the binding does not
     *            re-login after an openHAB restart
     * @return a future that completes with the authenticated session once login succeeds
     */
    public static CompletableFuture<UniFiSession> create(HttpClient httpClient, Executor executor, String baseUrl,
            String username, String password, boolean unifios, boolean enableSessionPersistence) {
        UniFiAuthenticator authenticator = new UniFiAuthenticator(httpClient, executor, baseUrl, username, password,
                unifios, enableSessionPersistence);
        UniFiRequestThrottler throttler = new UniFiRequestThrottler();
        UniFiSessionImpl session = new UniFiSessionImpl(baseUrl, authenticator, throttler);
        return authenticator.authenticate().thenApply(ignored -> session);
    }
}
