package org.openhab.binding.lgtvserial.internal.protocol.serial.commands;

public class OSDSelectCommand extends BaseOnOffCommand {

    protected OSDSelectCommand(int setId) {
        super('k', 'l', setId);
    }

}
