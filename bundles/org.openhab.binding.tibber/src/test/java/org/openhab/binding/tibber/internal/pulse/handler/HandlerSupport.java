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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.openhab.binding.tibber.internal.TibberPulseBindingConstants.*;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.BridgeBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.types.State;

/**
 * Builds things and records what a handler publishes through its callback. No credential, address or id of a real
 * device appears here: the ids are synthetic.
 *
 * @author Simon Spielmann - Initial contribution
 */
@NonNullByDefault
final class HandlerSupport {

    static final String PASSWORD = "s3cret-Pw";
    static final String DEVICE_A = "aaaaaaaaaaaaaaaa";
    static final String DEVICE_B = "bbbbbbbbbbbbbbbb";
    static final String SYNTHETIC_METER_ID = "0a01454652aabbccddee";

    private HandlerSupport() {
    }

    static Bridge bridge(String id, String hostname, String password) {
        ThingUID uid = new ThingUID(THING_TYPE_PULSE_BRIDGE, id);
        return BridgeBuilder.create(THING_TYPE_PULSE_BRIDGE, uid)
                .withConfiguration(new Configuration(Map.of("hostname", hostname, "password", password))).build();
    }

    static Thing meter(Bridge bridge, String id, String deviceId, @Nullable Integer staleTimeout) {
        Map<String, Object> config = new HashMap<>();
        config.put("deviceId", deviceId);
        if (staleTimeout != null) {
            config.put("staleTimeout", staleTimeout);
        }
        return ThingBuilder.create(THING_TYPE_PULSE_METER, new ThingUID(THING_TYPE_PULSE_METER, bridge.getUID(), id))
                .withBridge(bridge.getUID()).withConfiguration(new Configuration(config)).build();
    }

    /** The bytes of one stream message: the 37-byte header and the payload. */
    static byte[] message(String deviceId, byte[] payload) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.writeBytes(("<device:" + deviceId + " topic:\"sml\">").getBytes(StandardCharsets.US_ASCII));
        out.writeBytes(payload);
        return out.toByteArray();
    }

    static Map<String, QuantityType<?>> values(Object... pairs) {
        Map<String, QuantityType<?>> values = new HashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            values.put((String) pairs[i], (QuantityType<?>) pairs[i + 1]);
        }
        return values;
    }

    static QuantityType<?> watts(String value) {
        return new QuantityType<>(value + " W");
    }

    static QuantityType<?> kilowattHours(String value) {
        return new QuantityType<>(value + " kWh");
    }

    static QuantityType<?> hertz(String value) {
        return new QuantityType<>(value + " Hz");
    }

    static QuantityType<?> volts(String value) {
        return new QuantityType<>(value + " V");
    }

    static ThingStatusInfo info(ThingStatus status, ThingStatusDetail detail) {
        return new ThingStatusInfo(status, detail, null);
    }

    /** Sets the status that a bridge handler would have published. */
    static void setStatus(Thing thing, ThingStatus status) {
        thing.setStatusInfo(info(status, ThingStatusDetail.NONE));
    }

    /** A wall clock that tests move by hand. */
    @NonNullByDefault({})
    static final class TestClock extends Clock {
        private final AtomicReference<Instant> now = new AtomicReference<>(Instant.parse("2026-01-01T12:00:00Z"));

        void advanceSeconds(long seconds) {
            now.updateAndGet(i -> i.plusSeconds(seconds));
        }

        @Override
        public ZoneOffset getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(java.time.ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now.get();
        }
    }

    /** What one handler published, in order. */
    static final class Recorder {
        record Update(String channel, State state) {
        }

        final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        final List<ThingStatusInfo> statuses = new CopyOnWriteArrayList<>();
        final List<Update> updates = new CopyOnWriteArrayList<>();
        /** Runs on the calling thread before a channel update is recorded; a test uses it to hold the caller. */
        volatile @Nullable Runnable beforeStateUpdate;

        Recorder(Thing thing, @Nullable Bridge bridge) {
            doAnswer(invocation -> {
                ThingStatusInfo info = invocation.getArgument(1);
                statuses.add(info);
                thing.setStatusInfo(info);
                return null;
            }).when(callback).statusUpdated(any(), any());
            doAnswer(invocation -> {
                Runnable hook = beforeStateUpdate;
                if (hook != null) {
                    hook.run();
                }
                updates.add(new Update(invocation.<org.openhab.core.thing.ChannelUID> getArgument(0).getId(),
                        invocation.getArgument(1)));
                return null;
            }).when(callback).stateUpdated(any(), any());
            if (bridge != null) {
                when(callback.getBridge(bridge.getUID())).thenReturn(bridge);
            }
        }

        ThingStatusInfo lastStatus() {
            return statuses.get(statuses.size() - 1);
        }

        @Nullable
        State lastState(String channel) {
            for (int i = updates.size() - 1; i >= 0; i--) {
                if (updates.get(i).channel().equals(channel)) {
                    return updates.get(i).state();
                }
            }
            return null;
        }

        long count(String channel) {
            return updates.stream().filter(u -> u.channel().equals(channel)).count();
        }

        void clear() {
            statuses.clear();
            updates.clear();
        }
    }

    static QuantityType<?> amperes(String value) {
        return new QuantityType<>(value + " A");
    }
}
