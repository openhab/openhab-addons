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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dispatches messages to listeners keyed by a message type.
 *
 * <p>
 * Listeners are invoked on the device loop, one at a time; exceptions thrown by a listener are caught and
 * logged so one failing listener never affects the others.
 *
 * @param <T> dispatch type (an enum keying the kind of message)
 * @param <M> message type
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class MessageDispatcher<T extends Enum<T>, M> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageDispatcher.class);

    /**
     * Handle for a registered listener, used to stop listening.
     */
    public interface Registration {
        /**
         * Removes the listener; further messages are no longer delivered to it.
         */
        void remove();
    }

    private final DeviceLoop loop;
    private final ConcurrentMap<T, CopyOnWriteArrayList<Entry<M>>> listeners = new ConcurrentHashMap<>();

    private record Entry<M> (Predicate<M> filter, Consumer<M> listener) {
    }

    /**
     * Creates a dispatcher delivering messages on the given device loop.
     *
     * @param loop device loop used for listener invocation
     */
    public MessageDispatcher(DeviceLoop loop) {
        this.loop = Objects.requireNonNull(loop, "loop");
    }

    /**
     * Registers a listener for all messages of a type.
     *
     * @param type dispatch type to listen to
     * @param listener callback invoked on the device loop
     * @return registration handle for removal
     */
    public Registration listenTo(T type, Consumer<M> listener) {
        return listenTo(type, listener, message -> true);
    }

    /**
     * Registers a listener for messages of a type matching a filter.
     *
     * @param type dispatch type to listen to
     * @param listener callback invoked on the device loop
     * @param filter predicate evaluated at dispatch time; only matching messages are delivered
     * @return registration handle for removal
     */
    public Registration listenTo(T type, Consumer<M> listener, Predicate<M> filter) {
        Objects.requireNonNull(listener, "listener");
        Objects.requireNonNull(filter, "filter");
        Entry<M> entry = new Entry<>(filter, listener);
        listeners.computeIfAbsent(type, key -> new CopyOnWriteArrayList<>()).add(entry);
        return () -> {
            List<Entry<M>> entries = listeners.get(type);
            if (entries != null) {
                entries.remove(entry);
            }
        };
    }

    /**
     * Dispatches a message to all matching listeners via the device loop.
     *
     * <p>
     * Filters are evaluated on the calling thread; listener callbacks run on the device
     * loop. Listener exceptions are caught and logged.
     *
     * @param type dispatch type of the message
     * @param message the message
     * @return future completed once every matching listener has been invoked
     */
    public CompletableFuture<Void> dispatch(T type, M message) {
        List<Entry<M>> entries = listeners.getOrDefault(type, new CopyOnWriteArrayList<>());
        List<CompletableFuture<Void>> invocations = new ArrayList<>();
        for (Entry<M> entry : entries) {
            boolean matches;
            try {
                matches = entry.filter().test(message);
            } catch (RuntimeException e) {
                LOGGER.debug("Error in dispatch filter for type {}", type, e);
                continue;
            }
            if (!matches) {
                continue;
            }
            LOGGER.debug("Dispatching message with type {} to {}", type, entry.listener());
            invocations.add(loop.submitVoid(() -> {
                try {
                    entry.listener().accept(message);
                } catch (RuntimeException e) {
                    LOGGER.debug("Error during dispatch of type {}", type, e);
                }
            }));
        }
        return CompletableFuture.allOf(invocations.toArray(CompletableFuture[]::new));
    }
}
