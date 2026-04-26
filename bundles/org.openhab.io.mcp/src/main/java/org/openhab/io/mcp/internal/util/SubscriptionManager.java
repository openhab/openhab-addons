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
package org.openhab.io.mcp.internal.util;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Tracks per-MCP-session subscriptions + buffered events. Exposed to MCP tools
 * (see {@code WatchTools}) so an LLM that doesn't use the MCP Resources API can
 * still subscribe to item changes via tool calls and poll for events.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class SubscriptionManager {

    /** Max buffered events per session (older events dropped). */
    private static final int MAX_BUFFERED_EVENTS = 200;

    public record ChangeEvent(String itemName, String state, String oldState, Instant timestamp) {
    }

    private static class SessionState {
        final Set<String> watchedItems = Collections.synchronizedSet(new LinkedHashSet<>());
        final Deque<ChangeEvent> events = new ArrayDeque<>();
    }

    private final Map<String, SessionState> bySession = new ConcurrentHashMap<>();

    /** Subscribe a session to a set of item names. Returns newly-subscribed items. */
    public List<String> watch(String sessionId, List<String> itemNames) {
        SessionState s = bySession.computeIfAbsent(sessionId, k -> new SessionState());
        List<String> added = new ArrayList<>();
        for (String name : itemNames) {
            if (s.watchedItems.add(name)) {
                added.add(name);
            }
        }
        return added;
    }

    public List<String> unwatch(String sessionId, List<String> itemNames) {
        SessionState s = bySession.get(sessionId);
        if (s == null) {
            return List.of();
        }
        List<String> removed = new ArrayList<>();
        for (String name : itemNames) {
            if (s.watchedItems.remove(name)) {
                removed.add(name);
            }
        }
        return removed;
    }

    public Set<String> watched(String sessionId) {
        SessionState s = bySession.get(sessionId);
        return s == null ? Set.of() : Set.copyOf(s.watchedItems);
    }

    /**
     * Called by the event bridge when an item changes state. For every session
     * that's watching this item, append to its buffered event log.
     */
    public void recordIfWatched(String itemName, String state, String oldState, Instant timestamp) {
        for (Map.Entry<String, SessionState> e : bySession.entrySet()) {
            SessionState s = e.getValue();
            if (!s.watchedItems.contains(itemName)) {
                continue;
            }
            synchronized (s.events) {
                s.events.addLast(new ChangeEvent(itemName, state, oldState, timestamp));
                while (s.events.size() > MAX_BUFFERED_EVENTS) {
                    s.events.removeFirst();
                }
            }
        }
    }

    /**
     * Drain and return all events buffered for this session (since the last call).
     * The buffer is cleared; a fresh call returns an empty list until new events arrive.
     */
    public List<ChangeEvent> drainEvents(String sessionId) {
        SessionState s = bySession.get(sessionId);
        if (s == null) {
            return List.of();
        }
        List<ChangeEvent> out;
        synchronized (s.events) {
            out = new ArrayList<>(s.events);
            s.events.clear();
        }
        return out;
    }

    /** Forget all state for a session (call on session disconnect). */
    public void onSessionClosed(String sessionId) {
        bySession.remove(sessionId);
    }
}
