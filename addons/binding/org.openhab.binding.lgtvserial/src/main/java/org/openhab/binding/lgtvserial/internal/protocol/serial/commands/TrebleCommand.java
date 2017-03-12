package org.openhab.binding.lgtvserial.internal.protocol.serial.commands;

public class TrebleCommand extends BasePercentCommand {

    protected TrebleCommand(int setId) {
        super('k', 'r', setId);
    }

}
