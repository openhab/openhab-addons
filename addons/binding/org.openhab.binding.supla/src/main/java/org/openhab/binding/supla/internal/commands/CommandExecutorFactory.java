package org.openhab.binding.supla.internal.commands;

import org.eclipse.smarthome.core.thing.ChannelUID;

import java.util.Optional;

public interface CommandExecutorFactory {
    Optional<CommandExecutor> findCommand(ChannelUID channelUID);
}
