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
package org.openhab.binding.tesla.internal.throttler;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link AbstractChannelThrottler} is abstract class implementing a
 * throttler with one global execution rate, or rate limiter
 *
 * @author Karel Goderis - Initial contribution
 */
@NonNullByDefault
abstract class AbstractChannelThrottler implements ChannelThrottler {

    protected final Rate totalRate;
    protected final TimeProvider timeProvider;
    protected final ScheduledExecutorService scheduler;
    protected final Map<Object, Rate> channels = new HashMap<>();

    protected AbstractChannelThrottler(Rate totalRate, ScheduledExecutorService scheduler, Map<Object, Rate> channels,
            TimeProvider timeProvider) {
        this.totalRate = totalRate;
        this.scheduler = scheduler;
        this.channels.putAll(channels);
        this.timeProvider = timeProvider;
    }

    protected synchronized long callTime(@Nullable Rate channel) {
        long now = timeProvider.getCurrentTimeInMillis();
        long callTime = totalRate.callTime(now);
        if (channel != null) {
            callTime = Math.max(callTime, channel.callTime(now));
            channel.addCall(callTime);
        }
        totalRate.addCall(callTime);
        return callTime;
    }

    protected long getThrottleDelay(Object channelKey) {
        long delay = callTime(channels.get(channelKey)) - timeProvider.getCurrentTimeInMillis();
        return delay < 0 ? 0 : delay;
    }
}
