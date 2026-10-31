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
import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openhab.binding.rachio.internal.RachioConfiguration;
import org.openhab.binding.rachio.internal.api.RachioApi;
import org.openhab.binding.rachio.internal.api.RachioApiException;
import org.openhab.binding.rachio.internal.api.RachioDevice;
import org.openhab.binding.rachio.internal.api.json.RachioDeviceGsonDTO.RachioCloudDevice;
import org.openhab.binding.rachio.internal.handler.RachioCloudWebhookRegistry.CloudWebhookLease;
import org.openhab.binding.rachio.internal.utils.ClientRateLimitManager.RequestPurpose;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.rest.Webhook;
import org.openhab.core.io.rest.WebhookService;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.BridgeBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;

/**
 * Verifies lease handoff, concurrent Cloud registration and retained controller intent.
 *
 * @author Kovacs Istvan - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings("null")
class RachioCloudWebhookRecoveryTest {
    @Test
    void redundantAcquisitionDoesNotLeakThePreviouslyPublishedLease() throws Exception {
        WebhookService provider = provider();
        RachioCloudWebhookRegistry registry = spy(new RachioCloudWebhookRegistry(() -> provider));
        CountDownLatch firstAcquired = new CountDownLatch(1);
        CountDownLatch secondAcquired = new CountDownLatch(1);
        CountDownLatch allowFirst = new CountDownLatch(1);
        CountDownLatch allowSecond = new CountDownLatch(1);
        AtomicInteger acquisitions = new AtomicInteger();
        doAnswer(invocation -> {
            CloudWebhookLease lease = (CloudWebhookLease) invocation.callRealMethod();
            if (acquisitions.incrementAndGet() == 1) {
                firstAcquired.countDown();
                await(allowFirst);
            } else {
                secondAcquired.countDown();
                await(allowSecond);
            }
            return lease;
        }).when(registry).acquire(anyString());
        ExecutorService workers = Executors.newFixedThreadPool(2);
        try (Fixture fixture = new Fixture(registry)) {
            try {
                Future<String> first = workers.submit(fixture.bridge::getModernWebhookUrlForRegistration);
                await(firstAcquired);
                Future<String> second = workers.submit(fixture.bridge::getModernWebhookUrlForRegistration);
                await(secondAcquired);
                allowFirst.countDown();
                assertFalse(first.get(5, TimeUnit.SECONDS).isBlank());
                allowSecond.countDown();
                assertEquals(first.get(), second.get(5, TimeUnit.SECONDS));
                assertEquals(1, registry.activeConsumerCount());
                fixture.bridge.dispose();
                assertEquals(0, registry.activeConsumerCount());
                verify(provider, times(1)).removeWebhook(startsWith(SERVLET_CLOUD_WEBHOOK_PATH_PREFIX));
            } finally {
                allowFirst.countDown();
                allowSecond.countDown();
                workers.shutdownNow();
                assertTrue(workers.awaitTermination(5, TimeUnit.SECONDS));
            }
        }
    }

    @Test
    void abandonedOldAcquireCannotReleaseTheNewLifecycleOwner() throws Exception {
        WebhookService provider = provider();
        RachioCloudWebhookRegistry registry = spy(new RachioCloudWebhookRegistry(() -> provider));
        CountDownLatch oldAcquired = new CountDownLatch(1);
        CountDownLatch currentAcquired = new CountDownLatch(1);
        CountDownLatch allowOld = new CountDownLatch(1);
        CountDownLatch allowCurrent = new CountDownLatch(1);
        AtomicInteger acquisitions = new AtomicInteger();
        doAnswer(invocation -> {
            CloudWebhookLease lease = (CloudWebhookLease) invocation.callRealMethod();
            if (acquisitions.incrementAndGet() == 1) {
                oldAcquired.countDown();
                await(allowOld);
            } else {
                currentAcquired.countDown();
                await(allowCurrent);
            }
            return lease;
        }).when(registry).acquire(anyString());
        ExecutorService workers = Executors.newFixedThreadPool(2);
        try (Fixture fixture = new Fixture(registry)) {
            try {
                Future<String> old = workers.submit(fixture.bridge::getModernWebhookUrlForRegistration);
                await(oldAcquired);
                fixture.bridge.dispose();
                setField(fixture.bridge, "lifecycleGeneration", 2L);
                setField(fixture.bridge, "disposed", false);
                setField(fixture.bridge, "apiReady", true);
                Future<String> current = workers.submit(fixture.bridge::getModernWebhookUrlForRegistration);
                await(currentAcquired);
                allowOld.countDown();
                assertEquals("", old.get(5, TimeUnit.SECONDS));
                allowCurrent.countDown();
                assertFalse(current.get(5, TimeUnit.SECONDS).isBlank());
                assertEquals(1, registry.activeConsumerCount());
                verify(provider, never()).removeWebhook(anyString());
                fixture.bridge.dispose();
                assertEquals(0, registry.activeConsumerCount());
                verify(provider, times(1)).removeWebhook(startsWith(SERVLET_CLOUD_WEBHOOK_PATH_PREFIX));
            } finally {
                allowOld.countDown();
                allowCurrent.countDown();
                workers.shutdownNow();
                assertTrue(workers.awaitTermination(5, TimeUnit.SECONDS));
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "controller", "valve", "program" })
    void overlappingCallsForOneCloudTargetShareTheInFlightAttempt(String resource) throws Exception {
        WebhookService service = provider();
        RachioCloudWebhookRegistry registry = new RachioCloudWebhookRegistry(() -> service);
        CountDownLatch registrationStarted = new CountDownLatch(1);
        CountDownLatch allowRegistration = new CountDownLatch(1);
        AtomicInteger calls = new AtomicInteger();
        AtomicBoolean remoteCallbackPresent = new AtomicBoolean();
        ExecutorService workers = Executors.newFixedThreadPool(2);
        try (Fixture fixture = new Fixture(registry)) {
            fixture.config.clearAllCallbacks = true;
            fixture.config.autoConfigureHoseTimerWebhooks = true;
            org.mockito.stubbing.Answer<Void> register = invocation -> {
                if (calls.incrementAndGet() > 1) {
                    remoteCallbackPresent.set(false);
                    throw new RachioApiException("create failed after duplicate cleanup");
                }
                remoteCallbackPresent.set(true);
                registrationStarted.countDown();
                await(allowRegistration);
                return null;
            };
            doAnswer(register).when(fixture.api).registerWebHook(anyString(), anyString(), eq(""), eq(""),
                    eq("recovery-test"), eq(true), any(RequestPurpose.class));
            doAnswer(register).when(fixture.api).registerWebHookTarget(any(), anyString(), eq(""), eq(""),
                    eq("recovery-test"), eq(true), any(RequestPurpose.class));
            try {
                Future<?> first = workers.submit(() -> fixture.register(resource));
                await(registrationStarted);
                Future<?> overlapping = workers.submit(() -> fixture.register(resource));
                overlapping.get(5, TimeUnit.SECONDS);
                assertEquals(1, calls.get());
                assertTrue(remoteCallbackPresent.get());
                allowRegistration.countDown();
                first.get(5, TimeUnit.SECONDS);
                assertTrue(remoteCallbackPresent.get());
                assertEquals(1, calls.get());
            } finally {
                allowRegistration.countDown();
                workers.shutdownNow();
                assertTrue(workers.awaitTermination(5, TimeUnit.SECONDS));
            }
        }
    }

    @Test
    void configuredControllerKeepsItsRetryAcrossTemporaryCatalogueAbsence() throws Exception {
        AtomicReference<@Nullable WebhookService> provider = new AtomicReference<>();
        try (Fixture fixture = new Fixture(new RachioCloudWebhookRegistry(provider::get))) {
            Thing thing = ThingBuilder
                    .create(THING_TYPE_DEVICE,
                            new ThingUID(THING_TYPE_DEVICE, fixture.bridge.getThing().getUID(), "controller"))
                    .withBridge(fixture.bridge.getThing().getUID())
                    .withConfiguration(new Configuration(Map.of(PROPERTY_DEV_ID, "controller-A"))).build();
            RachioDeviceHandler child = spy(new RachioDeviceHandler(thing));
            child.setCallback(mock(ThingHandlerCallback.class));
            doNothing().when(child).postChannelData();
            setField(child, "cloudHandler", fixture.bridge);
            RachioCloudDevice dto = new RachioCloudDevice();
            dto.id = "controller-A";
            dto.name = "Controller";
            RachioDevice device = new RachioDevice(dto);
            AtomicReference<@Nullable RachioDevice> catalogue = new AtomicReference<>(device);
            doAnswer(invocation -> catalogue.get()).when(fixture.bridge).getDevByConfiguredDeviceId(any(Thing.class),
                    eq("controller-A"));
            child.rebindToCurrentModel(device, "test setup");
            fixture.bridge.rachioStatusListeners.add(child);
            try {
                fixture.bridge.registerWebHook("controller-A", RequestPurpose.INITIALIZATION);
                assertEquals(1, fixture.retries.size());
                catalogue.set(null);
                child.onDeviceCatalogChanged();
                assertNull(child.getBoundControllerId());
                assertEquals("controller-A", child.getConfiguredControllerId());
                fixture.retries.getFirst().run();
                assertEquals(2, fixture.retries.size());
                catalogue.set(device);
                child.onDeviceCatalogChanged();
                provider.set(provider());
                fixture.retries.getLast().run();
                verify(fixture.api).registerWebHook(eq("controller-A"), anyString(), eq(""), eq(""),
                        eq("recovery-test"), eq(false), eq(RequestPurpose.BACKGROUND_REFRESH));
                assertEquals("controller-A", child.getBoundControllerId());
            } finally {
                child.dispose();
            }
        }
    }

    private static WebhookService provider() throws Exception {
        WebhookService service = mock(WebhookService.class);
        when(service.requestWebhook(startsWith(SERVLET_CLOUD_WEBHOOK_PATH_PREFIX))).thenReturn(
                CompletableFuture.completedFuture(new Webhook(URI.create("https://cloud.example.org/current").toURL(),
                        Instant.now().plusSeconds(86400))));
        when(service.removeWebhook(startsWith(SERVLET_CLOUD_WEBHOOK_PATH_PREFIX)))
                .thenReturn(CompletableFuture.completedFuture(null));
        return service;
    }

    private static void await(CountDownLatch latch) throws InterruptedException {
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Timed out waiting for the test operation");
    }

    private static void setField(Object target, String name, Object value) throws ReflectiveOperationException {
        for (Class<?> type = target.getClass(); type != null; type = type.getSuperclass()) {
            try {
                Field field = type.getDeclaredField(name);
                field.setAccessible(true);
                field.set(target, value);
                return;
            } catch (NoSuchFieldException ignored) {
            }
        }
        throw new NoSuchFieldException(name);
    }

    private static final class Fixture implements AutoCloseable {
        final RachioConfiguration config = new RachioConfiguration();
        final RachioApi api = mock(RachioApi.class);
        final List<Runnable> retries = new CopyOnWriteArrayList<>();
        final RachioBridgeHandler bridge;

        Fixture(RachioCloudWebhookRegistry registry) throws Exception {
            when(api.fillProperties()).thenReturn(new HashMap<>());
            when(api.getExternalId()).thenReturn("recovery-test");
            bridge = spy(new RachioBridgeHandler(
                    BridgeBuilder.create(THING_TYPE_CLOUD, "recovery").withConfiguration(new Configuration()).build(),
                    mock(HttpClient.class), registry));
            bridge.setCallback(mock(ThingHandlerCallback.class));
            config.autoConfigureWebhooks = true;
            config.useCloudWebhook = true;
            setField(bridge, "thingConfig", config);
            setField(bridge, "rachioApi", api);
            setField(bridge, "apiReady", true);
            ScheduledExecutorService scheduler = mock(ScheduledExecutorService.class);
            doAnswer(invocation -> {
                long delay = invocation.getArgument(1);
                if (delay > 0 && delay <= 300) {
                    retries.add(invocation.getArgument(0));
                }
                return mock(ScheduledFuture.class);
            }).when(scheduler).schedule(any(Runnable.class), anyLong(), eq(TimeUnit.SECONDS));
            setField(bridge, "webhookCoordinator", new RachioWebhookCoordinator(registry, scheduler));
        }

        void register(String resource) {
            try {
                switch (resource) {
                    case "controller" -> bridge.registerWebHook("target", RequestPurpose.INITIALIZATION);
                    case "valve" -> bridge.registerValveWebHook("target", RequestPurpose.INITIALIZATION);
                    case "program" -> bridge.registerValveProgramWebHook("target", RequestPurpose.INITIALIZATION);
                    default -> throw new IllegalArgumentException(resource);
                }
            } catch (RachioApiException e) {
                throw new AssertionError(e);
            }
        }

        @Override
        public void close() {
            bridge.dispose();
        }
    }
}
