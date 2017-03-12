package org.openhab.binding.lgtvserial.internal.protocol.serial.commands;

public class ISMMethodCommand extends BaseStringCommand {

    protected ISMMethodCommand(int setId) {
        super('j', 'p', setId);
    }

}
