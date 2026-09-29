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
package org.openhab.binding.chromecast.internal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link ChromecastScheduler}.
 *
 * @author Jason Hubbard - Initial contribution
 */
@NonNullByDefault
class ChromecastSchedulerTest {

    private final ScheduledExecutorService executor = mock(ScheduledExecutorService.class);
    private final ChromecastScheduler scheduler = new ChromecastScheduler(executor, 10, () -> {
    }, 10, () -> {
    });

    @Test
    void schedulesWhileAlive() {
        scheduler.scheduleConnect();
        scheduler.scheduleRefresh();

        verify(executor).schedule(any(Runnable.class), anyLong(), any(TimeUnit.class));
        verify(executor).scheduleWithFixedDelay(any(Runnable.class), anyLong(), anyLong(), any(TimeUnit.class));
    }

    @Test
    void doesNotScheduleAfterDestroy() {
        scheduler.destroy();
        scheduler.scheduleConnect();
        scheduler.scheduleRefresh();

        verify(executor, never()).schedule(any(Runnable.class), anyLong(), any(TimeUnit.class));
        verify(executor, never()).scheduleWithFixedDelay(any(Runnable.class), anyLong(), anyLong(),
                any(TimeUnit.class));
    }
}
