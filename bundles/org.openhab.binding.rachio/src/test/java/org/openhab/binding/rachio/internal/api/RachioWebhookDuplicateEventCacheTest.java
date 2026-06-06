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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Tests Rachio webhook duplicate event cache behavior.
 *
 * @author openHAB Contributors - Initial contribution
 */
@NonNullByDefault
class RachioWebhookDuplicateEventCacheTest {
    @Test
    void eventIdIsProcessedOnlyAfterMarkingAndWhileRetained() {
        AtomicLong clockMillis = new AtomicLong(1_000);
        RachioWebhookDuplicateEventCache cache = new RachioWebhookDuplicateEventCache(1_000, 2048, clockMillis::get);

        assertThat(cache.isProcessed("event-1"), is(false));
        assertThat(cache.isProcessed("event-1"), is(false));

        cache.markProcessed("event-1");
        assertThat(cache.isProcessed("event-1"), is(true));

        clockMillis.addAndGet(1_001);
        assertThat(cache.isProcessed("event-1"), is(false));
    }

    @Test
    void blankEventIdsAreNotCached() {
        RachioWebhookDuplicateEventCache cache = new RachioWebhookDuplicateEventCache();

        cache.markProcessed("");
        cache.markProcessed("  ");

        assertThat(cache.isProcessed(""), is(false));
        assertThat(cache.isProcessed("  "), is(false));
        assertThat(cache.size(), is(0));
    }

    @Test
    void cacheIsBounded() {
        AtomicLong clockMillis = new AtomicLong(1_000);
        RachioWebhookDuplicateEventCache cache = new RachioWebhookDuplicateEventCache(60_000, 2, () -> {
            return clockMillis.getAndIncrement();
        });

        cache.markProcessed("event-1");
        cache.markProcessed("event-2");
        cache.markProcessed("event-3");

        assertThat(cache.size(), is(2));
    }
}
