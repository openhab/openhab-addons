package org.openhab.binding.lgtvserial.internal.protocol.serial.commands;

public class VSizeCommand extends BasePercentCommand {

    protected VSizeCommand(int setId) {
        super('f', 't', setId);
    }

}
