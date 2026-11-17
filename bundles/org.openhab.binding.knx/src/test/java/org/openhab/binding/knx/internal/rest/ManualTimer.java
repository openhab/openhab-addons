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
package org.openhab.binding.knx.internal.rest;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * A timer for the stream tests that only runs a task when the test moves the time on; tasks run on the test's
 * thread.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
final class ManualTimer implements TelegramStream.Timer {
    private long now = 1_000_000;
    private long order;
    private final List<Task> tasks = new ArrayList<>();

    private static final class Task {
        final Runnable action;
        final long period;
        final long order;
        long due;
        boolean cancelled;

        Task(Runnable action, long due, long period, long order) {
            this.action = action;
            this.due = due;
            this.period = period;
            this.order = order;
        }
    }

    @Override
    public synchronized Runnable later(Runnable task, long delayMillis) {
        return add(task, delayMillis, 0);
    }

    @Override
    public synchronized Runnable every(Runnable task, long periodMillis) {
        return add(task, periodMillis, periodMillis);
    }

    private Runnable add(Runnable action, long delay, long period) {
        Task task = new Task(action, now + Math.max(0, delay), period, order++);
        tasks.add(task);
        return () -> {
            synchronized (this) {
                task.cancelled = true;
                tasks.remove(task);
            }
        };
    }

    @Override
    public synchronized long now() {
        return now;
    }

    /** {@return the number of tasks that run once and are still waiting} */
    synchronized int waitingOnce() {
        return (int) tasks.stream().filter(task -> task.period == 0).count();
    }

    /** {@return the delay of the earliest task that runs once, if any} */
    synchronized Optional<Long> nextOnceIn() {
        return tasks.stream().filter(task -> task.period == 0).map(task -> task.due - now).min(Long::compare);
    }

    /** Runs every task due by now, also one that a task scheduled for now. */
    void runDue() {
        advance(0);
    }

    /** {@return whether a task was due by now; runs the earliest of them and no other} */
    boolean runNextDue() {
        Task next;
        synchronized (this) {
            Optional<Task> due = tasks.stream().filter(task -> task.due <= now)
                    .min(Comparator.<Task> comparingLong(task -> task.due).thenComparingLong(task -> task.order));
            if (due.isEmpty()) {
                return false;
            }
            next = due.get();
            if (next.period > 0) {
                next.due += next.period;
            } else {
                tasks.remove(next);
            }
        }
        if (!next.cancelled) {
            next.action.run();
        }
        return true;
    }

    /** Moves the time on, running each task when its time comes, in the order of time. */
    void advance(long millis) {
        long target;
        synchronized (this) {
            target = now + millis;
        }
        while (true) {
            Task next;
            synchronized (this) {
                Optional<Task> due = tasks.stream().filter(task -> task.due <= target)
                        .min(Comparator.<Task> comparingLong(task -> task.due).thenComparingLong(task -> task.order));
                if (due.isEmpty()) {
                    now = target;
                    return;
                }
                next = due.get();
                now = Math.max(now, next.due);
                if (next.period > 0) {
                    next.due += next.period;
                } else {
                    tasks.remove(next);
                }
            }
            if (!next.cancelled) {
                next.action.run();
            }
        }
    }
}
