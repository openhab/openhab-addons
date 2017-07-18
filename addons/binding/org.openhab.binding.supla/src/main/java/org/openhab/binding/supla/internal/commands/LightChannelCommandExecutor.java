package org.openhab.binding.supla.internal.commands;

import org.openhab.binding.supla.internal.api.ChannelManager;
import org.openhab.binding.supla.internal.supla.entities.SuplaChannel;

final class LightChannelCommandExecutor extends SwitchChannelCommandExecutor implements CommandExecutor {
    LightChannelCommandExecutor(ChannelManager channelManager, SuplaChannel suplaChannel) {
        super(channelManager, suplaChannel);
    }
}
