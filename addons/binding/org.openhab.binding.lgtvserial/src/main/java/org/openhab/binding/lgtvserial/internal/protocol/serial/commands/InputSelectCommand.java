package org.openhab.binding.lgtvserial.internal.protocol.serial.commands;

public class InputSelectCommand extends BaseStringCommand {

    protected InputSelectCommand(int setId) {
        super('k', 'b', setId);
    }

}
