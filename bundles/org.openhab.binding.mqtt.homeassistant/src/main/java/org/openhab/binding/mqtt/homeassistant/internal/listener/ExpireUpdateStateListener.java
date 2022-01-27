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
package org.openhab.binding.mqtt.homeassistant.internal.listener;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mqtt.generic.AvailabilityTracker;
import org.openhab.binding.mqtt.generic.ChannelStateUpdateListener;
import org.openhab.binding.mqtt.generic.values.Value;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.State;

/**
 * A listener to reset the channel value after a timeout.
 *
 * @author Jochen Klein - Initial contribution
 */
@NonNullByDefault
public class ExpireUpdateStateListener extends ChannelStateUpdateListenerProxy {

    private final int expireAfter;
    private final Value value;
    private final AvailabilityTracker tracker;
    private final ScheduledExecutorService scheduler;

    private final AtomicReference<@Nullable ScheduledFuture<?>> expire = new AtomicReference<>();

    public ExpireUpdateStateListener(ChannelStateUpdateListener original, int expireAfter, Value value,
            AvailabilityTracker tracker, ScheduledExecutorService scheduler) {
        super(original);
        this.expireAfter = expireAfter;
        this.value = value;
        this.tracker = tracker;
        this.scheduler = scheduler;
    }

    @Override
    public void updateChannelState(final ChannelUID channelUID, State state) {
        super.updateChannelState(channelUID, state);

        ScheduledFuture<?> oldExpire = expire.getAndSet(scheduler.schedule(() -> {
            value.resetState();
            tracker.resetMessageReceived();
            ExpireUpdateStateListener.super.updateChannelState(channelUID, value.getChannelState());
        }, expireAfter, TimeUnit.SECONDS));

        if (oldExpire != null) {
            oldExpire.cancel(false);
        }
    }
}
