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
package org.openhab.binding.atv.internal.client.core;

import java.time.Clock;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Shared runtime services for the library: the scheduler used for timers (heartbeats,
 * timeouts, reconnect backoff), a clock, a factory for per-device loops, and an optional
 * service for hosting local files over HTTP.
 *
 * <p>
 * The scheduler, clock, and file host are constructor-injectable so tests can substitute
 * deterministic implementations and hosts can provide their own HTTP serving. Most callers use
 * {@link #defaultRuntime()}.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class AtvRuntime {

    private final ScheduledExecutorService scheduler;
    private final Clock clock;
    private final @Nullable FileHostService fileHost;

    /**
     * Creates a runtime with an injected scheduler and clock, and no file host.
     *
     * @param scheduler executor used for all timed operations
     * @param clock time source
     */
    public AtvRuntime(ScheduledExecutorService scheduler, Clock clock) {
        this(scheduler, clock, null);
    }

    /**
     * Creates a runtime with an injected scheduler, clock, and file host.
     *
     * @param scheduler executor used for all timed operations
     * @param clock time source
     * @param fileHost service for hosting local files over HTTP, or {@code null} if unsupported
     */
    public AtvRuntime(ScheduledExecutorService scheduler, Clock clock, @Nullable FileHostService fileHost) {
        this.scheduler = Objects.requireNonNull(scheduler, "scheduler");
        this.clock = Objects.requireNonNull(clock, "clock");
        this.fileHost = fileHost;
    }

    /**
     * Creates a runtime with a single daemon platform-thread scheduler and the system clock.
     */
    public AtvRuntime() {
        this(Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "atv-scheduler");
            thread.setDaemon(true);
            return thread;
        }), Clock.systemUTC());
    }

    /**
     * Returns the shared scheduler for timed operations.
     */
    public ScheduledExecutorService scheduler() {
        return scheduler;
    }

    /**
     * Returns the runtime clock.
     */
    public Clock clock() {
        return clock;
    }

    /**
     * Returns the service for hosting local files over HTTP, or {@code null} if the host
     * application did not provide one.
     */
    public @Nullable FileHostService fileHost() {
        return fileHost;
    }

    /**
     * Creates a new per-device serial loop.
     */
    public DeviceLoop newDeviceLoop() {
        return new DeviceLoop();
    }

    /**
     * Returns the lazily created process-wide default runtime.
     */
    public static AtvRuntime defaultRuntime() {
        return DefaultHolder.INSTANCE;
    }

    private static final class DefaultHolder {
        private static final AtvRuntime INSTANCE = new AtvRuntime();
    }
}
