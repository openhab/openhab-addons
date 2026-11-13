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
package org.openhab.binding.atv.internal.client.protocols.raop;

import java.time.Duration;
import java.util.concurrent.locks.LockSupport;
import java.util.function.LongConsumer;
import java.util.function.LongSupplier;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Timing knobs for the RAOP streaming pipeline.
 *
 * <p>
 * The monotonic clock, sleeping primitive and periodic-task intervals are all
 * injectable so tests can run deterministic or accelerated streaming sessions while
 * production code uses real time.
 *
 * @param nanoClock monotonic nanosecond clock used for pacing
 * @param sleeper sleeps for the given number of nanoseconds
 * @param syncInterval interval between sync packets (1 second)
 * @param keepAliveInterval AirPlay 1 feedback keep-alive interval (25 seconds)
 * @param feedbackInterval AirPlay 2 feedback interval (2 seconds)
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public record StreamTiming(LongSupplier nanoClock, LongConsumer sleeper, Duration syncInterval,
        Duration keepAliveInterval, Duration feedbackInterval) {

    /** Returns real-time defaults. */
    public static StreamTiming realTime() {
        return new StreamTiming(System::nanoTime, LockSupport::parkNanos, Duration.ofSeconds(1), Duration.ofSeconds(25),
                Duration.ofSeconds(2));
    }
}
