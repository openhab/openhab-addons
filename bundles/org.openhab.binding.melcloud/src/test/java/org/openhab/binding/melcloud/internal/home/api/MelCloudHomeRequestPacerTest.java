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
package org.openhab.binding.melcloud.internal.home.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.openhab.binding.melcloud.internal.exceptions.MelCloudCommException;

/**
 * Unit tests for {@link MelCloudHomeRequestPacer}.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
class MelCloudHomeRequestPacerTest {

    @Test
    void whenNoPriorCallThenTaskRunsImmediatelyOnCallingThread() {
        // Arrange
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        MelCloudHomeRequestPacer pacer = new MelCloudHomeRequestPacer(scheduler);
        Thread callingThread = Thread.currentThread();
        AtomicBoolean ran = new AtomicBoolean(false);
        AtomicReference<@Nullable Thread> executingThread = new AtomicReference<>();

        // Act
        pacer.schedule(() -> {
            ran.set(true);
            executingThread.set(Thread.currentThread());
        });

        // Assert
        assertTrue(ran.get(), "task should have run synchronously before schedule() returned");
        assertEquals(callingThread, executingThread.get());
    }

    @Test
    void whenSecondCallArrivesWithinIntervalThenItIsDelayedNotDropped() throws InterruptedException {
        // Arrange
        long intervalMillis = 200;
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        MelCloudHomeRequestPacer pacer = new MelCloudHomeRequestPacer(scheduler, intervalMillis);
        CountDownLatch secondDone = new CountDownLatch(1);

        // Act
        pacer.schedule(() -> {
            // First call: reserves the next slot, nothing to assert.
        });
        pacer.schedule(secondDone::countDown);

        // Assert
        boolean completedBeforeInterval = secondDone.await(intervalMillis / 2, TimeUnit.MILLISECONDS);
        assertFalse(completedBeforeInterval, "second call must not run before the pacing interval elapses");

        boolean completedEventually = secondDone.await(2, TimeUnit.SECONDS);
        assertTrue(completedEventually, "second call must still run, only delayed");
    }

    @Test
    void whenScheduleBlockingCallSucceedsThenItReturnsTheResult() throws MelCloudCommException {
        // Arrange
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        MelCloudHomeRequestPacer pacer = new MelCloudHomeRequestPacer(scheduler);

        // Act
        String result = pacer.scheduleBlocking(() -> "ok");

        // Assert
        assertEquals("ok", result);
    }

    @Test
    void whenScheduleBlockingCallThrowsThenTheOriginalExceptionPropagates() {
        // Arrange
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        MelCloudHomeRequestPacer pacer = new MelCloudHomeRequestPacer(scheduler);
        MelCloudCommException failure = new MelCloudCommException("boom");

        // Act & Assert
        MelCloudCommException thrown = assertThrows(MelCloudCommException.class, () -> pacer.scheduleBlocking(() -> {
            throw failure;
        }));
        assertEquals(failure, thrown);
    }

    @Test
    void whenScheduleBlockingCallIsDelayedByPacingThenItStillBlocksUntilCompletion() throws MelCloudCommException {
        // Arrange: same pacing setup as whenSecondCallArrivesWithinIntervalThenItIsDelayedNotDropped, but asserting
        // that a blocking caller genuinely waits for the delayed result instead of racing it.
        long intervalMillis = 200;
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        MelCloudHomeRequestPacer pacer = new MelCloudHomeRequestPacer(scheduler, intervalMillis);

        // Act
        pacer.schedule(() -> {
            // First call: reserves the next slot, nothing to assert.
        });
        long before = System.nanoTime();
        String result = pacer.scheduleBlocking(() -> "delayed-ok");
        long elapsedMillis = (System.nanoTime() - before) / 1_000_000;

        // Assert
        assertEquals("delayed-ok", result);
        assertTrue(elapsedMillis >= intervalMillis / 2, "scheduleBlocking should have waited for its paced slot");
    }

    @Test
    void whenScheduleBlockingCallThrowsRuntimeExceptionThenItIsWrappedInsteadOfBlockingForever() {
        // Arrange: the first call reserves the slot so the second one runs on the scheduler thread
        long intervalMillis = 50;
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        MelCloudHomeRequestPacer pacer = new MelCloudHomeRequestPacer(scheduler, intervalMillis);
        pacer.schedule(() -> {
            // First call: reserves the next slot, nothing to assert.
        });

        // Act & Assert
        MelCloudCommException thrown = assertThrows(MelCloudCommException.class, () -> pacer.scheduleBlocking(() -> {
            throw new IllegalStateException("unexpected");
        }));
        assertTrue(thrown.getCause() instanceof IllegalStateException);
    }

    @Test
    void whenScheduleBlockingTimesOutThenItsPacedCallIsSkippedInsteadOfRunningLater() throws InterruptedException {
        // Arrange: a pacing delay far beyond the blocking timeout, so the caller always gives up first
        long intervalMillis = 500;
        long blockingTimeoutMillis = 50;
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        MelCloudHomeRequestPacer pacer = new MelCloudHomeRequestPacer(scheduler, intervalMillis, blockingTimeoutMillis);
        pacer.schedule(() -> {
            // First call: reserves the next slot, nothing to assert.
        });
        AtomicBoolean ran = new AtomicBoolean(false);

        // Act
        assertThrows(MelCloudCommException.class, () -> pacer.scheduleBlocking(() -> {
            ran.set(true);
            return "too late";
        }));

        // Assert - a marker queued after the paced task on the same single-threaded scheduler can only run once the
        // paced task has been passed, so it proves the paced call was skipped rather than merely still pending.
        CountDownLatch markerDone = new CountDownLatch(1);
        scheduler.schedule(markerDone::countDown, intervalMillis + 200, TimeUnit.MILLISECONDS);
        assertTrue(markerDone.await(5, TimeUnit.SECONDS), "marker task should have run");
        assertFalse(ran.get(), "a call that timed out must not reach the API afterwards");
    }
}
