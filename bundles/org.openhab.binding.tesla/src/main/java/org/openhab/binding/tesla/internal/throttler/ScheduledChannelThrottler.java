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
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * The {@link ScheduledChannelThrottler} implements a throttler that maintains a
 * single execution rates, and does not maintain order of calls (thus has to
 * start from back rather than try to insert things in middle)
 *
 * @author Karel Goderis - Initial contribution
 */
public final class ScheduledChannelThrottler extends AbstractChannelThrottler {

    public ScheduledChannelThrottler(Rate totalRate) {
        this(totalRate, Executors.newSingleThreadScheduledExecutor(), new HashMap<>(), TimeProvider.SYSTEM_PROVIDER);
    }

    public ScheduledChannelThrottler(Rate totalRate, Map<Object, Rate> channels) {
        this(totalRate, Executors.newSingleThreadScheduledExecutor(), channels, TimeProvider.SYSTEM_PROVIDER);
    }

    public ScheduledChannelThrottler(Rate totalRate, ScheduledExecutorService scheduler, Map<Object, Rate> channels,
            TimeProvider timeProvider) {
        super(totalRate, scheduler, channels, timeProvider);
    }

    public void submitSync(Object channelKey, Runnable task) throws InterruptedException {
        Thread.sleep(getThrottleDelay(channelKey));
        task.run();
    }

    public void submitSync(Runnable task) throws InterruptedException {
        long delay = callTime(null) - timeProvider.getCurrentTimeInMillis();
        Thread.sleep(getThrottleDelay(delay));
        task.run();
    }

    @Override
    public Future<?> submit(Runnable task) {
        long delay = callTime(null) - timeProvider.getCurrentTimeInMillis();
        return scheduler.schedule(task, delay < 0 ? 0 : delay, TimeUnit.MILLISECONDS);
    }

    @Override
    public Future<?> submit(Object channelKey, Runnable task) {
        return scheduler.schedule(task, getThrottleDelay(channelKey), TimeUnit.MILLISECONDS);
    }
}
