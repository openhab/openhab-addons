package org.openhab.binding.lgtvserial.internal.protocol.serial.commands;

public class PowerSavingCommand extends BaseStringCommand {

    protected PowerSavingCommand(int setId) {
        super('f', 'l', setId);
    }

}
