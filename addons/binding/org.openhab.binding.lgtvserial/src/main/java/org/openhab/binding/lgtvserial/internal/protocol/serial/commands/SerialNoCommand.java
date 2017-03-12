package org.openhab.binding.lgtvserial.internal.protocol.serial.commands;

public class SerialNoCommand extends BaseStringCommand {

    protected SerialNoCommand(int setId) {
        super('f', 'y', setId);
    }

}
