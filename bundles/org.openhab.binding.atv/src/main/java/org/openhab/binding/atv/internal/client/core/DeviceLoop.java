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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Serial executor backed by a single virtual thread.
 *
 * <p>
 * All state mutation for one device runs on its device loop: tasks run one at a time, in
 * submission order, so code running on the loop never needs additional synchronization for
 * device state.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class DeviceLoop {

    private static final AtomicLong COUNTER = new AtomicLong();

    private final ExecutorService executor;

    private volatile @Nullable Thread loopThread;

    /**
     * Creates a new device loop with its own single virtual thread.
     */
    public DeviceLoop() {
        var factory = Thread.ofVirtual().name("atv-device-loop-" + COUNTER.incrementAndGet()).factory();
        this.executor = Executors.newSingleThreadExecutor(task -> {
            Thread thread = factory.newThread(task);
            loopThread = thread;
            return thread;
        });
    }

    /**
     * Returns whether the calling thread is the loop thread. Loop-confined code can use this
     * to run inline instead of (dead)locking on a task submitted to itself.
     *
     * @return {@code true} when called from the loop thread
     */
    @SuppressWarnings("PMD.CompareObjectsWithEquals")
    public boolean inLoop() {
        return Thread.currentThread() == loopThread;
    }

    /**
     * Runs a task on the device loop. Tasks execute serially in submission order.
     *
     * @param task the task to run
     * @throws RejectedExecutionException if the loop has been shut down
     */
    public void execute(Runnable task) {
        executor.execute(task);
    }

    /**
     * Runs a value-producing task on the device loop.
     *
     * @param <T> result type
     * @param task the task to run
     * @return future completed with the task result, or exceptionally if the task throws
     *         or the loop has been shut down
     */
    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    public <T> CompletableFuture<T> submit(Supplier<T> task) {
        CompletableFuture<T> result = new CompletableFuture<>();
        try {
            executor.execute(() -> {
                try {
                    result.complete(task.get());
                } catch (Throwable t) {
                    result.completeExceptionally(t);
                }
            });
        } catch (RejectedExecutionException e) {
            result.completeExceptionally(e);
        }
        return result;
    }

    /**
     * Runs a task with no result on the device loop.
     *
     * @param task the task to run
     * @return future completed with {@code null} once the task has run, or exceptionally if
     *         the task throws or the loop has been shut down
     */
    public CompletableFuture<Void> submitVoid(Runnable task) {
        try {
            return CompletableFuture.runAsync(task, executor);
        } catch (RejectedExecutionException e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Runs an asynchronous task on the device loop and flattens its result.
     *
     * <p>
     * The supplier is invoked on the loop; the returned future completes when the future
     * produced by the supplier completes. Only the supplier invocation itself is serialized
     * on the loop, not the completion of the inner future.
     *
     * @param <T> result type
     * @param task supplier of the asynchronous computation, invoked on the loop
     * @return future mirroring the inner future's outcome, or exceptionally if the supplier
     *         throws, returns null, or the loop has been shut down
     */
    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    public <T> CompletableFuture<T> submitAsync(Supplier<CompletableFuture<T>> task) {
        CompletableFuture<T> result = new CompletableFuture<>();
        try {
            executor.execute(() -> {
                try {
                    CompletableFuture<T> inner = task.get();
                    if (inner == null) {
                        result.completeExceptionally(new NullPointerException("submitAsync supplier returned null"));
                        return;
                    }
                    inner.whenComplete((value, error) -> {
                        if (error != null) {
                            result.completeExceptionally(error);
                        } else {
                            result.complete(value);
                        }
                    });
                } catch (Throwable t) {
                    result.completeExceptionally(t);
                }
            });
        } catch (RejectedExecutionException e) {
            result.completeExceptionally(e);
        }
        return result;
    }

    /**
     * Shuts the loop down. Already submitted tasks still run; new submissions are rejected.
     */
    public void shutdown() {
        executor.shutdown();
    }
}
