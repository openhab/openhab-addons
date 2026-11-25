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
package org.openhab.binding.tibber.internal.pulse.stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.openhab.binding.tibber.internal.TibberPulseBindingConstants.*;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tibber.internal.pulse.handler.BridgeHandler;
import org.openhab.binding.tibber.internal.pulse.handler.BridgeHandler.StreamConnection;
import org.openhab.binding.tibber.internal.pulse.handler.BridgeHandler.StreamConnectionFactory;
import org.openhab.binding.tibber.internal.pulse.handler.MeterHandler;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.BridgeBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.types.State;

/**
 * A bridge handler with one meter handler below it, wired to callbacks that record what they publish and that pass
 * the bridge status on to the meter the way the framework does. The stream client behind the bridge handler is
 * created by the factory the test supplies, usually one that points at a {@link FakeBridge}.
 *
 * @author Simon Spielmann - Initial contribution
 */
@NonNullByDefault
final class HandlerHarness {

    final Bridge bridge;
    final BridgeHandler bridgeHandler;
    final Thing meter;
    final MeterHandler meterHandler;
    final BlockingQueue<ThingStatusInfo> bridgeStatuses = new LinkedBlockingQueue<>();
    final BlockingQueue<ThingStatusInfo> meterStatuses = new LinkedBlockingQueue<>();
    private final Map<String, State> meterStates = new ConcurrentHashMap<>();

    HandlerHarness(String hostname, String password, String deviceId, StreamConnectionFactory factory) {
        ThingUID bridgeUid = new ThingUID(THING_TYPE_PULSE_BRIDGE, "stream");
        bridge = BridgeBuilder.create(THING_TYPE_PULSE_BRIDGE, bridgeUid)
                .withConfiguration(new Configuration(Map.of("hostname", hostname, "password", password))).build();
        meter = ThingBuilder.create(THING_TYPE_PULSE_METER, new ThingUID(THING_TYPE_PULSE_METER, bridgeUid, "m1"))
                .withBridge(bridgeUid).withConfiguration(new Configuration(Map.of("deviceId", deviceId))).build();
        ((org.openhab.core.thing.internal.BridgeImpl) bridge).addThing(meter);

        ThingHandlerCallback bridgeCallback = mock(ThingHandlerCallback.class);
        doAnswer(invocation -> {
            ThingStatusInfo info = invocation.getArgument(1);
            bridge.setStatusInfo(info);
            bridgeStatuses.add(info);
            // The framework tells the children after every status change of the bridge.
            for (Thing child : bridge.getThings()) {
                ThingHandler childHandler = child.getHandler();
                if (childHandler != null) {
                    childHandler.bridgeStatusChanged(info);
                }
            }
            return null;
        }).when(bridgeCallback).statusUpdated(any(), any());
        bridgeHandler = new BridgeHandler(bridge, factory);
        bridgeHandler.setCallback(bridgeCallback);
        bridge.setHandler(bridgeHandler);

        ThingHandlerCallback meterCallback = mock(ThingHandlerCallback.class);
        doAnswer(invocation -> {
            ThingStatusInfo info = invocation.getArgument(1);
            meter.setStatusInfo(info);
            meterStatuses.add(info);
            return null;
        }).when(meterCallback).statusUpdated(any(), any());
        doAnswer(invocation -> {
            meterStates.put(invocation.<ChannelUID> getArgument(0).getId(), invocation.getArgument(1));
            return null;
        }).when(meterCallback).stateUpdated(any(), any());
        when(meterCallback.getBridge(bridgeUid)).thenReturn(bridge);
        meterHandler = new MeterHandler(meter);
        meterHandler.setCallback(meterCallback);
        meter.setHandler(meterHandler);
    }

    /**
     * Initializes the meter, then the bridge. The bridge starts its stream at once, so in the other order the fake
     * bridge
     * could send its only reading before the meter knows its device id. The meter follows the bridge status either way.
     */
    void initialize() {
        meterHandler.initialize();
        bridgeHandler.initialize();
    }

    void dispose() {
        meterHandler.dispose();
        bridgeHandler.dispose();
    }

    @Nullable
    State meterState(String channel) {
        return meterStates.get(channel);
    }

    static ThingStatusInfo await(BlockingQueue<ThingStatusInfo> queue, Predicate<ThingStatusInfo> condition,
            long timeoutMillis) throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(timeoutMillis);
        while (true) {
            long left = TimeUnit.NANOSECONDS.toMillis(deadline - System.nanoTime());
            ThingStatusInfo info = left <= 0 ? null : queue.poll(left, TimeUnit.MILLISECONDS);
            if (info == null) {
                throw new AssertionError("expected status did not appear within " + timeoutMillis + " ms");
            }
            if (condition.test(info)) {
                return info;
            }
        }
    }

    /** Waits until the meter shows a value other than null for the channel. */
    @Nullable
    State awaitMeterState(String channel, Predicate<State> condition, long timeoutMillis) throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(timeoutMillis);
        while (System.nanoTime() < deadline) {
            State state = meterStates.get(channel);
            if (state != null && condition.test(state)) {
                return state;
            }
            Thread.sleep(5);
        }
        throw new AssertionError("channel " + channel + " did not reach the expected state");
    }

    /** A stream connection factory that points every client at the given port on the loopback address. */
    static StreamConnectionFactory factory(StreamTestSupport support, java.util.function.IntSupplier port,
            StreamClient.Timing timing, StreamClient.BackoffWaiter waiter) {
        return (host, password, threadName, listener) -> {
            StreamClient client = support.client(port.getAsInt(), password, threadName, timing, waiter,
                    System::nanoTime, listener);
            return new StreamConnection() {
                @Override
                public void start() {
                    client.start();
                }

                @Override
                public void stop() {
                    client.stop();
                }
            };
        };
    }
}
