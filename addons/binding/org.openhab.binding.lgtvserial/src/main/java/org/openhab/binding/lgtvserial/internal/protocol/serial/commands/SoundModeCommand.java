package org.openhab.binding.lgtvserial.internal.protocol.serial.commands;

public class SoundModeCommand extends BaseStringCommand {

    protected SoundModeCommand(int setId) {
        super('d', 'y', setId);
    }

}
