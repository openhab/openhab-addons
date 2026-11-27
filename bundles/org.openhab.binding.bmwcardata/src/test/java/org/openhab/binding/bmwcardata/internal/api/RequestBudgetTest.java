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
package org.openhab.binding.bmwcardata.internal.api;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.openhab.binding.bmwcardata.internal.MapStorage;

/**
 * Tests for {@link RequestBudget}.
 *
 * @author Mark Herwege - Initial contribution
 * @author Michael Weger - Adaptation to the BMW CarData binding
 */
@NonNullByDefault
class RequestBudgetTest {

    /** A clock the test moves forward. */
    private static class TestClock extends Clock {
        private Instant now = Instant.parse("2026-10-21T10:00:00Z");

        void advance(Duration duration) {
            now = now.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(@Nullable ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }

    @Test
    void enforcesRollingWindow() {
        TestClock clock = new TestClock();
        RequestBudget budget = new RequestBudget(new MapStorage(), "budget", 3, clock);

        assertTrue(budget.tryAcquire());
        clock.advance(Duration.ofHours(1));
        assertTrue(budget.tryAcquire());
        assertTrue(budget.tryAcquire());
        assertFalse(budget.tryAcquire());
        assertEquals(3, budget.used());

        // the first request ages out after 24 hours
        clock.advance(Duration.ofHours(23));
        assertTrue(budget.hasRoomFor(1));
        assertFalse(budget.hasRoomFor(2));
        assertTrue(budget.tryAcquire());
        assertFalse(budget.tryAcquire());
    }

    @Test
    void survivesRestart() {
        TestClock clock = new TestClock();
        MapStorage storage = new MapStorage();
        RequestBudget budget = new RequestBudget(storage, "budget", 2, clock);
        assertTrue(budget.tryAcquire());
        assertTrue(budget.tryAcquire());

        RequestBudget restarted = new RequestBudget(storage, "budget", 2, clock);
        assertEquals(2, restarted.used());
        assertFalse(restarted.tryAcquire());
    }

    @Test
    void exhaustedByBmw() {
        TestClock clock = new TestClock();
        RequestBudget budget = new RequestBudget(new MapStorage(), "budget", 40, clock);
        assertTrue(budget.tryAcquire());
        budget.markExhausted();
        assertFalse(budget.tryAcquire());
        clock.advance(Duration.ofHours(24).plusSeconds(1));
        assertTrue(budget.tryAcquire());
    }

    @Test
    void budgetIsLimitedToBmwLimit() {
        TestClock clock = new TestClock();
        assertEquals(RequestBudget.BMW_DAILY_LIMIT, new RequestBudget(new MapStorage(), "budget", 100, clock).budget());
        assertEquals(1, new RequestBudget(new MapStorage(), "budget", 0, clock).budget());
        assertEquals(40, new RequestBudget(new MapStorage(), "budget", 40, clock).budget());
    }

    @Test
    void ignoresCorruptStorage() {
        MapStorage storage = new MapStorage();
        storage.put("budget", "{not a list");
        RequestBudget budget = new RequestBudget(storage, "budget", 2, new TestClock());
        assertEquals(0, budget.used());
    }
}
