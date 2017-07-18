package org.openhab.binding.supla.internal.commands;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.supla.internal.api.ChannelManager;
import org.openhab.binding.supla.internal.supla.entities.SuplaChannel;
import org.openhab.binding.supla.internal.supla.entities.SuplaChannelStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.eclipse.smarthome.core.library.types.OnOffType.OFF;
import static org.eclipse.smarthome.core.library.types.OnOffType.ON;

class SwitchChannelCommandExecutor implements CommandExecutor {
    private final Logger logger = LoggerFactory.getLogger(SwitchChannelCommandExecutor.class);
    private final ChannelManager channelManager;
    private final SuplaChannel suplaChannel;

    SwitchChannelCommandExecutor(ChannelManager channelManager, SuplaChannel suplaChannel) {
        this.channelManager = checkNotNull(channelManager);
        this.suplaChannel = suplaChannel;
    }

    @Override
    public void execute(Consumer<? super State> updateState, Command command) {
        if (command instanceof OnOffType) {
            final OnOffType switchCommand = (OnOffType) command;
            if(switchCommand == ON) {
                final boolean turnOn = channelManager.turnOn(suplaChannel);
                if(!turnOn) {
                    updateState.accept(OFF);
                }
            } else {
                final boolean turnOff = channelManager.turnOff(suplaChannel);
                if(!turnOff) {
                    updateState.accept(ON);
                }
            }
        } else if (command instanceof RefreshType) {
            channelManager.obtainChannelStatus(suplaChannel)
                    .map(this::getState)
                    .ifPresent(updateState);
        } else {
            logger.debug("Don't know how to handle {} for {}", command.getClass().getSimpleName(), this.getClass().getSimpleName());
        }
    }

    private OnOffType getState(SuplaChannelStatus status) {
        return status.isEnabled() ? ON : OFF;
    }
}
