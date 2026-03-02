/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.jellyfin.internal.util.timeout;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link SessionTimeoutMonitor}.
 *
 * @author Patrik Gfeller - Initial contribution
 */
@NonNullByDefault
class SessionTimeoutMonitorTest {

    private static final long TIMEOUT_MS = 200; // Short timeout for fast tests
    private @NonNullByDefault({}) ScheduledExecutorService exec;

    @BeforeEach
    void setUp() {
        exec = Executors.newSingleThreadScheduledExecutor();
    }

    @AfterEach
    void tearDown() {
        exec.shutdownNow();
    }

    @Test
    void testNotTimedOutAfterRecentActivity() throws InterruptedException {
        SessionTimeoutMonitor monitor = new SessionTimeoutMonitor(TIMEOUT_MS);
        monitor.recordActivity();

        assertFalse(monitor.isTimedOut(), "Should not be timed out immediately after activity");
    }

    @Test
    void testIsTimedOutAfterThreshold() throws InterruptedException {
        SessionTimeoutMonitor monitor = new SessionTimeoutMonitor(TIMEOUT_MS);
        monitor.recordActivity();

        Thread.sleep(TIMEOUT_MS + 50);

        assertTrue(monitor.isTimedOut(), "Should be timed out after threshold elapsed");
    }

    @Test
    void testNotTimedOutWithNoActivityRecorded() {
        SessionTimeoutMonitor monitor = new SessionTimeoutMonitor(TIMEOUT_MS);
        // recordActivity never called

        assertFalse(monitor.isTimedOut(), "Should not be timed out if no activity was ever recorded");
    }

    @Test
    void testResetActivityClearsTimedOutState() throws InterruptedException {
        SessionTimeoutMonitor monitor = new SessionTimeoutMonitor(TIMEOUT_MS);
        monitor.recordActivity();
        Thread.sleep(TIMEOUT_MS + 50);
        assertTrue(monitor.isTimedOut());

        monitor.resetActivity();

        assertFalse(monitor.isTimedOut(), "After resetActivity, should not be timed out");
    }

    @Test
    void testOnTimeoutCallbackFired() throws InterruptedException {
        SessionTimeoutMonitor monitor = new SessionTimeoutMonitor(TIMEOUT_MS);
        AtomicInteger callCount = new AtomicInteger();

        monitor.recordActivity();
        monitor.start(exec, "test-device", () -> true, callCount::incrementAndGet);

        // Wait long enough for the monitor to check and detect timeout
        Thread.sleep(TIMEOUT_MS * 3);
        monitor.stop();

        assertTrue(callCount.get() >= 1, "onTimeout callback should have been triggered");
    }

    @Test
    void testOnTimeoutCallbackNotFiredWhenNoSession() throws InterruptedException {
        SessionTimeoutMonitor monitor = new SessionTimeoutMonitor(TIMEOUT_MS);
        AtomicInteger callCount = new AtomicInteger();

        monitor.recordActivity();
        // hasSession returns false → timeout check is skipped
        monitor.start(exec, "test-device", () -> false, callCount::incrementAndGet);

        Thread.sleep(TIMEOUT_MS * 3);
        monitor.stop();

        assertEquals(0, callCount.get(), "onTimeout must not fire when hasSession returns false");
    }

    @Test
    void testStopCancelsMonitor() throws InterruptedException {
        SessionTimeoutMonitor monitor = new SessionTimeoutMonitor(TIMEOUT_MS);
        AtomicInteger callCount = new AtomicInteger();

        monitor.recordActivity();
        monitor.start(exec, "test-device", () -> true, callCount::incrementAndGet);
        monitor.stop();

        // Ensure enough time passes that the monitor would have fired if still active
        Thread.sleep(TIMEOUT_MS * 3);

        assertEquals(0, callCount.get(), "Callback must not fire after stop()");
    }
}
