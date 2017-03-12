package org.openhab.binding.lgtvserial.internal.protocol.serial.commands;

public class ContrastCommand extends BasePercentCommand {

    protected ContrastCommand(int setId) {
        super('k', 'g', setId);
    }

}
