package org.openhab.binding.lgtvserial.internal.protocol.serial.commands;

public class SoftwareVersionCommand extends BaseStringCommand {

    protected SoftwareVersionCommand(int setId) {
        super('f', 'z', setId);
    }

}
