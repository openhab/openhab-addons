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
}
