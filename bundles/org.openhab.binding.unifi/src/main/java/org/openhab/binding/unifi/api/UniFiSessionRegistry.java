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

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Process-wide registry of authenticated {@link UniFiSession} instances, keyed by {@code (host, username)}.
 * <p>
 * When the shared {@code unifi:controller} bridge in the parent binding finishes logging in, it
 * {@link #register(String, String, UniFiSession) registers} its session here. The Network, Protect, and Access
 * child bindings then call {@link #lookup(String, String)} from their own API clients before creating a fresh
 * session via
 * {@link UniFiSessions#create(org.eclipse.jetty.client.HttpClient, java.util.concurrent.Executor, String, String, String, boolean, boolean)}.
 * If a matching session already exists — which is the typical case on a UniFi OS console (UDM/UDR/UNVR) where
 * one local user is shared across Network, Protect, and Access — the child binding reuses it and no extra
 * {@code POST /api/auth/login} is performed.
 * <p>
 * Ownership semantics: a session returned from {@link #lookup(String, String)} is <em>not owned</em> by the
 * caller. The owning {@code unifi:controller} bridge handler is responsible for the session's lifecycle and
 * re-authentication. Callers may read from it, stamp auth headers on outgoing requests, and trigger
 * re-authentication; they must not invalidate or replace it.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class UniFiSessionRegistry {

    private static final UniFiSessionRegistry INSTANCE = new UniFiSessionRegistry();

    private final Map<String, UniFiSession> sessions = new ConcurrentHashMap<>();

    private UniFiSessionRegistry() {
    }

    public static UniFiSessionRegistry getInstance() {
        return INSTANCE;
    }

    /**
     * Register a freshly-authenticated session. Called by the parent {@code unifi:controller} bridge handler
     * after its initial login completes. Replaces any prior registration for the same {@code (host, username)}.
     */
    public void register(String host, String username, UniFiSession session) {
        sessions.put(key(host, username), session);
    }

    /**
     * Unregister the session for the given {@code (host, username)}. Called by the parent bridge handler on
     * dispose so child bindings fall back to creating their own session if the controller bridge goes away.
     */
    public void unregister(String host, String username) {
        sessions.remove(key(host, username));
    }

    /**
     * Look up a registered session for the given {@code (host, username)}. Returns {@code null} if no
     * {@code unifi:controller} bridge is currently online for this console/user combination, in which case the
     * caller should fall back to creating its own session.
     */
    @Nullable
    public UniFiSession lookup(String host, String username) {
        return sessions.get(key(host, username));
    }

    private static String key(String host, String username) {
        return host.toLowerCase(Locale.ROOT) + "|" + username;
    }
}
