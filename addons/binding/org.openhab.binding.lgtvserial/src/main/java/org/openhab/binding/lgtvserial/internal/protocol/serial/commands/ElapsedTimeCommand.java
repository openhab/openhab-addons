package org.openhab.binding.lgtvserial.internal.protocol.serial.commands;

public class ElapsedTimeCommand extends BaseDecimalCommand {

    protected ElapsedTimeCommand(int setId) {
        super('d', 'l', setId, false);
    }

}
