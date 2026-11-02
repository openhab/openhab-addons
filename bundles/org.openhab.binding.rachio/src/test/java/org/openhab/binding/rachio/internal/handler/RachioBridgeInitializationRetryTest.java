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
package org.openhab.binding.rachio.internal.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.*;

import java.lang.reflect.Field;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openhab.binding.rachio.internal.api.RachioApi;
import org.openhab.binding.rachio.internal.api.RachioApiException;
import org.openhab.binding.rachio.internal.api.RachioApiResult;
import org.openhab.binding.rachio.internal.api.RachioApiThrottledException;
import org.openhab.binding.rachio.internal.utils.ClientRateLimitManager.Priority;
import org.openhab.binding.rachio.internal.utils.ClientRateLimitManager.RateLimitThrottleException;
import org.openhab.binding.rachio.internal.utils.ClientRateLimitManager.RequestPurpose;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.BridgeBuilder;

/**
 * Tests automatic initialization recovery without reusing a previous API or manually reinitializing the bridge.
 *
 * @author Kovacs Istvan - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings("null")
@Timeout(15)
class RachioBridgeInitializationRetryTest {
    @Test
    void transientInitializationFailuresRecoverOnScheduledRetries() throws Exception {
        try (Fixture fixture = new Fixture()) {
            fixture.handler.apis.add(failedApi(503));
            fixture.handler.apis.add(failedApi(0));
            RachioApi recovered = api();
            fixture.handler.apis.add(recovered);
            fixture.handler.initialize();
            fixture.run(0, 0);
            assertNull(fixture.handler.currentLifecycleSnapshot());
            fixture.run(1, 30);
            assertNull(fixture.handler.currentLifecycleSnapshot());
            fixture.run(2, 60);
            assertSame(recovered, fixture.handler.currentLifecycleSnapshot().api());
            assertEquals(3, fixture.tasks.size());
            assertEquals(3, fixture.handler.createdApis);
        }
    }

    @Test
    void repeatedFailuresHaveCappedBackoff() throws Exception {
        try (Fixture fixture = new Fixture()) {
            for (int i = 0; i < 8; i++) {
                fixture.handler.apis.add(failedApi(503));
            }
            fixture.handler.initialize();
            long[] delays = { 0, 30, 60, 120, 240, 300, 300, 300 };
            for (int i = 0; i < delays.length; i++) {
                fixture.run(i, delays[i]);
            }
            assertEquals(9, fixture.tasks.size());
            assertEquals(300, fixture.tasks.getLast().delaySeconds());
        }
    }

    @ParameterizedTest
    @ValueSource(ints = { 401, 403 })
    void authenticationFailureDoesNotRetry(int code) throws Exception {
        try (Fixture fixture = new Fixture()) {
            fixture.handler.apis.add(failedApi(code));
            fixture.handler.initialize();
            fixture.run(0, 0);
            assertEquals(1, fixture.tasks.size());
            assertNull(fixture.handler.currentLifecycleSnapshot());
        }
    }

    @Test
    void accountRateLimitDefersInitializationInsteadOfRetryingFrequently() throws Exception {
        try (Fixture fixture = new Fixture()) {
            fixture.handler.apis.add(failedApi(429));
            fixture.handler.initialize();
            fixture.run(0, 0);
            assertEquals(TimeUnit.DAYS.toSeconds(1), fixture.tasks.get(1).delaySeconds());
        }
    }

    @Test
    void missingApiKeyDoesNotScheduleInitialization() throws Exception {
        try (Fixture fixture = new Fixture()) {
            fixture.handler.getThing().getConfiguration().put(PARAM_APIKEY, "");
            fixture.handler.initialize();
            assertTrue(fixture.tasks.isEmpty());
        }
    }

    @Test
    void localThrottleHonorsItsSuggestedRetryDelay() throws Exception {
        try (Fixture fixture = new Fixture()) {
            RachioApi api = api();
            RachioApiThrottledException failure = new RachioApiThrottledException(
                    new RateLimitThrottleException(Priority.MEDIUM, RequestPurpose.INITIALIZATION, 0.001, 1),
                    new RachioApiResult());
            doThrow(failure).when(api).initialize(anyString(), any(), any(), any());
            fixture.handler.apis.add(api);
            fixture.handler.initialize();
            fixture.run(0, 0);
            assertEquals(Math.max(30, failure.getSuggestedRetryDelay().toSeconds()),
                    fixture.tasks.get(1).delaySeconds());
        }
    }

    @Test
    void disposedBridgeCannotRunQueuedRetry() throws Exception {
        try (Fixture fixture = new Fixture()) {
            fixture.handler.apis.add(failedApi(503));
            fixture.handler.initialize();
            fixture.run(0, 0);
            Attempt retry = fixture.tasks.get(1);
            fixture.handler.dispose();
            verify(retry.future()).cancel(true);
            retry.task().run();
            assertEquals(1, fixture.handler.createdApis);
            assertEquals(2, fixture.tasks.size());
        }
    }

    @Test
    void reinitializationCancelsOldRetryAndResetsBackoff() throws Exception {
        try (Fixture fixture = new Fixture()) {
            fixture.handler.apis.add(failedApi(503));
            fixture.handler.apis.add(failedApi(503));
            fixture.handler.apis.add(failedApi(503));
            fixture.handler.initialize();
            fixture.run(0, 0);
            fixture.run(1, 30);
            Attempt old = fixture.tasks.get(2);
            fixture.handler.initialize();
            verify(old.future()).cancel(true);
            old.task().run();
            assertEquals(2, fixture.handler.createdApis);
            fixture.run(3, 0);
            assertEquals(30, fixture.tasks.get(4).delaySeconds());
        }
    }

    private static RachioApi api() {
        RachioApi api = mock(RachioApi.class);
        when(api.fillProperties()).thenReturn(new HashMap<>());
        when(api.getDevices()).thenReturn(new HashMap<>());
        return api;
    }

    private static RachioApi failedApi(int code) throws Exception {
        RachioApi api = api();
        RachioApiResult result = new RachioApiResult();
        result.responseCode = code;
        doThrow(new RachioApiException("Initialization failed", result)).when(api).initialize(anyString(), any(), any(),
                any());
        return api;
    }

    private record Attempt(Runnable task, long delaySeconds, ScheduledFuture<?> future) {
    }

    private static class Fixture implements AutoCloseable {
        final List<Attempt> tasks = new ArrayList<>();
        final TestBridge handler;

        Fixture() throws Exception {
            ScheduledExecutorService scheduler = mock(ScheduledExecutorService.class);
            when(scheduler.schedule(any(Runnable.class), anyLong(), eq(TimeUnit.SECONDS))).thenAnswer(invocation -> {
                return recordAttempt(invocation.getArgument(0), invocation.getArgument(1));
            });
            when(scheduler.submit(any(Runnable.class)))
                    .thenAnswer(invocation -> recordAttempt(invocation.getArgument(0), 0));
            handler = new TestBridge(BridgeBuilder.create(THING_TYPE_CLOUD, "retry")
                    .withConfiguration(new Configuration(Map.of(PARAM_APIKEY, "test-key"))).build());
            handler.setCallback(mock(ThingHandlerCallback.class));
            for (Class<?> type = handler.getClass(); type != null; type = type.getSuperclass()) {
                try {
                    Field field = type.getDeclaredField("scheduler");
                    field.setAccessible(true);
                    field.set(handler, scheduler);
                    break;
                } catch (NoSuchFieldException ignored) {
                }
            }
        }

        void run(int index, long delaySeconds) {
            assertTrue(tasks.size() > index, "An initialization attempt must be scheduled");
            Attempt task = tasks.get(index);
            assertEquals(delaySeconds, task.delaySeconds());
            task.task().run();
        }

        private ScheduledFuture<?> recordAttempt(Runnable task, long delaySeconds) {
            ScheduledFuture<?> future = mock(ScheduledFuture.class);
            tasks.add(new Attempt(task, delaySeconds, future));
            return future;
        }

        @Override
        public void close() {
            handler.dispose();
        }
    }

    private static class TestBridge extends RachioBridgeHandler {
        final Queue<RachioApi> apis = new ArrayDeque<>();
        int createdApis;

        TestBridge(Bridge bridge) {
            super(bridge, mock(HttpClient.class));
        }

        @Override
        RachioApi createRachioApi(String personId) {
            createdApis++;
            return apis.remove();
        }
    }
}
