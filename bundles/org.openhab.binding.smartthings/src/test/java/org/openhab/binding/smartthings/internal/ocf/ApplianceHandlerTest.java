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
package org.openhab.binding.smartthings.internal.ocf;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.smartthings.internal.SmartThingsBindingConstants.THING_TYPE_APPLIANCE;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Deterministic handler tests with no socket or appliance access.
 *
 * @author Kai Kreuzer - Initial contribution
 */
@NonNullByDefault
class ApplianceHandlerTest {
    private static final String DEVICE_ID = "51c01fcf-1b37-4f98-8d68-1e4c5c07edcc";
    private static final ThingUID UID = new ThingUID(THING_TYPE_APPLIANCE, "test");
    private static final long WAIT_SECONDS = 5;
    private final ControlledScheduler executor = new ControlledScheduler();
    private final FakeTransport transport = new FakeTransport();
    private final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
    private final BlockingQueue<ThingStatusInfo> statuses = new LinkedBlockingQueue<>();
    private final Map<String, State> states = new ConcurrentHashMap<>();
    private final List<Thing> changes = new CopyOnWriteArrayList<>();
    private final List<ApplianceHandler> handlers = new ArrayList<>();
    private Runnable afterThingUpdate = () -> {
    };

    @AfterEach
    void tearDown() throws InterruptedException {
        transport.release.countDown();
        handlers.forEach(ApplianceHandler::dispose);
        executor.shutdownNow();
        assertTrue(executor.awaitTermination(WAIT_SECONDS, TimeUnit.SECONDS));
    }

    private ApplianceHandler initialize(Map<String, Object> overrides, Map<String, String> properties)
            throws Exception {
        Map<String, Object> settings = new ConcurrentHashMap<>(Map.of("host", "127.0.0.1", "port", 5684, "keyStore",
                "unused.p12", "serverFingerprint", "00".repeat(32), "refreshInterval", 10));
        settings.putAll(overrides);
        Thing thing = ThingBuilder.create(THING_TYPE_APPLIANCE, UID).withConfiguration(new Configuration(settings))
                .withProperties(properties).build();
        doAnswer(invocation -> {
            statuses.add(invocation.getArgument(1));
            return null;
        }).when(callback).statusUpdated(any(), any());
        doAnswer(invocation -> {
            ChannelUID channel = invocation.getArgument(0);
            states.put(channel.getId(), invocation.getArgument(1));
            return null;
        }).when(callback).stateUpdated(any(), any());
        doAnswer(invocation -> {
            changes.add(invocation.getArgument(0));
            afterThingUpdate.run();
            return null;
        }).when(callback).thingUpdated(any());
        ApplianceHandler handler = new ApplianceHandler(thing, config -> transport, executor);
        handlers.add(handler);
        handler.setCallback(callback);
        assertTimeoutPreemptively(Duration.ofSeconds(2), handler::initialize);
        return handler;
    }

    private ApplianceHandler initialize() throws Exception {
        return initialize(Map.of(), Map.of());
    }

    private ThingStatusInfo awaitStatus(ThingStatus expected) throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(WAIT_SECONDS);
        while (System.nanoTime() < deadline) {
            ThingStatusInfo status = statuses.poll(deadline - System.nanoTime(), TimeUnit.NANOSECONDS);
            if (status != null && status.getStatus() == expected) {
                return status;
            }
        }
        fail("Expected status " + expected);
        throw new AssertionError();
    }

    private static Channel channel(ApplianceHandler handler, String label) {
        return handler.getThing().getChannels().stream().filter(candidate -> label.equals(candidate.getLabel()))
                .findFirst().orElseThrow();
    }

    @Test
    void initializesAsynchronouslyAndPublishesTheFirstBatchEntry() throws Exception {
        ApplianceHandler handler = initialize();
        awaitStatus(ThingStatus.ONLINE);
        Channel power = channel(handler, "Power");
        assertEquals(OnOffType.ON, states.get(power.getUID().getId()));
        assertEquals("smartthings:switch", power.getChannelTypeUID().toString());
        assertEquals("smartthings:switch-readonly", channel(handler, "Remote Control").getChannelTypeUID().toString());
        assertEquals(DEVICE_ID, handler.getThing().getProperties().get("deviceId"));
        assertFalse(handler.getThing().getProperties().containsKey("ownerId"));
        assertEquals(List.of("/oic/d", "/device/0"), transport.reads);
        assertEquals(1, changes.size());
    }

    @Test
    void refreshOnlyReadsAndUnchangedChannelsAreNotRebuilt() throws Exception {
        ApplianceHandler handler = initialize();
        awaitStatus(ThingStatus.ONLINE);
        handler.handleCommand(channel(handler, "Power").getUID(), RefreshType.REFRESH);
        awaitStatus(ThingStatus.ONLINE);
        assertTrue(transport.posts.isEmpty());
        assertEquals(1, changes.size());
        assertEquals(4, transport.reads.size());
    }

    @Test
    void readsBatchInterfaceOnlyForDefaultLinksAndHydratesNonSecurityStubs() throws Exception {
        transport.batch = json("""
                {"links":[{"href":"/power/0"},{"href":"/oic/sec/cred"}]}
                """);
        transport.batchInterface = json("""
                [{"href":"/power/0"},{"href":"/remotectrl/0","rep":{"value":true}}]
                """);
        ApplianceHandler handler = initialize();
        awaitStatus(ThingStatus.ONLINE);
        assertEquals(OnOffType.ON, states.get(channel(handler, "Power").getUID().getId()));
        assertEquals(List.of("/oic/d", "/device/0", "/device/0?if=oic.if.b", "/power/0"), transport.reads);
        assertFalse(handler.getThing().getChannels().stream()
                .anyMatch(candidate -> candidate.getLabel().contains("/oic/sec/")));
    }

    @Test
    void unsupportedBatchInterfaceStillHydratesDefaultLinks() throws Exception {
        transport.batch = json("{\"links\":[{\"href\":\"/power/0\"}]}");
        transport.failBatch = true;
        ApplianceHandler handler = initialize();
        awaitStatus(ThingStatus.ONLINE);
        assertEquals(OnOffType.ON, states.get(channel(handler, "Power").getUID().getId()));
        assertTrue(transport.reads.contains("/power/0"));
    }

    @Test
    void linkOnlyCollectionsAreHydratedAgainOnEveryPoll() throws Exception {
        transport.batch = json("{\"links\":[{\"href\":\"/power/0\"}]}");
        transport.failBatch = true;
        ApplianceHandler handler = initialize();
        awaitStatus(ThingStatus.ONLINE);
        ChannelUID power = channel(handler, "Power").getUID();
        assertEquals(OnOffType.ON, states.get(power.getId()));
        transport.power = false;
        handler.handleCommand(power, RefreshType.REFRESH);
        awaitStatus(ThingStatus.ONLINE);
        assertEquals(OnOffType.OFF, states.get(power.getId()));
        assertEquals(2, transport.reads.stream().filter("/power/0"::equals).count());
    }

    @Test
    void unavailableOptionalDiagnosticDoesNotBlockPowerControl() throws Exception {
        transport.batch = json("""
                [{"href":"/power/0","rep":{"value":true}},
                 {"href":"/remotectrl/0","rep":{"value":true}},
                 {"href":"/diagnostic/0","rep":{"value":"previous"}}]
                """);
        ApplianceHandler handler = initialize();
        awaitStatus(ThingStatus.ONLINE);
        handler.handleCommand(channel(handler, "Power").getUID(), OnOffType.OFF);
        awaitStatus(ThingStatus.ONLINE);
        assertTrue(transport.reads.contains("/diagnostic/0"));
        assertEquals(List.of("/power/0:{\"value\":false}"), transport.posts);
        assertEquals(OnOffType.OFF, states.get(channel(handler, "Power").getUID().getId()));
    }

    @ParameterizedTest
    @ValueSource(strings = { "/power/0", "/remotectrl/0" })
    void unavailableTargetOrRemoteControlGatePreventsWrites(String href) throws Exception {
        ApplianceHandler handler = initialize();
        awaitStatus(ThingStatus.ONLINE);
        transport.unavailable = href;
        handler.handleCommand(channel(handler, "Power").getUID(), OnOffType.OFF);
        awaitStatus(ThingStatus.OFFLINE);
        assertTrue(transport.posts.isEmpty());
    }

    @Test
    void readbackDoesNotPublishAnOptimisticCommandState() throws Exception {
        ApplianceHandler handler = initialize();
        awaitStatus(ThingStatus.ONLINE);
        ChannelUID power = channel(handler, "Power").getUID();
        transport.blockReadback = true;
        handler.handleCommand(power, OnOffType.OFF);
        assertTrue(transport.entered.await(WAIT_SECONDS, TimeUnit.SECONDS));
        assertEquals(OnOffType.ON, states.get(power.getId()));
        assertEquals(1, transport.posts.size());
        transport.release.countDown();
        awaitStatus(ThingStatus.ONLINE);
        assertEquals(OnOffType.OFF, states.get(power.getId()));
    }

    @Test
    void failedReadbackDoesNotRetryTheWriteOrPublishAnOptimisticState() throws Exception {
        ApplianceHandler handler = initialize();
        awaitStatus(ThingStatus.ONLINE);
        ChannelUID power = channel(handler, "Power").getUID();
        transport.failReadback = true;
        handler.handleCommand(power, OnOffType.OFF);
        awaitStatus(ThingStatus.OFFLINE);
        assertEquals(1, transport.posts.size());
        assertEquals(OnOffType.ON, states.get(power.getId()));
        transport.failReadback = false;
        executor.firePoll();
        awaitStatus(ThingStatus.ONLINE);
        assertEquals(1, transport.posts.size());
    }

    @Test
    void synchronousDisposalFromThingUpdatePreventsSubsequentPublication() throws Exception {
        afterThingUpdate = () -> handlers.getFirst().dispose();
        initialize();
        assertTrue(transport.closed.await(WAIT_SECONDS, TimeUnit.SECONDS));
        executor.awaitWorkers();
        assertEquals(1, changes.size());
        assertTrue(states.isEmpty());
        assertFalse(statuses.stream().anyMatch(status -> status.getStatus() == ThingStatus.ONLINE));
        assertFalse(executor.hasActivePoll());
    }

    @Test
    void disposalDuringLiveValidationPreventsPost() throws Exception {
        ApplianceHandler handler = initialize();
        awaitStatus(ThingStatus.ONLINE);
        transport.block = true;
        handler.handleCommand(channel(handler, "Power").getUID(), OnOffType.OFF);
        assertTrue(transport.entered.await(WAIT_SECONDS, TimeUnit.SECONDS));
        handler.dispose();
        assertTrue(transport.closed.await(WAIT_SECONDS, TimeUnit.SECONDS));
        transport.release.countDown();
        assertTrue(transport.finished.await(WAIT_SECONDS, TimeUnit.SECONDS));
        executor.awaitWorkers();
        assertTrue(transport.posts.isEmpty());
    }

    @Test
    void channelWritabilityChangesWithoutRenamingTheChannel() throws Exception {
        ApplianceHandler handler = initialize();
        awaitStatus(ThingStatus.ONLINE);
        ChannelUID uid = channel(handler, "Power").getUID();
        transport.batch = json("""
                [{"href":"/power/0","rep":{"value":"unknown"}},
                 {"href":"/remotectrl/0","rep":{"value":true}}]
                """);
        handler.handleCommand(uid, RefreshType.REFRESH);
        awaitStatus(ThingStatus.ONLINE);
        assertEquals(uid, channel(handler, "Power").getUID());
        assertEquals("smartthings:switch-readonly", channel(handler, "Power").getChannelTypeUID().toString());
        assertEquals(2, changes.size());
    }

    @Test
    void incompleteBatchStubDoesNotReplacePreviouslyKnownState() throws Exception {
        ApplianceHandler handler = initialize();
        awaitStatus(ThingStatus.ONLINE);
        transport.batch = json("""
                [{"href":"/power/0","rep":{"href":"/power/0"}},
                 {"href":"/remotectrl/0","rep":{"value":true}}]
                """);
        handler.handleCommand(channel(handler, "Power").getUID(), RefreshType.REFRESH);
        awaitStatus(ThingStatus.ONLINE);
        assertEquals(OnOffType.ON, states.get(channel(handler, "Power").getUID().getId()));
        assertEquals(1, changes.size());
    }

    @Test
    void writesMinimalCommandThenReadsBackActualState() throws Exception {
        ApplianceHandler handler = initialize();
        awaitStatus(ThingStatus.ONLINE);
        handler.handleCommand(channel(handler, "Power").getUID(), OnOffType.OFF);
        awaitStatus(ThingStatus.ONLINE);
        assertEquals(List.of("/power/0:{\"value\":false}"), transport.posts);
        assertEquals(OnOffType.OFF, states.get(channel(handler, "Power").getUID().getId()));
        assertEquals("/power/0", transport.reads.getLast());
        assertEquals(1, transport.maximumActive.get());
    }

    @Test
    void remoteControlIsRecheckedBeforePosting() throws Exception {
        ApplianceHandler handler = initialize();
        awaitStatus(ThingStatus.ONLINE);
        transport.remote = false;
        handler.handleCommand(channel(handler, "Power").getUID(), OnOffType.OFF);
        transport.awaitRead("/remotectrl/0");
        handler.handleCommand(channel(handler, "Power").getUID(), RefreshType.REFRESH);
        awaitStatus(ThingStatus.ONLINE);
        assertTrue(transport.posts.isEmpty());
        assertEquals(OnOffType.ON, states.get(channel(handler, "Power").getUID().getId()));
    }

    @Test
    void readonlyAndForeignChannelsNeverPost() throws Exception {
        ApplianceHandler handler = initialize();
        awaitStatus(ThingStatus.ONLINE);
        handler.handleCommand(channel(handler, "Remote Control").getUID(), OnOffType.OFF);
        handler.handleCommand(new ChannelUID(new ThingUID(THING_TYPE_APPLIANCE, "other"), "power"), OnOffType.OFF);
        handler.handleCommand(channel(handler, "Power").getUID(), RefreshType.REFRESH);
        awaitStatus(ThingStatus.ONLINE);
        assertTrue(transport.posts.isEmpty());
    }

    @Test
    void failedPostIsNotRetriedAndScheduledReadsRecover() throws Exception {
        ApplianceHandler handler = initialize();
        awaitStatus(ThingStatus.ONLINE);
        transport.failPost = true;
        handler.handleCommand(channel(handler, "Power").getUID(), OnOffType.OFF);
        assertEquals(ThingStatusDetail.COMMUNICATION_ERROR, awaitStatus(ThingStatus.OFFLINE).getStatusDetail());
        assertEquals(1, transport.posts.size());
        executor.firePoll();
        awaitStatus(ThingStatus.ONLINE);
        assertEquals(1, transport.posts.size());
        assertEquals(OnOffType.ON, states.get(channel(handler, "Power").getUID().getId()));
    }

    @Test
    void failedReadGoesOfflineAndScheduledReadsRecover() throws Exception {
        transport.failRead = true;
        ApplianceHandler handler = initialize();
        assertEquals(ThingStatusDetail.COMMUNICATION_ERROR, awaitStatus(ThingStatus.OFFLINE).getStatusDetail());
        assertTrue(states.isEmpty());
        transport.failRead = false;
        executor.firePoll();
        awaitStatus(ThingStatus.ONLINE);
        assertEquals(OnOffType.ON, states.get(channel(handler, "Power").getUID().getId()));
    }

    @Test
    void refusesConfiguredAndRememberedIdentityMismatch() throws Exception {
        String different = "956b1f9e-cc6d-4b45-a61a-309132aac52b";
        initialize(Map.of("deviceId", different), Map.of());
        assertEquals(ThingStatusDetail.CONFIGURATION_ERROR, awaitStatus(ThingStatus.OFFLINE).getStatusDetail());
        assertTrue(states.isEmpty());
        assertTrue(changes.isEmpty());
        initialize(Map.of(), Map.of("deviceId", different));
        assertEquals(ThingStatusDetail.CONFIGURATION_ERROR, awaitStatus(ThingStatus.OFFLINE).getStatusDetail());
        assertTrue(states.isEmpty());
        initialize(Map.of(), Map.of("deviceid", different));
        assertEquals(ThingStatusDetail.CONFIGURATION_ERROR, awaitStatus(ThingStatus.OFFLINE).getStatusDetail());
        assertTrue(states.isEmpty());
        assertEquals(List.of("/oic/d", "/oic/d", "/oic/d"), transport.reads);
    }

    @Test
    void migratesLegacyIdentityAfterAuthentication() throws Exception {
        ApplianceHandler handler = initialize(Map.of(), Map.of("deviceid", DEVICE_ID));
        awaitStatus(ThingStatus.ONLINE);
        assertEquals(DEVICE_ID, handler.getThing().getProperties().get("deviceId"));
        assertFalse(handler.getThing().getProperties().containsKey("deviceid"));
    }

    @Test
    void malformedIdentityNeverPublishesResourceState() throws Exception {
        transport.identity = json("{\"di\":\"not-a-uuid\"}");
        initialize();
        assertEquals(ThingStatusDetail.COMMUNICATION_ERROR, awaitStatus(ThingStatus.OFFLINE).getStatusDetail());
        assertTrue(changes.isEmpty());
        assertTrue(states.isEmpty());
    }

    @Test
    void invalidConfigurationNeverCreatesTransportOrDisclosesCredentials() throws Exception {
        initialize(Map.of("host", "", "keyStorePassword", "SECRET_VALUE"), Map.of());
        ThingStatusInfo status = awaitStatus(ThingStatus.OFFLINE);
        assertEquals(ThingStatusDetail.CONFIGURATION_ERROR, status.getStatusDetail());
        assertFalse(status.toString().contains("SECRET_VALUE"));
        assertTrue(transport.reads.isEmpty());
    }

    @Test
    void disposalDoesNotBlockOnRequestsOrPublishLateResults() throws Exception {
        transport.block = true;
        ApplianceHandler handler = initialize();
        assertTrue(transport.entered.await(WAIT_SECONDS, TimeUnit.SECONDS));
        assertTimeoutPreemptively(Duration.ofSeconds(2), handler::dispose);
        assertTrue(transport.closed.await(WAIT_SECONDS, TimeUnit.SECONDS));
        transport.release.countDown();
        assertTrue(transport.finished.await(WAIT_SECONDS, TimeUnit.SECONDS));
        executor.awaitWorkers();
        assertTrue(states.isEmpty());
        assertTrue(changes.isEmpty());
        assertFalse(statuses.stream().anyMatch(status -> status.getStatus() == ThingStatus.ONLINE));
        assertFalse(executor.hasActivePoll());
    }

    @Test
    void reinitializationSuppressesStaleCallbacksAndKeepsIoSerialized() throws Exception {
        transport.block = true;
        ApplianceHandler handler = initialize();
        assertTrue(transport.entered.await(WAIT_SECONDS, TimeUnit.SECONDS));
        assertTimeoutPreemptively(Duration.ofSeconds(2), handler::initialize);
        assertTrue(transport.closed.await(WAIT_SECONDS, TimeUnit.SECONDS));
        transport.block = false;
        transport.release.countDown();
        awaitStatus(ThingStatus.ONLINE);
        assertEquals(1, changes.size());
        assertEquals(1, transport.maximumActive.get());
        assertEquals(DEVICE_ID, handler.getThing().getProperties().get("deviceId"));
    }

    @Test
    void commandQueueAndRefreshRequestsAreBoundedWhileIoIsBlocked() throws Exception {
        ApplianceHandler handler = initialize();
        awaitStatus(ThingStatus.ONLINE);
        ChannelUID power = channel(handler, "Power").getUID();
        transport.block = true;
        handler.handleCommand(power, RefreshType.REFRESH);
        assertTrue(transport.entered.await(WAIT_SECONDS, TimeUnit.SECONDS));
        for (int index = 0; index < 200; index++) {
            handler.handleCommand(power, OnOffType.OFF);
            handler.handleCommand(power, RefreshType.REFRESH);
        }
        transport.block = false;
        transport.release.countDown();
        for (int index = 0; index < 18; index++) {
            awaitStatus(ThingStatus.ONLINE);
        }
        assertEquals(16, transport.posts.size());
        assertEquals(1, transport.maximumActive.get());
    }

    private static JsonElement json(String text) {
        return JsonParser.parseString(text);
    }

    private static final class FakeTransport implements Transport {
        final List<String> reads = new CopyOnWriteArrayList<>();
        final List<String> posts = new CopyOnWriteArrayList<>();
        final BlockingQueue<String> completedReads = new LinkedBlockingQueue<>();
        final CountDownLatch entered = new CountDownLatch(1);
        final CountDownLatch release = new CountDownLatch(1);
        final CountDownLatch finished = new CountDownLatch(1);
        final CountDownLatch closed = new CountDownLatch(1);
        final AtomicInteger active = new AtomicInteger();
        final AtomicInteger maximumActive = new AtomicInteger();
        volatile boolean failRead;
        volatile boolean failPost;
        volatile boolean failBatch;
        volatile boolean failReadback;
        volatile boolean block;
        volatile boolean blockReadback;
        volatile boolean power = true;
        volatile boolean remote = true;
        volatile String unavailable = "";
        volatile JsonElement identity = json("{\"di\":\"" + DEVICE_ID + "\",\"rt\":[\"oic.d.airconditioner\"]}");
        volatile JsonElement batch = json("""
                [{"href":"/power/0","rep":{"value":true}},
                 {"href":"/remotectrl/0","rep":{"value":true}}]
                """);
        volatile JsonElement batchInterface = json("[]");

        @Override
        public JsonElement get(String path) throws IOException {
            maximumActive.accumulateAndGet(active.incrementAndGet(), Math::max);
            try {
                reads.add(path);
                if (block || blockReadback && !posts.isEmpty()) {
                    entered.countDown();
                    // Simulate a transport that returns late even when cancellation interrupts its caller.
                    boolean interrupted = false;
                    while (true) {
                        try {
                            if (!release.await(WAIT_SECONDS, TimeUnit.SECONDS)) {
                                throw new IOException("Test exchange timed out");
                            }
                            break;
                        } catch (InterruptedException e) {
                            interrupted = true;
                        }
                    }
                    if (interrupted) {
                        Thread.currentThread().interrupt();
                    }
                }
                if (failRead || path.equals(unavailable) || failBatch && path.contains("?if=")
                        || failReadback && !posts.isEmpty()) {
                    throw new IOException("Test read failure");
                }
                return switch (path) {
                    case "/oic/d" -> identity.deepCopy();
                    case "/device/0" -> batch.deepCopy();
                    case "/device/0?if=oic.if.b" -> batchInterface.deepCopy();
                    case "/power/0" -> json("{\"value\":" + power + "}");
                    case "/remotectrl/0" -> json("{\"value\":" + remote + "}");
                    default -> throw new IOException("Unexpected test resource");
                };
            } finally {
                active.decrementAndGet();
                completedReads.add(path);
                finished.countDown();
            }
        }

        @Override
        public void post(String href, JsonObject fields) throws IOException {
            maximumActive.accumulateAndGet(active.incrementAndGet(), Math::max);
            try {
                posts.add(href + ":" + fields);
                if (failPost) {
                    throw new IOException("Test write acknowledgement lost");
                }
                power = fields.get("value").getAsBoolean();
            } finally {
                active.decrementAndGet();
            }
        }

        @Override
        public void close() {
            closed.countDown();
        }

        void awaitRead(String expected) throws InterruptedException {
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(WAIT_SECONDS);
            while (System.nanoTime() < deadline) {
                String path = completedReads.poll(deadline - System.nanoTime(), TimeUnit.NANOSECONDS);
                if (expected.equals(path)) {
                    return;
                }
            }
            fail("Expected read " + expected);
        }
    }

    private record Poll(Runnable task, ScheduledFuture<?> future) {
    }

    private static final class ControlledScheduler extends ScheduledThreadPoolExecutor {
        private final BlockingQueue<Poll> polls = new LinkedBlockingQueue<>();

        ControlledScheduler() {
            super(3, task -> {
                Thread thread = new Thread(task, "appliance-handler-test");
                thread.setDaemon(true);
                return thread;
            });
        }

        @Override
        @NonNullByDefault({})
        public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
            if (delay == 0) {
                return super.schedule(command, delay, unit);
            }
            ScheduledFuture<?> future = super.schedule(command, 1, TimeUnit.DAYS);
            polls.add(new Poll(command, future));
            return future;
        }

        void firePoll() throws InterruptedException {
            Poll poll = polls.poll(WAIT_SECONDS, TimeUnit.SECONDS);
            assertNotNull(poll);
            poll.future().cancel(false);
            execute(poll.task());
        }

        boolean hasActivePoll() {
            return polls.stream().anyMatch(poll -> !poll.future().isCancelled());
        }

        void awaitWorkers() throws Exception {
            for (int index = 0; index < 3; index++) {
                submit(() -> {
                }).get(WAIT_SECONDS, TimeUnit.SECONDS);
            }
        }
    }
}
