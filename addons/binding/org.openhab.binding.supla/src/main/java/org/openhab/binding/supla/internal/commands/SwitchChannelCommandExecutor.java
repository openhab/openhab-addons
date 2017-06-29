package org.openhab.binding.supla.internal.commands;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class SwitchChannelCommandExecutor implements CommandExecutor {
    private final Logger logger = LoggerFactory.getLogger(SwitchChannelCommandExecutor.class);

    @Override
    public void execute(Command command) {
        if (command instanceof OnOffType) {
            // TODO do on/off
        } else if (command instanceof RefreshType) {
            // TODO do refresh for channel
        } else {
            logger.debug("Don't know how to handle {} for {}", command.getClass().getSimpleName(), this.getClass().getSimpleName());
        }
    }
}
