package org.openhab.binding.lgtvserial.internal.protocol.serial.commands;

public class HPositionCommand extends BasePercentCommand {

    protected HPositionCommand(int setId) {
        super('f', 'q', setId);
    }

}
