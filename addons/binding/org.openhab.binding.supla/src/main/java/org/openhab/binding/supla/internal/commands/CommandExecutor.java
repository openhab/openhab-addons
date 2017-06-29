package org.openhab.binding.supla.internal.commands;

import org.eclipse.smarthome.core.types.Command;

public interface CommandExecutor {
    void execute(Command command);
}
