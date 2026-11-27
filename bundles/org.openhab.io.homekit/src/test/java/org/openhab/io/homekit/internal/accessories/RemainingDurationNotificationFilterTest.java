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
package org.openhab.io.homekit.internal.accessories;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Tests HomeKit remaining-duration notification requirements independently of wall-clock timing.
 *
 * @author Leo Siepel - Initial contribution
 */
@NonNullByDefault
class RemainingDurationNotificationFilterTest {
    private final AtomicLong nanoTime = new AtomicLong();
    private final RemainingDurationNotificationFilter filter = new RemainingDurationNotificationFilter(100,
            nanoTime::get);

    @Test
    void suppressesNormalCountdownIncludingSkippedUpdates() {
        advanceMillis(1000);
        assertFalse(filter.shouldNotify(99));
        advanceMillis(1000);
        assertFalse(filter.shouldNotify(98));
        advanceMillis(6000);
        assertFalse(filter.shouldNotify(92));
    }

    @Test
    void toleratesSubSecondSamplingJitter() {
        advanceMillis(900);
        assertFalse(filter.shouldNotify(99));
        advanceMillis(1200);
        assertFalse(filter.shouldNotify(98));
        advanceMillis(800);
        assertFalse(filter.shouldNotify(97));
    }

    @Test
    void notifiesWhenCountdownIsExtended() {
        advanceMillis(8000);
        assertFalse(filter.shouldNotify(92));
        assertTrue(filter.shouldNotify(95));
        advanceMillis(1000);
        assertFalse(filter.shouldNotify(94));
    }

    @Test
    void notifiesWhenCountdownIsShortened() {
        advanceMillis(8000);
        assertFalse(filter.shouldNotify(92));
        assertTrue(filter.shouldNotify(85));
        advanceMillis(1000);
        assertFalse(filter.shouldNotify(84));
    }

    @Test
    void notifiesWhenDurationReachesZero() {
        advanceMillis(99000);
        assertFalse(filter.shouldNotify(1));
        advanceMillis(1000);
        assertTrue(filter.shouldNotify(0));
        advanceMillis(1000);
        assertFalse(filter.shouldNotify(0));
    }

    @Test
    void notifiesWhenStoppedEarlyAndRestarted() {
        advanceMillis(8000);
        assertTrue(filter.shouldNotify(0));
        advanceMillis(1000);
        assertTrue(filter.shouldNotify(100));
        advanceMillis(1000);
        assertFalse(filter.shouldNotify(99));
    }

    @Test
    void notifiesWhenCountdownIsPaused() {
        advanceMillis(8000);
        assertFalse(filter.shouldNotify(92));
        advanceMillis(1000);
        assertTrue(filter.shouldNotify(92));
    }

    @Test
    void doesNotAccumulateTimingDriftAcrossSuppressedUpdates() {
        advanceMillis(1500);
        assertFalse(filter.shouldNotify(99));
        advanceMillis(1500);
        assertTrue(filter.shouldNotify(98));
    }

    @Test
    void notifiesForOneSecondCorrections() {
        assertTrue(filter.shouldNotify(99));
        assertTrue(filter.shouldNotify(100));
    }

    private void advanceMillis(long millis) {
        nanoTime.addAndGet(TimeUnit.MILLISECONDS.toNanos(millis));
    }
}
