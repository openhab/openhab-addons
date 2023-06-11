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
package org.openhab.binding.mqtt.homeassistant.internal.listener;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mqtt.generic.ChannelStateUpdateListener;
import org.openhab.binding.mqtt.generic.values.Value;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.State;

/**
 * A listener to set the binary sensor value to 'off' after a timeout.
 *
 * @author Jochen Klein - Initial contribution
 */
@NonNullByDefault
public class OffDelayUpdateStateListener extends ChannelStateUpdateListenerProxy {

    private final int offDelay;
    private final Value value;
    private final ScheduledExecutorService scheduler;

    private final AtomicReference<@Nullable ScheduledFuture<?>> delay = new AtomicReference<>();

    public OffDelayUpdateStateListener(ChannelStateUpdateListener original, int offDelay, Value value,
            ScheduledExecutorService scheduler) {
        super(original);
        this.offDelay = offDelay;
        this.value = value;
        this.scheduler = scheduler;
    }

    @Override
    public void updateChannelState(final ChannelUID channelUID, State state) {
        super.updateChannelState(channelUID, state);

        ScheduledFuture<?> newDelay = null;

        if (OnOffType.ON == state) {
            newDelay = scheduler.schedule(() -> {
                value.update(OnOffType.OFF);
                OffDelayUpdateStateListener.super.updateChannelState(channelUID, value.getChannelState());
            }, offDelay, TimeUnit.SECONDS);
        }

        ScheduledFuture<?> oldDelay = delay.getAndSet(newDelay);
        if (oldDelay != null) {
            oldDelay.cancel(false);
        }
    }
}
