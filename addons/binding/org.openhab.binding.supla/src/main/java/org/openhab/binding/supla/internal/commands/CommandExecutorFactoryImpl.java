package org.openhab.binding.supla.internal.commands;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static org.openhab.binding.supla.SuplaBindingConstants.LIGHT_CHANNEL_ID;
import static org.openhab.binding.supla.SuplaBindingConstants.SWITCH_CHANNEL_ID;

public class CommandExecutorFactoryImpl implements CommandExecutorFactory {
    private final Logger logger = LoggerFactory.getLogger(CommandExecutorFactoryImpl.class);

    @Override
    public Optional<CommandExecutor> findCommand(ChannelUID channelUID) {
        final String id = channelUID.getId();
        if (LIGHT_CHANNEL_ID.equals(id)) {
            return Optional.of(new LightChannelCommandExecutor());
        } else if (SWITCH_CHANNEL_ID.equals(id)) {
            return Optional.of(new SwitchChannelCommandExecutor());
        } else {
            logger.debug("Don't know how to handle channel " + id);
            return Optional.empty();
        }
    }
}
