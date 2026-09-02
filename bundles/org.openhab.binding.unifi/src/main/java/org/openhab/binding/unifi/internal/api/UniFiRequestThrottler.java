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
package org.openhab.binding.unifi.internal.api;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Token-bucket rate limiter for outbound requests against a UniFi console. Blocks the caller when the window is
 * full so bursts from multiple child bindings hitting the same console do not trip HTTP 429 or lock out the
 * underlying user account.
 * <p>
 * Ported from the per-bridge throttle originally implemented inside the UniFi Protect binding.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class UniFiRequestThrottler {

    private final int maxRequestsPerWindow;
    private final long windowNanos;
    private final Deque<Long> timestamps = new ArrayDeque<>();
    private final Object lock = new Object();

    public UniFiRequestThrottler() {
        this(7, TimeUnit.SECONDS.toNanos(1));
    }

    public UniFiRequestThrottler(int maxRequestsPerWindow, long windowNanos) {
        this.maxRequestsPerWindow = maxRequestsPerWindow;
        this.windowNanos = windowNanos;
    }

    /**
     * Blocks the calling thread until it is safe to issue another request. Records the timestamp of the granted
     * request so subsequent calls see an updated window.
     *
     * @throws InterruptedException if the thread is interrupted while waiting for a free slot
     */
    public void acquire() throws InterruptedException {
        while (true) {
            long waitNanos;
            synchronized (lock) {
                long now = System.nanoTime();
                while (!timestamps.isEmpty() && now - timestamps.peekFirst() >= windowNanos) {
                    timestamps.removeFirst();
                }
                if (timestamps.size() < maxRequestsPerWindow) {
                    timestamps.addLast(now);
                    return;
                }
                Long oldest = timestamps.peekFirst();
                waitNanos = (oldest + windowNanos) - now;
            }
            if (waitNanos > 0L) {
                TimeUnit.NANOSECONDS.sleep(waitNanos);
            }
        }
    }
}
