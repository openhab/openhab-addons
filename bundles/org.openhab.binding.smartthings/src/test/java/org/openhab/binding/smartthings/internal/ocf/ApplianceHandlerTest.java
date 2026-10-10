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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.events.EventPublisher;
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
import org.openhab.core.thing.link.ItemChannelLinkRegistry;
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
    private final ApplianceDescriptionProvider descriptionProvider = new ApplianceDescriptionProvider(
            mock(EventPublisher.class), mock(ItemChannelLinkRegistry.class));
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
        ApplianceHandler handler = new ApplianceHandler(thing, config -> transport, executor, descriptionProvider,
                executor.nanoTime::get);
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
        assertEquals("system:power", power.getChannelTypeUID().toString());
        assertEquals("smartthings:switch-readonly", channel(handler, "Remote Control").getChannelTypeUID().toString());
        assertEquals(DEVICE_ID, handler.getThing().getProperties().get("deviceId"));
        assertFalse(handler.getThing().getProperties().containsKey("ownerId"));
        assertEquals(List.of("/oic/d", "/device/0"), transport.reads);
        assertEquals(1, changes.size());
    }

    @Test
    void observationsPublishImmediatelyWithoutPollingAndRetainPartialFields() throws Exception {
        transport.observe = true;
        ApplianceHandler handler = initialize();
        awaitStatus(ThingStatus.ONLINE);
        assertEquals(2, transport.listeners.size());
        transport.listeners.get("/power/0").onUpdate(json("{\"value\":false}"));
        awaitStatus(ThingStatus.ONLINE);
        assertEquals(OnOffType.OFF, states.get(channel(handler, "Power").getUID().getId()));
        transport.listeners.get("/remotectrl/0").onUpdate(json("{\"value\":true}"));
        awaitStatus(ThingStatus.ONLINE);
        executor.awaitWorkers();
        assertFalse(executor.hasActivePoll());
        transport.listeners.get("/power/0").onUpdate(json("{\"rt\":[\"oic.r.switch.binary\"]}"));
        awaitStatus(ThingStatus.ONLINE);
        assertEquals(OnOffType.OFF, states.get(channel(handler, "Power").getUID().getId()));
        assertEquals(List.of("/oic/d", "/device/0"), transport.reads);
    }

    @Test
    void observationFailureRestoresPollingAndRetriesSubscriptions() throws Exception {
        transport.observe = true;
        ApplianceHandler handler = initialize();
        awaitStatus(ThingStatus.ONLINE);
        Transport.Listener old = transport.listeners.get("/power/0");
        old.onUpdate(json("{\"value\":true}"));
        transport.listeners.get("/remotectrl/0").onUpdate(json("{\"value\":true}"));
        awaitStatus(ThingStatus.ONLINE);
        awaitStatus(ThingStatus.ONLINE);
        executor.awaitWorkers();
        assertFalse(executor.hasActivePoll());
        old.onFailure();
        assertTrue(executor.hasActivePoll());
        executor.firePoll();
        awaitStatus(ThingStatus.ONLINE);
        assertNotSame(old, transport.listeners.get("/power/0"));
        assertEquals(1, transport.subscriptionsClosed.get());
        old.onUpdate(json("{\"value\":false}"));
        executor.awaitWorkers();
        assertEquals(OnOffType.ON, states.get(channel(handler, "Power").getUID().getId()));
        transport.listeners.get("/power/0").onUpdate(json("{\"value\":false}"));
        awaitStatus(ThingStatus.ONLINE);
        executor.awaitWorkers();
        assertFalse(executor.hasActivePoll());
    }

    @Test
    void repeatedFailuresBackOffEvenWhenInitialRegistrationSucceeds() throws Exception {
        transport.observe = true;
        initialize();
        awaitStatus(ThingStatus.ONLINE);
        transport.listeners.get("/power/0").onUpdate(json("{\"value\":true}"));
        transport.listeners.get("/remotectrl/0").onUpdate(json("{\"value\":true}"));
        awaitStatus(ThingStatus.ONLINE);
        awaitStatus(ThingStatus.ONLINE);
        transport.listeners.get("/power/0").onFailure();
        executor.firePoll();
        awaitStatus(ThingStatus.ONLINE);
        Transport.Listener replacement = transport.listeners.get("/power/0");
        replacement.onUpdate(json("{\"value\":true}"));
        awaitStatus(ThingStatus.ONLINE);
        replacement.onFailure();
        replacement.onFailure();
        executor.firePoll();
        awaitStatus(ThingStatus.ONLINE);
        assertSame(replacement, transport.listeners.get("/power/0"));
        assertEquals(2, transport.observeAttempts.get("/power/0").get());
        executor.firePoll();
        awaitStatus(ThingStatus.ONLINE);
        assertNotSame(replacement, transport.listeners.get("/power/0"));
        assertEquals(3, transport.observeAttempts.get("/power/0").get());
        assertEquals(2, transport.subscriptionsClosed.get());
        Transport.Listener recovered = transport.listeners.get("/power/0");
        recovered.onUpdate(json("{\"value\":true}"));
        awaitStatus(ThingStatus.ONLINE);
        recovered.onUpdate(json("{\"value\":false}"));
        awaitStatus(ThingStatus.ONLINE);
        recovered.onFailure();
        executor.firePoll();
        awaitStatus(ThingStatus.ONLINE);
        assertEquals(4, transport.observeAttempts.get("/power/0").get());
        assertEquals(1, transport.observeAttempts.get("/remotectrl/0").get());
        assertEquals(1, Collections.frequency(transport.reads, "/device/0"));
        assertEquals(0, Collections.frequency(transport.reads, "/remotectrl/0"));
    }

    @ParameterizedTest
    @ValueSource(ints = { 10, 60, 600 })
    void unsupportedSubscriptionsBackOffWithABoundedDelayWhilePollingContinues(int interval) throws Exception {
        transport.observe = true;
        transport.unobserved = "/remotectrl/0";
        initialize(Map.of("refreshInterval", interval), Map.of());
        awaitStatus(ThingStatus.ONLINE);
        transport.listeners.get("/power/0").onUpdate(json("{\"value\":true}"));
        awaitStatus(ThingStatus.ONLINE);
        int attempts = 1;
        int polls = 0;
        for (int retry = 0; retry < 7; retry++) {
            int delay = Math.min(interval << retry, Math.max(interval, 300));
            for (int elapsed = interval; elapsed <= delay; elapsed += interval) {
                executor.firePoll();
                awaitStatus(ThingStatus.ONLINE);
                polls++;
                assertEquals(attempts + (elapsed == delay ? 1 : 0),
                        transport.observeAttempts.get("/remotectrl/0").get());
            }
            attempts++;
        }
        assertEquals(polls, Collections.frequency(transport.reads, "/remotectrl/0"));
        assertEquals(0, Collections.frequency(transport.reads, "/power/0"));
        assertEquals(1, transport.observeAttempts.get("/power/0").get());
    }

    @Test
    void pendingRegistrationsExpireAndRecoverWithoutLateUpdates() throws Exception {
        transport.observe = true;
        ApplianceHandler handler = initialize(Map.of("timeout", 1), Map.of());
        awaitStatus(ThingStatus.ONLINE);
        Transport.Listener pending = transport.listeners.get("/power/0");
        transport.power = false;
        transport.listeners.get("/remotectrl/0").onUpdate(json("{\"value\":true}"));
        awaitStatus(ThingStatus.ONLINE);
        executor.firePoll();
        awaitStatus(ThingStatus.ONLINE);
        assertEquals(1, transport.subscriptionsClosed.get());
        assertEquals(OnOffType.OFF, states.get(channel(handler, "Power").getUID().getId()));
        pending.onUpdate(json("{\"value\":true}"));
        executor.firePoll();
        awaitStatus(ThingStatus.ONLINE);
        assertNotSame(pending, transport.listeners.get("/power/0"));
        assertEquals(OnOffType.OFF, states.get(channel(handler, "Power").getUID().getId()));
        transport.listeners.get("/power/0").onUpdate(json("{\"value\":false}"));
        awaitStatus(ThingStatus.ONLINE);
        assertFalse(executor.hasActivePoll());
    }

    @Test
    void simultaneousFailuresPreserveHealthyPushesAndCoalesceFallbackReads() throws Exception {
        transport.observe = true;
        StringBuilder batch = new StringBuilder("[{\"href\":\"/power/0\",\"rep\":{\"value\":true}}");
        for (int index = 0; index < 17; index++) {
            batch.append(",{\"href\":\"/optional/").append(index).append("\",\"rep\":{\"value\":true}}");
        }
        transport.batch = json(batch.append(']').toString());
        ApplianceHandler handler = initialize();
        awaitStatus(ThingStatus.ONLINE);
        for (Transport.Listener listener : transport.listeners.values()) {
            listener.onUpdate(json("{\"value\":true}"));
        }
        for (int index = 0; index < 18; index++) {
            awaitStatus(ThingStatus.ONLINE);
        }
        Transport.Listener power = transport.listeners.get("/power/0");
        for (int index = 0; index < 17; index++) {
            transport.listeners.get("/optional/" + index).onFailure();
        }
        power.onUpdate(json("{\"value\":false}"));
        awaitStatus(ThingStatus.ONLINE);
        assertEquals(OnOffType.OFF, states.get(channel(handler, "Power").getUID().getId()));
        executor.firePoll();
        awaitStatus(ThingStatus.ONLINE);
        assertEquals(17, transport.subscriptionsClosed.get());
        assertEquals(19, transport.reads.size());
        assertSame(power, transport.listeners.get("/power/0"));
        assertEquals(OnOffType.OFF, states.get(channel(handler, "Power").getUID().getId()));
        for (int index = 0; index < 17; index++) {
            String href = "/optional/" + index;
            assertEquals(2, transport.observeAttempts.get(href).get());
            assertEquals(1, Collections.frequency(transport.reads, href));
            transport.listeners.get(href).onUpdate(json("{\"value\":true}"));
        }
        for (int index = 0; index < 17; index++) {
            awaitStatus(ThingStatus.ONLINE);
        }
        assertFalse(executor.hasActivePoll());
    }

    @Test
    void unavailableOptionalFallbackResourceDoesNotDiscardHealthySubscriptions() throws Exception {
        transport.observe = true;
        ApplianceHandler handler = initialize();
        awaitStatus(ThingStatus.ONLINE);
        Transport.Listener power = transport.listeners.get("/power/0");
        power.onUpdate(json("{\"value\":true}"));
        transport.listeners.get("/remotectrl/0").onUpdate(json("{\"value\":true}"));
        awaitStatus(ThingStatus.ONLINE);
        awaitStatus(ThingStatus.ONLINE);
        transport.unavailable = "/remotectrl/0";
        transport.listeners.get("/remotectrl/0").onFailure();
        executor.firePoll();
        awaitStatus(ThingStatus.ONLINE);
        assertSame(power, transport.listeners.get("/power/0"));
        assertEquals(2, Collections.frequency(transport.reads, "/oic/d"));
        assertFalse(statuses.stream().anyMatch(status -> status.getStatus() == ThingStatus.OFFLINE));
        power.onUpdate(json("{\"value\":false}"));
        awaitStatus(ThingStatus.ONLINE);
        assertEquals(OnOffType.OFF, states.get(channel(handler, "Power").getUID().getId()));
    }

    @Test
    void failedFallbackConnectionReauthenticatesAndReestablishesSubscriptions() throws Exception {
        transport.observe = true;
        initialize();
        awaitStatus(ThingStatus.ONLINE);
        Transport.Listener old = transport.listeners.get("/power/0");
        old.onUpdate(json("{\"value\":true}"));
        transport.listeners.get("/remotectrl/0").onUpdate(json("{\"value\":true}"));
        awaitStatus(ThingStatus.ONLINE);
        awaitStatus(ThingStatus.ONLINE);
        old.onFailure();
        transport.failRead = true;
        executor.firePoll();
        awaitStatus(ThingStatus.OFFLINE);
        assertTrue(transport.closed.await(WAIT_SECONDS, TimeUnit.SECONDS));
        transport.failRead = false;
        executor.firePoll();
        awaitStatus(ThingStatus.ONLINE);
        assertNotSame(old, transport.listeners.get("/power/0"));
        assertEquals(2, Collections.frequency(transport.reads, "/device/0"));
        transport.listeners.get("/power/0").onUpdate(json("{\"value\":false}"));
        transport.listeners.get("/remotectrl/0").onUpdate(json("{\"value\":true}"));
        awaitStatus(ThingStatus.ONLINE);
        awaitStatus(ThingStatus.ONLINE);
        assertFalse(executor.hasActivePoll());
    }

    @Test
    void explicitRefreshDoesNotBypassSubscriptionBackoff() throws Exception {
        transport.observe = true;
        transport.unobserved = "/remotectrl/0";
        ApplianceHandler handler = initialize();
        awaitStatus(ThingStatus.ONLINE);
        transport.listeners.get("/power/0").onUpdate(json("{\"value\":true}"));
        awaitStatus(ThingStatus.ONLINE);
        handler.handleCommand(channel(handler, "Power").getUID(), RefreshType.REFRESH);
        awaitStatus(ThingStatus.ONLINE);
        assertEquals(2, Collections.frequency(transport.reads, "/device/0"));
        assertEquals(1, transport.observeAttempts.get("/remotectrl/0").get());
        executor.firePoll();
        awaitStatus(ThingStatus.ONLINE);
        assertEquals(2, transport.observeAttempts.get("/remotectrl/0").get());
    }

    @Test
    void malformedNotificationsAndPendingSubscriptionsKeepFallbackPolling() throws Exception {
        transport.observe = true;
        ApplianceHandler handler = initialize();
        awaitStatus(ThingStatus.ONLINE);
        executor.awaitWorkers();
        assertTrue(executor.hasActivePoll());
        transport.listeners.get("/power/0").onUpdate(json("[42]"));
        executor.awaitWorkers();
        assertTrue(executor.hasActivePoll());
        assertEquals(OnOffType.ON, states.get(channel(handler, "Power").getUID().getId()));
        assertFalse(statuses.stream().anyMatch(status -> status.getStatus() == ThingStatus.OFFLINE));
    }

    @Test
    void queuedNotificationsCannotOverwriteANewerCommandReadback() throws Exception {
        transport.observe = true;
        ApplianceHandler handler = initialize();
        awaitStatus(ThingStatus.ONLINE);
        ChannelUID power = channel(handler, "Power").getUID();
        transport.blockReadback = true;
        handler.handleCommand(power, OnOffType.ON);
        assertTrue(transport.entered.await(WAIT_SECONDS, TimeUnit.SECONDS));
        transport.listeners.get("/power/0").onUpdate(json("{\"value\":false}"));
        transport.release.countDown();
        awaitStatus(ThingStatus.ONLINE);
        executor.awaitWorkers();
        assertEquals(OnOffType.ON, states.get(power.getId()));
        verify(callback, never()).stateUpdated(eq(power), eq(OnOffType.OFF));
    }

    @Test
    void unreadOptionalLinksDoNotPreventHealthySubscriptionsFromStoppingPolling() throws Exception {
        transport.observe = true;
        transport.batch = json("""
                [{"href":"/power/0","rep":{"value":true}},
                 {"href":"/remotectrl/0","rep":{"value":true}},
                 {"href":"/optional/0"}]
                """);
        initialize();
        awaitStatus(ThingStatus.ONLINE);
        assertEquals(2, transport.listeners.size());
        assertTrue(transport.reads.contains("/optional/0"));
        transport.listeners.get("/power/0").onUpdate(json("{\"value\":true}"));
        transport.listeners.get("/remotectrl/0").onUpdate(json("{\"value\":true}"));
        awaitStatus(ThingStatus.ONLINE);
        awaitStatus(ThingStatus.ONLINE);
        assertFalse(executor.hasActivePoll());
    }

    @Test
    void notificationsDoNotPostponePollingForUnsupportedResources() throws Exception {
        transport.observe = true;
        transport.unobserved = "/remotectrl/0";
        initialize();
        awaitStatus(ThingStatus.ONLINE);
        Transport.Listener power = transport.listeners.get("/power/0");
        power.onUpdate(json("{\"value\":false}"));
        awaitStatus(ThingStatus.ONLINE);
        power.onUpdate(json("{\"value\":true}"));
        awaitStatus(ThingStatus.ONLINE);
        int reads = transport.reads.size();
        executor.firePoll();
        awaitStatus(ThingStatus.ONLINE);
        assertTrue(transport.reads.size() > reads);
        assertSame(power, transport.listeners.get("/power/0"));
    }

    @Test
    void communicationFailureInvalidatesObservationsAndRecoversWithNewOnes() throws Exception {
        transport.observe = true;
        ApplianceHandler handler = initialize();
        awaitStatus(ThingStatus.ONLINE);
        ChannelUID power = channel(handler, "Power").getUID();
        Transport.Listener previous = transport.listeners.get("/power/0");
        previous.onUpdate(json("{\"value\":true}"));
        transport.listeners.get("/remotectrl/0").onUpdate(json("{\"value\":true}"));
        awaitStatus(ThingStatus.ONLINE);
        awaitStatus(ThingStatus.ONLINE);
        executor.awaitWorkers();
        transport.failRead = true;
        handler.handleCommand(power, RefreshType.REFRESH);
        awaitStatus(ThingStatus.OFFLINE);
        assertTrue(transport.closed.await(WAIT_SECONDS, TimeUnit.SECONDS));
        previous.onUpdate(json("{\"value\":false}"));
        previous.onFailure();
        executor.awaitWorkers();
        verify(callback, never()).stateUpdated(eq(power), eq(OnOffType.OFF));
        transport.failRead = false;
        executor.firePoll();
        awaitStatus(ThingStatus.ONLINE);
        Transport.Listener recovered = transport.listeners.get("/power/0");
        assertNotSame(previous, recovered);
        recovered.onUpdate(json("{\"value\":false}"));
        awaitStatus(ThingStatus.ONLINE);
        assertEquals(OnOffType.OFF, states.get(power.getId()));
    }

    @Test
    void lateNotificationsAfterDisposalOrReinitializationAreIgnored() throws Exception {
        transport.observe = true;
        ApplianceHandler handler = initialize();
        awaitStatus(ThingStatus.ONLINE);
        Transport.Listener old = transport.listeners.get("/power/0");
        handler.initialize();
        awaitStatus(ThingStatus.ONLINE);
        executor.awaitWorkers();
        old.onUpdate(json("{\"value\":false}"));
        old.onFailure();
        executor.awaitWorkers();
        assertEquals(OnOffType.ON, states.get(channel(handler, "Power").getUID().getId()));
        Transport.Listener current = transport.listeners.get("/power/0");
        handler.dispose();
        current.onUpdate(json("{\"value\":false}"));
        current.onFailure();
        executor.awaitWorkers();
        assertEquals(OnOffType.ON, states.get(channel(handler, "Power").getUID().getId()));
        assertFalse(executor.hasActivePoll());
    }

    @Test
    void powerMeasurementsUseTheSystemChannelType() throws Exception {
        transport.identity = json("{\"di\":\"" + DEVICE_ID + "\"}");
        transport.batch = json(
                "[{\"href\":\"/energy/consumption/vs/0\",\"rep\":" + "{\"x.com.samsung.da.instantaneousPower\":93}}]");
        ApplianceHandler handler = initialize();
        awaitStatus(ThingStatus.ONLINE);

        Channel power = channel(handler, "Power Consumption");
        assertEquals("system:electric-power", power.getChannelTypeUID().toString());
        assertNull(descriptionProvider.getStateDescription(power, null, null).getPattern());
    }

    @Test
    void currentTemperaturesUseTheIndoorTemperatureSystemChannelType() throws Exception {
        transport.batch = json(
                "[{\"href\":\"/temperature/current/0\",\"rep\":" + "{\"temperature\":21,\"units\":\"C\"}}]");
        ApplianceHandler handler = initialize();
        awaitStatus(ThingStatus.ONLINE);

        Channel temperature = channel(handler, "Temperature");
        assertEquals("system:indoor-temperature", temperature.getChannelTypeUID().toString());
        assertEquals("temperature-current", temperature.getUID().getId());
    }

    @Test
    void publishesCapabilityDescriptionOnDynamicChannels() throws Exception {
        transport.identity = json("{\"di\":\"" + DEVICE_ID + "\",\"rt\":[\"oic.d.dehumidifier\"]}");
        transport.batch = json("""
                [{"href":"/mode/vs/0","rep":{"x.com.samsung.da.modes":["Auto"],
                 "x.com.samsung.da.supportedModes":["Auto","Sleep"],
                 "x.com.samsung.da.modesName":["Automatic","Sleep Mode"]}},
                 {"href":"/remotectrl/0","rep":{"value":true}}]
                """);
        ApplianceHandler handler = initialize();
        awaitStatus(ThingStatus.ONLINE);

        assertEquals("Operating Mode reported by the appliance. Supported command values (display names): "
                + "Auto (Automatic), Sleep (Sleep Mode).", channel(handler, "Operating Mode").getDescription());
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
    void refreshesChannelDescriptionsAndClearsStateDescriptionsOnDisposal() throws Exception {
        transport.batch = json("""
                [{"href":"/mode/vs/0","rep":{"x.com.samsung.da.modes":"Auto",
                  "x.com.samsung.da.supportedModes":["Auto","Sleep"]}}]
                """);
        ApplianceHandler handler = initialize();
        awaitStatus(ThingStatus.ONLINE);
        Channel mode = channel(handler, "Operating Mode");
        assertEquals(2, descriptionProvider.getStateDescription(mode, null, null).getOptions().size());
        transport.batch = json("""
                [{"href":"/mode/vs/0","rep":{"x.com.samsung.da.supportedModes":["Auto","Sleep","Turbo"]}}]
                """);
        handler.handleCommand(mode.getUID(), RefreshType.REFRESH);
        awaitStatus(ThingStatus.ONLINE);
        assertEquals(2, changes.size());
        Channel refreshedMode = channel(handler, "Operating Mode");
        assertEquals(mode.getUID(), refreshedMode.getUID());
        assertTrue(refreshedMode.getDescription().contains("Auto, Sleep, Turbo"));
        assertEquals(3, descriptionProvider.getStateDescription(mode, null, null).getOptions().size());
        handler.dispose();
        assertNull(descriptionProvider.getStateDescription(mode, null, null));
        assertNull(descriptionProvider.getCommandDescription(mode, null, null));
    }

    @Test
    void keepsPowerChannelIdWhenStandardResourceSupersedesVendorChannel() throws Exception {
        transport.batch = json("""
                [{"href":"/power/vs/0","rep":{"x.com.samsung.da.power":"On"}},
                 {"href":"/remotectrl/0","rep":{"value":true}}]
                """);
        ApplianceHandler handler = initialize();
        awaitStatus(ThingStatus.ONLINE);
        Channel vendor = channel(handler, "Power");
        assertNotNull(descriptionProvider.getStateDescription(vendor, null, null));
        transport.batch = json("[{\"href\":\"/power/0\",\"rep\":{\"value\":true}}]");
        handler.handleCommand(vendor.getUID(), RefreshType.REFRESH);
        awaitStatus(ThingStatus.ONLINE);
        Channel standard = channel(handler, "Power");
        assertEquals(vendor.getUID(), standard.getUID());
        assertNotNull(descriptionProvider.getStateDescription(standard, null, null));
    }

    @Test
    void disposalDuringRefreshCannotRestoreDescriptions() throws Exception {
        ApplianceHandler handler = initialize();
        awaitStatus(ThingStatus.ONLINE);
        Channel power = channel(handler, "Power");
        transport.block = true;
        handler.handleCommand(power.getUID(), RefreshType.REFRESH);
        assertTrue(transport.entered.await(WAIT_SECONDS, TimeUnit.SECONDS));
        assertTimeoutPreemptively(Duration.ofSeconds(2), handler::dispose);
        transport.release.countDown();
        executor.awaitWorkers();
        assertNull(descriptionProvider.getStateDescription(power, null, null));
        assertNull(descriptionProvider.getCommandDescription(power, null, null));
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
        final Map<String, Listener> listeners = new ConcurrentHashMap<>();
        final Map<String, AtomicInteger> observeAttempts = new ConcurrentHashMap<>();
        final AtomicInteger subscriptionsClosed = new AtomicInteger();
        volatile boolean observe;
        volatile String unobserved = "";
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
                    default -> {
                        for (JsonElement element : batch.getAsJsonArray()) {
                            JsonObject resource = element.getAsJsonObject();
                            if (path.equals(resource.get("href").getAsString()) && resource.has("rep")) {
                                yield resource.get("rep").deepCopy();
                            }
                        }
                        throw new IOException("Unexpected test resource");
                    }
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
        public Subscription observe(String href, Listener listener) throws IOException {
            observeAttempts.computeIfAbsent(href, ignored -> new AtomicInteger()).incrementAndGet();
            if (!observe || href.equals(unobserved)) {
                throw new IOException("Test appliance does not support Observe");
            }
            listeners.put(href, listener);
            return subscriptionsClosed::incrementAndGet;
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

    private record Poll(Runnable task, ScheduledFuture<?> future, long due) {
    }

    private static final class ControlledScheduler extends ScheduledThreadPoolExecutor {
        private final BlockingQueue<Poll> polls = new LinkedBlockingQueue<>();
        private final List<ScheduledFuture<?>> workers = new ArrayList<>();
        private final AtomicLong nanoTime = new AtomicLong();

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
                synchronized (workers) {
                    ScheduledFuture<?> future = super.schedule(command, delay, unit);
                    workers.add(future);
                    return future;
                }
            }
            ScheduledFuture<?> future = super.schedule(command, 1, TimeUnit.DAYS);
            polls.add(new Poll(command, future, nanoTime.get() + unit.toNanos(delay)));
            return future;
        }

        void firePoll() throws Exception {
            // Finish timer scheduling before advancing virtual time or canceling the next poll.
            awaitWorkers();
            Poll poll;
            do {
                poll = polls.poll(WAIT_SECONDS, TimeUnit.SECONDS);
                assertNotNull(poll);
            } while (poll.future().isCancelled());
            poll.future().cancel(false);
            nanoTime.accumulateAndGet(poll.due(), Math::max);
            execute(poll.task());
        }

        boolean hasActivePoll() {
            return polls.stream().anyMatch(poll -> !poll.future().isCancelled());
        }

        void awaitWorkers() throws Exception {
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(WAIT_SECONDS);
            while (true) {
                ScheduledFuture<?> future;
                synchronized (workers) {
                    if (workers.isEmpty()) {
                        return;
                    }
                    future = workers.removeFirst();
                }
                try {
                    future.get(Math.max(1, deadline - System.nanoTime()), TimeUnit.NANOSECONDS);
                } catch (CancellationException e) {
                    continue;
                }
            }
        }
    }
}
