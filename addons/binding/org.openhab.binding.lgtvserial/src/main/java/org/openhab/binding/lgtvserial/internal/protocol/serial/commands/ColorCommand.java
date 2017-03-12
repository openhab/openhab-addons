package org.openhab.binding.lgtvserial.internal.protocol.serial.commands;

public class ColorCommand extends BasePercentCommand {

    protected ColorCommand(int setId) {
        super('k', 'i', setId);
    }

}
