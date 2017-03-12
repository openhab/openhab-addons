package org.openhab.binding.lgtvserial.internal.protocol.serial.commands;

public class AspectRatioCommand extends BaseStringCommand {

    protected AspectRatioCommand(int setId) {
        super('k', 'c', setId);
    }

}
