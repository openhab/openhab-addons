package org.openhab.binding.lgtvserial.internal.protocol.serial.commands;

public class PictureModeCommand extends BaseStringCommand {

    protected PictureModeCommand(int setId) {
        super('d', 'x', setId);
    }

}
