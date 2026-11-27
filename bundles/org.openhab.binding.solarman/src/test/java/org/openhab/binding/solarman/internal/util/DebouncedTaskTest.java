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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link DebouncedTask}.
 *
 * @author Oleksandr Mishchuk - Initial contribution
 */
@NonNullByDefault
class DebouncedTaskTest {
    private final ScheduledExecutorService scheduler = (@NonNull ScheduledExecutorService) mock(
            ScheduledExecutorService.class);
    private final Runnable task = () -> {
    };
    private final DebouncedTask debouncedTask = new DebouncedTask(scheduler, task, Duration.ofSeconds(2));

    @Test
    void triggerSchedulesTaskAfterDelay() {
        ScheduledFuture<?> future = mockFuture();
        when(scheduler.schedule(any(Runnable.class), anyLong(), any())).thenAnswer(i -> future);

        debouncedTask.trigger();

        verify(scheduler).schedule(task, 2000, TimeUnit.MILLISECONDS);
        verify(future, never()).cancel(false);
    }

    @Test
    void repeatedTriggerRestartsDelay() {
        ScheduledFuture<?> first = mockFuture();
        ScheduledFuture<?> second = mockFuture();
        ScheduledFuture<?> third = mockFuture();
        when(scheduler.schedule(any(Runnable.class), anyLong(), any())).thenAnswer(i -> first).thenAnswer(i -> second)
                .thenAnswer(i -> third);

        debouncedTask.trigger();
        debouncedTask.trigger();
        debouncedTask.trigger();

        verify(scheduler, times(3)).schedule(eq(task), eq(2000L), eq(TimeUnit.MILLISECONDS));
        verify(first).cancel(false);
        verify(second).cancel(false);
        verify(third, never()).cancel(false);
    }

    @Test
    void cancelStopsPendingTask() {
        ScheduledFuture<?> future = mockFuture();
        when(scheduler.schedule(any(Runnable.class), anyLong(), any())).thenAnswer(i -> future);

        debouncedTask.trigger();
        debouncedTask.cancel();
        debouncedTask.cancel();

        verify(future, times(1)).cancel(false);
    }

    private static ScheduledFuture<?> mockFuture() {
        return (@NonNull ScheduledFuture<?>) mock(ScheduledFuture.class);
    }
}
