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
package org.openhab.binding.tibberpulselocal.internal.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.openhab.binding.tibberpulselocal.internal.TibberPulseLocalBindingConstants.*;
import static org.openhab.binding.tibberpulselocal.internal.handler.HandlerSupport.*;
import static org.openhab.binding.tibberpulselocal.internal.sml.SmlFrameBuilder.*;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.openhab.binding.tibberpulselocal.internal.handler.BridgeHandler.StreamConnection;
import org.openhab.binding.tibberpulselocal.internal.handler.HandlerSupport.Recorder;
import org.openhab.binding.tibberpulselocal.internal.sml.SmlFrameBuilder;
import org.openhab.binding.tibberpulselocal.internal.sml.SmlReading;
import org.openhab.binding.tibberpulselocal.internal.stream.StreamListener;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.internal.BridgeImpl;
import org.openhab.core.types.RefreshType;

/**
 * Failure paths and races of the handlers that the scenario tests do not reach (QA review, stage 5): resources of a
 * failed initialization, two meter things for one device, and concurrent use of one meter handler.
 *
 * @author Simon Spielmann - Initial contribution
 */
@NonNullByDefault
@Timeout(60)
class HandlerRobustnessTest {

    private final List<MeterHandler> meters = new ArrayList<>();
    private final List<BridgeHandler> bridges = new ArrayList<>();

    @AfterEach
    void tearDown() {
        meters.forEach(MeterHandler::dispose);
        bridges.forEach(BridgeHandler::dispose);
    }

    private static final class QuietConnection implements StreamConnection {
        @Override
        public void start() {
        }

        @Override
        public void stop() {
        }
    }

    private MeterHandler addMeter(Bridge bridge, String id, String deviceId, Recorder[] out) {
        Thing thing = meter(bridge, id, deviceId, null);
        Recorder recorder = new Recorder(thing, bridge);
        MeterHandler handler = new MeterHandler(thing);
        handler.setCallback(recorder.callback);
        thing.setHandler(handler);
        ((BridgeImpl) bridge).addThing(thing);
        meters.add(handler);
        out[0] = recorder;
        handler.initialize();
        return handler;
    }

    @Test
    void whenTheStreamCannotBeStartedThenItIsStoppedAgain() {
        AtomicLong stops = new AtomicLong();
        Bridge bridge = bridge("b1", "bridge.example", PASSWORD);
        BridgeHandler handler = new BridgeHandler(bridge, (host, password, name, listener) -> {
            return new StreamConnection() {
                @Override
                public void start() {
                    throw new IllegalStateException("no thread");
                }

                @Override
                public void stop() {
                    stops.incrementAndGet();
                }
            };
        });
        handler.setCallback(new Recorder(bridge, null).callback);
        bridge.setHandler(handler);
        bridges.add(handler);

        assertThrows(IllegalStateException.class, handler::initialize);

        // The framework does not call dispose() for a handler whose initialize() threw.
        assertEquals(1, stops.get());
    }

    @Test
    void whenTwoMeterThingsUseTheSameDeviceIdThenBothReceiveTheReading() {
        List<StreamListener> listeners = new CopyOnWriteArrayList<>();
        Bridge bridge = bridge("b1", "bridge.example", PASSWORD);
        BridgeHandler handler = new BridgeHandler(bridge, (host, password, name, listener) -> {
            listeners.add(listener);
            return new QuietConnection();
        });
        handler.setCallback(new Recorder(bridge, null).callback);
        bridge.setHandler(handler);
        bridges.add(handler);
        handler.initialize();
        listeners.get(0).onStreamActive();
        Recorder[] first = new Recorder[1];
        Recorder[] second = new Recorder[1];
        addMeter(bridge, "m1", DEVICE_A, first);
        addMeter(bridge, "m2", DEVICE_A.toUpperCase(java.util.Locale.ROOT), second);
        byte[] frame = new SmlFrameBuilder().signed(POWER, UNIT_W, 0, 321, 8).build();

        boolean valid = listeners.get(0).onMessage(message(DEVICE_A, frame));

        assertTrue(valid);
        assertEquals(watts("321"), first[0].lastState(CHANNEL_POWER));
        assertEquals(watts("321"), second[0].lastState(CHANNEL_POWER));
    }

    @Test
    void whenTheBridgeIsInitializedAndDisposedFiveHundredTimesThenEveryStartHasItsStop() {
        AtomicLong starts = new AtomicLong();
        AtomicLong stops = new AtomicLong();
        Bridge bridge = bridge("b1", "bridge.example", PASSWORD);
        BridgeHandler handler = new BridgeHandler(bridge, (host, password, name, listener) -> {
            return new StreamConnection() {
                @Override
                public void start() {
                    starts.incrementAndGet();
                }

                @Override
                public void stop() {
                    stops.incrementAndGet();
                }
            };
        });
        handler.setCallback(new Recorder(bridge, null).callback);
        bridge.setHandler(handler);

        for (int i = 0; i < 500; i++) {
            handler.initialize();
            handler.dispose();
        }

        assertEquals(500, starts.get());
        assertEquals(500, stops.get());
    }

    @Test
    void whenReadingsStatusChangesAndStaleChecksRunConcurrentlyWithRestartsThenNothingDeadlocksOrFails()
            throws Exception {
        Bridge bridge = bridge("b1", "bridge.example", PASSWORD);
        setStatus(bridge, ThingStatus.ONLINE);
        Thing thing = meter(bridge, "m1", DEVICE_A, null);
        // A callback that only counts: the recording callback of the other tests would grow without bound here.
        AtomicLong statuses = new AtomicLong();
        AtomicLong updates = new AtomicLong();
        ThingHandlerCallback callback = mock(ThingHandlerCallback.class, withSettings().stubOnly());
        doAnswer(invocation -> {
            statuses.incrementAndGet();
            return null;
        }).when(callback).statusUpdated(any(), any());
        doAnswer(invocation -> {
            updates.incrementAndGet();
            return null;
        }).when(callback).stateUpdated(any(), any());
        when(callback.getBridge(bridge.getUID())).thenReturn(bridge);
        MeterHandler handler = new MeterHandler(thing);
        handler.setCallback(callback);
        thing.setHandler(handler);
        meters.add(handler);
        handler.initialize();
        SmlReading reading = new SmlReading(values(CHANNEL_POWER, watts("5"), CHANNEL_ENERGY_IMPORT,
                kilowattHours("1.5"), CHANNEL_FREQUENCY, hertz("50.0")), null);
        List<Throwable> failures = new CopyOnWriteArrayList<>();
        CountDownLatch go = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(5);
        try {
            List<Future<?>> tasks = new ArrayList<>();
            Runnable[] jobs = { () -> handler.onReading(reading), handler::checkStale,
                    () -> handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_POWER), RefreshType.REFRESH),
                    () -> {
                        bridge.setStatusInfo(info(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR));
                        handler.bridgeStatusChanged(info(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR));
                    }, () -> {
                        bridge.setStatusInfo(info(ThingStatus.ONLINE, ThingStatusDetail.NONE));
                        handler.bridgeStatusChanged(info(ThingStatus.ONLINE, ThingStatusDetail.NONE));
                    } };
            for (Runnable job : jobs) {
                tasks.add(pool.submit(() -> {
                    go.await();
                    for (int i = 0; i < 3_000; i++) {
                        try {
                            job.run();
                        } catch (RuntimeException e) {
                            failures.add(e);
                            return null;
                        }
                    }
                    return null;
                }));
            }
            go.countDown();
            // The framework restarts a meter on a configuration change while the stream keeps delivering.
            for (int i = 0; i < 300; i++) {
                handler.dispose();
                handler.initialize();
            }
            for (Future<?> task : tasks) {
                task.get(20, TimeUnit.SECONDS);
            }
        } catch (TimeoutException e) {
            long[] deadlocked = ManagementFactory.getThreadMXBean().findDeadlockedThreads();
            fail("a worker did not finish; deadlocked threads: " + (deadlocked == null ? 0 : deadlocked.length));
        } finally {
            pool.shutdownNow();
        }
        assertTrue(failures.isEmpty(), () -> "failures: " + failures);

        handler.dispose();
        long statusCount = statuses.get();
        long updateCount = updates.get();
        handler.onReading(reading);
        handler.checkStale();
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_POWER), RefreshType.REFRESH);
        handler.bridgeStatusChanged(info(ThingStatus.ONLINE, ThingStatusDetail.NONE));

        assertFalse(handler.isStaleTimerArmed(), "no stale timer after dispose");
        assertEquals(statusCount, statuses.get(), "no status after dispose");
        assertEquals(updateCount, updates.get(), "no channel state after dispose");
    }
}
