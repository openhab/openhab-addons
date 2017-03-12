package org.openhab.binding.lgtvserial.internal.protocol.serial.commands;

public class NaturalModeCommand extends BaseOnOffCommand {

    protected NaturalModeCommand(int setId) {
        super('d', 'j', setId);
    }

}
