/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.mqtt.generic.internal.tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Collects objects over time until a specified delay passed by.
 * Then call the user back with a list of accumulated objects and start over again.
 *
 * @author David Graeff - Initial contribution
 *
 * @param <TYPE> Any object
 */
@NonNullByDefault
public class DelayedBatchProcessing<TYPE> implements Consumer<TYPE> {
    private final int delay;
    private final Consumer<List<TYPE>> consumer;
    private final List<TYPE> queue = Collections.synchronizedList(new ArrayList<>());
    private final ScheduledExecutorService executor;
    protected @Nullable ScheduledFuture<?> future;

    /**
     * Creates a {@link DelayedBatchProcessing}.
     *
     * @param delay A delay in milliseconds
     * @param consumer A consumer of the list of collected objects
     * @param executor A scheduled executor service
     */
    public DelayedBatchProcessing(int delay, Consumer<List<TYPE>> consumer, ScheduledExecutorService executor) {
        this.delay = delay;
        this.consumer = consumer;
        this.executor = executor;
        if (delay <= 0) {
            throw new IllegalArgumentException("Delay need to be greater than 0!");
        }
    }

    /**
     * Add new object to the batch process list. If the list was empty, the delay timer
     * is armed and all successive objects are accumulated from here on.
     *
     * @param t An object
     */
    @Override
    public void accept(TYPE t) {
        queue.add(t);
        final ScheduledFuture<?> scheduledFuture = this.future;
        if (scheduledFuture == null || scheduledFuture.isDone()) {
            this.future = executor.schedule(this::run, delay, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * Return the so far accumulated objects, but do not deliver them to the target consumer anymore.
     *
     * @return A list of accumulated objects
     */
    public List<TYPE> join() {
        ScheduledFuture<?> scheduledFuture = this.future;
        if (scheduledFuture != null && !scheduledFuture.isDone()) {
            scheduledFuture.cancel(false);
        }
        List<TYPE> lqueue = new ArrayList<>();
        synchronized (queue) {
            lqueue.addAll(queue);
            queue.clear();
        }
        return lqueue;
    }

    /**
     * Return true if there is a delayed processing going on.
     */
    public boolean isArmed() {
        ScheduledFuture<?> scheduledFuture = this.future;
        return scheduledFuture != null && !scheduledFuture.isDone();
    }

    /**
     * Deliver queued items now to the target consumer.
     */
    public void forceProcessNow() {
        ScheduledFuture<?> scheduledFuture = this.future;
        if (scheduledFuture != null && !scheduledFuture.isDone()) {
            scheduledFuture.cancel(false);
        }
        run();
    }

    private void run() {
        List<TYPE> lqueue = new ArrayList<>();
        synchronized (queue) {
            lqueue.addAll(queue);
            queue.clear();
        }

        if (!lqueue.isEmpty()) {
            consumer.accept(lqueue);
        }
    }
}
