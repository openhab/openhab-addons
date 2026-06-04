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

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link SubscriptionManager}.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
class SubscriptionManagerTest {

    private @Nullable SubscriptionManager manager;

    @BeforeEach
    void setUp() {
        manager = new SubscriptionManager();
    }

    private SubscriptionManager mgr() {
        SubscriptionManager m = manager;
        assertNotNull(m);
        return m;
    }

    @Test
    void testWatchAddsItems() {
        List<String> added = mgr().watch("s1", List.of("Item1", "Item2"));
        assertEquals(List.of("Item1", "Item2"), added);
    }

    @Test
    void testWatchIdempotent() {
        mgr().watch("s1", List.of("Item1"));
        List<String> added = mgr().watch("s1", List.of("Item1"));
        assertTrue(added.isEmpty());
    }

    @Test
    void testWatchedReturnsSet() {
        mgr().watch("s1", List.of("Item1", "Item2"));
        Set<String> watched = mgr().watched("s1");
        assertEquals(Set.of("Item1", "Item2"), watched);
    }

    @Test
    void testWatchedUnknownSession() {
        assertTrue(mgr().watched("unknown").isEmpty());
    }

    @Test
    void testUnwatchRemovesItems() {
        mgr().watch("s1", List.of("Item1", "Item2"));
        List<String> removed = mgr().unwatch("s1", List.of("Item1"));
        assertEquals(List.of("Item1"), removed);
        assertEquals(Set.of("Item2"), mgr().watched("s1"));
    }

    @Test
    void testUnwatchUnknownSession() {
        assertTrue(mgr().unwatch("unknown", List.of("Item1")).isEmpty());
    }

    @Test
    void testRecordBuffersEvent() {
        mgr().watch("s1", List.of("Item1"));
        mgr().recordIfWatched("Item1", "ON", "OFF", Instant.now());
        List<SubscriptionManager.ChangeEvent> events = mgr().drainEvents("s1");
        assertEquals(1, events.size());
        assertEquals("Item1", events.get(0).itemName());
        assertEquals("ON", events.get(0).state());
        assertEquals("OFF", events.get(0).oldState());
    }

    @Test
    void testRecordIgnoresUnwatched() {
        mgr().watch("s1", List.of("Item1"));
        mgr().recordIfWatched("Item2", "ON", "OFF", Instant.now());
        assertTrue(mgr().drainEvents("s1").isEmpty());
    }

    @Test
    void testDrainReturnsAndClears() {
        mgr().watch("s1", List.of("Item1"));
        mgr().recordIfWatched("Item1", "ON", "OFF", Instant.now());
        assertEquals(1, mgr().drainEvents("s1").size());
        assertTrue(mgr().drainEvents("s1").isEmpty());
    }

    @Test
    void testDrainUnknownSession() {
        assertTrue(mgr().drainEvents("unknown").isEmpty());
    }

    @Test
    void testFifoEvictionAt200() {
        mgr().watch("s1", List.of("Item1"));
        for (int i = 0; i < 210; i++) {
            mgr().recordIfWatched("Item1", String.valueOf(i), String.valueOf(i - 1), Instant.now());
        }
        List<SubscriptionManager.ChangeEvent> events = mgr().drainEvents("s1");
        assertEquals(200, events.size());
        assertEquals("10", events.get(0).state());
    }

    @Test
    void testMultipleSessionsIndependent() {
        mgr().watch("s1", List.of("Item1"));
        mgr().watch("s2", List.of("Item2"));
        mgr().recordIfWatched("Item1", "ON", "OFF", Instant.now());
        assertEquals(1, mgr().drainEvents("s1").size());
        assertTrue(mgr().drainEvents("s2").isEmpty());
    }

    @Test
    void testOnSessionClosedCleansUp() {
        mgr().watch("s1", List.of("Item1"));
        mgr().recordIfWatched("Item1", "ON", "OFF", Instant.now());
        mgr().onSessionClosed("s1");
        assertTrue(mgr().watched("s1").isEmpty());
        assertTrue(mgr().drainEvents("s1").isEmpty());
    }
}
