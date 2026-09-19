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
package org.openhab.binding.rachio.internal.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Tests rate-limit window ordering and constructor contracts.
 *
 * @author Kovacs Istvan - Initial contribution
 */
@NonNullByDefault
public class ClientRateLimitManagerTest {

    @Test
    public void outOfOrderResponsesCannotIncreaseRemainingBudget() {
        ClientRateLimitManager manager = manager();
        String reset = Instant.now().plusSeconds(3600).toString();

        manager.updateRateLimit(1700, 1200, reset);
        manager.updateRateLimit(1700, 1250, reset);

        assertEquals(1200, manager.getRateRemaining());
    }

    @Test
    public void responseFromOlderWindowIsIgnored() {
        ClientRateLimitManager manager = manager();
        Instant newerReset = Instant.now().plusSeconds(7200);

        manager.updateRateLimit(1700, 1000, newerReset.toString());
        manager.updateRateLimit(1700, 50, newerReset.minusSeconds(3600).toString());

        assertEquals(1000, manager.getRateRemaining());
        assertEquals(newerReset.toString(), manager.getRateResetAsString());
    }

    @Test
    public void newerWindowCanResetRemainingBudget() {
        ClientRateLimitManager manager = manager();
        Instant firstReset = Instant.now().plusSeconds(3600);
        Instant nextReset = firstReset.plusSeconds(3600);

        manager.updateRateLimit(1700, 10, firstReset.toString());
        manager.updateRateLimit(1700, 1690, nextReset.toString());

        assertEquals(1690, manager.getRateRemaining());
        assertEquals(nextReset.toString(), manager.getRateResetAsString());
    }

    @Test
    public void invalidBucketConfigurationIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new ClientRateLimitManager(0, Duration.ofSeconds(1)));
        assertThrows(IllegalArgumentException.class, () -> new ClientRateLimitManager(1, Duration.ZERO));
    }

    @Test
    public void expiredRequestBucketsNoLongerThrottle() {
        AtomicLong clock = new AtomicLong(1000);
        ClientRateLimitManager manager = new ClientRateLimitManager(1, Duration.ofSeconds(1), clock::get);
        String reset = Instant.ofEpochMilli(clock.get() + Duration.ofHours(1).toMillis()).toString();

        manager.updateRateLimit(1000, 1000, reset);
        assertThrows(ClientRateLimitManager.RateLimitThrottleException.class,
                () -> manager.tryThrottle(ClientRateLimitManager.Priority.LOW));

        clock.addAndGet(Duration.ofSeconds(1).toMillis() + 1);

        assertDoesNotThrow(() -> manager.tryThrottle(ClientRateLimitManager.Priority.LOW));
    }

    private ClientRateLimitManager manager() {
        return new ClientRateLimitManager(10, Duration.ofSeconds(1));
    }
}
