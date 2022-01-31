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
package org.openhab.binding.mqtt.homeassistant.internal;

import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mqtt.generic.ChannelConfig;
import org.openhab.binding.mqtt.generic.ChannelState;
import org.openhab.binding.mqtt.generic.ChannelStateUpdateListener;
import org.openhab.binding.mqtt.generic.values.Value;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extended {@link ChannelState} with added filter for {@link #publishValue(Command)}
 *
 * @author Anton Kharuzhy - Initial contribution
 */
@NonNullByDefault
public class HomeAssistantChannelState extends ChannelState {
    private final Logger logger = LoggerFactory.getLogger(HomeAssistantChannelState.class);
    private final @Nullable Predicate<Command> commandFilter;

    /**
     * Creates a new channel state.
     *
     * @param config The channel configuration
     * @param channelUID The channelUID is used for the {@link ChannelStateUpdateListener} to notify about value changes
     * @param cachedValue MQTT only notifies us once about a value, during the subscribe. The channel state therefore
     *            needs a cache for the current value.
     * @param channelStateUpdateListener A channel state update listener
     * @param commandFilter A filter for commands, on <code>true</code> command will be published, on
     *            <code>false</code> ignored. Can be <code>null</code> to publish all commands.
     */
    public HomeAssistantChannelState(ChannelConfig config, ChannelUID channelUID, Value cachedValue,
            @Nullable ChannelStateUpdateListener channelStateUpdateListener,
            @Nullable Predicate<Command> commandFilter) {
        super(config, channelUID, cachedValue, channelStateUpdateListener);
        this.commandFilter = commandFilter;
    }

    @Override
    public CompletableFuture<Boolean> publishValue(Command command) {
        if (commandFilter != null && !commandFilter.test(command)) {
            logger.trace("Channel {} updates are disabled by command filter, ignoring command {}", channelUID, command);
            return CompletableFuture.completedFuture(false);
        }
        return super.publishValue(command);
    }
}
