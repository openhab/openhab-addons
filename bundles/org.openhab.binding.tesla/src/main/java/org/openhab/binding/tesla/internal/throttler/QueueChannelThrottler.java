/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.tesla.internal.throttler;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link QueueChannelThrottler} implements a throttler that maintains
 * multiple execution rates, and maintains the order of calls
 *
 * @author Karel Goderis - Initial contribution
 */
public final class QueueChannelThrottler extends AbstractMultiRateChannelThrottler {

    private final Logger logger = LoggerFactory.getLogger(QueueChannelThrottler.class);

    private static final int MAX_QUEUE_LENGTH = 150;
    private BlockingQueue<FutureTask<?>> tasks;
    private final Rate overallRate;

    private final Runnable processQueueTask = () -> {
        FutureTask<?> task = tasks.poll();
        if (task != null && !task.isCancelled()) {
            task.run();
        }
    };

    public QueueChannelThrottler(Rate someRate) {
        this(someRate, Executors.newScheduledThreadPool(1), new HashMap<>(), TimeProvider.SYSTEM_PROVIDER,
                MAX_QUEUE_LENGTH);
    }

    public QueueChannelThrottler(Rate someRate, ScheduledExecutorService scheduler) {
        this(someRate, scheduler, new HashMap<>(), TimeProvider.SYSTEM_PROVIDER, MAX_QUEUE_LENGTH);
    }

    public QueueChannelThrottler(Rate someRate, ScheduledExecutorService scheduler, Map<Object, Rate> channels) {
        this(someRate, scheduler, channels, TimeProvider.SYSTEM_PROVIDER, MAX_QUEUE_LENGTH);
    }

    public QueueChannelThrottler(Rate someRate, Map<Object, Rate> channels, int queueLength) {
        this(someRate, Executors.newScheduledThreadPool(1), channels, TimeProvider.SYSTEM_PROVIDER, queueLength);
    }

    public QueueChannelThrottler(Rate someRate, ScheduledExecutorService scheduler, Map<Object, Rate> channels,
            TimeProvider timeProvider, int queueLength) {
        super(someRate, scheduler, channels, timeProvider);
        overallRate = someRate;
        tasks = new LinkedBlockingQueue<>(queueLength);
    }

    @Override
    public Future<?> submit(Runnable task) {
        return submit(null, task);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Future<?> submit(Object channelKey, Runnable task) {
        FutureTask runTask = new FutureTask(task, null);
        try {
            if (tasks.offer(runTask, overallRate.timeInMillis(), TimeUnit.MILLISECONDS)) {
                long throttledTime = channelKey == null ? callTime(null) : callTime(channels.get(channelKey));
                long now = timeProvider.getCurrentTimeInMillis();
                scheduler.schedule(processQueueTask, throttledTime < now ? 0 : throttledTime - now,
                        TimeUnit.MILLISECONDS);
                return runTask;
            } else {
                logger.warn("The QueueThrottler can not take the task '{}' at this point in time", runTask.toString());
            }
        } catch (InterruptedException e) {
            logger.error("An exception occurred while scheduling a new taks: '{}'", e.getMessage());
        }

        return null;
    }
}
