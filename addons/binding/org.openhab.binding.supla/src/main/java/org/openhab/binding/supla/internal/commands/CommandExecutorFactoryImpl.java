package org.openhab.binding.supla.internal.commands;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.openhab.binding.supla.internal.api.ChannelManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.openhab.binding.supla.SuplaBindingConstants.LIGHT_CHANNEL_ID;
import static org.openhab.binding.supla.SuplaBindingConstants.SWITCH_CHANNEL_ID;

public class CommandExecutorFactoryImpl implements CommandExecutorFactory {
    private final Logger logger = LoggerFactory.getLogger(CommandExecutorFactoryImpl.class);
    private final ChannelManager channelManager;

    public CommandExecutorFactoryImpl(ChannelManager channelManager) {
        this.channelManager = checkNotNull(channelManager);
    }

    @Override
    public Optional<CommandExecutor> findCommand(ChannelUID channelUID) {
        long channelId = -1; // TODO
        final String id = channelUID.getId();
        if (LIGHT_CHANNEL_ID.equals(id)) {
            return Optional.of(new LightChannelCommandExecutor(channelManager, channelId));
        } else if (SWITCH_CHANNEL_ID.equals(id)) {
            return Optional.of(new SwitchChannelCommandExecutor(channelManager, channelId));
        } else {
            logger.debug("Don't know how to handle channel " + id);
            return Optional.empty();
        }
    }
}
