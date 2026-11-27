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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Records the callbacks of a stream client in order.
 *
 * @author Simon Spielmann - Initial contribution
 */
@NonNullByDefault
final class RecordingListener implements StreamListener {

    record Active() {
    }

    record Message(byte[] bytes) {
    }

    record Lost(StreamFailure failure) {
    }

    private final BlockingQueue<Object> events = new LinkedBlockingQueue<>();
    private final Predicate<byte[]> validity;

    /** @param validity decides the return value of {@link #onMessage(byte[])} */
    RecordingListener(Predicate<byte[]> validity) {
        this.validity = validity;
    }

    RecordingListener() {
        this(message -> true);
    }

    @Override
    public void onStreamActive() {
        events.add(new Active());
    }

    @Override
    public boolean onMessage(byte[] message) {
        events.add(new Message(message));
        return validity.test(message);
    }

    @Override
    public void onStreamLost(StreamFailure failure) {
        events.add(new Lost(failure));
    }

    /** @return the next event, or null after the timeout */
    @Nullable
    Object next(long timeoutMillis) throws InterruptedException {
        return events.poll(timeoutMillis, TimeUnit.MILLISECONDS);
    }

    /** Waits for the next event of the given type, skipping others; fails after the timeout. */
    <T> T await(Class<T> type, long timeoutMillis) throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(timeoutMillis);
        while (true) {
            long left = TimeUnit.NANOSECONDS.toMillis(deadline - System.nanoTime());
            Object event = left <= 0 ? null : events.poll(left, TimeUnit.MILLISECONDS);
            if (event == null) {
                throw new AssertionError("no " + type.getSimpleName() + " within " + timeoutMillis + " ms");
            }
            if (type.isInstance(event)) {
                return type.cast(event);
            }
        }
    }

    boolean isEmpty() {
        return events.isEmpty();
    }
}
