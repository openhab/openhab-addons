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
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openhab.binding.rachio.internal.api.RachioApi;
import org.openhab.binding.rachio.internal.api.RachioApiException;
import org.openhab.binding.rachio.internal.api.json.RachioValve;
import org.openhab.binding.rachio.internal.api.json.RachioValveDayViewsResponse;
import org.openhab.binding.rachio.internal.api.json.RachioValveProgram;
import org.openhab.binding.rachio.internal.handler.RachioBridgeHandler.LifecycleSnapshot;
import org.openhab.binding.rachio.internal.utils.ClientRateLimitManager.RequestPurpose;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.rest.Webhook;
import org.openhab.core.io.rest.WebhookService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.BridgeBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;

/**
 * Tests that asynchronous bridge reinitialization cannot combine a new configuration with the previous API.
 *
 * @author Kovacs Istvan - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings("null")
@Timeout(15)
class RachioBridgeApiReadinessTest {
    @Test
    void uninitializedBridgeHasNoUsableApiSnapshot() throws Exception {
        try (Fixture fixture = new Fixture()) {
            assertNull(fixture.handler.currentLifecycleSnapshot());
            fixture.handler.registerWebHook("controller", RequestPurpose.BACKGROUND_REFRESH);
            verify(fixture.provider, never()).requestWebhook(anyString());
        }
    }

    @Test
    void reinitializationDefersRegistrationUntilNewApiIsReady() throws Exception {
        try (Fixture fixture = new Fixture()) {
            RachioApi previousApi = api("previous");
            fixture.initialize(previousApi);
            LifecycleSnapshot previous = fixture.handler.currentLifecycleSnapshot();
            assertNotNull(previous);

            RachioApi replacementApi = api("replacement");
            BlockingInitialization initialization = new BlockingInitialization(replacementApi, false);
            fixture.prepareReinitialization(replacementApi);
            try {
                fixture.startInitialization();
                initialization.awaitStarted();

                assertNull(fixture.handler.currentLifecycleSnapshot());
                assertFalse(fixture.handler.isLifecycleCurrent(previous.generation(), previousApi));
                assertFalse(fixture.handler.publishWebhookStateIfCurrent(previous,
                        () -> fail("Previous API must not publish during reinitialization")));
                fixture.handler.onCloudWebhookProviderChanged();
                fixture.handler.registerWebHook("controller", RequestPurpose.BACKGROUND_REFRESH);
                verifyRegistration(previousApi, never());
                verifyRegistration(replacementApi, never());

                initialization.release();
                fixture.awaitInitialization();
                LifecycleSnapshot current = fixture.handler.currentLifecycleSnapshot();
                assertNotNull(current);
                assertSame(replacementApi, current.api());
                assertTrue(current.configuration().clearAllCallbacks);
                assertTrue(fixture.handler.isLifecycleCurrent(current.generation(), replacementApi));
                verifyRegistration(previousApi, never());
                verifyRegistration(replacementApi, times(1));
            } finally {
                initialization.release();
            }
        }
    }

    @Test
    void failedReinitializationKeepsPreviousApiRetiredAndNextInitializationRecovers() throws Exception {
        try (Fixture fixture = new Fixture()) {
            RachioApi previousApi = api("previous");
            fixture.initialize(previousApi);
            RachioApi failedApi = api("failed");
            BlockingInitialization initialization = new BlockingInitialization(failedApi, true);
            fixture.prepareReinitialization(failedApi);
            try {
                fixture.startInitialization();
                initialization.awaitStarted();
                initialization.release();
                fixture.awaitInitialization();

                assertNull(fixture.handler.currentLifecycleSnapshot());
                fixture.handler.onCloudWebhookProviderChanged();
                fixture.handler.registerWebHook("controller", RequestPurpose.BACKGROUND_REFRESH);
                verifyRegistration(previousApi, never());
                verifyRegistration(failedApi, never());

                RachioApi recoveredApi = api("recovered");
                fixture.prepareReinitialization(recoveredApi);
                fixture.startInitialization();
                fixture.awaitInitialization();
                LifecycleSnapshot recovered = fixture.handler.currentLifecycleSnapshot();
                assertNotNull(recovered);
                assertSame(recoveredApi, recovered.api());
                verifyRegistration(recoveredApi, times(1));
                verifyRegistration(previousApi, never());
            } finally {
                initialization.release();
            }
        }
    }

    @Test
    void invalidReinitializationCannotUsePreviousApi() throws Exception {
        try (Fixture fixture = new Fixture()) {
            RachioApi previousApi = api("previous");
            fixture.initialize(previousApi);
            fixture.handler.dispose();
            fixture.handler.handleConfigurationUpdate(Map.of(PARAM_APIKEY, ""));
            clearInvocations(previousApi);

            fixture.startInitialization();
            assertNull(fixture.handler.currentLifecycleSnapshot());
            fixture.handler.onCloudWebhookProviderChanged();
            fixture.handler.registerWebHook("controller", RequestPurpose.BACKGROUND_REFRESH);
            verifyRegistration(previousApi, never());
            assertEquals(1, fixture.handler.createdApis);
        }
    }

    @ParameterizedTest
    @CsvSource({ "valve,false", "valve,true", "program,false", "program,true" })
    void oldApiSmartHoseLoadIsRegisteredAfterReplacementBecomesReady(String resource, boolean cloudUrl)
            throws Exception {
        try (Fixture fixture = new Fixture()) {
            fixture.thing.getConfiguration().put(PARAM_AUTO_CONFIGURE_HOSE_TIMER_WEBHOOKS, true);
            if (!cloudUrl) {
                fixture.thing.getConfiguration().put(PARAM_PUBLIC_WEBHOOK_URL,
                        "https://manual.example.org/rachio/webhook");
            }
            RachioApi previousApi = api("previous");
            fixture.initialize(previousApi);
            CountDownLatch loadStarted = new CountDownLatch(1);
            CountDownLatch releaseLoad = new CountDownLatch(1);
            RachioValve valve = new RachioValve();
            valve.id = "valve";
            RachioValveProgram program = new RachioValveProgram();
            program.id = "program";
            when(previousApi.getValveDayViews(anyString(), any(), any())).thenReturn(new RachioValveDayViewsResponse());
            doAnswer(invocation -> {
                loadStarted.countDown();
                assertTrue(releaseLoad.await(5, TimeUnit.SECONDS));
                return valve;
            }).when(previousApi).getValve(eq("valve"), eq(RequestPurpose.INITIALIZATION));
            doAnswer(invocation -> {
                loadStarted.countDown();
                assertTrue(releaseLoad.await(5, TimeUnit.SECONDS));
                return program;
            }).when(previousApi).getValveProgramV2(eq("program"), eq(RequestPurpose.INITIALIZATION));
            boolean isValve = "valve".equals(resource);
            Thing thing = spy(
                    ThingBuilder.create(isValve ? THING_TYPE_VALVE : THING_TYPE_VALVE_PROGRAM, resource)
                            .withBridge(fixture.thing.getUID())
                            .withConfiguration(new Configuration(
                                    Map.of(isValve ? PROPERTY_VALVE_ID : PROPERTY_VALVE_PROGRAM_ID, resource)))
                            .build());
            when(thing.getStatus()).thenReturn(ThingStatus.ONLINE);
            AbstractRachioThingHandler child = isValve ? new RachioValveHandler(thing) {
                @Override
                protected boolean initializeCloudHandler() {
                    cloudHandler = fixture.handler;
                    bridge = fixture.thing;
                    return true;
                }

                @Override
                protected void scheduleHandlerTask(long generation, Runnable task) {
                    task.run();
                }
            } : new RachioValveProgramHandler(thing) {
                @Override
                protected boolean initializeCloudHandler() {
                    cloudHandler = fixture.handler;
                    bridge = fixture.thing;
                    return true;
                }

                @Override
                protected void scheduleHandlerTask(long generation, Runnable task) {
                    task.run();
                }
            };
            ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
            child.setCallback(callback);
            RachioApi replacementApi = api("replacement");
            BlockingInitialization initialization = new BlockingInitialization(replacementApi, false);
            ExecutorService worker = Executors.newSingleThreadExecutor();
            try {
                Future<?> load = worker.submit(child::initialize);
                assertTrue(loadStarted.await(5, TimeUnit.SECONDS));
                fixture.prepareReinitialization(replacementApi);
                fixture.startInitialization();
                initialization.awaitStarted();
                releaseLoad.countDown();
                load.get(5, TimeUnit.SECONDS);
                verify(callback).statusUpdated(eq(thing), argThat(info -> info.getStatus() == ThingStatus.ONLINE));
                verify(previousApi, never()).registerWebHookTarget(any(), anyString(), anyString(), anyString(),
                        anyString(), anyBoolean(), any());
                verify(replacementApi, never()).registerWebHookTarget(any(), anyString(), anyString(), anyString(),
                        anyString(), anyBoolean(), any());
                initialization.release();
                fixture.awaitInitialization();
                verify(replacementApi).registerWebHookTarget(argThat(target -> resource.equals(target.getResourceId())),
                        anyString(), anyString(), anyString(), eq("replacement"), eq(true),
                        eq(RequestPurpose.INITIALIZATION));
            } finally {
                releaseLoad.countDown();
                initialization.release();
                worker.shutdownNow();
                assertTrue(worker.awaitTermination(5, TimeUnit.SECONDS));
                child.dispose();
            }
        }
    }

    private static RachioApi api(String externalId) {
        RachioApi api = mock(RachioApi.class);
        when(api.fillProperties()).thenReturn(new HashMap<>());
        when(api.getDevices()).thenReturn(new HashMap<>());
        when(api.getExternalId()).thenReturn(externalId);
        return api;
    }

    private static void verifyRegistration(RachioApi api, org.mockito.verification.VerificationMode mode)
            throws RachioApiException {
        verify(api, mode).registerWebHook(eq("controller"), anyString(), eq(""), eq(""), anyString(), anyBoolean(),
                any(RequestPurpose.class));
    }

    private static final class BlockingInitialization {
        private final CountDownLatch started = new CountDownLatch(1);
        private final CountDownLatch released = new CountDownLatch(1);

        BlockingInitialization(RachioApi api, boolean fail) throws Exception {
            doAnswer(invocation -> {
                started.countDown();
                assertTrue(released.await(5, TimeUnit.SECONDS), "Initialization must be released");
                if (fail) {
                    throw new RachioApiException("Synthetic initialization failure");
                }
                return null;
            }).when(api).initialize(anyString(), any(), any(), any());
        }

        void awaitStarted() throws InterruptedException {
            assertTrue(started.await(5, TimeUnit.SECONDS), "Initialization must start");
        }

        void release() {
            released.countDown();
        }
    }

    private static final class Fixture implements AutoCloseable {
        private final WebhookService provider = mock(WebhookService.class);
        private final Bridge thing = BridgeBuilder.create(THING_TYPE_CLOUD, "readiness")
                .withConfiguration(new Configuration(Map.of(PARAM_APIKEY, "previous-key", PARAM_AUTO_CONFIGURE_WEBHOOKS,
                        true, PARAM_USE_CLOUD_WEBHOOK, true)))
                .build();
        private final TestBridgeHandler handler;
        private @Nullable Future<?> initialAttempt;

        Fixture() throws Exception {
            when(provider.requestWebhook(startsWith(SERVLET_CLOUD_WEBHOOK_PATH_PREFIX))).thenReturn(
                    CompletableFuture.completedFuture(new Webhook(URI.create("https://example.test/webhook").toURL(),
                            Instant.now().plusSeconds(86400))));
            when(provider.removeWebhook(startsWith(SERVLET_CLOUD_WEBHOOK_PATH_PREFIX)))
                    .thenReturn(CompletableFuture.completedFuture(null));
            handler = new TestBridgeHandler(thing, provider);
            handler.setCallback(mock(ThingHandlerCallback.class));
            RachioDeviceHandler controller = mock(RachioDeviceHandler.class);
            when(controller.getBoundControllerId()).thenReturn("controller");
            when(controller.getConfiguredControllerId()).thenReturn("controller");
            handler.rachioStatusListeners.add(controller);
        }

        void initialize(RachioApi api) throws Exception {
            handler.apis.add(api);
            startInitialization();
            awaitInitialization();
            assertSame(api, handler.currentLifecycleSnapshot().api());
        }

        void prepareReinitialization(RachioApi replacementApi) {
            handler.dispose();
            handler.handleConfigurationUpdate(Map.of(PARAM_APIKEY, "replacement-key", PARAM_CLEAR_CALLBACK, true));
            clearInvocations(handler.getWebhookApi());
            handler.apis.add(replacementApi);
        }

        void startInitialization() throws Exception {
            handler.initialize();
            Field field = RachioBridgeHandler.class.getDeclaredField("initializationJob");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            AtomicReference<Future<?>> job = (AtomicReference<Future<?>>) field.get(handler);
            initialAttempt = job.get();
        }

        void awaitInitialization() throws Exception {
            assertNotNull(initialAttempt);
            initialAttempt.get(5, TimeUnit.SECONDS);
        }

        @Override
        public void close() throws Exception {
            try {
                handler.dispose();
            } finally {
                handler.apis.clear();
            }
        }
    }

    private static final class TestBridgeHandler extends RachioBridgeHandler {
        private final ConcurrentLinkedQueue<RachioApi> apis = new ConcurrentLinkedQueue<>();
        private int createdApis;

        TestBridgeHandler(Bridge bridge, WebhookService provider) {
            super(bridge, mock(HttpClient.class), () -> provider);
        }

        @Override
        RachioApi createRachioApi(String personId) {
            createdApis++;
            RachioApi api = apis.poll();
            assertNotNull(api, "Initialization must use a supplied API");
            return api;
        }
    }
}
