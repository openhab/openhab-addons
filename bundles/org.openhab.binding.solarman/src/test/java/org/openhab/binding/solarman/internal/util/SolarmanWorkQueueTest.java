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
package org.openhab.binding.solarman.internal.util;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Verifies non-blocking submission, serialization, bounded capacity and cancellation without sleeping.
 *
 * @author Ricardo Costa - Initial contribution
 */
@NonNullByDefault
class SolarmanWorkQueueTest {
    @Test
    void serializesCommandsAndPollingAndCancelsEverything() {
        ScheduledExecutorService scheduler = mock(ScheduledExecutorService.class);
        ScheduledFuture<?> future = mock(ScheduledFuture.class);
        AtomicReference<Runnable> tick = new AtomicReference<>();
        doAnswer(invocation -> {
            tick.set(invocation.getArgument(0));
            return future;
        }).when(scheduler).scheduleWithFixedDelay(any(Runnable.class), eq(250L), eq(250L), eq(TimeUnit.MILLISECONDS));
        List<String> calls = new ArrayList<>();
        SolarmanWorkQueue queue = new SolarmanWorkQueue(scheduler, () -> calls.add("poll"), 30,
                error -> fail(error.getMessage()));
        assertTrue(queue.submit(() -> calls.add("write-1")));
        assertTrue(queue.submit(() -> calls.add("write-2")));
        assertTrue(calls.isEmpty(), "Submitting must not execute payload construction or I/O");
        Objects.requireNonNull(tick.get()).run();
        Objects.requireNonNull(tick.get()).run();
        assertEquals(List.of("write-1", "poll", "write-2", "poll"), calls);
        for (int i = 0; i < 32; i++) {
            assertTrue(queue.submit(() -> calls.add("queued")));
        }
        assertFalse(queue.submit(() -> calls.add("overflow")));
        queue.close();
        Objects.requireNonNull(tick.get()).run();
        assertEquals(4, calls.size());
        assertFalse(queue.submit(() -> calls.add("after-dispose")));
        verify(future).cancel(true);
        verify(scheduler, never()).shutdown();
        verify(scheduler, never()).shutdownNow();
    }

    @Test
    void refreshesAfterSixthCommandFailsAndContinuesWithLaterCommands() {
        ScheduledExecutorService scheduler = mock(ScheduledExecutorService.class);
        ScheduledFuture<?> future = mock(ScheduledFuture.class);
        AtomicReference<Runnable> tick = new AtomicReference<>();
        doAnswer(invocation -> {
            tick.set(invocation.getArgument(0));
            return future;
        }).when(scheduler).scheduleWithFixedDelay(any(Runnable.class), eq(250L), eq(250L), eq(TimeUnit.MILLISECONDS));
        List<String> calls = new ArrayList<>();
        try (SolarmanWorkQueue queue = new SolarmanWorkQueue(scheduler, () -> calls.add("poll"), 30,
                error -> calls.add("error"))) {
            for (int period = 1; period <= 6; period++) {
                int current = period;
                assertTrue(queue.submit(() -> {
                    calls.add("write-" + current);
                    if (current == 6) {
                        throw new IllegalStateException("Write failed");
                    }
                }));
            }
            for (int period = 1; period <= 6; period++) {
                Objects.requireNonNull(tick.get()).run();
            }
            assertEquals(List.of("write-1", "poll", "write-2", "poll", "write-3", "poll", "write-4", "poll", "write-5",
                    "poll", "write-6", "error", "poll"), calls);
            assertTrue(queue.submit(() -> calls.add("next-write")));
            Objects.requireNonNull(tick.get()).run();
            assertEquals(List.of("next-write", "poll"), calls.subList(13, 15));
        }
        verify(future).cancel(true);
    }

    @Test
    void completeConfirmationReplacesPollUntilTheNextRefreshInterval() {
        try (QueueFixture fixture = new QueueFixture()) {
            assertTrue(fixture.queue.submitWithSnapshot(() -> {
                fixture.calls.add("confirmed");
                return true;
            }));
            fixture.tick();
            assertEquals(List.of("confirmed"), fixture.calls);
            fixture.time.addAndGet(TimeUnit.SECONDS.toNanos(29));
            fixture.tick();
            assertEquals(List.of("confirmed"), fixture.calls);
            fixture.time.addAndGet(TimeUnit.SECONDS.toNanos(1));
            fixture.tick();
            assertEquals(List.of("confirmed", "poll"), fixture.calls);
        }
    }

    @Test
    void successfulBurstSkipsPollsButFinalFailureStillRefreshes() {
        try (QueueFixture fixture = new QueueFixture()) {
            for (int period = 1; period <= 6; period++) {
                int current = period;
                assertTrue(fixture.queue.submitWithSnapshot(() -> {
                    fixture.calls.add("write-" + current);
                    if (current == 6) {
                        throw new IllegalStateException("Failed final command");
                    }
                    return true;
                }));
            }
            for (int period = 1; period <= 6; period++) {
                fixture.tick();
            }
            assertEquals(List.of("write-1", "write-2", "write-3", "write-4", "write-5", "write-6", "error", "poll"),
                    fixture.calls);
            assertTrue(fixture.queue.submitWithSnapshot(() -> false));
            fixture.tick();
            assertEquals(3, fixture.calls.stream().filter(s -> s.equals("poll") || s.equals("error")).count());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "before-command", "during-command", "during-poll" })
    void explicitRefreshRequestsSurviveConfirmedCommandsAndPolling(String when) {
        try (QueueFixture fixture = new QueueFixture()) {
            if ("before-command".equals(when)) {
                fixture.queue.requestRefresh();
            }
            assertTrue(fixture.queue.submitWithSnapshot(() -> {
                fixture.calls.add("confirmed");
                if ("during-command".equals(when)) {
                    fixture.queue.requestRefresh();
                }
                return true;
            }));
            fixture.tick();
            if ("during-poll".equals(when)) {
                fixture.duringPoll.set(fixture.queue::requestRefresh);
                fixture.queue.requestRefresh();
                fixture.tick();
                fixture.duringPoll.set(() -> {
                });
                fixture.tick();
                assertEquals(List.of("confirmed", "poll", "poll"), fixture.calls);
            } else {
                fixture.tick();
                assertEquals(List.of("confirmed", "poll"), fixture.calls);
            }
        }
    }

    @Test
    void disposalDuringConfirmedCommandPreventsExplicitRefresh() {
        try (QueueFixture fixture = new QueueFixture()) {
            fixture.queue.requestRefresh();
            assertTrue(fixture.queue.submitWithSnapshot(() -> {
                fixture.queue.close();
                return true;
            }));
            fixture.tick();
            assertTrue(fixture.calls.isEmpty());
            assertFalse(fixture.queue.submitWithSnapshot(() -> true));
            verify(fixture.future).cancel(true);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "confirmed", "refresh", "failure", "dispose", "rejected" })
    void delayedTransactionRetainsOwnershipAndCleansUpOnEveryExit(String scenario) throws Exception {
        try (QueueFixture fixture = new QueueFixture()) {
            SolarmanWorkQueue.Transaction transaction = mock(SolarmanWorkQueue.Transaction.class);
            doAnswer(invocation -> {
                fixture.calls.add("write");
                return SolarmanWorkQueue.Step.DELAYED;
            }).doAnswer(invocation -> {
                fixture.calls.add("confirm");
                if ("failure".equals(scenario)) {
                    throw new IllegalStateException("Readback failed");
                }
                return SolarmanWorkQueue.Step.CONFIRMED;
            }).when(transaction).advance();
            if ("rejected".equals(scenario)) {
                doThrow(new RejectedExecutionException("Scheduler stopped")).when(fixture.scheduler)
                        .schedule(any(Runnable.class), eq(50L), eq(TimeUnit.MILLISECONDS));
            }
            assertTrue(fixture.queue.submitTransaction(transaction));
            assertTrue(fixture.queue.submit(() -> fixture.calls.add("next")));
            fixture.tick();
            if ("rejected".equals(scenario)) {
                assertEquals(List.of("write", "error", "poll"), fixture.calls);
                verify(transaction).close();
                return;
            }
            assertEquals(List.of("write"), fixture.calls);
            verify(fixture.scheduler).schedule(any(Runnable.class), eq(50L), eq(TimeUnit.MILLISECONDS));
            fixture.tick();
            assertEquals(List.of("write"), fixture.calls, "A waiting transaction must exclude commands and polling");
            Runnable delayed = Objects.requireNonNull(fixture.deferred.get());
            if ("dispose".equals(scenario)) {
                fixture.queue.close();
                delayed.run();
                fixture.tick();
                assertEquals(List.of("write"), fixture.calls);
                verify(fixture.delayedFuture).cancel(true);
                verify(transaction).close();
                return;
            }
            if ("refresh".equals(scenario)) {
                fixture.queue.requestRefresh();
            }
            fixture.time.addAndGet(TimeUnit.MILLISECONDS.toNanos(50));
            delayed.run();
            verify(transaction).close();
            if ("failure".equals(scenario)) {
                assertEquals(List.of("write", "confirm", "error", "poll"), fixture.calls);
            } else if ("refresh".equals(scenario)) {
                assertEquals(List.of("write", "confirm", "poll"), fixture.calls);
            } else {
                assertEquals(List.of("write", "confirm"), fixture.calls);
            }
            fixture.tick();
            assertEquals("next", fixture.calls.get(fixture.calls.size() - 2));
            assertEquals("poll", fixture.calls.getLast());
        }
    }

    @Test
    void disposalFromFirstStepPreventsTimerScheduling() throws Exception {
        try (QueueFixture fixture = new QueueFixture()) {
            assertTrue(fixture.queue.submitTransaction(() -> {
                fixture.queue.close();
                return SolarmanWorkQueue.Step.DELAYED;
            }));
            fixture.tick();
            verify(fixture.scheduler, never()).schedule(any(Runnable.class), anyLong(), any(TimeUnit.class));
            assertTrue(fixture.calls.isEmpty());
        }
    }

    private static class QueueFixture implements AutoCloseable {
        final AtomicReference<Runnable> scheduled = new AtomicReference<>();
        final AtomicReference<Runnable> duringPoll = new AtomicReference<>(() -> {
        });
        final AtomicLong time = new AtomicLong(1);
        final List<String> calls = new ArrayList<>();
        final ScheduledFuture<?> future = mock(ScheduledFuture.class);
        final ScheduledFuture<?> delayedFuture = mock(ScheduledFuture.class);
        final ScheduledExecutorService scheduler = mock(ScheduledExecutorService.class);
        final AtomicReference<Runnable> deferred = new AtomicReference<>();
        final SolarmanWorkQueue queue;

        QueueFixture() {
            doAnswer(invocation -> {
                scheduled.set(invocation.getArgument(0));
                return future;
            }).when(scheduler).scheduleWithFixedDelay(any(Runnable.class), eq(250L), eq(250L),
                    eq(TimeUnit.MILLISECONDS));
            doAnswer(invocation -> {
                deferred.set(invocation.getArgument(0));
                return delayedFuture;
            }).when(scheduler).schedule(any(Runnable.class), eq(50L), eq(TimeUnit.MILLISECONDS));
            queue = new SolarmanWorkQueue(scheduler, () -> {
                calls.add("poll");
                Objects.requireNonNull(duringPoll.get()).run();
            }, 30, error -> calls.add("error"), time::get);
        }

        void tick() {
            Objects.requireNonNull(scheduled.get()).run();
        }

        @Override
        public void close() {
            queue.close();
        }
    }
}
