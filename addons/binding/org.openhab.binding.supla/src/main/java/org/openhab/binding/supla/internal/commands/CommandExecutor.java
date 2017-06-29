package org.openhab.binding.supla.internal.commands;

import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;

import java.util.function.Consumer;

public interface CommandExecutor {
    void execute(Consumer<? super State> updateState, Command command);
}
