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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Registry of listeners with dispatch on the device loop.
 *
 * <p>
 * Producers hold a registry and announce state changes with {@link #fire(Consumer)}; listener exceptions are
 * caught and logged. An optional {@code maxCalls} limit caps the total number of fired notifications — the
 * device relay uses {@code maxCalls = 1} for {@code DeviceListener} so connection-lost/closed is delivered
 * exactly once.
 *
 * @param <L> listener type
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class ListenerRegistry<L> {

    /** Value for {@code maxCalls} meaning no limit. */
    public static final int NO_MAX_CALLS = 0;

    private static final Logger LOGGER = LoggerFactory.getLogger(ListenerRegistry.class);

    private final DeviceLoop loop;
    private final int maxCalls;
    private final List<L> listeners = new CopyOnWriteArrayList<>();
    private final AtomicInteger callsMade = new AtomicInteger();

    /**
     * Creates a registry without a call limit.
     *
     * @param loop device loop used for listener invocation
     */
    public ListenerRegistry(DeviceLoop loop) {
        this(loop, NO_MAX_CALLS);
    }

    /**
     * Creates a registry with a call limit.
     *
     * @param loop device loop used for listener invocation
     * @param maxCalls maximum number of {@link #fire(Consumer)} calls that are delivered;
     *            {@link #NO_MAX_CALLS} for no limit
     */
    public ListenerRegistry(DeviceLoop loop, int maxCalls) {
        this.loop = Objects.requireNonNull(loop, "loop");
        if (maxCalls < 0) {
            throw new IllegalArgumentException("maxCalls must be >= 0");
        }
        this.maxCalls = maxCalls;
    }

    /**
     * Adds a listener. Adding the same listener twice results in double notifications.
     *
     * @param listener listener to add
     */
    public void add(L listener) {
        listeners.add(Objects.requireNonNull(listener, "listener"));
    }

    /**
     * Removes a previously added listener. Unknown listeners are ignored.
     *
     * @param listener listener to remove
     */
    public void remove(L listener) {
        listeners.remove(listener);
    }

    /**
     * Notifies all listeners on the device loop.
     *
     * <p>
     * Counts against {@code maxCalls} even when no listeners are registered; once the limit
     * is reached, further calls are silently dropped. Listener exceptions are caught and
     * logged.
     *
     * @param invocation callback applied to each listener on the device loop
     * @return future completed once every listener has been invoked (already completed if
     *         the call was dropped by the limit)
     */
    public CompletableFuture<Void> fire(Consumer<L> invocation) {
        Objects.requireNonNull(invocation, "invocation");
        if (maxCalls != NO_MAX_CALLS && callsMade.incrementAndGet() > maxCalls) {
            return CompletableFuture.completedFuture(null);
        }
        List<CompletableFuture<Void>> invocations = new ArrayList<>();
        for (L listener : listeners) {
            invocations.add(loop.submitVoid(() -> {
                try {
                    invocation.accept(listener);
                } catch (RuntimeException e) {
                    LOGGER.debug("Error notifying listener {}", listener, e);
                }
            }));
        }
        return CompletableFuture.allOf(invocations.toArray(CompletableFuture[]::new));
    }
}
