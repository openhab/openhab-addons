package org.openhab.binding.lgtvserial.internal.protocol.serial.commands;

public class VolumeMuteCommand extends BaseOnOffCommand {

    protected VolumeMuteCommand(int setId) {
        super('k', 'e', setId);
    }

}
