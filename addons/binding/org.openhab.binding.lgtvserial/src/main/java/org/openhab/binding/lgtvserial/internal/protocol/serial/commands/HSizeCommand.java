package org.openhab.binding.lgtvserial.internal.protocol.serial.commands;

public class HSizeCommand extends BasePercentCommand {

    protected HSizeCommand(int setId) {
        super('f', 's', setId);
    }

}
