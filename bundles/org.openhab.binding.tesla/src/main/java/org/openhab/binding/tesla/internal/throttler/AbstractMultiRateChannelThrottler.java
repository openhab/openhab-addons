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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link AbstractMultiRateChannelThrottler} is abstract class implementing
 * a throttler with multiple global execution rates, or rate limiters
 *
 * @author Karel Goderis - Initial contribution
 */
@NonNullByDefault
abstract class AbstractMultiRateChannelThrottler implements ChannelThrottler {

    protected final TimeProvider timeProvider;
    protected final ScheduledExecutorService scheduler;
    protected final Map<Object, Rate> channels = new HashMap<>();
    protected final ArrayList<Rate> rates = new ArrayList<>();

    protected AbstractMultiRateChannelThrottler(Rate rate, ScheduledExecutorService scheduler,
            Map<Object, Rate> channels, TimeProvider timeProvider) {
        this.rates.add(rate);
        this.scheduler = scheduler;
        this.channels.putAll(channels);
        this.timeProvider = timeProvider;
    }

    public synchronized void addRate(Rate rate) {
        this.rates.add(rate);
    }

    protected synchronized long callTime(@Nullable Rate channel) {
        long maxCallTime = 0;
        long finalCallTime = 0;
        long now = timeProvider.getCurrentTimeInMillis();
        Iterator<Rate> iterator = rates.iterator();
        while (iterator.hasNext()) {
            Rate someRate = iterator.next();
            maxCallTime = Math.max(maxCallTime, someRate.callTime(now));
        }

        if (channel != null) {
            finalCallTime = Math.max(maxCallTime, channel.callTime(now));
            channel.addCall(finalCallTime);
        }

        iterator = rates.iterator();
        while (iterator.hasNext()) {
            Rate someRate = iterator.next();
            someRate.addCall(finalCallTime);
        }

        return finalCallTime;
    }

    protected long getThrottleDelay(Object channelKey) {
        long delay = callTime(channels.get(channelKey)) - timeProvider.getCurrentTimeInMillis();
        return delay < 0 ? 0 : delay;
    }
}
