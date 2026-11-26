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

import static org.openhab.binding.tibber.internal.TibberPulseBindingConstants.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalLong;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.QuantityType;

/**
 * The latest value of every channel of one meter and the times they were updated, and the rules by which they go
 * stale. It is pure and clock-free: every method that depends on time takes the current monotonic time in
 * nanoseconds, which makes the stale scenarios testable without a scheduler.
 *
 * Not thread-safe; the {@link MeterHandler} guards it with its lock.
 *
 * Two levels of staleness exist. A live channel that was not updated for the stale timeout expires on its own. The
 * meter as a whole expires when no valid reading arrived for the stale timeout, counted from the last reading or, if
 * there was none, from {@link #restartClock(long)} (the opening of the stream). The energy counters never expire.
 *
 * @author Simon Spielmann - Initial contribution
 */
@NonNullByDefault
final class MeterState {

    /** What expired in one call of {@link #expire(long, long)}. */
    record Expiry(List<String> channels, boolean meter) {
        boolean isEmpty() {
            return channels.isEmpty() && !meter;
        }
    }

    private final Map<String, QuantityType<?>> values = new HashMap<>();
    private final Map<String, Long> updatedNanos = new HashMap<>();
    private boolean clockRunning;
    private long clockStartNanos;
    private boolean meterExpired;

    /**
     * Records the values of a valid reading and restarts the meter-level clock.
     */
    void apply(Map<String, QuantityType<?>> reading, long nowNanos) {
        for (Map.Entry<String, QuantityType<?>> entry : reading.entrySet()) {
            values.put(entry.getKey(), entry.getValue());
            updatedNanos.put(entry.getKey(), nowNanos);
        }
        restartClock(nowNanos);
    }

    /**
     * Starts counting the meter-level stale time from now, for example when the stream was opened.
     */
    void restartClock(long nowNanos) {
        clockRunning = true;
        clockStartNanos = nowNanos;
        meterExpired = false;
    }

    /**
     * Stops the meter-level clock: nothing can expire until it is restarted. Used while the stream is down.
     */
    void stopClock() {
        clockRunning = false;
        meterExpired = false;
    }

    @Nullable
    QuantityType<?> value(String channel) {
        return values.get(channel);
    }

    /**
     * Removes the live values; the energy counters stay.
     *
     * @return the channels that held a value
     */
    List<String> clearLive() {
        List<String> cleared = new ArrayList<>();
        for (String channel : LIVE_CHANNELS) {
            if (values.remove(channel) != null) {
                cleared.add(channel);
            }
            updatedNanos.remove(channel);
        }
        return cleared;
    }

    /**
     * Expires what has gone stale. The result reports each expiry once.
     *
     * @param staleNanos the stale timeout
     */
    Expiry expire(long nowNanos, long staleNanos) {
        List<String> channels = new ArrayList<>();
        for (String channel : LIVE_CHANNELS) {
            Long updated = updatedNanos.get(channel);
            if (updated != null && nowNanos - updated >= staleNanos) {
                updatedNanos.remove(channel);
                if (values.remove(channel) != null) {
                    channels.add(channel);
                }
            }
        }
        boolean meter = false;
        if (clockRunning && !meterExpired && nowNanos - clockStartNanos >= staleNanos) {
            meterExpired = true;
            meter = true;
        }
        return new Expiry(channels, meter);
    }

    /**
     * @return the time in nanoseconds at which the next expiry is due, or empty if nothing is pending
     */
    OptionalLong nextExpiry(long staleNanos) {
        boolean found = false;
        long earliest = 0;
        for (Map.Entry<String, Long> entry : updatedNanos.entrySet()) {
            if (LIVE_CHANNELS.contains(entry.getKey())) {
                long due = entry.getValue() + staleNanos;
                if (!found || due - earliest < 0) {
                    earliest = due;
                    found = true;
                }
            }
        }
        if (clockRunning && !meterExpired) {
            long due = clockStartNanos + staleNanos;
            if (!found || due - earliest < 0) {
                earliest = due;
                found = true;
            }
        }
        return found ? OptionalLong.of(earliest) : OptionalLong.empty();
    }
}
