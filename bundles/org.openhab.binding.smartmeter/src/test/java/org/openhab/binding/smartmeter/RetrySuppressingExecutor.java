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

package org.openhab.binding.smartmeter;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 *
 * @author Leo Siepel - Initial contribution
 *
 */
@NonNullByDefault
public class RetrySuppressingExecutor extends ScheduledThreadPoolExecutor {

    private final Duration retryDelay;
    private final AtomicBoolean suppressNextRetry = new AtomicBoolean();
    private final CountDownLatch retrySuppressed = new CountDownLatch(1);
    private final CountDownLatch sourceTaskFinished = new CountDownLatch(1);
    private @Nullable Thread sourceThread;

    RetrySuppressingExecutor(int corePoolSize, Duration retryDelay) {
        super(corePoolSize);
        this.retryDelay = retryDelay;
    }

    void suppressNextRetry() {
        suppressNextRetry.set(true);
    }

    void awaitRetrySuppressed(Duration timeout) {
        await(retrySuppressed, timeout, "The outer retry was not suppressed");
    }

    void markSourceTask() {
        sourceThread = Thread.currentThread();
    }

    void awaitSourceTaskFinished(Duration timeout) {
        await(sourceTaskFinished, timeout, "The meter source task did not finish");
    }

    private void await(CountDownLatch latch, Duration timeout, String failureMessage) {
        try {
            if (!latch.await(timeout.toMillis(), TimeUnit.MILLISECONDS)) {
                throw new AssertionError(failureMessage);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError("Interrupted while waiting for an executor event", e);
        }
    }

    @Override
    @NonNullByDefault({})
    protected void afterExecute(Runnable runnable, Throwable throwable) {
        super.afterExecute(runnable, throwable);
        if (Thread.currentThread().equals(sourceThread)) {
            sourceTaskFinished.countDown();
        }
    }

    @Override
    public <V> ScheduledFuture<V> schedule(@Nullable Callable<V> task, long delay, @Nullable TimeUnit unit) {
        Callable<V> callable = Objects.requireNonNull(task);
        TimeUnit timeUnit = Objects.requireNonNull(unit);
        if (timeUnit.toMillis(delay) == retryDelay.toMillis() && suppressNextRetry.compareAndSet(true, false)) {
            ScheduledFuture<V> future = super.schedule(() -> null, delay, timeUnit);
            retrySuppressed.countDown();
            return future;
        }
        return super.schedule(callable, delay, timeUnit);
    }
}
