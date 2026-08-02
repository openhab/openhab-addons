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
package org.openhab.binding.rachio.internal.api;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Tests atomic webhook event claiming.
 *
 * @author Kovacs Istvan - Initial contribution
 */
@NonNullByDefault
public class RachioWebhookDuplicateEventCacheTest {

    @Test
    public void concurrentClaimsHaveSingleWinner() throws Exception {
        RachioWebhookDuplicateEventCache cache = new RachioWebhookDuplicateEventCache();
        ExecutorService executor = Executors.newFixedThreadPool(16);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<Boolean>> claims = new ArrayList<>();
        try {
            for (int i = 0; i < 32; i++) {
                claims.add(executor.submit(() -> {
                    start.await();
                    return cache.claim("event-1");
                }));
            }
            start.countDown();

            int winners = 0;
            for (Future<Boolean> claim : claims) {
                if (claim.get()) {
                    winners++;
                }
            }
            assertEquals(1, winners);
            assertEquals(1, cache.size());
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    public void failedClaimCanBeRetried() {
        RachioWebhookDuplicateEventCache cache = new RachioWebhookDuplicateEventCache();

        assertTrue(cache.claim("event-1"));
        assertFalse(cache.claim("event-1"));
        cache.release("event-1");
        assertTrue(cache.claim("event-1"));
    }

    @Test
    public void expiredClaimCanBeReclaimed() {
        AtomicLong clock = new AtomicLong(1000);
        RachioWebhookDuplicateEventCache cache = new RachioWebhookDuplicateEventCache(100, 10, clock::get);

        assertTrue(cache.claim("event-1"));
        clock.addAndGet(101);
        assertTrue(cache.claim("event-1"));
    }
}
