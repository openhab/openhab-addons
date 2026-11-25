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
package org.openhab.binding.tibber.internal.pulse.discovery;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.openhab.binding.tibber.internal.TibberBindingConstants.TIBBER_THING_TYPE;
import static org.openhab.binding.tibber.internal.TibberPulseBindingConstants.*;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.openhab.binding.tibber.internal.pulse.handler.BridgeHandler;
import org.openhab.binding.tibber.internal.pulse.handler.BridgeHandler.StreamConnection;
import org.openhab.binding.tibber.internal.pulse.sml.SmlFrameBuilder;
import org.openhab.binding.tibber.internal.pulse.stream.StreamListener;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.config.discovery.DiscoveryListener;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.BridgeBuilder;

/**
 * Tests the scenarios of the {@code discovery} spec, "Meter discovery from the stream" and "Discovery never opens
 * streams", with the bridge handler fed through its stream listener.
 *
 * @author Simon Spielmann - Initial contribution
 */
@NonNullByDefault
@Timeout(30)
class MeterDiscoveryServiceTest {

    private static final long WAIT_MILLIS = 10_000;
    private static final String DEVICE_A = "aaaaaaaaaaaaaaaa";
    private static final String PASSWORD = "s3cret-Pw";

    private final AtomicInteger opened = new AtomicInteger();
    private final List<StreamListener> listeners = new CopyOnWriteArrayList<>();
    private final BlockingQueue<DiscoveryResult> results = new LinkedBlockingQueue<>();
    private @Nullable SchedulerExposingBridgeHandler handler;
    private @Nullable MeterDiscoveryService service;
    private @Nullable Bridge bridge;

    @BeforeEach
    void setUp() {
        Bridge b = BridgeBuilder.create(THING_TYPE_PULSE_BRIDGE, new ThingUID(THING_TYPE_PULSE_BRIDGE, "b1"))
                .withConfiguration(
                        new Configuration(java.util.Map.of("hostname", "bridge.example", "password", PASSWORD)))
                .build();
        SchedulerExposingBridgeHandler h = new SchedulerExposingBridgeHandler(b, (host, password, name, listener) -> {
            opened.incrementAndGet();
            listeners.add(listener);
            return new StreamConnection() {
                @Override
                public void start() {
                }

                @Override
                public void stop() {
                }
            };
        });
        h.setCallback(mock(ThingHandlerCallback.class));
        b.setHandler(h);
        h.initialize();
        listeners.get(0).onStreamActive();
        MeterDiscoveryService s = new MeterDiscoveryService();
        s.setThingHandler(h);
        s.addDiscoveryListener(new DiscoveryListener() {
            @Override
            public void thingDiscovered(DiscoveryService source, DiscoveryResult result) {
                results.add(result);
            }

            @Override
            public void thingRemoved(DiscoveryService source, ThingUID thingUID) {
            }

            @Override
            public @Nullable Collection<ThingUID> removeOlderResults(DiscoveryService source, Instant timestamp,
                    @Nullable Collection<ThingTypeUID> thingTypeUIDs, @Nullable ThingUID bridgeUID) {
                return null;
            }
        });
        bridge = b;
        handler = h;
        service = s;
    }

    @AfterEach
    void tearDown() {
        MeterDiscoveryService s = service;
        if (s != null) {
            s.deactivate();
            s.dispose();
        }
        BridgeHandler h = handler;
        if (h != null) {
            h.dispose();
        }
    }

    private static byte[] message(String deviceId, long watts) {
        byte[] frame = new SmlFrameBuilder().signed(SmlFrameBuilder.POWER, SmlFrameBuilder.UNIT_W, 0, watts, 8)
                .unsigned(SmlFrameBuilder.ENERGY_IMPORT, SmlFrameBuilder.UNIT_WH, -1, BigInteger.valueOf(1), 8).build();
        return SmlFrameBuilder.concat(("<device:" + deviceId + " topic:\"sml\">").getBytes(StandardCharsets.US_ASCII),
                frame);
    }

    private StreamListener stream() {
        return listeners.get(listeners.size() - 1);
    }

    private MeterDiscoveryService service() {
        MeterDiscoveryService s = service;
        assertNotNull(s);
        return s;
    }

    @Test
    void whenTheServiceIsInitializedThenItOffersEachNewDeviceOnTheStreamOfTheBridge() throws Exception {
        service().initialize();

        stream().onMessage(message(DEVICE_A, 100));

        DiscoveryResult result = results.poll(WAIT_MILLIS, TimeUnit.MILLISECONDS);
        assertNotNull(result);
        assertEquals(new ThingUID(THING_TYPE_PULSE_METER, bridge().getUID(), DEVICE_A), result.getThingUID());
        assertEquals(bridge().getUID(), result.getBridgeUID());
        assertEquals(DEVICE_A, result.getProperties().get("deviceId"));
        assertEquals("deviceId", result.getRepresentationProperty());
        assertEquals(THING_TYPE_PULSE_METER, result.getThingTypeUID());
    }

    private Bridge bridge() {
        Bridge b = bridge;
        assertNotNull(b);
        return b;
    }

    /** Exposes the scheduler that runs the discovery callbacks, so that a test can hold them back. */
    private static final class SchedulerExposingBridgeHandler extends BridgeHandler {

        SchedulerExposingBridgeHandler(Bridge bridge, StreamConnectionFactory factory) {
            super(bridge, factory);
        }

        ScheduledExecutorService scheduler() {
            return scheduler;
        }
    }

    /**
     * Queues far more waiting tasks than the scheduler runs at once (the pool of core has 5 threads unless configured).
     * The pool takes tasks in submission order, so a task queued after these does not run before the latch is released.
     */
    private CountDownLatch holdBackScheduler() {
        SchedulerExposingBridgeHandler h = handler;
        assertNotNull(h);
        CountDownLatch release = new CountDownLatch(1);
        for (int i = 0; i < 100; i++) {
            h.scheduler().execute(() -> {
                try {
                    release.await(WAIT_MILLIS, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
        return release;
    }

    @Test
    void whenManyReadingsArriveBeforeAcceptanceThenOnlyOneResultIsCreated() throws Exception {
        service().initialize();

        for (int i = 0; i < 100; i++) {
            stream().onMessage(message(DEVICE_A, i));
        }

        assertNotNull(results.poll(WAIT_MILLIS, TimeUnit.MILLISECONDS));
        assertNull(results.poll(300, TimeUnit.MILLISECONDS));
    }

    @Test
    void whenAMessageIsIgnoredOrDroppedThenNothingIsOffered() throws Exception {
        service().initialize();
        byte[] valid = message(DEVICE_A, 1);

        stream().onMessage("<device:aaaaaaaaaaaaaaaa topic:\"reading\">".getBytes(StandardCharsets.US_ASCII));
        stream().onMessage(java.util.Arrays.copyOf(valid, valid.length - 20));
        stream().onMessage("garbage".getBytes(StandardCharsets.US_ASCII));

        assertNull(results.poll(300, TimeUnit.MILLISECONDS));
    }

    @Test
    void whenAScanIsStartedThenTheDevicesSeenSoFarAreOffered() throws Exception {
        stream().onMessage(message(DEVICE_A, 1));
        stream().onMessage(message("bbbbbbbbbbbbbbbb", 1));

        service().startScan(null);

        List<@Nullable Object> ids = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            DiscoveryResult result = results.poll(WAIT_MILLIS, TimeUnit.MILLISECONDS);
            assertNotNull(result);
            ids.add(result.getProperties().get("deviceId"));
        }
        assertTrue(ids.containsAll(List.of(DEVICE_A, "bbbbbbbbbbbbbbbb")), ids.toString());
    }

    @Test
    void whenTheServiceIsDisposedThenLaterDevicesAreNotOffered() throws Exception {
        service().initialize();
        service().dispose();

        stream().onMessage(message(DEVICE_A, 1));

        assertNull(results.poll(300, TimeUnit.MILLISECONDS));
    }

    @Test
    void whenBackgroundDiscoveryIsDisabledWhileACallbackIsQueuedThenTheDeviceIsNotOfferedUntilAScan() throws Exception {
        service().initialize();
        CountDownLatch release = holdBackScheduler();
        try {
            stream().onMessage(message(DEVICE_A, 1));
            service().modified(Map.of(DiscoveryService.CONFIG_PROPERTY_BACKGROUND_DISCOVERY, false));
        } finally {
            release.countDown();
        }

        assertNull(results.poll(300, TimeUnit.MILLISECONDS),
                "a callback queued before the stop must not reach the inbox");

        service().startScan(null);
        DiscoveryResult result = results.poll(WAIT_MILLIS, TimeUnit.MILLISECONDS);
        assertNotNull(result, "an explicit scan still offers the device");
        assertEquals(DEVICE_A, result.getProperties().get("deviceId"));
    }

    @Test
    void whenDiscoveryRunsThenItNeverOpensAConnection() throws Exception {
        int before = opened.get();

        service().initialize();
        stream().onMessage(message(DEVICE_A, 1));
        service().startScan(null);
        assertNotNull(results.poll(WAIT_MILLIS, TimeUnit.MILLISECONDS));

        assertEquals(before, opened.get(), "the only stream is the one of the bridge handler");
        assertEquals(1, opened.get());
    }

    @Test
    void whenAResultIsOfferedThenItCarriesNoPassword() throws Exception {
        service().initialize();
        stream().onMessage(message(DEVICE_A, 1));

        DiscoveryResult result = results.poll(WAIT_MILLIS, TimeUnit.MILLISECONDS);

        assertNotNull(result);
        assertFalse(result.toString().contains(PASSWORD));
        assertFalse(result.getLabel().contains(PASSWORD));
        assertTrue(result.getProperties().values().stream().noneMatch(v -> String.valueOf(v).contains(PASSWORD)));
    }

    // discovery: Discovery offers only Pulse thing types

    @Test
    void whenTheSupportedThingTypesAreListedThenItIsExactlyThePulseMeterOfTheTibberBinding() {
        assertEquals(Set.of(new ThingTypeUID("tibber", "pulse-meter")), service().getSupportedThingTypes());
    }

    @Test
    void whenADeviceIsOfferedThenNoEntryForTheCloudThingTypeIsCreated() throws Exception {
        service().initialize();

        stream().onMessage(message(DEVICE_A, 100));

        DiscoveryResult result = results.poll(WAIT_MILLIS, TimeUnit.MILLISECONDS);
        assertNotNull(result);
        assertNotEquals(TIBBER_THING_TYPE, result.getThingTypeUID());
        assertTrue(results.isEmpty());
        assertFalse(service().getSupportedThingTypes().contains(TIBBER_THING_TYPE));
    }

    @Test
    void whenTheHandlerListsItsServicesThenTheDiscoveryServiceIsAmongThem() {
        BridgeHandler h = handler;
        assertNotNull(h);

        assertTrue(h.getServices().contains(MeterDiscoveryService.class));
    }
}
