package org.openhab.binding.lgtvserial.internal.protocol.serial.commands;

public class VPositionCommand extends BasePercentCommand {

    protected VPositionCommand(int setId) {
        super('f', 'r', setId);
    }

}
