package org.openhab.binding.supla.internal.commands;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.openhab.binding.supla.internal.supla.entities.SuplaChannel;

import java.util.Optional;

public interface CommandExecutorFactory {
    Optional<CommandExecutor> findCommand(SuplaChannel suplaChannel, ChannelUID channelUID);
}
