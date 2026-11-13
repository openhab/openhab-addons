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
package org.openhab.binding.solarman.internal.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.LongSupplier;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Serializes bounded commands, scheduled confirmation steps and polling for one logger.
 *
 * @author Ricardo Costa - Initial contribution
 */
@NonNullByDefault
public final class SolarmanWorkQueue implements AutoCloseable {
    private static final long CONFIRMATION_DELAY_MILLIS = 50;

    public enum Step {
        CONFIRMED,
        REFRESH,
        DELAYED
    }

    @FunctionalInterface
    public interface Transaction extends AutoCloseable {
        Step advance() throws Exception;

        @Override
        default void close() {
        }
    }

    private final ArrayBlockingQueue<Transaction> commands = new ArrayBlockingQueue<>(32);
    private final Consumer<Exception> errorHandler;
    private final ScheduledExecutorService scheduler;
    private final ScheduledFuture<?> task;
    private volatile boolean closed;
    private boolean busy;
    private long generation;
    private @Nullable Transaction active;
    private @Nullable ScheduledFuture<?> continuation;
    private long nextPoll;
    private final AtomicLong refreshRequests = new AtomicLong();
    private long completedRefreshRequests;
    private final LongSupplier nanoTime;
    private final long intervalNanos;

    public SolarmanWorkQueue(ScheduledExecutorService scheduler, Runnable poll, int intervalSeconds,
            Consumer<Exception> errorHandler) {
        this(scheduler, poll, intervalSeconds, errorHandler, System::nanoTime);
    }

    SolarmanWorkQueue(ScheduledExecutorService scheduler, Runnable poll, int intervalSeconds,
            Consumer<Exception> errorHandler, LongSupplier nanoTime) {
        this.nanoTime = nanoTime;
        this.scheduler = scheduler;
        if (intervalSeconds < 1) {
            throw new IllegalArgumentException("Refresh interval must be positive");
        }
        this.errorHandler = errorHandler;
        intervalNanos = TimeUnit.SECONDS.toNanos(intervalSeconds);
        task = scheduler.scheduleWithFixedDelay(() -> runCycle(poll), 250, 250, TimeUnit.MILLISECONDS);
    }

    public boolean submit(Runnable command) {
        return submitWithSnapshot(() -> {
            command.run();
            return false;
        });
    }

    /**
     * Queues a command that returns true only after publishing a complete confirmed device snapshot.
     */
    public boolean submitWithSnapshot(BooleanSupplier command) {
        return submitTransaction(() -> command.getAsBoolean() ? Step.CONFIRMED : Step.REFRESH);
    }

    public synchronized boolean submitTransaction(Transaction command) {
        return !closed && commands.offer(command);
    }

    public void requestRefresh() {
        refreshRequests.incrementAndGet();
    }

    private void runCycle(Runnable poll) {
        Optional<Transaction> command;
        long current;
        synchronized (this) {
            if (closed || busy) {
                return;
            }
            command = Optional.ofNullable(commands.poll());
            active = null;
            if (command.isPresent()) {
                active = command.orElseThrow();
            }
            busy = true;
            current = ++generation;
        }
        if (command.isPresent()) {
            runStep(command.orElseThrow(), current, poll);
        } else {
            try {
                pollIfDue(poll);
            } finally {
                release(current);
            }
        }
    }

    private void runStep(Transaction command, long current, Runnable poll) {
        synchronized (this) {
            if (closed || current != generation) {
                return;
            }
        }
        Step result = Step.REFRESH;
        boolean deferred = false;
        try {
            result = command.advance();
            if (result == Step.DELAYED) {
                synchronized (this) {
                    if (!closed && current == generation) {
                        continuation = scheduler.schedule(() -> runStep(command, current, poll),
                                CONFIRMATION_DELAY_MILLIS, TimeUnit.MILLISECONDS);
                        deferred = true;
                    }
                }
            }
        } catch (Exception e) {
            if (!closed) {
                errorHandler.accept(e);
            }
        } finally {
            if (!deferred) {
                try {
                    command.close();
                    synchronized (this) {
                        if (!closed && current == generation) {
                            continuation = null;
                            if (result == Step.CONFIRMED) {
                                nextPoll = nanoTime.getAsLong() + intervalNanos;
                            } else {
                                requestRefresh();
                            }
                        }
                    }
                    pollIfDue(poll);
                } finally {
                    release(current);
                }
            }
        }
    }

    private synchronized void release(long current) {
        if (current == generation) {
            active = null;
            busy = false;
        }
    }

    private void pollIfDue(Runnable poll) {
        synchronized (this) {
            long requestedRefresh = refreshRequests.get();
            if (closed || requestedRefresh == completedRefreshRequests && nextPoll != 0
                    && nanoTime.getAsLong() - nextPoll < 0) {
                return;
            }
            completedRefreshRequests = requestedRefresh;
            nextPoll = nanoTime.getAsLong() + intervalNanos;
        }
        try {
            poll.run();
        } catch (Exception e) {
            if (!closed) {
                errorHandler.accept(e);
            }
        }
    }

    @Override
    public void close() {
        @Nullable
        Transaction pending;
        @Nullable
        ScheduledFuture<?> delayed;
        List<Transaction> queued = new ArrayList<>();
        synchronized (this) {
            if (closed) {
                return;
            }
            closed = true;
            generation++;
            pending = active;
            active = null;
            delayed = continuation;
            continuation = null;
            commands.drainTo(queued);
        }
        task.cancel(true);
        if (delayed != null) {
            delayed.cancel(true);
        }
        if (pending != null) {
            pending.close();
        }
        queued.forEach(Transaction::close);
    }
}
