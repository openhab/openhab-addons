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
package org.openhab.binding.tibber.internal.pulse.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.openhab.binding.tibber.internal.TibberPulseBindingConstants.*;
import static org.openhab.binding.tibber.internal.pulse.handler.HandlerSupport.*;
import static org.openhab.binding.tibber.internal.pulse.sml.SmlFrameBuilder.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.openhab.binding.tibber.internal.pulse.handler.BridgeHandler.StreamConnection;
import org.openhab.binding.tibber.internal.pulse.handler.HandlerSupport.Recorder;
import org.openhab.binding.tibber.internal.pulse.handler.HandlerSupport.TestClock;
import org.openhab.binding.tibber.internal.pulse.sml.SmlFrameBuilder;
import org.openhab.binding.tibber.internal.pulse.sml.SmlReading;
import org.openhab.binding.tibber.internal.pulse.stream.StreamListener;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.internal.BridgeImpl;
import org.openhab.core.types.UnDefType;

/**
 * QA probes for the rule "a reading is applied only if the device id of the reading equals the device id the meter is
 * configured for at the moment of application" ({@code thing-lifecycle}, "Meter identification"): every order of
 * dispose, initialize, invalid configuration, and a pre-filter that is out of date, each with a reading in flight.
 * A dropped reading must have no effect at all: no channel state, no status, no property, no stale timer, and it must
 * not move the stale clock.
 *
 * @author Simon Spielmann - Initial contribution
 */
@NonNullByDefault
@Timeout(60)
class MeterReconfigurationRaceTest {

    private static final String DEVICE_C = "cccccccccccccccc";
    private static final long SECOND = TimeUnit.SECONDS.toNanos(1);
    private static final long WAIT_SECONDS = 20;

    private final Bridge bridge = bridge("b1", "bridge.example", PASSWORD);
    private final AtomicLong nanos = new AtomicLong(1_000 * SECOND);
    private final TestClock wall = new TestClock();
    private final List<MeterHandler> started = new ArrayList<>();
    private final List<BridgeHandler> bridges = new ArrayList<>();

    @AfterEach
    void tearDown() {
        started.forEach(MeterHandler::dispose);
        bridges.forEach(BridgeHandler::dispose);
    }

    /** Writes the configuration the way the framework does it: dispose, new configuration, initialize. */
    private static class Reconfigurable extends MeterHandler {
        Reconfigurable(Thing thing, java.util.function.LongSupplier nanos, java.time.Clock wall) {
            super(thing, nanos, wall);
        }

        void reconfigure(Map<String, Object> changes) {
            dispose();
            Configuration configuration = editConfiguration();
            changes.forEach(configuration::put);
            updateConfiguration(configuration);
            initialize();
        }
    }

    private static final class Recorded {
        final Reconfigurable handler;
        final Recorder recorder;
        final Thing thing;

        Recorded(Reconfigurable handler, Recorder recorder, Thing thing) {
            this.handler = handler;
            this.recorder = recorder;
            this.thing = thing;
        }
    }

    private Recorded start(String id, String deviceId) {
        Thing thing = meter(bridge, id, deviceId, null);
        setStatus(bridge, ThingStatus.ONLINE);
        Recorder recorder = new Recorder(thing, bridge);
        Reconfigurable handler = new Reconfigurable(thing, nanos::get, wall);
        handler.setCallback(recorder.callback);
        thing.setHandler(handler);
        ((BridgeImpl) bridge).addThing(thing);
        started.add(handler);
        handler.initialize();
        return new Recorded(handler, recorder, thing);
    }

    private static SmlReading late() {
        return new SmlReading(values(CHANNEL_POWER, watts("900"), CHANNEL_ENERGY_IMPORT, kilowattHours("1001")),
                SYNTHETIC_METER_ID);
    }

    private static void assertNoEffect(Recorded m, String why) {
        assertTrue(m.recorder.updates.isEmpty(), () -> why + ": channel updates " + m.recorder.updates);
        assertTrue(m.recorder.statuses.isEmpty(), () -> why + ": status updates " + m.recorder.statuses);
        assertNull(m.thing.getProperties().get(PROPERTY_METER_ID), why + ": property meterId");
    }

    private Recorded onlineMeterOfA() {
        Recorded m = start("m1", DEVICE_A);
        m.handler.onReading(DEVICE_A, new SmlReading(
                values(CHANNEL_POWER, watts("1200"), CHANNEL_ENERGY_IMPORT, kilowattHours("1000")), null));
        assertEquals(ThingStatus.ONLINE, m.thing.getStatus());
        return m;
    }

    // thing-lifecycle: Meter identification (probe: dispose with a reading in flight)
    @Test
    void readingAfterDisposeIsDroppedWithoutAnyEffectAndArmsNoTimer() {
        Recorded m = onlineMeterOfA();
        m.handler.dispose();
        m.recorder.clear();

        m.handler.onReading(DEVICE_A, late());

        assertNoEffect(m, "after dispose");
        assertFalse(m.handler.isStaleTimerArmed());
    }

    // probe: the framework runs dispose() and initialize() with the same id; a reading between them is lost, the next
    // one is applied
    @Test
    void readingBetweenDisposeAndInitializeOfTheSameIdIsDroppedAndTheNextReadingIsApplied() {
        Recorded m = onlineMeterOfA();
        m.handler.dispose();
        m.recorder.clear();
        m.handler.onReading(DEVICE_A, late());
        assertNoEffect(m, "between dispose and initialize");

        m.handler.initialize();
        assertEquals(ThingStatus.UNKNOWN, m.thing.getStatus());
        m.handler.onReading(DEVICE_A, late());

        assertEquals(ThingStatus.ONLINE, m.thing.getStatus());
        assertEquals(watts("900"), m.recorder.lastState(CHANNEL_POWER));
        assertEquals(SYNTHETIC_METER_ID, m.thing.getProperties().get(PROPERTY_METER_ID));
    }

    // probe: invalid device id with a reading of the previous id in flight
    @Test
    void readingOfThePreviousDeviceIsDroppedWhenTheNewDeviceIdIsInvalidAndAppliedAgainAfterTheFix() {
        Recorded m = onlineMeterOfA();

        m.handler.reconfigure(Map.of("deviceId", "not-a-device-id"));
        assertEquals(ThingStatus.OFFLINE, m.thing.getStatus());
        assertEquals(ThingStatusDetail.CONFIGURATION_ERROR, m.thing.getStatusInfo().getStatusDetail());
        m.recorder.clear();
        m.handler.onReading(DEVICE_A, late());

        assertNoEffect(m, "invalid device id");
        assertFalse(m.handler.isStaleTimerArmed());
        assertEquals(ThingStatus.OFFLINE, m.thing.getStatus());
        assertFalse(m.handler.matches(DEVICE_A));

        m.handler.reconfigure(Map.of("deviceId", DEVICE_A));
        m.handler.onReading(DEVICE_A, late());
        assertEquals(ThingStatus.ONLINE, m.thing.getStatus());
        assertEquals(watts("900"), m.recorder.lastState(CHANNEL_POWER));
    }

    // probe: invalid stale timeout with a valid device id: the configuration is invalid as a whole
    @Test
    void readingIsDroppedWhileTheStaleTimeoutIsInvalid() {
        Recorded m = onlineMeterOfA();

        m.handler.reconfigure(Map.of("staleTimeout", 5));
        assertEquals(ThingStatus.OFFLINE, m.thing.getStatus());
        m.recorder.clear();
        m.handler.onReading(DEVICE_A, late());

        assertNoEffect(m, "invalid stale timeout");
        assertFalse(m.handler.isStaleTimerArmed());
    }

    // probe: a dropped reading must not move the stale clock of the new device
    @Test
    void droppedReadingOfThePreviousDeviceDoesNotKeepTheNewDeviceFromGoingStale() {
        Recorded m = onlineMeterOfA();
        m.handler.reconfigure(Map.of("deviceId", DEVICE_B));
        assertEquals(ThingStatus.UNKNOWN, m.thing.getStatus());

        nanos.addAndGet(50 * SECOND);
        m.handler.onReading(DEVICE_A, late());
        nanos.addAndGet(20 * SECOND);
        m.handler.checkStale();

        assertEquals(ThingStatus.OFFLINE, m.thing.getStatus());
        assertEquals(ThingStatusDetail.COMMUNICATION_ERROR, m.thing.getStatusInfo().getStatusDetail());
        assertEquals(UnDefType.UNDEF, m.recorder.lastState(CHANNEL_POWER));
        assertEquals(UnDefType.UNDEF, m.recorder.lastState(CHANNEL_ENERGY_IMPORT));
    }

    // contract: the bridge hands over the lower-case id of the header; the meter does not normalize it
    @Test
    void readingWithAnUpperCaseDeviceIdArgumentIsDroppedBecauseTheContractIsLowerCase() {
        Recorded m = start("m1", DEVICE_A.toUpperCase(java.util.Locale.ROOT));
        m.recorder.clear();

        m.handler.onReading(DEVICE_A.toUpperCase(java.util.Locale.ROOT), late());
        assertNoEffect(m, "upper-case argument");

        m.handler.onReading(DEVICE_A, late());
        assertEquals(ThingStatus.ONLINE, m.thing.getStatus());
    }

    /** A meter whose pre-filter is out of date: it still claims every device, as if it had been found before. */
    private static final class StalePreFilter extends Reconfigurable {
        StalePreFilter(Thing thing, java.util.function.LongSupplier nanos, java.time.Clock wall) {
            super(thing, nanos, wall);
        }

        @Override
        boolean matches(String device) {
            return true;
        }
    }

    private StreamListener startBridge() {
        List<StreamListener> listeners = new CopyOnWriteArrayList<>();
        BridgeHandler handler = new BridgeHandler(bridge, (host, password, name, listener) -> {
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
        handler.setCallback(new Recorder(bridge, null).callback);
        bridge.setHandler(handler);
        bridges.add(handler);
        handler.initialize();
        listeners.get(0).onStreamActive();
        return listeners.get(0);
    }

    // probe: pre-filter and onReading together, twin meters; the pre-filter says yes for both, onReading decides
    @Test
    void whenThePreFilterIsOutOfDateThenTheBridgeStillAppliesTheReadingOnlyToTheMeterOfThatDevice() {
        StreamListener listener = startBridge();
        Thing staleThing = meter(bridge, "m1", DEVICE_B, null);
        Recorder staleRecorder = new Recorder(staleThing, bridge);
        StalePreFilter stale = new StalePreFilter(staleThing, nanos::get, wall);
        stale.setCallback(staleRecorder.callback);
        staleThing.setHandler(stale);
        ((BridgeImpl) bridge).addThing(staleThing);
        started.add(stale);
        stale.initialize();
        Recorded twin = start("m2", DEVICE_A);
        staleRecorder.clear();
        twin.recorder.clear();
        byte[] frame = new SmlFrameBuilder().signed(POWER, UNIT_W, 0, 321, 8).build();

        boolean valid = listener.onMessage(message(DEVICE_A, frame));

        assertTrue(valid);
        assertTrue(staleRecorder.updates.isEmpty(),
                () -> "updates of the meter of the other device: " + staleRecorder.updates);
        assertTrue(staleRecorder.statuses.isEmpty());
        assertEquals(ThingStatus.UNKNOWN, staleThing.getStatus());
        assertEquals(watts("321"), twin.recorder.lastState(CHANNEL_POWER));
        assertEquals(ThingStatus.ONLINE, twin.thing.getStatus());
    }

    // probe: a meter that was removed while the bridge iterates (handler disposed, pre-filter out of date)
    @Test
    void whenAMeterIsDisposedAfterThePreFilterFoundItThenTheReadingIsDropped() {
        StreamListener listener = startBridge();
        Thing removedThing = meter(bridge, "m1", DEVICE_A, null);
        Recorder removedRecorder = new Recorder(removedThing, bridge);
        StalePreFilter removed = new StalePreFilter(removedThing, nanos::get, wall);
        removed.setCallback(removedRecorder.callback);
        removedThing.setHandler(removed);
        ((BridgeImpl) bridge).addThing(removedThing);
        started.add(removed);
        removed.initialize();
        removed.dispose();
        removedRecorder.clear();

        boolean valid = listener
                .onMessage(message(DEVICE_A, new SmlFrameBuilder().signed(POWER, UNIT_W, 0, 321, 8).build()));

        assertTrue(valid);
        assertTrue(removedRecorder.updates.isEmpty());
        assertTrue(removedRecorder.statuses.isEmpty());
        assertFalse(removed.isStaleTimerArmed());
    }

    // probe: the header of a message may carry upper-case digits; the bridge lower-cases them before the hand-over
    @Test
    void whenTheHeaderCarriesAnUpperCaseDeviceIdThenTheMeterOfThatDeviceStillAppliesTheReading() {
        StreamListener listener = startBridge();
        Recorded m = start("m1", DEVICE_A);
        m.recorder.clear();

        listener.onMessage(message(DEVICE_A.toUpperCase(java.util.Locale.ROOT),
                new SmlFrameBuilder().signed(POWER, UNIT_W, 0, 321, 8).build()));

        assertEquals(watts("321"), m.recorder.lastState(CHANNEL_POWER));
    }

    // probe: reconfiguration to other ids over and over while readings of a device the meter is never configured for
    // keep arriving: nothing of them may ever be published. No timing assertion: the outcome must hold for every
    // interleaving.
    @Test
    void whenTheMeterIsReconfiguredRepeatedlyThenReadingsOfAForeignDeviceNeverReachAChannelOrTheStatus()
            throws Exception {
        Thing thing = meter(bridge, "m1", DEVICE_B, null);
        setStatus(bridge, ThingStatus.ONLINE);
        AtomicLong foreign = new AtomicLong();
        AtomicLong online = new AtomicLong();
        ThingHandlerCallback callback = mock(ThingHandlerCallback.class, withSettings().stubOnly());
        doAnswer(invocation -> {
            Object state = invocation.getArgument(1);
            if (watts("900").equals(state) || kilowattHours("1001").equals(state)) {
                foreign.incrementAndGet();
            }
            return null;
        }).when(callback).stateUpdated(any(), any());
        doAnswer(invocation -> {
            org.openhab.core.thing.ThingStatusInfo info = invocation.getArgument(1);
            if (info.getStatus() == ThingStatus.ONLINE) {
                online.incrementAndGet();
            }
            return null;
        }).when(callback).statusUpdated(any(), any());
        when(callback.getBridge(bridge.getUID())).thenReturn(bridge);
        Reconfigurable handler = new Reconfigurable(thing, nanos::get, wall);
        handler.setCallback(callback);
        thing.setHandler(handler);
        started.add(handler);
        handler.initialize();
        SmlReading reading = late();
        AtomicBoolean stop = new AtomicBoolean();
        CountDownLatch running = new CountDownLatch(1);
        Thread reader = new Thread(() -> {
            running.countDown();
            while (!stop.get()) {
                handler.onReading(DEVICE_A, reading);
            }
        }, "qa-reader");
        reader.setDaemon(true);
        try {
            reader.start();
            assertTrue(running.await(WAIT_SECONDS, TimeUnit.SECONDS));
            for (int i = 0; i < 400; i++) {
                handler.reconfigure(Map.of("deviceId", i % 2 == 0 ? DEVICE_C : DEVICE_B));
            }
        } finally {
            stop.set(true);
            reader.join(TimeUnit.SECONDS.toMillis(WAIT_SECONDS));
        }

        assertFalse(reader.isAlive(), "the reader did not stop");
        assertEquals(0, foreign.get(), "values of the foreign device were published");
        assertEquals(0, online.get(), "the meter went ONLINE without a reading of its own device");
        assertNull(thing.getProperties().get(PROPERTY_METER_ID));
    }
}
