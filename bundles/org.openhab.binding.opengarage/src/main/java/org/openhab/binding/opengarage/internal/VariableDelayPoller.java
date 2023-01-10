/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.opengarage.internal;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Variable poller will schedule a task to run periodically, but will allow the
 * schedule to be modified on-the-fly
 *
 * @author Tim Harper - Initial contribution
 */
public class VariableDelayPoller {
    private final Logger logger = LoggerFactory.getLogger(VariableDelayPoller.class);
    private @NonNullByDefault ScheduledExecutorService scheduler;
    private @NonNullByDefault Supplier<Long> task;
    private long defaultPollSeconds = 0;
    private AtomicLong lastThreadId = new AtomicLong(0);
    private Future<?> currentFuture = CompletableFuture.completedFuture(null);

    /**
     * @param scheduler - Reference to the Java scheduler instance.
     * @param task - task to invoke. Task returns number of seconds in which the task should be invoked again.
     * @param defaultPollSeconds - number of seconds in which to run the task should the task fail to return a value
     */
    public VariableDelayPoller(ScheduledExecutorService scheduler, Supplier<Long> task, long defaultPollSeconds) {
        this.scheduler = scheduler;
        this.defaultPollSeconds = defaultPollSeconds;
        this.task = task;

        startNextPoll(lastThreadId.get(), 1);
    };

    /**
     * Stop the poller from running.
     *
     * @param abortIfRunning If the task is currently running in another thread, try to abort it.
     */
    synchronized public void stop(boolean abortIfRunning) {
        lastThreadId.getAndIncrement(); // prevent future tasks from being scheduled
        this.currentFuture.cancel(abortIfRunning);
    }

    private void doPoll(long threadId) {
        var nextPollDuration = this.defaultPollSeconds;
        try {
            nextPollDuration = task.get();
        } catch (Exception e) {
            logger.warn("error occurred while invoking periodic task", e);
        }
        startNextPoll(threadId, nextPollDuration);
    }

    /**
     * Reschedule the next invocation of the task, canceling the scheduled task in the future.
     *
     * @param inSeconds How many seconds from now should we schedule the task?
     * @param abortExistingIfRunning If the task happens to be running now, should we abort it?
     */
    synchronized public void reschedule(long inSeconds, boolean abortExistingIfRunning) {
        logger.debug("Cancelled current future in order to reschedule...");
        this.currentFuture.cancel(abortExistingIfRunning);
        startNextPoll(lastThreadId.get(), inSeconds);
    }

    synchronized private void startNextPoll(long threadId, long inSeconds) {
        var nextThreadId = threadId + 1;
        // we want to be extra-sure that we don't have multiple poll invocations starting parallel poll sequences.
        if (lastThreadId.compareAndSet(threadId, nextThreadId)) {
            logger.debug("Starting next poll in {}", inSeconds);
            this.currentFuture = this.scheduler.schedule(() -> this.doPoll(nextThreadId), inSeconds, TimeUnit.SECONDS);
        }
    }
}
