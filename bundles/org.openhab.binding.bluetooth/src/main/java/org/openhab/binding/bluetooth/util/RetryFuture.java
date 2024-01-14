/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.bluetooth.util;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * This is a utility class that allows adding {@link CompletableFuture} capabilities to a {@link Callable}.
 * The provided callable will be executed asynchronously and the result will be used
 * to complete the {@code RetryFuture} instance. As per its namesake, the RetryFuture allows
 * the callable to reschedule itself by throwing a {@link RetryException}. Any other exception
 * will simply complete the RetryFuture exceptionally as per {@link CompletableFuture#completeExceptionally(Throwable)}.
 *
 * @author Connor Petty - Initial contribution
 *
 */
@NonNullByDefault
public class RetryFuture<T> extends HeritableFuture<T> {

    private final ScheduledExecutorService scheduler;

    public RetryFuture(Callable<T> callable, ScheduledExecutorService scheduler, long delay, TimeUnit unit) {
        this.scheduler = scheduler;
        setParentFuture(() -> scheduler.schedule(new CallableTask(callable), delay, unit));
    }

    public RetryFuture(Supplier<CompletableFuture<T>> supplier, ScheduledExecutorService scheduler, long delay,
            TimeUnit unit) {
        this.scheduler = scheduler;
        setParentFuture(() -> scheduler.schedule(new ComposeTask(supplier), delay, unit));
    }

    @Override
    public Executor defaultExecutor() {
        return scheduler;
    }

    private class CallableTask implements Runnable {

        private final Callable<T> callable;

        public CallableTask(Callable<T> callable) {
            this.callable = callable;
        }

        @Override
        public void run() {
            try {
                complete(callable.call());
            } catch (RetryException e) {
                setParentFuture(() -> {
                    if (!isDone()) {
                        return scheduler.schedule(this, e.delay, e.unit);
                    }
                    return null;
                });
            } catch (Exception e) {
                completeExceptionally(e);
            }
        }
    }

    private class ComposeTask implements Runnable {

        private final Supplier<CompletableFuture<T>> supplier;

        public ComposeTask(Supplier<CompletableFuture<T>> supplier) {
            this.supplier = supplier;
        }

        @Override
        public void run() {
            CompletableFuture<T> future = supplier.get();
            setParentFuture(() -> future);
            future.whenComplete((result, th) -> {
                if (th instanceof CompletionException) {
                    th = th.getCause();
                }
                if (th instanceof RetryException e) {
                    setParentFuture(() -> {
                        if (!isDone()) {
                            return scheduler.schedule(this, e.delay, e.unit);
                        }
                        return null;
                    });
                } else if (th != null) {
                    completeExceptionally(th);
                } else {
                    complete(result);
                }
            });
        }
    }

    /**
     * This is a convinience method for calling {@code new RetryFuture<>(callable, scheduler)}
     *
     * @param <T> the result type of the callable task.
     * @param callable the task to execute
     * @param scheduler the scheduler to use
     * @return a CompletableFuture that will return the result of the callable.
     */
    public static <T> CompletableFuture<T> callWithRetry(Callable<T> callable, ScheduledExecutorService scheduler) {
        return new RetryFuture<>(callable, scheduler, 0, TimeUnit.NANOSECONDS);
    }

    public static <T> CompletableFuture<T> scheduleWithRetry(Callable<T> callable, ScheduledExecutorService scheduler,
            long delay, TimeUnit unit) {
        return new RetryFuture<>(callable, scheduler, delay, unit);
    }

    @SafeVarargs
    public static <T> CompletableFuture<T> scheduleWithRetryForExceptions(Callable<T> callable,
            ScheduledExecutorService scheduler, long initDelay, long retryDelay, TimeUnit unit,
            Class<? extends Exception>... exceptions) {
        Callable<T> task = () -> {
            try {
                return callable.call();
            } catch (RetryException ex) {
                throw ex;
            } catch (Exception ex) {
                for (Class<? extends Exception> exClass : exceptions) {
                    if (exClass.isInstance(ex)) {
                        throw new RetryException(retryDelay, unit);
                    }
                }
                throw ex;
            }
        };
        return new RetryFuture<>(task, scheduler, initDelay, unit);
    }

    public static <T> CompletableFuture<T> composeWithRetry(Supplier<CompletableFuture<T>> supplier,
            ScheduledExecutorService scheduler) {
        return new RetryFuture<>(supplier, scheduler, 0, TimeUnit.NANOSECONDS);
    }

    public static <T> CompletableFuture<T> composeWithRetry(Supplier<CompletableFuture<T>> supplier,
            ScheduledExecutorService scheduler, long initDelay, TimeUnit unit) {
        return new RetryFuture<>(supplier, scheduler, initDelay, unit);
    }
}
