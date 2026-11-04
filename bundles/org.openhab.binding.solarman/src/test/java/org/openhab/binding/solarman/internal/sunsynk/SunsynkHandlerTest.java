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
package org.openhab.binding.solarman.internal.sunsynk;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.openhab.binding.solarman.internal.SolarmanBindingConstants;
import org.openhab.binding.solarman.internal.util.SolarmanWorkQueue;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;

/**
 * Verifies that concurrent or reentrant disposal cannot leave a newly scheduled worker behind.
 *
 * @author Ricardo Costa - Initial contribution
 */
@NonNullByDefault
class SunsynkHandlerTest {
    @Test
    void disposalDuringProfileLoadingPreventsSchedulingAndLateCallbacks() throws Exception {
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        AtomicInteger scheduled = new AtomicInteger();
        SunsynkHandler handler = new SunsynkHandler(thing()) {
            @Override
            protected SunsynkProfile loadProfile(String name) throws IOException {
                entered.countDown();
                try {
                    if (!release.await(5, TimeUnit.SECONDS)) {
                        throw new IOException("Profile loading test timed out");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IOException("Profile loading was cancelled", e);
                }
                return super.loadProfile(name);
            }

            @Override
            protected SolarmanWorkQueue createWorkQueue(Runnable poll, int interval, Consumer<Exception> errors) {
                scheduled.incrementAndGet();
                return super.createWorkQueue(poll, interval, errors);
            }
        };
        ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        handler.setCallback(callback);
        FutureTask<@Nullable Void> initialization = new FutureTask<>(() -> {
            handler.initialize();
            return null;
        });
        Thread initializing = Thread.ofVirtual().name("solarman-initialize-test").start(initialization);
        try {
            assertTrue(entered.await(5, TimeUnit.SECONDS), "Initialization did not reach profile loading");
            handler.dispose();
            clearInvocations(callback);
            release.countDown();
            initialization.get(5, TimeUnit.SECONDS);
            assertEquals(0, scheduled.get(), "No worker may be created after disposal");
            verifyNoInteractions(callback);
        } finally {
            release.countDown();
            initialization.cancel(true);
            initializing.join(5000);
            handler.dispose();
            assertFalse(initializing.isAlive());
        }
    }

    @Test
    void disposalFromStatusCallbackCancelsTheAlreadyOwnedFuture() {
        ScheduledExecutorService testScheduler = mock(ScheduledExecutorService.class);
        ScheduledFuture<?> future = mock(ScheduledFuture.class);
        doReturn(future).when(testScheduler).scheduleWithFixedDelay(any(Runnable.class), anyLong(), anyLong(),
                any(TimeUnit.class));
        SunsynkHandler handler = new SunsynkHandler(thing()) {
            @Override
            protected SolarmanWorkQueue createWorkQueue(Runnable poll, int interval, Consumer<Exception> errors) {
                return new SolarmanWorkQueue(testScheduler, poll, interval, errors);
            }
        };
        ThingHandlerCallback callback = mock(ThingHandlerCallback.class, invocation -> {
            if ("statusUpdated".equals(invocation.getMethod().getName())) {
                handler.dispose();
            }
            return Answers.RETURNS_DEFAULTS.answer(invocation);
        });
        handler.setCallback(callback);
        try {
            handler.initialize();
            verify(future).cancel(true);
            verify(testScheduler, never()).shutdown();
        } finally {
            handler.dispose();
        }
    }

    private static Thing thing() {
        ThingTypeUID type = SolarmanBindingConstants.SUNSYNK_PROFILES.keySet().iterator().next();
        Thing thing = mock(Thing.class);
        when(thing.getThingTypeUID()).thenReturn(type);
        when(thing.getUID()).thenReturn(new ThingUID(type, "lifecycle"));
        when(thing.getConfiguration()).thenReturn(
                new Configuration(Map.of("hostname", "127.0.0.1", "serialNumber", "1234567890", "timeZone", "UTC")));
        return thing;
    }
}
