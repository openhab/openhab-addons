/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.supla.internal.commands;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.openhab.binding.supla.internal.api.ChannelManager;
import org.openhab.binding.supla.internal.supla.entities.SuplaChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.openhab.binding.supla.SuplaBindingConstants.LIGHT_CHANNEL_ID;
import static org.openhab.binding.supla.SuplaBindingConstants.SWITCH_CHANNEL_ID;

public final class CommandExecutorFactoryImpl implements CommandExecutorFactory {
    private final Logger logger = LoggerFactory.getLogger(CommandExecutorFactoryImpl.class);
    private final ChannelManager channelManager;

    public CommandExecutorFactoryImpl(ChannelManager channelManager) {
        this.channelManager = checkNotNull(channelManager);
    }

    @Override
    public Optional<CommandExecutor> findCommand(SuplaChannel suplaChannel, ChannelUID channelUID) {
        final String id = channelUID.getId();
        if (LIGHT_CHANNEL_ID.equals(id)) {
            return Optional.of(new LightChannelCommandExecutor(channelManager, suplaChannel));
        } else if (SWITCH_CHANNEL_ID.equals(id)) {
            return Optional.of(new SwitchChannelCommandExecutor(channelManager, suplaChannel));
        } else {
            logger.debug("Don't know how to handle channel {}", id);
            return Optional.empty();
        }
    }
}
