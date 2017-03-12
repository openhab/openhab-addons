package org.openhab.binding.lgtvserial.internal.protocol.serial.commands;

public class DPMCommand extends BaseOnOffCommand {

    protected DPMCommand(int setId) {
        super('f', 'j', setId);
    }

}
